package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.model.SubscriptionStatus
import com.shakeexpense.app.domain.model.UserEntitlement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EntitlementManager(
    initialEntitlement: UserEntitlement = UserEntitlement(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE)
) {

    private val _currentEntitlement = MutableStateFlow(initialEntitlement)
    val currentEntitlement: StateFlow<UserEntitlement> = _currentEntitlement.asStateFlow()

    fun updateEntitlement(entitlement: UserEntitlement) {
        _currentEntitlement.value = entitlement
    }

    fun updatePlan(plan: SubscriptionPlan) {
        _currentEntitlement.value = UserEntitlement(plan, SubscriptionStatus.ACTIVE)
    }

    fun canAccess(plan: SubscriptionPlan, feature: FeatureCapability): Boolean {
        return when (plan) {
            SubscriptionPlan.FREE -> false
            SubscriptionPlan.PLUS -> {
                when (feature) {
                    FeatureCapability.FAMILY_DASHBOARD,
                    FeatureCapability.FAMILY_LIMIT_ALERTS,
                    FeatureCapability.FAMILY_PRIVACY_CONTROLS,
                    FeatureCapability.SHARED_FAMILY_BUDGETS,
                    FeatureCapability.FAMILY_REPORTS -> false
                    else -> true
                }
            }
            SubscriptionPlan.FAMILY_PRO -> true
        }
    }

    fun hasFeature(feature: FeatureCapability): Boolean {
        val entitlement = _currentEntitlement.value
        if (entitlement.status != SubscriptionStatus.ACTIVE && entitlement.status != SubscriptionStatus.GRACE_PERIOD) {
            return false
        }
        return canAccess(entitlement.plan, feature)
    }
}
