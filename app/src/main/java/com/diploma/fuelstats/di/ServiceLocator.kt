package com.diploma.fuelstats.di

import android.content.Context
import androidx.room.Room
import com.diploma.fuelstats.data.local.AppDatabase
import com.diploma.fuelstats.data.local.auth.AuthSessionStorage
import com.diploma.fuelstats.data.remote.stats.StatsRemoteApi
import com.diploma.fuelstats.domain.repositories.CarLocalDataSource
import com.diploma.fuelstats.domain.repositories.FuelRepository
import com.diploma.fuelstats.data.repository.RemoteStatsRepository
import com.diploma.fuelstats.data.repository.RoomCarLocalDataSource
import com.diploma.fuelstats.data.repository.RoomFuelRepository
import com.diploma.fuelstats.data.repository.RoomTelemetryRepository
import com.diploma.fuelstats.domain.repositories.TelemetryRepository
import com.diploma.fuelstats.domain.model.Car

object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun provideDatabase(context: Context = appContext): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "fuelstats_db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { db -> database = db }
        }
    }

    val fuelRepository: FuelRepository by lazy {
        val db = provideDatabase()
        RoomFuelRepository(
            fuelEntryDao = db.fuelEntryDao()
        )
    }

    val carLocalDataSource: CarLocalDataSource by lazy {
        val db = provideDatabase()
        RoomCarLocalDataSource(
            carDao = db.carDao()
        )
    }

    val telemetryRepository: TelemetryRepository by lazy {
        val db = provideDatabase()
        RoomTelemetryRepository(
            telemetrySampleDao = db.telemetrySampleDao()
        )
    }





    val authSessionStorage by lazy {
        AuthSessionStorage(appContext)
    }

    private val loggingInterceptor by lazy {
        okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authInterceptor by lazy {
        com.diploma.fuelstats.data.remote.auth.AuthInterceptor(authSessionStorage)
    }

    private val okHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // локальный сервер
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    val authApi by lazy {
        retrofit.create(com.diploma.fuelstats.data.remote.auth.AuthApi::class.java)
    }

    val authRepository by lazy {
        com.diploma.fuelstats.data.repository.AuthRepository(authApi)
    }

    val statsRemoteApi: StatsRemoteApi by lazy {
        retrofit.create(StatsRemoteApi::class.java)
    }

    val remoteStatsRepository: RemoteStatsRepository by lazy {
        RemoteStatsRepository(statsRemoteApi)
    }

    var currentCar: Car? = null
}