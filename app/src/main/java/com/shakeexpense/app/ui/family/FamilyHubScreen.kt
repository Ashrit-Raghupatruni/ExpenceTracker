package com.shakeexpense.app.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.domain.model.FamilyBudget
import com.shakeexpense.app.domain.model.FamilyPrivacySettings
import com.shakeexpense.app.domain.usecase.FamilyAiReport
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.ui.tracker.CategoryBreakdownList
import com.shakeexpense.app.ui.tracker.SpreadsheetDataGrid
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHubScreen(
    viewModel: FamilyViewModel,
    onLaunchGoogleSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with Title & Sync Action
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Family & Parental Hub",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.currentFamilyGroup?.let { "${it.familyName} • Code: ${it.inviteCode}" }
                                    ?: "Multi-device cloud sync & parental controls",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerSync() },
                            enabled = !state.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Family Actions (Create / Join Family Bar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openCreateFamilyDialog() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Family", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.openJoinFamilyDialog() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Join via Code", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Sync message banner
            if (state.syncMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.syncMessage ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Family Spending Alert Banner (if >= 70%)
                item {
                    FamilyThresholdAlertBanner(state)
                }

                // 2. Family Monthly Spending Limit Card
                item {
                    FamilyMonthlyLimitCard(
                        state = state,
                        onEditLimit = { viewModel.onOpenEditFamilyLimitDialog() },
                        onAiReport = { viewModel.onGenerateFamilyReport() }
                    )
                }

                // 3. Shared Family Budgets Section
                item {
                    SharedFamilyBudgetsSection(
                        state = state,
                        onAddBudget = { viewModel.onOpenAddBudgetDialog() },
                        onDeleteBudget = { viewModel.onDeleteCategoryBudget(it) }
                    )
                }

                // 4. Family Members List Header
                item {
                    FamilyMembersListHeader(
                        memberCount = state.membersWithSummaries.size,
                        onInvite = { viewModel.openAddMemberDialog() }
                    )
                }

                // 5. Family Member Cards
                items(state.membersWithSummaries) { item ->
                    FamilyMemberCard(
                        member = item.member,
                        isCurrentUser = item.member.id == state.currentUserId,
                        onClick = { viewModel.onMemberSelected(item.member) },
                        onPrivacyClick = { viewModel.onOpenPrivacySettingsDialog(item.member) }
                    )
                }
            }
        }

        // Invite Member Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.openAddMemberDialog() },
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = "Invite Family Member")
        }

        // Edit Family Monthly Limit Dialog
        if (state.isEditFamilyLimitDialogOpen) {
            EditFamilyLimitDialog(
                currentLimitCents = state.familyMonthlyLimitCents,
                onDismiss = { viewModel.onDismissEditFamilyLimitDialog() },
                onSave = { viewModel.onSaveFamilyLimit(it) }
            )
        }

        // Add Category Budget Dialog
        if (state.isAddBudgetDialogOpen) {
            AddCategoryBudgetDialog(
                onDismiss = { viewModel.onDismissAddBudgetDialog() },
                onSave = { category, amount -> viewModel.onSaveCategoryBudget(category, amount) }
            )
        }

        // Granular Privacy Controls Dialog
        if (state.isPrivacySettingsDialogOpen && state.editingMemberPrivacy != null) {
            MemberPrivacySettingsDialog(
                member = state.editingMemberPrivacy!!,
                onDismiss = { viewModel.onDismissPrivacySettingsDialog() },
                onSave = { settings -> viewModel.onSavePrivacySettings(state.editingMemberPrivacy!!.id, settings) }
            )
        }

        // Family AI Financial Report Dialog
        if (state.isFamilyReportOpen && state.familyAiReport != null) {
            FamilyReportDialog(
                report = state.familyAiReport!!,
                onDismiss = { viewModel.onDismissFamilyReport() }
            )
        }

        // Family Upgrade Paywall Dialog
        if (state.showUpgradePaywallDialog) {
            FamilyUpgradePaywallDialog(
                featureTitle = state.paywallFeatureTitle,
                onDismiss = { viewModel.onDismissUpgradePaywall() }
            )
        }

        // Create Family Dialog
        if (state.isCreateFamilyDialogOpen) {
            CreateFamilyDialog(
                onDismiss = { viewModel.closeCreateFamilyDialog() },
                onCreate = { familyName -> viewModel.createFamily(familyName) }
            )
        }

        // Join Family Dialog
        if (state.isJoinFamilyDialogOpen) {
            JoinFamilyDialog(
                onDismiss = { viewModel.closeJoinFamilyDialog() },
                onJoin = { code, name, role -> viewModel.joinFamily(code, name, role) }
            )
        }

        // Family Created Invite & QR Dialog
        if (state.createdFamilyInvite != null) {
            FamilyInviteQrDialog(
                family = state.createdFamilyInvite!!,
                onDismiss = { viewModel.dismissCreatedFamilyInvite() }
            )
        }

        // Invite Member Dialog
        if (state.isAddMemberDialogOpen) {
            InviteFamilyMemberDialog(
                family = state.currentFamilyGroup,
                onDismiss = { viewModel.closeAddMemberDialog() },
                onCreateFamilyRequested = { viewModel.openCreateFamilyDialog() }
            )
        }

        // Sign In Required Dialog for Sync / Family
        if (state.showSignInRequiredDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSignInRequiredDialog() },
                title = { Text("Sign In Required to Sync") },
                text = { Text("You need to sign in with your Google account before you can synchronize expenses with the cloud and family devices.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissSignInRequiredDialog()
                            onLaunchGoogleSignIn()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Sign In with Google")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissSignInRequiredDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Member Detail Bottom Sheet
        if (state.selectedMember != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onDismissMemberDetail() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MemberDetailContent(
                    member = state.selectedMember!!,
                    state = state,
                    onClose = { viewModel.onDismissMemberDetail() },
                    onRemoveMember = { viewModel.removeMember(it) },
                    onRequestExit = { viewModel.requestChildExit(it) }
                )
            }
        }
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    isCurrentUser: Boolean = false,
    onClick: () -> Unit,
    onPrivacyClick: () -> Unit = {}
) {
    val isParent = member.role == FamilyRole.PARENT
    val badgeColor = if (isParent) Color(0xFF2563EB) else Color(0xFF10B981)
    val badgeBg = if (isParent) Color(0xFFDBEAFE) else Color(0xFFD1FAE5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "You",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (member.isExitRequested) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "Exit Requested",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "User ID: ${member.id}",
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPrivacyClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Privacy Settings",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = member.role.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemberDetailContent(
    member: FamilyMember,
    state: FamilyState,
    onClose: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onRequestExit: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = member.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "User ID: ${member.id}",
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Role: ${member.role.name} • Individual Spending Overview",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spending Summary Banner
        val summary = state.selectedMemberSummary
        val totalSpentCents = summary?.totalDebitCents ?: 0L
        val totalCreditCents = summary?.totalCreditCents ?: 0L
        val txnCount = summary?.transactionCount ?: 0

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MEMBER SPENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "₹ ${formatCurrency(totalSpentCents)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Transactions: $txnCount",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Credit: ₹ ${formatCurrency(totalCreditCents)}",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Member's Category Breakdown
        Text(
            text = "Category Breakdown",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (state.selectedMemberBreakdown.isEmpty()) {
            Text(
                text = "No category transactions yet for ${member.name}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Box(modifier = Modifier.height(180.dp)) {
                CategoryBreakdownList(
                    breakdowns = state.selectedMemberBreakdown,
                    totalDebitCents = totalSpentCents
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Member's Transactions
        Text(
            text = "Transaction History",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (state.selectedMemberExpenses.isEmpty()) {
            Text(
                text = "No recent transactions found",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Box(modifier = Modifier.height(180.dp)) {
                SpreadsheetDataGrid(records = state.selectedMemberExpenses)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parent Removal / Child Exit Controls
        var showRemoveDialog by remember { mutableStateOf(false) }
        var showExitDialog by remember { mutableStateOf(false) }

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                title = { Text("Remove ${member.name}?") },
                text = { Text("As parent, removing this member will detach their device from the family group.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveMember(member.id)
                            showRemoveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Remove Member")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Request Exit from Family") },
                text = { Text("Parent approval is required for a child to exit this family. Submit exit request to parents?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onRequestExit(member.id)
                            showExitDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Submit Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showRemoveDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Remove Member", fontSize = 11.sp)
            }

            if (member.role == FamilyRole.CHILD) {
                OutlinedButton(
                    onClick = { showExitDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Request Exit", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CreateFamilyDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create Family Group") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Create a cloud family group to share and synchronize expenses across devices.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family Name") },
                    placeholder = { Text("e.g. Sharma Family") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (familyName.isNotBlank()) onCreate(familyName.trim()) },
                enabled = familyName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Generate Invite Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun JoinFamilyDialog(
    onDismiss: () -> Unit,
    onJoin: (String, String, FamilyRole) -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(FamilyRole.CHILD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Join Family via Code / QR") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("6-Digit Invite Code") },
                    placeholder = { Text("e.g. SHK-8821") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Your Name") },
                    placeholder = { Text("e.g. Rohan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Your Role in Family:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = role == FamilyRole.CHILD,
                        onClick = { role = FamilyRole.CHILD }
                    )
                    Text("Child", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = role == FamilyRole.PARENT,
                        onClick = { role = FamilyRole.PARENT }
                    )
                    Text("Parent", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (inviteCode.isNotBlank() && userName.isNotBlank()) onJoin(inviteCode, userName, role) },
                enabled = inviteCode.isNotBlank() && userName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Join Family")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun InviteFamilyMemberDialog(
    family: FamilyGroupDto?,
    onDismiss: () -> Unit,
    onCreateFamilyRequested: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Invite Family Member")
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (family != null) {
                    Text(
                        text = "To connect a real member, have them install ShakeExpense and enter your family invite code.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "FAMILY INVITE CODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = family.inviteCode,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF2563EB)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Group: ${family.familyName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(family.inviteCode))
                                android.widget.Toast.makeText(context, "Invite code copied!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Code", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Join my family group on ShakeExpense!\n\nFamily: ${family.familyName}\nInvite Code: ${family.inviteCode}\n\nDownload the app and enter this code in the Family tab to connect."
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Family Invite"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Invite", fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = "You haven't created a family group yet. Create your family group to generate your official Invite Code & QR.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onDismiss()
                            onCreateFamilyRequested()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Create Family Group")
                    }
                }
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
fun FamilyInviteQrDialog(
    family: FamilyGroupDto,
    onDismiss: () -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Family Created Online!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = family.familyName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Share this 6-digit Invite Code or QR with your family members to link their devices.",
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Visual QR Pattern
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 11f
                        val codeHash = family.inviteCode.hashCode()
                        // Draw QR finder corners
                        drawRect(Color.Black, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.White, androidx.compose.ui.geometry.Offset(cellSize, cellSize), androidx.compose.ui.geometry.Size(cellSize, cellSize))
                        drawRect(Color.Black, androidx.compose.ui.geometry.Offset(size.width - cellSize * 3, 0f), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.White, androidx.compose.ui.geometry.Offset(size.width - cellSize * 2, cellSize), androidx.compose.ui.geometry.Size(cellSize, cellSize))
                        drawRect(Color.Black, androidx.compose.ui.geometry.Offset(0f, size.height - cellSize * 3), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.White, androidx.compose.ui.geometry.Offset(cellSize, size.height - cellSize * 2), androidx.compose.ui.geometry.Size(cellSize, cellSize))

                        // Draw QR data cells
                        for (i in 0..10) {
                            for (j in 0..10) {
                                if ((i < 4 && j < 4) || (i > 6 && j < 4) || (i < 4 && j > 6)) continue
                                val bit = ((codeHash shr ((i * 11 + j) % 31)) and 1) == 1
                                if (bit || (i + j) % 3 == 0) {
                                    drawRect(
                                        Color.Black,
                                        androidx.compose.ui.geometry.Offset(i * cellSize, j * cellSize),
                                        androidx.compose.ui.geometry.Size(cellSize * 0.85f, cellSize * 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Invite Code Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INVITE CODE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = family.inviteCode,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(family.inviteCode))
                                android.widget.Toast.makeText(context, "Code copied: ${family.inviteCode}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Done")
            }
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

@Composable
fun FamilyThresholdAlertBanner(state: FamilyState) {
    if (state.familyMonthlyLimitCents <= 0L) return
    val spentCents = state.familyCurrentSpentCents
    val limitCents = state.familyMonthlyLimitCents
    val pct = (spentCents.toDouble() / limitCents.toDouble() * 100.0).toInt()
    if (pct < 70) return

    val (bgColor, textColor, iconColor, message) = when {
        pct >= 100 -> {
            val excess = (spentCents - limitCents).coerceAtLeast(0L) / 100
            Quadruple(
                Color(0xFFFEF2F2),
                Color(0xFF991B1B),
                Color(0xFFDC2626),
                if (excess > 0) "🚨 Family monthly spending limit exceeded by ₹$excess!"
                else "🚨 Family monthly spending limit reached!"
            )
        }
        pct >= 90 -> Quadruple(
            Color(0xFFFFF7ED),
            Color(0xFF9A3412),
            Color(0xFFEA580C),
            "🚨 Critical: Family has used 90% of the monthly limit (₹${spentCents / 100} / ₹${limitCents / 100})."
        )
        pct >= 80 -> Quadruple(
            Color(0xFFFFFBEB),
            Color(0xFF92400E),
            Color(0xFFD97706),
            "⚠️ Caution: Family has reached 80% of the monthly limit (₹${spentCents / 100} / ₹${limitCents / 100})."
        )
        else -> Quadruple(
            Color(0xFFFFFBEB),
            Color(0xFF92400E),
            Color(0xFFD97706),
            "⚠️ Notice: Family has used 70% of the monthly limit (₹${spentCents / 100} / ₹${limitCents / 100})."
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun FamilyMonthlyLimitCard(
    state: FamilyState,
    onEditLimit: () -> Unit,
    onAiReport: () -> Unit
) {
    val limitCents = state.familyMonthlyLimitCents
    val spentCents = state.familyCurrentSpentCents
    val remainingCents = (limitCents - spentCents).coerceAtLeast(0L)
    val pct = if (limitCents > 0L) (spentCents.toFloat() / limitCents.toFloat()).coerceIn(0f, 1f) else 0f
    val pctDisplay = if (limitCents > 0L) (spentCents.toDouble() / limitCents.toDouble() * 100.0) else 0.0

    val progressColor = when {
        pctDisplay >= 100.0 -> Color(0xFFDC2626)
        pctDisplay >= 85.0 -> Color(0xFFEA580C)
        pctDisplay >= 70.0 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Family Monthly Limit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (limitCents > 0L) "₹${formatCurrency(limitCents)}" else "No limit set",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = progressColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (limitCents > 0L) "${String.format(Locale.US, "%.1f", pctDisplay)}%" else "0%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Spent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${formatCurrency(spentCents)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${formatCurrency(remainingCents)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditLimit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Monthly Limit", fontSize = 11.sp)
                }

                Button(
                    onClick = onAiReport,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Family Report", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SharedFamilyBudgetsSection(
    state: FamilyState,
    onAddBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shared Family Budgets",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Collective category limits for the entire household",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddBudget() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Budget", color = Color(0xFF3B82F6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.sharedBudgets.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No shared family budget has been created yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.sharedBudgets.forEach { budget ->
                        SharedBudgetRow(budget = budget, onDelete = { onDeleteBudget(budget.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedBudgetRow(
    budget: FamilyBudget,
    onDelete: () -> Unit
) {
    val pct = (budget.spentCents.toFloat() / budget.limitCents.toFloat()).coerceIn(0f, 1f)
    val pctDisplay = budget.usagePercentage
    val barColor = if (budget.isExceeded) Color(0xFFDC2626) else if (pctDisplay >= 80.0) Color(0xFFD97706) else Color(0xFF2563EB)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.categoryName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${formatCurrency(budget.spentCents)} / ₹${formatCurrency(budget.limitCents)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Budget", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
fun FamilyMembersListHeader(
    memberCount: Int,
    onInvite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "FAMILY MEMBERS ($memberCount/5)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onInvite,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Invite Member", fontSize = 12.sp)
        }
    }
}

@Composable
fun EditFamilyLimitDialog(
    currentLimitCents: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var limitInput by remember { mutableStateOf(if (currentLimitCents > 0L) (currentLimitCents / 100).toString() else "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Family Monthly Limit", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Set a collective monthly spending cap for all family members. When spending crosses 70%, 80%, 90%, or 100%, authorized members will be alerted.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = {
                        limitInput = it
                        error = null
                    },
                    label = { Text("Monthly Limit (₹)") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = limitInput.toLongOrNull()
                    if (amount == null || amount <= 0L) {
                        error = "Please enter an amount greater than zero"
                    } else if (amount > 10_000_000L) {
                        error = "Limit exceeds maximum supported value"
                    } else {
                        onSave(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Limit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddCategoryBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    val categoryList = listOf(
        Pair("Food", Color(0xFFF59E0B)),
        Pair("Transport", Color(0xFF3B82F6)),
        Pair("Groceries", Color(0xFF10B981)),
        Pair("Bills", Color(0xFF8B5CF6)),
        Pair("Shopping", Color(0xFFEC4899)),
        Pair("Entertainment", Color(0xFFA855F7)),
        Pair("Education", Color(0xFF6366F1)),
        Pair("Health", Color(0xFFEF4444)),
        Pair("Others", Color(0xFF64748B))
    )
    var selectedCategory by remember { mutableStateOf(categoryList.first().first) }
    var limitInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Shared Category Budget", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Select a category and specify the monthly spending limit for the entire family:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryList.forEach { (catName, catColor) ->
                        val isSelected = selectedCategory.equals(catName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) catColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = catName }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = catName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = limitInput,
                    onValueChange = {
                        limitInput = it
                        error = null
                    },
                    label = { Text("$selectedCategory Monthly Limit (₹)") },
                    placeholder = { Text("e.g. 5000") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = limitInput.toLongOrNull()
                    if (amount == null || amount <= 0L) {
                        error = "Please enter an amount greater than ₹0"
                    } else if (amount > 10_000_000L) {
                        error = "Amount exceeds limit"
                    } else {
                        onSave(selectedCategory, amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
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
fun MemberPrivacySettingsDialog(
    member: FamilyMember,
    onDismiss: () -> Unit,
    onSave: (FamilyPrivacySettings) -> Unit
) {
    var shareTx by remember { mutableStateOf(member.privacySettings.shareTransactions) }
    var shareMonth by remember { mutableStateOf(member.privacySettings.shareMonthlyTotal) }
    var shareCat by remember { mutableStateOf(member.privacySettings.shareCategoryTotals) }
    var receiveAlerts by remember { mutableStateOf(member.privacySettings.receiveFamilyAlerts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Financial Privacy (${member.name})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Configure granular data-sharing and notifications for this family profile:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PrivacyToggleRow(
                    title = "Share transactions with family",
                    description = "When OFF, individual transaction details are confidential",
                    checked = shareTx,
                    onCheckedChange = { shareTx = it }
                )

                PrivacyToggleRow(
                    title = "Share monthly spending total",
                    description = "Allow family to see aggregate monthly spend",
                    checked = shareMonth,
                    onCheckedChange = { shareMonth = it }
                )

                PrivacyToggleRow(
                    title = "Share category totals",
                    description = "Allow family to see category spending breakdown",
                    checked = shareCat,
                    onCheckedChange = { shareCat = it }
                )

                PrivacyToggleRow(
                    title = "Receive family financial alerts",
                    description = "Get notified when family monthly limit is crossed",
                    checked = receiveAlerts,
                    onCheckedChange = { receiveAlerts = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        FamilyPrivacySettings(
                            shareTransactions = shareTx,
                            shareMonthlyTotal = shareMonth,
                            shareCategoryTotals = shareCat,
                            receiveFamilyAlerts = receiveAlerts
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Privacy")
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
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
        )
    }
}

@Composable
fun FamilyReportDialog(
    report: FamilyAiReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Family Financial Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(report.monthTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))

                if (!report.hasEnoughData) {
                    Text("Not enough family financial data to generate a report.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // Overview Summary Card
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Spending:", fontSize = 12.sp)
                                Text("₹${formatCurrency(report.totalSpendingCents)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Family Limit:", fontSize = 12.sp)
                                Text("₹${formatCurrency(report.familyLimitCents)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Remaining:", fontSize = 12.sp)
                                Text("₹${formatCurrency(report.remainingLimitCents)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    // Top Categories
                    Column {
                        Text("TOP CATEGORIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))
                        report.topCategories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.categoryName, fontSize = 12.sp)
                                Text("₹${formatCurrency(cat.totalCents)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // AI Insights
                    Column {
                        Text("AI INSIGHTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                        Spacer(modifier = Modifier.height(6.dp))
                        report.aiInsights.forEach { insight ->
                            Text("• $insight", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    // Budget Health
                    if (report.budgetHealth.isNotEmpty()) {
                        Column {
                            Text("BUDGET HEALTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(6.dp))
                            report.budgetHealth.forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(b.categoryName, fontSize = 12.sp)
                                    val statusColor = if (b.status == "EXCEEDED") Color(0xFFDC2626) else Color(0xFF10B981)
                                    Text("${String.format(Locale.US, "%.0f", b.usagePercentage)}% (${b.status})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FamilyUpgradePaywallDialog(
    featureTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("FAMILY PRO Feature", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "$featureTitle is available exclusively on the FAMILY PRO plan (₹99/month or ₹999/year).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Family Pro unlocks up to 5 family members, shared collective budgets, monthly limit alerts, granular privacy controls, and AI family reports.",
                    fontSize = 11.sp,
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


