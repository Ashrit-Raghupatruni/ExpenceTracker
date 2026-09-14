package com.shakeexpense.app.ui.tracker

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.populateInitialData
import com.shakeexpense.app.data.repository.CategoryRepositoryImpl
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.DeleteExpenseUseCase
import com.shakeexpense.app.domain.usecase.EditExpenseUseCase
import com.shakeexpense.app.domain.usecase.GetCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetSpendingTotalsUseCase
import com.shakeexpense.app.domain.usecase.GetSpreadsheetStreamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TrackerViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var expenseRepo: ExpenseRepositoryImpl
    private lateinit var categoryRepo: CategoryRepositoryImpl
    private lateinit var getSpreadsheetStreamUseCase: GetSpreadsheetStreamUseCase
    private lateinit var getSpendingTotalsUseCase: GetSpendingTotalsUseCase
    private lateinit var getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase
    private lateinit var editExpenseUseCase: EditExpenseUseCase
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        categoryRepo = CategoryRepositoryImpl(db.categoryDao())
        getSpreadsheetStreamUseCase = GetSpreadsheetStreamUseCase(expenseRepo)
        getSpendingTotalsUseCase = GetSpendingTotalsUseCase(expenseRepo)
        getCategoryBreakdownUseCase = GetCategoryBreakdownUseCase(expenseRepo)
        editExpenseUseCase = EditExpenseUseCase(expenseRepo)
        deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun testSpendingTotalsCalculation() = runBlocking {
        populateInitialData(db)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val yesterday = todayStart - (24 * 60 * 60 * 1000L)

        // Expense 1: Today Bills ₹750 (75000 cents)
        val exp1 = ExpenseEntity(
            uuid = "exp_today_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 4, // Bills
            amountCents = 75000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now
        )
        // Expense 2: Today Food ₹250 (25000 cents)
        val exp2 = ExpenseEntity(
            uuid = "exp_today_2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 25000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now - 10000L
        )
        // Expense 3: Yesterday Transport ₹120 (12000 cents)
        val exp3 = ExpenseEntity(
            uuid = "exp_yesterday",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 2, // Transport
            amountCents = 12000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = yesterday
        )
        db.expenseDao().insertExpenses(listOf(exp1, exp2, exp3))

        val totals = getSpendingTotalsUseCase().first()

        // Today total: 75000 + 25000 = 100000 cents (₹1000)
        assertEquals(100000L, totals.todayDebitCents)
        // All time total: 100000 + 12000 = 112000 cents (₹1120)
        assertEquals(112000L, totals.allTimeDebitCents)
    }

    @Test
    fun testSpreadsheetStreamOrdering() = runBlocking {
        populateInitialData(db)

        val expOld = ExpenseEntity(
            uuid = "old",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 10000L,
            timestamp = 1000L
        )
        val expNew = ExpenseEntity(
            uuid = "new",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 2,
            amountCents = 20000L,
            timestamp = 5000L
        )
        db.expenseDao().insertExpenses(listOf(expOld, expNew))

        val stream = getSpreadsheetStreamUseCase().first()
        assertEquals(2, stream.size)
        // Newest first (5000L before 1000L)
        assertEquals("new", stream[0].expenseUuid)
        assertEquals("old", stream[1].expenseUuid)
    }

    @Test
    fun testCategoryBreakdownAggregation() = runBlocking {
        populateInitialData(db)

        val expFood1 = ExpenseEntity(
            uuid = "f1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 25000L,
            type = "DEBIT"
        )
        val expFood2 = ExpenseEntity(
            uuid = "f2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 15000L,
            type = "DEBIT"
        )
        val expBills = ExpenseEntity(
            uuid = "b1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 4, // Bills
            amountCents = 75000L,
            type = "DEBIT"
        )
        db.expenseDao().insertExpenses(listOf(expFood1, expFood2, expBills))

        val breakdown = getCategoryBreakdownUseCase().first()
        assertEquals(2, breakdown.size)

        // Top category: Bills (75000 cents)
        assertEquals("Bills", breakdown[0].categoryName)
        assertEquals(75000L, breakdown[0].totalCents)
        assertEquals(1, breakdown[0].count)

        // Second category: Food (40000 cents, 2 transactions)
        assertEquals("Food", breakdown[1].categoryName)
        assertEquals(40000L, breakdown[1].totalCents)
        assertEquals(2, breakdown[1].count)
    }

    @Test
    fun testEditExpenseFlow() = runBlocking {
        populateInitialData(db)

        val exp = ExpenseEntity(
            uuid = "exp_edit_test",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 20000L, // ₹200
            type = "DEBIT"
        )
        db.expenseDao().insertExpense(exp)

        // Edit amount to ₹350 (35000 cents) and category to Transport (id = 2)
        val editResult = editExpenseUseCase(
            uuid = "exp_edit_test",
            amountCents = 35000L,
            categoryId = 2,
            type = TransactionType.DEBIT,
            customName = "Ola Ride"
        )
        assertTrue(editResult.isSuccess)

        val updated = db.expenseDao().getExpenseByUuid("exp_edit_test")
        assertEquals(35000L, updated?.amountCents)
        assertEquals(2L, updated?.categoryId)
        assertEquals("Ola Ride", updated?.customName)
    }

    @Test
    fun testDeleteExpenseFlow() = runBlocking {
        populateInitialData(db)

        val exp = ExpenseEntity(
            uuid = "exp_delete_test",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 15000L,
            type = "DEBIT"
        )
        db.expenseDao().insertExpense(exp)

        val deleteResult = deleteExpenseUseCase("exp_delete_test")
        assertTrue(deleteResult.isSuccess)

        val retrieved = db.expenseDao().getExpenseByUuid("exp_delete_test")
        assertNull(retrieved)
    }

    @Test
    fun testSyncDisplayStatusOfflineAndPending() = runBlocking {
        populateInitialData(db)

        // 1. Offline scenario
        val viewModelOffline = TrackerViewModel(
            getSpreadsheetStreamUseCase,
            getSpendingTotalsUseCase,
            getCategoryBreakdownUseCase,
            categoryRepo,
            expenseRepo,
            editExpenseUseCase,
            deleteExpenseUseCase,
            isOnlineFlow = flowOf(false)
        )
        var count1 = 0
        while (viewModelOffline.state.value.syncDisplayStatus != SyncDisplayStatus.NOT_SYNCED_OFFLINE && count1++ < 30) {
            kotlinx.coroutines.delay(20)
        }
        assertEquals(SyncDisplayStatus.NOT_SYNCED_OFFLINE, viewModelOffline.state.value.syncDisplayStatus)

        // 2. Online with pending expenses
        val pendingExp = ExpenseEntity(
            uuid = "pending_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 10000L,
            syncStatus = "PENDING"
        )
        db.expenseDao().insertExpense(pendingExp)

        val viewModelPending = TrackerViewModel(
            getSpreadsheetStreamUseCase,
            getSpendingTotalsUseCase,
            getCategoryBreakdownUseCase,
            categoryRepo,
            expenseRepo,
            editExpenseUseCase,
            deleteExpenseUseCase,
            isOnlineFlow = flowOf(true)
        )
        var count2 = 0
        while (viewModelPending.state.value.syncDisplayStatus != SyncDisplayStatus.NOT_SYNCED_PENDING && count2++ < 30) {
            kotlinx.coroutines.delay(20)
        }
        assertEquals(SyncDisplayStatus.NOT_SYNCED_PENDING, viewModelPending.state.value.syncDisplayStatus)
        assertEquals(1, viewModelPending.state.value.pendingSyncCount)
    }

    @Test
    fun testTrackerViewModelTabSelection() = runBlocking {
        populateInitialData(db)

        val viewModel = TrackerViewModel(
            getSpreadsheetStreamUseCase,
            getSpendingTotalsUseCase,
            getCategoryBreakdownUseCase,
            categoryRepo,
            expenseRepo,
            editExpenseUseCase,
            deleteExpenseUseCase
        )

        assertEquals(TrackerTab.DASHBOARD, viewModel.state.value.selectedTab)

        viewModel.onTabSelected(TrackerTab.CATEGORY_BREAKDOWN)
        assertEquals(TrackerTab.CATEGORY_BREAKDOWN, viewModel.state.value.selectedTab)

        viewModel.onTabSelected(TrackerTab.SPREADSHEET)
        assertEquals(TrackerTab.SPREADSHEET, viewModel.state.value.selectedTab)

        viewModel.onTabSelected(TrackerTab.DASHBOARD)
        assertEquals(TrackerTab.DASHBOARD, viewModel.state.value.selectedTab)
    }

    @Test
    fun testSwipeToDeleteReactiveStateUpdates() = runBlocking {
        populateInitialData(db)

        val exp1 = ExpenseEntity(
            uuid = "exp_swipe_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 25000L, // ₹250
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = System.currentTimeMillis()
        )
        val exp2 = ExpenseEntity(
            uuid = "exp_swipe_2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 4, // Bills
            amountCents = 75000L, // ₹750
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = System.currentTimeMillis()
        )
        db.expenseDao().insertExpenses(listOf(exp1, exp2))

        val viewModel = TrackerViewModel(
            getSpreadsheetStreamUseCase,
            getSpendingTotalsUseCase,
            getCategoryBreakdownUseCase,
            categoryRepo,
            expenseRepo,
            editExpenseUseCase,
            deleteExpenseUseCase
        )

        var count = 0
        while (viewModel.state.value.records.size < 2 && count++ < 100) {
            kotlinx.coroutines.delay(30)
        }
        assertEquals(2, viewModel.state.value.records.size)
        assertEquals(100000L, viewModel.state.value.totals.allTimeDebitCents) // ₹1000

        // Perform Swipe to Delete on exp1 (Food ₹250)
        viewModel.onDeleteExpense("exp_swipe_1")

        count = 0
        while (viewModel.state.value.records.size != 1 && count++ < 100) {
            kotlinx.coroutines.delay(30)
        }

        // Verify expense list immediately updated
        assertEquals(1, viewModel.state.value.records.size)
        assertEquals("exp_swipe_2", viewModel.state.value.records[0].expenseUuid)

        // Verify totals immediately updated
        assertEquals(75000L, viewModel.state.value.totals.allTimeDebitCents) // ₹750

        // Verify category breakdown updated
        val breakdowns = viewModel.state.value.categoryBreakdowns
        assertEquals(1, breakdowns.size)
        assertEquals("Bills", breakdowns[0].categoryName)
        assertEquals(75000L, breakdowns[0].totalCents)
    }
}
