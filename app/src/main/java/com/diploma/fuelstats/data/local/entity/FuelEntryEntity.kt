package com.diploma.fuelstats.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_entries")
data class FuelEntryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val carId: Long,

    val odometerKm: Int,

    val litersAdded: Double,

    val isFullTank: Boolean,

    val timestampMillis: Long
)