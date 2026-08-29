package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.domain.model.FeatureCapability

data class AdvancedSearchQuery(
    val merchantQuery: String? = null,
    val categoryId: Long? = null,
    val minAmountCents: Long? = null,
    val maxAmountCents: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val transactionType: String? = null, // DEBIT or CREDIT
    val keyword: String? = null
)

class AdvancedTransactionSearchUseCase(
    private val entitlementManager: EntitlementManager
) {

    operator fun invoke(
        expenses: List<ExpenseEntity>,
        query: AdvancedSearchQuery
    ): List<ExpenseEntity> {
        if (!entitlementManager.hasFeature(FeatureCapability.ADVANCED_SEARCH)) {
            // Feature strictly restricted on Free tier
            return emptyList()
        }

        return expenses.filter { item ->
            val matchMerchant = query.merchantQuery.isNullOrBlank() ||
                    item.customName?.contains(query.merchantQuery, ignoreCase = true) == true ||
                    item.bankRef?.contains(query.merchantQuery, ignoreCase = true) == true

            val matchCategory = query.categoryId == null || item.categoryId == query.categoryId

            val matchMin = query.minAmountCents == null || item.amountCents >= query.minAmountCents

            val matchMax = query.maxAmountCents == null || item.amountCents <= query.maxAmountCents

            val matchStart = query.startDate == null || item.timestamp >= query.startDate

            val matchEnd = query.endDate == null || item.timestamp <= query.endDate

            val matchType = query.transactionType.isNullOrBlank() ||
                    item.type.equals(query.transactionType, ignoreCase = true)

            val matchKeyword = query.keyword.isNullOrBlank() ||
                    item.customName?.contains(query.keyword, ignoreCase = true) == true ||
                    item.bankRef?.contains(query.keyword, ignoreCase = true) == true

            matchMerchant && matchCategory && matchMin && matchMax && matchStart && matchEnd && matchType && matchKeyword
        }
    }

    fun filterRecords(
        records: List<com.shakeexpense.app.domain.model.ExpenseRecordItem>,
        query: AdvancedSearchQuery,
        plan: com.shakeexpense.app.domain.model.SubscriptionPlan = com.shakeexpense.app.domain.model.SubscriptionPlan.FREE
    ): List<com.shakeexpense.app.domain.model.ExpenseRecordItem> {
        if (!entitlementManager.canAccess(plan, FeatureCapability.ADVANCED_SEARCH)) {
            return emptyList()
        }

        return records.filter { item ->
            val matchMerchant = query.merchantQuery.isNullOrBlank() ||
                    item.customName?.contains(query.merchantQuery, ignoreCase = true) == true ||
                    item.bankRef?.contains(query.merchantQuery, ignoreCase = true) == true ||
                    item.categoryName.contains(query.merchantQuery, ignoreCase = true)

            val matchCategory = query.categoryId == null || item.categoryId == query.categoryId

            val matchMin = query.minAmountCents == null || item.amountCents >= query.minAmountCents

            val matchMax = query.maxAmountCents == null || item.amountCents <= query.maxAmountCents

            val matchStart = query.startDate == null || item.timestamp >= query.startDate

            val matchEnd = query.endDate == null || item.timestamp <= query.endDate

            val matchType = query.transactionType.isNullOrBlank() ||
                    item.transactionType.equals(query.transactionType, ignoreCase = true)

            val matchKeyword = query.keyword.isNullOrBlank() ||
                    item.customName?.contains(query.keyword, ignoreCase = true) == true ||
                    item.bankRef?.contains(query.keyword, ignoreCase = true) == true ||
                    item.categoryName.contains(query.keyword, ignoreCase = true)

            matchMerchant && matchCategory && matchMin && matchMax && matchStart && matchEnd && matchType && matchKeyword
        }
    }
}
