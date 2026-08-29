package com.shakeexpense.app.data.repository

import com.shakeexpense.app.data.database.dao.RecurringPaymentDao
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import kotlinx.coroutines.flow.Flow

interface RecurringPaymentRepository {
    fun getActiveRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>>
    suspend fun getActiveRecurringPayments(userId: String): List<RecurringPaymentEntity>
    fun getAllRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>>
    suspend fun findMatchingRecurring(userId: String, query: String): RecurringPaymentEntity?
    suspend fun insertRecurringPayment(payment: RecurringPaymentEntity): Long
    suspend fun insertRecurringPayments(payments: List<RecurringPaymentEntity>)
    suspend fun updateRecurringPayment(payment: RecurringPaymentEntity)
    suspend fun setPaymentActiveState(id: Long, isActive: Boolean)
    suspend fun deleteById(id: Long)
}

class RecurringPaymentRepositoryImpl(
    private val recurringPaymentDao: RecurringPaymentDao
) : RecurringPaymentRepository {

    override fun getActiveRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>> {
        return recurringPaymentDao.getActiveRecurringPaymentsFlow(userId)
    }

    override suspend fun getActiveRecurringPayments(userId: String): List<RecurringPaymentEntity> {
        return recurringPaymentDao.getActiveRecurringPayments(userId)
    }

    override fun getAllRecurringPaymentsFlow(userId: String): Flow<List<RecurringPaymentEntity>> {
        return recurringPaymentDao.getAllRecurringPaymentsFlow(userId)
    }

    override suspend fun findMatchingRecurring(userId: String, query: String): RecurringPaymentEntity? {
        return recurringPaymentDao.findMatchingRecurring(userId, query)
    }

    override suspend fun insertRecurringPayment(payment: RecurringPaymentEntity): Long {
        return recurringPaymentDao.insertRecurringPayment(payment)
    }

    override suspend fun insertRecurringPayments(payments: List<RecurringPaymentEntity>) {
        recurringPaymentDao.insertRecurringPayments(payments)
    }

    override suspend fun updateRecurringPayment(payment: RecurringPaymentEntity) {
        recurringPaymentDao.updateRecurringPayment(payment)
    }

    override suspend fun setPaymentActiveState(id: Long, isActive: Boolean) {
        recurringPaymentDao.setPaymentActiveState(id, isActive)
    }

    override suspend fun deleteById(id: Long) {
        recurringPaymentDao.deleteById(id)
    }
}
