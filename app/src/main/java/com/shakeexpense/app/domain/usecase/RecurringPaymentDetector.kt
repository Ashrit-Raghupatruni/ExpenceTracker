package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import java.util.Calendar

data class DetectedSubscription(
    val name: String,
    val amountCents: Long,
    val categoryName: String,
    val frequency: String = "MONTHLY", // "MONTHLY" or "YEARLY"
    val occurrences: Int,
    val lastChargedTimestamp: Long = System.currentTimeMillis(),
    val nextDueTimestamp: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L)
)

class RecurringPaymentDetector {

    companion object {
        private val KNOWN_MONTHLY_SUBSCRIPTIONS = listOf(
            "netflix", "spotify", "hotstar", "youtube", "apple music", "sonyliv", "zee5", "jiocinema",
            "crunchyroll", "audible", "icloud", "google one", "chatgpt", "github", "dropbox", "notion",
            "airtel", "jio", "vi", "wifi", "broadband", "act fibernet", "tata play", "dth",
            "electricity", "bescom", "tneb", "msedcl", "water", "gas", "rent", "maintenance", "gym", "cult.fit"
        )

        private val KNOWN_YEARLY_SUBSCRIPTIONS = listOf(
            "prime", "amazon prime", "disney", "insurance", "lic", "star health", "hdfc ergo",
            "vehicle insurance", "car insurance", "bike insurance", "term insurance", "annual maintenance", "hosting", "domain"
        )

        private val GENERIC_CATEGORY_NAMES = setOf(
            "food", "transport", "groceries", "grocery", "bills", "shopping", "entertainment", "others", "expense", "general"
        )
    }

    fun detectRecurring(records: List<ExpenseRecordItem>): List<DetectedSubscription> {
        if (records.isEmpty()) return emptyList()

        val results = mutableListOf<DetectedSubscription>()
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }

        // 1. Match Against Known Subscription & Utility Signatures
        val allKnownKeywords = KNOWN_MONTHLY_SUBSCRIPTIONS + KNOWN_YEARLY_SUBSCRIPTIONS
        val keywordGroups = debits.filter { item ->
            val text = "${item.customName ?: ""} ${item.bankRef ?: ""}".lowercase()
            allKnownKeywords.any { text.contains(it) }
        }.groupBy { (it.customName ?: it.bankRef ?: "").trim() }

        for ((rawName, items) in keywordGroups) {
            val cleanName = rawName.ifBlank { items.first().categoryName }
            val mostRecent = items.maxByOrNull { it.timestamp } ?: items.first()
            val mostCommonAmount = items.groupBy { it.amountCents }.maxByOrNull { it.value.size }?.key ?: items.first().amountCents

            val lower = cleanName.lowercase()
            val isYearly = KNOWN_YEARLY_SUBSCRIPTIONS.any { lower.contains(it) } && !KNOWN_MONTHLY_SUBSCRIPTIONS.any { lower.contains(it) }
            val cadence = if (isYearly) "YEARLY" else "MONTHLY"

            val nextDue = if (cadence == "YEARLY") {
                mostRecent.timestamp + (365L * 24 * 60 * 60 * 1000L)
            } else {
                mostRecent.timestamp + (30L * 24 * 60 * 60 * 1000L)
            }

            results.add(
                DetectedSubscription(
                    name = cleanName.replaceFirstChar { it.uppercase() },
                    amountCents = mostCommonAmount,
                    categoryName = items.first().categoryName,
                    frequency = cadence,
                    occurrences = items.size,
                    lastChargedTimestamp = mostRecent.timestamp,
                    nextDueTimestamp = nextDue
                )
            )
        }

        // 2. Strict Evidence-Based Periodic Interval Matching (for non-keyword items)
        // Group by customName/merchant name and identical amount
        val merchantGroups = debits.filter {
            val name = (it.customName ?: "").trim().lowercase()
            name.isNotBlank() && !GENERIC_CATEGORY_NAMES.contains(name)
        }.groupBy { Pair((it.customName ?: "").trim().lowercase(), it.amountCents) }

        for ((pair, items) in merchantGroups) {
            val (nameLower, amountCents) = pair
            if (results.any { it.name.lowercase() == nameLower && it.amountCents == amountCents }) continue

            // Require at least 2 occurrences
            if (items.size >= 2) {
                val sortedItems = items.sortedBy { it.timestamp }
                var hasPeriodicInterval = false
                var detectedCadence = "MONTHLY"

                for (i in 0 until sortedItems.size - 1) {
                    val deltaMs = sortedItems[i + 1].timestamp - sortedItems[i].timestamp
                    val deltaDays = deltaMs / (1000L * 60 * 60 * 24)

                    // Monthly recurrence: 25 to 35 days apart
                    if (deltaDays in 25..35) {
                        hasPeriodicInterval = true
                        detectedCadence = "MONTHLY"
                        break
                    }
                    // Yearly recurrence: 350 to 380 days apart
                    if (deltaDays in 350..380) {
                        hasPeriodicInterval = true
                        detectedCadence = "YEARLY"
                        break
                    }
                }

                if (hasPeriodicInterval) {
                    val mostRecent = sortedItems.last()
                    val nextDue = if (detectedCadence == "YEARLY") {
                        mostRecent.timestamp + (365L * 24 * 60 * 60 * 1000L)
                    } else {
                        mostRecent.timestamp + (30L * 24 * 60 * 60 * 1000L)
                    }

                    results.add(
                        DetectedSubscription(
                            name = (items.first().customName ?: nameLower).replaceFirstChar { it.uppercase() },
                            amountCents = amountCents,
                            categoryName = items.first().categoryName,
                            frequency = detectedCadence,
                            occurrences = items.size,
                            lastChargedTimestamp = mostRecent.timestamp,
                            nextDueTimestamp = nextDue
                        )
                    )
                }
            }
        }

        return results.distinctBy { Pair(it.name.lowercase(), it.amountCents) }
    }
}

