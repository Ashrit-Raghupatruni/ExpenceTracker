package com.shakeexpense.app.data.repository

import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.Expense
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.MemberSpendingSummary
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.pipeline.TransactionEvent
import com.shakeexpense.app.domain.pipeline.TransactionEventPipeline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ExpenseRepository {
    suspend fun saveExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun getExpenseByUuid(uuid: String): Expense?
    fun getSpreadsheetStream(): Flow<List<ExpenseRecordItem>>
    fun getExpensesByUserId(userId: String): Flow<List<ExpenseRecordItem>>
    fun getMemberSpendingSummary(userId: String, role: FamilyRole = FamilyRole.CHILD): Flow<MemberSpendingSummary?>
    fun getMemberCategoryBreakdown(userId: String): Flow<List<CategorySubtotal>>
    fun getAllCategoryBreakdown(): Flow<List<CategorySubtotal>>
    suspend fun getPendingSyncExpenses(): List<ExpenseEntity>
    fun getPendingSyncCount(): Flow<Int>
    suspend fun updateSyncStatus(uuids: List<String>, status: SyncStatus)
    suspend fun deleteExpenseByUuid(uuid: String)
    suspend fun findPotentialDuplicate(
        amountCents: Long,
        minTimestamp: Long,
        maxTimestamp: Long,
        bankRef: String?,
        userId: String? = null
    ): ExpenseEntity?
    suspend fun getExpensesInTimeRange(userId: String, startTime: Long, endTime: Long): List<ExpenseEntity>
    suspend fun deleteExpensesByUuids(uuids: List<String>): Int
    suspend fun migrateUserExpenses(fromUserId: String, toUserId: String)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>
}

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val pipeline: TransactionEventPipeline? = null
) : ExpenseRepository {

    override suspend fun saveExpense(expense: Expense): Long {
        val entity = ExpenseEntity(
            id = expense.id,
            uuid = expense.uuid,
            userId = expense.userId,
            categoryId = expense.categoryId,
            amountCents = expense.amountCents,
            type = expense.type.name,
            source = expense.source.name,
            customName = expense.customName,
            bankRef = expense.bankRef,
            timestamp = expense.timestamp,
            updatedAt = expense.updatedAt,
            syncStatus = expense.syncStatus.name
        )
        val id = expenseDao.insertExpense(entity)
        pipeline?.onTransactionEvent(TransactionEvent.Created(entity.copy(id = id)))
        return id
    }

    override suspend fun updateExpense(expense: Expense) {
        val entity = ExpenseEntity(
            id = expense.id,
            uuid = expense.uuid,
            userId = expense.userId,
            categoryId = expense.categoryId,
            amountCents = expense.amountCents,
            type = expense.type.name,
            source = expense.source.name,
            customName = expense.customName,
            bankRef = expense.bankRef,
            timestamp = expense.timestamp,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING.name
        )
        expenseDao.updateExpense(entity)
        pipeline?.onTransactionEvent(TransactionEvent.Updated(entity))
    }

    override suspend fun getExpenseByUuid(uuid: String): Expense? {
        val entity = expenseDao.getExpenseByUuid(uuid) ?: return null
        return Expense(
            id = entity.id,
            uuid = entity.uuid,
            userId = entity.userId,
            categoryId = entity.categoryId,
            categoryName = "",
            categoryColorHex = "",
            amountCents = entity.amountCents,
            type = if (entity.type.equals("CREDIT", ignoreCase = true)) com.shakeexpense.app.domain.model.TransactionType.CREDIT else com.shakeexpense.app.domain.model.TransactionType.DEBIT,
            source = com.shakeexpense.app.domain.model.TransactionSource.valueOf(entity.source),
            customName = entity.customName,
            bankRef = entity.bankRef,
            timestamp = entity.timestamp,
            updatedAt = entity.updatedAt,
            syncStatus = SyncStatus.valueOf(entity.syncStatus)
        )
    }

    override fun getSpreadsheetStream(): Flow<List<ExpenseRecordItem>> {
        return expenseDao.getSpreadsheetStream().map { rawList ->
            rawList.map { raw ->
                ExpenseRecordItem(
                    expenseId = raw.expenseId,
                    expenseUuid = raw.expenseUuid,
                    userId = raw.userId,
                    userName = raw.userName,
                    amountCents = raw.amountCents,
                    transactionType = raw.transactionType,
                    transactionSource = raw.transactionSource,
                    timestamp = raw.timestamp,
                    customName = raw.customName,
                    bankRef = raw.bankRef,
                    categoryId = raw.categoryId,
                    categoryName = raw.categoryName,
                    categoryColor = raw.categoryColor,
                    syncStatus = raw.syncStatus
                )
            }
        }
    }

    override fun getExpensesByUserId(userId: String): Flow<List<ExpenseRecordItem>> {
        return expenseDao.getExpensesByUserId(userId).map { rawList ->
            rawList.map { raw ->
                ExpenseRecordItem(
                    expenseId = raw.expenseId,
                    expenseUuid = raw.expenseUuid,
                    userId = raw.userId,
                    userName = raw.userName,
                    amountCents = raw.amountCents,
                    transactionType = raw.transactionType,
                    transactionSource = raw.transactionSource,
                    timestamp = raw.timestamp,
                    customName = raw.customName,
                    bankRef = raw.bankRef,
                    categoryId = raw.categoryId,
                    categoryName = raw.categoryName,
                    categoryColor = raw.categoryColor,
                    syncStatus = raw.syncStatus
                )
            }
        }
    }

    override fun getMemberSpendingSummary(userId: String, role: FamilyRole): Flow<MemberSpendingSummary?> {
        return expenseDao.getMemberSpendingSummary(userId).map { raw ->
            raw?.let {
                MemberSpendingSummary(
                    userId = it.userId ?: userId,
                    userName = it.userName ?: "Member",
                    role = role,
                    totalDebitCents = it.totalDebitCents,
                    totalCreditCents = it.totalCreditCents,
                    transactionCount = it.transactionCount
                )
            } ?: MemberSpendingSummary(
                userId = userId,
                userName = "Member",
                role = role,
                totalDebitCents = 0L,
                totalCreditCents = 0L,
                transactionCount = 0
            )
        }
    }

    override fun getMemberCategoryBreakdown(userId: String): Flow<List<CategorySubtotal>> {
        return expenseDao.getMemberCategoryBreakdown(userId).map { rawList ->
            rawList.map { raw ->
                CategorySubtotal(
                    categoryId = raw.categoryId,
                    categoryName = raw.categoryName,
                    colorHex = raw.categoryColor,
                    totalCents = raw.categoryTotalCents,
                    count = raw.transactionCount
                )
            }
        }
    }

    override fun getAllCategoryBreakdown(): Flow<List<CategorySubtotal>> {
        return expenseDao.getAllCategoryBreakdown().map { rawList ->
            rawList.map { raw ->
                CategorySubtotal(
                    categoryId = raw.categoryId,
                    categoryName = raw.categoryName,
                    colorHex = raw.categoryColor,
                    totalCents = raw.categoryTotalCents,
                    count = raw.transactionCount
                )
            }
        }
    }

    override suspend fun getPendingSyncExpenses(): List<ExpenseEntity> =
        expenseDao.getPendingSyncExpenses()

    override fun getPendingSyncCount(): Flow<Int> =
        expenseDao.getPendingSyncCountFlow()

    override suspend fun updateSyncStatus(uuids: List<String>, status: SyncStatus) =
        expenseDao.updateSyncStatus(uuids, status.name)

    override suspend fun deleteExpenseByUuid(uuid: String) {
        val existing = expenseDao.getExpenseByUuid(uuid)
        expenseDao.deleteExpenseByUuid(uuid)
        if (existing != null) {
            pipeline?.onTransactionEvent(TransactionEvent.Deleted(uuid, existing.userId))
        }
    }

    override suspend fun findPotentialDuplicate(
        amountCents: Long,
        minTimestamp: Long,
        maxTimestamp: Long,
        bankRef: String?,
        userId: String?
    ): ExpenseEntity? =
        expenseDao.findPotentialDuplicate(amountCents, minTimestamp, maxTimestamp, bankRef, userId)

    override suspend fun getExpensesInTimeRange(userId: String, startTime: Long, endTime: Long): List<ExpenseEntity> =
        expenseDao.getExpensesInTimeRange(userId, startTime, endTime)

    override suspend fun deleteExpensesByUuids(uuids: List<String>): Int {
        val count = expenseDao.deleteExpensesByUuids(uuids)
        for (u in uuids) {
            pipeline?.onTransactionEvent(TransactionEvent.Deleted(u, "default_local_user"))
        }
        return count
    }

    override suspend fun migrateUserExpenses(fromUserId: String, toUserId: String) {
        expenseDao.migrateUserExpenses(fromUserId, toUserId)
    }

    override suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long> {
        val ids = expenseDao.insertExpenses(expenses)
        for (e in expenses) {
            pipeline?.onTransactionEvent(TransactionEvent.Created(e))
        }
        return ids
    }
}
