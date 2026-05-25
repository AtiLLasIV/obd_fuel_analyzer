package com.diploma.fuelstats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diploma.fuelstats.data.local.entity.CarEntity

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: CarEntity)

    @Query("SELECT * FROM cars LIMIT 1")
    suspend fun getCurrentCar(): CarEntity?
}