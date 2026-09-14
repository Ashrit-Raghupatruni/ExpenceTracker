package com.shakeexpense.app.ui.tracker

import com.shakeexpense.app.domain.usecase.SafeToSpendSummary
import com.shakeexpense.app.domain.usecase.SpendingLimitState
import com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel
import com.shakeexpense.app.domain.usecase.PredictionResult
import com.shakeexpense.app.domain.usecase.PredictionConfidence
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.usecase.UnusualSpendingAlert
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.SpendingTotals
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // App Bar / Top Header
            TrackerTopAppBar(
                syncStatus = state.syncDisplayStatus,
                pendingCount = state.pendingSyncCount,
                onOpenSearch = { viewModel.onOpenAdvancedSearch() },
                onExportCsv = { viewModel.onExportCsv(context) },
                onOpenAi = { viewModel.onOpenAiAssistant() }
            )

            // Search Bar (When Advanced Search is Active)
            if (state.isAdvancedSearchOpen) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search payee, merchant, category, amount...", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.onCloseAdvancedSearch() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                }
            }

            // Unusual Spending Alerts Banner
            if (state.unusualAlerts.isNotEmpty()) {
                UnusualSpendingAlertBanner(alerts = state.unusualAlerts)
            }

            // Spending Limit Alerts Banner (70%, 80%, 90%, 100%)
            val limitState = state.spendingLimitState
            if (limitState != null && limitState.warningLevel != com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.NORMAL) {
                PersonalSpendingLimitAlertBanner(limitState = limitState)
            }

            // Consolidated Totals Header
            ConsolidatedTotalsHeader(
                totals = state.totals,
                safeToSpend = state.safeToSpend,
                spendingLimitState = state.spendingLimitState,
                nextMonthPrediction = state.nextMonthPrediction,
                onOpenBudgetSetup = { viewModel.onOpenBudgetSetupDialog() },
                onOpenLimitEdit = { viewModel.onOpenSpendingLimitEditDialog() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // View Switcher Tabs (All Entries | Grouped by Category)
            TabRow(
                selectedTabIndex = if (state.selectedTab == TrackerTab.SPREADSHEET) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF2563EB)
            ) {
                Tab(
                    selected = state.selectedTab == TrackerTab.SPREADSHEET,
                    onClick = { viewModel.onTabSelected(TrackerTab.SPREADSHEET) },
                    text = {
                        Text(
                            text = if (state.searchResults != null) "Search Results (${state.searchResults!!.size})" else "Spreadsheet View",
                            fontWeight = if (state.selectedTab == TrackerTab.SPREADSHEET) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = state.selectedTab == TrackerTab.CATEGORY_BREAKDOWN,
                    onClick = { viewModel.onTabSelected(TrackerTab.CATEGORY_BREAKDOWN) },
                    text = {
                        Text(
                            text = "Grouped by Category",
                            fontWeight = if (state.selectedTab == TrackerTab.CATEGORY_BREAKDOWN) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Tab Content
            when (state.selectedTab) {
                TrackerTab.SPREADSHEET -> {
                    SpreadsheetDataGrid(
                        records = state.searchResults ?: state.records,
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
        FloatingActionButton(
            onClick = onOpenQuickEntry,
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }

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
    onOpenSearch: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onOpenAi: () -> Unit = {}
) {
    val statusColor = when (syncStatus) {
        SyncDisplayStatus.SYNCED -> Color(0xFF10B981)
        SyncDisplayStatus.NOT_SYNCED_PENDING -> Color(0xFFF59E0B)
        SyncDisplayStatus.NOT_SYNCED_OFFLINE -> Color(0xFFEF4444)
    }

    val statusText = when (syncStatus) {
        SyncDisplayStatus.SYNCED -> "Synced ●"
        SyncDisplayStatus.NOT_SYNCED_PENDING -> "Pending ($pendingCount)"
        SyncDisplayStatus.NOT_SYNCED_OFFLINE -> "Offline"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ShakeExpense",
                    fontSize = 18.sp,
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search Transactions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onExportCsv) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onOpenAi) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Financial Assistant", tint = Color(0xFF2563EB))
                }
            }
        }
    }
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
                Icon(Icons.Default.Lock, contentDescription = "Locked Feature", tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "PLUS Feature", fontWeight = FontWeight.Bold)
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
                    text = "This feature is exclusively available on PLUS (₹59/mo) and FAMILY PRO (₹99/mo) plans. Upgrade now from Profile to unlock AI insights, predictive spending limits, advanced search, and full financial protection.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
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
        "How much can I save if I reduce food delivery?",
        "What are my unnecessary expenses?",
        "Why did I spend more this month?"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = Color(0xFF2563EB))
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAsk(prompt) }
                        ) {
                            Text(
                                text = "💬 $prompt",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Ask your own financial question...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            onAsk(queryText)
                            queryText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Ask AI", fontSize = 12.sp)
                }

                if (response != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "💡 AI Financial Analysis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
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

@Composable
fun UnusualSpendingAlertBanner(
    alerts: List<UnusualSpendingAlert>
) {
    if (alerts.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alerts.forEach { alert ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = alert.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = alert.description,
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalSpendingLimitAlertBanner(
    limitState: SpendingLimitState
) {
    val level = limitState.warningLevel
    if (level == com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.NORMAL) return

    val remainingCents = (limitState.activeMonthlyLimitCents - limitState.currentMonthExpensesCents).coerceAtLeast(0L)
    val excessCents = (limitState.currentMonthExpensesCents - limitState.activeMonthlyLimitCents).coerceAtLeast(0L)

    val (bgColor, borderColor, iconColor, textColor, message) = when (level) {
        com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.EXCEEDED -> Quintuple(
            Color(0xFFFEF2F2),
            Color(0xFFDC2626),
            Color(0xFFDC2626),
            Color(0xFF991B1B),
            "🚨 Monthly spending limit exceeded by ₹${formatCurrency(excessCents)}! (Used ${String.format(Locale.US, "%.0f", limitState.usagePercentage)}%)"
        )
        com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.NINETY_PERCENT -> Quintuple(
            Color(0xFFFFF7ED),
            Color(0xFFEA580C),
            Color(0xFFEA580C),
            Color(0xFF9A3412),
            "🚨 Warning: 90% of your monthly spending limit reached! Only ₹${formatCurrency(remainingCents)} left."
        )
        com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.EIGHTY_PERCENT -> Quintuple(
            Color(0xFFFFFBEB),
            Color(0xFFD97706),
            Color(0xFFD97706),
            Color(0xFF92400E),
            "⚠️ Caution: 80% of your monthly spending limit reached. ₹${formatCurrency(remainingCents)} remaining."
        )
        com.shakeexpense.app.domain.usecase.SpendingLimitWarningLevel.SEVENTY_PERCENT -> Quintuple(
            Color(0xFFFFFBEB),
            Color(0xFFD97706),
            Color(0xFFD97706),
            Color(0xFF92400E),
            "⚠️ Notice: 70% of your monthly spending limit reached. ₹${formatCurrency(remainingCents)} remaining."
        )
        else -> return
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Alert", tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
fun ConsolidatedTotalsHeader(
    totals: SpendingTotals,
    safeToSpend: SafeToSpendSummary? = null,
    spendingLimitState: SpendingLimitState? = null,
    nextMonthPrediction: PredictionResult? = null,
    onOpenBudgetSetup: () -> Unit = {},
    onOpenLimitEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B) // Rich Slate Dark
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL EXPENSES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "⚙️ Budget",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onOpenBudgetSetup() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Text(
                text = "₹ ${formatCurrency(totals.allTimeDebitCents)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TotalMetricItem(label = "Today", amountCents = totals.todayDebitCents)
                TotalMetricItem(label = "This Month", amountCents = totals.thisMonthDebitCents)
                TotalMetricItem(label = "All-Time", amountCents = totals.allTimeDebitCents)
            }

            // Safe to Spend Signature Feature Card
            if (safeToSpend != null) {
                var isSafeToSpendExpanded by rememberSaveable { mutableStateOf(true) }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F766E).copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Header Row with Title, summary (if collapsed), and Arrow Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSafeToSpendExpanded = !isSafeToSpendExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🛡️ SAFE TO SPEND TODAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5EEAD4),
                                letterSpacing = 0.5.sp
                            )
                            if (!isSafeToSpendExpanded) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹${formatCurrency(safeToSpend.safeToSpendTodayCents)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFCCFBF1)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSafeToSpendExpanded) {
                                Text(
                                    text = "Setup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5EEAD4),
                                    modifier = Modifier
                                        .clickable { onOpenBudgetSetup() }
                                        .padding(end = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { isSafeToSpendExpanded = !isSafeToSpendExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSafeToSpendExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isSafeToSpendExpanded) "Hide Safe to Spend" else "Show Safe to Spend",
                                    tint = Color(0xFF5EEAD4),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Expandable Details
                    AnimatedVisibility(
                        visible = isSafeToSpendExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "₹ ${formatCurrency(safeToSpend.safeToSpendTodayCents)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCCFBF1)
                            )
                            Text(
                                text = if (safeToSpend.monthlyIncomeCents > 0) {
                                    "₹${formatCurrency(safeToSpend.safeToSpendMonthlyCents)} safe remaining · ${safeToSpend.daysRemainingInCycle} days left"
                                } else {
                                    "Tap to configure monthly income & savings goal"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF99F6E4)
                            )
                        }
                    }
                }
            }

            // Monthly Limit Pacing Bar
            if (spendingLimitState != null && spendingLimitState.activeMonthlyLimitCents > 0L) {
                var isMonthlyLimitExpanded by rememberSaveable { mutableStateOf(true) }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Header Row with Title, Status badge, and Arrow Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMonthlyLimitExpanded = !isMonthlyLimitExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 MONTHLY LIMIT PACING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            letterSpacing = 0.5.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${spendingLimitState.usagePercentage.toInt()}% used",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (spendingLimitState.warningLevel) {
                                    SpendingLimitWarningLevel.EXCEEDED -> Color(0xFFEF4444)
                                    SpendingLimitWarningLevel.NINETY_PERCENT -> Color(0xFFF97316)
                                    SpendingLimitWarningLevel.EIGHTY_PERCENT, SpendingLimitWarningLevel.SEVENTY_PERCENT -> Color(0xFFFBBF24)
                                    SpendingLimitWarningLevel.NORMAL -> Color(0xFF34D399)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (isMonthlyLimitExpanded) {
                                Text(
                                    text = "Edit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF93C5FD),
                                    modifier = Modifier
                                        .clickable { onOpenLimitEdit() }
                                        .padding(end = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { isMonthlyLimitExpanded = !isMonthlyLimitExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMonthlyLimitExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isMonthlyLimitExpanded) "Hide Monthly Limit Pacing" else "Show Monthly Limit Pacing",
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Expandable Details
                    AnimatedVisibility(
                        visible = isMonthlyLimitExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            LinearProgressIndicator(
                                progress = { (spendingLimitState.usagePercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = when (spendingLimitState.warningLevel) {
                                    SpendingLimitWarningLevel.EXCEEDED -> Color(0xFFEF4444)
                                    SpendingLimitWarningLevel.NINETY_PERCENT -> Color(0xFFF97316)
                                    SpendingLimitWarningLevel.EIGHTY_PERCENT, SpendingLimitWarningLevel.SEVENTY_PERCENT -> Color(0xFFFBBF24)
                                    SpendingLimitWarningLevel.NORMAL -> Color(0xFF34D399)
                                },
                                trackColor = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${formatCurrency(spendingLimitState.currentMonthExpensesCents)} spent of ₹${formatCurrency(spendingLimitState.activeMonthlyLimitCents)} limit",
                                fontSize = 10.sp,
                                color = Color(0xFFBFDBFE)
                            )
                        }
                    }
                }
            }

            // Next-Month Prediction Card
            when (nextMonthPrediction) {
                is PredictionResult.Success -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF312E81).copy(alpha = 0.4f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔮", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next Month: ₹${formatCurrency(nextMonthPrediction.predictedNextMonthCents)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4F46E5).copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = when (nextMonthPrediction.confidence) {
                                    PredictionConfidence.HIGH -> "High Conf"
                                    PredictionConfidence.MODERATE -> "Moderate Conf"
                                    PredictionConfidence.LOW -> "Low Conf"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0E7FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                is PredictionResult.InsufficientData -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ℹ️ ${nextMonthPrediction.message}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                null -> {}
            }
        }
    }
}

@Composable
fun TotalMetricItem(label: String, amountCents: Long) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "₹ ${formatCurrency(amountCents)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF8FAFC)
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetDataGrid(
    records: List<ExpenseRecordItem>,
    onRecordClick: ((ExpenseRecordItem) -> Unit)? = null,
    onDeleteRecord: ((ExpenseRecordItem) -> Unit)? = null
) {
    var recordPendingDelete by remember { mutableStateOf<ExpenseRecordItem?>(null) }

    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No expenses logged yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Shake your phone or tap (+) to log an expense",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Density Spreadsheet Table Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATE / TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.6f)
                )
                Text(
                    text = "TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = "AMOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = DividerDefaults.color)

        // Spreadsheet Rows with Swipe to Delete
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(records, key = { _, item -> item.expenseUuid }) { index, record ->
                val isEven = index % 2 == 0
                val rowBackground = if (isEven) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }

                val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissVal ->
                        if (dismissVal == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart ||
                            dismissVal == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
                        ) {
                            recordPendingDelete = record
                        }
                        false // Do not immediately remove without dialog confirmation
                    }
                )

                androidx.compose.material3.SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val alignment = if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd) {
                            Alignment.CenterStart
                        } else {
                            Alignment.CenterEnd
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFDC2626))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delete",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = rowBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = onRecordClick != null) {
                                    onRecordClick?.invoke(record)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date / Time Column
                            Text(
                                text = formatDate(record.timestamp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.3f)
                            )

                            // Category Column (with color dot & custom label/note)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1.6f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(parseColorHex(record.categoryColor))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = record.displayCategory,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val noteText = record.customName?.trim()
                                    if (!noteText.isNullOrBlank() && !noteText.equals(record.displayCategory, ignoreCase = true)) {
                                        Text(
                                            text = noteText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Type Column (DEBIT / CREDIT Badge)
                            val isDebit = record.transactionType.equals("DEBIT", ignoreCase = true)
                            Box(
                                modifier = Modifier.weight(1.0f),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isDebit) Color(0xFFFEE2E2) else Color(0xFFD1FAE5),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = if (isDebit) "DEBIT" else "CREDIT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDebit) Color(0xFFDC2626) else Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Amount Column
                            Text(
                                text = "₹ ${formatCurrency(record.amountCents)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.End,
                                color = if (isDebit) Color(0xFFDC2626) else Color(0xFF059669),
                                modifier = Modifier.weight(1.3f)
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = DividerDefaults.color.copy(alpha = 0.5f))
            }
        }
    }

    if (recordPendingDelete != null) {
        val record = recordPendingDelete!!
        AlertDialog(
            onDismissRequest = { recordPendingDelete = null },
            title = { Text("Delete Expense") },
            text = {
                Text("Are you sure you want to delete this ${record.displayCategory} expense of ₹${formatCurrency(record.amountCents)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecord?.invoke(record)
                        recordPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
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

            val categoryColor = parseColorHex(item.colorHex)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.categoryName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${item.count} txns)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "₹ ${formatCurrency(item.totalCents)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                            color = categoryColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "$percentage%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this ₹${formatCurrency(record.amountCents)} transaction? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(record.expenseUuid) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
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
        title = { Text("Edit Expense") },
        text = {
            Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionType == TransactionType.DEBIT,
                        onClick = { transactionType = TransactionType.DEBIT },
                        label = { Text("DEBIT") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == TransactionType.CREDIT,
                        onClick = { transactionType = TransactionType.CREDIT },
                        label = { Text("CREDIT") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val displayCats = if (categories.isNotEmpty()) categories else listOf(
                        com.shakeexpense.app.data.database.entity.CategoryEntity(1, "Food", "#F59E0B"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(2, "Transport", "#3B82F6"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(3, "Groceries", "#10B981"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(4, "Bills", "#8B5CF6"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(5, "Shopping", "#EC4899"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(6, "Entertainment", "#A855F7"),
                        com.shakeexpense.app.data.database.entity.CategoryEntity(7, "Others", "#6B7280")
                    )

                    displayCats.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note / Custom Name
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Note / Merchant (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }

                Button(
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatCurrency(amountCents: Long): String {
    val whole = amountCents / 100
    val fraction = amountCents % 100
    return if (fraction == 0L) {
        "%,d".format(Locale.getDefault(), whole)
    } else {
        "%,d.%02d".format(Locale.getDefault(), whole, fraction)
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun parseColorHex(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF6B7280)
    return try {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (e: Exception) {
        Color(0xFF6B7280)
    }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚙️ Safe-to-Spend Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your expected monthly numbers. We calculate your safe daily spending dynamically to keep you on budget.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { char -> char.isDigit() } },
                    label = { Text("Monthly Inflow / Income (₹)") },
                    placeholder = { Text("e.g. 50000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = savingsText,
                    onValueChange = { savingsText = it.filter { char -> char.isDigit() } },
                    label = { Text("Monthly Savings Target (₹)") },
                    placeholder = { Text("e.g. 10000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cycleDayText,
                    onValueChange = { cycleDayText = it.filter { char -> char.isDigit() } },
                    label = { Text("Billing Cycle Start Day (1-28)") },
                    placeholder = { Text("1") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val income = (incomeText.toLongOrNull() ?: 0L) * 100L
                    val savings = (savingsText.toLongOrNull() ?: 0L) * 100L
                    val cycleDay = (cycleDayText.toIntOrNull() ?: 1).coerceIn(1, 28)
                    onSave(income, savings, cycleDay)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
            ) {
                Text("Save & Calculate")
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
                    text = "Set an active monthly spending limit to receive timely warning notifications (70%, 80%, 90%, 100%) and protect against unexpected expenses.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (systemRecommendedCents > 0L) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                limitText = (systemRecommendedCents / 100L).toString()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "System Recommended", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "₹${formatCurrency(systemRecommendedCents)}/month",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = "Use >", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { char -> char.isDigit() } },
                    label = { Text("Monthly Limit (₹)") },
                    placeholder = { Text("e.g. 20000") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = limitText.toLongOrNull()
                    onSave(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
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
