package com.shakeexpense.app.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberDatePickerState
import com.shakeexpense.app.domain.usecase.ResetPeriod
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shakeexpense.app.domain.model.MonthlyExpenseSummary
import com.shakeexpense.app.ui.theme.AppThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLaunchGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                hasNotificationPermission = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = { Text("Sign Out of ShakeExpense?") },
            text = { Text("Are you sure you want to sign out? Your offline expenses will remain safely stored on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Period Selection Dialog
    if (state.showResetPeriodDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissResetDialogs() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Reset Period")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose which period of your expense records you want to reset. Google account, family, and categories will remain untouched.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Today
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onSelectResetPeriod(ResetPeriod.TODAY) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Today", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Delete all expenses logged today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 2: This Week
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onSelectResetPeriod(ResetPeriod.THIS_WEEK) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("This Week", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Delete all expenses from Monday to today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 3: Specific Date
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onSelectResetPeriod(ResetPeriod.SPECIFIC_DATE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Specific Date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Pick a specific date to delete its expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissResetDialogs() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Material 3 DatePickerDialog for Specific Date
    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDismissResetDialogs() },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            viewModel.onSelectResetPeriod(ResetPeriod.SPECIFIC_DATE, selectedMillis)
                        } else {
                            viewModel.onDismissResetDialogs()
                        }
                    }
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onDismissResetDialogs() }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Final Confirmation Dialog
    if (state.showResetConfirmDialog && state.resetCandidate != null) {
        val candidate = state.resetCandidate!!
        AlertDialog(
            onDismissRequest = {
                if (!state.isResetting) viewModel.onDismissResetDialogs()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Expense Reset")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Are you sure you want to permanently delete your expenses for:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Period:", fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                                Text(candidate.periodLabel, fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expenses to Delete:", fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                                Text("${candidate.expenseCount} items", fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                                Text("₹${formatResetCurrency(candidate.totalAmountCents)}", fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    if (candidate.expenseCount == 0) {
                        Text(
                            text = "Note: There are 0 expenses found in this period. Nothing will be deleted.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "This action will permanently delete these ${candidate.expenseCount} records from your device and sync deletion to Firebase. Other family members' records and your settings are unaffected.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmReset() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = !state.isResetting
                ) {
                    if (state.isResetting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resetting...", color = Color.White)
                    } else {
                        Text("Reset Expenses", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onDismissResetDialogs() },
                    enabled = !state.isResetting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showSubscriptionModal) {
        SubscriptionPlansDialog(
            currentPlan = state.subscriptionPlan,
            onSelectPlan = { plan -> viewModel.selectPlan(plan) },
            onDismiss = { viewModel.onShowSubscriptionModal(false) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Screen Title & Sync Status Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "User Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Account, History & Customization",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sync status chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (!state.isOnline) Color(0xFFEF4444).copy(alpha = 0.15f)
                    else if (state.pendingSyncCount > 0) Color(0xFFF59E0B).copy(alpha = 0.15f)
                    else Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (!state.isOnline || state.pendingSyncCount > 0) Icons.Default.SyncProblem else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (!state.isOnline) Color(0xFFEF4444)
                            else if (state.pendingSyncCount > 0) Color(0xFFF59E0B)
                            else Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!state.isOnline) "Offline"
                            else if (state.pendingSyncCount > 0) "${state.pendingSyncCount} pending"
                            else "Synced",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!state.isOnline) Color(0xFFEF4444)
                            else if (state.pendingSyncCount > 0) Color(0xFFF59E0B)
                            else Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // 2. Google Account Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        if (!state.userProfile.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = state.userProfile.photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.userProfile.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!state.userProfile.email.isNullOrBlank()) {
                                Text(
                                    text = state.userProfile.email ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "User ID: ${state.userProfile.userId}",
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (state.userProfile.isFamilyLinked) Color(0xFF3B82F6).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = state.userProfile.familyStatusDisplay,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.userProfile.isFamilyLinked) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Sign In / Sign Out Action
                    if (state.userProfile.isAnonymous || state.userProfile.email.isNullOrBlank()) {
                        Button(
                            onClick = onLaunchGoogleSignIn,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !state.isSigningIn
                        ) {
                            if (state.isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting Google Account...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign in with Google", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showSignOutConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    }

                    if (state.authErrorMessage != null) {
                        Text(
                            text = state.authErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 3. Permissions & Setup Card
        item {
            PermissionsSetupCard(
                hasOverlayPermission = hasOverlayPermission,
                hasNotificationPermission = hasNotificationPermission,
                onOpenOverlaySettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                },
                onOpenNotificationSettings = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }

        // 4. Theme Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "System",
                            icon = Icons.Default.Palette,
                            selected = state.themeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Light",
                            icon = Icons.Default.LightMode,
                            selected = state.themeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Dark",
                            icon = Icons.Default.DarkMode,
                            selected = state.themeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3.4 Subscription Tier Card
        item {
            SubscriptionTierCard(
                currentPlan = state.subscriptionPlan,
                onOpenPlans = { viewModel.onShowSubscriptionModal(true) }
            )
        }

        // 3.5 Financial Safety Score Card (USP)
        item {
            FinancialSafetyCard(
                score = state.financialSafetyScore,
                primaryOpportunity = state.primaryOpportunity,
                spendingSubScore = state.safetySpendingSubScore,
                budgetSubScore = state.safetyBudgetSubScore,
                recurringSubScore = state.safetyRecurringSubScore,
                riskSignalsSubScore = state.safetyRiskSignalsSubScore
            )
        }

        // 3.6 Subscriptions & Recurring Payments Card
        item {
            SubscriptionsSummaryCard(
                subscriptions = state.recurringPayments,
                annualBurdenCents = state.recurringAnnualBurdenCents,
                unusedWarnings = state.recurringUnusedWarnings
            )
        }

        // 4. Data Management & Reset Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reset Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Clear your expense records for Today, This Week, or a Specific Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (state.resetSuccessMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state.resetSuccessMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearResetSuccessMessage() }) {
                                    Text("Dismiss", fontSize = 11.sp, color = Color(0xFF065F46))
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.onOpenResetExpensesDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset Expenses...",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 5. Monthly History Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monthly History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "${state.monthlyHistory.size} months",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Monthly History Cards
        if (state.monthlyHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "No recorded expense history found.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.monthlyHistory, key = { it.yearMonth }) { monthSummary ->
                MonthlyHistoryCard(
                    summary = monthSummary,
                    isExpanded = state.expandedMonthKey == monthSummary.yearMonth,
                    onToggleExpand = { viewModel.toggleExpandMonth(monthSummary.yearMonth) }
                )
            }
        }

        // 6. Help & Documentation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Help & Documentation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Everything you need to know about tracking, gestures, permissions, and family sync.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    GuideAccordionItem(
                        title = "💡 1. What is ShakeExpense?",
                        content = "ShakeExpense is an ultra-fast daily expense tracker with an instant shake-to-open translucent overlay HUD, spreadsheet organization, smart on-device bank notification detection, and real-time family expense synchronization."
                    )

                    GuideAccordionItem(
                        title = "➕ 2. How to Add Expenses",
                        content = "You can add expenses in two ways:\n• Shake Gesture: Shake your phone from anywhere to bring up the Quick Entry keypad.\n• Manual Add: Tap the '+' floating button on the Tracker tab to select a category and amount."
                    )

                    GuideAccordionItem(
                        title = "📳 3. How to Shake to Open Expense Entry",
                        content = "Gently shake your phone twice. ShakeExpense detects the physical gesture and immediately displays the translucent Quick Entry overlay above your current app or Android Home Screen."
                    )

                    GuideAccordionItem(
                        title = "🏷️ 4. Categories & Custom Tags",
                        content = "Choose from 7 standard categories (Food, Transport, Groceries, Bills, Shopping, Entertainment, Others). Selecting 'Others' lets you enter custom category names for personalized tracking."
                    )

                    GuideAccordionItem(
                        title = "🏦 5. Bank Notification Detection",
                        content = "When enabled, ShakeExpense automatically detects transaction SMS and notifications from supported banks and payment apps (UPI, GPay, Paytm, PhonePe, HDFC, SBI, ICICI, etc.). A prompt lets you confirm and categorize the expense in 1 tap."
                    )

                    GuideAccordionItem(
                        title = "👨‍👩‍👧‍👦 6. Family & Parental Sync",
                        content = "Parents can create a Family Group to generate an official 6-character Invite Code and QR Code. Family members join using the code, synchronizing child spending and category totals in real-time."
                    )

                    GuideAccordionItem(
                        title = "🔄 7. Offline / Online Synchronization",
                        content = "ShakeExpense is 100% offline-first. All transactions save instantly to your local Room database. When internet connection is restored, pending entries sync automatically to the cloud."
                    )

                    GuideAccordionItem(
                        title = "👤 8. Google Account & Data Persistence",
                        content = "All personal expenses are permanently linked to your authenticated Google User ID (Firebase UID). Logging out or switching devices safely preserves your transaction history, which restores automatically upon signing back in."
                    )

                    GuideAccordionItem(
                        title = "🛡️ 9. Required Permissions & Setup",
                        content = "• Shake Anywhere: Requires 'Display over other apps' (Overlay) to show the Quick Entry HUD from the Home Screen.\n• Bank Notification Detection: Requires 'Notification Access' to recognize bank transaction alerts on-device."
                    )

                    GuideAccordionItem(
                        title = "🛠️ 10. Troubleshooting & FAQ",
                        content = "• Shake not triggering? Ensure 'Shake Anywhere' overlay permission is granted and battery optimization is disabled for ShakeExpense (especially on OnePlus, Xiaomi, and Samsung devices).\n• Cloud sync pending? Check your internet connection; WorkManager will automatically retry in the background."
                    )

                    GuideAccordionItem(
                        title = "💎 11. Subscription Plans & Tiers",
                        content = "• FREE (₹0): Shake HUD, manual logging, on-device bank detection, basic spreadsheet, and 6-month history.\n• PLUS (₹59/mo or ₹699/yr): Safe-to-Spend, Financial Safety Score, AI Assistant, Prediction, 70-100% Alerts, CSV Export, and Recurring Detector.\n• FAMILY PRO (₹99/mo or ₹999/yr): All PLUS features for up to 5 family members + Shared Family Budgets, Granular Privacy, Family Limits, and Child Exit Governance.\n• Upgrade by selecting your tier and paying via UPI or Google Play."
                    )

                    GuideAccordionItem(
                        title = "🛡️ 12. Safe-to-Spend Daily Runway",
                        content = "Configure your monthly income and savings target in Tracker -> Safe-to-Spend Setup. The calculator factors in your upcoming recurring bills and shows you exactly how much money is safe to spend today without overrunning your monthly plan. If income is not configured, it safely displays ₹0."
                    )

                    GuideAccordionItem(
                        title = "📊 13. Dynamic Financial Safety Score",
                        content = "A real-time 0–100 score analyzing 4 authentic factors:\n• Spending: Discretionary purchases vs essential needs.\n• Budget: Monthly debit total vs planned budget.\n• Recurring: Fixed recurring obligations ratio.\n• Risk Signals: Over-budget warnings and sudden spending spikes.\nZero fake data: If no records exist, the score displays a clean empty state."
                    )

                    GuideAccordionItem(
                        title = "🤖 14. AI Financial Assistant & Predictions",
                        content = "PLUS and FAMILY PRO members can tap the AI Assistant on Tracker to ask natural language questions like 'Can I afford 2000 this weekend?' or 'How much did I spend on Food?'. Next-month predictions use statistical moving averages and regression based on your authentic spending habits."
                    )

                    GuideAccordionItem(
                        title = "👨‍👩‍👧‍👦 15. Shared Family Budgets (Family Pro)",
                        content = "Set collective household limits by category in the Family tab. Tap '+ Add Budget', pick a category (like Food or Groceries), and set a family-wide monthly cap. All members' transactions in that category aggregate in real-time with an interactive progress bar."
                    )

                    GuideAccordionItem(
                        title = "🚪 16. Child Exit Governance & Approval",
                        content = "Child members cannot detach from a family unilaterally. When a child taps 'Request Exit', an amber 'Exit Requested' badge is shown on their card. Parents receive the request and can choose to either approve detachment or dismiss the request."
                    )

                    GuideAccordionItem(
                        title = "🔒 17. Granular Family Privacy Controls",
                        content = "In Family Hub, tap the Shield icon next to any member to customize their privacy:\n• Share Transactions: Hide individual line items while sharing monthly totals.\n• Share Monthly Total: Hide monthly spending aggregate.\n• Share Category Totals: Hide category distributions.\n• Receive Alerts: Toggle family budget push notifications."
                    )

                    GuideAccordionItem(
                        title = "🔔 18. Spending Limit Alerts & Anti-Spam",
                        content = "Get notified at 70%, 80%, 90%, and 100% of your personal or family spending limit. The smart anti-spam engine guarantees you only receive one alert per threshold each calendar month, preventing notification floods."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOptionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@Composable
private fun MonthlyHistoryCard(
    summary: MonthlyExpenseSummary,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = summary.displayMonth,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = summary.totalDebitFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        if (summary.totalCreditCents > 0) {
                            Text(
                                text = "Credit: ${summary.totalCreditFormatted}",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Distribution Pills
            if (summary.categoryBreakdown.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    summary.categoryBreakdown.take(3).forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${cat.categoryName}: ${cat.totalFormatted}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Expandable Transaction List grouped by Date
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Daily Breakdown for ${summary.displayMonth}:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var expandedDates by remember {
                        mutableStateOf(emptySet<String>())
                    }

                    summary.dailyGroups.forEach { dayGroup ->
                        val isDateExpanded = expandedDates.contains(dayGroup.dateKey)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                // Date Header (Expandable Dropdown)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedDates = if (isDateExpanded) {
                                                expandedDates - dayGroup.dateKey
                                            } else {
                                                expandedDates + dayGroup.dateKey
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDateExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dayGroup.displayDate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${dayGroup.transactions.size})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = dayGroup.totalDebitFormatted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }

                                // Date Transactions List
                                AnimatedVisibility(
                                    visible = isDateExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, start = 8.dp)
                                    ) {
                                        dayGroup.transactions.forEach { tx ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                try {
                                                                    Color(android.graphics.Color.parseColor(tx.categoryColor))
                                                                } catch (e: Exception) {
                                                                    MaterialTheme.colorScheme.primary
                                                                }
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = tx.displayCategory,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = dateFormat.format(Date(tx.timestamp)),
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = if (tx.transactionType.equals("CREDIT", ignoreCase = true)) "+₹${tx.amountCents / 100}" else "₹${tx.amountCents / 100}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = if (tx.transactionType.equals("CREDIT", ignoreCase = true)) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideAccordionItem(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Text(
                text = content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun PermissionsSetupCard(
    hasOverlayPermission: Boolean,
    hasNotificationPermission: Boolean,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Permissions & Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure device permissions for instant HUD & smart parsing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 1. Shake Anywhere Dropdown (Overlay Permission)
            PermissionSetupDropdown(
                title = "Shake Anywhere",
                description = "Required to trigger the Quick Entry keypad instantly from the Android Home Screen or while using other apps.",
                isGranted = hasOverlayPermission,
                actionText = if (hasOverlayPermission) "Manage Overlay Setting" else "Open Overlay Settings",
                onAction = onOpenOverlaySettings,
                icon = Icons.Default.Vibration
            )

            // 2. Bank Notification Detection Dropdown
            PermissionSetupDropdown(
                title = "Bank Notification Detection",
                description = "ShakeExpense reads supported bank and payment notifications on your device to detect debit/credit transactions and ask for confirmation before adding them to your expenses.",
                isGranted = hasNotificationPermission,
                actionText = if (hasNotificationPermission) "Manage Notification Access" else "Enable Notification Access",
                onAction = onOpenNotificationSettings,
                icon = Icons.Default.NotificationsActive
            )
        }
    }
}

@Composable
private fun PermissionSetupDropdown(
    title: String,
    description: String,
    isGranted: Boolean,
    actionText: String,
    onAction: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp)
    ) {
        // Dropdown Header Row (Clickable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status Indicator Badge (✅ Enabled / ⚠️ Required)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "Enabled" else "Required",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expandable Dropdown Content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGranted) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF2563EB),
                        contentColor = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(text = actionText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun formatResetCurrency(cents: Long): String {
    val rupees = cents / 100.0
    val format = java.text.NumberFormat.getNumberInstance(Locale("en", "IN"))
    format.minimumFractionDigits = if (cents % 100L == 0L) 0 else 2
    format.maximumFractionDigits = 2
    return format.format(rupees)
}

@Composable
fun FinancialSafetyCard(
    score: Int?,
    primaryOpportunity: String?,
    spendingSubScore: String = "🟢 Healthy",
    budgetSubScore: String = "🟢 Healthy",
    recurringSubScore: String = "🟢 Healthy",
    riskSignalsSubScore: String = "🟢 None"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Deep Navy Slate
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Financial Safety System",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AI Protection & Health Analysis",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                if (score != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$score / 100",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

            if (score != null) {
                // Sub-scores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScorePill(label = "Spending", score = spendingSubScore)
                    ScorePill(label = "Budget", score = budgetSubScore)
                    ScorePill(label = "Recurring", score = recurringSubScore)
                    ScorePill(label = "Risk Signals", score = riskSignalsSubScore)
                }

                // Primary Opportunity
                if (!primaryOpportunity.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = primaryOpportunity,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Financial Safety Score will appear after enough financial activity is recorded.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ScorePill(label: String, score: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = score, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SubscriptionsSummaryCard(
    subscriptions: List<com.shakeexpense.app.data.database.entity.RecurringPaymentEntity>,
    annualBurdenCents: Long,
    unusedWarnings: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔄", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Recurring & Subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (subscriptions.isNotEmpty()) {
                                "${subscriptions.size} active services · ₹${annualBurdenCents / 100}/year"
                            } else {
                                "No subscriptions detected yet"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (subscriptions.isEmpty()) {
                Text(
                    text = "No recurring payments detected yet. As you record transactions or receive bank alerts, recurring payments (e.g. Netflix, Rent, EMI) will automatically be recognized.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            // Unused subscription warning banner
            if (unusedWarnings.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unusedWarnings.first(),
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Expanded list
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                subscriptions.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sub.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = sub.cadence,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${sub.amountCents / 100}/mo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionTierCard(
    currentPlan: com.shakeexpense.app.domain.model.SubscriptionPlan,
    onOpenPlans: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (currentPlan) {
                com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO -> Color(0xFF4338CA)
                com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS -> Color(0xFF1E3A8A)
                com.shakeexpense.app.domain.model.SubscriptionPlan.FREE -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Membership: ${currentPlan.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FREE) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                        Text(
                            text = when (currentPlan) {
                                com.shakeexpense.app.domain.model.SubscriptionPlan.FREE -> "Basic expense tracking · ₹0"
                                com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS -> "Individual Intelligence · ₹59/mo or ₹699/yr"
                                com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO -> "Family Safety & Limits · ₹99/mo or ₹999/yr"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FREE) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFE2E8F0)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenPlans,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FREE) MaterialTheme.colorScheme.primary else Color.White
                )
            ) {
                Text(text = "View Subscription Plans & Compare", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SubscriptionPlansDialog(
    currentPlan: com.shakeexpense.app.domain.model.SubscriptionPlan,
    onSelectPlan: (com.shakeexpense.app.domain.model.SubscriptionPlan) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingPaymentPlan by remember { mutableStateOf<com.shakeexpense.app.domain.model.SubscriptionPlan?>(null) }

    if (pendingPaymentPlan != null) {
        MembershipPaymentDialog(
            targetPlan = pendingPaymentPlan!!,
            onDismiss = { pendingPaymentPlan = null },
            onPaymentSuccess = { plan ->
                onSelectPlan(plan)
                pendingPaymentPlan = null
                onDismiss()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Subscription Plans", fontWeight = FontWeight.Bold)
                Text(
                    text = "Personal Financial Safety System",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FREE PLAN
                PlanOptionCard(
                    title = "FREE — ₹0",
                    subtitle = "Habit & Core Expense Tracking",
                    features = listOf(
                        "Manual expense & income entry",
                        "Automatic bank/UPI transaction detection",
                        "Daily/Weekly/Monthly views & basic charts",
                        "Basic budgets & local Room storage",
                        "Family participation (join groups)"
                    ),
                    isCurrent = currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FREE,
                    onSelect = { onSelectPlan(com.shakeexpense.app.domain.model.SubscriptionPlan.FREE) }
                )

                // PLUS PLAN
                PlanOptionCard(
                    title = "PLUS — ₹59/mo or ₹699/yr",
                    subtitle = "Understand & Protect Your Money",
                    features = listOf(
                        "Advanced search (merchant, category, amount, dates)",
                        "Unusual-spending pattern alerts",
                        "CSV transaction export",
                        "AI Financial Assistant",
                        "Next-month expense prediction (Statistical / ML)",
                        "Predicted & user-editable monthly spending limits",
                        "70%, 80%, 90%, 100% threshold notifications",
                        "Safe-to-Spend real-time daily runway"
                    ),
                    isCurrent = currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS,
                    isPopular = true,
                    onSelect = {
                        if (currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS) {
                            onDismiss()
                        } else {
                            pendingPaymentPlan = com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS
                        }
                    }
                )

                // FAMILY PRO PLAN
                PlanOptionCard(
                    title = "FAMILY PRO — ₹99/mo or ₹999/yr",
                    subtitle = "Manage Your Family's Finances Together",
                    features = listOf(
                        "Everything in PLUS for up to 5 family members",
                        "Shared family monthly spending limit",
                        "Family limit exceeded alerts (instant notification)",
                        "Family privacy controls (private / shared summary / shared)",
                        "Shared financial goals & family AI reports"
                    ),
                    isCurrent = currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO,
                    onSelect = {
                        if (currentPlan == com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO) {
                            onDismiss()
                        } else {
                            pendingPaymentPlan = com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun MembershipPaymentDialog(
    targetPlan: com.shakeexpense.app.domain.model.SubscriptionPlan,
    onDismiss: () -> Unit,
    onPaymentSuccess: (com.shakeexpense.app.domain.model.SubscriptionPlan) -> Unit
) {
    val context = LocalContext.current
    var isYearly by remember { mutableStateOf(false) }

    val amountInRupees = when (targetPlan) {
        com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS -> if (isYearly) 699 else 59
        com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO -> if (isYearly) 999 else 99
        com.shakeexpense.app.domain.model.SubscriptionPlan.FREE -> 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💳", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Payment Gateway Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Upgrade to ${targetPlan.displayName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Billing Cycle Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Select Billing Frequency", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isYearly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { isYearly = false }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Plan",
                                fontSize = 12.sp,
                                fontWeight = if (!isYearly) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = when (targetPlan) {
                                    com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS -> "₹59/month"
                                    com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO -> "₹99/month"
                                    else -> "₹0"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isYearly) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { isYearly = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Annual Plan",
                                    fontSize = 12.sp,
                                    fontWeight = if (isYearly) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Best value · Save up to 15%",
                                    fontSize = 10.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                            Text(
                                text = when (targetPlan) {
                                    com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS -> "₹699/year"
                                    com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO -> "₹999/year"
                                    else -> "₹0"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Membership Tier", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(targetPlan.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Billing Frequency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (isYearly) "Annual (12 Months)" else "Monthly (1 Month)", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxes & GST", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Inclusive", fontSize = 12.sp, color = Color(0xFF10B981))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("₹$amountInRupees", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Payment Gateway Redirect Action
                Button(
                    onClick = {
                        try {
                            val upiUri = Uri.parse("upi://pay?pa=shakeexpense@okhdfcbank&pn=ShakeExpense&am=$amountInRupees&cu=INR&tn=Membership_${targetPlan.name}")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            context.startActivity(Intent.createChooser(intent, "Pay ₹$amountInRupees with UPI"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Redirecting to Google Play Subscriptions...", Toast.LENGTH_SHORT).show()
                            try {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                                context.startActivity(webIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Please configure a UPI app or Play Store to complete payment", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Proceed to Pay ₹$amountInRupees (UPI / Gateway)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Complete / Verify Test Payment
                OutlinedButton(
                    onClick = {
                        onPaymentSuccess(targetPlan)
                        Toast.makeText(context, "Payment confirmed! Subscribed to ${targetPlan.displayName}", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Payment & Activate Membership", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PlanOptionCard(
    title: String,
    subtitle: String,
    features: List<String>,
    isCurrent: Boolean,
    isPopular: Boolean = false,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isPopular) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2563EB)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981)) {
                        Text(
                            text = "CURRENT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isPopular) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB)) {
                        Text(
                            text = "POPULAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            features.forEach { feature ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "✓", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = feature, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

