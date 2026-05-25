package com.diploma.fuelstats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diploma.fuelstats.data.local.entity.TelemetrySampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetrySampleDao {

    @Insert
    suspend fun insert(sample: TelemetrySampleEntity)

    @Insert
    suspend fun insertAll(samples: List<TelemetrySampleEntity>)

    @Query("""
        SELECT * FROM telemetry_samples
        WHERE carId = :carId
        ORDER BY timestampMillis ASC
    """)
    suspend fun getSamplesForCar(carId: Long): List<TelemetrySampleEntity>

    @Query("""
    SELECT * FROM telemetry_samples
    WHERE carId = :carId
    ORDER BY timestampMillis ASC
    """)
    fun observeSamplesForCar(carId: Long): Flow<List<TelemetrySampleEntity>>

    @Query("""
        SELECT * FROM telemetry_samples
        WHERE carId = :carId
          AND timestampMillis BETWEEN :fromMillis AND :toMillis
        ORDER BY timestampMillis ASC
    """)
    suspend fun getSamplesForCarBetween(
        carId: Long,
        fromMillis: Long,
        toMillis: Long
    ): List<TelemetrySampleEntity>

    @Query("DELETE FROM telemetry_samples WHERE carId = :carId")
    suspend fun deleteSamplesForCar(carId: Long)

    @Query("DELETE FROM telemetry_samples")
    suspend fun deleteAll()
}