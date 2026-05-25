package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.AmbientConsumptionPoint

object AmbientConsumptionChartCalculator {

    private data class Bucket(
        val label: String,
        val minInclusive: Double,
        val maxExclusive: Double?
    )

    private val buckets = listOf(
        Bucket("<-20", Double.NEGATIVE_INFINITY, -20.0),
        Bucket("-20- -10", -20.0, -10.0),
        Bucket("-10–0", -10.0, 0.0),
        Bucket("0–10", 0.0, 10.0),
        Bucket("10–20", 10.0, 20.0),
        Bucket("20–30", 20.0, 30.0),
        Bucket("30+", 30.0, null)
    )

    fun build(samples: List<TelemetrySample>): List<AmbientConsumptionPoint> {
        val instantPoints = samples.mapNotNull { sample ->
            val maf = sample.mafGramsPerSec
            val speed = sample.speedKmh
            val ambient = sample.ambientTempC

            if (maf == null || speed == null || ambient == null || speed <= 1.0) {
                return@mapNotNull null
            }

            val fuelRateLph = TelemetryConsumptionCalculator.calculateFuelRateLph(maf)
            val consumption =
                TelemetryConsumptionCalculator.calculateConsumptionLPer100Km(
                    fuelRateLph = fuelRateLph,
                    speedKmh = speed
                )

            ambient to consumption
        }

        return buckets.mapNotNull { bucket ->
            val values = instantPoints
                .filter { (ambient, _) ->
                    if (bucket.maxExclusive == null) {
                        ambient >= bucket.minInclusive
                    } else {
                        ambient >= bucket.minInclusive && ambient < bucket.maxExclusive
                    }
                }
                .map { it.second }

            if (values.isEmpty()) {
                null
            } else {
                AmbientConsumptionPoint(
                    label = bucket.label,
                    averageConsumptionLPer100Km = values.average()
                )
            }
        }
    }
}