package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan

data class UnusualSpendingAlert(
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val transactionUuid: String? = null
)

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

class UnusualSpendingDetector(
    private val entitlementManager: EntitlementManager = EntitlementManager()
) {

    fun detectAlerts(
        records: List<ExpenseRecordItem>,
        plan: SubscriptionPlan
    ): List<UnusualSpendingAlert> {
        if (!entitlementManager.canAccess(plan, FeatureCapability.UNUSUAL_SPENDING_ALERTS)) {
            return emptyList()
        }

        if (records.size < 2) return emptyList()

        val alerts = mutableListOf<UnusualSpendingAlert>()
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
        if (debits.isEmpty()) return emptyList()

        // 1. Duplicate transaction detection: same amount within 10 minutes (600,000 ms)
        for (i in 0 until debits.size - 1) {
            val current = debits[i]
            val next = debits[i + 1]
            if (current.amountCents == next.amountCents &&
                Math.abs(current.timestamp - next.timestamp) < 10 * 60 * 1000L
            ) {
                alerts.add(
                    UnusualSpendingAlert(
                        title = "Possible Duplicate Transaction",
                        description = "Detected two identical transactions of ₹${current.amountCents / 100} logged within minutes.",
                        severity = AlertSeverity.WARNING,
                        transactionUuid = current.expenseUuid
                    )
                )
                break
            }
        }

        // 2. Large transaction spike: single transaction > 3x average debit
        val avgDebit = debits.map { it.amountCents }.average()
        val latest = debits.firstOrNull()
        if (latest != null && latest.amountCents > (avgDebit * 3.0).toLong() && latest.amountCents > 200000L) {
            alerts.add(
                UnusualSpendingAlert(
                    title = "Unusual transaction",
                    description = "Recent transaction of ₹${latest.amountCents / 100} (${latest.customName ?: latest.categoryName}) is significantly higher than your average spending of ₹${avgDebit.toLong() / 100}.",
                    severity = AlertSeverity.WARNING,
                    transactionUuid = latest.expenseUuid
                )
            )
        }

        // 3. Category spending spike
        val recentWindow = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val recentDebits = debits.filter { it.timestamp >= recentWindow }
        val categoryCounts = recentDebits.groupBy { it.categoryName }
        for ((cat, items) in categoryCounts) {
            if (items.size >= 4) {
                alerts.add(
                    UnusualSpendingAlert(
                        title = "Spending pattern changed",
                        description = "You've made ${items.size} $cat transactions in the past 24 hours totaling ₹${items.sumOf { it.amountCents } / 100}.",
                        severity = AlertSeverity.INFO
                    )
                )
                break
            }
        }

        return alerts.take(3)
    }
}
