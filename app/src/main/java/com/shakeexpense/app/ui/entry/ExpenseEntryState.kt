package com.shakeexpense.app.ui.entry

import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.domain.model.TransactionType

data class ExpenseEntryState(
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val customCategoryName: String = "",
    val amountInput: String = "",
    val transactionType: TransactionType = TransactionType.DEBIT,
    val isOthersSelected: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val amountCents: Long
        get() {
            if (amountInput.isBlank()) return 0L
            return try {
                val cleaned = amountInput.trim()
                if (cleaned.contains(".")) {
                    val parts = cleaned.split(".")
                    val whole = parts[0].toLongOrNull() ?: 0L
                    val decimalStr = parts[1].padEnd(2, '0').take(2)
                    val decimal = decimalStr.toLongOrNull() ?: 0L
                    (whole * 100) + decimal
                } else {
                    (cleaned.toLongOrNull() ?: 0L) * 100
                }
            } catch (e: Exception) {
                0L
            }
        }

    val isSaveEnabled: Boolean
        get() = selectedCategory != null && amountCents > 0 && !isSaving
}
