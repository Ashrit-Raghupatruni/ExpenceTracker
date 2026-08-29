package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.model.SubscriptionStatus
import com.shakeexpense.app.domain.model.UserEntitlement
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportTransactionsCsvUseCaseTest {

    @Test
    fun testFreeTier_returnsNullCsv() {
        val entitlementManager = EntitlementManager(UserEntitlement(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE))
        val exportUseCase = ExportTransactionsCsvUseCase(entitlementManager)

        val expenses = listOf(
            ExpenseEntity(id = 1L, uuid = "u1", userId = "user1", customName = "Coffee", amountCents = 15000L, timestamp = 1000L, categoryId = 1L)
        )

        assertNull(exportUseCase.generateCsv(expenses))
    }

    @Test
    fun testPlusTier_returnsValidCsv() {
        val entitlementManager = EntitlementManager(UserEntitlement(SubscriptionPlan.PLUS, SubscriptionStatus.ACTIVE))
        val exportUseCase = ExportTransactionsCsvUseCase(entitlementManager)

        val expenses = listOf(
            ExpenseEntity(id = 1L, uuid = "u1", userId = "user1", customName = "Coffee, Latte", amountCents = 25000L, timestamp = 1000L, categoryId = 1L, bankRef = "Starbucks")
        )

        val csv = exportUseCase.generateCsv(expenses, mapOf(1L to "Food"))
        assertNotNull(csv)
        assertTrue(csv!!.contains("ID,UUID,Date,Type,Source,Category,Amount (INR),Custom Name,Bank Ref"))
        assertTrue(csv.contains("\"Coffee, Latte\""))
        assertTrue(csv.contains("\"Starbucks\""))
    }
}
