package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import java.util.Calendar

data class SafeToSpendSummary(
    val monthlyIncomeCents: Long,
    val totalSpentThisMonthCents: Long,
    val committedBillsCents: Long,
    val savingsTargetCents: Long,
    val safeToSpendMonthlyCents: Long,
    val safeToSpendTodayCents: Long,
    val daysRemainingInCycle: Int,
    val cycleEndTimestamp: Long,
    val statusLabel: String
)

class SafeToSpendCalculator {

    fun calculate(
        profile: FinancialProfileEntity?,
        totalSpentThisMonthCents: Long,
        activeRecurring: List<RecurringPaymentEntity>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): SafeToSpendSummary {
        val incomeCents = profile?.monthlyIncomeCents ?: 0L
        val savingsTargetCents = profile?.savingsTargetCents ?: 0L
        val billingCycleDay = profile?.billingCycleDay?.coerceIn(1, 28) ?: 1

        // Calculate days remaining in billing cycle
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val daysRemaining: Int = if (currentDay >= billingCycleDay) {
            maxDaysInMonth - currentDay + billingCycleDay
        } else {
            billingCycleDay - currentDay
        }.coerceAtLeast(1)

        val cycleEndCal = Calendar.getInstance().apply {
            timeInMillis = currentTimestamp
            add(Calendar.DAY_OF_MONTH, daysRemaining)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val cycleEndTimestamp = cycleEndCal.timeInMillis

        // Committed recurring payments & bills for remaining cycle
        val committedBillsCents = activeRecurring.sumOf { recurring ->
            if (recurring.cadence.equals("YEARLY", ignoreCase = true)) {
                recurring.amountCents / 12L
            } else {
                recurring.amountCents
            }
        }

        // Effective base: if income is set, use it; otherwise 0 (no fake baseline)
        val netSafeMonthlyCents = if (incomeCents > 0L) {
            maxOf(
                0L,
                incomeCents - (totalSpentThisMonthCents + committedBillsCents + savingsTargetCents)
            )
        } else {
            0L
        }

        val safeTodayCents = if (incomeCents > 0L) {
            maxOf(0L, netSafeMonthlyCents / daysRemaining)
        } else {
            0L
        }

        val statusLabel = when {
            incomeCents <= 0L -> "Configure monthly income in Setup"
            netSafeMonthlyCents == 0L -> "Exceeded planned budget"
            safeTodayCents > 100000L -> "Comfortable balance"
            safeTodayCents > 30000L -> "On track"
            else -> "Strict daily limit"
        }

        return SafeToSpendSummary(
            monthlyIncomeCents = incomeCents,
            totalSpentThisMonthCents = totalSpentThisMonthCents,
            committedBillsCents = committedBillsCents,
            savingsTargetCents = savingsTargetCents,
            safeToSpendMonthlyCents = netSafeMonthlyCents,
            safeToSpendTodayCents = safeTodayCents,
            daysRemainingInCycle = daysRemaining,
            cycleEndTimestamp = cycleEndTimestamp,
            statusLabel = statusLabel
        )
    }
}
