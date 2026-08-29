package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.data.repository.CategoryRepository
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.Expense
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.model.TransactionSource
import com.shakeexpense.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        categoryName: String,
        categoryColorHex: String,
        amountCents: Long,
        type: TransactionType = TransactionType.DEBIT,
        source: TransactionSource = TransactionSource.MANUAL_SHAKE,
        customName: String? = null,
        bankRef: String? = null,
        userId: String = AppDatabase.DEFAULT_USER_ID,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Long> {
        if (amountCents <= 0) {
            return Result.failure(IllegalArgumentException("Expense amount must be greater than zero."))
        }
        if (categoryId <= 0) {
            return Result.failure(IllegalArgumentException("A valid category must be selected."))
        }

        val trimmedCustomName = if (categoryName.equals("Others", ignoreCase = true)) {
            customName?.trim()?.ifBlank { null }
        } else {
            customName?.trim()
        }

        val expense = Expense(
            id = 0,
            uuid = UUID.randomUUID().toString(),
            userId = userId,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorHex = categoryColorHex,
            amountCents = amountCents,
            type = type,
            source = source,
            customName = trimmedCustomName,
            bankRef = bankRef,
            timestamp = timestamp,
            updatedAt = timestamp,
            syncStatus = SyncStatus.PENDING
        )

        val insertedId = expenseRepository.saveExpense(expense)
        return Result.success(insertedId)
    }
}

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<CategoryEntity>> =
        categoryRepository.getAllCategories()
}

class GetExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<ExpenseRecordItem>> =
        expenseRepository.getSpreadsheetStream()
}
