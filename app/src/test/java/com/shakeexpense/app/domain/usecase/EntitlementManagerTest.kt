package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.model.SubscriptionStatus
import com.shakeexpense.app.domain.model.UserEntitlement
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementManagerTest {

    @Test
    fun testFreePlan_doesNotAllowAdvancedSearchOrCsvExport() {
        val manager = EntitlementManager(UserEntitlement(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE))

        assertFalse(manager.hasFeature(FeatureCapability.ADVANCED_SEARCH))
        assertFalse(manager.hasFeature(FeatureCapability.UNUSUAL_SPENDING_ALERTS))
        assertFalse(manager.hasFeature(FeatureCapability.CSV_EXPORT))
        assertFalse(manager.hasFeature(FeatureCapability.AI_ASSISTANT))
        assertFalse(manager.hasFeature(FeatureCapability.EXPENSE_PREDICTION))

        assertFalse(manager.hasFeature(FeatureCapability.SAFE_TO_SPEND))
        assertFalse(manager.hasFeature(FeatureCapability.FINANCIAL_SAFETY_SCORE))
    }

    @Test
    fun testPlusPlan_allowsIndividualIntelligence() {
        val manager = EntitlementManager(UserEntitlement(SubscriptionPlan.PLUS, SubscriptionStatus.ACTIVE))

        assertTrue(manager.hasFeature(FeatureCapability.ADVANCED_SEARCH))
        assertTrue(manager.hasFeature(FeatureCapability.CSV_EXPORT))
        assertTrue(manager.hasFeature(FeatureCapability.EXPENSE_PREDICTION))
        assertTrue(manager.hasFeature(FeatureCapability.EDITABLE_SPENDING_LIMIT))
        assertTrue(manager.hasFeature(FeatureCapability.AI_ASSISTANT))

        // Family specific controls remain restricted
        assertFalse(manager.hasFeature(FeatureCapability.FAMILY_LIMIT_ALERTS))
    }

    @Test
    fun testFamilyProPlan_allowsAllCapabilities() {
        val manager = EntitlementManager(UserEntitlement(SubscriptionPlan.FAMILY_PRO, SubscriptionStatus.ACTIVE))

        assertTrue(manager.hasFeature(FeatureCapability.ADVANCED_SEARCH))
        assertTrue(manager.hasFeature(FeatureCapability.CSV_EXPORT))
        assertTrue(manager.hasFeature(FeatureCapability.FAMILY_LIMIT_ALERTS))
        assertTrue(manager.hasFeature(FeatureCapability.FAMILY_PRIVACY_CONTROLS))
    }
}
