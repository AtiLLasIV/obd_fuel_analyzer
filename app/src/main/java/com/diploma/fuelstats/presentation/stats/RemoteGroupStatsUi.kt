package com.diploma.fuelstats.presentation.stats

data class RemoteGroupStatsUi(
    val title: String,
    val manualAvgConsumptionText: String,
    val obdAvgConsumptionText: String,
    val avgRefuelLitersText: String,
    val avgDistanceBetweenRefuelsText: String,
    val vehiclesCountText: String
)