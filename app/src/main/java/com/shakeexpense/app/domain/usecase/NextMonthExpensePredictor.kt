package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import java.util.Calendar

enum class PredictionConfidence {
    LOW,
    MODERATE,
    HIGH
}

sealed class PredictionResult {
    data class InsufficientData(val message: String) : PredictionResult()
    data class Success(
        val predictedNextMonthCents: Long,
        val categoryBreakdown: Map<Long, Long>,
        val confidence: PredictionConfidence,
        val explanation: String
    ) : PredictionResult()
}

class NextMonthExpensePredictor {

    fun predictFromRecords(
        records: List<com.shakeexpense.app.domain.model.ExpenseRecordItem>,
        activeRecurring: List<RecurringPaymentEntity>,
        monthlyIncomeCents: Long = 0L,
        now: Long = System.currentTimeMillis()
    ): PredictionResult {
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
        if (debits.size < 5) {
            return PredictionResult.InsufficientData("More spending history is needed to generate a reliable prediction.")
        }

        val cal = Calendar.getInstance()
        val monthlyGroups = debits.groupBy {
            cal.timeInMillis = if (it.timestamp > 0L) it.timestamp else now
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
        }

        val recurringMonthlyCents = activeRecurring.sumOf {
            if (it.cadence.equals("YEARLY", ignoreCase = true)) it.amountCents / 12L else it.amountCents
        }

        return when {
            monthlyGroups.size < 2 -> {
                val currentMonthDebits: Long = debits.sumOf { item -> item.amountCents }
                val currentDay = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH)
                val maxDays = Calendar.getInstance().apply { timeInMillis = now }.getActualMaximum(Calendar.DAY_OF_MONTH)

                val projectedCurrentMonth = if (currentDay > 0) (currentMonthDebits / currentDay.toLong()) * maxDays.toLong() else currentMonthDebits
                val predicted = maxOf(projectedCurrentMonth, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { item -> item.amountCents } },
                    confidence = PredictionConfidence.MODERATE,
                    explanation = "Estimated using your current daily spending run-rate and known recurring bills."
                )
            }
            monthlyGroups.size in 2..3 -> {
                val totals: List<Long> = monthlyGroups.values.map { monthList -> monthList.sumOf { item -> item.amountCents } }
                val avg = totals.average().toLong()
                val predicted = maxOf(avg, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) ->
                        (list.sumOf { item -> item.amountCents } / monthlyGroups.size.toLong())
                    },
                    confidence = PredictionConfidence.MODERATE,
                    explanation = "Calculated from your 2-3 month moving average and recurring subscriptions."
                )
            }
            else -> {
                val totals: List<Long> = monthlyGroups.values.map { monthList -> monthList.sumOf { item -> item.amountCents } }
                val weights = listOf(0.5, 0.3, 0.2)
                val recentTotals = totals.takeLast(3)
                val weightedSum = recentTotals.zip(weights).sumOf { (amount, weight) -> amount.toDouble() * weight }.toLong()
                val predicted = maxOf(weightedSum, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) ->
                        (list.sumOf { item -> item.amountCents } / monthlyGroups.size.toLong())
                    },
                    confidence = PredictionConfidence.HIGH,
                    explanation = "High-confidence forecast based on multi-month weighted trend analysis."
                )
            }
        }
    }

    fun predict(
        allExpenses: List<ExpenseEntity>,
        activeRecurring: List<RecurringPaymentEntity>,
        monthlyIncomeCents: Long = 0L,
        now: Long = System.currentTimeMillis()
    ): PredictionResult {
        val debits = allExpenses.filter { it.type.equals("DEBIT", ignoreCase = true) }
        if (debits.size < 5) {
            return PredictionResult.InsufficientData("More spending history is needed to generate a reliable prediction.")
        }

        // Group expenses by Year-Month
        val cal = Calendar.getInstance()
        val monthlyGroups = debits.groupBy {
            cal.timeInMillis = it.timestamp
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
        }

        val recurringMonthlyCents = activeRecurring.sumOf {
            if (it.cadence.equals("YEARLY", ignoreCase = true)) it.amountCents / 12L else it.amountCents
        }

        return when {
            monthlyGroups.size < 2 -> {
                // Level 1 / Early Level 2: Current month only
                val currentMonthDebits = debits.sumOf { it.amountCents }
                val currentDay = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_MONTH)
                val maxDays = Calendar.getInstance().apply { timeInMillis = now }.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Extrapolate current month run rate
                val projectedCurrentMonth = if (currentDay > 0) (currentMonthDebits / currentDay) * maxDays else currentMonthDebits
                val predicted = maxOf(projectedCurrentMonth, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amountCents } },
                    confidence = PredictionConfidence.MODERATE,
                    explanation = "Estimated using your current daily spending run-rate and known recurring bills."
                )
            }
            monthlyGroups.size in 2..3 -> {
                // Level 2: Statistical weighted moving average across 2-3 months
                val totals: List<Long> = monthlyGroups.values.map { monthList -> monthList.sumOf { item -> item.amountCents } }
                val avg = totals.average().toLong()
                val predicted = maxOf(avg, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) ->
                        (list.sumOf { item -> item.amountCents } / monthlyGroups.size.toLong())
                    },
                    confidence = PredictionConfidence.MODERATE,
                    explanation = "Calculated from your 2-3 month moving average and recurring subscriptions."
                )
            }
            else -> {
                // Level 3: Sufficient historical data (>3 months)
                val totals: List<Long> = monthlyGroups.values.map { monthList -> monthList.sumOf { item -> item.amountCents } }
                val weights = listOf(0.5, 0.3, 0.2)
                val recentTotals = totals.takeLast(3)
                val weightedSum = recentTotals.zip(weights).sumOf { (amount, weight) -> amount.toDouble() * weight }.toLong()
                val predicted = maxOf(weightedSum, recurringMonthlyCents)

                PredictionResult.Success(
                    predictedNextMonthCents = predicted,
                    categoryBreakdown = debits.groupBy { it.categoryId }.mapValues { (_, list) ->
                        (list.sumOf { item -> item.amountCents } / monthlyGroups.size.toLong())
                    },
                    confidence = PredictionConfidence.HIGH,
                    explanation = "High-confidence forecast based on multi-month weighted trend analysis."
                )
            }
        }
    }
}
