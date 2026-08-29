package com.shakeexpense.app.data.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

data class ExpenseWithDetailsRaw(
    @ColumnInfo(name = "expense_id") val expenseId: Long,
    @ColumnInfo(name = "expense_uuid") val expenseUuid: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "transaction_type") val transactionType: String,
    @ColumnInfo(name = "transaction_source") val transactionSource: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "custom_name") val customName: String?,
    @ColumnInfo(name = "bank_ref") val bankRef: String?,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_color") val categoryColor: String,
    @ColumnInfo(name = "sync_status") val syncStatus: String
)

data class MemberExpenseSummaryRaw(
    @ColumnInfo(name = "user_id") val userId: String?,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "total_debit_cents") val totalDebitCents: Long,
    @ColumnInfo(name = "total_credit_cents") val totalCreditCents: Long,
    @ColumnInfo(name = "transaction_count") val transactionCount: Int
)

data class CategoryBreakdownRaw(
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_color") val categoryColor: String,
    @ColumnInfo(name = "category_total_cents") val categoryTotalCents: Long,
    @ColumnInfo(name = "transaction_count") val transactionCount: Int
)

@Dao
interface ExpenseDao {

    @Query(
        """
        SELECT 
            e.id AS expense_id,
            e.uuid AS expense_uuid,
            e.user_id AS user_id,
            COALESCE(m.name, 'You') AS user_name,
            e.amount_cents AS amount_cents,
            e.type AS transaction_type,
            e.source AS transaction_source,
            e.timestamp AS timestamp,
            e.custom_name AS custom_name,
            e.bank_ref AS bank_ref,
            COALESCE(c.id, e.category_id) AS category_id,
            COALESCE(c.name, 'Expense') AS category_name,
            COALESCE(c.color_hex, '#F59E0B') AS category_color,
            e.sync_status AS sync_status
        FROM expenses e
        LEFT JOIN categories c ON e.category_id = c.id
        LEFT JOIN family_members m ON e.user_id = m.id
        ORDER BY e.timestamp DESC
        """
    )
    fun getSpreadsheetStream(): Flow<List<ExpenseWithDetailsRaw>>

    @Query(
        """
        SELECT 
            e.id AS expense_id,
            e.uuid AS expense_uuid,
            e.user_id AS user_id,
            COALESCE(m.name, 'You') AS user_name,
            e.amount_cents AS amount_cents,
            e.type AS transaction_type,
            e.source AS transaction_source,
            e.timestamp AS timestamp,
            e.custom_name AS custom_name,
            e.bank_ref AS bank_ref,
            COALESCE(c.id, e.category_id) AS category_id,
            COALESCE(c.name, 'Expense') AS category_name,
            COALESCE(c.color_hex, '#F59E0B') AS category_color,
            e.sync_status AS sync_status
        FROM expenses e
        LEFT JOIN categories c ON e.category_id = c.id
        LEFT JOIN family_members m ON e.user_id = m.id
        ORDER BY e.timestamp DESC
        """
    )
    suspend fun getSpreadsheetStreamSync(): List<ExpenseWithDetailsRaw>

    @Query(
        """
        SELECT 
            e.id AS expense_id,
            e.uuid AS expense_uuid,
            e.user_id AS user_id,
            COALESCE(m.name, 'You') AS user_name,
            e.amount_cents AS amount_cents,
            e.type AS transaction_type,
            e.source AS transaction_source,
            e.timestamp AS timestamp,
            e.custom_name AS custom_name,
            e.bank_ref AS bank_ref,
            COALESCE(c.id, e.category_id) AS category_id,
            COALESCE(c.name, 'Expense') AS category_name,
            COALESCE(c.color_hex, '#F59E0B') AS category_color,
            e.sync_status AS sync_status
        FROM expenses e
        LEFT JOIN categories c ON e.category_id = c.id
        LEFT JOIN family_members m ON e.user_id = m.id
        WHERE e.user_id = :targetUserId
        ORDER BY e.timestamp DESC
        """
    )
    fun getExpensesByUserId(targetUserId: String): Flow<List<ExpenseWithDetailsRaw>>

    @Query(
        """
        SELECT 
            e.user_id AS user_id,
            COALESCE(m.name, 'You') AS user_name,
            COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount_cents ELSE 0 END), 0) AS total_debit_cents,
            COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount_cents ELSE 0 END), 0) AS total_credit_cents,
            COUNT(e.id) AS transaction_count
        FROM expenses e
        LEFT JOIN family_members m ON e.user_id = m.id
        WHERE e.user_id = :targetUserId
        """
    )
    fun getMemberSpendingSummary(targetUserId: String): Flow<MemberExpenseSummaryRaw?>

    @Query(
        """
        SELECT 
            e.user_id AS user_id,
            COALESCE(m.name, 'You') AS user_name,
            COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount_cents ELSE 0 END), 0) AS total_debit_cents,
            COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount_cents ELSE 0 END), 0) AS total_credit_cents,
            COUNT(e.id) AS transaction_count
        FROM expenses e
        LEFT JOIN family_members m ON e.user_id = m.id
        WHERE e.user_id = :targetUserId
        """
    )
    suspend fun getMemberSpendingSummarySync(targetUserId: String): MemberExpenseSummaryRaw?

    @Query(
        """
        SELECT 
            COALESCE(c.id, e.category_id) AS category_id,
            COALESCE(c.name, 'Expense') AS category_name,
            COALESCE(c.color_hex, '#F59E0B') AS category_color,
            COALESCE(SUM(e.amount_cents), 0) AS category_total_cents,
            COUNT(e.id) AS transaction_count
        FROM expenses e
        LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.user_id = :targetUserId AND e.type = 'DEBIT'
        GROUP BY COALESCE(c.id, e.category_id)
        ORDER BY category_total_cents DESC
        """
    )
    fun getMemberCategoryBreakdown(targetUserId: String): Flow<List<CategoryBreakdownRaw>>

    @Query(
        """
        SELECT 
            COALESCE(c.id, e.category_id) AS category_id,
            COALESCE(c.name, 'Expense') AS category_name,
            COALESCE(c.color_hex, '#F59E0B') AS category_color,
            COALESCE(SUM(e.amount_cents), 0) AS category_total_cents,
            COUNT(e.id) AS transaction_count
        FROM expenses e
        LEFT JOIN categories c ON e.category_id = c.id
        WHERE e.type = 'DEBIT'
        GROUP BY COALESCE(c.id, e.category_id)
        ORDER BY category_total_cents DESC
        """
    )
    fun getAllCategoryBreakdown(): Flow<List<CategoryBreakdownRaw>>

    @Query(
        """
        SELECT * FROM expenses 
        WHERE sync_status = 'PENDING' 
        ORDER BY updated_at ASC
        """
    )
    suspend fun getPendingSyncExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE uuid = :uuid LIMIT 1")
    suspend fun getExpenseByUuid(uuid: String): ExpenseEntity?

    @Query(
        """
        SELECT * FROM expenses
        WHERE (:userId IS NULL OR user_id = :userId)
          AND amount_cents = :amountCents
          AND timestamp BETWEEN :minTimestamp AND :maxTimestamp
          AND (:bankRef IS NULL OR bank_ref = :bankRef)
        LIMIT 1
        """
    )
    suspend fun findPotentialDuplicate(
        amountCents: Long,
        minTimestamp: Long,
        maxTimestamp: Long,
        bankRef: String?,
        userId: String? = null
    ): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET sync_status = :syncStatus, updated_at = :updatedAt WHERE uuid IN (:uuids)")
    suspend fun updateSyncStatus(uuids: List<String>, syncStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE uuid = :uuid")
    suspend fun deleteExpenseByUuid(uuid: String)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount_cents ELSE 0 END), 0) FROM expenses WHERE timestamp >= :sinceTimestamp")
    fun getTotalSpentSince(sinceTimestamp: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount_cents ELSE 0 END), 0) FROM expenses")
    fun getAllTimeTotalSpent(): Flow<Long>

    @Query("SELECT COUNT(*) FROM expenses WHERE sync_status = 'PENDING'")
    fun getPendingSyncCountFlow(): Flow<Int>

    @Query("SELECT * FROM expenses WHERE user_id = :userId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getExpensesInTimeRange(userId: String, startTime: Long, endTime: Long): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE uuid IN (:uuids)")
    suspend fun deleteExpensesByUuids(uuids: List<String>): Int

    @Query("UPDATE expenses SET user_id = :toUserId WHERE user_id = :fromUserId")
    suspend fun migrateUserExpenses(fromUserId: String, toUserId: String)
}
