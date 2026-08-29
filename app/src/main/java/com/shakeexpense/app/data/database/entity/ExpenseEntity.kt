package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["user_id"]),
        Index(value = ["category_id"]),
        Index(value = ["sync_status"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    @ColumnInfo(name = "type", defaultValue = "'DEBIT'")
    val type: String = "DEBIT",
    @ColumnInfo(name = "source", defaultValue = "'MANUAL_SHAKE'")
    val source: String = "MANUAL_SHAKE",
    @ColumnInfo(name = "custom_name")
    val customName: String? = null,
    @ColumnInfo(name = "bank_ref")
    val bankRef: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status", defaultValue = "'PENDING'")
    val syncStatus: String = "PENDING"
)
