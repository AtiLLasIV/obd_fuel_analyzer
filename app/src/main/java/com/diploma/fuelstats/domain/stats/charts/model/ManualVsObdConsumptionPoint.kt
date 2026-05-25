package com.diploma.fuelstats.domain.stats.charts.model

data class ManualVsObdConsumptionPoint(
    val label: String,
    val manualConsumptionLPer100Km: Double,
    val obdConsumptionLPer100Km: Double
)