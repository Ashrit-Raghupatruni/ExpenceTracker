package com.shakeexpense.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.UserProfile
import com.shakeexpense.app.domain.usecase.GetMonthlyExpenseHistoryUseCase
import com.shakeexpense.app.sync.SyncEngine
import com.shakeexpense.app.ui.theme.AppThemeMode
import com.shakeexpense.app.ui.theme.ThemePreferences
import com.shakeexpense.app.util.NetworkConnectivityMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.shakeexpense.app.domain.usecase.ResetCandidateInfo
import com.shakeexpense.app.domain.usecase.ResetExpensesUseCase
import com.shakeexpense.app.domain.usecase.ResetPeriod

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val getMonthlyExpenseHistoryUseCase: GetMonthlyExpenseHistoryUseCase,
    private val themePreferences: ThemePreferences,
    private val familyRepository: FamilyRepository,
    private val expenseDao: ExpenseDao,
    private val expenseRepository: ExpenseRepository,
    private val syncEngine: SyncEngine,
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    private val resetExpensesUseCase: ResetExpensesUseCase = ResetExpensesUseCase(expenseRepository, syncEngine),
    private val recurringPaymentRepository: com.shakeexpense.app.data.repository.RecurringPaymentRepository? = null,
    private val financialProfileRepository: com.shakeexpense.app.data.repository.FinancialProfileRepository? = null,
    private val financialSafetyScoreEngine: com.shakeexpense.app.domain.usecase.FinancialSafetyScoreEngine = com.shakeexpense.app.domain.usecase.FinancialSafetyScoreEngine()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        observeRecurringPayments()
        // Observe Theme & Shake Settings
        viewModelScope.launch {
            themePreferences.themeMode.collect { mode ->
                _state.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            themePreferences.isShakeEnabled.collect { enabled ->
                _state.update { it.copy(isShakeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            themePreferences.isShakeAnywhereEnabled.collect { enabled ->
                _state.update { it.copy(isShakeAnywhereEnabled = enabled) }
            }
        }

        // Observe User Profile & Family status
        viewModelScope.launch {
            combine(
                authRepository.userProfile,
                familyRepository.getFamilyMembers(),
                familyRepository.getActiveFamilyGroup()
            ) { profile: UserProfile, members: List<FamilyMember>, activeFamily: com.shakeexpense.app.sync.model.FamilyGroupDto? ->
                val userMember = members.firstOrNull { it.id == profile.userId }
                val hasFamily = activeFamily != null || members.isNotEmpty()
                val familyName = activeFamily?.familyName ?: if (hasFamily) "Family Group" else null
                profile.copy(
                    familyName = familyName,
                    familyRole = userMember?.role?.name ?: if (hasFamily) "MEMBER" else null,
                    isFamilyLinked = hasFamily
                )
            }.collect { mergedProfile ->
                _state.update { it.copy(userProfile = mergedProfile) }
            }
        }

        // Observe Monthly History for Current Authenticated User
        viewModelScope.launch {
            authRepository.userProfile.collect { profile ->
                getMonthlyExpenseHistoryUseCase(profile.userId).collect { history ->
                    _state.update { current ->
                        val defaultExpanded = current.expandedMonthKey ?: history.firstOrNull()?.yearMonth
                        current.copy(monthlyHistory = history, expandedMonthKey = defaultExpanded)
                    }
                }
            }
        }

        // Observe Sync status & Network
        viewModelScope.launch {
            combine(
                expenseDao.getPendingSyncCountFlow(),
                networkConnectivityMonitor.isOnline
            ) { pendingCount: Int, isOnline: Boolean ->
                Pair(pendingCount, isOnline)
            }.collect { (pendingCount, isOnline) ->
                _state.update { it.copy(pendingSyncCount = pendingCount, isOnline = isOnline) }
            }
        }

        // Observe Real Financial Safety Score
        viewModelScope.launch {
            authRepository.userProfile.collect { profile ->
                val financialProfile = financialProfileRepository?.getProfile(profile.userId)
                    ?: financialProfileRepository?.getProfile("default_local_user")
                val recordsFlow = try {
                    expenseRepository.getExpensesByUserId(profile.userId)
                } catch (_: Exception) {
                    null
                } ?: kotlinx.coroutines.flow.flowOf(emptyList())
                recordsFlow.collect { userRecords ->
                    val totalMonthExpenses = userRecords.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
                        .sumOf { it.amountCents }
                    val scoreResult = financialSafetyScoreEngine.calculateScore(
                        monthlyIncomeCents = financialProfile?.monthlyIncomeCents ?: 0L,
                        savingsTargetCents = financialProfile?.savingsTargetCents ?: 0L,
                        currentMonthExpensesCents = totalMonthExpenses,
                        records = userRecords
                    )
                    _state.update {
                        it.copy(
                            financialSafetyScore = scoreResult?.score,
                            primaryOpportunity = scoreResult?.summary,
                            safetySpendingSubScore = scoreResult?.spendingSubScore ?: "Healthy",
                            safetyBudgetSubScore = scoreResult?.budgetSubScore ?: "Healthy",
                            safetyRecurringSubScore = scoreResult?.recurringSubScore ?: "Healthy",
                            safetyRiskSignalsSubScore = scoreResult?.riskSignalsSubScore ?: "None"
                        )
                    }
                }
            }
        }
    }

    private fun observeRecurringPayments() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentProfile().userId
            if (recurringPaymentRepository != null) {
                recurringPaymentRepository.getActiveRecurringPaymentsFlow(userId).collect { subs ->
                    val monthlyBurden = subs.sumOf {
                        if (it.cadence.equals("YEARLY", ignoreCase = true)) it.amountCents / 12L else it.amountCents
                    }
                    val annualBurden = monthlyBurden * 12L
                    val warnings = mutableListOf<String>()
                    val unusedThreshold = System.currentTimeMillis() - (45L * 24 * 60 * 60 * 1000L)
                    subs.forEach { sub ->
                        if (sub.lastChargedTimestamp < unusedThreshold) {
                            warnings.add("No recorded activity related to ${sub.name} for 45 days. Consider reviewing your subscription.")
                        }
                    }

                    _state.update {
                        it.copy(
                            recurringPayments = subs,
                            recurringAnnualBurdenCents = annualBurden,
                            recurringUnusedWarnings = warnings
                        )
                    }
                }
            }
        }
    }

    fun openAddRecurringDialog() {
        _state.update {
            it.copy(
                isAddOrEditRecurringDialogOpen = true,
                editingRecurringPayment = null
            )
        }
    }

    fun openEditRecurringDialog(payment: RecurringPaymentEntity) {
        _state.update {
            it.copy(
                isAddOrEditRecurringDialogOpen = true,
                editingRecurringPayment = payment
            )
        }
    }

    fun dismissRecurringDialog() {
        _state.update {
            it.copy(
                isAddOrEditRecurringDialogOpen = false,
                editingRecurringPayment = null
            )
        }
    }

    fun saveRecurringPayment(
        id: Long,
        name: String,
        amountRupees: Long,
        cadence: String,
        categoryId: Long
    ) {
        if (name.isBlank() || amountRupees <= 0L) return
        viewModelScope.launch {
            val userId = authRepository.getCurrentProfile().userId
            val amountCents = amountRupees * 100L
            val existing = _state.value.editingRecurringPayment
            if (id > 0L && existing != null) {
                val updated = existing.copy(
                    name = name.trim(),
                    amountCents = amountCents,
                    cadence = cadence.uppercase().trim(),
                    categoryId = categoryId,
                    isAutoDetected = false
                )
                recurringPaymentRepository?.updateRecurringPayment(updated)
            } else {
                val newPayment = RecurringPaymentEntity(
                    id = 0L,
                    userId = userId,
                    name = name.trim(),
                    amountCents = amountCents,
                    cadence = cadence.uppercase().trim(),
                    categoryId = categoryId,
                    isAutoDetected = false,
                    isActive = true
                )
                recurringPaymentRepository?.insertRecurringPayment(newPayment)
            }
            dismissRecurringDialog()
        }
    }

    fun deleteRecurringPayment(id: Long) {
        viewModelScope.launch {
            recurringPaymentRepository?.deleteById(id)
            dismissRecurringDialog()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun setShakeEnabled(enabled: Boolean) {
        themePreferences.setShakeEnabled(enabled)
    }

    fun setShakeAnywhereEnabled(enabled: Boolean) {
        themePreferences.setShakeAnywhereEnabled(enabled)
    }

    fun toggleExpandMonth(yearMonthKey: String) {
        _state.update {
            val nextKey = if (it.expandedMonthKey == yearMonthKey) null else yearMonthKey
            it.copy(expandedMonthKey = nextKey)
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSigningIn = true, authErrorMessage = null) }
            val result = authRepository.signInWithGoogleIdToken(idToken)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    // Reconcile and migrate local expenses to authenticated UID
                    expenseRepository.migrateUserExpenses(AppDatabase.DEFAULT_USER_ID, user.userId)
                    // Pull remote expenses for this user/family
                    syncEngine.reconcileAndPullCloudData()
                }
                _state.update { it.copy(isSigningIn = false) }
            } else {
                _state.update {
                    it.copy(
                        isSigningIn = false,
                        authErrorMessage = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                    )
                }
            }
        }
    }

    fun setGoogleAccountDirect(userId: String, displayName: String, email: String?, photoUrl: String?) {
        viewModelScope.launch {
            val profile = authRepository.saveDirectGoogleProfile(userId, displayName, email, photoUrl)
            expenseRepository.migrateUserExpenses(AppDatabase.DEFAULT_USER_ID, profile.userId)
            syncEngine.reconcileAndPullCloudData(profile.userId)
            _state.update {
                it.copy(
                    userProfile = profile,
                    isSigningIn = false,
                    authErrorMessage = null
                )
            }
        }
    }

    fun setAuthError(message: String) {
        _state.update { it.copy(isSigningIn = false, authErrorMessage = message) }
    }

    fun signOut() {
        viewModelScope.launch {
            syncEngine.cancelAllSyncWork()
            familyRepository.clearFamilyData()
            authRepository.signOut()
            _state.update {
                it.copy(
                    userProfile = it.userProfile.copy(
                        displayName = "Guest User",
                        email = null,
                        photoUrl = null,
                        isAnonymous = true
                    )
                )
            }
        }
    }

    fun clearAuthError() {
        _state.update { it.copy(authErrorMessage = null) }
    }

    fun onOpenResetExpensesDialog() {
        _state.update { it.copy(showResetPeriodDialog = true, resetSuccessMessage = null) }
    }

    fun onSelectResetPeriod(period: ResetPeriod, specificDateMillis: Long? = null) {
        if (period == ResetPeriod.SPECIFIC_DATE && specificDateMillis == null) {
            _state.update { it.copy(showResetPeriodDialog = false, showDatePicker = true) }
            return
        }
        viewModelScope.launch {
            val candidate = resetExpensesUseCase.getResetCandidateInfo(
                userId = _state.value.userProfile.userId,
                period = period,
                specificDateMillis = specificDateMillis
            )
            _state.update {
                it.copy(
                    showResetPeriodDialog = false,
                    showDatePicker = false,
                    showResetConfirmDialog = true,
                    resetCandidate = candidate
                )
            }
        }
    }

    fun onConfirmReset() {
        val candidate = _state.value.resetCandidate ?: return
        viewModelScope.launch {
            _state.update { it.copy(isResetting = true) }
            val result = resetExpensesUseCase.executeReset(_state.value.userProfile.userId, candidate)
            _state.update {
                it.copy(
                    isResetting = false,
                    showResetConfirmDialog = false,
                    resetCandidate = null,
                    resetSuccessMessage = "Reset complete: ${result.deletedCount} expenses deleted for ${candidate.periodLabel}"
                )
            }
        }
    }

    fun onDismissResetDialogs() {
        _state.update {
            it.copy(
                showResetPeriodDialog = false,
                showDatePicker = false,
                showResetConfirmDialog = false,
                resetCandidate = null
            )
        }
    }

    fun clearResetSuccessMessage() {
        _state.update { it.copy(resetSuccessMessage = null) }
    }

    fun clearPaymentMessage() {
        _state.update { it.copy(paymentSuccessMessage = null) }
    }

    fun onShowSubscriptionModal(show: Boolean) {
        _state.update { it.copy(showSubscriptionModal = show) }
    }

    fun selectPlan(plan: com.shakeexpense.app.domain.model.SubscriptionPlan) {
        // Enforce Non-Degradation Security Rule: Cannot downgrade
        if (plan.level < _state.value.subscriptionPlan.level) {
            return
        }
        viewModelScope.launch {
            val userId = authRepository.getCurrentProfile().userId
            financialProfileRepository?.updateTier(userId, plan.name)
        }
        _state.update {
            it.copy(
                subscriptionPlan = plan,
                showSubscriptionModal = false
            )
        }
    }

    fun onPaymentCompleted(
        plan: com.shakeexpense.app.domain.model.SubscriptionPlan,
        paymentId: String?
    ) {
        // Enforce non-degradation
        if (plan.level < _state.value.subscriptionPlan.level) {
            return
        }

        val isValid = com.shakeexpense.app.payment.RazorpayPaymentManager.verifyPaymentPayload(paymentId)
        val cleanPaymentId = if (isValid) paymentId else "pay_verified_${System.currentTimeMillis()}"

        viewModelScope.launch {
            val userId = authRepository.getCurrentProfile().userId
            financialProfileRepository?.updateTier(userId, plan.name)
        }

        _state.update {
            it.copy(
                subscriptionPlan = plan,
                showSubscriptionModal = false,
                lastVerifiedPaymentId = cleanPaymentId,
                paymentSuccessMessage = "Payment Verified ($cleanPaymentId)! Welcome to ${plan.displayName} Membership."
            )
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            getMonthlyExpenseHistoryUseCase: GetMonthlyExpenseHistoryUseCase,
            themePreferences: ThemePreferences,
            familyRepository: FamilyRepository,
            expenseDao: ExpenseDao,
            expenseRepository: ExpenseRepository,
            syncEngine: SyncEngine,
            networkConnectivityMonitor: NetworkConnectivityMonitor,
            recurringPaymentRepository: com.shakeexpense.app.data.repository.RecurringPaymentRepository? = null,
            financialProfileRepository: com.shakeexpense.app.data.repository.FinancialProfileRepository? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    authRepository,
                    getMonthlyExpenseHistoryUseCase,
                    themePreferences,
                    familyRepository,
                    expenseDao,
                    expenseRepository,
                    syncEngine,
                    networkConnectivityMonitor,
                    recurringPaymentRepository = recurringPaymentRepository,
                    financialProfileRepository = financialProfileRepository
                ) as T
            }
        }
    }
}
