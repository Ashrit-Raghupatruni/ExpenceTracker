package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class SpendingTotals(
    val todayDebitCents: Long = 0L,
    val thisMonthDebitCents: Long = 0L,
    val allTimeDebitCents: Long = 0L,
    val allTimeCreditCents: Long = 0L,
    val transactionCount: Int = 0
) {
    val formattedTodayDebit: Double
        get() = todayDebitCents / 100.0

    val formattedMonthDebit: Double
        get() = thisMonthDebitCents / 100.0

    val formattedAllTimeDebit: Double
        get() = allTimeDebitCents / 100.0

    val formattedAllTimeCredit: Double
        get() = allTimeCreditCents / 100.0
}

class GetSpreadsheetStreamUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<ExpenseRecordItem>> =
        expenseRepository.getSpreadsheetStream()
}

class GetCategoryBreakdownUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<CategorySubtotal>> =
        expenseRepository.getAllCategoryBreakdown()
}

class GetSpendingTotalsUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<SpendingTotals> {
        return expenseRepository.getSpreadsheetStream().map { records ->
            calculateTotals(records, System.currentTimeMillis())
        }
    }

    fun calculateTotals(records: List<ExpenseRecordItem>, currentTimestamp: Long): SpendingTotals {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis

        var todayDebit = 0L
        var monthDebit = 0L
        var allTimeDebit = 0L
        var allTimeCredit = 0L

        records.forEach { record ->
            if (record.transactionType.equals("DEBIT", ignoreCase = true)) {
                allTimeDebit += record.amountCents
                if (record.timestamp >= startOfMonth) {
                    monthDebit += record.amountCents
                }
                if (record.timestamp >= startOfToday) {
                    todayDebit += record.amountCents
                }
            } else if (record.transactionType.equals("CREDIT", ignoreCase = true)) {
                allTimeCredit += record.amountCents
            }
        }

        return SpendingTotals(
            todayDebitCents = todayDebit,
            thisMonthDebitCents = monthDebit,
            allTimeDebitCents = allTimeDebit,
            allTimeCreditCents = allTimeCredit,
            transactionCount = records.size
        )
    }
}
