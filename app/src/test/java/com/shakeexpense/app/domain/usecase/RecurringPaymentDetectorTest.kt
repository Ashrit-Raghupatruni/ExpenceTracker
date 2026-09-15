package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class RecurringPaymentDetectorTest {

    private val detector = RecurringPaymentDetector()

    @Test
    fun testKnownSubscriptionDetectedImmediately() {
        val now = System.currentTimeMillis()
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "sub_1",
                userId = "user_1",
                amountCents = 64900L, // ₹649 Netflix
                transactionType = "DEBIT",
                transactionSource = "BANK_NOTIFICATION",
                timestamp = now - TimeUnit.DAYS.toMillis(5),
                categoryName = "Entertainment",
                customName = "Netflix Premium Subscription"
            )
        )

        val result = detector.detectRecurring(records)
        assertEquals(1, result.size)
        assertEquals("Netflix Premium Subscription", result[0].name)
        assertEquals(64900L, result[0].amountCents)
        assertEquals("MONTHLY", result[0].frequency)
    }

    @Test
    fun testTwoRandomSameCategoryExpensesAreNotFlaggedAsRecurring() {
        val now = System.currentTimeMillis()
        // Two coffees bought in the same category on the same day for ₹150 each
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "food_1",
                userId = "user_1",
                amountCents = 15000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now - TimeUnit.HOURS.toMillis(4),
                categoryName = "Food",
                customName = "Cafe Coffee Day"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "food_2",
                userId = "user_1",
                amountCents = 15000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now - TimeUnit.HOURS.toMillis(1),
                categoryName = "Food",
                customName = "Starbucks"
            )
        )

        val result = detector.detectRecurring(records)
        // Must NOT falsely flag as recurring
        assertEquals(0, result.size)
    }

    @Test
    fun testPeriodicMonthlyIntervalDetected() {
        val now = System.currentTimeMillis()
        // Custom landlord rent paid 30 days apart
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "rent_1",
                userId = "user_1",
                amountCents = 2500000L, // ₹25,000
                transactionType = "DEBIT",
                transactionSource = "UPI",
                timestamp = now - TimeUnit.DAYS.toMillis(60),
                categoryName = "Bills",
                customName = "House Rent"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "rent_2",
                userId = "user_1",
                amountCents = 2500000L, // ₹25,000
                transactionType = "DEBIT",
                transactionSource = "UPI",
                timestamp = now - TimeUnit.DAYS.toMillis(30),
                categoryName = "Bills",
                customName = "House Rent"
            ),
            ExpenseRecordItem(
                expenseId = 3L,
                expenseUuid = "rent_3",
                userId = "user_1",
                amountCents = 2500000L, // ₹25,000
                transactionType = "DEBIT",
                transactionSource = "UPI",
                timestamp = now,
                categoryName = "Bills",
                customName = "House Rent"
            )
        )

        val result = detector.detectRecurring(records)
        assertEquals(1, result.size)
        assertEquals("House Rent", result[0].name)
        assertEquals(2500000L, result[0].amountCents)
        assertEquals("MONTHLY", result[0].frequency)
    }

    @Test
    fun testYearlySubscriptionDetected() {
        val now = System.currentTimeMillis()
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "prime_annual",
                userId = "user_1",
                amountCents = 149900L, // ₹1,499
                transactionType = "DEBIT",
                transactionSource = "BANK_NOTIFICATION",
                timestamp = now - TimeUnit.DAYS.toMillis(10),
                categoryName = "Entertainment",
                customName = "Amazon Prime Annual Plan"
            )
        )

        val result = detector.detectRecurring(records)
        assertEquals(1, result.size)
        assertEquals("Amazon Prime Annual Plan", result[0].name)
        assertEquals("YEARLY", result[0].frequency)
    }
}
