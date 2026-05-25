package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.RpmConsumptionPoint

object RpmConsumptionChartCalculator {

    private data class Bucket(
        val label: String,
        val minInclusive: Double,
        val maxExclusive: Double?
    )

    private val buckets = listOf(
        Bucket("0–1000", 0.0, 1000.0),
        Bucket("1000–1500", 1000.0, 1500.0),
        Bucket("1500–2000", 1500.0, 2000.0),
        Bucket("2000–2500", 2000.0, 2500.0),
        Bucket("2500–3000", 2500.0, 3000.0),
        Bucket("3000+", 3000.0, null)
    )

    fun build(samples: List<TelemetrySample>): List<RpmConsumptionPoint> {
        val instantPoints = samples.mapNotNull { sample ->
            val maf = sample.mafGramsPerSec
            val speed = sample.speedKmh
            val rpm = sample.rpm

            if (maf == null || speed == null || rpm == null || speed <= 1.0) {
                return@mapNotNull null
            }

            val fuelRateLph = TelemetryConsumptionCalculator.calculateFuelRateLph(maf)
            val consumption =
                TelemetryConsumptionCalculator.calculateConsumptionLPer100Km(
                    fuelRateLph = fuelRateLph,
                    speedKmh = speed
                )

            rpm to consumption
        }

        return buckets.mapNotNull { bucket ->
            val values = instantPoints
                .filter { (rpm, _) ->
                    if (bucket.maxExclusive == null) {
                        rpm >= bucket.minInclusive
                    } else {
                        rpm >= bucket.minInclusive && rpm < bucket.maxExclusive
                    }
                }
                .map { it.second }

            if (values.isEmpty()) {
                null
            } else {
                RpmConsumptionPoint(
                    label = bucket.label,
                    averageConsumptionLPer100Km = values.average()
                )
            }
        }
    }
}