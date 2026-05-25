package com.diploma.fuelstats.domain.model

data class Car(
    val id: Long,
    val brand: String,
    val vehicleType: String,
    val syncVehicleId: String,
    val model: String,
    val year: Int,
    val tankCapacityLiters: Double? = null,
    val currentOdometerKm: Int
)