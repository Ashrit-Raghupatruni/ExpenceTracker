package com.shakeexpense.app.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.TransactionSource
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.ui.design.*
import com.shakeexpense.app.ui.entry.ExpenseEntryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickEntryOverlayScreen(
    viewModel: ExpenseEntryViewModel,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isDark = isAppDarkTheme()

    // High-Opacity Dim Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // High-Contrast Solid Frosted Input Panel
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercept clicks inside card
                ),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            borderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with HUD Title & Close Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "QUICK EXPENSE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    NeuIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close overlay",
                        onClick = onDismiss,
                        size = 32.dp
                    )
                }

                // Transaction Type Selector (DEBIT / CREDIT)
                NeuSegmentedControl(
                    items = listOf("DEBIT", "CREDIT"),
                    selectedIndex = if (state.transactionType == TransactionType.DEBIT) 0 else 1,
                    onSelectIndex = { index ->
                        viewModel.onTransactionTypeChanged(if (index == 0) TransactionType.DEBIT else TransactionType.CREDIT)
                    }
                )

                // Category Chips (Neutral with single primary accent selection)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.categories.forEach { category ->
                        val isSelected = state.selectedCategory?.id == category.id
                        NeuPill(
                            label = category.name,
                            isSelected = isSelected,
                            onClick = { viewModel.onCategorySelected(category) },
                            icon = getCategoryVectorIcon(category.name),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Detail Notes Text Input
                val notePlaceholder = when (state.selectedCategory?.name?.lowercase()) {
                    "food" -> "e.g. Lunch, Coffee, Snacks"
                    "transport" -> "e.g. Metro, Cab, Fuel"
                    "shopping" -> "e.g. Groceries, Clothes"
                    "bills" -> "e.g. Electricity, Wifi, Rent"
                    "entertainment" -> "e.g. Movie, Streaming"
                    else -> "Note (Optional)"
                }

                GlassTextField(
                    value = state.noteInput,
                    onValueChange = { viewModel.onNoteChanged(it) },
                    placeholder = notePlaceholder
                )

                // Hero Amount Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) ShakeDesignTokens.GlassBorderDark else ShakeDesignTokens.GlassBorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.amountInput.isEmpty()) "₹ 0.00" else "₹ ${state.amountInput}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FinancialFormatter.TabularFontFamily,
                            color = if (state.transactionType == TransactionType.DEBIT) ShakeDesignTokens.ExceededRed else ShakeDesignTokens.HealthyGreen
                        )
                    }
                }

                // Error message
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = ShakeDesignTokens.ExceededRed,
                        fontSize = 11.sp
                    )
                }

                // Numeric Keypad
                OverlayKeypad(
                    onDigitClick = { viewModel.onDigitPressed(it) },
                    onDecimalClick = { viewModel.onDecimalPressed() },
                    onBackspaceClick = { viewModel.onBackspacePressed() }
                )

                // Save Action Button (with single check icon and clean label)
                NeuButton(
                    onClick = {
                        if (state.isSaveEnabled) {
                            viewModel.saveExpense(source = TransactionSource.MANUAL_SHAKE) {
                                onSaveSuccess()
                            }
                        }
                    },
                    isPrimary = state.isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (state.isSaveEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isSaving) "SAVING..." else "SAVE EXPENSE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (state.isSaveEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OverlayKeypad(
    onDigitClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    val isAction = key == "⌫"
                    NeuKeypadButton(
                        symbol = key,
                        onClick = {
                            when (key) {
                                "⌫" -> onBackspaceClick()
                                "." -> onDecimalClick()
                                else -> onDigitClick(key)
                            }
                        },
                        isAccent = isAction,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
