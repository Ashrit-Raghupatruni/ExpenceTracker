package com.shakeexpense.app.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShakeDetectorTest {

    private var shakeCount = 0
    private lateinit var detector: ShakeDetector

    @Before
    fun setUp() {
        shakeCount = 0
        detector = ShakeDetector {
            shakeCount++
        }
    }

    @Test
    fun testStationaryGravityDoesNotTriggerShake() {
        // Stationary device sitting on a table (1g on Z axis)
        val triggered = detector.processAcceleration(0f, 0f, 9.81f, 1000L)
        assertFalse(triggered)
        assertEquals(0, shakeCount)
    }

    @Test
    fun testLowMovementBelowThresholdDoesNotTriggerShake() {
        // Net acceleration below 13.5 threshold (e.g. 15 m/s^2 total, net ~5.2 m/s^2)
        val triggered1 = detector.processAcceleration(10f, 10f, 5f, 1000L)
        val triggered2 = detector.processAcceleration(-10f, -10f, 5f, 1200L)
        assertFalse(triggered1)
        assertFalse(triggered2)
        assertEquals(0, shakeCount)
    }

    @Test
    fun testSingleJerkWithoutReversalsDoesNotTriggerShake() {
        // Sudden acceleration in one direction without reversal (e.g. phone dropped or picked up)
        val triggered = detector.processAcceleration(25f, 0f, 9.81f, 1000L)
        assertFalse(triggered)
        assertEquals(0, shakeCount)
    }

    @Test
    fun testValidShakeSequenceTriggersListener() {
        // Peak 1: strong positive X acceleration (net acceleration: sqrt(25^2 + 9.81^2) - 9.81 = ~17 m/s^2 > 13.5)
        val p1 = detector.processAcceleration(25f, 0f, 9.81f, 1000L)
        assertFalse(p1)

        // Peak 2: strong negative X acceleration (Reversal 1) at 150ms
        val p2 = detector.processAcceleration(-25f, 0f, 9.81f, 1150L)
        assertFalse(p2)

        // Peak 3: strong positive X acceleration (Reversal 2) at 300ms (within 400ms window)
        val p3 = detector.processAcceleration(25f, 0f, 9.81f, 1300L)
        assertTrue(p3)
    }

    @Test
    fun testDebouncePreventsRapidDuplicateTriggers() {
        // First shake at 1000ms - 1300ms
        detector.processAcceleration(25f, 0f, 9.81f, 1000L)
        detector.processAcceleration(-25f, 0f, 9.81f, 1150L)
        val firstShake = detector.processAcceleration(25f, 0f, 9.81f, 1300L)
        assertTrue(firstShake)

        // Immediate second shake at 1500ms (within 1000ms debounce window from 1300ms)
        detector.processAcceleration(25f, 0f, 9.81f, 1400L)
        detector.processAcceleration(-25f, 0f, 9.81f, 1500L)
        val debouncedShake = detector.processAcceleration(25f, 0f, 9.81f, 1600L)
        assertFalse(debouncedShake)

        // Third shake at 2500ms (after 1000ms debounce window)
        detector.processAcceleration(25f, 0f, 9.81f, 2400L)
        detector.processAcceleration(-25f, 0f, 9.81f, 2550L)
        val allowedShake = detector.processAcceleration(25f, 0f, 9.81f, 2700L)
        assertTrue(allowedShake)
    }
}
