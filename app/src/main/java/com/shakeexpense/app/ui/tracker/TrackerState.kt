package com.shakeexpense.app.ui.tracker

import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.usecase.AiAssistantResponse
import com.shakeexpense.app.domain.usecase.PredictionResult
import com.shakeexpense.app.domain.usecase.SafeToSpendSummary
import com.shakeexpense.app.domain.usecase.SpendingLimitState
import com.shakeexpense.app.domain.usecase.SpendingTotals
import com.shakeexpense.app.domain.usecase.UnusualSpendingAlert

enum class TrackerTab {
    SPREADSHEET,
    CATEGORY_BREAKDOWN
}

enum class SyncDisplayStatus {
    SYNCED,
    NOT_SYNCED_OFFLINE,
    NOT_SYNCED_PENDING
}

data class TrackerState(
    val selectedTab: TrackerTab = TrackerTab.SPREADSHEET,
    val totals: SpendingTotals = SpendingTotals(),
    val records: List<ExpenseRecordItem> = emptyList(),
    val categoryBreakdowns: List<CategorySubtotal> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val editingRecord: ExpenseRecordItem? = null,
    val isOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val syncDisplayStatus: SyncDisplayStatus = SyncDisplayStatus.SYNCED,
    val isLoading: Boolean = false,
    val safeToSpend: SafeToSpendSummary? = null,
    val showBudgetSetupDialog: Boolean = false,
    val spendingLimitState: SpendingLimitState? = null,
    val nextMonthPrediction: PredictionResult? = null,
    val showSpendingLimitEditDialog: Boolean = false,
    val activePlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val isAdvancedSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<ExpenseRecordItem>? = null,
    val isAiAssistantOpen: Boolean = false,
    val aiAssistantResponse: AiAssistantResponse? = null,
    val unusualAlerts: List<UnusualSpendingAlert> = emptyList(),
    val showUpgradePaywall: Boolean = false,
    val paywallFeatureTitle: String = "",
    val csvExportMessage: String? = null
)
