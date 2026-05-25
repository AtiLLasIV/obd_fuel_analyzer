package com.diploma.fuelstats.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diploma.fuelstats.data.local.auth.AuthSessionStorage
import com.diploma.fuelstats.domain.repositories.CarLocalDataSource
import com.diploma.fuelstats.domain.repositories.FuelRepository
import com.diploma.fuelstats.data.repository.RemoteStatsRepository
import com.diploma.fuelstats.domain.repositories.TelemetryRepository

class StatsViewModelFactory(
    private val carLocalDataSource: CarLocalDataSource,
    private val fuelRepository: FuelRepository,
    private val telemetryRepository: TelemetryRepository,
    private val remoteStatsRepository: RemoteStatsRepository,
    private val authSessionStorage: AuthSessionStorage,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(
                carLocalDataSource = carLocalDataSource,
                fuelRepository = fuelRepository,
                telemetryRepository = telemetryRepository,
                remoteStatsRepository = remoteStatsRepository,
                authSessionStorage = authSessionStorage,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}