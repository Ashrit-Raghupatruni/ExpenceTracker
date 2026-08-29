package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_payments",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["name"])
    ]
)
data class RecurringPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    @ColumnInfo(name = "cadence", defaultValue = "'MONTHLY'")
    val cadence: String = "MONTHLY",
    @ColumnInfo(name = "category_id", defaultValue = "4")
    val categoryId: Long = 4L,
    @ColumnInfo(name = "last_charged_timestamp")
    val lastChargedTimestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "next_due_timestamp")
    val nextDueTimestamp: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L),
    @ColumnInfo(name = "is_auto_detected", defaultValue = "1")
    val isAutoDetected: Boolean = true,
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
    @ColumnInfo(name = "merchant_key")
    val merchantKey: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
