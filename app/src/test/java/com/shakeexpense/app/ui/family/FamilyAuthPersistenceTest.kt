package com.shakeexpense.app.ui.family

import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.FamilyRepository
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.UserProfile
import com.shakeexpense.app.domain.usecase.AddFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.CreateFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.GetFamilyMembersUseCase
import com.shakeexpense.app.domain.usecase.GetMemberCategoryBreakdownUseCase
import com.shakeexpense.app.domain.usecase.GetMemberExpensesUseCase
import com.shakeexpense.app.domain.usecase.GetMemberSpendingSummaryUseCase
import com.shakeexpense.app.domain.usecase.JoinFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.RemoveFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.RequestChildExitUseCase
import com.shakeexpense.app.domain.usecase.SyncFamilyExpensesUseCase
import com.shakeexpense.app.sync.SyncApiClient
import com.shakeexpense.app.sync.model.FamilyGroupDto
import com.shakeexpense.app.sync.model.FamilyMemberDto
import com.shakeexpense.app.sync.model.JoinFamilyResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyAuthPersistenceTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var syncApiClient: SyncApiClient
    private lateinit var familyRepository: FamilyRepository
    private lateinit var getFamilyMembersUseCase: GetFamilyMembersUseCase
    private lateinit var addFamilyMemberUseCase: AddFamilyMemberUseCase
    private lateinit var removeFamilyMemberUseCase: RemoveFamilyMemberUseCase
    private lateinit var requestChildExitUseCase: RequestChildExitUseCase
    private lateinit var createFamilyGroupUseCase: CreateFamilyGroupUseCase
    private lateinit var joinFamilyGroupUseCase: JoinFamilyGroupUseCase
    private lateinit var getMemberSpendingSummaryUseCase: GetMemberSpendingSummaryUseCase
    private lateinit var getMemberCategoryBreakdownUseCase: GetMemberCategoryBreakdownUseCase
    private lateinit var getMemberExpensesUseCase: GetMemberExpensesUseCase
    private lateinit var syncFamilyExpensesUseCase: SyncFamilyExpensesUseCase
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        syncApiClient = mock(SyncApiClient::class.java)
        familyRepository = mock(FamilyRepository::class.java)
        getFamilyMembersUseCase = GetFamilyMembersUseCase(familyRepository)
        addFamilyMemberUseCase = AddFamilyMemberUseCase(familyRepository)
        removeFamilyMemberUseCase = RemoveFamilyMemberUseCase(familyRepository)
        requestChildExitUseCase = RequestChildExitUseCase(familyRepository)
        createFamilyGroupUseCase = CreateFamilyGroupUseCase(syncApiClient, familyRepository)
        joinFamilyGroupUseCase = JoinFamilyGroupUseCase(syncApiClient, familyRepository)
        getMemberSpendingSummaryUseCase = mock(GetMemberSpendingSummaryUseCase::class.java)
        getMemberCategoryBreakdownUseCase = mock(GetMemberCategoryBreakdownUseCase::class.java)
        getMemberExpensesUseCase = mock(GetMemberExpensesUseCase::class.java)
        syncFamilyExpensesUseCase = mock(SyncFamilyExpensesUseCase::class.java)
        authRepository = mock(AuthRepository::class.java)

        `when`(familyRepository.getFamilyMembers()).thenReturn(flowOf(emptyList()))
        `when`(familyRepository.getActiveFamilyGroup()).thenReturn(flowOf(null))
        `when`(familyRepository.getFamilyBudgets(org.mockito.ArgumentMatchers.anyString())).thenReturn(flowOf(emptyList()))
        val defaultProfile = UserProfile(
            userId = "firebase_user_a",
            displayName = "User A",
            email = "user_a@gmail.com",
            isAnonymous = false
        )
        `when`(authRepository.getCurrentProfile()).thenReturn(defaultProfile)
        `when`(authRepository.userProfile).thenReturn(flowOf(defaultProfile))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCreateFamilyMaintainsUserAuthAndPersistsGroup() = runTest {
        val createdGroup = FamilyGroupDto(
            familyId = "fam_12345",
            familyName = "Sharma Family",
            creatorUserId = "firebase_user_a",
            inviteCode = "SHK-7711",
            createdAt = System.currentTimeMillis()
        )
        `when`(syncApiClient.createFamily("Sharma Family", "firebase_user_a", "User A"))
            .thenReturn(Result.success(createdGroup))

        val viewModel = FamilyViewModel(
            getFamilyMembersUseCase,
            addFamilyMemberUseCase,
            removeFamilyMemberUseCase,
            requestChildExitUseCase,
            createFamilyGroupUseCase,
            joinFamilyGroupUseCase,
            getMemberSpendingSummaryUseCase,
            getMemberCategoryBreakdownUseCase,
            getMemberExpensesUseCase,
            syncFamilyExpensesUseCase,
            familyRepository,
            authRepository
        )

        viewModel.createFamily("Sharma Family")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.currentFamilyGroup)
        assertEquals("fam_12345", viewModel.state.value.currentFamilyGroup?.familyId)
        assertEquals("SHK-7711", viewModel.state.value.currentFamilyGroup?.inviteCode)
        // Verify family group was persisted to local Room storage
        verify(familyRepository).saveFamilyGroup(createdGroup)
        // Verify user remains authenticated User A
        assertEquals("firebase_user_a", authRepository.getCurrentProfile().userId)
    }

    @Test
    fun testJoinFamilyMaintainsUserAuth() = runTest {
        val dto = FamilyMemberDto(
            userId = "firebase_user_b",
            familyId = "",
            name = "Rohan",
            role = "CHILD",
            deviceId = "device_local"
        )
        val userBProfile = UserProfile(
            userId = "firebase_user_b",
            displayName = "User B",
            email = "user_b@gmail.com",
            isAnonymous = false
        )
        `when`(authRepository.getCurrentProfile()).thenReturn(userBProfile)
        `when`(authRepository.userProfile).thenReturn(flowOf(userBProfile))
        `when`(syncApiClient.joinFamilyByInviteCode("SHK-7711", dto)).thenReturn(
            JoinFamilyResult(
                isSuccess = true,
                familyId = "fam_12345",
                familyName = "Sharma Family",
                members = listOf(dto),
                errorMessage = null
            )
        )

        val viewModel = FamilyViewModel(
            getFamilyMembersUseCase,
            addFamilyMemberUseCase,
            removeFamilyMemberUseCase,
            requestChildExitUseCase,
            createFamilyGroupUseCase,
            joinFamilyGroupUseCase,
            getMemberSpendingSummaryUseCase,
            getMemberCategoryBreakdownUseCase,
            getMemberExpensesUseCase,
            syncFamilyExpensesUseCase,
            familyRepository,
            authRepository
        )

        viewModel.joinFamily("SHK-7711", "Rohan", FamilyRole.CHILD)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("fam_12345", viewModel.state.value.currentFamilyGroup?.familyId)
        // Verify User B remained logged in without any logout
        assertEquals("firebase_user_b", authRepository.getCurrentProfile().userId)
    }

    @Test
    fun testFamilyRestorationAfterAppRestart() = runTest {
        val cachedGroup = FamilyGroupDto(
            familyId = "fam_persistent_99",
            familyName = "Kapoor Family",
            creatorUserId = "firebase_user_a",
            inviteCode = "SHK-9988",
            createdAt = 1700000000000L
        )
        val cachedMember = FamilyMember(
            id = "firebase_user_a",
            familyId = "fam_persistent_99",
            name = "Me",
            role = FamilyRole.PARENT,
            deviceId = "device_local"
        )
        `when`(familyRepository.getActiveFamilyGroup()).thenReturn(flowOf(cachedGroup))
        `when`(familyRepository.getFamilyMembers()).thenReturn(flowOf(listOf(cachedMember)))

        val viewModel = FamilyViewModel(
            getFamilyMembersUseCase,
            addFamilyMemberUseCase,
            removeFamilyMemberUseCase,
            requestChildExitUseCase,
            createFamilyGroupUseCase,
            joinFamilyGroupUseCase,
            getMemberSpendingSummaryUseCase,
            getMemberCategoryBreakdownUseCase,
            getMemberExpensesUseCase,
            syncFamilyExpensesUseCase,
            familyRepository,
            authRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that upon opening ViewModel, family name & invite code are immediately restored from Room
        assertNotNull(viewModel.state.value.currentFamilyGroup)
        assertEquals("Kapoor Family", viewModel.state.value.currentFamilyGroup?.familyName)
        assertEquals("SHK-9988", viewModel.state.value.currentFamilyGroup?.inviteCode)
        assertEquals("fam_persistent_99", viewModel.state.value.currentFamilyGroup?.familyId)
        assertEquals(1, viewModel.state.value.membersWithSummaries.size)
        assertEquals("Me", viewModel.state.value.membersWithSummaries[0].member.name)
    }
}
