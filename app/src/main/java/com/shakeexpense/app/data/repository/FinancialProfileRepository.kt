package com.shakeexpense.app.data.repository

import com.shakeexpense.app.data.database.dao.FinancialProfileDao
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import kotlinx.coroutines.flow.Flow

interface FinancialProfileRepository {
    fun getProfileFlow(userId: String): Flow<FinancialProfileEntity?>
    suspend fun getProfile(userId: String): FinancialProfileEntity?
    suspend fun saveProfile(profile: FinancialProfileEntity)
    suspend fun updateBudgetParameters(userId: String, incomeCents: Long, savingsTargetCents: Long, cycleDay: Int)
    suspend fun updateSafetyScore(userId: String, score: Int)
    suspend fun updateTier(userId: String, tier: String)
    suspend fun updateSpendingLimit(userId: String, limitCents: Long)
}

class FinancialProfileRepositoryImpl(
    private val financialProfileDao: FinancialProfileDao
) : FinancialProfileRepository {

    override fun getProfileFlow(userId: String): Flow<FinancialProfileEntity?> {
        return financialProfileDao.getProfileFlow(userId)
    }

    override suspend fun getProfile(userId: String): FinancialProfileEntity? {
        return financialProfileDao.getProfile(userId)
    }

    override suspend fun saveProfile(profile: FinancialProfileEntity) {
        financialProfileDao.insertOrUpdateProfile(profile)
    }

    override suspend fun updateBudgetParameters(
        userId: String,
        incomeCents: Long,
        savingsTargetCents: Long,
        cycleDay: Int
    ) {
        val existing = financialProfileDao.getProfile(userId)
        val updated = (existing ?: FinancialProfileEntity(
            userId = userId,
            tier = "FREE",
            safetyScore = 0
        )).copy(
            monthlyIncomeCents = incomeCents,
            savingsTargetCents = savingsTargetCents,
            billingCycleDay = cycleDay,
            updatedAt = System.currentTimeMillis()
        )
        financialProfileDao.insertOrUpdateProfile(updated)

        // Ensure default_local_user is also synced for offline/standalone mode
        if (userId != "default_local_user") {
            financialProfileDao.insertOrUpdateProfile(
                updated.copy(userId = "default_local_user")
            )
        }
    }

    override suspend fun updateSafetyScore(userId: String, score: Int) {
        val existing = financialProfileDao.getProfile(userId)
        val updated = (existing ?: FinancialProfileEntity(
            userId = userId,
            tier = "FREE",
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            billingCycleDay = 1
        )).copy(safetyScore = score, updatedAt = System.currentTimeMillis())
        financialProfileDao.insertOrUpdateProfile(updated)
    }

    override suspend fun updateTier(userId: String, tier: String) {
        val existing = financialProfileDao.getProfile(userId)
        val updated = (existing ?: FinancialProfileEntity(
            userId = userId,
            safetyScore = 0,
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            billingCycleDay = 1
        )).copy(tier = tier, updatedAt = System.currentTimeMillis())
        financialProfileDao.insertOrUpdateProfile(updated)

        if (userId != "default_local_user") {
            financialProfileDao.insertOrUpdateProfile(
                updated.copy(userId = "default_local_user")
            )
        }
    }

    override suspend fun updateSpendingLimit(userId: String, limitCents: Long) {
        val existing = financialProfileDao.getProfile(userId)
        val updated = (existing ?: FinancialProfileEntity(
            userId = userId,
            tier = "FREE",
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            billingCycleDay = 1
        )).copy(monthlySpendingLimitCents = limitCents, updatedAt = System.currentTimeMillis())
        financialProfileDao.insertOrUpdateProfile(updated)

        if (userId != "default_local_user") {
            financialProfileDao.insertOrUpdateProfile(
                updated.copy(userId = "default_local_user")
            )
        }
    }
}
