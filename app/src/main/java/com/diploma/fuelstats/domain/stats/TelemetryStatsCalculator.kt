package com.diploma.fuelstats.domain.stats

import com.diploma.fuelstats.domain.model.TelemetrySample

object TelemetryStatsCalculator {

    fun calculate(samples: List<TelemetrySample>): TelemetryStatsSummary {
        return TelemetryStatsSummary(
            samplesCount = samples.size,
            averageSpeedKmh = averageOfNotNull(samples.map { it.speedKmh }),
            averageRpm = averageOfNotNull(samples.map { it.rpm }),
            averageCoolantTempC = averageOfNotNull(samples.map { it.coolantTempC }),
            averageAmbientTempC = averageOfNotNull(samples.map { it.ambientTempC })
        )
    }

    private fun averageOfNotNull(values: List<Double?>): Double? {
        val nonNullValues = values.filterNotNull()
        if (nonNullValues.isEmpty()) return null
        return nonNullValues.average()
    }
}