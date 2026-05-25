package com.diploma.fuelstats.data.local.mappers

import com.diploma.fuelstats.data.local.entity.CarEntity
import com.diploma.fuelstats.domain.model.Car
import kotlin.String

fun CarEntity.toDomain(): Car =
    Car(
        id = id,
        brand = brand,
        model = model,
        vehicleType = vehicleType,
        syncVehicleId = syncVehicleId,
        year = year,
        tankCapacityLiters = tankCapacityLiters,
        currentOdometerKm = currentOdometerKm
    )

fun Car.toEntity(): CarEntity =
    CarEntity(
        id = if (id == 0L) 1L else id,
        brand = brand,
        vehicleType = vehicleType,
        syncVehicleId = syncVehicleId,
        model = model,
        year = year,
        tankCapacityLiters = tankCapacityLiters,
        currentOdometerKm = currentOdometerKm
    )