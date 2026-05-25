package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.InstantConsumptionPoint

object TelemetryConsumptionCalculator {
    fun calculateFuelRateLph(mafGramsPerSec: Double): Double {
        return mafGramsPerSec * 3600.0 / 14.7 / 750.0
    }

    fun calculateConsumptionLPer100Km(
        fuelRateLph: Double,
        speedKmh: Double
    ): Double {
        return fuelRateLph / speedKmh * 100.0
    }

    fun buildInstantConsumptionPoints(
        samples: List<TelemetrySample>
    ): List<InstantConsumptionPoint> {
        return samples
            .sortedBy { it.timestampMillis }
            .mapNotNull { sample ->
                val maf = sample.mafGramsPerSec
                val speed = sample.speedKmh

                if (maf == null || speed == null || speed <= 1.0) {
                    return@mapNotNull null
                }

                val fuelRateLph = calculateFuelRateLph(maf)
                val consumptionLPer100Km =
                    calculateConsumptionLPer100Km(fuelRateLph, speed)

                InstantConsumptionPoint(
                    timestampMillis = sample.timestampMillis,
                    consumptionLPer100Km = consumptionLPer100Km
                )
            }
    }
}