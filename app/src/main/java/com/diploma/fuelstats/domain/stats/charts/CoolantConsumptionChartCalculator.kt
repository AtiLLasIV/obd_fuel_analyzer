package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.CoolantConsumptionPoint

object CoolantConsumptionChartCalculator {

    private data class Bucket(
        val label: String,
        val minInclusive: Double,
        val maxExclusive: Double?
    )

    private val buckets = listOf(
        Bucket("<40", Double.NEGATIVE_INFINITY, 40.0),
        Bucket("40–60", 40.0, 60.0),
        Bucket("60–70", 60.0, 70.0),
        Bucket("70–80", 70.0, 80.0),
        Bucket("80–90", 80.0, 90.0),
        Bucket("90–100", 90.0, 100.0),
        Bucket("100+", 100.0, null)
    )

    fun build(samples: List<TelemetrySample>): List<CoolantConsumptionPoint> {
        val instantPoints = samples.mapNotNull { sample ->
            val maf = sample.mafGramsPerSec
            val speed = sample.speedKmh
            val coolant = sample.coolantTempC

            if (maf == null || speed == null || coolant == null || speed <= 1.0) {
                return@mapNotNull null
            }

            val fuelRateLph = TelemetryConsumptionCalculator.calculateFuelRateLph(maf)
            val consumption =
                TelemetryConsumptionCalculator.calculateConsumptionLPer100Km(
                    fuelRateLph = fuelRateLph,
                    speedKmh = speed
                )

            coolant to consumption
        }

        return buckets.mapNotNull { bucket ->
            val values = instantPoints
                .filter { (coolant, _) ->
                    if (bucket.maxExclusive == null) {
                        coolant >= bucket.minInclusive
                    } else {
                        coolant >= bucket.minInclusive && coolant < bucket.maxExclusive
                    }
                }
                .map { it.second }

            if (values.isEmpty()) {
                null
            } else {
                CoolantConsumptionPoint(
                    label = bucket.label,
                    averageConsumptionLPer100Km = values.average()
                )
            }
        }
    }
}