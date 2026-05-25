package com.diploma.fuelstats.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.fuelstats.data.local.auth.AuthSessionStorage
import com.diploma.fuelstats.data.remote.stats.GroupStatsResponseDto
import com.diploma.fuelstats.data.remote.stats.ServerStatsRequestBuilder
import com.diploma.fuelstats.domain.repositories.CarLocalDataSource
import com.diploma.fuelstats.domain.repositories.FuelRepository
import com.diploma.fuelstats.data.repository.RemoteStatsRepository
import com.diploma.fuelstats.domain.repositories.TelemetryRepository
import com.diploma.fuelstats.di.ServiceLocator
import com.diploma.fuelstats.domain.model.Car
import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.StatsCalculator
import com.diploma.fuelstats.domain.stats.TelemetryStatsCalculator
import com.diploma.fuelstats.domain.stats.charts.AmbientConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.ConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.CoolantConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.FuelLevelChartCalculator
import com.diploma.fuelstats.domain.stats.charts.ManualVsObdConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.RpmConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.SpeedConsumptionChartCalculator
import com.diploma.fuelstats.domain.stats.charts.TelemetryConsumptionCalculator
import com.diploma.fuelstats.presentation.car.VehicleTypeUi
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.views.cartesian.data.columnSeries
import com.patrykandpatrick.vico.views.cartesian.data.lineSeries
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class StatsViewModel(
    private val carLocalDataSource: CarLocalDataSource,
    private val fuelRepository: FuelRepository,
    private val telemetryRepository: TelemetryRepository,
    private val remoteStatsRepository: RemoteStatsRepository,
    private val authSessionStorage: AuthSessionStorage,
) : ViewModel() {

    companion object {
        private const val TAG = "StatsVM"

        val ConsumptionIntervalLabelsKey = ExtraStore.Key<List<String>>()
        val InstantConsumptionLabelsKey = ExtraStore.Key<List<String>>()
        val SpeedConsumptionLabelsKey = ExtraStore.Key<List<String>>()
        val RpmConsumptionLabelsKey = ExtraStore.Key<List<String>>()
        val AmbientConsumptionLabelsKey = ExtraStore.Key<List<String>>()
        val CoolantConsumptionLabelsKey = ExtraStore.Key<List<String>>()
        val FuelLevelLabelsKey = ExtraStore.Key<List<String>>()
        val RefuelMarkersXKey = ExtraStore.Key<List<Double>>()
        val ManualVsObdLabelsKey = ExtraStore.Key<List<String>>()
    }

    val consumptionChartModelProducer = CartesianChartModelProducer()
    val instantConsumptionChartModelProducer = CartesianChartModelProducer()
    val speedConsumptionChartModelProducer = CartesianChartModelProducer()
    val rpmConsumptionChartModelProducer = CartesianChartModelProducer()
    val ambientConsumptionChartModelProducer = CartesianChartModelProducer()
    val coolantConsumptionChartModelProducer = CartesianChartModelProducer()
    val fuelLevelChartModelProducer = CartesianChartModelProducer()
    val manualVsObdChartModelProducer = CartesianChartModelProducer()

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var observeStatsJob: Job? = null
    private var serverSyncJob: Job? = null

    private data class ServerStatsSyncResult(
        val modelStats: RemoteGroupStatsUi?,
        val typeStats: RemoteGroupStatsUi?,
        val message: String?
    )

    fun loadStats() {

        observeStatsJob?.cancel()
        serverSyncJob?.cancel()

        observeStatsJob = viewModelScope.launch {
            val car = carLocalDataSource.getCurrentCar()

            if (car == null) {
                _uiState.value = StatsUiState(
                    totalDistanceText = "нет автомобиля",
                    averageConsumptionText = "нет автомобиля",
                    lastIntervalConsumptionText = "нет автомобиля",
                    averageSpeedText = "нет автомобиля",
                    averageRpmText = "нет автомобиля",
                    averageCoolantTempText = "нет автомобиля",
                    averageAmbientTempText = "нет автомобиля",
                    currentSpeedText = "нет автомобиля",
                    currentRpmText = "нет автомобиля",
                    currentCoolantTempText = "нет автомобиля",
                    currentAmbientTempText = "нет автомобиля",
                    currentFuelLevelText = "нет автомобиля",
                    currentInstantConsumptionText = "нет автомобиля",
                    isAuthorized = authSessionStorage.isAuthorized(),
                    serverStatsMessage = "Сначала создайте профиль автомобиля.",
                    isLoading = false
                )
                return@launch
            }

            ServiceLocator.currentCar = car

            val entries = fuelRepository.getEntries(car.id)
            val initialSamples = telemetryRepository.getSamplesForCar(car.id)

            rebuildLocalStatsUi(
                car = car,
                entries = entries,
                samples = initialSamples
            )

            serverSyncJob = launch {
                syncStatsWithServerOnceAndUpdateUi(
                    car = car,
                    entries = entries,
                    samples = initialSamples
                )
            }

            telemetryRepository.observeSamplesForCar(car.id)
                .collectLatest { samples ->
                    rebuildLocalStatsUi(
                        car = car,
                        entries = entries,
                        samples = samples
                    )
                }
        }
    }

    private suspend fun syncStatsWithServerOnceAndUpdateUi(
        car: Car,
        entries: List<FuelEntry>,
        samples: List<TelemetrySample>
    ) {
        _uiState.update { oldState ->
            oldState.copy(
                isAuthorized = authSessionStorage.isAuthorized(),
                isServerStatsLoading = true
            )
        }

        val result = syncStatsWithServerOnce(
            car = car,
            entries = entries,
            samples = samples
        )

        _uiState.update { oldState ->
            oldState.copy(
                isAuthorized = authSessionStorage.isAuthorized(),
                isServerStatsLoading = false,
                serverStatsMessage = result.message,
                modelStats = result.modelStats,
                typeStats = result.typeStats
            )
        }
    }

    private suspend fun syncStatsWithServerOnce(
        car: Car,
        entries: List<FuelEntry>,
        samples: List<TelemetrySample>
    ): ServerStatsSyncResult {
        if (!authSessionStorage.isAuthorized()) {
            return ServerStatsSyncResult(
                modelStats = null,
                typeStats = null,
                message = "Войдите в аккаунт, чтобы сравнивать статистику с похожими автомобилями."
            )
        }

        if (!canSyncCarStats(car)) {
            return ServerStatsSyncResult(
                modelStats = null,
                typeStats = null,
                message = "Заполните профиль автомобиля, чтобы сравнивать статистику с сервером."
            )
        }

        return try {
            val request = ServerStatsRequestBuilder.build(
                car = car,
                entries = entries,
                samples = samples
            )

            remoteStatsRepository.sendStats(request)

            val modelStats = try {
                remoteStatsRepository.getStatsByModel(
                    brand = car.brand,
                    model = car.model
                ).toUi(
                    title = "Средние значения для ${car.brand} ${car.model}".trim()
                )
            } catch (e: Exception) {
                null
            }

            val typeStats = try {
                remoteStatsRepository.getStatsByType(
                    vehicleType = car.vehicleType
                ).toUi(
                    title = "Средние значения для типа «${VehicleTypeUi.labelOf(car.vehicleType)}»"
                )
            } catch (e: Exception) {
                null
            }

            val message = when {
                modelStats == null && typeStats == null ->
                    "Не удалось загрузить серверную статистику."

                modelStats == null ->
                    "Не удалось загрузить средние значения по модели."

                typeStats == null ->
                    "Не удалось загрузить средние значения по типу автомобиля."

                else -> null
            }

            ServerStatsSyncResult(
                modelStats = modelStats,
                typeStats = typeStats,
                message = message
            )
        } catch (e: Exception) {

            ServerStatsSyncResult(
                modelStats = null,
                typeStats = null,
                message = "Не удалось отправить статистику на сервер."
            )
        }
    }

    private suspend fun rebuildLocalStatsUi(
        car: Car,
        entries: List<FuelEntry>,
        samples: List<TelemetrySample>
    ) {
        val stats = StatsCalculator.calculate(entries)
        val consumptionIntervalPoints = ConsumptionChartCalculator.build(entries)

        val telemetryStats = TelemetryStatsCalculator.calculate(samples)

        val instantConsumptionPoints =
            TelemetryConsumptionCalculator.buildInstantConsumptionPoints(samples)
        val speedConsumptionPoints = SpeedConsumptionChartCalculator.build(samples)
        val rpmConsumptionPoints = RpmConsumptionChartCalculator.build(samples)
        val ambientConsumptionPoints = AmbientConsumptionChartCalculator.build(samples)
        val coolantConsumptionPoints = CoolantConsumptionChartCalculator.build(samples)
        val fuelLevelPoints = FuelLevelChartCalculator.build(samples)
        val manualVsObdConsumptionPoints =
            ManualVsObdConsumptionChartCalculator.build(entries, samples)

        val latestSample = samples.maxByOrNull { it.timestampMillis }
        val latestInstantPoint = instantConsumptionPoints.maxByOrNull { it.timestampMillis }

        val currentSpeedText = latestSample?.speedKmh?.let {
            "${formatOneDigit(it)} км/ч"
        } ?: "нет данных"

        val currentRpmText = latestSample?.rpm?.let {
            "${String.format(Locale.getDefault(), "%.0f", it)} об/мин"
        } ?: "нет данных"

        val currentCoolantTempText = latestSample?.coolantTempC?.let {
            "${formatOneDigit(it)} °C"
        } ?: "нет данных"

        val currentAmbientTempText = latestSample?.ambientTempC?.let {
            "${formatOneDigit(it)} °C"
        } ?: "нет данных"

        val currentFuelLevelText = latestSample?.fuelLevelPercent?.let {
            "${formatOneDigit(it)} %"
        } ?: "нет данных"

        val currentInstantConsumptionText = latestInstantPoint?.consumptionLPer100Km?.let {
            "${formatOneDigit(it)} л / 100 км"
        } ?: "нет данных"

        val hasConsumptionIntervalsChart = consumptionIntervalPoints.isNotEmpty()
        val hasInstantConsumptionChart = instantConsumptionPoints.isNotEmpty()
        val hasSpeedConsumptionChart = speedConsumptionPoints.isNotEmpty()
        val hasRpmConsumptionChart = rpmConsumptionPoints.isNotEmpty()
        val hasAmbientConsumptionChart = ambientConsumptionPoints.isNotEmpty()
        val hasCoolantConsumptionChart = coolantConsumptionPoints.isNotEmpty()
        val hasFuelLevelChart = fuelLevelPoints.isNotEmpty()
        val hasManualVsObdChart = manualVsObdConsumptionPoints.isNotEmpty()

        val instantRefuelMarkerIndices = buildRefuelMarkerIndices(
            entries = entries,
            timestamps = instantConsumptionPoints.map { it.timestampMillis }
        )

        val fuelLevelRefuelMarkerIndices = buildRefuelMarkerIndices(
            entries = entries,
            timestamps = fuelLevelPoints.map { it.timestampMillis }
        )

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        consumptionChartModelProducer.runTransaction {
            if (consumptionIntervalPoints.isNotEmpty()) {
                columnSeries {
                    series(consumptionIntervalPoints.map { it.consumptionLPer100Km })
                }
                extras { store ->
                    store[ConsumptionIntervalLabelsKey] =
                        consumptionIntervalPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                }
                extras { store ->
                    store[ConsumptionIntervalLabelsKey] = listOf("-")
                }
            }
        }

        instantConsumptionChartModelProducer.runTransaction {
            if (instantConsumptionPoints.isNotEmpty()) {
                lineSeries {
                    series(
                        x = instantConsumptionPoints.indices.map { it.toDouble() },
                        y = instantConsumptionPoints.map { it.consumptionLPer100Km }
                    )
                }
                extras { store ->
                    store[InstantConsumptionLabelsKey] =
                        instantConsumptionPoints.map { point ->
                            timeFormat.format(Date(point.timestampMillis))
                        }
                    store[RefuelMarkersXKey] = instantRefuelMarkerIndices
                }
            } else {
                lineSeries {
                    series(
                        x = listOf(0.0),
                        y = listOf(0.0)
                    )
                }
                extras { store ->
                    store[InstantConsumptionLabelsKey] = listOf("-")
                    store[RefuelMarkersXKey] = emptyList()
                }
            }
        }

        fuelLevelChartModelProducer.runTransaction {
            if (fuelLevelPoints.isNotEmpty()) {
                lineSeries {
                    series(
                        x = fuelLevelPoints.indices.map { it.toDouble() },
                        y = fuelLevelPoints.map { it.fuelLevelPercent }
                    )
                }
                extras { store ->
                    store[FuelLevelLabelsKey] =
                        fuelLevelPoints.map { point ->
                            timeFormat.format(Date(point.timestampMillis))
                        }
                    store[RefuelMarkersXKey] = fuelLevelRefuelMarkerIndices
                }
            } else {
                lineSeries {
                    series(
                        x = listOf(0.0),
                        y = listOf(0.0)
                    )
                }
                extras { store ->
                    store[FuelLevelLabelsKey] = listOf("-")
                    store[RefuelMarkersXKey] = emptyList()
                }
            }
        }

        speedConsumptionChartModelProducer.runTransaction {
            if (speedConsumptionPoints.isNotEmpty()) {
                columnSeries {
                    series(speedConsumptionPoints.map { it.averageConsumptionLPer100Km })
                }
                extras { store ->
                    store[SpeedConsumptionLabelsKey] =
                        speedConsumptionPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                }
                extras { store ->
                    store[SpeedConsumptionLabelsKey] = listOf("-")
                }
            }
        }

        rpmConsumptionChartModelProducer.runTransaction {
            if (rpmConsumptionPoints.isNotEmpty()) {
                columnSeries {
                    series(rpmConsumptionPoints.map { it.averageConsumptionLPer100Km })
                }
                extras { store ->
                    store[RpmConsumptionLabelsKey] =
                        rpmConsumptionPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                }
                extras { store ->
                    store[RpmConsumptionLabelsKey] = listOf("-")
                }
            }
        }

        ambientConsumptionChartModelProducer.runTransaction {
            if (ambientConsumptionPoints.isNotEmpty()) {
                columnSeries {
                    series(ambientConsumptionPoints.map { it.averageConsumptionLPer100Km })
                }
                extras { store ->
                    store[AmbientConsumptionLabelsKey] =
                        ambientConsumptionPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                }
                extras { store ->
                    store[AmbientConsumptionLabelsKey] = listOf("-")
                }
            }
        }

        coolantConsumptionChartModelProducer.runTransaction {
            if (coolantConsumptionPoints.isNotEmpty()) {
                columnSeries {
                    series(coolantConsumptionPoints.map { it.averageConsumptionLPer100Km })
                }
                extras { store ->
                    store[CoolantConsumptionLabelsKey] =
                        coolantConsumptionPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                }
                extras { store ->
                    store[CoolantConsumptionLabelsKey] = listOf("-")
                }
            }
        }

        manualVsObdChartModelProducer.runTransaction {
            if (manualVsObdConsumptionPoints.isNotEmpty()) {
                columnSeries {
                    series(manualVsObdConsumptionPoints.map { it.manualConsumptionLPer100Km })
                    series(manualVsObdConsumptionPoints.map { it.obdConsumptionLPer100Km })
                }
                extras { store ->
                    store[ManualVsObdLabelsKey] =
                        manualVsObdConsumptionPoints.map { it.label }
                }
            } else {
                columnSeries {
                    series(listOf(0.0))
                    series(listOf(0.0))
                }
                extras { store ->
                    store[ManualVsObdLabelsKey] = listOf("-")
                }
            }
        }

        val oldState = _uiState.value

        _uiState.value = StatsUiState(
            totalRefuelsText = stats.totalRefuels.toString(),
            totalLitersText = "${formatOneDigit(stats.totalLiters)} л",
            totalDistanceText = if (stats.totalDistanceKm != null) {
                "${stats.totalDistanceKm} км"
            } else {
                "недостаточно данных"
            },
            averageConsumptionText = if (stats.averageConsumptionLPer100Km != null) {
                "${formatOneDigit(stats.averageConsumptionLPer100Km)} л / 100 км"
            } else {
                "недостаточно данных"
            },
            lastIntervalConsumptionText = if (stats.lastIntervalConsumptionLPer100Km != null) {
                "${formatOneDigit(stats.lastIntervalConsumptionLPer100Km)} л / 100 км"
            } else {
                "недостаточно данных"
            },

            telemetrySamplesCountText = telemetryStats.samplesCount.toString(),

            averageSpeedText = if (telemetryStats.averageSpeedKmh != null) {
                "${formatOneDigit(telemetryStats.averageSpeedKmh)} км/ч"
            } else {
                "недостаточно данных"
            },
            averageRpmText = if (telemetryStats.averageRpm != null) {
                "${String.format(Locale.getDefault(), "%.0f", telemetryStats.averageRpm)} об/мин"
            } else {
                "недостаточно данных"
            },
            averageCoolantTempText = if (telemetryStats.averageCoolantTempC != null) {
                "${formatOneDigit(telemetryStats.averageCoolantTempC)} °C"
            } else {
                "недостаточно данных"
            },
            averageAmbientTempText = if (telemetryStats.averageAmbientTempC != null) {
                "${formatOneDigit(telemetryStats.averageAmbientTempC)} °C"
            } else {
                "недостаточно данных"
            },

            currentSpeedText = currentSpeedText,
            currentRpmText = currentRpmText,
            currentCoolantTempText = currentCoolantTempText,
            currentAmbientTempText = currentAmbientTempText,
            currentFuelLevelText = currentFuelLevelText,
            currentInstantConsumptionText = currentInstantConsumptionText,

            hasConsumptionIntervalsChart = hasConsumptionIntervalsChart,
            hasInstantConsumptionChart = hasInstantConsumptionChart,
            hasSpeedConsumptionChart = hasSpeedConsumptionChart,
            hasRpmConsumptionChart = hasRpmConsumptionChart,
            hasAmbientConsumptionChart = hasAmbientConsumptionChart,
            hasCoolantConsumptionChart = hasCoolantConsumptionChart,
            hasFuelLevelChart = hasFuelLevelChart,
            hasManualVsObdChart = hasManualVsObdChart,

            consumptionIntervalPoints = consumptionIntervalPoints,
            instantConsumptionPoints = instantConsumptionPoints,
            speedConsumptionPoints = speedConsumptionPoints,
            rpmConsumptionPoints = rpmConsumptionPoints,
            ambientConsumptionPoints = ambientConsumptionPoints,
            coolantConsumptionPoints = coolantConsumptionPoints,
            fuelLevelPoints = fuelLevelPoints,
            manualVsObdConsumptionPoints = manualVsObdConsumptionPoints,

            isAuthorized = authSessionStorage.isAuthorized(),
            isServerStatsLoading = oldState.isServerStatsLoading,
            serverStatsMessage = oldState.serverStatsMessage,
            modelStats = oldState.modelStats,
            typeStats = oldState.typeStats,

            isLoading = false
        )
    }
    private fun canSyncCarStats(car: Car): Boolean {
        val hasSyncVehicleId = car.syncVehicleId.isNotBlank()
        val hasBrand = car.brand.isNotBlank()
        val hasModel = car.model.isNotBlank()
        val hasVehicleType = car.vehicleType.isNotBlank()

        return hasSyncVehicleId && hasBrand && hasModel && hasVehicleType
    }
    private fun GroupStatsResponseDto.toUi(title: String): RemoteGroupStatsUi {
        return RemoteGroupStatsUi(
            title = title,
            manualAvgConsumptionText = formatNullableConsumption(manualAvgConsumptionLPer100Km),
            obdAvgConsumptionText = formatNullableConsumption(obdAvgConsumptionLPer100Km),
            avgRefuelLitersText = avgRefuelLiters?.let {
                "${formatOneDigit(it)} л"
            } ?: "нет данных",
            avgDistanceBetweenRefuelsText = avgDistanceBetweenRefuelsKm?.let {
                "${formatOneDigit(it)} км"
            } ?: "нет данных",
            vehiclesCountText = vehiclesCount.toString()
        )
    }

    private fun formatNullableConsumption(value: Double?): String {
        return value?.let { "${formatOneDigit(it)} л / 100 км" } ?: "нет данных"
    }

    private fun buildRefuelMarkerIndices(
        entries: List<FuelEntry>,
        timestamps: List<Long>
    ): List<Double> {
        if (entries.isEmpty() || timestamps.isEmpty()) {
            return emptyList()
        }

        val maxAllowedDiffMillis = 15 * 60 * 1000L

        val result = entries.mapNotNull { entry ->
            val entryTimestamp = entry.timestampMillis

            val closestIndex = timestamps.indices.minByOrNull { index ->
                abs(timestamps[index] - entryTimestamp)
            } ?: return@mapNotNull null

            val diffMillis = abs(timestamps[closestIndex] - entryTimestamp)
            if (diffMillis > maxAllowedDiffMillis) {
                return@mapNotNull null
            }

            closestIndex.toDouble()
        }.distinct()

        return result
    }

    private fun formatOneDigit(value: Double): String =
        String.format(Locale.getDefault(), "%.1f", value)
}