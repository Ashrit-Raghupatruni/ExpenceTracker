package com.shakeexpense.app.sync.model

data class FamilyGroupDto(
    val familyId: String,
    val familyName: String,
    val creatorUserId: String,
    val inviteCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val monthlySpendingLimitCents: Long = 0L
)

data class FamilyMemberDto(
    val userId: String,
    val familyId: String,
    val name: String,
    val role: String,
    val deviceId: String
)

data class SyncExpenseDto(
    val uuid: String,
    val userId: String,
    val categoryId: Long,
    val amountCents: Long,
    val type: String,
    val source: String,
    val customName: String? = null,
    val bankRef: String? = null,
    val timestamp: Long,
    val updatedAt: Long
)

data class SyncPushRequest(
    val familyId: String,
    val deviceId: String,
    val expenses: List<SyncExpenseDto>
)

data class SyncPullResponse(
    val serverTimestamp: Long,
    val expenses: List<SyncExpenseDto>
)

data class JoinFamilyResult(
    val isSuccess: Boolean,
    val familyId: String?,
    val familyName: String?,
    val members: List<FamilyMemberDto>,
    val errorMessage: String? = null
)
