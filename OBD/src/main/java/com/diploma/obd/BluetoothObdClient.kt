package com.diploma.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import com.github.eltonvs.obd.command.ObdProtocols
import com.github.eltonvs.obd.command.Switcher
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SelectProtocolCommand
import com.github.eltonvs.obd.command.at.SetEchoCommand
import com.github.eltonvs.obd.command.at.SetHeadersCommand
import com.github.eltonvs.obd.command.at.SetLineFeedCommand
import com.github.eltonvs.obd.command.at.SetSpacesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.LoadCommand
import com.github.eltonvs.obd.command.engine.MassAirFlowCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.RuntimeCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.engine.ThrottlePositionCommand
import com.github.eltonvs.obd.command.fuel.FuelConsumptionRateCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.command.temperature.AirIntakeTemperatureCommand
import com.github.eltonvs.obd.command.temperature.AmbientAirTemperatureCommand
import com.github.eltonvs.obd.command.temperature.EngineCoolantTemperatureCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.github.eltonvs.obd.command.ObdResponse

class BluetoothObdClient(
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter(),
    private val socketFactory: (String) -> BluetoothSocket? = { address ->
        bluetoothAdapter
            ?.getRemoteDevice(address)
            ?.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
    }
) : ObdClient {

    private val stateMutex = Mutex()
    private val _isConnected = MutableStateFlow(false)

    private var socket: BluetoothSocket? = null
    private var connection: ObdDeviceConnection? = null
    private var connectedAddress: String? = null
    private var cachedVin: String? = null

    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    @SuppressLint("MissingPermission")
    override suspend fun connect(address: String): Boolean = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            if (_isConnected.value && connectedAddress == address && socket?.isConnected == true) {
                return@withLock true
            }

            closeCurrentConnection()

            val newSocket = runCatching { socketFactory(address) }.getOrNull() ?: return@withLock false
            val connected = runCatching {
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery()
                }

                newSocket.connect()

                val newConnection = ObdDeviceConnection(
                    newSocket.inputStream,
                    newSocket.outputStream
                )

                initializeAdapter(newConnection)

                socket = newSocket
                connection = newConnection
                connectedAddress = address
                cachedVin = null
                _isConnected.value = true
                true
            }.getOrElse {
                newSocket.safeClose()
                closeCurrentConnection()
                false
            }

            connected
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            closeCurrentConnection()
        }
    }

    override suspend fun getSpeedKmh(): Double? = readDouble { run(SpeedCommand()) }

    override suspend fun getRpm(): Double? = readDouble { run(RPMCommand()) }

    override suspend fun getFuelLevelPercent(): Double? = readDouble { run(FuelLevelCommand()) }

    override suspend fun getCoolantTempC(): Double? =
        readDouble { run(EngineCoolantTemperatureCommand()) }

    override suspend fun getAmbientTempC(): Double? =
        readDouble { run(AmbientAirTemperatureCommand()) } ?: getIntakeAirTempC()

    override suspend fun getIntakeAirTempC(): Double? =
        readDouble { run(AirIntakeTemperatureCommand()) }

    override suspend fun getEngineLoadPercent(): Double? =
        readDouble { run(LoadCommand()) }

    override suspend fun getMafGramsPerSec(): Double? =
        readDouble { run(MassAirFlowCommand()) }

    override suspend fun getRunTimeSinceEngineStartSec(): Int? =
        withContext(Dispatchers.IO) {
            stateMutex.withLock {
                val currentConnection = connection ?: return@withLock null
                runCatching { currentConnection.run(RuntimeCommand()).value.parseRuntimeSeconds() }
                    .getOrElse {
                        markDisconnectedIfSocketBroken(it)
                        null
                    }
            }
        }

    override suspend fun getEngineFuelRateLph(): Double? =
        readDouble { run(FuelConsumptionRateCommand()) }

    override suspend fun getThrottlePositionPercent(): Double? =
        readDouble { run(ThrottlePositionCommand()) }

    override suspend fun getVin(): String? = withContext(Dispatchers.IO) {
        stateMutex.withLock {
            cachedVin?.let { return@withLock it }

            val currentConnection = connection ?: return@withLock null
            runCatching {
                currentConnection.run(VINCommand(), useCache = true).value.trim().takeIf { it.isNotEmpty() }
            }.getOrNull()?.also { cachedVin = it }
        }
    }

    private suspend fun initializeAdapter(obdConnection: ObdDeviceConnection) {
        obdConnection.run(ResetAdapterCommand(), delayTime = 1_000L)
        obdConnection.run(SetEchoCommand(Switcher.OFF))
        obdConnection.run(SetLineFeedCommand(Switcher.OFF))
        obdConnection.run(SetSpacesCommand(Switcher.OFF))
        obdConnection.run(SetHeadersCommand(Switcher.OFF))
        obdConnection.run(SelectProtocolCommand(ObdProtocols.AUTO), delayTime = 300L)
    }

    private suspend fun readDouble(block: suspend ObdDeviceConnection.() -> ObdResponse): Double? =
        withContext(Dispatchers.IO) {
            stateMutex.withLock {
                val currentConnection = connection ?: return@withLock null
                runCatching { currentConnection.block().value.extractNumber()?.toDoubleOrNull() }
                    .getOrElse {
                        markDisconnectedIfSocketBroken(it)
                        null
                    }
            }
        }

    private fun markDisconnectedIfSocketBroken(error: Throwable) {
        val broken = error is java.io.IOException || socket?.isConnected == false
        if (broken) {
            closeCurrentConnection()
        }
    }

    private fun closeCurrentConnection() {
        connection = null
        socket.safeClose()
        socket = null
        connectedAddress = null
        cachedVin = null
        _isConnected.value = false
    }

    private fun BluetoothSocket?.safeClose() {
        runCatching { this?.close() }
    }

    private fun String.extractNumber(): String? =
        NUMBER_REGEX.find(this.replace(',', '.'))?.value

    private fun String.parseRuntimeSeconds(): Int? {
        val parts = trim().split(':')
        if (parts.size != 3) return extractNumber()?.toIntOrNull()

        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        val seconds = parts[2].toIntOrNull() ?: return null
        return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds
    }

    private companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val NUMBER_REGEX = Regex("""-?\d+(?:\.\d+)?""")
        const val SECONDS_PER_HOUR = 3_600
        const val SECONDS_PER_MINUTE = 60
    }
}
