package com.shakeexpense.app.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.usecase.AddFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.CreateFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.GetFamilyMembersUseCase
import com.shakeexpense.app.domain.usecase.GetMemberCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetMemberExpensesUseCase
import com.shakeexpense.app.domain.usecase.GetMemberSpendingSummaryUseCase
import com.shakeexpense.app.domain.usecase.JoinFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.RemoveFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.RequestChildExitUseCase
import com.shakeexpense.app.domain.usecase.SyncFamilyExpensesUseCase
import com.shakeexpense.app.sync.model.FamilyGroupDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.FamilyBudget
import com.shakeexpense.app.domain.model.FamilyPrivacySettings
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.usecase.EntitlementManager
import com.shakeexpense.app.domain.usecase.FamilyAiReportGeneratorUseCase
import kotlinx.coroutines.flow.firstOrNull
import com.shakeexpense.app.notification.SpendingAlertNotificationManager

class FamilyViewModel(
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase,
    private val addFamilyMemberUseCase: AddFamilyMemberUseCase,
    private val removeFamilyMemberUseCase: RemoveFamilyMemberUseCase,
    private val requestChildExitUseCase: RequestChildExitUseCase,
    private val createFamilyGroupUseCase: CreateFamilyGroupUseCase,
    private val joinFamilyGroupUseCase: JoinFamilyGroupUseCase,
    private val getMemberSpendingSummaryUseCase: GetMemberSpendingSummaryUseCase,
    private val getMemberCategoryBreakdownUseCase: GetMemberCategoryBreakdownUseCase,
    private val getMemberExpensesUseCase: GetMemberExpensesUseCase,
    private val syncFamilyExpensesUseCase: SyncFamilyExpensesUseCase,
    private val familyRepository: FamilyRepository,
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository? = null,
    private val entitlementManager: EntitlementManager = EntitlementManager(),
    private val familyAiReportGenerator: FamilyAiReportGeneratorUseCase = FamilyAiReportGeneratorUseCase()
) : ViewModel() {

    private val _state = MutableStateFlow(FamilyState(isLoading = true))
    val state: StateFlow<FamilyState> = _state.asStateFlow()

    private var memberDetailJob: Job? = null

    init {
        observeFamilyData()
        viewModelScope.launch {
            entitlementManager.currentEntitlement.collect { ent ->
                _state.update { it.copy(activePlan = ent.plan) }
            }
        }
    }

    private fun observeFamilyData() {
        val expensesStream = expenseRepository?.getSpreadsheetStream() ?: kotlinx.coroutines.flow.flowOf(emptyList())

        viewModelScope.launch {
            combine(
                getFamilyMembersUseCase(),
                familyRepository.getActiveFamilyGroup(),
                authRepository.userProfile,
                expensesStream
            ) { members, activeFamily, profile, allExpenses ->
                FamilyDataBundle(members, activeFamily, profile.userId, allExpenses)
            }.collect { (members, activeFamily, currentUid, allExpenses) ->
                val familyLimit = activeFamily?.monthlySpendingLimitCents ?: 0L

                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val startOfMonth = calendar.timeInMillis

                // Calculate spending per member for the current month
                var totalFamilySpent = 0L
                val memberSummaries = members.map { member ->
                    val memberDebits = allExpenses.filter {
                        (it.userId == member.id || (member.id == currentUid && it.userId == "default_local_user")) &&
                        it.transactionType.equals("DEBIT", ignoreCase = true)
                    }
                    val currentMonthDebits = memberDebits.filter { it.timestamp >= startOfMonth }
                    val memberTotal = currentMonthDebits.sumOf { it.amountCents }
                    totalFamilySpent += memberTotal
                    FamilyMemberWithSummary(
                        member = member,
                        totalDebitCents = memberTotal,
                        transactionCount = currentMonthDebits.size
                    )
                }

                _state.update { current ->
                    current.copy(
                        membersWithSummaries = memberSummaries,
                        currentFamilyGroup = activeFamily ?: current.currentFamilyGroup,
                        currentUserId = currentUid,
                        familyMonthlyLimitCents = familyLimit,
                        familyCurrentSpentCents = totalFamilySpent,
                        isLoading = false
                    )
                }
            }
        }

        // Observe shared category budgets
        viewModelScope.launch {
            combine(
                familyRepository.getActiveFamilyGroup(),
                expensesStream
            ) { group, allExpenses ->
                Pair(group, allExpenses)
            }.collect { (group, allExpenses) ->
                if (group != null) {
                    val calendar = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.DAY_OF_MONTH, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val startOfMonth = calendar.timeInMillis

                    val budgetsFlow = familyRepository.getFamilyBudgets(group.familyId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
                    budgetsFlow.collect { budgets ->
                        val updatedBudgets = budgets.map { b ->
                            val catSpent = allExpenses.filter {
                                it.transactionType.equals("DEBIT", ignoreCase = true) &&
                                it.timestamp >= startOfMonth &&
                                it.categoryName.equals(b.categoryName, ignoreCase = true)
                            }.sumOf { it.amountCents }
                            b.copy(spentCents = catSpent)
                        }
                        _state.update { it.copy(sharedBudgets = updatedBudgets) }
                    }
                }
            }
        }

        // Auto-sync family members and transactions from cloud
        viewModelScope.launch {
            familyRepository.getActiveFamilyGroup().collect { group ->
                if (group != null) {
                    try {
                        syncFamilyExpensesUseCase(group.familyId)
                    } catch (e: Exception) {
                        // Handled
                    }
                }
            }
        }
    }

    private data class FamilyDataBundle(
        val members: List<FamilyMember>,
        val activeFamily: FamilyGroupDto?,
        val currentUid: String,
        val allExpenses: List<com.shakeexpense.app.domain.model.ExpenseRecordItem>
    )

    fun onMemberSelected(member: FamilyMember) {
        _state.update { it.copy(selectedMember = member) }
        loadMemberDetails(member.id, member.role)
    }

    fun onDismissMemberDetail() {
        memberDetailJob?.cancel()
        _state.update {
            it.copy(
                selectedMember = null,
                selectedMemberSummary = null,
                selectedMemberBreakdown = emptyList(),
                selectedMemberExpenses = emptyList()
            )
        }
    }

    private fun loadMemberDetails(userId: String, role: FamilyRole) {
        memberDetailJob?.cancel()
        memberDetailJob = viewModelScope.launch {
            combine(
                getMemberSpendingSummaryUseCase(userId, role),
                getMemberCategoryBreakdownUseCase(userId),
                getMemberExpensesUseCase(userId)
            ) { summary, breakdown, expenses ->
                Triple(summary, breakdown, expenses)
            }.collect { (summary, breakdown, expenses) ->
                _state.update { current ->
                    current.copy(
                        selectedMemberSummary = summary,
                        selectedMemberBreakdown = breakdown,
                        selectedMemberExpenses = expenses
                    )
                }
            }
        }
    }

    fun openAddMemberDialog() = _state.update { it.copy(isAddMemberDialogOpen = true) }
    fun closeAddMemberDialog() = _state.update { it.copy(isAddMemberDialogOpen = false) }

    fun openCreateFamilyDialog() = _state.update { it.copy(isCreateFamilyDialogOpen = true) }
    fun closeCreateFamilyDialog() = _state.update { it.copy(isCreateFamilyDialogOpen = false) }

    fun openJoinFamilyDialog() = _state.update { it.copy(isJoinFamilyDialogOpen = true) }
    fun closeJoinFamilyDialog() = _state.update { it.copy(isJoinFamilyDialogOpen = false) }
    fun dismissCreatedFamilyInvite() = _state.update { it.copy(createdFamilyInvite = null) }
    fun dismissSignInRequiredDialog() = _state.update { it.copy(showSignInRequiredDialog = false) }

    fun createFamily(familyName: String) {
        viewModelScope.launch {
            val profile = authRepository.getCurrentProfile()
            if (profile.isAnonymous || profile.email.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        isCreateFamilyDialogOpen = false,
                        showSignInRequiredDialog = true,
                        syncMessage = "Please sign in with Google to create a family group"
                    )
                }
                return@launch
            }
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            val currentUserId = profile.userId
            val result = createFamilyGroupUseCase(familyName, currentUserId, profile.displayName)
            if (result.isSuccess) {
                val family = result.getOrNull()
                _state.update {
                    it.copy(
                        currentFamilyGroup = family,
                        createdFamilyInvite = family,
                        isCreateFamilyDialogOpen = false,
                        isSyncing = false,
                        syncMessage = "Created family '${family?.familyName}' • Invite Code: ${family?.inviteCode}"
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = "Failed to create family: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun joinFamily(inviteCode: String, userName: String, role: FamilyRole) {
        viewModelScope.launch {
            val profile = authRepository.getCurrentProfile()
            if (profile.isAnonymous || profile.email.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        isJoinFamilyDialogOpen = false,
                        showSignInRequiredDialog = true,
                        syncMessage = "Please sign in with Google to join a family group"
                    )
                }
                return@launch
            }
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            val currentUserId = profile.userId
            val resolvedName = userName.ifBlank { profile.displayName }
            val member = FamilyMember(
                id = currentUserId,
                familyId = "",
                name = resolvedName,
                role = role,
                deviceId = "device_local"
            )
            val result = joinFamilyGroupUseCase(inviteCode, member)
            if (result.isSuccess) {
                val familyGroup = FamilyGroupDto(
                    familyId = result.familyId ?: "",
                    familyName = result.familyName ?: "Family Group",
                    creatorUserId = "",
                    inviteCode = inviteCode.uppercase(),
                    createdAt = System.currentTimeMillis()
                )
                _state.update {
                    it.copy(
                        currentFamilyGroup = familyGroup,
                        isJoinFamilyDialogOpen = false,
                        isSyncing = false,
                        syncMessage = "Successfully joined family '${result.familyName}'"
                    )
                }
                triggerSync()
            } else {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = "Failed to join: ${result.errorMessage}"
                    )
                }
            }
        }
    }

    fun addFamilyMember(name: String, role: FamilyRole) {
        viewModelScope.launch {
            val familyId = _state.value.currentFamilyGroup?.familyId ?: "family_local_01"
            val result = addFamilyMemberUseCase(name.trim(), role, familyId)
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isAddMemberDialogOpen = false,
                        syncMessage = "Added member '$name' ($role)"
                    )
                }
            }
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch {
            removeFamilyMemberUseCase(memberId)
            onDismissMemberDetail()
            _state.update { it.copy(syncMessage = "Family member removed successfully") }
        }
    }

    fun requestChildExit(childMemberId: String) {
        viewModelScope.launch {
            val result = requestChildExitUseCase(childMemberId)
            _state.update { it.copy(syncMessage = result.getOrNull()) }
        }
    }

    fun dismissChildExit(childMemberId: String) {
        viewModelScope.launch {
            familyRepository.setMemberExitRequested(childMemberId, false)
            _state.update { it.copy(syncMessage = "Exit request dismissed") }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val profile = authRepository.getCurrentProfile()
            if (profile.isAnonymous || profile.email.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        showSignInRequiredDialog = true,
                        syncMessage = "Please sign in with Google to sync cloud data"
                    )
                }
                return@launch
            }
            _state.update { it.copy(isSyncing = true, syncMessage = null) }
            try {
                val familyId = _state.value.currentFamilyGroup?.familyId ?: "family_default_01"
                val count = syncFamilyExpensesUseCase(familyId)
                _state.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = if (count > 0) "Synchronized $count transactions with cloud" else "All transactions in sync with cloud"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSyncing = false, syncMessage = "Sync failed: ${e.message}") }
            }
        }
    }

    fun onOpenEditFamilyLimitDialog() {
        if (!entitlementManager.hasFeature(FeatureCapability.FAMILY_LIMIT_ALERTS)) {
            _state.update {
                it.copy(
                    showUpgradePaywallDialog = true,
                    paywallFeatureTitle = "Family Monthly Limit & Alerts"
                )
            }
            return
        }
        _state.update { it.copy(isEditFamilyLimitDialogOpen = true) }
    }

    fun onDismissEditFamilyLimitDialog() {
        _state.update { it.copy(isEditFamilyLimitDialogOpen = false) }
    }

    fun onSaveFamilyLimit(limitRupees: Long) {
        if (limitRupees <= 0) {
            _state.update { it.copy(syncMessage = "Limit must be greater than zero") }
            return
        }
        viewModelScope.launch {
            val familyId = _state.value.currentFamilyGroup?.familyId ?: return@launch
            val limitCents = limitRupees * 100L
            familyRepository.updateFamilySpendingLimit(familyId, limitCents)
            try {
                com.shakeexpense.app.sync.SyncApiClientProvider.get().updateFamilySpendingLimit(familyId, limitCents)
            } catch (_: Exception) {}
            SpendingAlertNotificationManager.resetThresholds(familyId, "FAMILY")
            _state.update {
                it.copy(
                    familyMonthlyLimitCents = limitCents,
                    isEditFamilyLimitDialogOpen = false,
                    syncMessage = "Family monthly spending limit set to ₹$limitRupees"
                )
            }
        }
    }

    fun onOpenAddBudgetDialog() {
        if (!entitlementManager.hasFeature(FeatureCapability.SHARED_FAMILY_BUDGETS)) {
            _state.update {
                it.copy(
                    showUpgradePaywallDialog = true,
                    paywallFeatureTitle = "Shared Family Category Budgets"
                )
            }
            return
        }
        _state.update { it.copy(isAddBudgetDialogOpen = true) }
    }

    fun onDismissAddBudgetDialog() {
        _state.update { it.copy(isAddBudgetDialogOpen = false) }
    }

    fun onSaveCategoryBudget(categoryName: String, limitRupees: Long) {
        if (limitRupees <= 0 || categoryName.isBlank()) {
            _state.update { it.copy(syncMessage = "Please specify a valid category and budget") }
            return
        }
        viewModelScope.launch {
            val familyId = _state.value.currentFamilyGroup?.familyId ?: return@launch
            val budget = FamilyBudget(
                id = "budget_${familyId}_${categoryName.lowercase().trim()}",
                familyId = familyId,
                categoryName = categoryName.trim(),
                limitCents = limitRupees * 100L
            )
            familyRepository.saveFamilyBudget(budget)
            _state.update {
                it.copy(
                    isAddBudgetDialogOpen = false,
                    syncMessage = "Shared budget for $categoryName updated (₹$limitRupees)"
                )
            }
        }
    }

    fun onDeleteCategoryBudget(budgetId: String) {
        viewModelScope.launch {
            familyRepository.deleteFamilyBudget(budgetId)
            _state.update { it.copy(syncMessage = "Category budget removed") }
        }
    }

    fun onOpenPrivacySettingsDialog(member: FamilyMember) {
        _state.update {
            it.copy(
                isPrivacySettingsDialogOpen = true,
                editingMemberPrivacy = member
            )
        }
    }

    fun onDismissPrivacySettingsDialog() {
        _state.update {
            it.copy(
                isPrivacySettingsDialogOpen = false,
                editingMemberPrivacy = null
            )
        }
    }

    fun onSavePrivacySettings(memberId: String, settings: FamilyPrivacySettings) {
        viewModelScope.launch {
            familyRepository.updateMemberPrivacy(memberId, settings)
            _state.update {
                it.copy(
                    isPrivacySettingsDialogOpen = false,
                    editingMemberPrivacy = null,
                    syncMessage = "Privacy settings updated"
                )
            }
        }
    }

    fun onGenerateFamilyReport() {
        if (!entitlementManager.hasFeature(FeatureCapability.FAMILY_REPORTS)) {
            _state.update {
                it.copy(
                    showUpgradePaywallDialog = true,
                    paywallFeatureTitle = "Family Financial Reports & AI Insights"
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isGeneratingFamilyReport = true) }
            val currentUid = _state.value.currentUserId
            val familyMembers = _state.value.membersWithSummaries.map { it.member }

            // Aggregate current month transactions for family
            val currentExpenses = mutableListOf<com.shakeexpense.app.domain.model.ExpenseRecordItem>()
            if (expenseRepository != null) {
                for (m in familyMembers) {
                    val mExpenses: List<com.shakeexpense.app.domain.model.ExpenseRecordItem> =
                        expenseRepository.getExpensesByUserId(m.id).firstOrNull() ?: emptyList()
                    currentExpenses.addAll(mExpenses)
                }
            }

            val report = familyAiReportGenerator(
                currentMonthExpenses = currentExpenses,
                previousMonthExpenses = emptyList(),
                members = familyMembers,
                familyLimitCents = _state.value.familyMonthlyLimitCents,
                sharedBudgets = _state.value.sharedBudgets,
                familyMonthlyIncomeCents = 0L,
                currentViewerUserId = currentUid
            )

            _state.update {
                it.copy(
                    familyAiReport = report,
                    isGeneratingFamilyReport = false,
                    isFamilyReportOpen = true
                )
            }
        }
    }

    fun onDismissFamilyReport() {
        _state.update { it.copy(isFamilyReportOpen = false) }
    }

    fun onDismissUpgradePaywall() {
        _state.update { it.copy(showUpgradePaywallDialog = false) }
    }

    companion object {
        fun provideFactory(
            getFamilyMembersUseCase: GetFamilyMembersUseCase,
            addFamilyMemberUseCase: AddFamilyMemberUseCase,
            removeFamilyMemberUseCase: RemoveFamilyMemberUseCase,
            requestChildExitUseCase: RequestChildExitUseCase,
            createFamilyGroupUseCase: CreateFamilyGroupUseCase,
            joinFamilyGroupUseCase: JoinFamilyGroupUseCase,
            getMemberSpendingSummaryUseCase: GetMemberSpendingSummaryUseCase,
            getMemberCategoryBreakdownUseCase: GetMemberCategoryBreakdownUseCase,
            getMemberExpensesUseCase: GetMemberExpensesUseCase,
            syncFamilyExpensesUseCase: SyncFamilyExpensesUseCase,
            familyRepository: FamilyRepository,
            authRepository: AuthRepository,
            expenseRepository: ExpenseRepository? = null,
            entitlementManager: EntitlementManager = EntitlementManager()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FamilyViewModel(
                    getFamilyMembersUseCase,
                    addFamilyMemberUseCase,
                    removeFamilyMemberUseCase,
                    requestChildExitUseCase,
                    createFamilyGroupUseCase,
                    joinFamilyGroupUseCase,
                    getMemberSpendingSummaryUseCase,
                    getMemberCategoryBreakdownUseCase,
                    getMemberExpensesUseCase,
                    syncFamilyExpensesUseCase,
                    familyRepository,
                    authRepository,
                    expenseRepository,
                    entitlementManager
                ) as T
            }
        }
    }
}
