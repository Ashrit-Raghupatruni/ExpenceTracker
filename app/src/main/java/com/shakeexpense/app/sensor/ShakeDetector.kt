package com.shakeexpense.app.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs
import kotlin.math.sqrt

class ShakeDetector(
    private val onShakeListener: () -> Unit
) : SensorEventListener {

    companion object {
        const val GRAVITY_EARTH = 9.80665f
        const val SHAKE_THRESHOLD = 11.5f // m/s^2 above gravity for reliable natural shake
        const val REVERSAL_TIME_WINDOW_MS = 450L
        const val DEBOUNCE_TIME_MS = 750L
    }

    private var lastUpdateTimestamp: Long = 0
    private var lastShakeTimestamp: Long = 0
    private var reversalCount: Int = 0
    private var lastSign: Int = 0
    private var windowStartTime: Long = 0

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val currentTime = System.currentTimeMillis()

        val isShake = processAcceleration(x, y, z, currentTime)
        if (isShake) {
            onShakeListener()
        }
    }

    fun processAcceleration(x: Float, y: Float, z: Float, currentTime: Long): Boolean {
        // Calculate magnitude and subtract gravity
        val totalAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val netAcceleration = abs(totalAcceleration - GRAVITY_EARTH)

        // Check if acceleration exceeds threshold
        if (netAcceleration >= SHAKE_THRESHOLD) {
            val dominantAxisSign = when {
                abs(x) >= abs(y) && abs(x) >= abs(z) -> if (x > 0) 1 else -1
                abs(y) >= abs(x) && abs(y) >= abs(z) -> if (y > 0) 1 else -1
                else -> if (z > 0) 1 else -1
            }

            if (windowStartTime == 0L || (currentTime - windowStartTime) > REVERSAL_TIME_WINDOW_MS) {
                // Reset window
                windowStartTime = currentTime
                reversalCount = 0
                lastSign = dominantAxisSign
            } else {
                // Check for direction reversal
                if (lastSign != 0 && dominantAxisSign != lastSign) {
                    reversalCount++
                    lastSign = dominantAxisSign

                    // Check if required reversals met and debounce window passed
                    if (reversalCount >= 2 && (currentTime - lastShakeTimestamp) >= DEBOUNCE_TIME_MS) {
                        lastShakeTimestamp = currentTime
                        reversalCount = 0
                        windowStartTime = 0L
                        lastSign = 0
                        return true
                    }
                }
            }
        }

        lastUpdateTimestamp = currentTime
        return false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun reset() {
        lastUpdateTimestamp = 0
        lastShakeTimestamp = 0
        reversalCount = 0
        lastSign = 0
        windowStartTime = 0L
    }
}
