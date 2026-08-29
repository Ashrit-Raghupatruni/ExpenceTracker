package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialSafetyScoreEngineTest {

    private val engine = FinancialSafetyScoreEngine()

    @Test
    fun `test zero data returns null respecting no fake data rule`() {
        val result = engine.calculateScore(
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            currentMonthExpensesCents = 0L,
            records = emptyList()
        )
        assertNull(result)
    }

    @Test
    fun `test healthy budget and savings produces high score`() {
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "u1",
                userId = "usr",
                amountCents = 500000L, // ₹5,000 groceries
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = System.currentTimeMillis(),
                categoryName = "Groceries",
                categoryColor = "#10B981"
            )
        )

        val result = engine.calculateScore(
            monthlyIncomeCents = 10000000L, // ₹100,000 income
            savingsTargetCents = 2500000L,  // ₹25,000 savings (25%)
            currentMonthExpensesCents = 1500000L, // ₹15,000 expenses
            records = records
        )

        assertNotNull(result)
        assertTrue(result!!.score >= 70)
        assertEquals("Excellent", result.rating)
    }
}
