package com.shakeexpense.app.ui.tracker

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.PredictionResult
import com.shakeexpense.app.domain.usecase.SafeToSpendSummary
import com.shakeexpense.app.domain.usecase.SpendingLimitState
import com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel
import com.shakeexpense.app.domain.usecase.SpendingTotals
import com.shakeexpense.app.ui.calendar.FinancialCalendarScreen
import com.shakeexpense.app.ui.design.*
import com.shakeexpense.app.ui.forecast.CashFlowForecastScreen
import com.shakeexpense.app.ui.notifications.InAppNotificationItem
import com.shakeexpense.app.ui.notifications.NotificationCategory
import com.shakeexpense.app.ui.notifications.NotificationsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackerMainScreen(
    viewModel: TrackerViewModel,
    onOpenQuickEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isDark = isAppDarkTheme()

    // Derive in-app notifications
    val inAppNotifications = remember(
        state.spendingLimitState,
        state.unusualAlerts,
        state.recurringPayments,
        state.readNotificationIds,
        state.dismissedNotificationIds
    ) {
        val list = mutableListOf<InAppNotificationItem>()
        val dismissed = state.dismissedNotificationIds
        val read = state.readNotificationIds

        // 1. Budget warnings
        state.spendingLimitState?.let { limit ->
            if (limit.warningLevel != SpendingLimitWarningLevel.NORMAL) {
                val id = "budget_${limit.warningLevel.name}"
                if (!dismissed.contains(id)) {
                    val remainingCents = (limit.activeMonthlyLimitCents - limit.currentMonthExpensesCents).coerceAtLeast(0L)
                    val excessCents = (limit.currentMonthExpensesCents - limit.activeMonthlyLimitCents).coerceAtLeast(0L)
                    val title = when (limit.warningLevel) {
                        SpendingLimitWarningLevel.EXCEEDED -> "Monthly Spending Limit Exceeded"
                        SpendingLimitWarningLevel.NINETY_PERCENT -> "90% Spending Limit Reached"
                        SpendingLimitWarningLevel.EIGHTY_PERCENT -> "80% Spending Limit Reached"
                        SpendingLimitWarningLevel.SEVENTY_PERCENT -> "70% Spending Limit Reached"
                        else -> "Spending Limit Alert"
                    }
                    val msg = when (limit.warningLevel) {
                        SpendingLimitWarningLevel.EXCEEDED -> "Monthly spending limit exceeded by ₹${FinancialFormatter.formatCents(excessCents)}."
                        else -> "You have used ${limit.usagePercentage.toInt()}% of your monthly limit. ₹${FinancialFormatter.formatCents(remainingCents)} remaining."
                    }
                    list.add(
                        InAppNotificationItem(
                            id = id,
                            title = title,
                            message = msg,
                            timestamp = System.currentTimeMillis(),
                            category = NotificationCategory.BUDGET,
                            isRead = read.contains(id),
                            isWarning = limit.warningLevel == SpendingLimitWarningLevel.EXCEEDED || limit.warningLevel == SpendingLimitWarningLevel.NINETY_PERCENT
                        )
                    )
                }
            }
        }

        // 2. Unusual spending alerts
        state.unusualAlerts.forEach { alert ->
            val id = "unusual_${alert.transactionUuid ?: alert.title.hashCode()}"
            if (!dismissed.contains(id)) {
                list.add(
                    InAppNotificationItem(
                        id = id,
                        title = alert.title,
                        message = alert.description,
                        timestamp = System.currentTimeMillis(),
                        category = NotificationCategory.UNUSUAL_SPENDING,
                        isRead = read.contains(id),
                        isWarning = false
                    )
                )
            }
        }

        // 3. Upcoming recurring bills (next 7 days)
        val now = System.currentTimeMillis()
        val sevenDays = 7L * 24 * 60 * 60 * 1000L
        state.recurringPayments.filter { it.isActive && it.nextDueTimestamp in now..(now + sevenDays) }.forEach { payment ->
            val id = "rec_${payment.id}_${payment.nextDueTimestamp}"
            if (!dismissed.contains(id)) {
                val dueStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(payment.nextDueTimestamp))
                list.add(
                    InAppNotificationItem(
                        id = id,
                        title = "Upcoming Bill: ${payment.name}",
                        message = "₹${FinancialFormatter.formatCents(payment.amountCents)} due on $dueStr (${payment.cadence.lowercase()}).",
                        timestamp = payment.nextDueTimestamp,
                        category = NotificationCategory.RECURRING,
                        isRead = read.contains(id),
                        isWarning = false
                    )
                )
            }
        }

        list
    }
    val unreadNotificationsCount = inAppNotifications.count { !it.isRead }

    // Handle Sub-Screens
    when (state.activeSubScreen) {
        TrackerSubScreen.NOTIFICATIONS -> {
            NotificationsScreen(
                notifications = inAppNotifications,
                onBackClick = { viewModel.onCloseSubScreen() },
                onMarkAllRead = { viewModel.onMarkAllNotificationsRead(inAppNotifications.map { it.id }) },
                onNotificationClick = { viewModel.onMarkNotificationRead(it.id) },
                onClearNotification = { viewModel.onDismissNotification(it) },
                modifier = modifier
            )
            return
        }
        TrackerSubScreen.FINANCIAL_CALENDAR -> {
            FinancialCalendarScreen(
                records = state.records,
                recurringPayments = state.recurringPayments,
                onBackClick = { viewModel.onCloseSubScreen() },
                onRecordClick = { viewModel.onSelectRecordForEdit(it) },
                modifier = modifier
            )
            return
        }
        TrackerSubScreen.CASH_FLOW_FORECAST -> {
            val currentBalance = state.totals.allTimeCreditCents - state.totals.allTimeDebitCents
            CashFlowForecastScreen(
                financialProfile = state.financialProfile,
                recurringPayments = state.recurringPayments,
                predictionResult = state.nextMonthPrediction,
                currentBalanceCents = currentBalance,
                onBackClick = { viewModel.onCloseSubScreen() },
                modifier = modifier
            )
            return
        }
        TrackerSubScreen.NONE -> { /* Fall through to main tracker view */ }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) ShakeDesignTokens.CanvasDark else ShakeDesignTokens.CanvasLight)
        ) {
            // App Bar / Top Header
            TrackerTopAppBar(
                syncStatus = state.syncDisplayStatus,
                pendingCount = state.pendingSyncCount,
                unreadNotificationCount = unreadNotificationsCount,
                onOpenNotifications = { viewModel.onOpenSubScreen(TrackerSubScreen.NOTIFICATIONS) },
                onOpenSearch = { viewModel.onOpenAdvancedSearch() },
                onExportCsv = { viewModel.onExportCsv(context) },
                onOpenAi = { viewModel.onOpenAiAssistant() }
            )

            // Search Bar (When Advanced Search is Active)
            if (state.isAdvancedSearchOpen) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    GlassTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = "Search payee, merchant, category...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = {
                            NeuIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close Search",
                                onClick = { viewModel.onCloseAdvancedSearch() },
                                size = 28.dp
                            )
                        }
                    )
                }
            }

            // View Switcher (Dashboard | Spreadsheet | Categories)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                NeuSegmentedControl(
                    items = listOf(
                        "Dashboard",
                        if (state.searchResults != null) "Search (${state.searchResults!!.size})" else "Spreadsheet",
                        "Categories"
                    ),
                    selectedIndex = when (state.selectedTab) {
                        TrackerTab.DASHBOARD -> 0
                        TrackerTab.SPREADSHEET -> 1
                        TrackerTab.CATEGORY_BREAKDOWN -> 2
                    },
                    onSelectIndex = { index ->
                        when (index) {
                            0 -> viewModel.onTabSelected(TrackerTab.DASHBOARD)
                            1 -> viewModel.onTabSelected(TrackerTab.SPREADSHEET)
                            2 -> viewModel.onTabSelected(TrackerTab.CATEGORY_BREAKDOWN)
                        }
                    }
                )
            }

            // Tab Content
            when (state.selectedTab) {
                TrackerTab.DASHBOARD -> {
                    TrackerDashboardView(
                        state = state,
                        onOpenBudgetSetup = { viewModel.onOpenBudgetSetupDialog() },
                        onOpenLimitEdit = { viewModel.onOpenSpendingLimitEditDialog() },
                        onOpenSpreadsheet = { viewModel.onTabSelected(TrackerTab.SPREADSHEET) },
                        onOpenCalendar = { viewModel.onOpenSubScreen(TrackerSubScreen.FINANCIAL_CALENDAR) },
                        onOpenForecast = { viewModel.onOpenSubScreen(TrackerSubScreen.CASH_FLOW_FORECAST) },
                        onRecordClick = { viewModel.onSelectRecordForEdit(it) }
                    )
                }
                TrackerTab.SPREADSHEET -> {
                    SpreadsheetDataGrid(
                        records = state.searchResults ?: state.records,
                        totals = state.totals,
                        onRecordClick = { viewModel.onSelectRecordForEdit(it) },
                        onDeleteRecord = { viewModel.onDeleteExpense(it.expenseUuid) }
                    )
                }
                TrackerTab.CATEGORY_BREAKDOWN -> {
                    CategoryBreakdownList(
                        breakdowns = state.categoryBreakdowns,
                        totalDebitCents = state.totals.allTimeDebitCents
                    )
                }
            }
        }

        // Floating Action Button (+) for Quick Entry
        QuickActionFAB(
            onClick = onOpenQuickEntry,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )

        // Edit / Delete Expense Dialog
        if (state.editingRecord != null) {
            EditExpenseDialog(
                record = state.editingRecord!!,
                categories = state.categories,
                onDismiss = { viewModel.onDismissEdit() },
                onSave = { uuid, amountCents, categoryId, type, customName ->
                    viewModel.onUpdateExpense(uuid, amountCents, categoryId, type, customName)
                },
                onDelete = { uuid ->
                    viewModel.onDeleteExpense(uuid)
                }
            )
        }

        // Budget & Safe-to-Spend Setup Dialog
        if (state.showBudgetSetupDialog) {
            BudgetSetupDialog(
                currentIncomeCents = state.safeToSpend?.monthlyIncomeCents ?: 0L,
                currentSavingsCents = state.safeToSpend?.savingsTargetCents ?: 0L,
                onDismiss = { viewModel.onDismissBudgetSetupDialog() },
                onSave = { incomeCents, savingsCents, cycleDay ->
                    viewModel.onSaveBudgetSetup(incomeCents, savingsCents, cycleDay)
                }
            )
        }

        // Monthly Spending Limit Edit Dialog
        if (state.showSpendingLimitEditDialog) {
            SpendingLimitEditDialog(
                currentLimitCents = state.spendingLimitState?.activeMonthlyLimitCents ?: 0L,
                systemRecommendedCents = state.spendingLimitState?.systemRecommendedLimitCents ?: 0L,
                onDismiss = { viewModel.onDismissSpendingLimitEditDialog() },
                onSave = { limitRupees -> viewModel.onSaveUserMonthlyLimit(limitRupees) }
            )
        }

        // Upgrade Paywall Dialog
        if (state.showUpgradePaywall) {
            UpgradePaywallDialog(
                featureTitle = state.paywallFeatureTitle,
                onDismiss = { viewModel.onDismissPaywall() }
            )
        }

        // AI Assistant Dialog
        if (state.isAiAssistantOpen) {
            AiAssistantDialog(
                response = state.aiAssistantResponse,
                onAsk = { question -> viewModel.onAskAiQuestion(question) },
                onDismiss = { viewModel.onCloseAiAssistant() }
            )
        }
    }
}

@Composable
fun TrackerTopAppBar(
    syncStatus: SyncDisplayStatus,
    pendingCount: Int,
    unreadNotificationCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenSearch: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onOpenAi: () -> Unit = {}
) {
    val isDark = isAppDarkTheme()
    val statusColor = when (syncStatus) {
        SyncDisplayStatus.SYNCED -> ShakeDesignTokens.HealthyGreen
        SyncDisplayStatus.NOT_SYNCED_PENDING -> ShakeDesignTokens.WarningAmber
        SyncDisplayStatus.NOT_SYNCED_OFFLINE -> ShakeDesignTokens.ExceededRed
    }

    val statusText = when (syncStatus) {
        SyncDisplayStatus.SYNCED -> "Synced"
        SyncDisplayStatus.NOT_SYNCED_PENDING -> "Pending ($pendingCount)"
        SyncDisplayStatus.NOT_SYNCED_OFFLINE -> "Offline"
    }

    Surface(
        color = if (isDark) ShakeDesignTokens.CanvasDark else ShakeDesignTokens.CanvasLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ShakeExpense",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NeuIconButton(
                    icon = Icons.Default.Search,
                    contentDescription = "Search Transactions",
                    onClick = onOpenSearch,
                    size = 36.dp
                )
                NeuIconButton(
                    icon = Icons.Default.FileDownload,
                    contentDescription = "Export CSV",
                    onClick = onExportCsv,
                    size = 36.dp
                )
                NeuIconButton(
                    icon = Icons.Default.AutoAwesome,
                    contentDescription = "AI Financial Assistant",
                    onClick = onOpenAi,
                    size = 36.dp,
                    tint = MaterialTheme.colorScheme.primary
                )
                Box {
                    NeuIconButton(
                        icon = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        onClick = onOpenNotifications,
                        size = 36.dp
                    )
                    if (unreadNotificationCount > 0) {
                        NeuNotificationBadge(
                            count = unreadNotificationCount,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackerDashboardView(
    state: TrackerState,
    onOpenBudgetSetup: () -> Unit,
    onOpenLimitEdit: () -> Unit,
    onOpenSpreadsheet: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenForecast: () -> Unit,
    onRecordClick: (ExpenseRecordItem) -> Unit
) {
    var isSafeToSpendCollapsed by rememberSaveable { mutableStateOf(false) }
    var isPacingCollapsed by rememberSaveable { mutableStateOf(true) } // Closed by default

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Safe to Spend Hero Card
        item {
            SafeToSpendHeroCard(
                safeTodayCents = state.safeToSpend?.safeToSpendTodayCents ?: 0L,
                remainingMonthCents = state.safeToSpend?.safeToSpendMonthlyCents ?: 0L,
                incomeCents = state.safeToSpend?.monthlyIncomeCents ?: 0L,
                spentThisMonthCents = state.totals.thisMonthDebitCents,
                daysRemainingInCycle = state.safeToSpend?.daysRemainingInCycle ?: 30,
                onConfigureClick = onOpenBudgetSetup,
                isCollapsed = isSafeToSpendCollapsed,
                onToggleCollapse = { isSafeToSpendCollapsed = !isSafeToSpendCollapsed }
            )
        }

        // 2. Spending Limit Pacing Card (if configured) - Closed by default
        val limitState = state.spendingLimitState
        if (limitState != null && limitState.activeMonthlyLimitCents > 0L) {
            item {
                SpendingLimitPacingCard(
                    limitState = limitState,
                    spentThisMonthCents = state.totals.thisMonthDebitCents,
                    onOpenLimitEdit = onOpenLimitEdit,
                    isCollapsed = isPacingCollapsed,
                    onToggleCollapse = { isPacingCollapsed = !isPacingCollapsed }
                )
            }
        }

        // 3. Quick Stats Grid (Today, This Month, All Time)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialMetricCard(
                    title = "Today",
                    amountCents = state.totals.todayDebitCents,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = "This Month",
                    amountCents = state.totals.thisMonthDebitCents,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = "All Time",
                    amountCents = state.totals.allTimeDebitCents,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Financial Hub Shortcuts (Financial Calendar & Cash Flow Forecast)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinancialMetricCard(
                    title = "Calendar",
                    amountCents = state.totals.thisMonthDebitCents,
                    subtitle = "View events & due dates",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onOpenCalendar,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = "Forecast",
                    amountCents = (state.totals.allTimeCreditCents - state.totals.allTimeDebitCents),
                    subtitle = "30-90 day runway analysis",
                    icon = Icons.Default.TrendingUp,
                    accentColor = ShakeDesignTokens.AccentCyan,
                    onClick = onOpenForecast,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onOpenSpreadsheet) {
                    Text(
                        text = "View All (${state.records.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 6. Recent Transactions Preview List
        val recentList = state.records.take(5)
        if (recentList.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No expenses recorded yet. Tap '+' or shake your phone to log your first transaction.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        } else {
            items(recentList.size) { index ->
                val record = recentList[index]
                GlassTransactionRow(
                    record = record,
                    onClick = { onRecordClick(record) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SpendingLimitPacingCard(
    limitState: SpendingLimitState,
    spentThisMonthCents: Long,
    onOpenLimitEdit: () -> Unit,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    val usagePercent = limitState.usagePercentage.toInt().coerceIn(0, 100)
    val remainingCents = (limitState.activeMonthlyLimitCents - spentThisMonthCents).coerceAtLeast(0L)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = 2.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleCollapse() }
                ) {
                    Text(
                        text = "MONTHLY LIMIT PACING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenLimitEdit, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Limit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onToggleCollapse, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Collapse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${FinancialFormatter.formatCents(spentThisMonthCents)} / ₹${FinancialFormatter.formatCents(limitState.activeMonthlyLimitCents)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FinancialFormatter.TabularFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$usagePercent% spent",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        usagePercent >= 90 -> ShakeDesignTokens.ExceededRed
                        usagePercent >= 75 -> ShakeDesignTokens.WarningAmber
                        else -> ShakeDesignTokens.HealthyGreen
                    }
                )
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { (limitState.usagePercentage.toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            usagePercent >= 90 -> ShakeDesignTokens.ExceededRed
                            usagePercent >= 75 -> ShakeDesignTokens.WarningAmber
                            else -> ShakeDesignTokens.HealthyGreen
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Remaining allowance: ₹${FinancialFormatter.formatCents(remainingCents)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SpreadsheetDataGrid(
    records: List<ExpenseRecordItem>,
    totals: SpendingTotals = SpendingTotals(),
    onRecordClick: ((ExpenseRecordItem) -> Unit)? = null,
    onDeleteRecord: ((ExpenseRecordItem) -> Unit)? = null
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No records found matching your filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(records, key = { _, item -> item.expenseUuid }) { _, record ->
            GlassTransactionRow(
                record = record,
                onClick = { onRecordClick?.invoke(record) }
            )
        }
    }
}

@Composable
fun CategoryBreakdownList(
    breakdowns: List<CategorySubtotal>,
    totalDebitCents: Long
) {
    if (breakdowns.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category data available yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(breakdowns) { _, item ->
            val percentage = if (totalDebitCents > 0) {
                ((item.totalCents.toDouble() / totalDebitCents.toDouble()) * 100).toInt()
            } else 0

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getCategoryVectorIcon(item.categoryName),
                                        contentDescription = item.categoryName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.categoryName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.count} transactions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "₹${FinancialFormatter.formatCents(item.totalCents)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FinancialFormatter.TabularFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { if (totalDebitCents > 0) (item.totalCents.toFloat() / totalDebitCents.toFloat()) else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$percentage%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditExpenseDialog(
    record: ExpenseRecordItem,
    categories: List<com.shakeexpense.app.data.database.entity.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (uuid: String, amountCents: Long, categoryId: Long, type: TransactionType, customName: String?) -> Unit,
    onDelete: (uuid: String) -> Unit
) {
    var amountText by remember { mutableStateOf((record.amountCents / 100.0).toString().removeSuffix(".0")) }
    var selectedCategoryId by remember { mutableLongStateOf(record.categoryId) }
    var transactionType by remember {
        mutableStateOf(
            if (record.transactionType.equals("CREDIT", ignoreCase = true)) TransactionType.CREDIT else TransactionType.DEBIT
        )
    }
    var customName by remember { mutableStateOf(record.customName ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this ₹${FinancialFormatter.formatCents(record.amountCents)} transaction? This cannot be undone.") },
            confirmButton = {
                NeuButton(
                    onClick = { onDelete(record.expenseUuid) },
                    containerColor = ShakeDesignTokens.ExceededRed,
                    contentColor = Color.White
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Selector
                NeuSegmentedControl(
                    items = listOf("DEBIT", "CREDIT"),
                    selectedIndex = if (transactionType == TransactionType.DEBIT) 0 else 1,
                    onSelectIndex = { index ->
                        transactionType = if (index == 0) TransactionType.DEBIT else TransactionType.CREDIT
                    }
                )

                // Amount Field
                GlassTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = "Amount (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Note Field
                GlassTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    placeholder = "Note / payee (Optional)"
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeuButton(
                    onClick = { showDeleteConfirm = true },
                    containerColor = ShakeDesignTokens.ExceededRed.copy(alpha = 0.12f),
                    contentColor = ShakeDesignTokens.ExceededRed
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }

                NeuButton(
                    onClick = {
                        val parsed = amountText.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            val amountCents = (parsed * 100).toLong()
                            onSave(
                                record.expenseUuid,
                                amountCents,
                                selectedCategoryId,
                                transactionType,
                                customName.ifBlank { null }
                            )
                        }
                    },
                    isPrimary = true
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BudgetSetupDialog(
    currentIncomeCents: Long,
    currentSavingsCents: Long,
    onDismiss: () -> Unit,
    onSave: (incomeCents: Long, savingsCents: Long, cycleDay: Int) -> Unit
) {
    var incomeText by remember {
        mutableStateOf(if (currentIncomeCents > 0) (currentIncomeCents / 100).toString() else "")
    }
    var savingsText by remember {
        mutableStateOf(if (currentSavingsCents > 0) (currentSavingsCents / 100).toString() else "")
    }
    var cycleDayText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Safe-to-Spend Configuration",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure your monthly income and savings targets to compute your safe daily spending runway.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GlassTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { char -> char.isDigit() } },
                    placeholder = "Monthly Income (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                GlassTextField(
                    value = savingsText,
                    onValueChange = { savingsText = it.filter { char -> char.isDigit() } },
                    placeholder = "Monthly Savings Target (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                GlassTextField(
                    value = cycleDayText,
                    onValueChange = { cycleDayText = it.filter { char -> char.isDigit() } },
                    placeholder = "Billing Cycle Start Day (1-28)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            NeuButton(
                onClick = {
                    val income = (incomeText.toLongOrNull() ?: 0L) * 100L
                    val savings = (savingsText.toLongOrNull() ?: 0L) * 100L
                    val cycleDay = (cycleDayText.toIntOrNull() ?: 1).coerceIn(1, 28)
                    onSave(income, savings, cycleDay)
                },
                isPrimary = true
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SpendingLimitEditDialog(
    currentLimitCents: Long,
    systemRecommendedCents: Long,
    onDismiss: () -> Unit,
    onSave: (Long?) -> Unit
) {
    var limitText by remember { mutableStateOf(if (currentLimitCents > 0L) (currentLimitCents / 100L).toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Spending Limit", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set an active monthly spending limit to receive timely warning notifications (70%, 80%, 90%, 100%) and protect against overspending.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (systemRecommendedCents > 0L) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                limitText = (systemRecommendedCents / 100L).toString()
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "System Recommended", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "₹${FinancialFormatter.formatCents(systemRecommendedCents)}/mo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = "Use Recommendation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                GlassTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { char -> char.isDigit() } },
                    placeholder = "Monthly Limit (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            NeuButton(
                onClick = {
                    val amount = limitText.toLongOrNull()
                    onSave(amount)
                },
                isPrimary = true
            ) {
                Text("Set Active Limit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpgradePaywallDialog(
    featureTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Locked Feature", tint = ShakeDesignTokens.WarningAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Premium Feature", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Unlock $featureTitle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "This feature is available on PLUS (₹59/mo) and FAMILY PRO (₹99/mo) plans. Upgrade from Profile to unlock AI analysis, predictions, and automated cash flow forecasting.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            NeuButton(
                onClick = onDismiss,
                isPrimary = true
            ) {
                Text("Got It")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AiAssistantDialog(
    response: com.shakeexpense.app.domain.usecase.AiAssistantResponse?,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    val suggestedQuestions = listOf(
        "Where did most of my money go this month?",
        "Can I afford ₹5,000 this week?",
        "How much can I save if I reduce dining out?",
        "What are my highest expense categories?",
        "Why did I spend more this month?"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "AI Financial Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Real-time personal intelligence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Suggested Questions:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestedQuestions.forEach { prompt ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAsk(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                GlassTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = "Ask your own financial question..."
                )

                NeuButton(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            onAsk(queryText)
                            queryText = ""
                        }
                    },
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ask AI", fontSize = 13.sp)
                }

                if (response != null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Financial Analysis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = response.answer,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (response.highlights.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                response.highlights.forEach { hl ->
                                    Text("• $hl", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
