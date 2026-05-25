package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.data.local.dao.CarDao
import com.diploma.fuelstats.data.local.mappers.toDomain
import com.diploma.fuelstats.data.local.mappers.toEntity
import com.diploma.fuelstats.domain.model.Car
import com.diploma.fuelstats.domain.repositories.CarLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomCarLocalDataSource(
    private val carDao: CarDao
) : CarLocalDataSource {

    override suspend fun getCurrentCar(): Car? {
        return withContext(Dispatchers.IO) {
            carDao.getCurrentCar()?.toDomain()
        }
    }

    override suspend fun saveCar(car: Car) {
        withContext(Dispatchers.IO) {
            carDao.insert(car.toEntity())
        }
    }
}