package com.shakeexpense.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthlySpendingLimitManagerTest {

    private val manager = MonthlySpendingLimitManager()

    @Test
    fun testRecommendedLimitCalculation_fromIncomeAndPredictions() {
        // Income ₹30,000 (3,000,000 cents), Predicted ₹22,300 (2,230,000 cents)
        val incomeCents = 3000000L
        val predictedCents = 2230000L
        val currentSpent = 1800000L // ₹18,000

        val state = manager.calculateLimitState(
            monthlyIncomeCents = incomeCents,
            predictedExpensesCents = predictedCents,
            currentMonthExpensesCents = currentSpent
        )

        // 20% savings target from ₹30,000 = ₹24,000; or 98% of ₹22,300 = ₹21,854
        assertTrue(state.systemRecommendedLimitCents > 0L)
        assertTrue(state.potentialSavingsCents > 0L)
        assertEquals(state.systemRecommendedLimitCents, state.activeMonthlyLimitCents)
    }

    @Test
    fun testUserSelectedLimit_overridesSystemRecommendation() {
        val incomeCents = 3000000L
        val predictedCents = 2230000L
        val userLimitCents = 2000000L // ₹20,000

        val state = manager.calculateLimitState(
            monthlyIncomeCents = incomeCents,
            predictedExpensesCents = predictedCents,
            currentMonthExpensesCents = 1800000L,
            userSelectedLimitCents = userLimitCents
        )

        // Active limit should be exactly ₹20,000
        assertEquals(userLimitCents, state.activeMonthlyLimitCents)
        assertEquals(90.0, state.usagePercentage, 0.1)
        assertEquals(SpendingLimitWarningLevel.NINETY_PERCENT, state.warningLevel)
    }

    @Test
    fun testLimitExceeded_whenExpensesSurpassLimit() {
        val state = manager.calculateLimitState(
            monthlyIncomeCents = 2000000L,
            predictedExpensesCents = 1500000L,
            currentMonthExpensesCents = 2100000L, // Exceeded
            userSelectedLimitCents = 2000000L
        )

        assertEquals(SpendingLimitWarningLevel.EXCEEDED, state.warningLevel)
        assertTrue(state.usagePercentage > 100.0)
    }
}
