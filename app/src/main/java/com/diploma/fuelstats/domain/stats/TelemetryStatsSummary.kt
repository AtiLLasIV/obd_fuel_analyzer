package com.diploma.fuelstats.domain.stats

data class TelemetryStatsSummary(
    val samplesCount: Int,
    val averageSpeedKmh: Double?,
    val averageRpm: Double?,
    val averageCoolantTempC: Double?,
    val averageAmbientTempC: Double?
)