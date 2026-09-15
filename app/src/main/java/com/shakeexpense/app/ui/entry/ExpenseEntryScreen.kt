package com.shakeexpense.app.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.ui.design.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseEntryContent(
    viewModel: ExpenseEntryViewModel,
    modifier: Modifier = Modifier,
    onSaveCompleted: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val isDark = isAppDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Transaction Type Selector (DEBIT / CREDIT)
        NeuSegmentedControl(
            items = listOf("DEBIT (Expense)", "CREDIT (Income)"),
            selectedIndex = if (state.transactionType == TransactionType.DEBIT) 0 else 1,
            onSelectIndex = { index ->
                viewModel.onTransactionTypeChanged(if (index == 0) TransactionType.DEBIT else TransactionType.CREDIT)
            }
        )

        // Category Chips
        Text(
            text = "SELECT CATEGORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

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
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.amountInput.isEmpty()) "₹ 0.00" else "₹ ${state.amountInput}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FinancialFormatter.TabularFontFamily,
                    color = if (state.transactionType == TransactionType.DEBIT) ShakeDesignTokens.ExceededRed else ShakeDesignTokens.HealthyGreen
                )
            }
        }

        // Error message if any
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = ShakeDesignTokens.ExceededRed,
                fontSize = 11.sp
            )
        }

        // Numeric Keypad
        NumericKeypad(
            onDigitClick = { viewModel.onDigitPressed(it) },
            onDecimalClick = { viewModel.onDecimalPressed() },
            onBackspaceClick = { viewModel.onBackspacePressed() }
        )

        // Save Button with single check icon
        NeuButton(
            onClick = {
                if (state.isSaveEnabled) {
                    viewModel.saveExpense {
                        onSaveCompleted()
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

@Composable
fun TransactionTypeChip(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    NeuPill(
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        color = selectedColor
    )
}

@Composable
fun NumericKeypad(
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
