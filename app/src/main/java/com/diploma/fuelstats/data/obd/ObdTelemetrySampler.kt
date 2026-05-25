package com.diploma.fuelstats.data.obd

import com.diploma.fuelstats.domain.repositories.TelemetryRepository
import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.obd.ObdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ObdTelemetrySampler(
    private val obdClient: ObdClient,
    private val telemetryRepository: TelemetryRepository,
    private val externalScope: CoroutineScope,
    private val refuelDetector: RefuelDetector? = null,
    private val onRefuelDetected: (() -> Unit)? = null,
    private val samplingIntervalMillis: Long = 5_000L // раз в 5 секунд опрос
) {

    private var samplingJob: Job? = null

    fun startSampling(carId: Long) {
        if (samplingJob?.isActive == true) return

        samplingJob = externalScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (obdClient.isConnected.value) {
                    val sample = TelemetrySample(
                        id = 0L,
                        carId = carId,
                        timestampMillis = System.currentTimeMillis(),
                        speedKmh = obdClient.getSpeedKmh(),
                        rpm = obdClient.getRpm(),
                        fuelLevelPercent = obdClient.getFuelLevelPercent(),
                        coolantTempC = obdClient.getCoolantTempC(),
                        ambientTempC = obdClient.getAmbientTempC(),
                        mafGramsPerSec = obdClient.getMafGramsPerSec(),
                    )

                    telemetryRepository.addSample(sample)

                    // детекция заправки
                    if (refuelDetector?.onSample(sample) == true) {
                        onRefuelDetected?.invoke()
                    }
                }

                delay(samplingIntervalMillis)
            }
        }
    }

    fun stopSampling() {
        samplingJob?.cancel()
        samplingJob = null
    }

    fun isSampling(): Boolean {
        return samplingJob?.isActive == true
    }
}