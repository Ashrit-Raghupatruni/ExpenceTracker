package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.MonthlyCategorySubtotal
import com.shakeexpense.app.domain.model.MonthlyExpenseSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetMonthlyExpenseHistoryUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(targetUserId: String? = null): Flow<List<MonthlyExpenseSummary>> {
        val stream = if (!targetUserId.isNullOrBlank()) {
            expenseRepository.getExpensesByUserId(targetUserId)
        } else {
            expenseRepository.getSpreadsheetStream()
        }
        return stream.map { records ->
            groupExpensesByMonth(records)
        }
    }

    fun groupExpensesByMonth(records: List<ExpenseRecordItem>): List<MonthlyExpenseSummary> {
        val keyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        val grouped = records.groupBy { record ->
            keyFormat.format(Date(record.timestamp))
        }

        // Sort months descending (latest first)
        val sortedKeys = grouped.keys.sortedDescending()

        return sortedKeys.map { yearMonthKey ->
            val monthRecords = grouped[yearMonthKey] ?: emptyList()
            val sampleTimestamp = monthRecords.firstOrNull()?.timestamp ?: System.currentTimeMillis()
            val displayMonth = displayFormat.format(Date(sampleTimestamp))

            var totalDebit = 0L
            var totalCredit = 0L

            for (rec in monthRecords) {
                if (rec.transactionType.equals("CREDIT", ignoreCase = true)) {
                    totalCredit += rec.amountCents
                } else {
                    totalDebit += rec.amountCents
                }
            }

            val netCents = totalCredit - totalDebit

            // Category breakdown for this month
            val categoryBreakdown = monthRecords
                .filter { !it.transactionType.equals("CREDIT", ignoreCase = true) }
                .groupBy { it.categoryId }
                .map { (catId, catRecords) ->
                    val first = catRecords.first()
                    val catTotal = catRecords.sumOf { it.amountCents }
                    MonthlyCategorySubtotal(
                        categoryId = catId,
                        categoryName = first.categoryName,
                        categoryColorHex = first.categoryColor,
                        totalCents = catTotal,
                        transactionCount = catRecords.size
                    )
                }
                .sortedByDescending { it.totalCents }

            MonthlyExpenseSummary(
                yearMonth = yearMonthKey,
                displayMonth = displayMonth,
                totalDebitCents = totalDebit,
                totalCreditCents = totalCredit,
                netCents = netCents,
                transactionCount = monthRecords.size,
                categoryBreakdown = categoryBreakdown,
                transactions = monthRecords
            )
        }
    }
}
