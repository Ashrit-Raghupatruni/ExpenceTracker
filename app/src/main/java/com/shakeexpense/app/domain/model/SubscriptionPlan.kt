package com.shakeexpense.app.domain.model

enum class SubscriptionPlan(
    val planId: String,
    val displayName: String,
    val monthlyPriceInr: Int,
    val yearlyPriceInr: Int,
    val level: Int
) {
    FREE("free", "FREE", 0, 0, 0),
    PLUS("plus", "PLUS", 59, 699, 1),
    FAMILY_PRO("family_pro", "FAMILY PRO", 99, 999, 2);

    fun canUpgradeTo(target: SubscriptionPlan): Boolean = target.level > this.level
}

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    GRACE_PERIOD,
    PAUSED
}

enum class FeatureCapability {
    ADVANCED_SEARCH,
    UNUSUAL_SPENDING_ALERTS,
    CSV_EXPORT,
    AI_ASSISTANT,
    EXPENSE_PREDICTION,
    EDITABLE_SPENDING_LIMIT,
    SAFE_TO_SPEND,
    FINANCIAL_SAFETY_SCORE,
    RECURRING_DETECTION,
    AI_MONTHLY_REPORT,
    FAMILY_DASHBOARD,
    FAMILY_LIMIT_ALERTS,
    FAMILY_PRIVACY_CONTROLS,
    SHARED_FAMILY_BUDGETS,
    FAMILY_REPORTS
}

data class UserEntitlement(
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val expiryTimestamp: Long? = null,
    val isAutoRenewing: Boolean = false
)
