package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.SpeedConsumptionPoint

object SpeedConsumptionChartCalculator {

    private data class Bucket(
        val label: String,
        val minInclusive: Double,
        val maxExclusive: Double?
    )

    private val buckets = listOf(
        Bucket("0–20", 0.0, 20.0),
        Bucket("20–40", 20.0, 40.0),
        Bucket("40–60", 40.0, 60.0),
        Bucket("60–80", 60.0, 80.0),
        Bucket("80–100", 80.0, 100.0),
        Bucket("100+", 100.0, null)
    )

    fun build(samples: List<TelemetrySample>): List<SpeedConsumptionPoint> {
        val instantPoints = samples.mapNotNull { sample ->
            val maf = sample.mafGramsPerSec
            val speed = sample.speedKmh

            if (maf == null || speed == null || speed <= 1.0) {
                return@mapNotNull null
            }

            val fuelRateLph = TelemetryConsumptionCalculator.calculateFuelRateLph(maf)
            val consumption =
                TelemetryConsumptionCalculator.calculateConsumptionLPer100Km(
                    fuelRateLph = fuelRateLph,
                    speedKmh = speed
                )

            speed to consumption
        }

        return buckets.mapNotNull { bucket ->
            val values = instantPoints
                .filter { (speed, _) ->
                    if (bucket.maxExclusive == null) {
                        speed >= bucket.minInclusive
                    } else {
                        speed >= bucket.minInclusive && speed < bucket.maxExclusive
                    }
                }
                .map { it.second }

            if (values.isEmpty()) {
                null
            } else {
                SpeedConsumptionPoint(
                    label = bucket.label,
                    averageConsumptionLPer100Km = values.average()
                )
            }
        }
    }
}