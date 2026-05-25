package com.diploma.fuelstats.domain.stats.charts.model

data class InstantConsumptionPoint(
    val timestampMillis: Long,
    val consumptionLPer100Km: Double
)
