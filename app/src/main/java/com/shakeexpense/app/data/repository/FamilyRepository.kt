package com.shakeexpense.app.data.repository

import com.shakeexpense.app.data.database.dao.FamilyGroupDao
import com.shakeexpense.app.data.database.dao.FamilyMemberDao
import com.shakeexpense.app.data.database.entity.FamilyGroupEntity
import com.shakeexpense.app.data.database.entity.FamilyMemberEntity
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.sync.model.FamilyGroupDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FamilyRepository {
    fun getFamilyMembers(): Flow<List<FamilyMember>>
    fun getFamilyMemberById(id: String): Flow<FamilyMember?>
    fun getActiveFamilyGroup(): Flow<FamilyGroupDto?>
    suspend fun saveFamilyGroup(family: FamilyGroupDto)
    suspend fun addFamilyMember(member: FamilyMember)
    suspend fun setFamilyMembers(members: List<FamilyMember>)
    suspend fun deleteFamilyMember(id: String)
    suspend fun clearFamilyData()
    suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long)
    suspend fun updateMemberPrivacyMode(memberId: String, privacyMode: com.shakeexpense.app.domain.model.PrivacyMode)
    suspend fun updateMemberPrivacy(memberId: String, privacySettings: com.shakeexpense.app.domain.model.FamilyPrivacySettings)
    fun getFamilyBudgets(familyId: String): Flow<List<com.shakeexpense.app.domain.model.FamilyBudget>>
    suspend fun saveFamilyBudget(budget: com.shakeexpense.app.domain.model.FamilyBudget)
    suspend fun deleteFamilyBudget(id: String)
    suspend fun setMemberExitRequested(memberId: String, requested: Boolean)
}

class FamilyRepositoryImpl(
    private val familyMemberDao: FamilyMemberDao,
    private val familyGroupDao: FamilyGroupDao? = null,
    private val familyBudgetDao: com.shakeexpense.app.data.database.dao.FamilyBudgetDao? = null
) : FamilyRepository {

    override fun getFamilyMembers(): Flow<List<FamilyMember>> {
        return familyMemberDao.getAllMembers().map { entities ->
            entities.map { entity ->
                val privacy = try {
                    com.shakeexpense.app.domain.model.PrivacyMode.valueOf(entity.privacyMode.uppercase())
                } catch (_: Exception) {
                    com.shakeexpense.app.domain.model.PrivacyMode.FULL_SHARED
                }
                FamilyMember(
                    id = entity.id,
                    familyId = entity.familyId,
                    name = entity.name,
                    role = if (entity.role.equals("PARENT", ignoreCase = true)) FamilyRole.PARENT else FamilyRole.CHILD,
                    deviceId = entity.deviceId,
                    createdAt = entity.createdAt,
                    privacyMode = privacy,
                    privacySettings = com.shakeexpense.app.domain.model.FamilyPrivacySettings(
                        shareTransactions = entity.shareTransactions,
                        shareMonthlyTotal = entity.shareMonthlyTotal,
                        shareCategoryTotals = entity.shareCategoryTotals,
                        receiveFamilyAlerts = entity.receiveFamilyAlerts
                    ),
                    isExitRequested = entity.isExitRequested
                )
            }
        }
    }

    override fun getFamilyMemberById(id: String): Flow<FamilyMember?> {
        return familyMemberDao.getMemberByIdFlow(id).map { entity ->
            entity?.let {
                val privacy = try {
                    com.shakeexpense.app.domain.model.PrivacyMode.valueOf(it.privacyMode.uppercase())
                } catch (_: Exception) {
                    com.shakeexpense.app.domain.model.PrivacyMode.FULL_SHARED
                }
                FamilyMember(
                    id = it.id,
                    familyId = it.familyId,
                    name = it.name,
                    role = if (it.role.equals("PARENT", ignoreCase = true)) FamilyRole.PARENT else FamilyRole.CHILD,
                    deviceId = it.deviceId,
                    createdAt = it.createdAt,
                    privacyMode = privacy,
                    privacySettings = com.shakeexpense.app.domain.model.FamilyPrivacySettings(
                        shareTransactions = it.shareTransactions,
                        shareMonthlyTotal = it.shareMonthlyTotal,
                        shareCategoryTotals = it.shareCategoryTotals,
                        receiveFamilyAlerts = it.receiveFamilyAlerts
                    ),
                    isExitRequested = it.isExitRequested
                )
            }
        }
    }

    override fun getActiveFamilyGroup(): Flow<FamilyGroupDto?> {
        if (familyGroupDao == null) {
            return kotlinx.coroutines.flow.flowOf(null)
        }
        return familyGroupDao.getActiveFamilyGroupFlow().map { entity ->
            entity?.let {
                FamilyGroupDto(
                    familyId = it.familyId,
                    familyName = it.familyName,
                    creatorUserId = it.ownerUid,
                    inviteCode = it.inviteCode,
                    createdAt = it.createdAt
                )
            }
        }
    }

    override suspend fun saveFamilyGroup(family: FamilyGroupDto) {
        val entity = FamilyGroupEntity(
            familyId = family.familyId,
            familyName = family.familyName,
            inviteCode = family.inviteCode,
            ownerUid = family.creatorUserId,
            createdAt = family.createdAt
        )
        familyGroupDao?.insertFamilyGroup(entity)
    }

    override suspend fun addFamilyMember(member: FamilyMember) {
        val entity = FamilyMemberEntity(
            id = member.id,
            familyId = member.familyId,
            name = member.name,
            role = member.role.name,
            deviceId = member.deviceId,
            createdAt = member.createdAt,
            privacyMode = member.privacyMode.name,
            shareTransactions = member.privacySettings.shareTransactions,
            shareMonthlyTotal = member.privacySettings.shareMonthlyTotal,
            shareCategoryTotals = member.privacySettings.shareCategoryTotals,
            receiveFamilyAlerts = member.privacySettings.receiveFamilyAlerts,
            isExitRequested = member.isExitRequested
        )
        familyMemberDao.insertMember(entity)
    }

    override suspend fun setFamilyMembers(members: List<FamilyMember>) {
        val entities = members.map { member ->
            FamilyMemberEntity(
                id = member.id,
                familyId = member.familyId,
                name = member.name,
                role = member.role.name,
                deviceId = member.deviceId,
                createdAt = member.createdAt,
                privacyMode = member.privacyMode.name,
                shareTransactions = member.privacySettings.shareTransactions,
                shareMonthlyTotal = member.privacySettings.shareMonthlyTotal,
                shareCategoryTotals = member.privacySettings.shareCategoryTotals,
                receiveFamilyAlerts = member.privacySettings.receiveFamilyAlerts,
                isExitRequested = member.isExitRequested
            )
        }
        familyMemberDao.insertMembers(entities)
    }

    override suspend fun deleteFamilyMember(id: String) {
        val member = familyMemberDao.getMemberById(id)
        if (member != null) {
            familyMemberDao.deleteMember(member)
        }
    }

    override suspend fun clearFamilyData() {
        familyGroupDao?.clearFamilyGroups()
        familyMemberDao.deleteAllMembers()
    }

    override suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long) {
        familyGroupDao?.updateSpendingLimit(familyId, limitCents)
    }

    override suspend fun updateMemberPrivacyMode(memberId: String, privacyMode: com.shakeexpense.app.domain.model.PrivacyMode) {
        familyMemberDao.updateMemberPrivacyMode(memberId, privacyMode.name)
    }

    override suspend fun updateMemberPrivacy(memberId: String, privacySettings: com.shakeexpense.app.domain.model.FamilyPrivacySettings) {
        familyMemberDao.updatePrivacySettings(
            id = memberId,
            shareTransactions = privacySettings.shareTransactions,
            shareMonthlyTotal = privacySettings.shareMonthlyTotal,
            shareCategoryTotals = privacySettings.shareCategoryTotals,
            receiveFamilyAlerts = privacySettings.receiveFamilyAlerts
        )
    }

    override fun getFamilyBudgets(familyId: String): Flow<List<com.shakeexpense.app.domain.model.FamilyBudget>> {
        if (familyBudgetDao == null) return kotlinx.coroutines.flow.flowOf(emptyList())
        return familyBudgetDao.getFamilyBudgetsFlow(familyId).map { entities ->
            entities.map { entity ->
                com.shakeexpense.app.domain.model.FamilyBudget(
                    id = entity.id,
                    familyId = entity.familyId,
                    categoryName = entity.categoryName,
                    limitCents = entity.limitCents,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override suspend fun saveFamilyBudget(budget: com.shakeexpense.app.domain.model.FamilyBudget) {
        val entity = com.shakeexpense.app.data.database.entity.FamilyBudgetEntity(
            id = budget.id,
            familyId = budget.familyId,
            categoryName = budget.categoryName,
            limitCents = budget.limitCents,
            updatedAt = budget.updatedAt
        )
        familyBudgetDao?.insertOrUpdateBudget(entity)
    }

    override suspend fun deleteFamilyBudget(id: String) {
        familyBudgetDao?.deleteBudget(id)
    }

    override suspend fun setMemberExitRequested(memberId: String, requested: Boolean) {
        familyMemberDao.updateExitRequested(memberId, requested)
    }
}
