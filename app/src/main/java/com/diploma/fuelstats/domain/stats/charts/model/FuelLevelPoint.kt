package com.diploma.fuelstats.domain.stats.charts.model

data class FuelLevelPoint(
    val timestampMillis: Long,
    val fuelLevelPercent: Double
)