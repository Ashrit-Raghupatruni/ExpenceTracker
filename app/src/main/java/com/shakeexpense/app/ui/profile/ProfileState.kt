package com.shakeexpense.app.ui.profile

import com.shakeexpense.app.domain.model.MonthlyExpenseSummary
import com.shakeexpense.app.domain.model.UserProfile
import com.shakeexpense.app.domain.usecase.ResetCandidateInfo
import com.shakeexpense.app.domain.usecase.ResetPeriod
import com.shakeexpense.app.ui.theme.AppThemeMode

import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import com.shakeexpense.app.domain.model.SubscriptionPlan

data class ProfileState(
    val userProfile: UserProfile = UserProfile(
        userId = "default_local_user",
        displayName = "Guest User",
        isAnonymous = true
    ),
    val monthlyHistory: List<MonthlyExpenseSummary> = emptyList(),
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isSigningIn: Boolean = false,
    val authErrorMessage: String? = null,
    val expandedMonthKey: String? = null,
    val pendingSyncCount: Int = 0,
    val isOnline: Boolean = true,
    val showResetPeriodDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val resetCandidate: ResetCandidateInfo? = null,
    val isResetting: Boolean = false,
    val resetSuccessMessage: String? = null,
    val recurringPayments: List<RecurringPaymentEntity> = emptyList(),
    val recurringAnnualBurdenCents: Long = 0L,
    val recurringUnusedWarnings: List<String> = emptyList(),
    val financialSafetyScore: Int? = null,
    val primaryOpportunity: String? = null,
    val safetySpendingSubScore: String = "Healthy",
    val safetyBudgetSubScore: String = "Healthy",
    val safetyRecurringSubScore: String = "Healthy",
    val safetyRiskSignalsSubScore: String = "None",
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val showSubscriptionModal: Boolean = false,
    val paymentSuccessMessage: String? = null,
    val lastVerifiedPaymentId: String? = null,
    val isAddOrEditRecurringDialogOpen: Boolean = false,
    val editingRecurringPayment: RecurringPaymentEntity? = null,
    val isShakeEnabled: Boolean = true,
    val isShakeAnywhereEnabled: Boolean = true
)
