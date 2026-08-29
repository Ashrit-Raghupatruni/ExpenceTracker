package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_profile")
data class FinancialProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "monthly_income_cents", defaultValue = "0")
    val monthlyIncomeCents: Long = 0L,
    @ColumnInfo(name = "savings_target_cents", defaultValue = "0")
    val savingsTargetCents: Long = 0L,
    @ColumnInfo(name = "billing_cycle_day", defaultValue = "1")
    val billingCycleDay: Int = 1,
    @ColumnInfo(name = "tier", defaultValue = "'FREE'")
    val tier: String = "FREE",
    @ColumnInfo(name = "safety_score", defaultValue = "80")
    val safetyScore: Int = 80,
    @ColumnInfo(name = "monthly_spending_limit_cents", defaultValue = "0")
    val monthlySpendingLimitCents: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
