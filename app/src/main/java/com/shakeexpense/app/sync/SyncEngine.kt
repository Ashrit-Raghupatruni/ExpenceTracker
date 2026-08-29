package com.shakeexpense.app.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.sync.model.SyncExpenseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncEngine(
    private val expenseRepository: ExpenseRepository,
    private val syncApiClient: SyncApiClient = BackendSyncApiClient(),
    private val context: Context? = null,
    private val familyRepository: FamilyRepository? = null
) {
    companion object {
        private const val TAG = "SyncEngine"
        const val PERIODIC_SYNC_WORK_NAME = "shake_expense_periodic_sync"
        const val ONE_TIME_SYNC_WORK_NAME = "shake_expense_one_time_sync"
        const val DEFAULT_FAMILY_ID = "family_default_01"

        /**
         * Last-Write-Wins (LWW) Conflict Resolution Helper
         * Returns true if the incoming remote entity should overwrite the existing local entity.
         */
        fun shouldRemoteOverwriteLocal(local: ExpenseEntity, remote: ExpenseEntity): Boolean {
            return remote.updatedAt > local.updatedAt
        }
    }

    private var lastSyncTimestamp: Long = 0L

    private val prefs by lazy {
        context?.getSharedPreferences("shake_expense_sync_prefs", Context.MODE_PRIVATE)
    }

    private val deletedUuids = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    fun isDeleted(uuid: String): Boolean {
        if (deletedUuids.contains(uuid)) return true
        val stored = prefs?.getStringSet("deleted_uuids", emptySet()) ?: emptySet()
        return stored.contains(uuid)
    }

    /**
     * Executes real two-way online backend synchronization:
     * 1. Drains and pushes local PENDING expenses to backend API
     * 2. Pulls remote family expenses from backend API
     * 3. Merges remote expenses into local Room SQLite using LWW conflict resolution
     */
    suspend fun performOnlineTwoWaySync(familyId: String = DEFAULT_FAMILY_ID, deviceId: String = "device_local"): Int {
        var pushedCount = 0
        var pulledCount = 0

        // Auto-migrate any unauthenticated local records if user is logged in
        if (context != null) {
            try {
                val authRepo = AuthRepository(context)
                val currentUid = authRepo.getCurrentProfile().userId
                if (currentUid != AppDatabase.DEFAULT_USER_ID) {
                    expenseRepository.migrateUserExpenses(AppDatabase.DEFAULT_USER_ID, currentUid)
                }
            } catch (e: Exception) {
                // Ignore in testing environments
            }
        }

        val activeGroup = try {
            familyRepository?.getActiveFamilyGroup()?.let { flow ->
                flow.firstOrNull()
            }
        } catch (e: Exception) { null }

        val targetFamilyId = if (familyId != DEFAULT_FAMILY_ID) {
            familyId
        } else {
            activeGroup?.familyId ?: DEFAULT_FAMILY_ID
        }

        // 1. Pull and sync updated family members from Firestore
        if (familyRepository != null && targetFamilyId != DEFAULT_FAMILY_ID) {
            try {
                val membersResult = syncApiClient.fetchFamilyMembers(targetFamilyId)
                if (membersResult.isSuccess) {
                    val remoteMembers = membersResult.getOrNull() ?: emptyList()
                    for (m in remoteMembers) {
                        val role = try {
                            FamilyRole.valueOf(m.role.uppercase())
                        } catch (e: Exception) {
                            FamilyRole.CHILD
                        }
                        familyRepository.addFamilyMember(
                            FamilyMember(
                                id = m.userId,
                                familyId = targetFamilyId,
                                name = m.name,
                                role = role,
                                deviceId = m.deviceId
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync family members: ${e.message}")
            }
        }

        // 2. Push local pending changes
        val pendingExpenses = expenseRepository.getPendingSyncExpenses()
        if (pendingExpenses.isNotEmpty()) {
            val syncDtos = pendingExpenses.map { entity ->
                SyncExpenseDto(
                    uuid = entity.uuid,
                    userId = entity.userId,
                    categoryId = entity.categoryId,
                    amountCents = entity.amountCents,
                    type = entity.type,
                    source = entity.source,
                    customName = entity.customName,
                    bankRef = entity.bankRef,
                    timestamp = entity.timestamp,
                    updatedAt = entity.updatedAt
                )
            }

            val pushResult = syncApiClient.pushExpenses(targetFamilyId, deviceId, syncDtos)
            if (pushResult.isSuccess) {
                val uuids = pendingExpenses.map { it.uuid }
                expenseRepository.updateSyncStatus(uuids, SyncStatus.SYNCED)
                pushedCount += uuids.size
                Log.d(TAG, "Successfully pushed ${uuids.size} local expenses to backend")
            }
        }

        // 3. Pull remote family changes
        val pullResult = syncApiClient.pullFamilyExpenses(targetFamilyId, lastSyncTimestamp)
        if (pullResult.isSuccess) {
            val remoteData = pullResult.getOrNull()
            if (remoteData != null) {
                lastSyncTimestamp = remoteData.serverTimestamp
                for (remoteDto in remoteData.expenses) {
                    if (isDeleted(remoteDto.uuid)) {
                        continue
                    }
                    val local = expenseRepository.getExpenseByUuid(remoteDto.uuid)

                    val remoteEntity = ExpenseEntity(
                        id = local?.id ?: 0L,
                        uuid = remoteDto.uuid,
                        userId = remoteDto.userId,
                        categoryId = remoteDto.categoryId,
                        amountCents = remoteDto.amountCents,
                        type = remoteDto.type,
                        source = remoteDto.source,
                        customName = remoteDto.customName,
                        bankRef = remoteDto.bankRef,
                        timestamp = remoteDto.timestamp,
                        updatedAt = remoteDto.updatedAt,
                        syncStatus = SyncStatus.SYNCED.name
                    )

                    if (local == null) {
                        // Insert new remote family member expense with original author's userId
                        expenseRepository.insertExpenses(listOf(remoteEntity))
                    } else if (remoteDto.updatedAt > local.updatedAt) {
                        // Overwrite with newer remote state, strictly retaining remote userId
                        expenseRepository.insertExpenses(listOf(remoteEntity))
                    }
                }
                pulledCount += remoteData.expenses.size
            }
        }

        return if (pushedCount > 0) pushedCount else pulledCount
    }

    /**
     * Complete reconciliation on login / initial sync:
     * 1. Auto-migrates any anonymous / default_local_user expenses to authenticated UID
     * 2. Restores remote family membership into Room
     * 3. Pulls all cloud expenses belonging to this UID and reconciles into Room SQLite
     */
    suspend fun reconcileAndPullCloudData(currentUserId: String? = null): Int = withContext(Dispatchers.IO) {
        var restoredCount = 0
        try {
            val targetUid = currentUserId
                ?: (if (context != null) AuthRepository(context).getCurrentProfile().userId else null)
                ?: return@withContext 0

            if (targetUid == AppDatabase.DEFAULT_USER_ID) return@withContext 0

            // 1. Auto-migrate unauthenticated local records to current authenticated UID
            expenseRepository.migrateUserExpenses(AppDatabase.DEFAULT_USER_ID, targetUid)

            // 2. Fetch and restore user's remote family membership
            val familyResult = syncApiClient.fetchUserFamilyMembership(targetUid)
            if (familyResult.isSuccess) {
                val (familyGroup, members) = familyResult.getOrNull() ?: Pair(null, emptyList())
                if (familyGroup != null && familyRepository != null) {
                    familyRepository.saveFamilyGroup(familyGroup)
                }
                if (members.isNotEmpty() && familyRepository != null) {
                    val familyMembers = members.map { dto ->
                        FamilyMember(
                            id = dto.userId,
                            familyId = dto.familyId,
                            name = dto.name,
                            role = if (dto.role.equals("PARENT", ignoreCase = true)) FamilyRole.PARENT else FamilyRole.CHILD,
                            deviceId = dto.deviceId,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    familyRepository.setFamilyMembers(familyMembers)
                }
            }

            // 3. Pull user's own cloud expenses from Firestore
            val userExpensesResult = syncApiClient.pullUserExpenses(targetUid, 0L)
            if (userExpensesResult.isSuccess) {
                val cloudExpenses = userExpensesResult.getOrNull() ?: emptyList()
                for (remoteDto in cloudExpenses) {
                    if (isDeleted(remoteDto.uuid)) {
                        continue
                    }
                    val local = expenseRepository.getExpenseByUuid(remoteDto.uuid)
                    if (local == null) {
                        val entity = ExpenseEntity(
                            uuid = remoteDto.uuid,
                            userId = remoteDto.userId.ifBlank { targetUid },
                            categoryId = remoteDto.categoryId,
                            amountCents = remoteDto.amountCents,
                            type = remoteDto.type,
                            source = remoteDto.source,
                            customName = remoteDto.customName,
                            bankRef = remoteDto.bankRef,
                            timestamp = remoteDto.timestamp,
                            updatedAt = remoteDto.updatedAt,
                            syncStatus = SyncStatus.SYNCED.name
                        )
                        expenseRepository.insertExpenses(listOf(entity))
                        restoredCount++
                    } else if (remoteDto.updatedAt > local.updatedAt) {
                        val entity = ExpenseEntity(
                            id = local.id,
                            uuid = local.uuid,
                            userId = remoteDto.userId.ifBlank { targetUid },
                            categoryId = remoteDto.categoryId,
                            amountCents = remoteDto.amountCents,
                            type = remoteDto.type,
                            source = remoteDto.source,
                            customName = remoteDto.customName,
                            bankRef = remoteDto.bankRef,
                            timestamp = remoteDto.timestamp,
                            updatedAt = remoteDto.updatedAt,
                            syncStatus = SyncStatus.SYNCED.name
                        )
                        expenseRepository.insertExpenses(listOf(entity))
                        restoredCount++
                    }
                }
            }

            // 4. Drain any remaining local pending changes to cloud
            performOnlineTwoWaySync()
        } catch (e: Exception) {
            Log.e(TAG, "Error reconciling cloud data: ${e.message}", e)
        }
        restoredCount
    }

    /**
     * Drains local offline pending sync queue and updates sync status
     */
    suspend fun performLocalSyncQueueDrain(): Int {
        return performOnlineTwoWaySync()
    }

    /**
     * Schedules periodic background sync every 4 hours with network constraints
     */
    fun schedulePeriodicSync() {
        val ctx = context ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<ExpenseSyncWorker>(
            4, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * Triggers immediate one-time background sync
     */
    fun triggerImmediateSync() {
        val ctx = context ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeSync = OneTimeWorkRequestBuilder<ExpenseSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeSync
        )
    }

    /**
     * Propagates expense deletions to cloud sync backend
     */
    suspend fun deleteExpenses(uuids: List<String>) {
        if (uuids.isEmpty()) return
        deletedUuids.addAll(uuids)
        try {
            val currentStored = prefs?.getStringSet("deleted_uuids", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentStored.addAll(uuids)
            prefs?.edit()?.putStringSet("deleted_uuids", currentStored)?.apply()
        } catch (e: Exception) {
            // Ignore if SharedPreferences unavailable in test
        }

        try {
            syncApiClient.deleteExpenses(uuids)
        } catch (e: Exception) {
            // Background sync queue handles retry
        }
    }

    /**
     * Cancels background sync workers upon sign-out
     */
    fun cancelAllSyncWork() {
        val ctx = context ?: return
        WorkManager.getInstance(ctx).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        WorkManager.getInstance(ctx).cancelUniqueWork(ONE_TIME_SYNC_WORK_NAME)
    }
}
