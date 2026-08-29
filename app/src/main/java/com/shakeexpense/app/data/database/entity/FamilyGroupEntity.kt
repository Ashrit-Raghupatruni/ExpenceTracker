package com.shakeexpense.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_groups")
data class FamilyGroupEntity(
    @PrimaryKey
    val familyId: String,
    @ColumnInfo(name = "family_name")
    val familyName: String,
    @ColumnInfo(name = "invite_code")
    val inviteCode: String,
    @ColumnInfo(name = "owner_uid")
    val ownerUid: String,
    @ColumnInfo(name = "monthly_spending_limit_cents", defaultValue = "0")
    val monthlySpendingLimitCents: Long = 0L,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
