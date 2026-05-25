package com.diploma.fuelstats.domain.repositories

import com.diploma.fuelstats.domain.model.FuelEntry

interface FuelRepository {
    suspend fun getEntries(carId: Long): List<FuelEntry>
    suspend fun addEntry(entry: FuelEntry)
    suspend fun deleteEntry(id: Long)
}