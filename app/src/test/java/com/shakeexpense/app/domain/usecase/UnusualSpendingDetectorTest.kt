package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SubscriptionPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnusualSpendingDetectorTest {

    private val detector = UnusualSpendingDetector()

    @Test
    fun `test free tier returns no alerts due to entitlement gating`() {
        val now = System.currentTimeMillis()
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "u1",
                userId = "usr",
                amountCents = 10000000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now,
                categoryName = "Shopping",
                categoryColor = "#EC4899"
            )
        )
        val alerts = detector.detectAlerts(records, SubscriptionPlan.FREE)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `test detector identifies rapid duplicate transactions within 10 minutes`() {
        val now = System.currentTimeMillis()
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "u1",
                userId = "usr",
                amountCents = 25000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now - 60000L, // 1 min ago
                categoryName = "Food",
                categoryColor = "#F59E0B",
                customName = "Swiggy"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "u2",
                userId = "usr",
                amountCents = 25000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now - 10000L, // 10 sec ago
                categoryName = "Food",
                categoryColor = "#F59E0B",
                customName = "Swiggy"
            )
        )

        val alerts = detector.detectAlerts(records, SubscriptionPlan.PLUS)
        assertEquals(1, alerts.size)
        assertTrue(alerts[0].title.contains("Possible Duplicate"))
    }
}
