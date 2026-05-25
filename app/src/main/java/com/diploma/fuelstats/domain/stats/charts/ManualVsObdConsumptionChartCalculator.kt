package com.diploma.fuelstats.domain.stats.charts

import android.util.Log
import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.charts.model.ManualVsObdConsumptionPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ManualVsObdConsumptionChartCalculator {

    private const val TAG = "ManualVsObdCalc"

    fun build(
        entries: List<FuelEntry>,
        samples: List<TelemetrySample>
    ): List<ManualVsObdConsumptionPoint> {
        val fullTankEntries = entries
            .filter { it.isFullTank }
            .sortedBy { it.timestampMillis }

        val sortedSamples = samples.sortedBy { it.timestampMillis }

        if (fullTankEntries.size < 2 || sortedSamples.size < 2) {
            Log.d(
                TAG,
                "build: not enough data, fullTankEntries=${fullTankEntries.size}, samples=${sortedSamples.size}"
            )
            return emptyList()
        }

        val labelFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

        val result = fullTankEntries.zipWithNext().mapNotNull { (previous, current) ->
            val distanceKm = current.odometerKm.toDouble() - previous.odometerKm.toDouble()
            if (distanceKm <= 0.0) {
                Log.d(
                    TAG,
                    "skip interval: invalid manual distance, prevOdo=${previous.odometerKm}, currentOdo=${current.odometerKm}"
                )
                return@mapNotNull null
            }

            if (current.timestampMillis <= previous.timestampMillis) {
                Log.d(
                    TAG,
                    "skip interval: invalid timestamps, previous=${previous.timestampMillis}, current=${current.timestampMillis}"
                )
                return@mapNotNull null
            }

            val manualConsumption = current.litersAdded / distanceKm * 100.0

            val intervalSamples = sortedSamples.filter {
                it.timestampMillis in previous.timestampMillis..current.timestampMillis
            }

            if (intervalSamples.size < 2) {
                Log.d(
                    TAG,
                    """
                    skip interval: not enough OBD samples
                    label=${labelFormat.format(Date(current.timestampMillis))}
                    intervalSamples=${intervalSamples.size}
                    previousTime=${Date(previous.timestampMillis)}
                    currentTime=${Date(current.timestampMillis)}
                    """.trimIndent()
                )
                return@mapNotNull null
            }

            var obdFuelLiters = 0.0
            var obdDistanceKm = 0.0
            var usedSegments = 0
            var skippedSegments = 0

            for (j in 0 until intervalSamples.lastIndex) {
                val cur = intervalSamples[j]
                val next = intervalSamples[j + 1]

                val maf = cur.mafGramsPerSec
                val speed = cur.speedKmh
                val deltaMillis = next.timestampMillis - cur.timestampMillis

                if (maf == null || speed == null || speed <= 1.0 || deltaMillis <= 0L) {
                    skippedSegments++
                    continue
                }

                if (deltaMillis > 10_000L) {
                    skippedSegments++
                    continue
                }

                val deltaHours = deltaMillis / 3_600_000.0
                val fuelRateLph = TelemetryConsumptionCalculator.calculateFuelRateLph(maf)

                if (fuelRateLph <= 0.0 || fuelRateLph > 80.0) {
                    skippedSegments++
                    continue
                }

                obdFuelLiters += fuelRateLph * deltaHours
                obdDistanceKm += speed * deltaHours
                usedSegments++
            }

            if (obdDistanceKm <= 0.0 || usedSegments == 0) {
                Log.d(
                    TAG,
                    """
                    skip interval: no usable OBD movement
                    label=${labelFormat.format(Date(current.timestampMillis))}
                    intervalSamples=${intervalSamples.size}
                    usedSegments=$usedSegments
                    skippedSegments=$skippedSegments
                    obdFuelLiters=$obdFuelLiters
                    obdDistanceKm=$obdDistanceKm
                    """.trimIndent()
                )
                return@mapNotNull null
            }

            val obdConsumption = obdFuelLiters / obdDistanceKm * 100.0

            val label = "${labelFormat.format(Date(previous.timestampMillis))}–${labelFormat.format(Date(current.timestampMillis))}"

            Log.d(
                TAG,
                """
                interval result:
                label=${labelFormat.format(Date(current.timestampMillis))}
                previousTime=${Date(previous.timestampMillis)}
                currentTime=${Date(current.timestampMillis)}
                manualDistanceKm=$distanceKm
                manualLiters=${current.litersAdded}
                manualConsumption=$manualConsumption
                intervalSamples=${intervalSamples.size}
                usedSegments=$usedSegments
                skippedSegments=$skippedSegments
                obdFuelLiters=$obdFuelLiters
                obdDistanceKm=$obdDistanceKm
                obdConsumption=$obdConsumption
                """.trimIndent()
            )

            ManualVsObdConsumptionPoint(
                label = label,
                manualConsumptionLPer100Km = manualConsumption,
                obdConsumptionLPer100Km = obdConsumption
            )
        }

        Log.d(TAG, "build: result=${result.size}")

        return result
    }
}