package com.shakeexpense.app.domain.usecase

enum class SpendingLimitWarningLevel {
    NORMAL,
    SEVENTY_PERCENT,
    EIGHTY_PERCENT,
    NINETY_PERCENT,
    EXCEEDED
}

data class SpendingLimitState(
    val systemRecommendedLimitCents: Long,
    val userSelectedLimitCents: Long?,
    val activeMonthlyLimitCents: Long,
    val currentMonthExpensesCents: Long,
    val usagePercentage: Double,
    val potentialSavingsCents: Long,
    val warningLevel: SpendingLimitWarningLevel,
    val explanation: String
)

class MonthlySpendingLimitManager {

    fun calculateLimitState(
        monthlyIncomeCents: Long,
        predictedExpensesCents: Long,
        currentMonthExpensesCents: Long,
        userSelectedLimitCents: Long? = null
    ): SpendingLimitState {
        val recommendedLimit = when {
            monthlyIncomeCents > 0L && predictedExpensesCents > 0L -> {
                // Goal: 20% savings or slight reduction below predicted expenses
                val targetFromIncome = (monthlyIncomeCents * 0.80).toLong()
                minOf(targetFromIncome, (predictedExpensesCents * 0.98).toLong())
            }
            monthlyIncomeCents > 0L -> (monthlyIncomeCents * 0.80).toLong()
            predictedExpensesCents > 0L -> (predictedExpensesCents * 0.95).toLong()
            else -> 0L
        }

        val activeLimit = userSelectedLimitCents ?: recommendedLimit
        val potentialSavings = if (monthlyIncomeCents > activeLimit) monthlyIncomeCents - activeLimit else 0L

        val usagePct = if (activeLimit > 0L) {
            (currentMonthExpensesCents.toDouble() / activeLimit.toDouble()) * 100.0
        } else {
            0.0
        }

        val warningLevel = when {
            activeLimit <= 0L -> SpendingLimitWarningLevel.NORMAL
            usagePct >= 100.0 -> SpendingLimitWarningLevel.EXCEEDED
            usagePct >= 90.0 -> SpendingLimitWarningLevel.NINETY_PERCENT
            usagePct >= 80.0 -> SpendingLimitWarningLevel.EIGHTY_PERCENT
            usagePct >= 70.0 -> SpendingLimitWarningLevel.SEVENTY_PERCENT
            else -> SpendingLimitWarningLevel.NORMAL
        }

        val explanation = when {
            userSelectedLimitCents != null -> "Custom limit set by you."
            monthlyIncomeCents > 0L -> "Calculated to preserve a 20% monthly savings buffer from your fixed income."
            predictedExpensesCents > 0L -> "Optimized to keep spending slightly below your predicted monthly expenses."
            else -> "Set an income or record expenses to generate an automatic monthly limit."
        }

        return SpendingLimitState(
            systemRecommendedLimitCents = recommendedLimit,
            userSelectedLimitCents = userSelectedLimitCents,
            activeMonthlyLimitCents = activeLimit,
            currentMonthExpensesCents = currentMonthExpensesCents,
            usagePercentage = usagePct,
            potentialSavingsCents = potentialSavings,
            warningLevel = warningLevel,
            explanation = explanation
        )
    }
}
