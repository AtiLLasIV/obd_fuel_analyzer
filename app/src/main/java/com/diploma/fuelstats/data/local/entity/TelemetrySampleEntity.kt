package com.diploma.fuelstats.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_samples")
data class TelemetrySampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val carId: Long,
    val timestampMillis: Long,
    val speedKmh: Double?,
    val rpm: Double?,
    val fuelLevelPercent: Double?,
    val coolantTempC: Double?,
    val ambientTempC: Double?,
    val mafGramsPerSec: Double?,
)