package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.ExpenseRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.MemberSpendingSummary
import com.shakeexpense.app.sync.SyncApiClient
import com.shakeexpense.app.sync.SyncEngine
import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.sync.model.FamilyMemberDto
import com.shakeexpense.app.sync.model.JoinFamilyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class GetFamilyMembersUseCase(
    private val familyRepository: FamilyRepository
) {
    operator fun invoke(): Flow<List<FamilyMember>> =
        familyRepository.getFamilyMembers()
}

class AddFamilyMemberUseCase(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(
        name: String,
        role: FamilyRole,
        familyId: String = "family_local_01",
        deviceId: String = "device_local"
    ): Result<FamilyMember> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Member name cannot be empty"))
        }

        val currentMembers = familyRepository.getFamilyMembers().first()
        if (currentMembers.size >= 5) {
            return Result.failure(IllegalStateException("Maximum of 5 family members allowed under Family plan."))
        }

        val member = FamilyMember(
            id = "member_${UUID.randomUUID().toString().take(8)}",
            familyId = familyId,
            name = trimmedName,
            role = role,
            deviceId = deviceId,
            createdAt = System.currentTimeMillis()
        )

        familyRepository.addFamilyMember(member)
        return Result.success(member)
    }
}

class RemoveFamilyMemberUseCase(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(memberId: String): Result<Unit> {
        familyRepository.deleteFamilyMember(memberId)
        return Result.success(Unit)
    }
}

class RequestChildExitUseCase(
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(childMemberId: String): Result<String> {
        return try {
            familyRepository.setMemberExitRequested(childMemberId, true)
            Result.success("Exit request submitted. Parent approval required to detach from family.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CreateFamilyGroupUseCase(
    private val syncApiClient: SyncApiClient,
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(familyName: String, creatorUserId: String, creatorName: String = "Parent"): Result<FamilyGroupDto> {
        val result = syncApiClient.createFamily(familyName, creatorUserId, creatorName)
        if (result.isSuccess) {
            val family = result.getOrNull()!!
            // Persist family group locally in Room
            familyRepository.saveFamilyGroup(family)
            // ensure local user is marked as PARENT in this family with their authentic name
            val parentMember = FamilyMember(
                id = creatorUserId,
                familyId = family.familyId,
                name = creatorName.ifBlank { "Parent" },
                role = FamilyRole.PARENT,
                deviceId = "device_creator"
            )
            familyRepository.addFamilyMember(parentMember)
        }
        return result
    }
}

class JoinFamilyGroupUseCase(
    private val syncApiClient: SyncApiClient,
    private val familyRepository: FamilyRepository
) {
    suspend operator fun invoke(inviteCode: String, member: FamilyMember): JoinFamilyResult {
        val dto = FamilyMemberDto(
            userId = member.id,
            familyId = "",
            name = member.name,
            role = member.role.name,
            deviceId = member.deviceId
        )
        val result = syncApiClient.joinFamilyByInviteCode(inviteCode, dto)
        if (result.isSuccess && result.familyId != null) {
            if (result.members.size > 5) {
                return JoinFamilyResult(
                    isSuccess = false,
                    familyId = null,
                    familyName = null,
                    members = emptyList(),
                    errorMessage = "This family group has reached the maximum capacity of 5 members."
                )
            }
            // Persist family group locally in Room
            val familyGroup = FamilyGroupDto(
                familyId = result.familyId,
                familyName = result.familyName ?: "Family Group",
                creatorUserId = "",
                inviteCode = inviteCode.uppercase(),
                createdAt = System.currentTimeMillis()
            )
            familyRepository.saveFamilyGroup(familyGroup)
            // Save updated member locally with new familyId
            familyRepository.addFamilyMember(member.copy(familyId = result.familyId))
            // Save all existing family members fetched from Firestore
            for (m in result.members) {
                val memberRole = try {
                    FamilyRole.valueOf(m.role.uppercase())
                } catch (e: Exception) {
                    FamilyRole.CHILD
                }
                familyRepository.addFamilyMember(
                    FamilyMember(
                        id = m.userId,
                        familyId = result.familyId,
                        name = m.name,
                        role = memberRole,
                        deviceId = m.deviceId
                    )
                )
            }
        }
        return result
    }
}

class GetMemberSpendingSummaryUseCase(
    private val expenseRepository: ExpenseRepository,
    private val familyRepository: FamilyRepository? = null
) {
    operator fun invoke(userId: String, role: FamilyRole = FamilyRole.CHILD, viewerUserId: String = ""): Flow<MemberSpendingSummary?> {
        return expenseRepository.getMemberSpendingSummary(userId, role).map { summary ->
            if (familyRepository != null && viewerUserId.isNotBlank() && viewerUserId != userId) {
                val member = familyRepository.getFamilyMemberById(userId).first()
                if (member != null && (!member.privacySettings.shareMonthlyTotal || member.privacyMode == com.shakeexpense.app.domain.model.PrivacyMode.PRIVATE)) {
                    null
                } else {
                    summary
                }
            } else {
                summary
            }
        }
    }
}

class GetMemberCategoryBreakdownUseCase(
    private val expenseRepository: ExpenseRepository,
    private val familyRepository: FamilyRepository? = null
) {
    operator fun invoke(userId: String, viewerUserId: String = ""): Flow<List<CategorySubtotal>> {
        return expenseRepository.getMemberCategoryBreakdown(userId).map { breakdown ->
            if (familyRepository != null && viewerUserId.isNotBlank() && viewerUserId != userId) {
                val member = familyRepository.getFamilyMemberById(userId).first()
                if (member != null && (!member.privacySettings.shareCategoryTotals || member.privacyMode == com.shakeexpense.app.domain.model.PrivacyMode.PRIVATE)) {
                    emptyList()
                } else {
                    breakdown
                }
            } else {
                breakdown
            }
        }
    }
}

class GetMemberExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
    private val familyRepository: FamilyRepository? = null
) {
    operator fun invoke(userId: String, viewerUserId: String = ""): Flow<List<ExpenseRecordItem>> {
        return expenseRepository.getExpensesByUserId(userId).map { list ->
            if (familyRepository != null && viewerUserId.isNotBlank() && viewerUserId != userId) {
                val member = familyRepository.getFamilyMemberById(userId).first()
                if (member != null && (!member.privacySettings.shareTransactions || member.privacyMode == com.shakeexpense.app.domain.model.PrivacyMode.PRIVATE)) {
                    emptyList()
                } else if (member?.privacyMode == com.shakeexpense.app.domain.model.PrivacyMode.SHARED_SUMMARY) {
                    list.map {
                        it.copy(
                            customName = "Confidential",
                            bankRef = null
                        )
                    }
                } else {
                    list
                }
            } else {
                list
            }
        }
    }
}

class SyncFamilyExpensesUseCase(
    private val syncEngine: SyncEngine
) {
    suspend operator fun invoke(familyId: String = SyncEngine.DEFAULT_FAMILY_ID): Int =
        syncEngine.performOnlineTwoWaySync(familyId)
}
