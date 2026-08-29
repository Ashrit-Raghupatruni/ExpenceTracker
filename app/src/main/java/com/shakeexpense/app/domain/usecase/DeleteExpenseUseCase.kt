package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.sync.SyncEngine

class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val syncEngine: SyncEngine? = null
) {
    suspend operator fun invoke(uuid: String): Result<Unit> {
        if (uuid.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid transaction ID"))
        }
        expenseRepository.deleteExpenseByUuid(uuid)
        syncEngine?.deleteExpenses(listOf(uuid))
        return Result.success(Unit)
    }
}

