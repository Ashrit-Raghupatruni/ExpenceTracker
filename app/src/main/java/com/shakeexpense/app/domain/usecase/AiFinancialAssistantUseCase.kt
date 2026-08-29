package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import java.util.Calendar

data class AiAssistantResponse(
    val answer: String,
    val highlights: List<String> = emptyList(),
    val isGated: Boolean = false
)

class AiFinancialAssistantUseCase(
    private val entitlementManager: EntitlementManager = EntitlementManager()
) {

    fun ask(
        question: String,
        plan: SubscriptionPlan,
        records: List<ExpenseRecordItem>,
        monthlyIncomeCents: Long = 0L,
        monthlySafeDailyCents: Long = 0L
    ): AiAssistantResponse {
        if (!entitlementManager.canAccess(plan, FeatureCapability.AI_ASSISTANT)) {
            return AiAssistantResponse(
                answer = "AI Financial Assistant is a PLUS feature. Upgrade to PLUS to ask interactive questions and receive personalized spending intelligence.",
                isGated = true
            )
        }

        val q = question.lowercase().trim()
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val thisMonthDebits = records.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == currentMonth &&
                cal.get(Calendar.YEAR) == currentYear &&
                it.transactionType.equals("DEBIT", ignoreCase = true)
        }

        val lastMonth = if (currentMonth == 0) 11 else currentMonth - 1
        val lastMonthYear = if (currentMonth == 0) currentYear - 1 else currentYear
        val lastMonthDebits = records.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == lastMonth &&
                cal.get(Calendar.YEAR) == lastMonthYear &&
                it.transactionType.equals("DEBIT", ignoreCase = true)
        }

        val thisMonthTotalCents = thisMonthDebits.sumOf { it.amountCents }
        val lastMonthTotalCents = lastMonthDebits.sumOf { it.amountCents }

        // 1. Can I afford ₹[amount]? (Can evaluate from budget runway even before records logged)
        val affordRegex = Regex("""(?:afford|spend|buy)\s*(?:₹|rs\.?|inr)?\s*(\d+)""")
        val affordMatch = affordRegex.find(q)
        if (affordMatch != null || q.contains("afford")) {
            val requestedAmount = affordMatch?.groupValues?.get(1)?.toLongOrNull() ?: 5000L
            val requestedCents = requestedAmount * 100L

            val hasIncomeOrSafeData = monthlySafeDailyCents > 0L || monthlyIncomeCents > 0L
            if (!hasIncomeOrSafeData) {
                return AiAssistantResponse(
                    answer = "To calculate whether you can afford ₹$requestedAmount, please configure your monthly income in the Safe-to-Spend setup. Without an established income or budget, I cannot accurately determine your safe spending runway.",
                    highlights = listOf(
                        "Affordability: Needs Income Data",
                        "Action: Set Monthly Income in Setup",
                        "Current Data: No income recorded"
                    )
                )
            }

            val weekRunwayCents = if (monthlySafeDailyCents > 0L) {
                monthlySafeDailyCents * 7L
            } else {
                val remainingMonthBudget = (monthlyIncomeCents - thisMonthTotalCents).coerceAtLeast(0L)
                (remainingMonthBudget / 4L)
            }

            return if (requestedCents <= weekRunwayCents) {
                val remainingAfter = (weekRunwayCents - requestedCents) / 100
                AiAssistantResponse(
                    answer = "Yes! You can comfortably afford ₹$requestedAmount this week. After this purchase, your remaining safe buffer for the week will be approximately ₹$remainingAfter.",
                    highlights = listOf(
                        "Affordable: Yes",
                        "Estimated Weekly Safe Runway: ₹${weekRunwayCents / 100}",
                        "Buffer After Purchase: ₹$remainingAfter"
                    )
                )
            } else {
                val shortfall = (requestedCents - weekRunwayCents) / 100
                AiAssistantResponse(
                    answer = "Caution: Spending ₹$requestedAmount exceeds your estimated weekly safe buffer (₹${weekRunwayCents / 100}) by ₹$shortfall. If you make this purchase, consider cutting non-essential spending for the next few days to stay on budget.",
                    highlights = listOf(
                        "Affordable: Tight / Caution",
                        "Weekly Safe Runway: ₹${weekRunwayCents / 100}",
                        "Exceeds Buffer By: ₹$shortfall"
                    )
                )
            }
        }

        if (thisMonthDebits.isEmpty() && records.isEmpty()) {
            return AiAssistantResponse(
                answer = "You haven't recorded any expenses yet. Once you log transactions, I can analyze where your money goes and give personalized advice!",
                highlights = listOf("No expenses logged yet")
            )
        }

        // 2. Where did most of my money go?
        if (q.contains("where") || q.contains("most") || q.contains("top")) {
            val byCategory = thisMonthDebits.groupBy { it.categoryName }
                .mapValues { (_, items) -> items.sumOf { it.amountCents } }
                .toList()
                .sortedByDescending { it.second }

            if (byCategory.isEmpty()) {
                return AiAssistantResponse(
                    answer = "No expenses recorded this month yet.",
                    highlights = emptyList()
                )
            }

            val topCat = byCategory.first()
            val topPercentage = if (thisMonthTotalCents > 0) (topCat.second * 100 / thisMonthTotalCents) else 0
            val topCategoryAmount = topCat.second / 100

            val topCategoryItems = thisMonthDebits.filter { it.categoryName == topCat.first }
                .sortedByDescending { it.amountCents }
                .take(3)
                .map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }

            return AiAssistantResponse(
                answer = "Most of your money went towards ${topCat.first} this month: ₹$topCategoryAmount ($topPercentage% of your total spending).\n\nTop expenses in this category:\n" + topCategoryItems.joinToString("\n"),
                highlights = listOf(
                    "Top Category: ${topCat.first} (₹$topCategoryAmount)",
                    "Share: $topPercentage% of monthly expenses",
                    "Total Spending: ₹${thisMonthTotalCents / 100}"
                )
            )
        }

        // 3. How much can I save if I reduce food/delivery?
        if (q.contains("food") || q.contains("delivery") || q.contains("zomato") || q.contains("swiggy") || q.contains("save")) {
            val foodCents = thisMonthDebits.filter {
                it.categoryName.equals("Food", ignoreCase = true) ||
                    (it.customName?.lowercase()?.contains("swiggy") == true) ||
                    (it.customName?.lowercase()?.contains("zomato") == true)
            }.sumOf { it.amountCents }

            val foodAmount = foodCents / 100
            val save20 = (foodCents * 20 / 100) / 100
            val save30 = (foodCents * 30 / 100) / 100
            val annualSavings = save30 * 12

            return AiAssistantResponse(
                answer = "You have spent ₹$foodAmount on food & dining this month.\n\n• Reducing by 20% saves: ₹$save20/month\n• Reducing by 30% saves: ₹$save30/month (₹$annualSavings/year!)\n\nCooking at home or ordering fewer times per week can quickly boost your savings target.",
                highlights = listOf(
                    "Current Food Spending: ₹$foodAmount",
                    "Monthly 30% Savings: ₹$save30",
                    "Annual Projected Savings: ₹$annualSavings"
                )
            )
        }

        // 4. What are my unnecessary/discretionary expenses?
        if (q.contains("unnecessary") || q.contains("discretionary") || q.contains("waste") || q.contains("extra")) {
            val discretionary = thisMonthDebits.filter {
                it.categoryName.equals("Shopping", ignoreCase = true) ||
                    it.categoryName.equals("Entertainment", ignoreCase = true) ||
                    it.categoryName.equals("Others", ignoreCase = true)
            }
            val discretionaryTotal = discretionary.sumOf { it.amountCents } / 100

            val topItems = discretionary.sortedByDescending { it.amountCents }.take(4)
                .map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }

            return AiAssistantResponse(
                answer = "Your discretionary spending (Shopping, Entertainment, Others) is ₹$discretionaryTotal this month across ${discretionary.size} transactions.\n\nKey discretionary items:\n" +
                    (if (topItems.isNotEmpty()) topItems.joinToString("\n") else "No major discretionary purchases recorded."),
                highlights = listOf(
                    "Total Discretionary: ₹$discretionaryTotal",
                    "Discretionary Count: ${discretionary.size}"
                )
            )
        }

        // 5. Why did I spend more this month? / Comparison
        if (q.contains("why") || q.contains("more") || q.contains("compare") || q.contains("last month")) {
            val diffCents = thisMonthTotalCents - lastMonthTotalCents
            val diffRupees = Math.abs(diffCents) / 100

            val catDiffs = thisMonthDebits.groupBy { it.categoryName }.mapValues { (_, items) -> items.sumOf { it.amountCents } }
                .map { (cat, thisCents) ->
                    val lastCents = lastMonthDebits.filter { it.categoryName == cat }.sumOf { it.amountCents }
                    Triple(cat, (thisCents - lastCents) / 100, thisCents / 100)
                }.sortedByDescending { it.second }

            val topIncrease = catDiffs.firstOrNull { it.second > 0 }

            val answerText = if (diffCents > 0) {
                "You spent ₹$diffRupees more this month than last month (₹${thisMonthTotalCents / 100} vs ₹${lastMonthTotalCents / 100})." +
                    if (topIncrease != null) "\n\nThe biggest increase was in **${topIncrease.first}**, where you spent ₹${topIncrease.second} more than last month." else ""
            } else {
                "Great news! You spent ₹$diffRupees less this month than last month (₹${thisMonthTotalCents / 100} vs ₹${lastMonthTotalCents / 100}). Keep up the disciplined spending!"
            }

            return AiAssistantResponse(
                answer = answerText,
                highlights = listOf(
                    "This Month: ₹${thisMonthTotalCents / 100}",
                    "Last Month: ₹${lastMonthTotalCents / 100}",
                    "Difference: " + (if (diffCents >= 0) "+₹$diffRupees" else "-₹$diffRupees")
                )
            )
        }

        // General Overview
        return AiAssistantResponse(
            answer = "Here is your current financial snapshot:\n\n• Total Spending this month: ₹${thisMonthTotalCents / 100} across ${thisMonthDebits.size} transactions.\n• Safe daily runway: ₹${monthlySafeDailyCents / 100}/day.\n\nAsk me specific questions like:\n• 'Where did most of my money go?'\n• 'Can I afford ₹2,000 this week?'\n• 'How much can I save if I reduce food delivery?'\n• 'Why did I spend more this month?'",
            highlights = listOf(
                "Month Total: ₹${thisMonthTotalCents / 100}",
                "Transactions: ${thisMonthDebits.size}",
                "Safe Runway: ₹${monthlySafeDailyCents / 100}/day"
            )
        )
    }
}
