package com.diploma.fuelstats.data.local.mappers

import com.diploma.fuelstats.data.local.entity.FuelEntryEntity
import com.diploma.fuelstats.domain.model.FuelEntry

fun FuelEntryEntity.toDomain(): FuelEntry =
    FuelEntry(
        id = id,
        carId = carId,
        odometerKm = odometerKm,
        litersAdded = litersAdded,
        isFullTank = isFullTank,
        timestampMillis = timestampMillis
    )

fun FuelEntry.toEntity(): FuelEntryEntity =
    FuelEntryEntity(
        id = if (id == 0L) 0L else id,
        carId = carId,
        odometerKm = odometerKm,
        litersAdded = litersAdded,
        isFullTank = isFullTank,
        timestampMillis = timestampMillis
    )