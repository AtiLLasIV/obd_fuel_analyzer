package com.diploma.fuelstats.domain.stats

import com.diploma.fuelstats.domain.model.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsCalculatorTest {

    @Test
    fun `returns empty summary for empty entries`() {
        val result = StatsCalculator.calculate(emptyList())

        assertEquals(0, result.totalRefuels)
        assertEquals(0.0, result.totalLiters, 0.0001)
        assertNull(result.totalDistanceKm)
        assertNull(result.averageConsumptionLPer100Km)
        assertNull(result.lastIntervalConsumptionLPer100Km)
    }

    @Test
    fun `calculates consumption by full tank method`() {
        val entries = listOf(
            fuelEntry(
                odometerKm = 1000,
                litersAdded = 40.0,
                isFullTank = true,
                timestampMillis = 0L
            ),
            fuelEntry(
                odometerKm = 1400,
                litersAdded = 32.0,
                isFullTank = true,
                timestampMillis = 1L
            )
        )

        val result = StatsCalculator.calculate(entries)

        assertEquals(2, result.totalRefuels)
        assertEquals(72.0, result.totalLiters, 0.0001)
        assertEquals(400, result.totalDistanceKm)
        assertEquals(8.0, result.averageConsumptionLPer100Km!!, 0.0001)
        assertEquals(8.0, result.lastIntervalConsumptionLPer100Km!!, 0.0001)
    }

    @Test
    fun `calculates full tank interval with intermediate non full refuel`() {
        val entries = listOf(
            fuelEntry(
                odometerKm = 1000,
                litersAdded = 40.0,
                isFullTank = true,
                timestampMillis = 0L
            ),
            fuelEntry(
                odometerKm = 1200,
                litersAdded = 15.0,
                isFullTank = false,
                timestampMillis = 1L
            ),
            fuelEntry(
                odometerKm = 1500,
                litersAdded = 30.0,
                isFullTank = true,
                timestampMillis = 2L
            )
        )

        val result = StatsCalculator.calculate(entries)

        assertEquals(3, result.totalRefuels)
        assertEquals(85.0, result.totalLiters, 0.0001)
        assertEquals(500, result.totalDistanceKm)

        assertEquals(9.0, result.averageConsumptionLPer100Km!!, 0.0001)
        assertEquals(9.0, result.lastIntervalConsumptionLPer100Km!!, 0.0001)
    }

    @Test
    fun `uses fallback calculation when there are no two full tank refuels`() {
        val entries = listOf(
            fuelEntry(
                odometerKm = 1000,
                litersAdded = 20.0,
                isFullTank = false,
                timestampMillis = 0L
            ),
            fuelEntry(
                odometerKm = 1250,
                litersAdded = 25.0,
                isFullTank = false,
                timestampMillis = 1L
            )
        )

        val result = StatsCalculator.calculate(entries)

        assertEquals(2, result.totalRefuels)
        assertEquals(45.0, result.totalLiters, 0.0001)
        assertEquals(250, result.totalDistanceKm)

        assertEquals(18.0, result.averageConsumptionLPer100Km!!, 0.0001)

        assertEquals(10.0, result.lastIntervalConsumptionLPer100Km!!, 0.0001)
    }

    private fun fuelEntry(
        odometerKm: Int,
        litersAdded: Double,
        isFullTank: Boolean,
        timestampMillis: Long
    ): FuelEntry {
        return FuelEntry(
            id = 0L,
            carId = 1L,
            odometerKm = odometerKm,
            litersAdded = litersAdded,
            isFullTank = isFullTank,
            timestampMillis = timestampMillis
        )
    }
}