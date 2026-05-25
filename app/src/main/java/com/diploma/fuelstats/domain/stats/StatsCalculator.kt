package com.diploma.fuelstats.domain.stats

import com.diploma.fuelstats.domain.model.FuelEntry

object StatsCalculator {
    fun calculate(entries: List<FuelEntry>): StatsSummary {
        if (entries.isEmpty()) {
            return StatsSummary(
                totalRefuels = 0,
                totalLiters = 0.0,
                totalDistanceKm = null,
                averageConsumptionLPer100Km = null,
                lastIntervalConsumptionLPer100Km = null
            )
        }

        val sorted = entries.sortedBy { it.odometerKm }

        val totalRefuels = sorted.size
        val totalLiters = sorted.sumOf { it.litersAdded }

        val minOdom = sorted.first().odometerKm
        val maxOdom = sorted.last().odometerKm
        val totalDistanceKmAll = (maxOdom - minOdom).takeIf { it > 0 }

        val fullIntervals = buildFullTankIntervals(sorted)

        val averageConsumption: Double?
        val lastIntervalConsumption: Double?

        if (fullIntervals.isNotEmpty()) {
            val totalDistanceIntervals = fullIntervals.sumOf { it.distanceKm }
            val totalLitersIntervals = fullIntervals.sumOf { it.liters }

            averageConsumption =
                if (totalDistanceIntervals > 0) {
                    totalLitersIntervals / (totalDistanceIntervals / 100.0)
                } else {
                    null
                }

            lastIntervalConsumption = fullIntervals.last().consumptionLPer100Km
        } else {
            averageConsumption = calculateFallbackAverageConsumption(
                totalLiters = totalLiters,
                totalDistanceKmAll = totalDistanceKmAll
            )

            lastIntervalConsumption = calculateFallbackLastIntervalConsumption(sorted)
        }

        return StatsSummary(
            totalRefuels = totalRefuels,
            totalLiters = totalLiters,
            totalDistanceKm = totalDistanceKmAll,
            averageConsumptionLPer100Km = averageConsumption,
            lastIntervalConsumptionLPer100Km = lastIntervalConsumption
        )
    }
    private data class FullTankInterval(
        val distanceKm: Int,
        val liters: Double,
        val consumptionLPer100Km: Double
    )

    private fun buildFullTankIntervals(
        sortedEntries: List<FuelEntry>
    ): List<FullTankInterval> {
        val fullIndices = sortedEntries
            .mapIndexedNotNull { index, entry ->
                if (entry.isFullTank) index else null
            }

        if (fullIndices.size < 2) return emptyList()

        val intervals = mutableListOf<FullTankInterval>()

        for (i in 1 until fullIndices.size) {
            val startIdx = fullIndices[i - 1]
            val endIdx = fullIndices[i]

            val start = sortedEntries[startIdx]
            val end = sortedEntries[endIdx]

            val distanceKm = end.odometerKm - start.odometerKm
            if (distanceKm <= 0) continue

            val liters = sortedEntries
                .subList(startIdx + 1, endIdx + 1)
                .sumOf { it.litersAdded }

            if (liters <= 0.0) continue

            val consumption = liters / (distanceKm / 100.0)

            intervals.add(
                FullTankInterval(
                    distanceKm = distanceKm,
                    liters = liters,
                    consumptionLPer100Km = consumption
                )
            )
        }

        return intervals
    }
    private fun calculateFallbackAverageConsumption(
        totalLiters: Double,
        totalDistanceKmAll: Int?
    ): Double? {
        if (totalDistanceKmAll == null || totalDistanceKmAll <= 0) return null
        if (totalLiters <= 0.0) return null

        return totalLiters / (totalDistanceKmAll / 100.0)
    }

    private fun calculateFallbackLastIntervalConsumption(
        sortedEntries: List<FuelEntry>
    ): Double? {
        if (sortedEntries.size < 2) return null

        val last = sortedEntries[sortedEntries.size - 1]
        val prev = sortedEntries[sortedEntries.size - 2]

        val distanceKm = last.odometerKm - prev.odometerKm
        if (distanceKm <= 0) return null

        val liters = last.litersAdded
        if (liters <= 0.0) return null

        return liters / (distanceKm / 100.0)
    }
}