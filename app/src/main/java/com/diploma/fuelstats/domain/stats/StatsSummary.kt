package com.diploma.fuelstats.domain.stats

data class StatsSummary(
    val totalRefuels: Int,
    val totalLiters: Double,
    val totalDistanceKm: Int?,
    val averageConsumptionLPer100Km: Double?,
    val lastIntervalConsumptionLPer100Km: Double?
)