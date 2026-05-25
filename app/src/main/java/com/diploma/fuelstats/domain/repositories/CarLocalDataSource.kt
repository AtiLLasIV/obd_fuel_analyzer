package com.diploma.fuelstats.domain.repositories

import com.diploma.fuelstats.domain.model.Car

interface CarLocalDataSource {

    suspend fun getCurrentCar(): Car?

    suspend fun saveCar(car: Car)
}