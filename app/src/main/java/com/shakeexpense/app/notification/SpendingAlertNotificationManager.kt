package com.shakeexpense.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.shakeexpense.app.R
import com.shakeexpense.app.ui.tracker.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class SpendingAlertNotificationManager(
    private val context: Context
) {

    companion object {
        const val CHANNEL_PERSONAL_LIMIT = "channel_personal_limit_alerts"
        const val CHANNEL_FAMILY_LIMIT = "channel_family_limit_alerts"
        const val CHANNEL_UNUSUAL_SPENDING = "channel_unusual_spending_alerts"

        // In-memory anti-spam state: key = "$userId|$yearMonth|$limitType" -> set of triggered percentages
        val triggeredThresholds = ConcurrentHashMap<String, MutableSet<Int>>()

        fun getYearMonthKey(timestamp: Long = System.currentTimeMillis()): String {
            return SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timestamp))
        }

        fun resetThresholds(userId: String, limitType: String) {
            val ym = getYearMonthKey()
            triggeredThresholds["$userId|$ym|$limitType"]?.clear()
        }

        fun shouldTriggerAlert(userId: String, percentage: Int, limitType: String, yearMonth: String = getYearMonthKey()): Boolean {
            val key = "$userId|$yearMonth|$limitType"
            val sentSet = triggeredThresholds.getOrPut(key) { ConcurrentHashMap.newKeySet() }
            val threshold = when {
                percentage >= 100 && !sentSet.contains(100) -> {
                    sentSet.addAll(listOf(70, 80, 90, 100))
                    100
                }
                percentage >= 90 && !sentSet.contains(90) -> {
                    sentSet.addAll(listOf(70, 80, 90))
                    90
                }
                percentage >= 80 && !sentSet.contains(80) -> {
                    sentSet.addAll(listOf(70, 80))
                    80
                }
                percentage >= 70 && !sentSet.contains(70) -> {
                    sentSet.add(70)
                    70
                }
                else -> null
            }
            return threshold != null
        }
    }

    private val notificationManager: NotificationManager? =
        ContextCompat.getSystemService(context, NotificationManager::class.java)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val personalChannel = NotificationChannel(
                CHANNEL_PERSONAL_LIMIT,
                "Personal Spending Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when personal monthly spending reaches 70%, 80%, 90%, or 100%"
                enableVibration(true)
            }

            val familyChannel = NotificationChannel(
                CHANNEL_FAMILY_LIMIT,
                "Family Spending Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when collective family monthly limit or category budgets are exceeded"
                enableVibration(true)
            }

            val unusualChannel = NotificationChannel(
                CHANNEL_UNUSUAL_SPENDING,
                "Unusual Spending Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for transaction spikes and rapid duplicate debits"
            }

            notificationManager?.createNotificationChannels(listOf(personalChannel, familyChannel, unusualChannel))
        }
    }

    /**
     * Checks thresholds (70, 80, 90, 100) and dispatches alert if newly crossed.
     * Guaranteed anti-spam: exactly 1 alert per threshold per month.
     */
    fun checkAndNotifyPersonalLimit(
        userId: String,
        currentSpentCents: Long,
        limitCents: Long,
        yearMonth: String = getYearMonthKey()
    ): Int? {
        if (limitCents <= 0L) return null

        val percentage = (currentSpentCents.toDouble() / limitCents.toDouble() * 100.0).toInt()
        val key = "$userId|$yearMonth|PERSONAL"
        val sentSet = triggeredThresholds.getOrPut(key) { ConcurrentHashMap.newKeySet() }

        val thresholdToTrigger = when {
            percentage >= 100 && !sentSet.contains(100) -> {
                sentSet.addAll(listOf(70, 80, 90, 100))
                100
            }
            percentage >= 90 && !sentSet.contains(90) -> {
                sentSet.addAll(listOf(70, 80, 90))
                90
            }
            percentage >= 80 && !sentSet.contains(80) -> {
                sentSet.addAll(listOf(70, 80))
                80
            }
            percentage >= 70 && !sentSet.contains(70) -> {
                sentSet.add(70)
                70
            }
            else -> null
        }

        if (thresholdToTrigger != null) {
            sendPersonalNotification(thresholdToTrigger, currentSpentCents, limitCents)
        }

        return thresholdToTrigger
    }

    private fun sendPersonalNotification(threshold: Int, spentCents: Long, limitCents: Long) {
        val spentRupees = spentCents / 100
        val limitRupees = limitCents / 100

        val (title, body) = when (threshold) {
            100 -> {
                val excess = (spentCents - limitCents).coerceAtLeast(0L) / 100
                Pair(
                    "🚨 Monthly Spending Limit Exceeded",
                    if (excess > 0) "Your monthly spending limit of ₹$limitRupees has been exceeded by ₹$excess (Total: ₹$spentRupees)."
                    else "Your monthly spending limit of ₹$limitRupees has been reached."
                )
            }
            90 -> Pair("🚨 90% Spending Limit Reached", "You've used 90% of your monthly spending limit (₹$spentRupees / ₹$limitRupees).")
            80 -> Pair("⚠️ 80% Spending Limit Reached", "You've used 80% of your monthly spending limit (₹$spentRupees / ₹$limitRupees).")
            70 -> Pair("⚠️ 70% Spending Limit Reached", "You've used 70% of your monthly spending limit (₹$spentRupees / ₹$limitRupees).")
            else -> return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            threshold,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PERSONAL_LIMIT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(1000 + threshold, notification)
    }

    /**
     * Checks family group monthly spending limit and dispatches alerts to permitted members.
     */
    fun checkAndNotifyFamilyLimit(
        familyId: String,
        familyName: String,
        currentSpentCents: Long,
        limitCents: Long,
        yearMonth: String = getYearMonthKey()
    ): Int? {
        if (limitCents <= 0L) return null

        val percentage = (currentSpentCents.toDouble() / limitCents.toDouble() * 100.0).toInt()
        val key = "$familyId|$yearMonth|FAMILY"
        val sentSet = triggeredThresholds.getOrPut(key) { ConcurrentHashMap.newKeySet() }

        val thresholdToTrigger = when {
            percentage >= 100 && !sentSet.contains(100) -> {
                sentSet.addAll(listOf(70, 80, 90, 100))
                100
            }
            percentage >= 90 && !sentSet.contains(90) -> {
                sentSet.addAll(listOf(70, 80, 90))
                90
            }
            percentage >= 80 && !sentSet.contains(80) -> {
                sentSet.addAll(listOf(70, 80))
                80
            }
            percentage >= 70 && !sentSet.contains(70) -> {
                sentSet.add(70)
                70
            }
            else -> null
        }

        if (thresholdToTrigger != null) {
            sendFamilyNotification(familyName, thresholdToTrigger, currentSpentCents, limitCents)
        }

        return thresholdToTrigger
    }

    private fun sendFamilyNotification(familyName: String, threshold: Int, spentCents: Long, limitCents: Long) {
        val spentRupees = spentCents / 100
        val limitRupees = limitCents / 100

        val (title, body) = when (threshold) {
            100 -> {
                val excess = (spentCents - limitCents).coerceAtLeast(0L) / 100
                Pair(
                    "🚨 Family Monthly Limit Exceeded",
                    if (excess > 0) "$familyName limit of ₹$limitRupees exceeded by ₹$excess (Total: ₹$spentRupees)."
                    else "$familyName monthly spending limit of ₹$limitRupees has been reached."
                )
            }
            90 -> Pair("🚨 Family 90% Spending Alert", "$familyName has reached 90% of the monthly limit (₹$spentRupees / ₹$limitRupees).")
            80 -> Pair("⚠️ Family 80% Spending Alert", "$familyName has reached 80% of the monthly limit (₹$spentRupees / ₹$limitRupees).")
            70 -> Pair("⚠️ Family 70% Spending Alert", "$familyName has reached 70% of the monthly limit (₹$spentRupees / ₹$limitRupees).")
            else -> return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2000 + threshold,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FAMILY_LIMIT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(2000 + threshold, notification)
    }

    fun checkAndNotifyFamilyCategoryBudget(
        familyId: String,
        familyName: String,
        categoryName: String,
        spentCents: Long,
        limitCents: Long,
        yearMonth: String = getYearMonthKey()
    ): Int? {
        if (limitCents <= 0L) return null

        val percentage = (spentCents.toDouble() / limitCents.toDouble() * 100.0).toInt()
        val key = "$familyId|$yearMonth|CAT_BUDGET|$categoryName"
        val sentSet = triggeredThresholds.getOrPut(key) { ConcurrentHashMap.newKeySet() }

        val thresholdToTrigger = when {
            percentage >= 100 && !sentSet.contains(100) -> 100
            percentage >= 80 && !sentSet.contains(80) -> 80
            else -> null
        }

        if (thresholdToTrigger != null) {
            sentSet.add(thresholdToTrigger)
            val spentRupees = spentCents / 100
            val limitRupees = limitCents / 100
            val title = if (thresholdToTrigger >= 100) "🚨 Family Budget Exceeded: $categoryName" else "⚠️ Family Budget Alert: $categoryName (80%)"
            val body = "$familyName $categoryName budget reached ₹$spentRupees / ₹$limitRupees."

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                3000 + categoryName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_FAMILY_LIMIT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager?.notify(3000 + Math.abs(categoryName.hashCode() % 1000), notification)
        }

        return thresholdToTrigger
    }
}
