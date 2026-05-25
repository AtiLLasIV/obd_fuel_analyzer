package com.diploma.fuelstats.data.repository

import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.repositories.FuelRepository

@Deprecated("Не используется после перехода на Room")
class InMemoryFuelRepository : FuelRepository {
    private val entries = mutableListOf<FuelEntry>()

    override suspend fun getEntries(carId: Long): List<FuelEntry> =
        entries.filter { it.carId == carId }.sortedBy { it.timestampMillis }

    override suspend fun addEntry(entry: FuelEntry) {
        val newId = (entries.maxOfOrNull { it.id } ?: 0L) + 1L
        val entryWithId = entry.copy(id = newId)
        entries.add(entryWithId)
    }

    override suspend fun deleteEntry(id: Long) {
        entries.removeAll { it.id == id }
    }
}