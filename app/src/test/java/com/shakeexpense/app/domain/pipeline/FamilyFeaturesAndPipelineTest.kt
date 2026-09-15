package com.shakeexpense.app.domain.pipeline

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FamilyBudget
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyPrivacySettings
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.MemberSpendingSummary
import com.shakeexpense.app.domain.model.PrivacyMode
import com.shakeexpense.app.domain.usecase.FamilyAiReportGeneratorUseCase
import com.shakeexpense.app.domain.usecase.GetMemberCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetMemberExpensesUseCase
import com.shakeexpense.app.domain.usecase.GetMemberSpendingSummaryUseCase
import com.shakeexpense.app.notification.SpendingAlertNotificationManager
import com.shakeexpense.app.sync.model.FamilyGroupDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class FamilyFeaturesAndPipelineTest {

    private class TestFamilyRepo(
        val members: MutableList<FamilyMember> = mutableListOf(),
        var group: FamilyGroupDto? = null,
        val budgets: MutableList<FamilyBudget> = mutableListOf()
    ) : FamilyRepository {
        override fun getFamilyMembers(): Flow<List<FamilyMember>> = flowOf(members)
        override fun getFamilyMemberById(id: String): Flow<FamilyMember?> = flowOf(members.find { it.id == id })
        override fun getActiveFamilyGroup(): Flow<FamilyGroupDto?> = flowOf(group)
        override suspend fun saveFamilyGroup(family: FamilyGroupDto) { group = family }
        override suspend fun addFamilyMember(member: FamilyMember) { members.add(member) }
        override suspend fun setFamilyMembers(newMembers: List<FamilyMember>) {
            members.clear()
            members.addAll(newMembers)
        }
        override suspend fun deleteFamilyMember(id: String) { members.removeAll { it.id == id } }
        override suspend fun clearFamilyData() { members.clear(); group = null }
        override suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long) {
            group = group?.copy(monthlySpendingLimitCents = limitCents)
        }
        override suspend fun updateMemberPrivacyMode(memberId: String, mode: PrivacyMode) {}
        override suspend fun updateMemberPrivacy(memberId: String, settings: FamilyPrivacySettings) {
            val idx = members.indexOfFirst { it.id == memberId }
            if (idx >= 0) members[idx] = members[idx].copy(privacySettings = settings)
        }
        override fun getFamilyBudgets(familyId: String): Flow<List<FamilyBudget>> = flowOf(budgets)
        override suspend fun saveFamilyBudget(budget: FamilyBudget) {
            budgets.removeAll { it.id == budget.id }
            budgets.add(budget)
        }
        override suspend fun deleteFamilyBudget(budgetId: String) {
            budgets.removeAll { it.id == budgetId }
        }
        override suspend fun setMemberExitRequested(memberId: String, requested: Boolean) {
            val idx = members.indexOfFirst { it.id == memberId }
            if (idx >= 0) members[idx] = members[idx].copy(isExitRequested = requested)
        }
    }

    @Test
    fun `test granular privacy hides transactions when shareTransactions is false`() = runBlocking {
        val memberA = FamilyMember(
            id = "user_a",
            familyId = "fam_1",
            name = "Alice",
            role = FamilyRole.CHILD,
            privacySettings = FamilyPrivacySettings(shareTransactions = false)
        )
        val familyRepo = TestFamilyRepo(members = mutableListOf(memberA))
        val expenseRepo = mock(ExpenseRepository::class.java)

        val txList = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "uuid_1",
                userId = "user_a",
                amountCents = 150000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL",
                timestamp = System.currentTimeMillis(),
                categoryName = "Food"
            )
        )
        `when`(expenseRepo.getExpensesByUserId("user_a")).thenReturn(flowOf(txList))

        val getMemberExpensesUseCase = GetMemberExpensesUseCase(expenseRepo, familyRepo)

        // Viewed by Parent "user_parent" -> should be empty list because shareTransactions is false
        val hiddenResult = getMemberExpensesUseCase(userId = "user_a", viewerUserId = "user_parent").first()
        assertTrue(hiddenResult.isEmpty())

        // Viewed by owner "user_a" -> should return their own transactions
        val ownerResult = getMemberExpensesUseCase(userId = "user_a", viewerUserId = "user_a").first()
        assertEquals(1, ownerResult.size)
        assertEquals(150000L, ownerResult.first().amountCents)
    }

    @Test
    fun `test granular privacy hides monthly total when shareMonthlyTotal is false`() = runBlocking {
        val memberA = FamilyMember(
            id = "user_a",
            familyId = "fam_1",
            name = "Alice",
            role = FamilyRole.CHILD,
            privacySettings = FamilyPrivacySettings(shareMonthlyTotal = false)
        )
        val familyRepo = TestFamilyRepo(members = mutableListOf(memberA))
        val expenseRepo = mock(ExpenseRepository::class.java)

        val summary = MemberSpendingSummary(
            userId = "user_a",
            userName = "Alice",
            role = FamilyRole.CHILD,
            totalDebitCents = 250000L,
            totalCreditCents = 0L,
            transactionCount = 5
        )
        `when`(expenseRepo.getMemberSpendingSummary("user_a", FamilyRole.CHILD)).thenReturn(flowOf(summary))

        val getSummaryUseCase = GetMemberSpendingSummaryUseCase(expenseRepo, familyRepo)

        // Viewed by other user -> returns null
        val hiddenResult = getSummaryUseCase(userId = "user_a", role = FamilyRole.CHILD, viewerUserId = "user_parent").first()
        assertNull(hiddenResult)

        // Viewed by self -> returns summary
        val selfResult = getSummaryUseCase(userId = "user_a", role = FamilyRole.CHILD, viewerUserId = "user_a").first()
        assertNotNull(selfResult)
        assertEquals(250000L, selfResult?.totalDebitCents)
    }

    @Test
    fun `test granular privacy hides category totals when shareCategoryTotals is false`() = runBlocking {
        val memberA = FamilyMember(
            id = "user_a",
            familyId = "fam_1",
            name = "Alice",
            role = FamilyRole.CHILD,
            privacySettings = FamilyPrivacySettings(shareCategoryTotals = false)
        )
        val familyRepo = TestFamilyRepo(members = mutableListOf(memberA))
        val expenseRepo = mock(ExpenseRepository::class.java)

        val breakdown = listOf(
            CategorySubtotal(categoryId = 1L, categoryName = "Shopping", colorHex = "#FF0000", totalCents = 50000L, count = 2)
        )
        `when`(expenseRepo.getMemberCategoryBreakdown("user_a")).thenReturn(flowOf(breakdown))

        val getBreakdownUseCase = GetMemberCategoryBreakdownUseCase(expenseRepo, familyRepo)

        // Other viewer -> empty list
        val hiddenResult = getBreakdownUseCase(userId = "user_a", viewerUserId = "user_parent").first()
        assertTrue(hiddenResult.isEmpty())

        // Owner -> full breakdown
        val selfResult = getBreakdownUseCase(userId = "user_a", viewerUserId = "user_a").first()
        assertEquals(1, selfResult.size)
    }

    @Test
    fun `test family AI report generator empty state when no transactions`() {
        val generator = FamilyAiReportGeneratorUseCase()
        val report = generator(
            currentMonthExpenses = emptyList(),
            previousMonthExpenses = emptyList(),
            members = emptyList(),
            familyLimitCents = 5000000L
        )

        assertFalse(report.hasEnoughData)
        assertEquals(0L, report.totalSpendingCents)
        assertEquals(5000000L, report.remainingLimitCents)
        assertEquals(1, report.aiInsights.size)
        assertEquals("Not enough family financial data to generate a report.", report.aiInsights.first())
    }

    @Test
    fun `test family AI report generator produces real data insights`() {
        val generator = FamilyAiReportGeneratorUseCase()

        val memberA = FamilyMember(id = "u1", familyId = "f1", name = "Priya", role = FamilyRole.PARENT)
        val memberB = FamilyMember(id = "u2", familyId = "f1", name = "Aarav", role = FamilyRole.CHILD)

        val currentMonthExpenses = listOf(
            ExpenseRecordItem(expenseId = 1, expenseUuid = "e1", userId = "u1", amountCents = 2000000L, transactionType = "DEBIT", transactionSource = "M", timestamp = 0, categoryName = "Food"),
            ExpenseRecordItem(expenseId = 2, expenseUuid = "e2", userId = "u2", amountCents = 1000000L, transactionType = "DEBIT", transactionSource = "M", timestamp = 0, categoryName = "Transport")
        )

        val budgets = listOf(
            FamilyBudget(id = "b1", familyId = "f1", categoryName = "Food", limitCents = 2500000L)
        )

        val report = generator(
            currentMonthExpenses = currentMonthExpenses,
            previousMonthExpenses = emptyList(),
            members = listOf(memberA, memberB),
            familyLimitCents = 5000000L,
            sharedBudgets = budgets,
            familyMonthlyIncomeCents = 8000000L
        )

        assertTrue(report.hasEnoughData)
        assertEquals(3000000L, report.totalSpendingCents)
        assertEquals(2000000L, report.remainingLimitCents)
        assertEquals(60.0, report.limitUsagePercentage, 0.1)
        assertEquals(2, report.memberContributions.size)
        assertEquals(1, report.budgetHealth.size)
        assertEquals("NEARING_LIMIT", report.budgetHealth.first().status)
        assertTrue(report.aiInsights.any { it.contains("Food is the largest category") })
    }

    @Test
    fun `test spending alert notification manager anti-spam suppresses duplicate triggers`() {
        SpendingAlertNotificationManager.resetThresholds("test_user", "PERSONAL")

        // First time passing 80% should return true (not notified yet)
        val shouldNotifyFirst = SpendingAlertNotificationManager.shouldTriggerAlert("test_user", 80, "PERSONAL")
        assertTrue("First trigger at 80% must be allowed", shouldNotifyFirst)

        // Second time at 80% or 85% must return false to prevent spam
        val shouldNotifySecond = SpendingAlertNotificationManager.shouldTriggerAlert("test_user", 80, "PERSONAL")
        assertFalse("Duplicate trigger at 80% must be suppressed", shouldNotifySecond)

        // But crossing into 90% must notify
        val shouldNotifyNinety = SpendingAlertNotificationManager.shouldTriggerAlert("test_user", 90, "PERSONAL")
        assertTrue("Progression to 90% must notify", shouldNotifyNinety)

        // Resetting thresholds clears state
        SpendingAlertNotificationManager.resetThresholds("test_user", "PERSONAL")
        val afterReset = SpendingAlertNotificationManager.shouldTriggerAlert("test_user", 80, "PERSONAL")
        assertTrue("After reset, 80% can trigger again", afterReset)
    }

    @Test
    fun `test child exit request persists isExitRequested on repository member`() = runBlocking {
        val child = FamilyMember(
            id = "child_1",
            familyId = "fam_1",
            name = "Rohan",
            role = FamilyRole.CHILD,
            isExitRequested = false
        )
        val repo = TestFamilyRepo(members = mutableListOf(child))
        val useCase = com.shakeexpense.app.domain.usecase.RequestChildExitUseCase(repo)

        val result = useCase("child_1")
        assertTrue(result.isSuccess)
        val updatedChild = repo.members.first { it.id == "child_1" }
        assertTrue("Member must have isExitRequested set to true", updatedChild.isExitRequested)
    }

    @Test
    fun `test entitlement manager locks down all premium capabilities on Free tier`() {
        val entitlementManager = com.shakeexpense.app.domain.usecase.EntitlementManager()

        // Free tier must have NO access to any of the 15 premium capabilities
        com.shakeexpense.app.domain.model.FeatureCapability.values().forEach { capability ->
            assertFalse(
                "Free user must not access $capability",
                entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.FREE, capability)
            )
        }

        // Plus tier must access individual features but NOT family-exclusive features
        assertTrue(entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS, com.shakeexpense.app.domain.model.FeatureCapability.SAFE_TO_SPEND))
        assertTrue(entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS, com.shakeexpense.app.domain.model.FeatureCapability.FINANCIAL_SAFETY_SCORE))
        assertFalse(entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS, com.shakeexpense.app.domain.model.FeatureCapability.SHARED_FAMILY_BUDGETS))
        assertFalse(entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS, com.shakeexpense.app.domain.model.FeatureCapability.FAMILY_REPORTS))

        // Family Pro tier must access all capabilities
        com.shakeexpense.app.domain.model.FeatureCapability.values().forEach { capability ->
            assertTrue(
                "Family Pro user must access $capability",
                entitlementManager.canAccess(com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO, capability)
            )
        }
    }

    @Test
    fun `test AI assistant afford query prompts setup when no income recorded`() = runBlocking {
        val assistant = com.shakeexpense.app.domain.usecase.AiFinancialAssistantUseCase()
        val response = assistant.ask(
            question = "Can I afford 5000 this week?",
            plan = com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS,
            records = emptyList<ExpenseRecordItem>(),
            monthlyIncomeCents = 0L,
            monthlySafeDailyCents = 0L
        )

        assertTrue(response.answer.contains("configure your monthly income in the Safe-to-Spend setup"))
        assertTrue(response.highlights.any { it.contains("Needs Income Data") })
    }

    @Test
    fun `test financial safety score calculates dynamic sub-scores`() {
        val engine = com.shakeexpense.app.domain.usecase.FinancialSafetyScoreEngine()

        val records = listOf(
            ExpenseRecordItem(
                expenseId = 1L,
                expenseUuid = "e1",
                userId = "u1",
                amountCents = 500000L,
                transactionType = "DEBIT",
                transactionSource = "MANUAL",
                timestamp = 0,
                categoryName = "Food"
            )
        )

        val result = engine.calculateScore(
            monthlyIncomeCents = 10000000L,
            savingsTargetCents = 3000000L,
            currentMonthExpensesCents = 500000L,
            records = records
        )

        assertNotNull(result)
        assertEquals("Healthy", result?.spendingSubScore)
        assertEquals("Healthy", result?.budgetSubScore)
        assertEquals("None", result?.riskSignalsSubScore)
    }
}
