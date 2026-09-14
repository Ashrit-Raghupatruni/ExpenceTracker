package com.shakeexpense.app.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.CategoryRepository
import com.shakeexpense.app.data.repository.CategoryRepositoryImpl
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.data.repository.FamilyRepositoryImpl
import com.shakeexpense.app.data.repository.FinancialProfileRepository
import com.shakeexpense.app.data.repository.FinancialProfileRepositoryImpl
import com.shakeexpense.app.data.repository.RecurringPaymentRepository
import com.shakeexpense.app.data.repository.RecurringPaymentRepositoryImpl
import com.shakeexpense.app.domain.pipeline.TransactionEventPipeline
import com.shakeexpense.app.domain.usecase.AddExpenseUseCase
import com.shakeexpense.app.domain.usecase.AddFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.CreateFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.DeleteExpenseUseCase
import com.shakeexpense.app.domain.usecase.EditExpenseUseCase
import com.shakeexpense.app.domain.usecase.EntitlementManager
import com.shakeexpense.app.domain.usecase.GetCategoriesUseCase
import com.shakeexpense.app.domain.usecase.GetCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetFamilyMembersUseCase
import com.shakeexpense.app.domain.usecase.GetMemberCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetMemberExpensesUseCase
import com.shakeexpense.app.domain.usecase.GetMemberSpendingSummaryUseCase
import com.shakeexpense.app.domain.usecase.GetMonthlyExpenseHistoryUseCase
import com.shakeexpense.app.domain.usecase.GetSpendingTotalsUseCase
import com.shakeexpense.app.domain.usecase.GetSpreadsheetStreamUseCase
import com.shakeexpense.app.domain.usecase.JoinFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.RemoveFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.RequestChildExitUseCase
import com.shakeexpense.app.domain.usecase.SyncFamilyExpensesUseCase
import com.shakeexpense.app.notification.SpendingAlertNotificationManager
import com.shakeexpense.app.sync.SyncApiClient
import com.shakeexpense.app.sync.SyncApiClientProvider
import com.shakeexpense.app.sync.SyncEngine
import com.shakeexpense.app.ui.entry.ExpenseEntryViewModel
import com.shakeexpense.app.ui.family.FamilyViewModel
import com.shakeexpense.app.ui.profile.ProfileViewModel
import com.shakeexpense.app.ui.theme.ThemePreferences
import com.shakeexpense.app.ui.tracker.TrackerViewModel
import com.shakeexpense.app.util.NetworkConnectivityMonitor
import kotlinx.coroutines.CoroutineScope

class AppContainer(
    val context: Context,
    val applicationScope: CoroutineScope
) {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context, applicationScope)
    }

    val spendingAlertNotificationManager: SpendingAlertNotificationManager by lazy {
        SpendingAlertNotificationManager(context)
    }

    val entitlementManager: EntitlementManager by lazy {
        EntitlementManager()
    }

    val transactionPipeline: TransactionEventPipeline by lazy {
        TransactionEventPipeline(
            expenseDao = database.expenseDao(),
            financialProfileDao = database.financialProfileDao(),
            familyGroupDao = database.familyGroupDao(),
            familyMemberDao = database.familyMemberDao(),
            familyBudgetDao = database.familyBudgetDao(),
            recurringPaymentDao = database.recurringPaymentDao(),
            notificationManager = spendingAlertNotificationManager,
            categoryDao = database.categoryDao(),
            pipelineScope = applicationScope
        )
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao(), transactionPipeline)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val familyRepository: FamilyRepository by lazy {
        FamilyRepositoryImpl(
            database.familyMemberDao(),
            database.familyGroupDao(),
            database.familyBudgetDao()
        )
    }

    val financialProfileRepository: FinancialProfileRepository by lazy {
        FinancialProfileRepositoryImpl(database.financialProfileDao())
    }

    val recurringPaymentRepository: RecurringPaymentRepository by lazy {
        RecurringPaymentRepositoryImpl(database.recurringPaymentDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(context)
    }

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences.getInstance(context)
    }

    val syncApiClient: SyncApiClient by lazy {
        SyncApiClientProvider.get()
    }

    val syncEngine: SyncEngine by lazy {
        SyncEngine(expenseRepository, syncApiClient, context, familyRepository)
    }

    val networkMonitor: NetworkConnectivityMonitor by lazy {
        NetworkConnectivityMonitor(context)
    }

    fun createTrackerViewModelFactory(currentUserId: String): ViewModelProvider.Factory {
        return TrackerViewModel.provideFactory(
            getSpreadsheetStreamUseCase = GetSpreadsheetStreamUseCase(expenseRepository),
            getSpendingTotalsUseCase = GetSpendingTotalsUseCase(expenseRepository),
            getCategoryBreakdownUseCase = GetCategoryBreakdownUseCase(expenseRepository),
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            editExpenseUseCase = EditExpenseUseCase(expenseRepository),
            deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository, syncEngine),
            isOnlineFlow = networkMonitor.isOnline,
            financialProfileRepository = financialProfileRepository,
            recurringPaymentRepository = recurringPaymentRepository,
            targetUserId = currentUserId
        )
    }

    fun createExpenseEntryViewModelFactory(): ViewModelProvider.Factory {
        return ExpenseEntryViewModel.provideFactory(
            getCategoriesUseCase = GetCategoriesUseCase(categoryRepository),
            addExpenseUseCase = AddExpenseUseCase(expenseRepository),
            authRepository = authRepository
        )
    }

    fun createFamilyViewModelFactory(): ViewModelProvider.Factory {
        return FamilyViewModel.provideFactory(
            getFamilyMembersUseCase = GetFamilyMembersUseCase(familyRepository),
            addFamilyMemberUseCase = AddFamilyMemberUseCase(familyRepository),
            removeFamilyMemberUseCase = RemoveFamilyMemberUseCase(familyRepository),
            requestChildExitUseCase = RequestChildExitUseCase(familyRepository),
            createFamilyGroupUseCase = CreateFamilyGroupUseCase(syncApiClient, familyRepository),
            joinFamilyGroupUseCase = JoinFamilyGroupUseCase(syncApiClient, familyRepository),
            getMemberSpendingSummaryUseCase = GetMemberSpendingSummaryUseCase(expenseRepository, familyRepository),
            getMemberCategoryBreakdownUseCase = GetMemberCategoryBreakdownUseCase(expenseRepository, familyRepository),
            getMemberExpensesUseCase = GetMemberExpensesUseCase(expenseRepository, familyRepository),
            syncFamilyExpensesUseCase = SyncFamilyExpensesUseCase(syncEngine),
            familyRepository = familyRepository,
            authRepository = authRepository,
            expenseRepository = expenseRepository,
            entitlementManager = entitlementManager
        )
    }

    fun createProfileViewModelFactory(): ViewModelProvider.Factory {
        return ProfileViewModel.provideFactory(
            authRepository = authRepository,
            getMonthlyExpenseHistoryUseCase = GetMonthlyExpenseHistoryUseCase(expenseRepository),
            themePreferences = themePreferences,
            familyRepository = familyRepository,
            expenseDao = database.expenseDao(),
            expenseRepository = expenseRepository,
            syncEngine = syncEngine,
            networkConnectivityMonitor = networkMonitor,
            recurringPaymentRepository = recurringPaymentRepository,
            financialProfileRepository = financialProfileRepository
        )
    }
}
