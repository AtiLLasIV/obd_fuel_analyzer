package com.diploma.fuelstats.domain.repositories

import com.diploma.fuelstats.domain.model.TelemetrySample
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {

    suspend fun addSample(sample: TelemetrySample)

    suspend fun getSamplesForCar(carId: Long): List<TelemetrySample>

    fun observeSamplesForCar(carId: Long): Flow<List<TelemetrySample>>

    suspend fun getSamplesForCarBetween(
        carId: Long,
        fromMillis: Long,
        toMillis: Long
    ): List<TelemetrySample>

    suspend fun deleteSamplesForCar(carId: Long)

    suspend fun deleteAll()
}