package com.diploma.fuelstats.data.remote.stats

import com.diploma.fuelstats.domain.model.Car
import com.diploma.fuelstats.domain.model.FuelEntry
import com.diploma.fuelstats.domain.model.TelemetrySample
import com.diploma.fuelstats.domain.stats.StatsCalculator
import com.diploma.fuelstats.domain.stats.charts.ManualVsObdConsumptionChartCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ServerStatsRequestBuilder {

    fun build(
        car: Car,
        entries: List<FuelEntry>,
        samples: List<TelemetrySample>
    ): SendStatsRequestDto {
        val stats = StatsCalculator.calculate(entries)

        val manualVsObdPoints = ManualVsObdConsumptionChartCalculator.build(
            entries = entries,
            samples = samples
        )

        val manualAvg = stats.averageConsumptionLPer100Km

        val obdAvg = manualVsObdPoints
            .map { it.obdConsumptionLPer100Km }
            .takeIf { it.isNotEmpty() }
            ?.average()

        val avgRefuelLiters = entries
            .map { it.litersAdded }
            .takeIf { it.isNotEmpty() }
            ?.average()

        val avgDistanceBetweenRefuelsKm = entries
            .sortedBy { it.odometerKm }
            .zipWithNext()
            .map { (previous, current) ->
                current.odometerKm - previous.odometerKm
            }
            .filter { it > 0 }
            .takeIf { it.isNotEmpty() }
            ?.average()

        return SendStatsRequestDto(
            vehicleId = car.syncVehicleId,
            brand = car.brand,
            model = car.model,
            vehicleType = car.vehicleType,
            manualAvgConsumptionLPer100Km = manualAvg,
            obdAvgConsumptionLPer100Km = obdAvg,
            avgRefuelLiters = avgRefuelLiters,
            avgDistanceBetweenRefuelsKm = avgDistanceBetweenRefuelsKm,
            manualRecordsCount = entries.size,
            telemetrySamplesCount = samples.size,
            updatedAt = nowIsoUtc()
        )
    }

    private fun nowIsoUtc(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }
}