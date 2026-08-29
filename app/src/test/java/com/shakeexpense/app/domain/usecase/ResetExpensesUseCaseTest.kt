package com.shakeexpense.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.populateInitialData
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.sync.BackendSyncApiClient
import com.shakeexpense.app.sync.SyncEngine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class ResetExpensesUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseRepo: ExpenseRepositoryImpl
    private lateinit var syncEngine: SyncEngine
    private lateinit var resetExpensesUseCase: ResetExpensesUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        val syncApiClient = BackendSyncApiClient()
        syncEngine = SyncEngine(expenseRepo, syncApiClient, null)
        resetExpensesUseCase = ResetExpensesUseCase(expenseRepo, syncEngine)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testResetTodayDeletesOnlyTodayExpensesForUser() = runBlocking {
        populateInitialData(db)
        val now = System.currentTimeMillis()
        val yesterday = now - (24 * 60 * 60 * 1000L)

        // User A expenses: 2 today (₹250, ₹750), 1 yesterday (₹100)
        val userAExp1 = ExpenseEntity(
            uuid = "user_a_today_1",
            userId = "user_A",
            categoryId = 1,
            amountCents = 25000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now
        )
        val userAExp2 = ExpenseEntity(
            uuid = "user_a_today_2",
            userId = "user_A",
            categoryId = 4,
            amountCents = 75000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now - 1000L
        )
        val userAExpYesterday = ExpenseEntity(
            uuid = "user_a_yesterday",
            userId = "user_A",
            categoryId = 2,
            amountCents = 10000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = yesterday
        )

        // User B expense: 1 today (₹500)
        val userBExpToday = ExpenseEntity(
            uuid = "user_b_today",
            userId = "user_B",
            categoryId = 3,
            amountCents = 50000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now
        )

        db.expenseDao().insertExpenses(listOf(userAExp1, userAExp2, userAExpYesterday, userBExpToday))

        // 1. Get Candidate Info for User A - TODAY
        val candidate = resetExpensesUseCase.getResetCandidateInfo("user_A", ResetPeriod.TODAY)
        assertEquals(2, candidate.expenseCount)
        assertEquals(100000L, candidate.totalAmountCents) // ₹1000
        assertTrue(candidate.candidateUuids.contains("user_a_today_1"))
        assertTrue(candidate.candidateUuids.contains("user_a_today_2"))

        // 2. Execute Reset
        val result = resetExpensesUseCase.executeReset("user_A", candidate)
        assertTrue(result.isSuccess)
        assertEquals(2, result.deletedCount)
        assertEquals(100000L, result.deletedAmountCents)

        // 3. Verify User A today's expenses are deleted
        assertNull(db.expenseDao().getExpenseByUuid("user_a_today_1"))
        assertNull(db.expenseDao().getExpenseByUuid("user_a_today_2"))

        // 4. Verify User A yesterday's expense is PRESERVED
        assertNotNull(db.expenseDao().getExpenseByUuid("user_a_yesterday"))

        // 5. Verify User B's today expense is UNTOUCHED
        assertNotNull(db.expenseDao().getExpenseByUuid("user_b_today"))
        val userBExpenses = db.expenseDao().getExpensesInTimeRange("user_B", 0, Long.MAX_VALUE)
        assertEquals(1, userBExpenses.size)
        assertEquals("user_b_today", userBExpenses[0].uuid)
    }

    @Test
    fun testResetSpecificDateDeletesTargetDateOnly() = runBlocking {
        populateInitialData(db)

        val targetCal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 15, 14, 0)
        }
        val targetDateMillis = targetCal.timeInMillis

        val otherCal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 20, 10, 0)
        }
        val otherDateMillis = otherCal.timeInMillis

        val expOn15th = ExpenseEntity(
            uuid = "exp_aug_15",
            userId = "user_A",
            categoryId = 1,
            amountCents = 35000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = targetDateMillis
        )
        val expOn20th = ExpenseEntity(
            uuid = "exp_aug_20",
            userId = "user_A",
            categoryId = 2,
            amountCents = 45000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = otherDateMillis
        )
        db.expenseDao().insertExpenses(listOf(expOn15th, expOn20th))

        // Get candidate for 15th August
        val candidate = resetExpensesUseCase.getResetCandidateInfo(
            userId = "user_A",
            period = ResetPeriod.SPECIFIC_DATE,
            specificDateMillis = targetDateMillis
        )
        assertEquals(1, candidate.expenseCount)
        assertEquals(35000L, candidate.totalAmountCents)
        assertEquals("exp_aug_15", candidate.candidateUuids[0])

        // Reset
        val result = resetExpensesUseCase.executeReset("user_A", candidate)
        assertTrue(result.isSuccess)
        assertEquals(1, result.deletedCount)

        // 15th deleted, 20th preserved
        assertNull(db.expenseDao().getExpenseByUuid("exp_aug_15"))
        assertNotNull(db.expenseDao().getExpenseByUuid("exp_aug_20"))
    }
}
