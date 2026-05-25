package com.diploma.fuelstats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diploma.fuelstats.data.local.entity.FuelEntryEntity

@Dao
interface FuelEntryDao {

    @Insert
    suspend fun insert(entry: FuelEntryEntity)

    @Query("""
    SELECT * FROM fuel_entries 
    WHERE carId = :carId 
    ORDER BY timestampMillis DESC
    """)
    suspend fun getEntriesForCar(carId: Long): List<FuelEntryEntity>

    @Query("DELETE FROM fuel_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}