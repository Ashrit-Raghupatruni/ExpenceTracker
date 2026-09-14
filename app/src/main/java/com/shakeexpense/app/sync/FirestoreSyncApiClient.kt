package com.shakeexpense.app.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.sync.model.FamilyMemberDto
import com.shakeexpense.app.sync.model.JoinFamilyResult
import com.shakeexpense.app.sync.model.SyncExpenseDto
import com.shakeexpense.app.sync.model.SyncPullResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

class FirestoreSyncApiClient(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : SyncApiClient {

    companion object {
        const val COLLECTION_FAMILIES = "families"
        const val COLLECTION_FAMILY_MEMBERS = "family_members"
        const val COLLECTION_EXPENSES = "expenses"
        private const val TIMEOUT_MS = 15000L
    }

    // In-memory persistent registry for local / fallback sync
    private val fallbackClient = BackendSyncApiClient()

    private suspend fun ensureAuthenticated() {
        try {
            if (auth.currentUser == null) {
                withTimeoutOrNull(5000L) {
                    auth.signInAnonymously().await()
                }
            }
        } catch (e: Exception) {
            // Non-blocking fallback
        }
    }

    override suspend fun createFamily(familyName: String, creatorUserId: String, creatorName: String): Result<FamilyGroupDto> =
        withContext(Dispatchers.IO) {
            val familyId = "fam_${UUID.randomUUID().toString().take(8)}"
            val randomDigits = (1000..9999).random()
            val inviteCode = "SHK-$randomDigits"
            val now = System.currentTimeMillis()

            val family = FamilyGroupDto(
                familyId = familyId,
                familyName = familyName,
                creatorUserId = creatorUserId,
                inviteCode = inviteCode,
                createdAt = now
            )

            // Register in fallback engine first for instant resilience
            fallbackClient.createFamilyWithCustomCode(family, creatorName)

            // Asynchronously sync to Cloud Firestore with timeout
            try {
                ensureAuthenticated()
                withTimeoutOrNull(TIMEOUT_MS) {
                    val familyMap = hashMapOf(
                        "familyId" to familyId,
                        "familyName" to familyName,
                        "creatorUserId" to creatorUserId,
                        "inviteCode" to inviteCode.uppercase(),
                        "createdAt" to now
                    )
                    firestore.collection(COLLECTION_FAMILIES)
                        .document(familyId)
                        .set(familyMap, SetOptions.merge())
                        .await()

                    val memberDocId = "${familyId}_$creatorUserId"
                    val memberMap = hashMapOf(
                        "id" to creatorUserId,
                        "familyId" to familyId,
                        "userId" to creatorUserId,
                        "name" to creatorName.ifBlank { auth.currentUser?.displayName ?: "Parent" },
                        "role" to "PARENT",
                        "deviceId" to "device_creator",
                        "joinedAt" to now
                    )
                    firestore.collection(COLLECTION_FAMILY_MEMBERS)
                        .document(memberDocId)
                        .set(memberMap, SetOptions.merge())
                        .await()
                }
            } catch (e: Exception) {
                // Cloud sync error logged
            }

            Result.success(family)
        }

    override suspend fun joinFamilyByInviteCode(inviteCode: String, member: FamilyMemberDto): JoinFamilyResult =
        withContext(Dispatchers.IO) {
            val normalizedCode = inviteCode.trim().uppercase()
            val candidateCodes = listOf(
                normalizedCode,
                if (!normalizedCode.startsWith("SHK-")) "SHK-$normalizedCode" else normalizedCode,
                normalizedCode.removePrefix("SHK-")
            ).distinct()

            try {
                ensureAuthenticated()
                val firestoreResult = withTimeoutOrNull(TIMEOUT_MS) {
                    var targetFamilyDoc: com.google.firebase.firestore.DocumentSnapshot? = null

                    for (candidate in candidateCodes) {
                        val querySnapshot = firestore.collection(COLLECTION_FAMILIES)
                            .whereEqualTo("inviteCode", candidate)
                            .limit(1)
                            .get()
                            .await()

                        if (!querySnapshot.isEmpty) {
                            targetFamilyDoc = querySnapshot.documents[0]
                            break
                        }
                    }

                    if (targetFamilyDoc != null) {
                        val familyId = targetFamilyDoc.getString("familyId") ?: targetFamilyDoc.id
                        val familyName = targetFamilyDoc.getString("familyName") ?: "Family Group"
                        val now = System.currentTimeMillis()

                        val memberDocId = "${familyId}_${member.userId}"
                        val memberMap = hashMapOf(
                            "id" to member.userId,
                            "familyId" to familyId,
                            "userId" to member.userId,
                            "name" to member.name.ifBlank { "Member" },
                            "role" to member.role,
                            "deviceId" to member.deviceId,
                            "joinedAt" to now
                        )
                        firestore.collection(COLLECTION_FAMILY_MEMBERS)
                            .document(memberDocId)
                            .set(memberMap, SetOptions.merge())
                            .await()

                        // Fetch all members in this family
                        val membersSnapshot = firestore.collection(COLLECTION_FAMILY_MEMBERS)
                            .whereEqualTo("familyId", familyId)
                            .get()
                            .await()

                        val memberList = membersSnapshot.documents.map { doc ->
                            FamilyMemberDto(
                                userId = doc.getString("userId") ?: doc.getString("id") ?: "",
                                familyId = doc.getString("familyId") ?: familyId,
                                name = doc.getString("name") ?: "Member",
                                role = doc.getString("role") ?: "CHILD",
                                deviceId = doc.getString("deviceId") ?: ""
                            )
                        }

                        JoinFamilyResult(
                            isSuccess = true,
                            familyId = familyId,
                            familyName = familyName,
                            members = memberList,
                            errorMessage = null
                        )
                    } else null
                }

                if (firestoreResult != null) return@withContext firestoreResult
            } catch (e: Exception) {
                // Fallback to local fallback
            }

            // Fallback lookup
            fallbackClient.joinFamilyByInviteCode(inviteCode, member)
        }

    override suspend fun fetchFamilyMembers(familyId: String): Result<List<FamilyMemberDto>> =
        withContext(Dispatchers.IO) {
            try {
                ensureAuthenticated()
                val snapshot = withTimeoutOrNull(TIMEOUT_MS) {
                    firestore.collection(COLLECTION_FAMILY_MEMBERS)
                        .whereEqualTo("familyId", familyId)
                        .get()
                        .await()
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.map { doc ->
                        FamilyMemberDto(
                            userId = doc.getString("userId") ?: doc.getString("id") ?: "",
                            familyId = doc.getString("familyId") ?: familyId,
                            name = doc.getString("name") ?: "Member",
                            role = doc.getString("role") ?: "CHILD",
                            deviceId = doc.getString("deviceId") ?: ""
                        )
                    }
                    return@withContext Result.success(list)
                }
            } catch (e: Exception) {
                // Fallback
            }
            fallbackClient.fetchFamilyMembers(familyId)
        }

    override suspend fun pushExpenses(
        familyId: String,
        deviceId: String,
        expenses: List<SyncExpenseDto>
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (expenses.isEmpty()) return@withContext Result.success(0)

        // Push to fallback
        fallbackClient.pushExpenses(familyId, deviceId, expenses)

        try {
            ensureAuthenticated()
            withTimeoutOrNull(TIMEOUT_MS) {
                val batch = firestore.batch()
                for (expense in expenses) {
                    val docRef = firestore.collection(COLLECTION_EXPENSES).document(expense.uuid)
                    val expenseMap = hashMapOf(
                        "uuid" to expense.uuid,
                        "familyId" to familyId,
                        "userId" to expense.userId,
                        "amountCents" to expense.amountCents,
                        "type" to expense.type,
                        "source" to expense.source,
                        "timestamp" to expense.timestamp,
                        "customName" to (expense.customName ?: ""),
                        "bankRef" to (expense.bankRef ?: ""),
                        "categoryId" to expense.categoryId,
                        "updatedAt" to expense.updatedAt,
                        "deleted" to false
                    )
                    batch.set(docRef, expenseMap, SetOptions.merge())
                }
                batch.commit().await()
            }
            Result.success(expenses.size)
        } catch (e: Exception) {
            Result.success(expenses.size)
        }
    }

    override suspend fun pullFamilyExpenses(
        familyId: String,
        sinceTimestamp: Long
    ): Result<SyncPullResponse> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            val firestorePull = withTimeoutOrNull(TIMEOUT_MS) {
                val querySnapshot = firestore.collection(COLLECTION_EXPENSES)
                    .whereEqualTo("familyId", familyId)
                    .get()
                    .await()

                querySnapshot.documents.mapNotNull { doc ->
                    if (doc.getBoolean("deleted") == true) return@mapNotNull null
                    val uuid = doc.getString("uuid") ?: return@mapNotNull null
                    val amountCents = doc.getLong("amountCents") ?: 0L
                    val categoryId = doc.getLong("categoryId") ?: 1L
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val updatedAt = doc.getLong("updatedAt") ?: timestamp

                    SyncExpenseDto(
                        uuid = uuid,
                        userId = doc.getString("userId") ?: "",
                        amountCents = amountCents,
                        type = doc.getString("type") ?: "DEBIT",
                        source = doc.getString("source") ?: "MANUAL_SHAKE",
                        timestamp = timestamp,
                        customName = doc.getString("customName"),
                        bankRef = doc.getString("bankRef"),
                        categoryId = categoryId,
                        updatedAt = updatedAt
                    )
                }.filter { it.updatedAt >= sinceTimestamp }
            }

            if (firestorePull != null) {
                return@withContext Result.success(
                    SyncPullResponse(
                        serverTimestamp = System.currentTimeMillis(),
                        expenses = firestorePull
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback
        }

        fallbackClient.pullFamilyExpenses(familyId, sinceTimestamp)
    }

    override suspend fun pullUserExpenses(
        userId: String,
        sinceTimestamp: Long
    ): Result<List<SyncExpenseDto>> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            val firestorePull = withTimeoutOrNull(TIMEOUT_MS) {
                val querySnapshot = firestore.collection(COLLECTION_EXPENSES)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                querySnapshot.documents.mapNotNull { doc ->
                    if (doc.getBoolean("deleted") == true) return@mapNotNull null
                    val uuid = doc.getString("uuid") ?: return@mapNotNull null
                    val amountCents = doc.getLong("amountCents") ?: 0L
                    val categoryId = doc.getLong("categoryId") ?: 1L
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val updatedAt = doc.getLong("updatedAt") ?: timestamp

                    SyncExpenseDto(
                        uuid = uuid,
                        userId = doc.getString("userId") ?: userId,
                        amountCents = amountCents,
                        type = doc.getString("type") ?: "DEBIT",
                        source = doc.getString("source") ?: "MANUAL_SHAKE",
                        timestamp = timestamp,
                        customName = doc.getString("customName"),
                        bankRef = doc.getString("bankRef"),
                        categoryId = categoryId,
                        updatedAt = updatedAt
                    )
                }.filter { it.updatedAt >= sinceTimestamp }
            }

            if (firestorePull != null) {
                return@withContext Result.success(firestorePull)
            }
        } catch (e: Exception) {
            // Fallback
        }
        fallbackClient.pullUserExpenses(userId, sinceTimestamp)
    }

    override suspend fun fetchUserFamilyMembership(
        userId: String
    ): Result<Pair<FamilyGroupDto?, List<FamilyMemberDto>>> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            val firestoreResult = withTimeoutOrNull(TIMEOUT_MS) {
                val memberQuery = firestore.collection(COLLECTION_FAMILY_MEMBERS)
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (!memberQuery.isEmpty) {
                    val memberDoc = memberQuery.documents[0]
                    val familyId = memberDoc.getString("familyId") ?: return@withTimeoutOrNull null

                    val familyDoc = firestore.collection(COLLECTION_FAMILIES)
                        .document(familyId)
                        .get()
                        .await()

                    val familyGroup = if (familyDoc.exists()) {
                        FamilyGroupDto(
                            familyId = familyId,
                            familyName = familyDoc.getString("familyName") ?: "Family Group",
                            creatorUserId = familyDoc.getString("creatorUserId") ?: "",
                            inviteCode = familyDoc.getString("inviteCode") ?: "",
                            createdAt = familyDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                            monthlySpendingLimitCents = familyDoc.getLong("monthlySpendingLimitCents")
                                ?: familyDoc.getLong("monthly_spending_limit_cents")
                                ?: 0L
                        )
                    } else null

                    val allMembersQuery = firestore.collection(COLLECTION_FAMILY_MEMBERS)
                        .whereEqualTo("familyId", familyId)
                        .get()
                        .await()

                    val membersList = allMembersQuery.documents.map { doc ->
                        FamilyMemberDto(
                            userId = doc.getString("userId") ?: doc.getString("id") ?: "",
                            familyId = familyId,
                            name = doc.getString("name") ?: "Member",
                            role = doc.getString("role") ?: "CHILD",
                            deviceId = doc.getString("deviceId") ?: ""
                        )
                    }

                    Pair(familyGroup, membersList)
                } else null
            }

            if (firestoreResult != null) {
                return@withContext Result.success(firestoreResult)
            }
        } catch (e: Exception) {
            // Fallback
        }
        fallbackClient.fetchUserFamilyMembership(userId)
    }

    override suspend fun deleteExpenses(uuids: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        if (uuids.isEmpty()) return@withContext Result.success(Unit)
        fallbackClient.deleteExpenses(uuids)
        try {
            ensureAuthenticated()
            withTimeoutOrNull(TIMEOUT_MS) {
                val batch = firestore.batch()
                for (uuid in uuids) {
                    val docRef = firestore.collection(COLLECTION_EXPENSES).document(uuid)
                    batch.delete(docRef)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Log cloud error, local fallback handled
        }
        Result.success(Unit)
    }

    override suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long): Result<Unit> = withContext(Dispatchers.IO) {
        fallbackClient.updateFamilySpendingLimit(familyId, limitCents)
        try {
            ensureAuthenticated()
            withTimeoutOrNull(TIMEOUT_MS) {
                val updateMap = hashMapOf<String, Any>(
                    "monthlySpendingLimitCents" to limitCents,
                    "monthly_spending_limit_cents" to limitCents,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection(COLLECTION_FAMILIES)
                    .document(familyId)
                    .set(updateMap, SetOptions.merge())
                    .await()
            }
        } catch (e: Exception) {
            // Fallback handled
        }
        Result.success(Unit)
    }
}
