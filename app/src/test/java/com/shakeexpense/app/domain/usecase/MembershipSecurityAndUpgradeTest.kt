package com.shakeexpense.app.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import com.shakeexpense.app.data.repository.FinancialProfileRepositoryImpl
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.payment.RazorpayPaymentManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MembershipSecurityAndUpgradeTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinancialProfileRepositoryImpl
    private val testUserId = "user_test_security_123"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinancialProfileRepositoryImpl(db.financialProfileDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testPlanLevelHierarchyAndCanUpgradeTo() {
        // FREE (0) -> PLUS (1) -> FAMILY_PRO (2)
        assertEquals(0, SubscriptionPlan.FREE.level)
        assertEquals(1, SubscriptionPlan.PLUS.level)
        assertEquals(2, SubscriptionPlan.FAMILY_PRO.level)

        // Upgrades allowed
        assertTrue(SubscriptionPlan.FREE.canUpgradeTo(SubscriptionPlan.PLUS))
        assertTrue(SubscriptionPlan.FREE.canUpgradeTo(SubscriptionPlan.FAMILY_PRO))
        assertTrue(SubscriptionPlan.PLUS.canUpgradeTo(SubscriptionPlan.FAMILY_PRO))

        // Downgrades or same-level upgrades blocked
        assertFalse(SubscriptionPlan.PLUS.canUpgradeTo(SubscriptionPlan.FREE))
        assertFalse(SubscriptionPlan.FAMILY_PRO.canUpgradeTo(SubscriptionPlan.FREE))
        assertFalse(SubscriptionPlan.FAMILY_PRO.canUpgradeTo(SubscriptionPlan.PLUS))
        assertFalse(SubscriptionPlan.PLUS.canUpgradeTo(SubscriptionPlan.PLUS))
        assertFalse(SubscriptionPlan.FAMILY_PRO.canUpgradeTo(SubscriptionPlan.FAMILY_PRO))
    }

    @Test
    fun testNonDegradationRuleBlocksDowngradeInRepository() = runBlocking {
        // 1. Initial State: User starts on PLUS tier
        db.financialProfileDao().insertOrUpdateProfile(
            FinancialProfileEntity(
                userId = testUserId,
                tier = "PLUS",
                monthlyIncomeCents = 5000000L,
                savingsTargetCents = 1000000L,
                billingCycleDay = 1
            )
        )

        // Verify tier is PLUS
        var profile = repository.getProfile(testUserId)
        assertEquals("PLUS", profile?.tier)

        // 2. Attempt malicious or erroneous downgrade to FREE
        repository.updateTier(testUserId, "FREE")

        // Assert tier is STILL PLUS (downgrade was rejected)
        profile = repository.getProfile(testUserId)
        assertEquals("PLUS", profile?.tier)

        // 3. Legitimate upgrade to FAMILY_PRO
        repository.updateTier(testUserId, "FAMILY_PRO")

        // Assert tier is updated to FAMILY_PRO
        profile = repository.getProfile(testUserId)
        assertEquals("FAMILY_PRO", profile?.tier)

        // 4. Attempt downgrade from FAMILY_PRO to PLUS
        repository.updateTier(testUserId, "PLUS")

        // Assert tier remains FAMILY_PRO (downgrade rejected)
        profile = repository.getProfile(testUserId)
        assertEquals("FAMILY_PRO", profile?.tier)
    }

    @Test
    fun testRazorpayPaymentPayloadValidation() {
        // Valid payment IDs
        assertTrue(RazorpayPaymentManager.verifyPaymentPayload("pay_Mno12345678"))
        assertTrue(RazorpayPaymentManager.verifyPaymentPayload("pay_test_987654321"))

        // Invalid payment IDs
        assertFalse(RazorpayPaymentManager.verifyPaymentPayload(null))
        assertFalse(RazorpayPaymentManager.verifyPaymentPayload(""))
        assertFalse(RazorpayPaymentManager.verifyPaymentPayload("   "))
        assertFalse(RazorpayPaymentManager.verifyPaymentPayload("short"))
    }

    @Test
    fun testHmacSha256SignatureVerification() {
        val orderId = "order_DBJOWzybf0sJbb"
        val paymentId = "pay_29QQoUBi66xm2f"
        val secret = "aftQl5GtgPdAhcxYuCuLmw1v"

        // Calculate expected signature using standard HMAC-SHA256
        val payload = "$orderId|$paymentId"
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val expectedSignature = mac.doFinal(payload.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

        // Valid signature matches
        assertTrue(RazorpayPaymentManager.verifySignature(orderId, paymentId, expectedSignature, secret))

        // Tampered signature or mismatched secret fails
        assertFalse(RazorpayPaymentManager.verifySignature(orderId, paymentId, "invalid_tampered_signature", secret))
        assertFalse(RazorpayPaymentManager.verifySignature(orderId, paymentId, expectedSignature, "wrong_secret"))
    }
}

