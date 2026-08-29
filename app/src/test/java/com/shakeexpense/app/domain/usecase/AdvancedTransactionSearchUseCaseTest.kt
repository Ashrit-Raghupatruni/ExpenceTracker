package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.model.SubscriptionStatus
import com.shakeexpense.app.domain.model.UserEntitlement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedTransactionSearchUseCaseTest {

    @Test
    fun testFreeTier_returnsEmptyResults() {
        val entitlementManager = EntitlementManager(UserEntitlement(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE))
        val searchUseCase = AdvancedTransactionSearchUseCase(entitlementManager)

        val expenses = listOf(
            ExpenseEntity(id = 1L, uuid = "u1", userId = "user1", customName = "Pizza", amountCents = 45000L, timestamp = 1000L, categoryId = 1L)
        )

        val results = searchUseCase(expenses, AdvancedSearchQuery(keyword = "Pizza"))
        assertTrue(results.isEmpty())
    }

    @Test
    fun testPlusTier_filtersCorrectly() {
        val entitlementManager = EntitlementManager(UserEntitlement(SubscriptionPlan.PLUS, SubscriptionStatus.ACTIVE))
        val searchUseCase = AdvancedTransactionSearchUseCase(entitlementManager)

        val expenses = listOf(
            ExpenseEntity(id = 1L, uuid = "u1", userId = "user1", customName = "Dinner at Domino's", amountCents = 85000L, timestamp = 1000L, categoryId = 1L),
            ExpenseEntity(id = 2L, uuid = "u2", userId = "user1", customName = "Bus Ticket", amountCents = 5000L, timestamp = 2000L, categoryId = 2L)
        )

        val results = searchUseCase(
            expenses,
            AdvancedSearchQuery(merchantQuery = "Domino", minAmountCents = 50000L)
        )

        assertEquals(1, results.size)
        assertEquals("Dinner at Domino's", results.first().customName)
    }
}
