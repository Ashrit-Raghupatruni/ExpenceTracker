package com.shakeexpense.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialProfileDao {

    @Query("SELECT * FROM financial_profile WHERE user_id = :userId LIMIT 1")
    fun getProfileFlow(userId: String): Flow<FinancialProfileEntity?>

    @Query("SELECT * FROM financial_profile WHERE user_id = :userId LIMIT 1")
    suspend fun getProfile(userId: String): FinancialProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: FinancialProfileEntity)

    @Query("UPDATE financial_profile SET monthly_income_cents = :incomeCents, savings_target_cents = :savingsTargetCents, billing_cycle_day = :cycleDay, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateBudgetParameters(userId: String, incomeCents: Long, savingsTargetCents: Long, cycleDay: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE financial_profile SET safety_score = :score, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateSafetyScore(userId: String, score: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE financial_profile SET tier = :tier, updated_at = :updatedAt WHERE user_id = :userId")
    suspend fun updateTier(userId: String, tier: String, updatedAt: Long = System.currentTimeMillis())
}
