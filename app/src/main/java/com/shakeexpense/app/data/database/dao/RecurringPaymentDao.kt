package com.shakeexpense.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringPaymentDao {

    @Query("SELECT * FROM recurring_payments WHERE user_id = :userId AND is_active = 1 ORDER BY next_due_timestamp ASC")
    fun getActiveRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>>

    @Query("SELECT * FROM recurring_payments WHERE user_id = :userId AND is_active = 1 ORDER BY next_due_timestamp ASC")
    suspend fun getActiveRecurringPayments(userId: String): List<RecurringPaymentEntity>

    @Query("SELECT * FROM recurring_payments WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>>

    @Query("SELECT * FROM recurring_payments WHERE id = :id LIMIT 1")
    suspend fun getRecurringPaymentById(id: Long): RecurringPaymentEntity?

    @Query("SELECT * FROM recurring_payments WHERE user_id = :userId AND (name LIKE '%' || :query || '%' OR merchant_key LIKE '%' || :query || '%') LIMIT 1")
    suspend fun findMatchingRecurring(userId: String, query: String): RecurringPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringPayment(payment: RecurringPaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringPayments(payments: List<RecurringPaymentEntity>)

    @Update
    suspend fun updateRecurringPayment(payment: RecurringPaymentEntity)

    @Query("UPDATE recurring_payments SET is_active = :isActive WHERE id = :id")
    suspend fun setPaymentActiveState(id: Long, isActive: Boolean)

    @Query("DELETE FROM recurring_payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recurring_payments WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
