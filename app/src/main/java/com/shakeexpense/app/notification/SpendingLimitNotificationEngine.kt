package com.shakeexpense.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shakeexpense.app.R
import java.util.Calendar

class SpendingLimitNotificationEngine(
    private val context: Context,
    private val prefs: SharedPreferences = context.getSharedPreferences("spending_limit_alerts", Context.MODE_PRIVATE)
) {

    companion object {
        const val CHANNEL_ID = "spending_limits"
        const val NOTIFICATION_ID_INDIVIDUAL = 3001
        const val NOTIFICATION_ID_FAMILY = 3002
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Spending Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when spending approaches or exceeds your monthly budget limits"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun checkAndNotifySpendingLimit(
        currentMonthExpensesCents: Long,
        activeMonthlyLimitCents: Long,
        currencySymbol: String = "₹"
    ) {
        if (activeMonthlyLimitCents <= 0L) return

        val usagePct = (currentMonthExpensesCents.toDouble() / activeMonthlyLimitCents.toDouble()) * 100.0
        val yearMonthKey = getCurrentYearMonthKey()

        val limitRupees = activeMonthlyLimitCents / 100L
        val spentRupees = currentMonthExpensesCents / 100L

        when {
            usagePct >= 100.0 -> {
                if (!prefs.getBoolean("notified_100_$yearMonthKey", false)) {
                    val overAmount = (currentMonthExpensesCents - activeMonthlyLimitCents) / 100L
                    sendNotification(
                        id = NOTIFICATION_ID_INDIVIDUAL,
                        title = "⚠️ Monthly Spending Limit Exceeded",
                        message = "Your monthly spending limit has been exceeded by $currencySymbol$overAmount."
                    )
                    prefs.edit().putBoolean("notified_100_$yearMonthKey", true).apply()
                }
            }
            usagePct >= 90.0 -> {
                if (!prefs.getBoolean("notified_90_$yearMonthKey", false)) {
                    sendNotification(
                        id = NOTIFICATION_ID_INDIVIDUAL,
                        title = "⚠️ 90% Spending Limit Reached",
                        message = "You've used 90% of your monthly spending limit ($currencySymbol$spentRupees of $currencySymbol$limitRupees)."
                    )
                    prefs.edit().putBoolean("notified_90_$yearMonthKey", true).apply()
                }
            }
            usagePct >= 80.0 -> {
                if (!prefs.getBoolean("notified_80_$yearMonthKey", false)) {
                    sendNotification(
                        id = NOTIFICATION_ID_INDIVIDUAL,
                        title = "⚠️ 80% Spending Limit Reached",
                        message = "You've used 80% of your monthly spending limit."
                    )
                    prefs.edit().putBoolean("notified_80_$yearMonthKey", true).apply()
                }
            }
            usagePct >= 70.0 -> {
                if (!prefs.getBoolean("notified_70_$yearMonthKey", false)) {
                    sendNotification(
                        id = NOTIFICATION_ID_INDIVIDUAL,
                        title = "ℹ️ 70% Spending Limit Reached",
                        message = "You've used 70% of your monthly spending limit."
                    )
                    prefs.edit().putBoolean("notified_70_$yearMonthKey", true).apply()
                }
            }
        }
    }

    fun checkAndNotifyFamilyLimit(
        familyTotalSpendingCents: Long,
        familyLimitCents: Long,
        currencySymbol: String = "₹"
    ) {
        if (familyLimitCents <= 0L || familyTotalSpendingCents <= familyLimitCents) return

        val yearMonthKey = getCurrentYearMonthKey()
        if (!prefs.getBoolean("notified_family_100_$yearMonthKey", false)) {
            val overRupees = (familyTotalSpendingCents - familyLimitCents) / 100L
            val familySpentRupees = familyTotalSpendingCents / 100L
            val familyLimitRupees = familyLimitCents / 100L

            sendNotification(
                id = NOTIFICATION_ID_FAMILY,
                title = "⚠️ Family Monthly Spending Limit Exceeded",
                message = "Family spending: $currencySymbol$familySpentRupees | Monthly limit: $currencySymbol$familyLimitRupees | Over limit: $currencySymbol$overRupees"
            )
            prefs.edit().putBoolean("notified_family_100_$yearMonthKey", true).apply()
        }
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted by user yet
        }
    }

    private fun getCurrentYearMonthKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
    }
}
