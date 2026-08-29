package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem

data class SafetyScoreResult(
    val score: Int, // 0 to 100
    val rating: String, // "Excellent", "Good", "Fair", "Needs Attention"
    val savingsRatePercentage: Int,
    val budgetAdherencePercentage: Int,
    val summary: String,
    val spendingSubScore: String = "🟢 Healthy",
    val budgetSubScore: String = "🟢 Healthy",
    val recurringSubScore: String = "🟢 Healthy",
    val riskSignalsSubScore: String = "🟢 None"
)

class FinancialSafetyScoreEngine {

    fun calculateScore(
        monthlyIncomeCents: Long,
        savingsTargetCents: Long,
        currentMonthExpensesCents: Long,
        records: List<ExpenseRecordItem>
    ): SafetyScoreResult? {
        if (records.isEmpty() && monthlyIncomeCents <= 0L && currentMonthExpensesCents <= 0L) {
            return null // Strict no-fake-data rule
        }

        var score = 50 // baseline

        // 1. Savings Target Factor (up to +25 or -20)
        val savingsRate = if (monthlyIncomeCents > 0L && savingsTargetCents > 0L) {
            ((savingsTargetCents.toDouble() / monthlyIncomeCents.toDouble()) * 100).toInt()
        } else 0

        score += when {
            savingsRate >= 30 -> 25
            savingsRate >= 20 -> 20
            savingsRate >= 10 -> 10
            savingsRate > 0 -> 5
            else -> 0
        }

        // 2. Budget Adherence Factor (up to +20 or -25)
        val effectiveBudget = if (monthlyIncomeCents > 0L) {
            (monthlyIncomeCents - savingsTargetCents).coerceAtLeast(0L)
        } else 0L

        val budgetUsage = if (effectiveBudget > 0L) {
            ((currentMonthExpensesCents.toDouble() / effectiveBudget.toDouble()) * 100).toInt()
        } else 0

        score += when {
            effectiveBudget <= 0L -> 0
            budgetUsage <= 70 -> 20
            budgetUsage <= 85 -> 10
            budgetUsage <= 100 -> 0
            budgetUsage <= 115 -> -15
            else -> -25
        }

        // 3. Discretionary Spending Factor (up to +15 or -15)
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
        val discretionaryDebits = debits.filter {
            it.categoryName.equals("Shopping", ignoreCase = true) ||
                it.categoryName.equals("Entertainment", ignoreCase = true) ||
                it.categoryName.equals("Others", ignoreCase = true)
        }
        val discretionaryCents = discretionaryDebits.sumOf { it.amountCents }
        val totalDebitsCents = debits.sumOf { it.amountCents }

        val discretionaryRatio = if (totalDebitsCents > 0L) {
            ((discretionaryCents.toDouble() / totalDebitsCents.toDouble()) * 100).toInt()
        } else 0

        if (totalDebitsCents > 0L) {
            score += when {
                discretionaryRatio <= 25 -> 10
                discretionaryRatio <= 40 -> 0
                else -> -10
            }
        }

        val clampedScore = score.coerceIn(10, 100)

        val (rating, summary) = when {
            clampedScore >= 80 -> Pair(
                "Excellent",
                "Your financial safety is rock solid. Healthy savings rate and disciplined category spending."
            )
            clampedScore >= 65 -> Pair(
                "Good",
                "Your spending is within safe limits. Maintain this pace to hit your monthly savings goal."
            )
            clampedScore >= 50 -> Pair(
                "Fair",
                "Spending is moderately high. Check discretionary categories to prevent exceeding your limit."
            )
            else -> Pair(
                "Needs Attention",
                "Current spending rate exceeds safe parameters. Consider trimming non-essential expenses."
            )
        }

        // Dynamic Sub-Scores derived authentically from financial records
        val spendingSubScore = when {
            totalDebitsCents <= 0L -> "🟢 Healthy"
            discretionaryRatio <= 25 -> "🟢 Healthy"
            discretionaryRatio <= 45 -> "🟡 Moderate"
            else -> "🔴 High"
        }

        val budgetSubScore = when {
            effectiveBudget <= 0L -> "🟢 Healthy"
            budgetUsage <= 70 -> "🟢 Healthy"
            budgetUsage <= 90 -> "🟡 Review"
            else -> "🔴 Exceeded"
        }

        val recurringDebits = debits.filter {
            it.transactionSource.equals("RECURRING", ignoreCase = true) ||
                it.customName?.contains("Subscription", ignoreCase = true) == true
        }
        val recurringCents = recurringDebits.sumOf { it.amountCents }
        val recurringRatio = if (totalDebitsCents > 0L) ((recurringCents.toDouble() / totalDebitsCents.toDouble()) * 100).toInt() else 0
        val recurringSubScore = when {
            recurringRatio <= 20 -> "🟢 Healthy"
            recurringRatio <= 35 -> "🟡 Review"
            else -> "🔴 Heavy"
        }

        val riskSignalsSubScore = when {
            effectiveBudget > 0L && budgetUsage > 100 -> "🔴 Alert"
            (effectiveBudget > 0L && budgetUsage > 85) || discretionaryRatio > 50 -> "🟡 Warning"
            else -> "🟢 None"
        }

        return SafetyScoreResult(
            score = clampedScore,
            rating = rating,
            savingsRatePercentage = savingsRate,
            budgetAdherencePercentage = budgetUsage,
            summary = summary,
            spendingSubScore = spendingSubScore,
            budgetSubScore = budgetSubScore,
            recurringSubScore = recurringSubScore,
            riskSignalsSubScore = riskSignalsSubScore
        )
    }
}
