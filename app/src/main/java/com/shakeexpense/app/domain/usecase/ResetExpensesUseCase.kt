package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.sync.SyncEngine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ResetPeriod {
    TODAY,
    THIS_WEEK,
    SPECIFIC_DATE
}

data class ResetCandidateInfo(
    val period: ResetPeriod,
    val periodLabel: String,
    val startTime: Long,
    val endTime: Long,
    val expenseCount: Int,
    val totalAmountCents: Long,
    val candidateUuids: List<String>
)

data class ResetResult(
    val isSuccess: Boolean,
    val deletedCount: Int,
    val deletedAmountCents: Long,
    val errorMessage: String? = null
)

class ResetExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
    private val syncEngine: SyncEngine? = null
) {
    suspend fun getResetCandidateInfo(
        userId: String,
        period: ResetPeriod,
        specificDateMillis: Long? = null
    ): ResetCandidateInfo {
        val (startTime, endTime, label) = computeTimeRange(period, specificDateMillis)
        val candidateEntities = expenseRepository.getExpensesInTimeRange(userId, startTime, endTime)
        val count = candidateEntities.size
        val totalCents = candidateEntities.filter { it.type.equals("DEBIT", ignoreCase = true) }.sumOf { it.amountCents }
        val uuids = candidateEntities.map { it.uuid }

        return ResetCandidateInfo(
            period = period,
            periodLabel = label,
            startTime = startTime,
            endTime = endTime,
            expenseCount = count,
            totalAmountCents = totalCents,
            candidateUuids = uuids
        )
    }

    suspend fun executeReset(
        userId: String,
        candidateInfo: ResetCandidateInfo
    ): ResetResult {
        if (candidateInfo.candidateUuids.isEmpty()) {
            return ResetResult(isSuccess = true, deletedCount = 0, deletedAmountCents = 0L)
        }

        val deletedCount = expenseRepository.deleteExpensesByUuids(candidateInfo.candidateUuids)
        syncEngine?.deleteExpenses(candidateInfo.candidateUuids)

        return ResetResult(
            isSuccess = true,
            deletedCount = deletedCount,
            deletedAmountCents = candidateInfo.totalAmountCents
        )
    }

    companion object {
        fun computeTimeRange(
            period: ResetPeriod,
            specificDateMillis: Long? = null
        ): Triple<Long, Long, String> {
            val now = System.currentTimeMillis()
            when (period) {
                ResetPeriod.TODAY -> {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = now
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(start)
                    return Triple(start, end, "Today ($dateStr)")
                }
                ResetPeriod.THIS_WEEK -> {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = now
                        firstDayOfWeek = Calendar.MONDAY
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val start = cal.timeInMillis
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = now
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    val end = endCal.timeInMillis
                    val startStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(start)
                    val endStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(end)
                    return Triple(start, end, "This Week ($startStr – $endStr)")
                }
                ResetPeriod.SPECIFIC_DATE -> {
                    val targetTime = specificDateMillis ?: now
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = targetTime
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(start)
                    return Triple(start, end, "Date ($dateStr)")
                }
            }
        }
    }
}
