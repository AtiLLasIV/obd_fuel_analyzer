package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.FuelLevelPoint

object FuelLevelChartCalculator {

    fun build(samples: List<TelemetrySample>): List<FuelLevelPoint> {
        return samples
            .sortedBy { it.timestampMillis }
            .mapNotNull { sample ->
                val fuelLevel = sample.fuelLevelPercent ?: return@mapNotNull null

                FuelLevelPoint(
                    timestampMillis = sample.timestampMillis,
                    fuelLevelPercent = fuelLevel
                )
            }
    }
}
