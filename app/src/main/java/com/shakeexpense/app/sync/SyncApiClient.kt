package com.shakeexpense.app.sync

import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.sync.model.FamilyMemberDto
import com.shakeexpense.app.sync.model.JoinFamilyResult
import com.shakeexpense.app.sync.model.SyncExpenseDto
import com.shakeexpense.app.sync.model.SyncPullResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

interface SyncApiClient {
    suspend fun createFamily(familyName: String, creatorUserId: String, creatorName: String = "Parent"): Result<FamilyGroupDto>
    suspend fun joinFamilyByInviteCode(inviteCode: String, member: FamilyMemberDto): JoinFamilyResult
    suspend fun pushExpenses(familyId: String, deviceId: String, expenses: List<SyncExpenseDto>): Result<Int>
    suspend fun pullFamilyExpenses(familyId: String, sinceTimestamp: Long): Result<SyncPullResponse>
    suspend fun pullUserExpenses(userId: String, sinceTimestamp: Long = 0L): Result<List<SyncExpenseDto>>
    suspend fun fetchUserFamilyMembership(userId: String): Result<Pair<FamilyGroupDto?, List<FamilyMemberDto>>>
    suspend fun fetchFamilyMembers(familyId: String): Result<List<FamilyMemberDto>>
    suspend fun deleteExpenses(uuids: List<String>): Result<Unit>
    suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long): Result<Unit>
}

object SyncApiClientProvider {
    private var instance: SyncApiClient? = null

    fun get(): SyncApiClient {
        return instance ?: try {
            FirestoreSyncApiClient().also { instance = it }
        } catch (e: Throwable) {
            android.util.Log.w("SyncApiClientProvider", "Firebase Firestore unconfigured, using local fallback client: ${e.message}")
            BackendSyncApiClient().also { instance = it }
        }
    }

    fun isCloudSyncSupported(): Boolean {
        return get() is FirestoreSyncApiClient
    }

    fun setCustom(client: SyncApiClient) {
        instance = client
    }
}

class BackendSyncApiClient(
    private val backendBaseUrl: String = "https://api.shakeexpense.internal/v1"
) : SyncApiClient {

    // In-memory backend registry simulating remote cloud store for zero-server fallback
    private val remoteFamilies = mutableMapOf<String, FamilyGroupDto>()
    private val remoteInviteCodes = mutableMapOf<String, String>() // InviteCode -> FamilyId
    private val remoteMembers = mutableMapOf<String, MutableList<FamilyMemberDto>>()
    private val remoteExpenses = mutableMapOf<String, MutableList<SyncExpenseDto>>() // FamilyId -> Expenses

    override suspend fun createFamily(familyName: String, creatorUserId: String, creatorName: String): Result<FamilyGroupDto> =
        withContext(Dispatchers.IO) {
            try {
                val familyId = "fam_${UUID.randomUUID().toString().take(8)}"
                val randomDigits = (1000..9999).random()
                val inviteCode = "SHK-$randomDigits"

                val family = FamilyGroupDto(
                    familyId = familyId,
                    familyName = familyName,
                    creatorUserId = creatorUserId,
                    inviteCode = inviteCode,
                    createdAt = System.currentTimeMillis()
                )

                remoteFamilies[familyId] = family
                remoteInviteCodes[inviteCode.uppercase()] = familyId
                remoteMembers[familyId] = mutableListOf(
                    FamilyMemberDto(
                        userId = creatorUserId,
                        familyId = familyId,
                        name = creatorName.ifBlank { "Parent" },
                        role = "PARENT",
                        deviceId = "device_creator"
                    )
                )
                remoteExpenses[familyId] = mutableListOf()

                Result.success(family)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun createFamilyWithCustomCode(family: FamilyGroupDto, creatorName: String = "Parent") {
        remoteFamilies[family.familyId] = family
        remoteInviteCodes[family.inviteCode.uppercase()] = family.familyId
        remoteMembers[family.familyId] = mutableListOf(
            FamilyMemberDto(
                userId = family.creatorUserId,
                familyId = family.familyId,
                name = creatorName.ifBlank { "Parent" },
                role = "PARENT",
                deviceId = "device_creator"
            )
        )
        remoteExpenses[family.familyId] = mutableListOf()
    }

    override suspend fun joinFamilyByInviteCode(inviteCode: String, member: FamilyMemberDto): JoinFamilyResult =
        withContext(Dispatchers.IO) {
            val normalizedCode = inviteCode.trim().uppercase()
            val familyId = remoteInviteCodes[normalizedCode]
                ?: return@withContext JoinFamilyResult(
                    isSuccess = false,
                    familyId = null,
                    familyName = null,
                    members = emptyList(),
                    errorMessage = "Invalid or expired Invite Code ($normalizedCode)"
                )

            val family = remoteFamilies[familyId]
            val membersList = remoteMembers.getOrPut(familyId) { mutableListOf() }
            if (membersList.none { it.userId == member.userId }) {
                membersList.add(member.copy(familyId = familyId))
            }

            JoinFamilyResult(
                isSuccess = true,
                familyId = familyId,
                familyName = family?.familyName ?: "Family Group",
                members = membersList.toList(),
                errorMessage = null
            )
        }

    override suspend fun pushExpenses(
        familyId: String,
        deviceId: String,
        expenses: List<SyncExpenseDto>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val expenseStore = remoteExpenses.getOrPut(familyId) { mutableListOf() }
            var updatedCount = 0

            for (incoming in expenses) {
                val existingIndex = expenseStore.indexOfFirst { it.uuid == incoming.uuid }
                if (existingIndex >= 0) {
                    val existing = expenseStore[existingIndex]
                    if (incoming.updatedAt > existing.updatedAt) {
                        expenseStore[existingIndex] = incoming
                        updatedCount++
                    }
                } else {
                    expenseStore.add(incoming)
                    updatedCount++
                }
            }

            Result.success(expenses.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pullFamilyExpenses(
        familyId: String,
        sinceTimestamp: Long
    ): Result<SyncPullResponse> = withContext(Dispatchers.IO) {
        try {
            val expenseStore = remoteExpenses[familyId] ?: emptyList()
            val filtered = expenseStore.filter { it.updatedAt >= sinceTimestamp }
            Result.success(
                SyncPullResponse(
                    serverTimestamp = System.currentTimeMillis(),
                    expenses = filtered
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pullUserExpenses(
        userId: String,
        sinceTimestamp: Long
    ): Result<List<SyncExpenseDto>> = withContext(Dispatchers.IO) {
        val allExpenses = remoteExpenses.values.flatten()
        val userExpenses = allExpenses.filter { it.userId == userId && it.updatedAt >= sinceTimestamp }
        Result.success(userExpenses)
    }

    override suspend fun fetchUserFamilyMembership(
        userId: String
    ): Result<Pair<FamilyGroupDto?, List<FamilyMemberDto>>> = withContext(Dispatchers.IO) {
        for ((famId, members) in remoteMembers) {
            if (members.any { it.userId == userId }) {
                val group = remoteFamilies[famId]
                return@withContext Result.success(Pair(group, members.toList()))
            }
        }
        Result.success(Pair(null, emptyList()))
    }

    override suspend fun fetchFamilyMembers(familyId: String): Result<List<FamilyMemberDto>> = withContext(Dispatchers.IO) {
        val members = remoteMembers[familyId] ?: emptyList()
        Result.success(members.toList())
    }

    override suspend fun deleteExpenses(uuids: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        val set = uuids.toSet()
        for ((_, expList) in remoteExpenses) {
            expList.removeAll { it.uuid in set }
        }
        Result.success(Unit)
    }

    override suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val current = remoteFamilies[familyId]
        if (current != null) {
            remoteFamilies[familyId] = current.copy(monthlySpendingLimitCents = limitCents)
        }
        Result.success(Unit)
    }
}
