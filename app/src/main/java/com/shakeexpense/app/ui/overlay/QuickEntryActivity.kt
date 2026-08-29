package com.shakeexpense.app.ui.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shakeexpense.app.ShakeExpenseApp
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.CategoryRepositoryImpl
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.AddExpenseUseCase
import com.shakeexpense.app.domain.usecase.GetCategoriesUseCase
import com.shakeexpense.app.ui.entry.ExpenseEntryViewModel
import com.shakeexpense.app.ui.theme.AppThemeMode
import com.shakeexpense.app.ui.theme.ShakeExpenseTheme
import com.shakeexpense.app.ui.theme.ThemePreferences
import com.shakeexpense.app.ui.tracker.MainActivity

class QuickEntryActivity : ComponentActivity() {

    private val viewModel: ExpenseEntryViewModel by viewModels {
        val app = application as ShakeExpenseApp
        val database = app.database
        val categoryRepo = CategoryRepositoryImpl(database.categoryDao())
        val expenseRepo = app.expenseRepository
        val authRepo = AuthRepository(this)
        ExpenseEntryViewModel.provideFactory(
            GetCategoriesUseCase(categoryRepo),
            AddExpenseUseCase(expenseRepo),
            authRepo
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Check for prefilled bank notification transaction data
        val prefillAmountCents = intent.getLongExtra("prefill_amount_cents", 0L)
        if (prefillAmountCents > 0L) {
            val typeStr = intent.getStringExtra("prefill_type") ?: "DEBIT"
            val type = if (typeStr.equals("CREDIT", ignoreCase = true)) TransactionType.CREDIT else TransactionType.DEBIT
            val categoryId = intent.getLongExtra("prefill_category_id", 1L)
            val customName = intent.getStringExtra("prefill_custom_name")
            viewModel.prefillTransaction(prefillAmountCents, type, categoryId, customName)
        }

        val themePreferences = ThemePreferences.getInstance(this)
        setContent {
            val currentThemeMode by themePreferences.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)
            ShakeExpenseTheme(themeMode = currentThemeMode) {
                QuickEntryOverlayScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        finish()
                    },
                    onSaveSuccess = {
                        triggerSaveHaptic()
                        android.widget.Toast.makeText(this@QuickEntryActivity, "Expense saved!", android.widget.Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
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
}
