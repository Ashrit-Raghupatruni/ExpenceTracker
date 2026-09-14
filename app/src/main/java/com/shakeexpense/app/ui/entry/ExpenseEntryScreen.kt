package com.shakeexpense.app.ui.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakeexpense.app.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseEntryContent(
    viewModel: ExpenseEntryViewModel,
    modifier: Modifier = Modifier,
    onSaveCompleted: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Transaction Type Selector (DEBIT / CREDIT)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TransactionTypeChip(
                label = "DEBIT (Expense)",
                isSelected = state.transactionType == TransactionType.DEBIT,
                selectedColor = Color(0xFFE11D48),
                onClick = { viewModel.onTransactionTypeChanged(TransactionType.DEBIT) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TransactionTypeChip(
                label = "CREDIT (Income)",
                isSelected = state.transactionType == TransactionType.CREDIT,
                selectedColor = Color(0xFF059669),
                onClick = { viewModel.onTransactionTypeChanged(TransactionType.CREDIT) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Chips
        Text(
            text = "SELECT CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.categories.forEach { category ->
                val isSelected = state.selectedCategory?.id == category.id
                val color = parseHexColor(category.colorHex)

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onCategorySelected(category) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .height(10.dp)
                                    .width(10.dp)
                            )
                            Text(
                                text = category.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.25f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Detail Notes Text Input (e.g. food -> idly, transport -> auto)
        val notePlaceholder = when (state.selectedCategory?.name?.lowercase()) {
            "food" -> "e.g. Idly, Lunch, Coffee, Snacks"
            "transport" -> "e.g. Auto, Metro, Cab, Fuel"
            "groceries" -> "e.g. Vegetables, Milk, Supermarket"
            "bills" -> "e.g. Electricity, Wifi, Rent"
            "shopping" -> "e.g. Clothes, Shoes, Electronics"
            "entertainment" -> "e.g. Movie, Concert, Games"
            "others" -> "e.g. Books, Gym, Gift"
            else -> "e.g. Idly, Auto, Notes (Optional)"
        }

        OutlinedTextField(
            value = state.noteInput,
            onValueChange = { viewModel.onNoteChanged(it) },
            label = { Text(if (state.isOthersSelected) "Category / Note" else "Note (Optional)") },
            placeholder = { Text(notePlaceholder) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hero Amount Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (state.amountInput.isEmpty()) "₹ 0.00" else "₹ ${state.amountInput}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.transactionType == TransactionType.DEBIT) Color(0xFFE11D48) else Color(0xFF059669)
                )
            }
        }

        // Error message if any
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Numeric Keypad
        NumericKeypad(
            onDigitClick = { viewModel.onDigitPressed(it) },
            onDecimalClick = { viewModel.onDecimalPressed() },
            onBackspaceClick = { viewModel.onBackspacePressed() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.saveExpense {
                    onSaveCompleted()
                }
            },
            enabled = state.isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.isSaving) "SAVING..." else "SAVE (✓)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) selectedColor else Color.Transparent,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
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
                    KeypadButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "." -> onDecimalClick()
                                "⌫" -> onBackspaceClick()
                                else -> onDigitClick(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(2.2f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (key == "⌫") {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = key,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun parseHexColor(colorHex: String): Color {
    return try {
        val cleanHex = colorHex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0xFF000000)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF64748B)
    }
}
