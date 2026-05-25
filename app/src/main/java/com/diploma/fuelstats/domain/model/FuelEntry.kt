package com.diploma.fuelstats.domain.model

data class FuelEntry(
    val id: Long,
    val carId: Long,
    val odometerKm: Int,
    val litersAdded: Double,
    val isFullTank: Boolean,
    val timestampMillis: Long = System.currentTimeMillis()
)