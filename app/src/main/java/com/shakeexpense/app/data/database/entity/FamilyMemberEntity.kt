package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "family_id")
    val familyId: String,
    val name: String,
    val role: String, // 'PARENT' | 'CHILD'
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "privacy_mode", defaultValue = "'FULL_SHARED'")
    val privacyMode: String = "FULL_SHARED",
    @ColumnInfo(name = "share_transactions", defaultValue = "1")
    val shareTransactions: Boolean = true,
    @ColumnInfo(name = "share_monthly_total", defaultValue = "1")
    val shareMonthlyTotal: Boolean = true,
    @ColumnInfo(name = "share_category_totals", defaultValue = "1")
    val shareCategoryTotals: Boolean = true,
    @ColumnInfo(name = "receive_family_alerts", defaultValue = "1")
    val receiveFamilyAlerts: Boolean = true,
    @ColumnInfo(name = "is_exit_requested", defaultValue = "0")
    val isExitRequested: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
