package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SubscriptionPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AiFinancialAssistantUseCaseTest {

    private val assistant = AiFinancialAssistantUseCase()

    @Test
    fun `test free tier is gated from AI Assistant`() {
        val response = assistant.ask(
            question = "Where did most of my money go?",
            plan = SubscriptionPlan.FREE,
            records = emptyList()
        )
        assertTrue(response.isGated)
        assertTrue(response.answer.contains("PLUS feature"))
    }

    @Test
    fun `test plus tier answers top spending category correctly`() {
        val now = System.currentTimeMillis()
        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "exp_1",
                userId = "user_1",
                amountCents = 150000L, // ₹1500 Food
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now,
                categoryName = "Food",
                categoryColor = "#F59E0B"
            ),
            ExpenseRecordItem(
                expenseId = 2L,
                expenseUuid = "exp_2",
                userId = "user_1",
                amountCents = 50000L, // ₹500 Transport
                transactionType = "DEBIT",
                transactionSource = "MANUAL_SHAKE",
                timestamp = now,
                categoryName = "Transport",
                categoryColor = "#3B82F6"
            )
        )

        val response = assistant.ask(
            question = "Where did most of my money go this month?",
            plan = SubscriptionPlan.PLUS,
            records = records,
            monthlyIncomeCents = 5000000L,
            monthlySafeDailyCents = 100000L
        )

        assertFalse(response.isGated)
        assertTrue(response.answer.contains("Food"))
        assertTrue(response.answer.contains("1500"))
    }

    @Test
    fun `test affordability check returns caution when exceeding buffer`() {
        val response = assistant.ask(
            question = "Can I afford 10000 this week?",
            plan = SubscriptionPlan.PLUS,
            records = emptyList(),
            monthlyIncomeCents = 3000000L, // ₹30,000
            monthlySafeDailyCents = 50000L // ₹500/day = ₹3,500/week
        )

        assertFalse(response.isGated)
        assertTrue(response.answer.contains("Caution") || response.answer.contains("exceeds"))
    }
}
