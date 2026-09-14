@file:Suppress("DEPRECATION")

package com.shakeexpense.app.ui.tracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.shakeexpense.app.ShakeExpenseApp
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.CategoryRepositoryImpl
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.data.repository.FamilyRepositoryImpl
import com.shakeexpense.app.domain.usecase.AddExpenseUseCase
import com.shakeexpense.app.domain.usecase.AddFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.CreateFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.DeleteExpenseUseCase
import com.shakeexpense.app.domain.usecase.EditExpenseUseCase
import com.shakeexpense.app.domain.usecase.GetCategoriesUseCase
import com.shakeexpense.app.domain.usecase.GetCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetFamilyMembersUseCase
import com.shakeexpense.app.domain.usecase.GetMemberCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetMemberExpensesUseCase
import com.shakeexpense.app.domain.usecase.GetMemberSpendingSummaryUseCase
import com.shakeexpense.app.domain.usecase.GetMonthlyExpenseHistoryUseCase
import com.shakeexpense.app.domain.usecase.GetSpendingTotalsUseCase
import com.shakeexpense.app.domain.usecase.GetSpreadsheetStreamUseCase
import com.shakeexpense.app.domain.usecase.JoinFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.RemoveFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.RequestChildExitUseCase
import com.shakeexpense.app.domain.usecase.SyncFamilyExpensesUseCase
import com.shakeexpense.app.sensor.ShakeSensorService
import com.shakeexpense.app.sync.SyncApiClientProvider
import com.shakeexpense.app.sync.SyncEngine
import com.shakeexpense.app.ui.entry.ExpenseEntryViewModel
import com.shakeexpense.app.ui.family.FamilyHubScreen
import com.shakeexpense.app.ui.family.FamilyViewModel
import com.shakeexpense.app.ui.overlay.QuickEntryActivity
import com.shakeexpense.app.ui.overlay.QuickEntryOverlayScreen
import com.shakeexpense.app.ui.profile.ProfileScreen
import com.shakeexpense.app.ui.profile.ProfileViewModel
import com.shakeexpense.app.ui.theme.AppThemeMode
import com.shakeexpense.app.ui.theme.ShakeExpenseTheme
import com.shakeexpense.app.ui.theme.ThemePreferences
import com.shakeexpense.app.util.NetworkConnectivityMonitor

enum class MainNavigationSection {
    TRACKER,
    FAMILY,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { (application as ShakeExpenseApp).container }
    private val themePreferences by lazy { appContainer.themePreferences }
    private val authRepository by lazy { appContainer.authRepository }

    private val trackerViewModel: TrackerViewModel by viewModels {
        val currentUserId = authRepository.getCurrentProfile().userId
        appContainer.createTrackerViewModelFactory(currentUserId)
    }

    private val expenseEntryViewModel: ExpenseEntryViewModel by viewModels {
        appContainer.createExpenseEntryViewModelFactory()
    }

    private val familyViewModel: FamilyViewModel by viewModels {
        appContainer.createFamilyViewModelFactory()
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        appContainer.createProfileViewModelFactory()
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val idToken = account.idToken
                    if (!idToken.isNullOrBlank()) {
                        profileViewModel.signInWithGoogleToken(idToken)
                    } else {
                        val userId = account.id ?: ("usr_" + (account.email ?: "google_user").replace("@", "_at_").replace(".", "_"))
                        profileViewModel.setGoogleAccountDirect(
                            userId = userId,
                            displayName = account.displayName ?: "Google User",
                            email = account.email,
                            photoUrl = account.photoUrl?.toString()
                        )
                    }
                }
            } catch (e: Exception) {
                val account = GoogleSignIn.getLastSignedInAccount(this)
                if (account != null) {
                    val userId = account.id ?: ("usr_" + (account.email ?: "google_user").replace("@", "_at_").replace(".", "_"))
                    profileViewModel.setGoogleAccountDirect(
                        userId = userId,
                        displayName = account.displayName ?: "Google User",
                        email = account.email,
                        photoUrl = account.photoUrl?.toString()
                    )
                } else {
                    profileViewModel.setAuthError(e.message ?: "Google Sign-In failed")
                }
            }
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermissionState.value = checkOverlayPermission()
    }

    private val hasOverlayPermissionState = mutableStateOf(false)

    private fun launchGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            client.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(client.signInIntent)
            }
        } catch (e: Exception) {
            profileViewModel.setAuthError(e.message ?: "Could not launch Google Sign-In")
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasOverlayPermissionState.value = checkOverlayPermission()

        val app = application as ShakeExpenseApp
        val currentUserId = authRepository.getCurrentProfile().userId
        lifecycleScope.launch {
            app.database.financialProfileDao().getProfileFlow(currentUserId).collect { profile ->
                val plan = when (profile?.tier?.uppercase()) {
                    "FAMILY_PRO" -> com.shakeexpense.app.domain.model.SubscriptionPlan.FAMILY_PRO
                    "PLUS" -> com.shakeexpense.app.domain.model.SubscriptionPlan.PLUS
                    else -> com.shakeexpense.app.domain.model.SubscriptionPlan.FREE
                }
                app.entitlementManager.updatePlan(plan)
            }
        }

        setContent {
            val currentThemeMode by themePreferences.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)

            ShakeExpenseTheme(themeMode = currentThemeMode) {
                var currentSection by rememberSaveable { mutableStateOf(MainNavigationSection.TRACKER) }
                var isQuickEntryOpen by rememberSaveable { mutableStateOf(false) }
                val hasOverlayPermission by hasOverlayPermissionState

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                shadowElevation = 8.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentSection == MainNavigationSection.TRACKER,
                                        onClick = { currentSection = MainNavigationSection.TRACKER },
                                        icon = { Icon(Icons.Default.TableChart, contentDescription = "Tracker") },
                                        label = { Text("Tracker", fontWeight = if (currentSection == MainNavigationSection.TRACKER) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                    NavigationBarItem(
                                        selected = currentSection == MainNavigationSection.FAMILY,
                                        onClick = { currentSection = MainNavigationSection.FAMILY },
                                        icon = { Icon(Icons.Default.Group, contentDescription = "Family Hub") },
                                        label = { Text("Family", fontWeight = if (currentSection == MainNavigationSection.FAMILY) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                    NavigationBarItem(
                                        selected = currentSection == MainNavigationSection.PROFILE,
                                        onClick = { currentSection = MainNavigationSection.PROFILE },
                                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                        label = { Text("Profile", fontWeight = if (currentSection == MainNavigationSection.PROFILE) FontWeight.Bold else FontWeight.Normal) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Permission helper banner for background shake popup
                            if (!hasOverlayPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Vibration,
                                                contentDescription = null,
                                                tint = Color(0xFFB45309),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Enable Shake from Any Screen",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                                Text(
                                                    text = "Grant 'Display over other apps' for instant HUD",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:$packageName")
                                                )
                                                startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Grant", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            when (currentSection) {
                                MainNavigationSection.TRACKER -> {
                                    TrackerMainScreen(
                                        viewModel = trackerViewModel,
                                        onOpenQuickEntry = {
                                            expenseEntryViewModel.resetSaveState()
                                            isQuickEntryOpen = true
                                        }
                                    )
                                }
                                MainNavigationSection.FAMILY -> {
                                    FamilyHubScreen(
                                        viewModel = familyViewModel,
                                        onLaunchGoogleSignIn = { launchGoogleSignIn() }
                                    )
                                }
                                MainNavigationSection.PROFILE -> {
                                    ProfileScreen(
                                        viewModel = profileViewModel,
                                        onLaunchGoogleSignIn = { launchGoogleSignIn() }
                                    )
                                }
                            }
                        }
                    }

                    // In-App Seamless Quick Entry Overlay
                    if (isQuickEntryOpen) {
                        QuickEntryOverlayScreen(
                            viewModel = expenseEntryViewModel,
                            onDismiss = { isQuickEntryOpen = false },
                            onSaveSuccess = {
                                triggerSaveHaptic()
                                isQuickEntryOpen = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun triggerSaveHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(70)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }

    override fun onResume() {
        super.onResume()
        hasOverlayPermissionState.value = checkOverlayPermission()
        ShakeSensorService.startService(this)
    }
}
