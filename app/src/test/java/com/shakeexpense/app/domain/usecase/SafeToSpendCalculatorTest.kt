package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SafeToSpendCalculatorTest {

    private val calculator = SafeToSpendCalculator()

    @Test
    fun testSafeToSpendCalculation_withIncomeAndBills() {
        val profile = FinancialProfileEntity(
            userId = "user_123",
            monthlyIncomeCents = 5000000L, // ₹50,000
            savingsTargetCents = 1000000L, // ₹10,000
            billingCycleDay = 1
        )

        val recurring = listOf(
            RecurringPaymentEntity(
                id = 1L,
                userId = "user_123",
                name = "Rent",
                amountCents = 1500000L, // ₹15,000
                cadence = "MONTHLY"
            ),
            RecurringPaymentEntity(
                id = 2L,
                userId = "user_123",
                name = "Netflix",
                amountCents = 64900L, // ₹649
                cadence = "MONTHLY"
            )
        )

        val totalSpentCents = 800000L // ₹8,000 spent so far

        // Fixed mid-month date: 15th
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 15, 12, 0, 0)
        }

        val summary = calculator.calculate(
            profile = profile,
            totalSpentThisMonthCents = totalSpentCents,
            activeRecurring = recurring,
            currentTimestamp = cal.timeInMillis
        )

        // ₹50,000 - (₹8,000 spent + ₹15,649 recurring + ₹10,000 savings) = ₹16,351 safe remaining
        val expectedSafeMonthly = 5000000L - (800000L + 1564900L + 1000000L)
        assertEquals(expectedSafeMonthly, summary.safeToSpendMonthlyCents)
        assertTrue(summary.safeToSpendTodayCents > 0L)
    }

    @Test
    fun testSafeToSpendCalculation_exceededBudget() {
        val profile = FinancialProfileEntity(
            userId = "user_123",
            monthlyIncomeCents = 2000000L, // ₹20,000
            savingsTargetCents = 500000L,  // ₹5,000
            billingCycleDay = 1
        )

        val totalSpentCents = 2500000L // ₹25,000 (exceeded)

        val summary = calculator.calculate(
            profile = profile,
            totalSpentThisMonthCents = totalSpentCents,
            activeRecurring = emptyList()
        )

        assertEquals(0L, summary.safeToSpendMonthlyCents)
        assertEquals(0L, summary.safeToSpendTodayCents)
        assertEquals("Exceeded planned budget", summary.statusLabel)
    }

    @Test
    fun testSafeToSpendCalculation_withoutIncome_returnsZeroSafeToSpend() {
        val profile = FinancialProfileEntity(
            userId = "user_123",
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            billingCycleDay = 1
        )

        val summary = calculator.calculate(
            profile = profile,
            totalSpentThisMonthCents = 500000L,
            activeRecurring = emptyList()
        )

        assertEquals(0L, summary.safeToSpendMonthlyCents)
        assertEquals(0L, summary.safeToSpendTodayCents)
        assertEquals("Configure monthly income in Setup", summary.statusLabel)
    }
}
