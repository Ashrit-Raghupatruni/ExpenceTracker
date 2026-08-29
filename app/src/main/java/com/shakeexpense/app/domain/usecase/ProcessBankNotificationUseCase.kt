package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.Expense
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.model.TransactionSource
import com.shakeexpense.app.notification.ParsedBankTransaction
import java.util.UUID

sealed class ProcessBankNotificationResult {
    data class Saved(val expense: Expense) : ProcessBankNotificationResult()
    data class DuplicateIgnored(val existingUuid: String) : ProcessBankNotificationResult()
    data class Ignored(val reason: String) : ProcessBankNotificationResult()
}

class ProcessBankNotificationUseCase(
    private val expenseRepository: ExpenseRepository
) {
    companion object {
        const val DEDUPLICATION_WINDOW_MS = 180_000L // 3 minutes (180s)
    }

    suspend fun checkIsDuplicate(transaction: ParsedBankTransaction, userId: String? = null): Boolean {
        val minTimestamp = transaction.timestamp - DEDUPLICATION_WINDOW_MS
        val maxTimestamp = transaction.timestamp + DEDUPLICATION_WINDOW_MS

        val duplicate = expenseRepository.findPotentialDuplicate(
            amountCents = transaction.amountCents,
            minTimestamp = minTimestamp,
            maxTimestamp = maxTimestamp,
            bankRef = null,
            userId = userId
        )
        return duplicate != null
    }

    suspend operator fun invoke(
        transaction: ParsedBankTransaction,
        userId: String = AppDatabase.DEFAULT_USER_ID,
        categoryId: Long? = null
    ): ProcessBankNotificationResult {
        if (transaction.amountCents <= 0) {
            return ProcessBankNotificationResult.Ignored("Invalid transaction amount")
        }

        val minTimestamp = transaction.timestamp - DEDUPLICATION_WINDOW_MS
        val maxTimestamp = transaction.timestamp + DEDUPLICATION_WINDOW_MS

        val duplicate = expenseRepository.findPotentialDuplicate(
            amountCents = transaction.amountCents,
            minTimestamp = minTimestamp,
            maxTimestamp = maxTimestamp,
            bankRef = null,
            userId = userId
        )
        if (duplicate != null) {
            return ProcessBankNotificationResult.DuplicateIgnored(duplicate.uuid)
        }

        val targetCategoryId = categoryId ?: transaction.suggestedCategoryId

        // Insert new expense
        val newExpense = Expense(
            uuid = UUID.randomUUID().toString(),
            userId = userId,
            categoryId = targetCategoryId,
            categoryName = "",
            categoryColorHex = "",
            customName = transaction.merchantOrPayee,
            amountCents = transaction.amountCents,
            type = transaction.type,
            source = TransactionSource.BANK_NOTIF,
            syncStatus = SyncStatus.PENDING,
            timestamp = transaction.timestamp
        )

        expenseRepository.saveExpense(newExpense)
        return ProcessBankNotificationResult.Saved(newExpense)
    }
}
