package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.stats.charts.model.ConsumptionIntervalPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ConsumptionChartCalculator {

    private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    fun build(entries: List<FuelEntry>): List<ConsumptionIntervalPoint> {
        if (entries.isEmpty()) return emptyList()

        val sortedEntries = entries.sortedBy { it.odometerKm }

        val result = mutableListOf<ConsumptionIntervalPoint>()

        var previousFullTankEntry: FuelEntry? = null
        var litersSincePreviousFullTank = 0.0

        for (entry in sortedEntries) {
            litersSincePreviousFullTank += entry.litersAdded

            if (!entry.isFullTank) {
                continue
            }

            val startEntry = previousFullTankEntry
            val endEntry = entry

            if (startEntry != null) {
                val distanceKm = endEntry.odometerKm - startEntry.odometerKm

                if (distanceKm > 0) {
                    val consumption = litersSincePreviousFullTank / distanceKm * 100.0

                    val label = buildIntervalLabel(
                        startTimestampMillis = startEntry.timestampMillis,
                        endTimestampMillis = endEntry.timestampMillis
                    )

                    result.add(
                        ConsumptionIntervalPoint(
                            label = label,
                            consumptionLPer100Km = consumption,
                            distanceKm = distanceKm,
                            litersUsed = litersSincePreviousFullTank
                        )
                    )
                }
            }
            previousFullTankEntry = entry
            litersSincePreviousFullTank = 0.0
        }

        return result
    }

    private fun buildIntervalLabel(
        startTimestampMillis: Long,
        endTimestampMillis: Long
    ): String {
        val startText = dateFormat.format(Date(startTimestampMillis))
        val endText = dateFormat.format(Date(endTimestampMillis))

        return if (startText == endText) {
            endText
        } else {
            "$startText–$endText"
        }
    }
}