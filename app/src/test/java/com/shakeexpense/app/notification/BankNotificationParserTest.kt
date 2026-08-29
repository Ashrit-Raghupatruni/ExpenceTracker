package com.shakeexpense.app.notification

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.populateInitialData
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.ProcessBankNotificationResult
import com.shakeexpense.app.domain.usecase.ProcessBankNotificationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BankNotificationParserTest {

    private lateinit var parser: BankNotificationParser
    private lateinit var db: AppDatabase
    private lateinit var expenseRepo: ExpenseRepositoryImpl
    private lateinit var processUseCase: ProcessBankNotificationUseCase

    @Before
    fun setUp() {
        parser = BankNotificationParser()
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        processUseCase = ProcessBankNotificationUseCase(expenseRepo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testHdfcFoodDebitNotification() {
        val text = "HDFC Bank: Rs 450.00 debited from a/c **1234 on 24-AUG at SWIGGY. Info: UPI/4231."
        val result = parser.parse(text)

        assertNotNull(result)
        assertEquals(45000L, result?.amountCents)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(1L, result?.suggestedCategoryId) // Food
    }

    @Test
    fun testSbiTransportDebitNotification() {
        val text = "Dear UPI user A/C 9876 debited by Rs. 120.00 on 24Aug26 transfer to Uber India."
        val result = parser.parse(text)

        assertNotNull(result)
        assertEquals(12000L, result?.amountCents)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(2L, result?.suggestedCategoryId) // Transport
    }

    @Test
    fun testIciciGroceriesNotification() {
        val text = "Rs 890 spent on your ICICI Bank Card at BLINKIT on 24-Aug-26."
        val result = parser.parse(text)

        assertNotNull(result)
        assertEquals(89000L, result?.amountCents)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(3L, result?.suggestedCategoryId) // Groceries
    }

    @Test
    fun testAxisBillsNotification() {
        val text = "Payment of Rs. 1499 for Airtel Postpaid bill successful from Axis Bank."
        val result = parser.parse(text)

        assertNotNull(result)
        assertEquals(149900L, result?.amountCents)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(4L, result?.suggestedCategoryId) // Bills
    }

    @Test
    fun testShoppingAndEntertainmentNotifications() {
        val shoppingText = "Rs. 2,999.00 paid to Amazon India for order #123 on 24-Aug."
        val shoppingResult = parser.parse(shoppingText)
        assertNotNull(shoppingResult)
        assertEquals(299900L, shoppingResult?.amountCents)
        assertEquals(5L, shoppingResult?.suggestedCategoryId) // Shopping

        val entText = "Rs 650 debited for BookMyShow tickets."
        val entResult = parser.parse(entText)
        assertNotNull(entResult)
        assertEquals(65000L, entResult?.amountCents)
        assertEquals(6L, entResult?.suggestedCategoryId) // Entertainment
    }

    @Test
    fun testPhonePeSentToYouCreditNotification() {
        // Real-world user notification: Title = "Yashwanth M", Body = "sent ₹61 to you."
        val title = "Yashwanth M"
        val text = "sent ₹61 to you."
        val result = parser.parse(text = text, title = title, packageName = "com.phonepe.app")

        assertNotNull(result)
        assertEquals(6100L, result?.amountCents)
        assertEquals(TransactionType.CREDIT, result?.type)
        assertEquals("Yashwanth M", result?.merchantOrPayee)
    }

    @Test
    fun testPhonePePaidDebitNotification() {
        val title = "PhonePe"
        val text = "Paid ₹320 to Starbucks India on UPI"
        val result = parser.parse(text = text, title = title, packageName = "com.phonepe.app")

        assertNotNull(result)
        assertEquals(32000L, result?.amountCents)
        assertEquals(TransactionType.DEBIT, result?.type)
        assertEquals(1L, result?.suggestedCategoryId) // Food
    }

    @Test
    fun testCreditRefundNotification() {
        val text = "INR 2,500.00 credited to your A/C 5544 on 24-Aug-26 by refund from Flipkart."
        val result = parser.parse(text)

        assertNotNull(result)
        assertEquals(250000L, result?.amountCents)
        assertEquals(TransactionType.CREDIT, result?.type)
    }

    @Test
    fun testNonFinancialNotificationIgnored() {
        val otpText = "Your OTP for login is 482910. Do not share with anyone."
        val result = parser.parse(otpText)
        assertNull(result)

        val promoText = "Get 50% discount on your next ride with Code RIDE50!"
        val promoResult = parser.parse(promoText)
        assertNull(promoResult)
    }

    @Test
    fun testUnrelatedAppsWithAmountsIgnored() {
        // WhatsApp with amount
        val waResult = parser.parse(
            text = "Hey bro, please send ₹500 for dinner yesterday",
            title = "Rahul",
            packageName = "com.whatsapp"
        )
        assertNull("WhatsApp notification with amount must be ignored", waResult)

        // Instagram with amount
        val instaResult = parser.parse(
            text = "Your cart items worth Rs. 1,499 are waiting!",
            title = "Instagram",
            packageName = "com.instagram.android"
        )
        assertNull("Instagram notification with amount must be ignored", instaResult)

        // Telegram with amount
        val tgResult = parser.parse(
            text = "Earn ₹5000 daily with this investment trick",
            title = "Crypto Channel",
            packageName = "org.telegram.messenger"
        )
        assertNull("Telegram notification with amount must be ignored", tgResult)

        // SMS from non-bank contact (friend sending amount)
        val friendSmsResult = parser.parse(
            text = "I sent ₹250 to Mom",
            title = "Rohan Friend",
            packageName = "com.google.android.apps.messaging"
        )
        assertNull("SMS from non-bank sender must be ignored", friendSmsResult)
    }

    @Test
    fun testSupportedBankAndUpiPackagesParsed() {
        // GPay notification
        val gpayResult = parser.parse(
            text = "Paid ₹450 to Starbucks on Google Pay",
            title = "Google Pay",
            packageName = "com.google.android.apps.nbu.paisa.user"
        )
        assertNotNull(gpayResult)
        assertEquals(45000L, gpayResult?.amountCents)
        assertEquals(TransactionType.DEBIT, gpayResult?.type)

        // HDFC Bank official SMS (TRAI format: VK-HDFCBK)
        val hdfcSmsResult = parser.parse(
            text = "Rs 850.00 debited from A/C **5678 on 27-AUG-26 at Zomato. UPI Ref 928371. Avl Bal Rs 15,400.00",
            title = "VK-HDFCBK",
            packageName = "com.google.android.apps.messaging"
        )
        assertNotNull(hdfcSmsResult)
        assertEquals(85000L, hdfcSmsResult?.amountCents)
        assertEquals(1L, hdfcSmsResult?.suggestedCategoryId) // Food
    }

    @Test
    fun testDeduplicationEngineAgainstManualShake() = runBlocking {
        populateInitialData(db)
        val now = System.currentTimeMillis()

        // 1. User manually logged ₹450 (45000 cents) via shake at timestamp `now`
        val manualExpense = ExpenseEntity(
            uuid = "manual_shake_uuid_123",
            userId = AppDatabase.DEFAULT_USER_ID,
            categoryId = 1,
            amountCents = 45000L,
            type = "DEBIT",
            source = "MANUAL_SHAKE",
            timestamp = now
        )
        db.expenseDao().insertExpenses(listOf(manualExpense))

        // 2. Bank SMS arrives 45 seconds later for same ₹450
        val bankTxnDuplicate = ParsedBankTransaction(
            amountCents = 45000L,
            type = TransactionType.DEBIT,
            merchantOrPayee = "SWIGGY",
            suggestedCategoryId = 1L,
            timestamp = now + 45_000L,
            rawPackageName = "com.google.android.apps.messaging"
        )

        val result1 = processUseCase(bankTxnDuplicate)
        assertTrue(result1 is ProcessBankNotificationResult.DuplicateIgnored)
        assertEquals("manual_shake_uuid_123", (result1 as ProcessBankNotificationResult.DuplicateIgnored).existingUuid)

        // 3. New bank transaction with different amount ₹890 arrives
        val bankTxnNew = ParsedBankTransaction(
            amountCents = 89000L,
            type = TransactionType.DEBIT,
            merchantOrPayee = "BLINKIT",
            suggestedCategoryId = 3L,
            timestamp = now + 60_000L,
            rawPackageName = "com.google.android.apps.messaging"
        )

        val result2 = processUseCase(bankTxnNew)
        assertTrue(result2 is ProcessBankNotificationResult.Saved)
        assertEquals(89000L, (result2 as ProcessBankNotificationResult.Saved).expense.amountCents)
        assertEquals(3L, result2.expense.categoryId)
    }
}
