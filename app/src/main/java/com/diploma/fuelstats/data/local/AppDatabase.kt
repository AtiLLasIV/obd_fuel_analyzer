package com.diploma.fuelstats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diploma.fuelstats.data.local.dao.CarDao
import com.diploma.fuelstats.data.local.dao.FuelEntryDao
import com.diploma.fuelstats.data.local.dao.TelemetrySampleDao
import com.diploma.fuelstats.data.local.entity.CarEntity
import com.diploma.fuelstats.data.local.entity.FuelEntryEntity
import com.diploma.fuelstats.data.local.entity.TelemetrySampleEntity

@Database(
    entities = [
        FuelEntryEntity::class,
        CarEntity::class,
        TelemetrySampleEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun carDao(): CarDao
    abstract fun telemetrySampleDao(): TelemetrySampleDao
}