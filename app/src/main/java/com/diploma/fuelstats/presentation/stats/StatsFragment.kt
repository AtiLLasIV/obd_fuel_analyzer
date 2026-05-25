package com.diploma.fuelstats.presentation.stats

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.diploma.fuelstats.R
import com.diploma.fuelstats.di.ServiceLocator
import com.google.android.material.card.MaterialCardView
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import com.patrykandpatrick.vico.views.cartesian.Scroll
import com.patrykandpatrick.vico.views.cartesian.ScrollHandler
import kotlinx.coroutines.launch

class StatsFragment : Fragment(R.layout.fragment_stats) {

    companion object {
        private const val TAG = "StatsFragment"
    }

    private lateinit var viewModel: StatsViewModel
    private lateinit var chartConsumptionIntervals: CartesianChartView
    private lateinit var chartInstantConsumption: CartesianChartView
    private lateinit var chartSpeedConsumption: CartesianChartView
    private lateinit var chartRpmConsumption: CartesianChartView
    private lateinit var chartAmbientConsumption: CartesianChartView
    private lateinit var chartCoolantConsumption: CartesianChartView
    private lateinit var chartFuelLevel: CartesianChartView
    private lateinit var chartManualVsObdConsumption: CartesianChartView

    private var intervalChartBound = false
    private var instantChartBound = false
    private var speedChartBound = false
    private var rpmChartBound = false
    private var ambientChartBound = false
    private var coolantChartBound = false
    private var fuelLevelChartBound = false
    private var manualVsObdChartBound = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotalRefuels: TextView = view.findViewById(R.id.tvTotalRefuels)
        val tvTotalLiters: TextView = view.findViewById(R.id.tvTotalLiters)
        val tvTotalDistance: TextView = view.findViewById(R.id.tvTotalDistance)
        val tvAverageConsumption: TextView = view.findViewById(R.id.tvAverageConsumption)
        val tvLastIntervalConsumption: TextView = view.findViewById(R.id.tvLastIntervalConsumption)

        val tvTelemetrySamplesCount: TextView = view.findViewById(R.id.tvTelemetrySamplesCount)
        val tvAverageSpeed: TextView = view.findViewById(R.id.tvCurrentSpeed)
        val tvAverageRpm: TextView = view.findViewById(R.id.tvCurrentRpm)
        val tvAverageCoolantTemp: TextView = view.findViewById(R.id.tvCurrentCoolantTemp)
        val tvAverageAmbientTemp: TextView = view.findViewById(R.id.tvCurrentAmbientTemp)

        // ДОБАВЛЕНО: views для серверной статистики
        val tvServerStatsMessage: TextView = view.findViewById(R.id.tvServerStatsMessage)

        val cardModelServerStats: MaterialCardView =
            view.findViewById(R.id.cardModelServerStats)
        val tvModelServerStatsTitle: TextView =
            view.findViewById(R.id.tvModelServerStatsTitle)
        val tvModelServerManualAvg: TextView =
            view.findViewById(R.id.tvModelServerManualAvg)
        val tvModelServerObdAvg: TextView =
            view.findViewById(R.id.tvModelServerObdAvg)
        val tvModelServerAvgRefuel: TextView =
            view.findViewById(R.id.tvModelServerAvgRefuel)
        val tvModelServerAvgDistance: TextView =
            view.findViewById(R.id.tvModelServerAvgDistance)
        val tvModelServerVehiclesCount: TextView =
            view.findViewById(R.id.tvModelServerVehiclesCount)

        val cardTypeServerStats: MaterialCardView =
            view.findViewById(R.id.cardTypeServerStats)
        val tvTypeServerStatsTitle: TextView =
            view.findViewById(R.id.tvTypeServerStatsTitle)
        val tvTypeServerManualAvg: TextView =
            view.findViewById(R.id.tvTypeServerManualAvg)
        val tvTypeServerObdAvg: TextView =
            view.findViewById(R.id.tvTypeServerObdAvg)
        val tvTypeServerAvgRefuel: TextView =
            view.findViewById(R.id.tvTypeServerAvgRefuel)
        val tvTypeServerAvgDistance: TextView =
            view.findViewById(R.id.tvTypeServerAvgDistance)
        val tvTypeServerVehiclesCount: TextView =
            view.findViewById(R.id.tvTypeServerVehiclesCount)

        val cardConsumptionIntervals: MaterialCardView =
            view.findViewById(R.id.cardConsumptionIntervals)
        val cardInstantConsumption: MaterialCardView =
            view.findViewById(R.id.cardInstantConsumption)
        val cardSpeedConsumption: MaterialCardView =
            view.findViewById(R.id.cardSpeedConsumption)
        val cardRpmConsumption: MaterialCardView =
            view.findViewById(R.id.cardRpmConsumption)
        val cardAmbientConsumption: MaterialCardView =
            view.findViewById(R.id.cardAmbientConsumption)
        val cardCoolantConsumption: MaterialCardView =
            view.findViewById(R.id.cardCoolantConsumption)
        val cardFuelLevel: MaterialCardView =
            view.findViewById(R.id.cardFuelLevel)
        val cardManualVsObdConsumption: MaterialCardView =
            view.findViewById(R.id.cardManualVsObdConsumption)

        chartConsumptionIntervals = view.findViewById(R.id.chartConsumptionIntervals)
        chartInstantConsumption = view.findViewById(R.id.chartInstantConsumption)
        chartSpeedConsumption = view.findViewById(R.id.chartSpeedConsumption)
        chartRpmConsumption = view.findViewById(R.id.chartRpmConsumption)
        chartAmbientConsumption = view.findViewById(R.id.chartAmbientConsumption)
        chartCoolantConsumption = view.findViewById(R.id.chartCoolantConsumption)
        chartFuelLevel = view.findViewById(R.id.chartFuelLevel)
        chartManualVsObdConsumption = view.findViewById(R.id.chartManualVsObdConsumption)

        viewModel = ViewModelProvider(
            this,
            StatsViewModelFactory(
                carLocalDataSource = ServiceLocator.carLocalDataSource,
                fuelRepository = ServiceLocator.fuelRepository,
                telemetryRepository = ServiceLocator.telemetryRepository,
                remoteStatsRepository = ServiceLocator.remoteStatsRepository,
                authSessionStorage = ServiceLocator.authSessionStorage,
            )
        )[StatsViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect

                    tvTotalRefuels.text = state.totalRefuelsText
                    tvTotalLiters.text = state.totalLitersText
                    tvTotalDistance.text = state.totalDistanceText
                    tvAverageConsumption.text = state.averageConsumptionText
                    tvLastIntervalConsumption.text = state.lastIntervalConsumptionText

                    tvTelemetrySamplesCount.text = state.telemetrySamplesCountText

                    tvAverageSpeed.text = state.currentSpeedText
                    tvAverageRpm.text = state.currentRpmText
                    tvAverageCoolantTemp.text = state.currentCoolantTempText
                    tvAverageAmbientTemp.text = state.currentAmbientTempText

                    val modelStats = state.modelStats
                    if (modelStats != null) {
                        cardModelServerStats.visibility = View.VISIBLE
                        tvModelServerStatsTitle.text = modelStats.title
                        tvModelServerManualAvg.text = modelStats.manualAvgConsumptionText
                        tvModelServerObdAvg.text = modelStats.obdAvgConsumptionText
                        tvModelServerAvgRefuel.text = modelStats.avgRefuelLitersText
                        tvModelServerAvgDistance.text = modelStats.avgDistanceBetweenRefuelsText
                        tvModelServerVehiclesCount.text = modelStats.vehiclesCountText
                    } else {
                        cardModelServerStats.visibility = View.GONE
                    }

                    val typeStats = state.typeStats
                    if (typeStats != null) {
                        cardTypeServerStats.visibility = View.VISIBLE
                        tvTypeServerStatsTitle.text = typeStats.title
                        tvTypeServerManualAvg.text = typeStats.manualAvgConsumptionText
                        tvTypeServerObdAvg.text = typeStats.obdAvgConsumptionText
                        tvTypeServerAvgRefuel.text = typeStats.avgRefuelLitersText
                        tvTypeServerAvgDistance.text = typeStats.avgDistanceBetweenRefuelsText
                        tvTypeServerVehiclesCount.text = typeStats.vehiclesCountText
                    } else {
                        cardTypeServerStats.visibility = View.GONE
                    }

                    val serverMessage = state.serverStatsMessage
                    if (serverMessage != null) {
                        tvServerStatsMessage.visibility = View.VISIBLE
                        tvServerStatsMessage.text = serverMessage
                    } else {
                        tvServerStatsMessage.visibility = View.GONE
                    }

                    if (state.hasConsumptionIntervalsChart) {
                        cardConsumptionIntervals.visibility = View.VISIBLE
                        if (!intervalChartBound) {
                            chartConsumptionIntervals.chart =
                                StatsChartFactory.createConsumptionIntervalsChart(requireContext())

                            chartConsumptionIntervals.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartConsumptionIntervals.modelProducer =
                                viewModel.consumptionChartModelProducer

                            intervalChartBound = true
                        }
                    } else {
                        cardConsumptionIntervals.visibility = View.GONE
                    }

                    if (state.hasInstantConsumptionChart) {
                        cardInstantConsumption.visibility = View.VISIBLE
                        if (!instantChartBound) {
                            chartInstantConsumption.chart =
                                StatsChartFactory.createInstantConsumptionChart(requireContext())

                            chartInstantConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartInstantConsumption.modelProducer =
                                viewModel.instantConsumptionChartModelProducer

                            instantChartBound = true
                        }
                    } else {
                        cardInstantConsumption.visibility = View.GONE
                    }

                    if (state.hasManualVsObdChart) {
                        cardManualVsObdConsumption.visibility = View.VISIBLE
                        if (!manualVsObdChartBound) {
                            chartManualVsObdConsumption.chart =
                                StatsChartFactory.createManualVsObdConsumptionChart(requireContext())

                            chartManualVsObdConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartManualVsObdConsumption.modelProducer =
                                viewModel.manualVsObdChartModelProducer

                            manualVsObdChartBound = true
                        }
                    } else {
                        cardManualVsObdConsumption.visibility = View.GONE
                    }

                    if (state.hasFuelLevelChart) {
                        cardFuelLevel.visibility = View.VISIBLE
                        if (!fuelLevelChartBound) {
                            chartFuelLevel.chart =
                                StatsChartFactory.createFuelLevelChart(requireContext())

                            chartFuelLevel.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartFuelLevel.modelProducer =
                                viewModel.fuelLevelChartModelProducer

                            fuelLevelChartBound = true
                        }
                    } else {
                        cardFuelLevel.visibility = View.GONE
                    }

                    if (state.hasSpeedConsumptionChart) {
                        cardSpeedConsumption.visibility = View.VISIBLE
                        if (!speedChartBound) {
                            chartSpeedConsumption.chart =
                                StatsChartFactory.createSpeedConsumptionChart(requireContext())

                            chartSpeedConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartSpeedConsumption.modelProducer =
                                viewModel.speedConsumptionChartModelProducer

                            speedChartBound = true
                        }
                    } else {
                        cardSpeedConsumption.visibility = View.GONE
                    }

                    if (state.hasRpmConsumptionChart) {
                        cardRpmConsumption.visibility = View.VISIBLE
                        if (!rpmChartBound) {
                            chartRpmConsumption.chart =
                                StatsChartFactory.createRpmConsumptionChart(requireContext())

                            chartRpmConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartRpmConsumption.modelProducer =
                                viewModel.rpmConsumptionChartModelProducer

                            rpmChartBound = true
                        }
                    } else {
                        cardRpmConsumption.visibility = View.GONE
                    }

                    if (state.hasAmbientConsumptionChart) {
                        cardAmbientConsumption.visibility = View.VISIBLE
                        if (!ambientChartBound) {
                            chartAmbientConsumption.chart =
                                StatsChartFactory.createAmbientConsumptionChart(requireContext())

                            chartAmbientConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartAmbientConsumption.modelProducer =
                                viewModel.ambientConsumptionChartModelProducer

                            ambientChartBound = true
                        }
                    } else {
                        cardAmbientConsumption.visibility = View.GONE
                    }

                    if (state.hasCoolantConsumptionChart) {
                        cardCoolantConsumption.visibility = View.VISIBLE
                        if (!coolantChartBound) {
                            chartCoolantConsumption.chart =
                                StatsChartFactory.createCoolantConsumptionChart(requireContext())

                            chartCoolantConsumption.scrollHandler = ScrollHandler(
                                scrollEnabled = true,
                                initialScroll = Scroll.Absolute.End,
                            )

                            chartCoolantConsumption.modelProducer =
                                viewModel.coolantConsumptionChartModelProducer

                            coolantChartBound = true
                        }
                    } else {
                        cardCoolantConsumption.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.loadStats()
    }
}