package com.diploma.fuelstats.data.obd

import com.diploma.fuelstats.domain.model.TelemetrySample
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefuelDetectorTest {

    @Test
    fun `returns true when fuel level rises while car is almost stopped`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        val firstSample = sample(
            timestampMillis = 0L,
            speedKmh = 0.0,
            fuelLevelPercent = 30.0
        )

        val refuelSample = sample(
            timestampMillis = 31 * 60 * 1000L,
            speedKmh = 0.0,
            fuelLevelPercent = 42.0
        )

        assertFalse(detector.onSample(firstSample))
        assertTrue(detector.onSample(refuelSample))
    }

    @Test
    fun `returns false when fuel level rises but car is moving`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        val firstSample = sample(
            timestampMillis = 0L,
            speedKmh = 0.0,
            fuelLevelPercent = 30.0
        )

        val movingSample = sample(
            timestampMillis = 31 * 60 * 1000L,
            speedKmh = 30.0,
            fuelLevelPercent = 42.0
        )

        assertFalse(detector.onSample(firstSample))
        assertFalse(detector.onSample(movingSample))
    }

    @Test
    fun `returns false for repeated detection within cooldown interval`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        assertFalse(detector.onSample(sample(0L, speedKmh = 0.0, fuelLevelPercent = 30.0)))

        assertTrue(
            detector.onSample(
                sample(
                    timestampMillis = 31 * 60 * 1000L,
                    speedKmh = 0.0,
                    fuelLevelPercent = 42.0
                )
            )
        )

        assertFalse(
            detector.onSample(
                sample(
                    timestampMillis = 32 * 60 * 1000L,
                    speedKmh = 0.0,
                    fuelLevelPercent = 55.0
                )
            )
        )
    }

    @Test
    fun `returns false when fuel level rise is below threshold`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        val firstSample = sample(
            timestampMillis = 0L,
            speedKmh = 0.0,
            fuelLevelPercent = 30.0
        )

        val smallRiseSample = sample(
            timestampMillis = 31 * 60 * 1000L,
            speedKmh = 0.0,
            fuelLevelPercent = 38.0
        )

        assertFalse(detector.onSample(firstSample))
        assertFalse(detector.onSample(smallRiseSample))
    }

    @Test
    fun `updates baseline when fuel level decreases and detects refuel from new baseline`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        assertFalse(
            detector.onSample(
                sample(
                    timestampMillis = 0L,
                    speedKmh = 20.0,
                    fuelLevelPercent = 50.0
                )
            )
        )

        assertFalse(
            detector.onSample(
                sample(
                    timestampMillis = 10 * 60 * 1000L,
                    speedKmh = 20.0,
                    fuelLevelPercent = 40.0
                )
            )
        )

        assertTrue(
            detector.onSample(
                sample(
                    timestampMillis = 31 * 60 * 1000L,
                    speedKmh = 0.0,
                    fuelLevelPercent = 51.0
                )
            )
        )
    }

    @Test
    fun `returns false when required telemetry values are missing`() {
        val detector = RefuelDetector(
            speedThresholdKmh = 2.0,
            fuelRiseThresholdPercent = 10.0,
            minIntervalBetweenAlertsMillis = 30 * 60 * 1000L
        )

        assertFalse(
            detector.onSample(
                sample(
                    timestampMillis = 0L,
                    speedKmh = null,
                    fuelLevelPercent = 30.0
                )
            )
        )

        assertFalse(
            detector.onSample(
                sample(
                    timestampMillis = 31 * 60 * 1000L,
                    speedKmh = 0.0,
                    fuelLevelPercent = null
                )
            )
        )
    }

    private fun sample(
        timestampMillis: Long,
        speedKmh: Double?,
        fuelLevelPercent: Double?
    ): TelemetrySample {
        return TelemetrySample(
            id = 0L,
            carId = 1L,
            timestampMillis = timestampMillis,
            speedKmh = speedKmh,
            rpm = null,
            fuelLevelPercent = fuelLevelPercent,
            coolantTempC = null,
            ambientTempC = null,
            mafGramsPerSec = null
        )
    }
}