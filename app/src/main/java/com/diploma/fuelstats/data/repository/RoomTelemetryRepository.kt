package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.data.local.dao.TelemetrySampleDao
import com.diploma.fuelstats.data.local.mappers.toDomain
import com.diploma.fuelstats.data.local.mappers.toEntity
import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.repositories.TelemetryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomTelemetryRepository(
    private val telemetrySampleDao: TelemetrySampleDao
) : TelemetryRepository {

    override suspend fun addSample(sample: TelemetrySample) {
        withContext(Dispatchers.IO) {
            telemetrySampleDao.insert(sample.toEntity())
        }
    }

    override suspend fun getSamplesForCar(carId: Long): List<TelemetrySample> {
        return withContext(Dispatchers.IO) {
            telemetrySampleDao.getSamplesForCar(carId)
                .map { it.toDomain() }
        }
    }

    override fun observeSamplesForCar(carId: Long): Flow<List<TelemetrySample>> {
        return telemetrySampleDao.observeSamplesForCar(carId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getSamplesForCarBetween(
        carId: Long,
        fromMillis: Long,
        toMillis: Long
    ): List<TelemetrySample> {
        return withContext(Dispatchers.IO) {
            telemetrySampleDao.getSamplesForCarBetween(carId, fromMillis, toMillis)
                .map { it.toDomain() }
        }
    }

    override suspend fun deleteSamplesForCar(carId: Long) {
        withContext(Dispatchers.IO) {
            telemetrySampleDao.deleteSamplesForCar(carId)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            telemetrySampleDao.deleteAll()
        }
    }
}
