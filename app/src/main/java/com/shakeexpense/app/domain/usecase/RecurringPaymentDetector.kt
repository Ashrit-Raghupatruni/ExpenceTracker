package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem

data class DetectedSubscription(
    val name: String,
    val amountCents: Long,
    val categoryName: String,
    val frequency: String = "MONTHLY",
    val occurrences: Int
)

class RecurringPaymentDetector {

    private val KNOWN_SUBSCRIPTION_KEYWORDS = listOf(
        "netflix", "spotify", "prime", "hotstar", "youtube", "apple",
        "airtel", "jio", "vi", "wifi", "broadband", "rent", "gym", "cloud",
        "electricity", "bescom", "maintenance", "newspaper", "insurance"
    )

    fun detectRecurring(records: List<ExpenseRecordItem>): List<DetectedSubscription> {
        if (records.isEmpty()) return emptyList()

        val results = mutableListOf<DetectedSubscription>()
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }

        // 1. Keyword-based matching
        val keywordMatches = debits.filter { item ->
            val text = "${item.customName ?: ""} ${item.bankRef ?: ""}".lowercase()
            KNOWN_SUBSCRIPTION_KEYWORDS.any { text.contains(it) }
        }.groupBy { (it.customName ?: it.categoryName).trim() }

        for ((name, items) in keywordMatches) {
            val mostCommonAmount = items.groupBy { it.amountCents }
                .maxByOrNull { it.value.size }?.key ?: items.first().amountCents
            results.add(
                DetectedSubscription(
                    name = name,
                    amountCents = mostCommonAmount,
                    categoryName = items.first().categoryName,
                    occurrences = items.size
                )
            )
        }

        // 2. Frequency / identical amount matching across different months
        val identicalAmountGroups = debits.groupBy { Pair(it.amountCents, it.categoryId) }
            .filter { it.value.size >= 2 }

        for ((pair, items) in identicalAmountGroups) {
            val name = items.first().customName ?: items.first().categoryName
            if (results.none { it.amountCents == pair.first && it.name.equals(name, ignoreCase = true) }) {
                results.add(
                    DetectedSubscription(
                        name = name,
                        amountCents = pair.first,
                        categoryName = items.first().categoryName,
                        occurrences = items.size
                    )
                )
            }
        }

        return results.distinctBy { Pair(it.name.lowercase(), it.amountCents) }
    }
}
