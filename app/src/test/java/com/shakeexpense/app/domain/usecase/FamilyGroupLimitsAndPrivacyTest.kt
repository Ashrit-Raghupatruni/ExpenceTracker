package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.PrivacyMode
import com.shakeexpense.app.sync.model.FamilyGroupDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyGroupLimitsAndPrivacyTest {

    private class FakeFamilyRepository(
        private val initialMembers: MutableList<FamilyMember> = mutableListOf()
    ) : FamilyRepository {
        override fun getFamilyMembers(): Flow<List<FamilyMember>> = flowOf(initialMembers)
        override fun getFamilyMemberById(id: String): Flow<FamilyMember?> = flowOf(initialMembers.find { it.id == id })
        override fun getActiveFamilyGroup(): Flow<FamilyGroupDto?> = flowOf(null)
        override suspend fun saveFamilyGroup(family: FamilyGroupDto) {}
        override suspend fun addFamilyMember(member: FamilyMember) { initialMembers.add(member) }
        override suspend fun setFamilyMembers(members: List<FamilyMember>) {
            initialMembers.clear()
            initialMembers.addAll(members)
        }
        override suspend fun deleteFamilyMember(id: String) { initialMembers.removeAll { it.id == id } }
        override suspend fun clearFamilyData() { initialMembers.clear() }
        override suspend fun updateFamilySpendingLimit(familyId: String, limitCents: Long) {}
        override suspend fun updateMemberPrivacyMode(memberId: String, mode: PrivacyMode) {
            val idx = initialMembers.indexOfFirst { it.id == memberId }
            if (idx >= 0) {
                initialMembers[idx] = initialMembers[idx].copy(privacyMode = mode)
            }
        }
        override suspend fun updateMemberPrivacy(memberId: String, settings: com.shakeexpense.app.domain.model.FamilyPrivacySettings) {
            val idx = initialMembers.indexOfFirst { it.id == memberId }
            if (idx >= 0) {
                initialMembers[idx] = initialMembers[idx].copy(privacySettings = settings)
            }
        }
        override fun getFamilyBudgets(familyId: String): Flow<List<com.shakeexpense.app.domain.model.FamilyBudget>> = flowOf(emptyList())
        override suspend fun saveFamilyBudget(budget: com.shakeexpense.app.domain.model.FamilyBudget) {}
        override suspend fun deleteFamilyBudget(budgetId: String) {}
        override suspend fun setMemberExitRequested(memberId: String, requested: Boolean) {
            val idx = initialMembers.indexOfFirst { it.id == memberId }
            if (idx >= 0) {
                initialMembers[idx] = initialMembers[idx].copy(isExitRequested = requested)
            }
        }
    }

    @Test
    fun `test family member addition enforces maximum 5 members limit`() = runBlocking {
        val existingMembers = (1..5).map {
            FamilyMember(
                id = "mem_$it",
                familyId = "fam_1",
                name = "Member $it",
                role = FamilyRole.CHILD,
                deviceId = "dev_$it"
            )
        }.toMutableList()

        val repo = FakeFamilyRepository(existingMembers)
        val addMemberUseCase = AddFamilyMemberUseCase(repo)

        val result = addMemberUseCase("Sixth Member", FamilyRole.CHILD)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Maximum of 5 family members") == true)
    }

    @Test
    fun `test adding under 5 members succeeds`() = runBlocking {
        val repo = FakeFamilyRepository()
        val addMemberUseCase = AddFamilyMemberUseCase(repo)

        val result = addMemberUseCase("First Child", FamilyRole.CHILD)
        assertTrue(result.isSuccess)
        assertEquals("First Child", result.getOrNull()?.name)
    }
}
