package com.diploma.fuelstats.domain.stats.charts.model

// одна точка для графика потребления топлива
data class ConsumptionIntervalPoint(
    val label: String,
    val consumptionLPer100Km: Double,
    val distanceKm: Int,
    val litersUsed: Double
)