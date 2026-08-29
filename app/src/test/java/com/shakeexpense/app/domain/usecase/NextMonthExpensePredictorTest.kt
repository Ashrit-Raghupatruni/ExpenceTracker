package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class NextMonthExpensePredictorTest {

    private val predictor = NextMonthExpensePredictor()

    @Test
    fun testInsufficientData_whenUnderFiveRecords() {
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "1",
                userId = "user1",
                userName = null,
                amountCents = 15000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = 1000L,
                customName = null,
                bankRef = null,
                categoryId = 1L,
                categoryName = "Food",
                categoryColor = "#FF0000",
                syncStatus = "SYNCED"
            )
        )

        val result = predictor.predictFromRecords(records, emptyList())
        assertTrue(result is PredictionResult.InsufficientData)
        assertEquals("More spending history is needed to generate a reliable prediction.", (result as PredictionResult.InsufficientData).message)
    }

    @Test
    fun testLevel2StatisticalPrediction_withSufficientRecords() {
        val cal = Calendar.getInstance()
        val records = (1..6).map { i ->
            ExpenseRecordItem(
                expenseId = i.toLong(),
                expenseUuid = "$i",
                userId = "user1",
                userName = null,
                amountCents = 200000L, // ₹2,000 each
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = cal.timeInMillis,
                customName = null,
                bankRef = null,
                categoryId = 1L,
                categoryName = "Groceries",
                categoryColor = "#00FF00",
                syncStatus = "SYNCED"
            )
        }

        val result = predictor.predictFromRecords(records, emptyList(), monthlyIncomeCents = 5000000L)
        assertTrue(result is PredictionResult.Success)
        val success = result as PredictionResult.Success
        assertTrue(success.predictedNextMonthCents > 0L)
        assertEquals(PredictionConfidence.MODERATE, success.confidence)
    }
}
