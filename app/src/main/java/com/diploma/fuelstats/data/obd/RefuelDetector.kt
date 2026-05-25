package com.diploma.fuelstats.data.obd

import com.diploma.fuelstats.domain.model.TelemetrySample

class RefuelDetector(
    private val speedThresholdKmh: Double = 2.0,
    private val fuelRiseThresholdPercent: Double = 10.0,
    private val minIntervalBetweenAlertsMillis: Long = 30 * 60 * 1000L // 30 мин
) {

    private var baselineFuelPercent: Double? = null
    private var lastAlertTimeMillis: Long = 0L

    fun onSample(sample: TelemetrySample): Boolean {
        val fuel = sample.fuelLevelPercent ?: return false
        val speed = sample.speedKmh ?: return false

        val baseline = baselineFuelPercent
        if (baseline == null) {
            baselineFuelPercent = fuel
            return false
        }

        if (fuel < baseline) {
            baselineFuelPercent = fuel
            return false
        }

        val rise = fuel - baseline
        val now = sample.timestampMillis
        val enoughTimePassed =
            now - lastAlertTimeMillis >= minIntervalBetweenAlertsMillis

        val isRefuel =
            speed < speedThresholdKmh &&
                    rise >= fuelRiseThresholdPercent &&
                    enoughTimePassed

        if (isRefuel) {
            baselineFuelPercent = fuel
            lastAlertTimeMillis = now
            return true
        }

        return false
    }

    fun reset() {
        baselineFuelPercent = null
        lastAlertTimeMillis = 0L
    }
}