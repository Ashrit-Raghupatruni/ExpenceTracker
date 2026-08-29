@file:Suppress("DEPRECATION")

package com.shakeexpense.app.sensor

import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shakeexpense.app.R
import com.shakeexpense.app.ui.overlay.QuickEntryActivity

class ShakeSensorService : Service() {

    companion object {
        private const val TAG = "ShakeSensorService"
        const val CHANNEL_ID = "shake_sensor_service_channel"
        const val SHAKE_ALERT_CHANNEL_ID = "shake_alert_channel"
        const val NOTIFICATION_ID = 1001
        const val SHAKE_ALERT_NOTIFICATION_ID = 1002

        const val ACTION_START = "com.shakeexpense.app.action.START_SENSOR"
        const val ACTION_STOP = "com.shakeexpense.app.action.STOP_SENSOR"
        const val ACTION_TRIGGER_SHAKE = "com.shakeexpense.app.action.TRIGGER_SHAKE"

        fun startService(context: Context) {
            val intent = Intent(context, ShakeSensorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ShakeSensorService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null
    private var screenStateReceiver: ScreenStateReceiver? = null
    private var isSensorRegistered = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initShakeDetection()
    }

    private fun initShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeDetector = ShakeDetector {
            onShakeDetected()
        }

        screenStateReceiver = ScreenStateReceiver(
            onScreenOn = { registerSensor() },
            onScreenOff = { unregisterSensor() }
        ).also {
            it.register(this)
        }

        registerSensor()
    }

    private fun registerSensor() {
        if (!isSensorRegistered && sensorManager != null && accelerometer != null && shakeDetector != null) {
            sensorManager?.registerListener(
                shakeDetector,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            isSensorRegistered = true
            Log.d(TAG, "Sensor registered for shake detection (SENSOR_DELAY_GAME)")
        }
    }

    private fun unregisterSensor() {
        if (isSensorRegistered && sensorManager != null && shakeDetector != null) {
            sensorManager?.unregisterListener(shakeDetector)
            shakeDetector?.reset()
            isSensorRegistered = false
            Log.d(TAG, "Sensor unregistered (screen off / stopped)")
        }
    }

    fun onShakeDetected() {
        Log.d(TAG, "Physical shake detected!")
        triggerHaptic()

        // Acquire a brief partial wakelock to ensure CPU wakes on aggressive OEM ROMs (OnePlus/Xiaomi)
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            val wakeLock = powerManager?.newWakeLock(
                android.os.PowerManager.PARTIAL_WAKE_LOCK or android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "ShakeExpense:ShakeWakeLock"
            )
            wakeLock?.acquire(1500L)
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquire skipped: ${e.message}")
        }

        val targetIntent = Intent(this, QuickEntryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        // Build PendingIntent with Background Activity Start Mode enabled on Android 14+
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic().apply {
                try {
                    val method = ActivityOptions::class.java.getMethod(
                        "setPendingIntentBackgroundActivityStartMode",
                        Int::class.javaPrimitiveType
                    )
                    method.invoke(this, 1 /* MODE_BACKGROUND_ACTIVITY_START_ALLOWED */)
                } catch (e: Exception) {
                    Log.w(TAG, "Could not set background start mode: ${e.message}")
                }
            }
            PendingIntent.getActivity(
                this,
                0,
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                options.toBundle()
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // 1. Direct Activity Launch
        try {
            startActivity(targetIntent)
            Log.d(TAG, "Direct startActivity invoked")
        } catch (e: Exception) {
            Log.w(TAG, "Direct startActivity failed: ${e.message}")
        }

        // 2. High-priority Full-Screen Notification fallback for background/home-screen launch
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val alertNotification = NotificationCompat.Builder(this, SHAKE_ALERT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⚡ Shake Detected — Tap to Log Expense")
                .setContentText("Tap to open Quick Entry keypad")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.mipmap.ic_launcher,
                    "Open Keypad",
                    pendingIntent
                )
                .setAutoCancel(true)
                .build()

            notificationManager?.notify(SHAKE_ALERT_NOTIFICATION_ID, alertNotification)
            Log.d(TAG, "FullScreen alert notification dispatched")
        } catch (e: Exception) {
            Log.w(TAG, "FullScreen notification dispatch failed: ${e.message}")
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic trigger failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
                registerSensor()
            }
            ACTION_STOP -> {
                unregisterSensor()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TRIGGER_SHAKE -> {
                onShakeDetected()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
                registerSensor()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            // Background Service Channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Shake Detection Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors device shake gestures for quick expense entry"
                setShowBadge(false)
            }

            // High Priority Alert Channel for Full-Screen Shake Overlay
            val alertChannel = NotificationChannel(
                SHAKE_ALERT_CHANNEL_ID,
                "Instant Shake Trigger",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pops up instant expense logging overlay upon physical shake"
                setBypassDnd(true)
                setShowBadge(true)
            }

            notificationManager?.createNotificationChannel(serviceChannel)
            notificationManager?.createNotificationChannel(alertChannel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, QuickEntryActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShakeExpense Active")
            .setContentText("Shake device anytime to log an expense")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        registerSensor()
        try {
            val restartIntent = Intent(applicationContext, ShakeSensorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(restartIntent)
            } else {
                startService(restartIntent)
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        screenStateReceiver?.unregister(this)
        unregisterSensor()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
