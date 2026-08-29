package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_budgets")
data class FamilyBudgetEntity(
    @PrimaryKey
    val id: String, // e.g. "budget_${familyId}_${categoryName}"
    @ColumnInfo(name = "family_id")
    val familyId: String,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "limit_cents")
    val limitCents: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
