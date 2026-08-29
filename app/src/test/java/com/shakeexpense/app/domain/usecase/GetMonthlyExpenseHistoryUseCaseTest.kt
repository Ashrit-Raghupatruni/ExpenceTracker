package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Calendar

class GetMonthlyExpenseHistoryUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var useCase: GetMonthlyExpenseHistoryUseCase

    @Before
    fun setUp() {
        expenseRepository = mock(ExpenseRepository::class.java)
        useCase = GetMonthlyExpenseHistoryUseCase(expenseRepository)
    }

    @Test
    fun testMonthlyGroupingAndCalculations() = runTest {
        val augCal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 15, 12, 0)
        }
        val augTime = augCal.timeInMillis

        val julCal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 10, 10, 0)
        }
        val julTime = julCal.timeInMillis

        val mockRecords = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "uuid-1",
                userId = "user_1",
                userName = "Rohan",
                amountCents = 15000L, // ₹150 DEBIT
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = augTime,
                customName = null,
                bankRef = null,
                categoryId = 1L,
                categoryName = "Food",
                categoryColor = "#EF4444",
                syncStatus = "SYNCED"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "uuid-2",
                userId = "user_1",
                userName = "Rohan",
                amountCents = 6100L, // ₹61 CREDIT
                transactionType = "CREDIT",
                transactionSource = "BANK_NOTIF",
                timestamp = augTime + 1000L,
                customName = "Yashwanth M",
                bankRef = "UPI123",
                categoryId = 7L,
                categoryName = "Others",
                categoryColor = "#6B7280",
                syncStatus = "SYNCED"
            ),
            ExpenseRecordItem(
                expenseId = 3L,
                expenseUuid = "uuid-3",
                userId = "user_1",
                userName = "Rohan",
                amountCents = 20000L, // ₹200 DEBIT in July
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = julTime,
                customName = null,
                bankRef = null,
                categoryId = 2L,
                categoryName = "Transport",
                categoryColor = "#3B82F6",
                syncStatus = "SYNCED"
            )
        )

        `when`(expenseRepository.getSpreadsheetStream()).thenReturn(flowOf(mockRecords))

        val months = useCase.groupExpensesByMonth(mockRecords)

        assertEquals(2, months.size)

        // August 2026
        val aug = months[0]
        assertEquals("2026-08", aug.yearMonth)
        assertEquals("August 2026", aug.displayMonth)
        assertEquals(15000L, aug.totalDebitCents)
        assertEquals(6100L, aug.totalCreditCents)
        assertEquals(-8900L, aug.netCents)
        assertEquals(2, aug.transactionCount)
        assertEquals("₹150", aug.totalDebitFormatted)
        assertEquals("₹61", aug.totalCreditFormatted)
        assertEquals("-₹89", aug.netFormatted)
        assertEquals(1, aug.categoryBreakdown.size)
        assertEquals("Food", aug.categoryBreakdown[0].categoryName)
        assertEquals("₹150", aug.categoryBreakdown[0].totalFormatted)

        // July 2026
        val jul = months[1]
        assertEquals("2026-07", jul.yearMonth)
        assertEquals("July 2026", jul.displayMonth)
        assertEquals(20000L, jul.totalDebitCents)
        assertEquals(0L, jul.totalCreditCents)
        assertEquals(-20000L, jul.netCents)
        assertEquals(1, jul.transactionCount)
    }

    @Test
    fun testEmptyExpenseRecordsReturnsEmptyList() = runTest {
        val months = useCase.groupExpensesByMonth(emptyList())
        assertTrue(months.isEmpty())
    }

    @Test
    fun testDailyExpenseGroupingWithinMonth() = runTest {
        val aug27Cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 27, 14, 30)
        }
        val aug26Cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 26, 9, 15)
        }

        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "uuid-1",
                userId = "user_1",
                userName = "Ashrith",
                amountCents = 25000L, // ₹250 Food on Aug 27
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = aug27Cal.timeInMillis,
                customName = null,
                bankRef = null,
                categoryId = 1L,
                categoryName = "Food",
                categoryColor = "#EF4444",
                syncStatus = "SYNCED"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "uuid-2",
                userId = "user_1",
                userName = "Ashrith",
                amountCents = 75000L, // ₹750 Bills on Aug 27
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = aug27Cal.timeInMillis + 10000L,
                customName = null,
                bankRef = null,
                categoryId = 4L,
                categoryName = "Bills",
                categoryColor = "#10B981",
                syncStatus = "SYNCED"
            ),
            ExpenseRecordItem(
                expenseId = 3L,
                expenseUuid = "uuid-3",
                userId = "user_1",
                userName = "Ashrith",
                amountCents = 12000L, // ₹120 Transport on Aug 26
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = aug26Cal.timeInMillis,
                customName = null,
                bankRef = null,
                categoryId = 2L,
                categoryName = "Transport",
                categoryColor = "#3B82F6",
                syncStatus = "SYNCED"
            )
        )

        val months = useCase.groupExpensesByMonth(records)
        assertEquals(1, months.size)

        val aug = months[0]
        assertEquals("August 2026", aug.displayMonth)
        assertEquals(112000L, aug.totalDebitCents) // ₹1120 total
        assertEquals(3, aug.transactionCount)

        // Verify Daily Groups
        val daily = aug.dailyGroups
        assertEquals(2, daily.size)

        // Day 1: Aug 27
        assertEquals("Aug 27", daily[0].displayDate)
        assertEquals(100000L, daily[0].totalDebitCents) // ₹1000
        assertEquals("₹1000", daily[0].totalDebitFormatted)
        assertEquals(2, daily[0].transactions.size)
        assertEquals("Food", daily[0].transactions[0].categoryName)
        assertEquals("Bills", daily[0].transactions[1].categoryName)

        // Day 2: Aug 26
        assertEquals("Aug 26", daily[1].displayDate)
        assertEquals(12000L, daily[1].totalDebitCents) // ₹120
        assertEquals("₹120", daily[1].totalDebitFormatted)
        assertEquals(1, daily[1].transactions.size)
        assertEquals("Transport", daily[1].transactions[0].categoryName)
    }
}
