package com.shakeexpense.app.sync

import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.Expense
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.model.TransactionSource
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.sync.model.SyncExpenseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class AccountDataPersistenceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var familyRepository: FamilyRepository
    private lateinit var syncApiClient: BackendSyncApiClient
    private lateinit var syncEngine: SyncEngine

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        expenseRepository = mock(ExpenseRepository::class.java)
        familyRepository = mock(FamilyRepository::class.java)
        syncApiClient = BackendSyncApiClient()
        syncEngine = SyncEngine(expenseRepository, syncApiClient, null, familyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testExpenseOwnershipByFirebaseUid() = runTest {
        val userAUid = "firebase_uid_user_a"
        val expenseA = Expense(
            id = 1L,
            uuid = "exp_user_a_01",
            userId = userAUid,
            categoryId = 1L,
            categoryName = "Food",
            categoryColorHex = "#F59E0B",
            amountCents = 50000L, // ₹500
            type = TransactionType.DEBIT,
            source = TransactionSource.MANUAL_SHAKE,
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L,
            syncStatus = SyncStatus.PENDING
        )

        // Push expense to cloud
        val pushDto = SyncExpenseDto(
            uuid = expenseA.uuid,
            userId = expenseA.userId,
            categoryId = expenseA.categoryId,
            amountCents = expenseA.amountCents,
            type = expenseA.type.name,
            source = expenseA.source.name,
            timestamp = expenseA.timestamp,
            updatedAt = expenseA.updatedAt
        )
        syncApiClient.pushExpenses("family_a", "device_01", listOf(pushDto))

        // Pull expenses for User A
        val pullUserAResult = syncApiClient.pullUserExpenses(userAUid)
        assertTrue(pullUserAResult.isSuccess)
        val userAExpenses = pullUserAResult.getOrNull()!!
        assertEquals(1, userAExpenses.size)
        assertEquals("exp_user_a_01", userAExpenses[0].uuid)
        assertEquals(userAUid, userAExpenses[0].userId)
        assertEquals(50000L, userAExpenses[0].amountCents)
    }

    @Test
    fun testDifferentAccountIsolation() = runTest {
        val userAUid = "user_account_a"
        val userBUid = "user_account_b"

        val expenseA = SyncExpenseDto(
            uuid = "exp_a_01",
            userId = userAUid,
            categoryId = 1L,
            amountCents = 50000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L
        )
        val expenseB = SyncExpenseDto(
            uuid = "exp_b_01",
            userId = userBUid,
            categoryId = 2L,
            amountCents = 75000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L
        )

        syncApiClient.pushExpenses("fam_a", "dev_a", listOf(expenseA))
        syncApiClient.pushExpenses("fam_b", "dev_b", listOf(expenseB))

        // User B must NOT see User A's expenses
        val userBExpenses = syncApiClient.pullUserExpenses(userBUid).getOrNull()!!
        assertEquals(1, userBExpenses.size)
        assertEquals("exp_b_01", userBExpenses[0].uuid)
        assertEquals(userBUid, userBExpenses[0].userId)
        assertTrue(userBExpenses.none { it.userId == userAUid })
    }

    @Test
    fun testSameAccountLoginRestoresExpensesAndFamily() = runTest {
        val userAUid = "user_account_a"
        val familyA = FamilyGroupDto(
            familyId = "fam_alpha",
            familyName = "Alpha Family",
            creatorUserId = userAUid,
            inviteCode = "SHK-1234",
            createdAt = 1700000000000L
        )
        syncApiClient.createFamilyWithCustomCode(familyA)

        val expense1 = SyncExpenseDto(
            uuid = "exp_01",
            userId = userAUid,
            categoryId = 1L,
            amountCents = 50000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L
        )
        val expense2 = SyncExpenseDto(
            uuid = "exp_02",
            userId = userAUid,
            categoryId = 4L,
            amountCents = 75000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L
        )
        syncApiClient.pushExpenses("fam_alpha", "dev_a", listOf(expense1, expense2))

        `when`(expenseRepository.getPendingSyncExpenses()).thenReturn(emptyList())
        `when`(expenseRepository.getExpenseByUuid("exp_01")).thenReturn(null)
        `when`(expenseRepository.getExpenseByUuid("exp_02")).thenReturn(null)

        // Simulate login & reconcile
        val restoredCount = syncEngine.reconcileAndPullCloudData(userAUid)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, restoredCount)
        // Verify family group was restored
        verify(familyRepository).saveFamilyGroup(familyA)
        // Verify expenses were inserted into Room
        verify(expenseRepository, org.mockito.Mockito.atLeastOnce()).insertExpenses(anyList())
    }

    @Test
    fun testEditAndDeletionPersistence() = runTest {
        val userAUid = "user_account_a"
        val originalExpense = SyncExpenseDto(
            uuid = "exp_edit_01",
            userId = userAUid,
            categoryId = 1L,
            amountCents = 50000L, // ₹500
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = 1700000000000L,
            updatedAt = 1700000000000L
        )
        syncApiClient.pushExpenses("fam_a", "dev_a", listOf(originalExpense))

        // Edit expense to ₹600 (amountCents = 60000)
        val editedExpense = originalExpense.copy(
            amountCents = 60000L,
            updatedAt = 1700000005000L
        )
        syncApiClient.pushExpenses("fam_a", "dev_a", listOf(editedExpense))

        val pullResult = syncApiClient.pullUserExpenses(userAUid).getOrNull()!!
        assertEquals(1, pullResult.size)
        assertEquals(60000L, pullResult[0].amountCents)
        assertEquals(1700000005000L, pullResult[0].updatedAt)
    }
}
