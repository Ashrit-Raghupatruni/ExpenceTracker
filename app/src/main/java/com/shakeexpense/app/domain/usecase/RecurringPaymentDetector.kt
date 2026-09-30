package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import java.util.regex.Pattern

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
        // Unambiguous Digital Subscriptions & Paid Memberships (can detect even from 1 occurrence with exact brand match)
        private val EXPLICIT_SUBSCRIPTION_BRANDS = listOf(
            "netflix", "spotify", "hotstar", "disney\\+?", "youtube premium", "apple music", "sonyliv",
            "zee5", "jiocinema", "crunchyroll", "audible", "icloud", "google one", "chatgpt", "copilot",
            "dropbox", "notion", "midjourney", "figma", "canva", "adobe", "playstation", "xbox",
            "cult\\.fit", "prime video", "amazon prime"
        )

        // Utility and Bill services (Require either Category == Bills or at least 2 occurrences)
        private val EXPLICIT_BILL_SERVICES = listOf(
            "broadband", "wifi bill", "electricity bill", "water bill", "gas bill", "lpg cylinder",
            "house rent", "maintenance charge", "tata play", "dth recharge", "airtel broadband",
            "jio fiber", "act fibernet", "bescom", "tneb", "msedcl", "gym membership"
        )

        // Yearly Subscriptions
        private val KNOWN_YEARLY_KEYWORDS = listOf(
            "prime", "amazon prime", "insurance", "lic", "star health", "hdfc ergo",
            "car insurance", "bike insurance", "term insurance", "annual maintenance", "hosting", "domain"
        )

        private val GENERIC_EXCLUDE_TERMS = setOf(
            "food", "transport", "groceries", "grocery", "shopping", "others", "expense", "general",
            "bottle", "water bottle", "snack", "tea", "coffee", "lunch", "dinner", "breakfast", "chocolat", "chocolate", "bonda"
        )
    }

    fun detectRecurring(records: List<ExpenseRecordItem>): List<DetectedSubscription> {
        if (records.isEmpty()) return emptyList()

        val results = mutableListOf<DetectedSubscription>()
        val debits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }

        // 1. Match Against Explicit Brand Subscriptions
        val explicitPatterns = EXPLICIT_SUBSCRIPTION_BRANDS.map { Pattern.compile("(?i)\\b$it\\b") }
        val billPatterns = EXPLICIT_BILL_SERVICES.map { Pattern.compile("(?i)\\b$it\\b") }

        val brandGroups = debits.filter { item ->
            val text = "${item.customName ?: ""} ${item.bankRef ?: ""}".trim()
            if (text.isBlank()) false
            else {
                // Must not be a generic excluded term
                val lower = text.lowercase()
                if (GENERIC_EXCLUDE_TERMS.contains(lower)) return@filter false

                // Check explicit brands
                val matchesBrand = explicitPatterns.any { it.matcher(text).find() }
                // Check bill services only if category is Bills or occurrences >= 2
                val matchesBill = billPatterns.any { it.matcher(text).find() } &&
                        (item.categoryName.equals("Bills", ignoreCase = true) || debits.count { d -> (d.customName ?: "").contains(text, ignoreCase = true) } >= 2)

                matchesBrand || matchesBill
            }
        }.groupBy { (it.customName ?: it.bankRef ?: "").trim() }

        for ((rawName, items) in brandGroups) {
            val cleanName = rawName.ifBlank { items.first().categoryName }
            val mostRecent = items.maxByOrNull { it.timestamp } ?: items.first()
            val mostCommonAmount = items.groupBy { it.amountCents }.maxByOrNull { it.value.size }?.key ?: items.first().amountCents

            val lower = cleanName.lowercase()
            val isYearly = lower.contains("annual") || lower.contains("yearly") || lower.contains("12 month") ||
                    (KNOWN_YEARLY_KEYWORDS.any { lower.contains(it) } && !lower.contains("monthly"))
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

        // 2. Strict Evidence-Based Periodic Interval Matching (for regular recurring expenses)
        // Groups by non-generic name and identical amount, strictly requiring >= 2 periodic occurrences
        val merchantGroups = debits.filter {
            val name = (it.customName ?: "").trim().lowercase()
            name.isNotBlank() && !GENERIC_EXCLUDE_TERMS.contains(name) && !name.contains("bottle") && !name.contains("water bottle")
        }.groupBy { Pair((it.customName ?: "").trim().lowercase(), it.amountCents) }

        for ((pair, items) in merchantGroups) {
            val (nameLower, amountCents) = pair
            if (results.any { it.name.lowercase() == nameLower && it.amountCents == amountCents }) continue

            // Require at least 2 occurrences spaced 25 to 35 days apart (monthly) or 350 to 380 days apart (yearly)
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


