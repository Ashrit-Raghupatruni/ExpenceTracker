package com.shakeexpense.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.populateInitialData
import com.shakeexpense.app.data.repository.CategoryRepositoryImpl
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.ui.entry.ExpenseEntryState
import com.shakeexpense.app.ui.entry.ExpenseEntryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ExpenseUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryRepo: CategoryRepositoryImpl
    private lateinit var expenseRepo: ExpenseRepositoryImpl
    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var getExpensesUseCase: GetExpensesUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepo = CategoryRepositoryImpl(db.categoryDao())
        expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        addExpenseUseCase = AddExpenseUseCase(expenseRepo)
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepo)
        getExpensesUseCase = GetExpensesUseCase(expenseRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun testAddExpenseSuccess() = runBlocking {
        populateInitialData(db)

        val result = addExpenseUseCase(
            categoryId = 1, // Food
            categoryName = "Food",
            categoryColorHex = "#F59E0B",
            amountCents = 35000L, // ₹350.00
            type = TransactionType.DEBIT,
            customName = "Lunch"
        )

        assertTrue(result.isSuccess)
        val insertedId = result.getOrNull()
        assertNotNull(insertedId)
        assertTrue(insertedId!! > 0)

        // Verify presence in repository
        val expenses = expenseRepo.getSpreadsheetStream().first()
        assertEquals(1, expenses.size)
        assertEquals(35000L, expenses[0].amountCents)
        assertEquals("Food", expenses[0].categoryName)
        assertEquals("DEBIT", expenses[0].transactionType)
    }

    @Test
    fun testAddExpenseValidationRejectsZeroOrNegativeAmount() = runBlocking {
        populateInitialData(db)

        val resultZero = addExpenseUseCase(
            categoryId = 1,
            categoryName = "Food",
            categoryColorHex = "#F59E0B",
            amountCents = 0L
        )
        assertTrue(resultZero.isFailure)
        assertEquals("Expense amount must be greater than zero.", resultZero.exceptionOrNull()?.message)

        val resultNegative = addExpenseUseCase(
            categoryId = 1,
            categoryName = "Food",
            categoryColorHex = "#F59E0B",
            amountCents = -500L
        )
        assertTrue(resultNegative.isFailure)
    }

    @Test
    fun testAddExpenseWithOthersCustomCategory() = runBlocking {
        populateInitialData(db)

        val result = addExpenseUseCase(
            categoryId = 7, // Others
            categoryName = "Others",
            categoryColorHex = "#64748B",
            amountCents = 75000L,
            customName = "Guitar Strings"
        )
        assertTrue(result.isSuccess)

        val expenses = expenseRepo.getSpreadsheetStream().first()
        assertEquals(1, expenses.size)
        assertEquals("Others", expenses[0].categoryName)
        assertEquals("Guitar Strings", expenses[0].customName)
        assertEquals("Guitar Strings", expenses[0].displayCategory)
    }

    @Test
    fun testExpenseEntryStateAmountCentsCalculation() {
        val state1 = ExpenseEntryState(amountInput = "250.75")
        assertEquals(25075L, state1.amountCents)

        val state2 = ExpenseEntryState(amountInput = "100")
        assertEquals(10000L, state2.amountCents)

        val state3 = ExpenseEntryState(amountInput = "0.5")
        assertEquals(50L, state3.amountCents)

        val state4 = ExpenseEntryState(amountInput = "")
        assertEquals(0L, state4.amountCents)
        assertFalse(state4.isSaveEnabled)
    }

    @Test
    fun testExpenseEntryViewModelKeypadInteractions() = runBlocking {
        populateInitialData(db)

        val viewModel = ExpenseEntryViewModel(getCategoriesUseCase, addExpenseUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Digits entry: 2 -> 5 -> 0 -> . -> 5
        viewModel.onDigitPressed("2")
        viewModel.onDigitPressed("5")
        viewModel.onDigitPressed("0")
        viewModel.onDecimalPressed()
        viewModel.onDigitPressed("5")

        assertEquals("250.5", viewModel.state.value.amountInput)
        assertEquals(25050L, viewModel.state.value.amountCents)

        // Backspace
        viewModel.onBackspacePressed()
        assertEquals("250.", viewModel.state.value.amountInput)

        // Backspace again
        viewModel.onBackspacePressed()
        assertEquals("250", viewModel.state.value.amountInput)
        assertEquals(25000L, viewModel.state.value.amountCents)
    }
}
