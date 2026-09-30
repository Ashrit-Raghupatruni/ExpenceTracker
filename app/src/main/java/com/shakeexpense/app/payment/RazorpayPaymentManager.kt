package com.shakeexpense.app.payment

import android.app.Activity
import android.util.Log
import com.razorpay.Checkout
import com.shakeexpense.app.domain.model.SubscriptionPlan
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object RazorpayPaymentManager {

    private const val TAG = "RazorpayPaymentManager"

    // Primary Razorpay Key configured for the project
    const val DEFAULT_RAZORPAY_KEY_ID = "rzp_test_TcIgDD93ICy3Ge"

    fun preload(activity: Activity) {
        try {
            Checkout.preload(activity.applicationContext)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to preload Razorpay checkout: ${e.message}")
        }
    }

    fun startPayment(
        activity: Activity,
        targetPlan: SubscriptionPlan,
        isYearly: Boolean,
        userEmail: String? = null,
        userPhone: String? = null,
        customKeyId: String? = null
    ): Boolean {
        val checkout = Checkout()
        val keyId = customKeyId?.ifBlank { DEFAULT_RAZORPAY_KEY_ID } ?: DEFAULT_RAZORPAY_KEY_ID
        checkout.setKeyID(keyId)

        val amountInRupees = if (isYearly) targetPlan.yearlyPriceInr else targetPlan.monthlyPriceInr
        val amountInPaise = amountInRupees * 100L

        return try {
            val options = JSONObject().apply {
                put("name", "ShakeExpense")
                put("description", "Upgrade to ${targetPlan.displayName} (${if (isYearly) "Annual Plan" else "Monthly Plan"})")
                put("image", "https://raw.githubusercontent.com/Ashrit-Raghupatruni/ExpenceTracker/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png")
                put("currency", "INR")
                put("amount", amountPaiseToString(amountInPaise))
                put("theme.color", "#4F46E5")

                val prefill = JSONObject().apply {
                    if (!userEmail.isNullOrBlank()) put("email", userEmail)
                    if (!userPhone.isNullOrBlank()) put("contact", userPhone)
                }
                put("prefill", prefill)

                val retryObj = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 2)
                }
                put("retry", retryObj)

                val notes = JSONObject().apply {
                    put("plan_id", targetPlan.planId)
                    put("plan_name", targetPlan.displayName)
                    put("billing_cycle", if (isYearly) "YEARLY" else "MONTHLY")
                    put("app_version", "4.0.0")
                }
                put("notes", notes)
            }

            checkout.open(activity, options)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating Razorpay checkout: ${e.message}", e)
            false
        }
    }

    private fun amountPaiseToString(amountPaise: Long): String {
        return amountPaise.toString()
    }

    fun verifyPaymentPayload(paymentId: String?): Boolean {
        if (paymentId.isNullOrBlank()) return false
        val trimmed = paymentId.trim()
        return trimmed.length >= 8
    }

    /**
     * Cryptographic signature verification using HMAC-SHA256
     * Signature = HMAC-SHA256(order_id + "|" + payment_id, KEY_SECRET)
     */
    fun verifySignature(
        orderId: String,
        paymentId: String,
        signature: String,
        secret: String
    ): Boolean {
        return try {
            if (orderId.isBlank() || paymentId.isBlank() || signature.isBlank() || secret.isBlank()) {
                return false
            }
            val payload = "$orderId|$paymentId"
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(secretKeySpec)
            val hash = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            val calculatedSignature = hash.joinToString("") { "%02x".format(it) }
            calculatedSignature.equals(signature.trim(), ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "HMAC-SHA256 verification error: ${e.message}")
            false
        }
    }
}

