package com.diploma.fuelstats.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, // пока одна машина и всегда 1L

    val brand: String,
    val vehicleType: String,
    val syncVehicleId: String,
    val model: String,
    val year: Int,
    val tankCapacityLiters: Double?,
    val currentOdometerKm: Int
)