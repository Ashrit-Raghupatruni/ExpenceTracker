package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.domain.model.Expense
import com.shakeexpense.app.domain.model.TransactionType

class EditExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        uuid: String,
        amountCents: Long,
        categoryId: Long,
        type: TransactionType,
        customName: String? = null
    ): Result<Unit> {
        if (amountCents <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }

        val existing = expenseRepository.getExpenseByUuid(uuid)
            ?: return Result.failure(IllegalStateException("Expense not found"))

        val updated = existing.copy(
            amountCents = amountCents,
            categoryId = categoryId,
            type = type,
            customName = customName,
            updatedAt = System.currentTimeMillis()
        )

        expenseRepository.updateExpense(updated)
        return Result.success(Unit)
    }
}
