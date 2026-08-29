package com.shakeexpense.app.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.populateInitialData
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.data.repository.FamilyRepositoryImpl
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.FamilyRole
import com.shakeexpense.app.domain.model.SyncStatus
import com.shakeexpense.app.domain.usecase.AddFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.CreateFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.GetFamilyMembersUseCase
import com.shakeexpense.app.domain.usecase.JoinFamilyGroupUseCase
import com.shakeexpense.app.domain.usecase.RemoveFamilyMemberUseCase
import com.shakeexpense.app.domain.usecase.RequestChildExitUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FamilySyncTest {

    private lateinit var db: AppDatabase
    private lateinit var familyRepo: FamilyRepositoryImpl
    private lateinit var expenseRepo: ExpenseRepositoryImpl
    private lateinit var syncApiClient: BackendSyncApiClient
    private lateinit var syncEngine: SyncEngine
    private lateinit var addMemberUseCase: AddFamilyMemberUseCase
    private lateinit var removeMemberUseCase: RemoveFamilyMemberUseCase
    private lateinit var requestChildExitUseCase: RequestChildExitUseCase
    private lateinit var getMembersUseCase: GetFamilyMembersUseCase
    private lateinit var createFamilyUseCase: CreateFamilyGroupUseCase
    private lateinit var joinFamilyUseCase: JoinFamilyGroupUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        familyRepo = FamilyRepositoryImpl(db.familyMemberDao())
        expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        syncApiClient = BackendSyncApiClient()
        syncEngine = SyncEngine(expenseRepo, syncApiClient, null)
        addMemberUseCase = AddFamilyMemberUseCase(familyRepo)
        removeMemberUseCase = RemoveFamilyMemberUseCase(familyRepo)
        requestChildExitUseCase = RequestChildExitUseCase(familyRepo)
        getMembersUseCase = GetFamilyMembersUseCase(familyRepo)
        createFamilyUseCase = CreateFamilyGroupUseCase(syncApiClient, familyRepo)
        joinFamilyUseCase = JoinFamilyGroupUseCase(syncApiClient, familyRepo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testCreateAndJoinFamilyGroup() = runBlocking {
        populateInitialData(db)

        // 1. Parent creates family
        val createResult = createFamilyUseCase("Sharma Household", "parent_user_01")
        assertTrue(createResult.isSuccess)
        val family = createResult.getOrNull()!!
        assertNotNull(family.familyId)
        assertTrue(family.inviteCode.startsWith("SHK-"))

        // 2. Child joins family using 6-digit invite code
        val childMember = FamilyMember(
            id = "child_user_02",
            familyId = "",
            name = "Rohan",
            role = FamilyRole.CHILD,
            deviceId = "child_phone"
        )
        val joinResult = joinFamilyUseCase(family.inviteCode, childMember)
        assertTrue(joinResult.isSuccess)
        assertEquals(family.familyId, joinResult.familyId)

        // Verify child is saved locally with family ID
        val savedChild = familyRepo.getFamilyMemberById("child_user_02").first()
        assertNotNull(savedChild)
        assertEquals(family.familyId, savedChild?.familyId)
    }

    @Test
    fun testParentRemoveMemberFlow() = runBlocking {
        populateInitialData(db)

        val child = FamilyMember(
            id = "child_to_remove",
            familyId = "family_01",
            name = "Rohan",
            role = FamilyRole.CHILD,
            deviceId = "device_child"
        )
        familyRepo.addFamilyMember(child)

        val membersBefore = getMembersUseCase().first()
        assertTrue(membersBefore.any { it.id == "child_to_remove" })

        // Parent deletes member
        val removeResult = removeMemberUseCase("child_to_remove")
        assertTrue(removeResult.isSuccess)

        val membersAfter = getMembersUseCase().first()
        assertFalse(membersAfter.any { it.id == "child_to_remove" })
    }

    @Test
    fun testChildExitRequestFlow() = runBlocking {
        populateInitialData(db)

        val exitResult = requestChildExitUseCase("child_01")
        assertTrue(exitResult.isSuccess)
        assertTrue(exitResult.getOrNull()?.contains("approval required", ignoreCase = true) == true)
    }

    @Test
    fun testAddAndRetrieveFamilyMembers() = runBlocking {
        populateInitialData(db)

        val result1 = addMemberUseCase("Rohan", FamilyRole.CHILD)
        assertTrue(result1.isSuccess)
        val child = result1.getOrNull()!!
        assertEquals("Rohan", child.name)
        assertEquals(FamilyRole.CHILD, child.role)

        val result2 = addMemberUseCase("Priya", FamilyRole.PARENT)
        assertTrue(result2.isSuccess)
        val parent = result2.getOrNull()!!
        assertEquals("Priya", parent.name)
        assertEquals(FamilyRole.PARENT, parent.role)

        val members = getMembersUseCase().first()
        // Default self user + Rohan + Priya = 3 members
        assertEquals(3, members.size)
    }

    @Test
    fun testAddFamilyMemberRejectsBlankName() = runBlocking {
        val result = addMemberUseCase("   ", FamilyRole.CHILD)
        assertTrue(result.isFailure)
    }

    @Test
    fun testMemberSpecificSpendingSummaryAndBreakdown() = runBlocking {
        populateInitialData(db)

        val child = FamilyMember(
            id = "child_user_01",
            familyId = "family_01",
            name = "Rohan",
            role = FamilyRole.CHILD,
            deviceId = "device_child"
        )
        familyRepo.addFamilyMember(child)

        // Expense 1 for Child: Food ₹200 (20000 cents)
        val exp1 = ExpenseEntity(
            uuid = "child_exp_1",
            userId = "child_user_01",
            categoryId = 1, // Food
            amountCents = 20000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE"
        )
        // Expense 2 for Child: Entertainment ₹500 (50000 cents)
        val exp2 = ExpenseEntity(
            uuid = "child_exp_2",
            userId = "child_user_01",
            categoryId = 6, // Entertainment
            amountCents = 50000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE"
        )
        // Expense 3 for Default Self User: Food ₹300
        val expSelf = ExpenseEntity(
            uuid = "self_exp",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 30000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE"
        )

        db.expenseDao().insertExpenses(listOf(exp1, exp2, expSelf))

        // Child summary check
        val summary = db.expenseDao().getMemberSpendingSummarySync("child_user_01")
        assertEquals(70000L, summary?.totalDebitCents)
        assertEquals(2, summary?.transactionCount)

        // Child breakdown check
        val breakdown = expenseRepo.getMemberCategoryBreakdown("child_user_01").first()
        assertEquals(2, breakdown.size)
        assertEquals("Entertainment", breakdown[0].categoryName)
        assertEquals(50000L, breakdown[0].totalCents)
        assertEquals("Food", breakdown[1].categoryName)
        assertEquals(20000L, breakdown[1].totalCents)
    }

    @Test
    fun testOfflineSyncQueueDrain() = runBlocking {
        val expPending1 = ExpenseEntity(
            uuid = "sync_uuid_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 15000L,
            syncStatus = "PENDING"
        )
        val expPending2 = ExpenseEntity(
            uuid = "sync_uuid_2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 2,
            amountCents = 25000L,
            syncStatus = "PENDING"
        )
        db.expenseDao().insertExpenses(listOf(expPending1, expPending2))

        val pendingBefore = expenseRepo.getPendingSyncExpenses()
        assertEquals(2, pendingBefore.size)

        val drainedCount = syncEngine.performLocalSyncQueueDrain()
        assertEquals(2, drainedCount)

        val pendingAfter = expenseRepo.getPendingSyncExpenses()
        assertEquals(0, pendingAfter.size)

        val exp1After = db.expenseDao().getExpenseByUuid("sync_uuid_1")
        assertEquals(SyncStatus.SYNCED.name, exp1After?.syncStatus)
    }

    @Test
    fun testLastWriteWinsConflictResolution() {
        val local = ExpenseEntity(
            uuid = "exp_lww",
            userId = "user_1",
            categoryId = 1,
            amountCents = 10000L,
            updatedAt = 1000L
        )

        // Remote record with newer timestamp -> Overwrites local
        val remoteNewer = ExpenseEntity(
            uuid = "exp_lww",
            userId = "user_1",
            categoryId = 1,
            amountCents = 12000L,
            updatedAt = 2000L
        )
        assertTrue(SyncEngine.shouldRemoteOverwriteLocal(local, remoteNewer))

        // Remote record with older timestamp -> Preserves local
        val remoteOlder = ExpenseEntity(
            uuid = "exp_lww",
            userId = "user_1",
            categoryId = 1,
            amountCents = 8000L,
            updatedAt = 500L
        )
        assertFalse(SyncEngine.shouldRemoteOverwriteLocal(local, remoteOlder))
    }

    @Test
    fun testCrossUserExpenseOwnershipIsolation() = runBlocking {
        populateInitialData(db)

        // User A (Parent) creates expense for ₹300
        val userAExpense = ExpenseEntity(
            uuid = "uuid_user_a_exp",
            userId = "google_user_A_id",
            categoryId = 1,
            amountCents = 30000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = System.currentTimeMillis()
        )

        // User B (Child/Member) creates expense for ₹700
        val userBExpense = ExpenseEntity(
            uuid = "uuid_user_b_exp",
            userId = "google_user_B_id",
            categoryId = 2,
            amountCents = 70000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = System.currentTimeMillis()
        )

        db.expenseDao().insertExpenses(listOf(userAExpense, userBExpense))

        // 1. Verify User A's spending summary contains ONLY User A's expense (₹300)
        val userASummary = db.expenseDao().getMemberSpendingSummarySync("google_user_A_id")
        assertEquals(30000L, userASummary?.totalDebitCents)
        assertEquals(1, userASummary?.transactionCount)

        // 2. Verify User B's spending summary contains ONLY User B's expense (₹700)
        val userBSummary = db.expenseDao().getMemberSpendingSummarySync("google_user_B_id")
        assertEquals(70000L, userBSummary?.totalDebitCents)
        assertEquals(1, userBSummary?.transactionCount)

        // 3. Verify individual expense queries return only that member's records
        val userAExpenses = expenseRepo.getExpensesByUserId("google_user_A_id").first()
        assertEquals(1, userAExpenses.size)
        assertEquals("uuid_user_a_exp", userAExpenses[0].expenseUuid)
        assertEquals("google_user_A_id", userAExpenses[0].userId)

        val userBExpenses = expenseRepo.getExpensesByUserId("google_user_B_id").first()
        assertEquals(1, userBExpenses.size)
        assertEquals("uuid_user_b_exp", userBExpenses[0].expenseUuid)
        assertEquals("google_user_B_id", userBExpenses[0].userId)
    }
}
