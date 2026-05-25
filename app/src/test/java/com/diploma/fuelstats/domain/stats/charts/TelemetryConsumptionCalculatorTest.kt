package com.diploma.fuelstats.domain.stats.charts

import com.diploma.fuelstats.domain.model.TelemetrySample
import org.junit.Assert.assertEquals
import org.junit.Test

class TelemetryConsumptionCalculatorTest {

    @Test
    fun `calculates fuel rate from maf`() {
        val result = TelemetryConsumptionCalculator.calculateFuelRateLph(
            mafGramsPerSec = 14.7
        )

        assertEquals(4.8, result, 0.0001)
    }

    @Test
    fun `calculates consumption per 100 km from fuel rate and speed`() {
        val result = TelemetryConsumptionCalculator.calculateConsumptionLPer100Km(
            fuelRateLph = 4.8,
            speedKmh = 100.0
        )

        assertEquals(4.8, result, 0.0001)
    }

    @Test
    fun `builds instant consumption points sorted by timestamp`() {
        val samples = listOf(
            sample(
                timestampMillis = 2000L,
                speedKmh = 100.0,
                mafGramsPerSec = 14.7
            ),
            sample(
                timestampMillis = 1000L,
                speedKmh = 50.0,
                mafGramsPerSec = 14.7
            )
        )

        val result = TelemetryConsumptionCalculator.buildInstantConsumptionPoints(samples)

        assertEquals(2, result.size)
        assertEquals(1000L, result[0].timestampMillis)
        assertEquals(2000L, result[1].timestampMillis)

        assertEquals(9.6, result[0].consumptionLPer100Km, 0.0001)
        assertEquals(4.8, result[1].consumptionLPer100Km, 0.0001)
    }

    @Test
    fun `ignores samples with missing maf missing speed or low speed`() {
        val samples = listOf(
            sample(
                timestampMillis = 1000L,
                speedKmh = null,
                mafGramsPerSec = 14.7
            ),
            sample(
                timestampMillis = 2000L,
                speedKmh = 100.0,
                mafGramsPerSec = null
            ),
            sample(
                timestampMillis = 3000L,
                speedKmh = 1.0,
                mafGramsPerSec = 14.7
            ),
            sample(
                timestampMillis = 4000L,
                speedKmh = 100.0,
                mafGramsPerSec = 14.7
            )
        )

        val result = TelemetryConsumptionCalculator.buildInstantConsumptionPoints(samples)

        assertEquals(1, result.size)
        assertEquals(4000L, result[0].timestampMillis)
        assertEquals(4.8, result[0].consumptionLPer100Km, 0.0001)
    }

    private fun sample(
        timestampMillis: Long,
        speedKmh: Double?,
        mafGramsPerSec: Double?
    ): TelemetrySample {
        return TelemetrySample(
            id = 0L,
            carId = 1L,
            timestampMillis = timestampMillis,
            speedKmh = speedKmh,
            rpm = null,
            fuelLevelPercent = null,
            coolantTempC = null,
            ambientTempC = null,
            mafGramsPerSec = mafGramsPerSec
        )
    }
}