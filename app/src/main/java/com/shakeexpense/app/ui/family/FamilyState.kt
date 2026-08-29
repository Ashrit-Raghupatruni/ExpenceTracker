package com.shakeexpense.app.ui.family

import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.MemberSpendingSummary
import com.shakeexpense.app.sync.model.FamilyGroupDto

import com.shakeexpense.app.domain.model.FamilyBudget
import com.shakeexpense.app.domain.usecase.FamilyAiReport

data class FamilyMemberWithSummary(
    val member: FamilyMember,
    val totalDebitCents: Long = 0L,
    val transactionCount: Int = 0
)

data class FamilyState(
    val membersWithSummaries: List<FamilyMemberWithSummary> = emptyList(),
    val currentFamilyGroup: FamilyGroupDto? = null,
    val selectedMember: FamilyMember? = null,
    val selectedMemberSummary: MemberSpendingSummary? = null,
    val selectedMemberBreakdown: List<CategorySubtotal> = emptyList(),
    val selectedMemberExpenses: List<ExpenseRecordItem> = emptyList(),
    val isAddMemberDialogOpen: Boolean = false,
    val isCreateFamilyDialogOpen: Boolean = false,
    val isJoinFamilyDialogOpen: Boolean = false,
    val createdFamilyInvite: FamilyGroupDto? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val showSignInRequiredDialog: Boolean = false,
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    // Family Monthly Limit
    val familyMonthlyLimitCents: Long = 0L,
    val familyCurrentSpentCents: Long = 0L,
    val isEditFamilyLimitDialogOpen: Boolean = false,
    // Shared Family Budgets
    val sharedBudgets: List<FamilyBudget> = emptyList(),
    val isAddBudgetDialogOpen: Boolean = false,
    // Granular Privacy Controls
    val isPrivacySettingsDialogOpen: Boolean = false,
    val editingMemberPrivacy: FamilyMember? = null,
    // Family Reports & AI Insights
    val isFamilyReportOpen: Boolean = false,
    val isGeneratingFamilyReport: Boolean = false,
    val familyAiReport: FamilyAiReport? = null,
    // Premium Entitlement Paywall
    val showUpgradePaywallDialog: Boolean = false,
    val paywallFeatureTitle: String = "",
    val activePlan: com.shakeexpense.app.domain.model.SubscriptionPlan = com.shakeexpense.app.domain.model.SubscriptionPlan.FREE
)
