package com.shakeexpense.app.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shakeexpense.app.data.repository.CategoryRepository
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.DeleteExpenseUseCase
import com.shakeexpense.app.domain.usecase.EditExpenseUseCase
import com.shakeexpense.app.domain.usecase.GetCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetSpendingTotalsUseCase
import com.shakeexpense.app.domain.usecase.GetSpreadsheetStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import android.content.Context
import android.content.Intent
import com.shakeexpense.app.data.repository.FinancialProfileRepository
import com.shakeexpense.app.data.repository.RecurringPaymentRepository
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.usecase.AdvancedSearchQuery
import com.shakeexpense.app.domain.usecase.AdvancedTransactionSearchUseCase
import com.shakeexpense.app.domain.usecase.AiFinancialAssistantUseCase
import com.shakeexpense.app.domain.usecase.EntitlementManager
import com.shakeexpense.app.domain.usecase.ExportTransactionsCsvUseCase
import com.shakeexpense.app.domain.usecase.MonthlySpendingLimitManager
import com.shakeexpense.app.domain.usecase.NextMonthExpensePredictor
import com.shakeexpense.app.domain.usecase.PredictionResult
import com.shakeexpense.app.domain.usecase.SafeToSpendCalculator
import com.shakeexpense.app.domain.usecase.UnusualSpendingDetector

class TrackerViewModel(
    private val getSpreadsheetStreamUseCase: GetSpreadsheetStreamUseCase,
    private val getSpendingTotalsUseCase: GetSpendingTotalsUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val editExpenseUseCase: EditExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val isOnlineFlow: Flow<Boolean> = flowOf(true),
    private val financialProfileRepository: FinancialProfileRepository? = null,
    private val recurringPaymentRepository: RecurringPaymentRepository? = null,
    private val targetUserId: String = "default_local_user",
    private val safeToSpendCalculator: SafeToSpendCalculator = SafeToSpendCalculator(),
    private val nextMonthExpensePredictor: NextMonthExpensePredictor = NextMonthExpensePredictor(),
    private val monthlySpendingLimitManager: MonthlySpendingLimitManager = MonthlySpendingLimitManager(),
    private val entitlementManager: EntitlementManager = EntitlementManager(),
    private val advancedSearchUseCase: AdvancedTransactionSearchUseCase = AdvancedTransactionSearchUseCase(entitlementManager),
    private val exportCsvUseCase: ExportTransactionsCsvUseCase = ExportTransactionsCsvUseCase(entitlementManager),
    private val aiAssistantUseCase: AiFinancialAssistantUseCase = AiFinancialAssistantUseCase(entitlementManager),
    private val unusualSpendingDetector: UnusualSpendingDetector = UnusualSpendingDetector(entitlementManager)
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerState(isLoading = true))
    val state: StateFlow<TrackerState> = _state.asStateFlow()
    private var userSelectedLimitCents: Long? = null

    init {
        observeTrackerData()
        observeCategories()
        observeSyncAndConnectivity()
        observeSafeToSpend()
    }

    private fun observeSafeToSpend() {
        viewModelScope.launch {
            val profileFlow = financialProfileRepository?.let { repo ->
                combine(
                    repo.getProfileFlow(targetUserId),
                    repo.getProfileFlow("default_local_user")
                ) { primary, defaultUser ->
                    primary ?: defaultUser
                }
            } ?: flowOf(null)
            val recurringFlow = recurringPaymentRepository?.getActiveRecurringPaymentsFlow(targetUserId) ?: flowOf(emptyList())

            combine(
                profileFlow,
                recurringFlow,
                getSpendingTotalsUseCase(targetUserId),
                getSpreadsheetStreamUseCase(targetUserId)
            ) { profile, recurring, totals, records ->
                val plan = try {
                    SubscriptionPlan.valueOf(profile?.tier ?: "FREE")
                } catch (_: Exception) {
                    SubscriptionPlan.FREE
                }
                entitlementManager.updatePlan(plan)

                val safeSummary = safeToSpendCalculator.calculate(profile, totals.thisMonthDebitCents, recurring)
                val prediction = if (entitlementManager.canAccess(plan, FeatureCapability.EXPENSE_PREDICTION)) {
                    nextMonthExpensePredictor.predictFromRecords(records, recurring, profile?.monthlyIncomeCents ?: 0L)
                } else null

                val savedLimitCents = profile?.monthlySpendingLimitCents?.takeIf { it > 0L }
                val effectiveLimitCents = userSelectedLimitCents ?: savedLimitCents

                val predictedCents = if (prediction is PredictionResult.Success) prediction.predictedNextMonthCents else 0L
                val limitState = monthlySpendingLimitManager.calculateLimitState(
                    monthlyIncomeCents = profile?.monthlyIncomeCents ?: 0L,
                    predictedExpensesCents = predictedCents,
                    currentMonthExpensesCents = totals.thisMonthDebitCents,
                    userSelectedLimitCents = effectiveLimitCents
                )

                val alerts = unusualSpendingDetector.detectAlerts(records, plan)

                _state.update {
                    it.copy(
                        safeToSpend = safeSummary,
                        financialProfile = profile,
                        recurringPayments = recurring,
                        nextMonthPrediction = prediction,
                        spendingLimitState = limitState,
                        activePlan = plan,
                        unusualAlerts = alerts
                    )
                }
            }.collect { }
        }
    }

    fun onOpenSubScreen(subScreen: TrackerSubScreen) {
        _state.update { it.copy(activeSubScreen = subScreen) }
    }

    fun onCloseSubScreen() {
        _state.update { it.copy(activeSubScreen = TrackerSubScreen.NONE) }
    }

    fun onMarkAllNotificationsRead(ids: List<String>) {
        _state.update { it.copy(readNotificationIds = it.readNotificationIds + ids) }
    }

    fun onMarkNotificationRead(id: String) {
        _state.update { it.copy(readNotificationIds = it.readNotificationIds + id) }
    }

    fun onDismissNotification(id: String) {
        _state.update { it.copy(dismissedNotificationIds = it.dismissedNotificationIds + id) }
    }

    fun onOpenSpendingLimitEditDialog() {
        _state.update { it.copy(showSpendingLimitEditDialog = true) }
    }

    fun onDismissSpendingLimitEditDialog() {
        _state.update { it.copy(showSpendingLimitEditDialog = false) }
    }

    fun onSaveUserMonthlyLimit(limitRupees: Long?) {
        userSelectedLimitCents = limitRupees?.let { it * 100L }
        viewModelScope.launch {
            if (limitRupees != null) {
                financialProfileRepository?.updateSpendingLimit(targetUserId, limitRupees * 100L)
            }
        }
        _state.update { current ->
            val existingState = current.spendingLimitState
            val updatedState = existingState?.copy(
                userSelectedLimitCents = userSelectedLimitCents,
                activeMonthlyLimitCents = userSelectedLimitCents ?: existingState.systemRecommendedLimitCents,
                usagePercentage = if ((userSelectedLimitCents ?: existingState.systemRecommendedLimitCents) > 0L) {
                    (existingState.currentMonthExpensesCents.toDouble() / (userSelectedLimitCents ?: existingState.systemRecommendedLimitCents).toDouble()) * 100.0
                } else 0.0,
                explanation = if (userSelectedLimitCents != null) "Custom limit set by you." else existingState.explanation
            )
            current.copy(
                spendingLimitState = updatedState,
                showSpendingLimitEditDialog = false
            )
        }
    }

    fun onOpenAdvancedSearch() {
        if (!entitlementManager.canAccess(state.value.activePlan, FeatureCapability.ADVANCED_SEARCH)) {
            _state.update { it.copy(showUpgradePaywall = true, paywallFeatureTitle = "Advanced Transaction Search") }
        } else {
            _state.update { it.copy(isAdvancedSearchOpen = true) }
        }
    }

    fun onCloseAdvancedSearch() {
        _state.update { it.copy(isAdvancedSearchOpen = false, searchResults = null, searchQuery = "") }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = null) }
        } else {
            val results = advancedSearchUseCase.filterRecords(
                records = state.value.records,
                query = AdvancedSearchQuery(keyword = query),
                plan = state.value.activePlan
            )
            _state.update { it.copy(searchResults = results) }
        }
    }

    fun onOpenAiAssistant() {
        if (!entitlementManager.canAccess(state.value.activePlan, FeatureCapability.AI_ASSISTANT)) {
            _state.update { it.copy(showUpgradePaywall = true, paywallFeatureTitle = "AI Financial Assistant") }
        } else {
            _state.update { it.copy(isAiAssistantOpen = true) }
        }
    }

    fun onCloseAiAssistant() {
        _state.update { it.copy(isAiAssistantOpen = false, aiAssistantResponse = null) }
    }

    fun onAskAiQuestion(question: String) {
        val resp = aiAssistantUseCase.ask(
            question = question,
            plan = state.value.activePlan,
            records = state.value.records,
            monthlyIncomeCents = state.value.safeToSpend?.monthlyIncomeCents ?: 0L,
            monthlySafeDailyCents = state.value.safeToSpend?.safeToSpendTodayCents ?: 0L
        )
        _state.update { it.copy(aiAssistantResponse = resp) }
    }

    fun onExportCsv(context: Context) {
        if (!entitlementManager.canAccess(state.value.activePlan, FeatureCapability.CSV_EXPORT)) {
            _state.update { it.copy(showUpgradePaywall = true, paywallFeatureTitle = "CSV Transaction Export") }
            return
        }
        val csv = exportCsvUseCase.generateCsvFromRecords(state.value.records, state.value.activePlan)
        if (csv != null) {
            try {
                val file = java.io.File(context.cacheDir, "shake_expense_export_${System.currentTimeMillis()}.csv")
                file.writeText(csv)
                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ShakeExpense Transactions Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Transactions CSV"))
            } catch (_: Exception) {
                _state.update { it.copy(csvExportMessage = "Failed to launch share intent") }
            }
        }
    }

    fun onDismissPaywall() {
        _state.update { it.copy(showUpgradePaywall = false) }
    }

    private fun observeTrackerData() {
        viewModelScope.launch {
            combine(
                getSpreadsheetStreamUseCase(targetUserId),
                getSpendingTotalsUseCase(targetUserId),
                getCategoryBreakdownUseCase(targetUserId)
            ) { records, totals, breakdowns ->
                Triple(records, totals, breakdowns)
            }.collect { (records, totals, breakdowns) ->
                _state.update { current ->
                    current.copy(
                        records = records,
                        totals = totals,
                        categoryBreakdowns = breakdowns,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun observeSyncAndConnectivity() {
        viewModelScope.launch {
            combine(
                isOnlineFlow,
                expenseRepository.getPendingSyncCount()
            ) { isOnline, pendingCount ->
                Pair(isOnline, pendingCount)
            }.collect { (isOnline, pendingCount) ->
                val syncStatus = when {
                    !isOnline -> SyncDisplayStatus.NOT_SYNCED_OFFLINE
                    pendingCount > 0 -> SyncDisplayStatus.NOT_SYNCED_PENDING
                    else -> SyncDisplayStatus.SYNCED
                }

                _state.update {
                    it.copy(
                        isOnline = isOnline,
                        pendingSyncCount = pendingCount,
                        syncDisplayStatus = syncStatus
                    )
                }
            }
        }
    }

    fun onTabSelected(tab: TrackerTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun onSelectRecordForEdit(record: ExpenseRecordItem) {
        _state.update { it.copy(editingRecord = record) }
    }

    fun onDismissEdit() {
        _state.update { it.copy(editingRecord = null) }
    }

    fun onUpdateExpense(
        uuid: String,
        amountCents: Long,
        categoryId: Long,
        type: TransactionType,
        customName: String?
    ) {
        viewModelScope.launch {
            editExpenseUseCase(
                uuid = uuid,
                amountCents = amountCents,
                categoryId = categoryId,
                type = type,
                customName = customName
            )
            _state.update { it.copy(editingRecord = null) }
        }
    }

    fun onDeleteExpense(uuid: String) {
        viewModelScope.launch {
            deleteExpenseUseCase(uuid)
            _state.update { it.copy(editingRecord = null) }
        }
    }

    fun onOpenBudgetSetupDialog() {
        _state.update { it.copy(showBudgetSetupDialog = true) }
    }

    fun onDismissBudgetSetupDialog() {
        _state.update { it.copy(showBudgetSetupDialog = false) }
    }

    fun onSaveBudgetSetup(incomeCents: Long, savingsTargetCents: Long, cycleDay: Int) {
        viewModelScope.launch {
            financialProfileRepository?.updateBudgetParameters(targetUserId, incomeCents, savingsTargetCents, cycleDay)
            val profile = financialProfileRepository?.getProfile(targetUserId)
                ?: financialProfileRepository?.getProfile("default_local_user")
            val totals = state.value.totals
            val safeSummary = safeToSpendCalculator.calculate(profile, totals.thisMonthDebitCents, emptyList())
            _state.update {
                it.copy(
                    safeToSpend = safeSummary,
                    showBudgetSetupDialog = false
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            getSpreadsheetStreamUseCase: GetSpreadsheetStreamUseCase,
            getSpendingTotalsUseCase: GetSpendingTotalsUseCase,
            getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
            categoryRepository: CategoryRepository,
            expenseRepository: ExpenseRepository,
            editExpenseUseCase: EditExpenseUseCase,
            deleteExpenseUseCase: DeleteExpenseUseCase,
            isOnlineFlow: Flow<Boolean> = flowOf(true),
            financialProfileRepository: FinancialProfileRepository? = null,
            recurringPaymentRepository: RecurringPaymentRepository? = null,
            targetUserId: String = "default_local_user"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TrackerViewModel(
                    getSpreadsheetStreamUseCase,
                    getSpendingTotalsUseCase,
                    getCategoryBreakdownUseCase,
                    categoryRepository,
                    expenseRepository,
                    editExpenseUseCase,
                    deleteExpenseUseCase,
                    isOnlineFlow,
                    financialProfileRepository,
                    recurringPaymentRepository,
                    targetUserId
                ) as T
            }
        }
    }
}
