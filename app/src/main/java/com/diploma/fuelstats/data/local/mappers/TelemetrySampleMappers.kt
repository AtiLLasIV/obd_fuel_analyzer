package com.diploma.fuelstats.data.local.mappers

import com.diploma.fuelstats.data.local.entity.TelemetrySampleEntity
import com.diploma.fuelstats.domain.model.TelemetrySample

fun TelemetrySampleEntity.toDomain(): TelemetrySample =
    TelemetrySample(
        id = id,
        carId = carId,
        timestampMillis = timestampMillis,
        speedKmh = speedKmh,
        rpm = rpm,
        fuelLevelPercent = fuelLevelPercent,
        coolantTempC = coolantTempC,
        ambientTempC = ambientTempC,
        mafGramsPerSec = mafGramsPerSec,
    )

fun TelemetrySample.toEntity(): TelemetrySampleEntity =
    TelemetrySampleEntity(
        id = id,
        carId = carId,
        timestampMillis = timestampMillis,
        speedKmh = speedKmh,
        rpm = rpm,
        fuelLevelPercent = fuelLevelPercent,
        coolantTempC = coolantTempC,
        ambientTempC = ambientTempC,
        mafGramsPerSec = mafGramsPerSec,
    )