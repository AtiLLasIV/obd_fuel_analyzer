package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.data.local.dao.FuelEntryDao
import com.diploma.fuelstats.data.local.mappers.toDomain
import com.diploma.fuelstats.data.local.mappers.toEntity
import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.repositories.FuelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomFuelRepository(
    private val fuelEntryDao: FuelEntryDao
) : FuelRepository {

    override suspend fun addEntry(entry: FuelEntry) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.insert(entry.toEntity())
        }
    }

    override suspend fun getEntries(carId: Long): List<FuelEntry> {
        return withContext(Dispatchers.IO) {
            fuelEntryDao.getEntriesForCar(carId)
                .map { it.toDomain() }
                .sortedByDescending { it.timestampMillis }
        }
    }

    override suspend fun deleteEntry(id: Long) {
        withContext(Dispatchers.IO) {
            fuelEntryDao.deleteById(id)
        }
    }
}