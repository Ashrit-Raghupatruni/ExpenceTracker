package com.shakeexpense.app.domain.model

data class MonthlyCategorySubtotal(
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val totalCents: Long,
    val transactionCount: Int
) {
    val totalFormatted: String
        get() = "₹${totalCents / 100}"
}

data class DailyExpenseGroup(
    val dateKey: String, // e.g. "2026-08-27"
    val displayDate: String, // e.g. "Aug 27"
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val transactions: List<ExpenseRecordItem>
) {
    val totalDebitFormatted: String
        get() = "₹${totalDebitCents / 100}"
}

data class MonthlyExpenseSummary(
    val yearMonth: String, // e.g. "2026-08"
    val displayMonth: String, // e.g. "August 2026"
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val netCents: Long,
    val transactionCount: Int,
    val categoryBreakdown: List<MonthlyCategorySubtotal>,
    val transactions: List<ExpenseRecordItem>
) {
    val totalDebitFormatted: String
        get() = "₹${totalDebitCents / 100}"

    val totalCreditFormatted: String
        get() = "₹${totalCreditCents / 100}"

    val netFormatted: String
        get() {
            val prefix = if (netCents >= 0) "+₹" else "-₹"
            return "$prefix${kotlin.math.abs(netCents) / 100}"
        }

    val dailyGroups: List<DailyExpenseGroup>
        get() {
            val keyFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val displayFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)

            return transactions
                .groupBy { keyFormat.format(java.util.Date(it.timestamp)) }
                .map { (dateKey, txns) ->
                    val sampleTs = txns.firstOrNull()?.timestamp ?: System.currentTimeMillis()
                    val displayDate = displayFormat.format(java.util.Date(sampleTs))
                    val debit = txns.filter { !it.transactionType.equals("CREDIT", ignoreCase = true) }.sumOf { it.amountCents }
                    val credit = txns.filter { it.transactionType.equals("CREDIT", ignoreCase = true) }.sumOf { it.amountCents }
                    DailyExpenseGroup(
                        dateKey = dateKey,
                        displayDate = displayDate,
                        totalDebitCents = debit,
                        totalCreditCents = credit,
                        transactions = txns
                    )
                }
                .sortedByDescending { it.dateKey }
        }
}
