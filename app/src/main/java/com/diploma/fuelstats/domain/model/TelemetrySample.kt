package com.diploma.fuelstats.domain.model

data class TelemetrySample(
    val id: Long,
    val carId: Long,
    val timestampMillis: Long,
    val speedKmh: Double?,
    val rpm: Double?,
    val fuelLevelPercent: Double?,
    val coolantTempC: Double?,
    val ambientTempC: Double?,
    val mafGramsPerSec: Double?
)