package com.shakeexpense.app.ui.profile

import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.UserProfile
import com.shakeexpense.app.domain.usecase.GetMonthlyExpenseHistoryUseCase
import com.shakeexpense.app.sync.SyncEngine
import com.shakeexpense.app.ui.theme.AppThemeMode
import com.shakeexpense.app.ui.theme.ThemePreferences
import com.shakeexpense.app.util.NetworkConnectivityMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var getMonthlyExpenseHistoryUseCase: GetMonthlyExpenseHistoryUseCase
    private lateinit var themePreferences: ThemePreferences
    private lateinit var familyRepository: FamilyRepository
    private lateinit var expenseDao: ExpenseDao
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var syncEngine: SyncEngine
    private lateinit var networkMonitor: NetworkConnectivityMonitor
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock(AuthRepository::class.java)
        getMonthlyExpenseHistoryUseCase = mock(GetMonthlyExpenseHistoryUseCase::class.java)
        themePreferences = mock(ThemePreferences::class.java)
        familyRepository = mock(FamilyRepository::class.java)
        expenseDao = mock(ExpenseDao::class.java)
        expenseRepository = mock(ExpenseRepository::class.java)
        syncEngine = mock(SyncEngine::class.java)
        networkMonitor = mock(NetworkConnectivityMonitor::class.java)

        `when`(themePreferences.themeMode).thenReturn(MutableStateFlow(AppThemeMode.DARK))
        `when`(themePreferences.isShakeEnabled).thenReturn(MutableStateFlow(true))
        `when`(themePreferences.isShakeAnywhereEnabled).thenReturn(MutableStateFlow(true))
        `when`(authRepository.userProfile).thenReturn(
            flowOf(
                UserProfile(
                    userId = "user_test",
                    displayName = "Test User",
                    email = "test@gmail.com",
                    photoUrl = "https://example.com/photo.jpg",
                    isAnonymous = false
                )
            )
        )
        `when`(familyRepository.getFamilyMembers()).thenReturn(flowOf(emptyList()))
        `when`(familyRepository.getActiveFamilyGroup()).thenReturn(flowOf(null))
        `when`(getMonthlyExpenseHistoryUseCase.invoke(org.mockito.ArgumentMatchers.anyString())).thenReturn(flowOf(emptyList()))
        `when`(getMonthlyExpenseHistoryUseCase.invoke(org.mockito.ArgumentMatchers.isNull())).thenReturn(flowOf(emptyList()))
        `when`(expenseDao.getPendingSyncCountFlow()).thenReturn(flowOf(0))
        `when`(networkMonitor.isOnline).thenReturn(MutableStateFlow(true))
        `when`(authRepository.getCurrentProfile()).thenReturn(
            UserProfile(
                userId = "user_test",
                displayName = "Test User",
                email = "test@gmail.com"
            )
        )

        viewModel = ProfileViewModel(
            authRepository,
            getMonthlyExpenseHistoryUseCase,
            themePreferences,
            familyRepository,
            expenseDao,
            expenseRepository,
            syncEngine,
            networkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testThemeModeUpdate() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AppThemeMode.DARK, viewModel.state.value.themeMode)

        viewModel.setThemeMode(AppThemeMode.LIGHT)
        verify(themePreferences).setThemeMode(AppThemeMode.LIGHT)
    }

    @Test
    fun testToggleExpandMonth() = runTest {
        assertNull(viewModel.state.value.expandedMonthKey)

        viewModel.toggleExpandMonth("2026-08")
        assertEquals("2026-08", viewModel.state.value.expandedMonthKey)

        viewModel.toggleExpandMonth("2026-08")
        assertNull(viewModel.state.value.expandedMonthKey)
    }

    @Test
    fun testResetExpensesDialogAndFlow() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Open Reset Dialog
        viewModel.onOpenResetExpensesDialog()
        assertTrue(viewModel.state.value.showResetPeriodDialog)
        assertNull(viewModel.state.value.resetSuccessMessage)

        // 2. Select Specific Date without date -> triggers date picker
        viewModel.onSelectResetPeriod(com.shakeexpense.app.domain.usecase.ResetPeriod.SPECIFIC_DATE)
        assertTrue(viewModel.state.value.showDatePicker)
        org.junit.Assert.assertFalse(viewModel.state.value.showResetPeriodDialog)

        // 3. Dismiss Dialogs
        viewModel.onDismissResetDialogs()
        org.junit.Assert.assertFalse(viewModel.state.value.showDatePicker)
        org.junit.Assert.assertFalse(viewModel.state.value.showResetPeriodDialog)
        org.junit.Assert.assertFalse(viewModel.state.value.showResetConfirmDialog)
    }
}
