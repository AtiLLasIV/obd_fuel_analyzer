package com.diploma.obd

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Заглушка [ObdClient] для разработки `app` без реального модуля
 */
class FakeObdClient : ObdClient {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override suspend fun connect(address: String): Boolean {
        if (_isConnected.value) return true
        delay(500L)
        _isConnected.value = true
        return true
    }

    override suspend fun disconnect() {
        _isConnected.value = false
    }

    override suspend fun getSpeedKmh(): Double? = readOrNull {
        Random.nextDouble(0.0, 130.0)
    }

    override suspend fun getRpm(): Double? = readOrNull {
        Random.nextDouble(700.0, 3500.0)
    }

    override suspend fun getFuelLevelPercent(): Double? = readOrNull {
        Random.nextDouble(10.0, 95.0)
    }

    override suspend fun getCoolantTempC(): Double? = readOrNull {
        Random.nextDouble(75.0, 95.0)
    }

    override suspend fun getAmbientTempC(): Double? = readOrNull {
        Random.nextDouble(-25.0, 30.0)
    }

    override suspend fun getIntakeAirTempC(): Double? = readOrNull {
        Random.nextDouble(-5.0, 45.0)
    }

    override suspend fun getEngineLoadPercent(): Double? = readOrNull {
        Random.nextDouble(10.0, 80.0)
    }

    override suspend fun getMafGramsPerSec(): Double? = readOrNull {
        Random.nextDouble(2.0, 25.0)
    }

    override suspend fun getRunTimeSinceEngineStartSec(): Int? = readOrNull {
        Random.nextInt(0, 7200)
    }

    override suspend fun getEngineFuelRateLph(): Double? = readOrNull {
        Random.nextDouble(0.5, 15.0)
    }

    override suspend fun getThrottlePositionPercent(): Double? = readOrNull {
        Random.nextDouble(5.0, 70.0)
    }

    override suspend fun getVin(): String? = readOrNull {
        "WAUZZZ8V9KA012345"
    }

    private suspend inline fun <T> readOrNull(crossinline producer: () -> T): T? {
        if (!_isConnected.value) return null
        delay(Random.nextLong(10L, 60L))
        return producer()
    }
}
