package com.shakeexpense.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.dao.CategoryDao
import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.database.dao.FamilyMemberDao
import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var familyMemberDao: FamilyMemberDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = db.categoryDao()
        familyMemberDao = db.familyMemberDao()
        expenseDao = db.expenseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSeedCategoriesInsertionAndOrdering() = runBlocking {
        populateInitialData(db)

        val categories = categoryDao.getAllCategoriesSync()
        assertEquals(7, categories.size)

        // Seeded order: Food(1), Transport(2), Groceries(3), Bills(4), Shopping(5), Entertainment(6), Others(99)
        assertEquals("Food", categories[0].name)
        assertEquals("#F59E0B", categories[0].colorHex)
        assertEquals(1, categories[0].displayOrder)

        assertEquals("Transport", categories[1].name)
        assertEquals("#3B82F6", categories[1].colorHex)

        assertEquals("Others", categories[6].name)
        assertEquals("#64748B", categories[6].colorHex)
        assertEquals(99, categories[6].displayOrder)
    }

    @Test
    fun testFamilyMemberCreationAndRetrieval() = runBlocking {
        populateInitialData(db)

        val childMember = FamilyMemberEntity(
            id = "child_user_rahul",
            familyId = "default_family",
            name = "Rahul",
            role = "CHILD",
            deviceId = "device_rahul_123",
            createdAt = System.currentTimeMillis()
        )
        familyMemberDao.insertMember(childMember)

        val members = familyMemberDao.getAllMembersSync()
        assertEquals(2, members.size) // Default local user + Rahul

        val retrieved = familyMemberDao.getMemberById("child_user_rahul")
        assertNotNull(retrieved)
        assertEquals("Rahul", retrieved?.name)
        assertEquals("CHILD", retrieved?.role)
    }

    @Test
    fun testMonetaryPrecisionAndArithmetic() = runBlocking {
        populateInitialData(db)

        // Test integer cent precision: ₹250.75 -> 25075 cents
        val expense = ExpenseEntity(
            uuid = UUID.randomUUID().toString(),
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1, // Food
            amountCents = 25075L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            customName = null,
            bankRef = null,
            timestamp = 1000L,
            updatedAt = 1000L,
            syncStatus = "PENDING"
        )
        val id = expenseDao.insertExpense(expense)
        assertTrue(id > 0)

        val retrieved = expenseDao.getExpenseById(id)
        assertNotNull(retrieved)
        assertEquals(25075L, retrieved?.amountCents)
        // Verify formatted amount without floating-point error
        val formatted = (retrieved!!.amountCents) / 100.0
        assertEquals(250.75, formatted, 0.0001)
    }

    @Test
    fun testTabularSpreadsheetStreamJoin() = runBlocking {
        populateInitialData(db)

        val childMember = FamilyMemberEntity(
            id = "child_user_1",
            familyId = "default_family",
            name = "Rahul",
            role = "CHILD",
            deviceId = "device_1",
            createdAt = 1000L
        )
        familyMemberDao.insertMember(childMember)

        val expense1 = ExpenseEntity(
            uuid = "exp_1",
            userId = "child_user_1",
            categoryId = 1, // Food
            amountCents = 35000L, // 350.00
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            customName = "Starbucks",
            bankRef = null,
            timestamp = 2000L,
            updatedAt = 2000L,
            syncStatus = "PENDING"
        )
        val expense2 = ExpenseEntity(
            uuid = "exp_2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 2, // Transport
            amountCents = 15000L, // 150.00
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            customName = null,
            bankRef = null,
            timestamp = 3000L,
            updatedAt = 3000L,
            syncStatus = "SYNCED"
        )
        expenseDao.insertExpenses(listOf(expense1, expense2))

        val spreadsheet = expenseDao.getSpreadsheetStream().first()
        assertEquals(2, spreadsheet.size)

        // Ordered by timestamp DESC: expense2 (3000) then expense1 (2000)
        assertEquals("exp_2", spreadsheet[0].expenseUuid)
        assertEquals("Transport", spreadsheet[0].categoryName)
        assertEquals("#3B82F6", spreadsheet[0].categoryColor)
        assertEquals("Me", spreadsheet[0].userName)

        assertEquals("exp_1", spreadsheet[1].expenseUuid)
        assertEquals("Food", spreadsheet[1].categoryName)
        assertEquals("Starbucks", spreadsheet[1].customName)
        assertEquals("Rahul", spreadsheet[1].userName)
    }

    @Test
    fun testMemberSpendingSummaryAndCategoryBreakdown() = runBlocking {
        populateInitialData(db)

        val rahulId = "user_rahul"
        familyMemberDao.insertMember(
            FamilyMemberEntity(
                id = rahulId,
                familyId = "fam_1",
                name = "Rahul",
                role = "CHILD",
                deviceId = "dev_r",
                createdAt = 1000L
            )
        )

        // Rahul: 2 Food debits (210000 cents = ₹2100), 1 Transport debit (110000 cents = ₹1100), 1 Credit (50000 cents = ₹500)
        expenseDao.insertExpenses(
            listOf(
                ExpenseEntity(
                    uuid = "r_1",
                    userId = rahulId,
                    categoryId = 1, // Food
                    amountCents = 150000L,
                    type = "DEBIT",
                    source = "MANUAL_SHAKE",
                    timestamp = 1000L,
                    updatedAt = 1000L
                ),
                ExpenseEntity(
                    uuid = "r_2",
                    userId = rahulId,
                    categoryId = 1, // Food
                    amountCents = 60000L,
                    type = "DEBIT",
                    source = "MANUAL_SHAKE",
                    timestamp = 2000L,
                    updatedAt = 2000L
                ),
                ExpenseEntity(
                    uuid = "r_3",
                    userId = rahulId,
                    categoryId = 2, // Transport
                    amountCents = 110000L,
                    type = "DEBIT",
                    source = "MANUAL_SHAKE",
                    timestamp = 3000L,
                    updatedAt = 3000L
                ),
                ExpenseEntity(
                    uuid = "r_4",
                    userId = rahulId,
                    categoryId = 1, // Food
                    amountCents = 50000L,
                    type = "CREDIT",
                    source = "BANK_NOTIF",
                    timestamp = 4000L,
                    updatedAt = 4000L
                )
            )
        )

        val summary = expenseDao.getMemberSpendingSummary(rahulId).first()
        assertNotNull(summary)
        assertEquals(320000L, summary?.totalDebitCents) // 150000 + 60000 + 110000 = 320000 (₹3200)
        assertEquals(50000L, summary?.totalCreditCents) // 50000 (₹500)
        assertEquals(4, summary?.transactionCount)

        val breakdown = expenseDao.getMemberCategoryBreakdown(rahulId).first()
        assertEquals(2, breakdown.size)
        // Top category: Food (210000 cents, count 2)
        assertEquals("Food", breakdown[0].categoryName)
        assertEquals(210000L, breakdown[0].categoryTotalCents)
        assertEquals(2, breakdown[0].transactionCount)

        // Second category: Transport (110000 cents, count 1)
        assertEquals("Transport", breakdown[1].categoryName)
        assertEquals(110000L, breakdown[1].categoryTotalCents)
        assertEquals(1, breakdown[1].transactionCount)
    }

    @Test
    fun testPendingSyncQueueAndStatusUpdate() = runBlocking {
        populateInitialData(db)

        val exp1 = ExpenseEntity(
            uuid = "sync_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 1000L,
            timestamp = 100L,
            updatedAt = 100L,
            syncStatus = "PENDING"
        )
        val exp2 = ExpenseEntity(
            uuid = "sync_2",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 2,
            amountCents = 2000L,
            timestamp = 200L,
            updatedAt = 200L,
            syncStatus = "SYNCED"
        )
        expenseDao.insertExpenses(listOf(exp1, exp2))

        val pending = expenseDao.getPendingSyncExpenses()
        assertEquals(1, pending.size)
        assertEquals("sync_1", pending[0].uuid)

        // Update sync status to SYNCED
        expenseDao.updateSyncStatus(listOf("sync_1"), "SYNCED", System.currentTimeMillis())
        val pendingAfter = expenseDao.getPendingSyncExpenses()
        assertTrue(pendingAfter.isEmpty())
    }

    @Test
    fun testBankNotificationDeduplicationQuery() = runBlocking {
        populateInitialData(db)

        val bankExp = ExpenseEntity(
            uuid = "bank_1",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 35000L,
            type = "DEBIT",
            source = "BANK_NOTIF",
            customName = "Starbucks",
            bankRef = "UPI-123456",
            timestamp = 5000L,
            updatedAt = 5000L
        )
        expenseDao.insertExpense(bankExp)

        // Search within 60-second window (4000 to 6000)
        val duplicate = expenseDao.findPotentialDuplicate(
            amountCents = 35000L,
            minTimestamp = 4000L,
            maxTimestamp = 6000L,
            bankRef = "UPI-123456"
        )
        assertNotNull(duplicate)
        assertEquals("bank_1", duplicate?.uuid)

        // Search with non-matching amount should be null
        val notDuplicate = expenseDao.findPotentialDuplicate(
            amountCents = 99999L,
            minTimestamp = 4000L,
            maxTimestamp = 6000L,
            bankRef = "UPI-123456"
        )
        assertNull(notDuplicate)
    }
}
