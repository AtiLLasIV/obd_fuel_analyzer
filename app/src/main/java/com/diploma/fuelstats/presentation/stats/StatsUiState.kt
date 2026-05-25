package com.diploma.fuelstats.presentation.stats

import com.diploma.fuelstats.domain.stats.charts.model.AmbientConsumptionPoint
import com.diploma.fuelstats.domain.stats.charts.model.ConsumptionIntervalPoint
import com.diploma.fuelstats.domain.stats.charts.model.CoolantConsumptionPoint
import com.diploma.fuelstats.domain.stats.charts.model.FuelLevelPoint
import com.diploma.fuelstats.domain.stats.charts.model.InstantConsumptionPoint
import com.diploma.fuelstats.domain.stats.charts.model.ManualVsObdConsumptionPoint
import com.diploma.fuelstats.domain.stats.charts.model.RpmConsumptionPoint
import com.diploma.fuelstats.domain.stats.charts.model.SpeedConsumptionPoint

data class StatsUiState(
    val totalRefuelsText: String = "",
    val totalLitersText: String = "",
    val totalDistanceText: String = "",
    val averageConsumptionText: String = "",
    val lastIntervalConsumptionText: String = "",
    val telemetrySamplesCountText: String = "",

    val averageSpeedText: String = "",
    val averageRpmText: String = "",
    val averageCoolantTempText: String = "",
    val averageAmbientTempText: String = "",

    val currentSpeedText: String = "",
    val currentRpmText: String = "",
    val currentCoolantTempText: String = "",
    val currentAmbientTempText: String = "",
    val currentFuelLevelText: String = "",
    val currentInstantConsumptionText: String = "",

    val hasConsumptionIntervalsChart: Boolean = false,
    val hasInstantConsumptionChart: Boolean = false,
    val hasSpeedConsumptionChart: Boolean = false,
    val hasRpmConsumptionChart: Boolean = false,
    val hasAmbientConsumptionChart: Boolean = false,
    val hasCoolantConsumptionChart: Boolean = false,
    val hasFuelLevelChart: Boolean = false,
    val hasManualVsObdChart: Boolean = false,


    val consumptionIntervalPoints: List<ConsumptionIntervalPoint> = emptyList(),
    val instantConsumptionPoints: List<InstantConsumptionPoint> = emptyList(),
    val speedConsumptionPoints: List<SpeedConsumptionPoint> = emptyList(),
    val rpmConsumptionPoints: List<RpmConsumptionPoint> = emptyList(),
    val ambientConsumptionPoints: List<AmbientConsumptionPoint> = emptyList(),
    val coolantConsumptionPoints: List<CoolantConsumptionPoint> = emptyList(),
    val fuelLevelPoints: List<FuelLevelPoint> = emptyList(),
    val manualVsObdConsumptionPoints: List<ManualVsObdConsumptionPoint> = emptyList(),

    val isAuthorized: Boolean = false,
    val isServerStatsLoading: Boolean = false,
    val serverStatsMessage: String? = null,

    val modelStats: RemoteGroupStatsUi? = null,
    val typeStats: RemoteGroupStatsUi? = null,

    val isLoading: Boolean = true
)