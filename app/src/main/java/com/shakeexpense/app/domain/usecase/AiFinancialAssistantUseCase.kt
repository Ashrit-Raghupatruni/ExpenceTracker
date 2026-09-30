package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FeatureCapability
import com.shakeexpense.app.domain.model.SubscriptionPlan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        val currentDayOfYear = now.get(Calendar.DAY_OF_YEAR)

        // Date range filters
        val allDebits = records.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
        val allCredits = records.filter { it.transactionType.equals("CREDIT", ignoreCase = true) }

        val thisMonthDebits = allDebits.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val lastMonth = if (currentMonth == 0) 11 else currentMonth - 1
        val lastMonthYear = if (currentMonth == 0) currentYear - 1 else currentYear
        val lastMonthDebits = allDebits.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == lastMonthYear
        }

        val todayDebits = allDebits.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.DAY_OF_YEAR) == currentDayOfYear && cal.get(Calendar.YEAR) == currentYear
        }

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayDebits = allDebits.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)
        }

        val weekThreshold = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000L)
        val thisWeekDebits = allDebits.filter { it.timestamp >= weekThreshold }

        val thisMonthTotalCents = thisMonthDebits.sumOf { it.amountCents }
        val lastMonthTotalCents = lastMonthDebits.sumOf { it.amountCents }
        val todayTotalCents = todayDebits.sumOf { it.amountCents }
        val yesterdayTotalCents = yesterdayDebits.sumOf { it.amountCents }
        val thisWeekTotalCents = thisWeekDebits.sumOf { it.amountCents }
        val allTimeTotalDebitCents = allDebits.sumOf { it.amountCents }
        val allTimeTotalCreditCents = allCredits.sumOf { it.amountCents }

        // 1. Affordability Query ("Can I afford ₹X?")
        val affordRegex = Regex("""(?:afford|spend|buy)\s*(?:₹|rs\.?|inr)?\s*(\d+)""")
        val affordMatch = affordRegex.find(q)
        if (affordMatch != null || (q.contains("afford") && !q.contains("how"))) {
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
                    answer = "Yes! You can comfortably afford ₹$requestedAmount this week. After this purchase, your estimated remaining safe buffer for the week will be approximately ₹$remainingAfter.",
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

        // 2. Time-Specific Queries (Today, Yesterday, This Week, All Time)
        if (q.contains("today")) {
            val items = todayDebits.take(4).map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }
            return AiAssistantResponse(
                answer = "Today you have spent ₹${todayTotalCents / 100} across ${todayDebits.size} transactions." +
                        (if (items.isNotEmpty()) "\n\nRecent items today:\n" + items.joinToString("\n") else "") +
                        if (monthlySafeDailyCents > 0) "\n\nYour safe daily runway is ₹${monthlySafeDailyCents / 100}." else "",
                highlights = listOf(
                    "Today's Spending: ₹${todayTotalCents / 100}",
                    "Transactions Today: ${todayDebits.size}",
                    "Daily Safe Runway: ₹${monthlySafeDailyCents / 100}"
                )
            )
        }

        if (q.contains("yesterday")) {
            val items = yesterdayDebits.take(4).map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }
            return AiAssistantResponse(
                answer = "Yesterday you spent ₹${yesterdayTotalCents / 100} across ${yesterdayDebits.size} transactions." +
                        (if (items.isNotEmpty()) "\n\nItems recorded yesterday:\n" + items.joinToString("\n") else ""),
                highlights = listOf(
                    "Yesterday's Total: ₹${yesterdayTotalCents / 100}",
                    "Transactions: ${yesterdayDebits.size}"
                )
            )
        }

        if (q.contains("this week") || q.contains("last 7 days") || q.contains("weekly")) {
            val byCat = thisWeekDebits.groupBy { it.categoryName }.mapValues { (_, v) -> v.sumOf { it.amountCents } / 100 }
            val catBreakdown = byCat.entries.sortedByDescending { it.value }.take(3).map { "• ${it.key}: ₹${it.value}" }
            return AiAssistantResponse(
                answer = "In the last 7 days, you spent ₹${thisWeekTotalCents / 100} across ${thisWeekDebits.size} transactions.\n\nTop categories this week:\n" +
                        catBreakdown.joinToString("\n"),
                highlights = listOf(
                    "7-Day Total: ₹${thisWeekTotalCents / 100}",
                    "7-Day Transactions: ${thisWeekDebits.size}"
                )
            )
        }

        if (q.contains("all time") || q.contains("total balance") || q.contains("total spent") || q.contains("net balance")) {
            val netCents = allTimeTotalCreditCents - allTimeTotalDebitCents
            val netFormatted = if (netCents >= 0) "+₹${netCents / 100}" else "-₹${Math.abs(netCents) / 100}"
            return AiAssistantResponse(
                answer = "All-time financial record summary:\n\n• Total Debits (Expenses): ₹${allTimeTotalDebitCents / 100} (${allDebits.size} entries)\n• Total Credits (Income/Refunds): ₹${allTimeTotalCreditCents / 100} (${allCredits.size} entries)\n• Net Balance Tracked: $netFormatted",
                highlights = listOf(
                    "Total Expenses: ₹${allTimeTotalDebitCents / 100}",
                    "Total Credits: ₹${allTimeTotalCreditCents / 100}",
                    "Net Tracked: $netFormatted"
                )
            )
        }

        // 3. Category Specific Inquiries (Food, Transport, Groceries, Bills, Shopping, Entertainment, Health, Education)
        val categoryKeywords = mapOf(
            "food" to listOf("food", "dining", "swiggy", "zomato", "restaurant", "cafe", "snack"),
            "transport" to listOf("transport", "uber", "ola", "metro", "fuel", "petrol", "cab", "travel"),
            "groceries" to listOf("grocery", "groceries", "blinkit", "zepto", "supermarket", "instamart", "bigbasket"),
            "bills" to listOf("bills", "bill", "electricity", "rent", "wifi", "broadband", "airtel", "jio", "water"),
            "shopping" to listOf("shopping", "amazon", "flipkart", "clothes", "myntra", "zara"),
            "entertainment" to listOf("entertainment", "movies", "cinema", "netflix", "spotify", "gaming", "prime")
        )

        for ((catName, keywords) in categoryKeywords) {
            if (keywords.any { q.contains(it) }) {
                val catDebits = thisMonthDebits.filter { item ->
                    item.categoryName.equals(catName, ignoreCase = true) ||
                            keywords.any { kw -> item.customName?.lowercase()?.contains(kw) == true }
                }
                val catTotal = catDebits.sumOf { it.amountCents } / 100
                val percent = if (thisMonthTotalCents > 0) (catDebits.sumOf { it.amountCents } * 100 / thisMonthTotalCents) else 0
                val topItems = catDebits.sortedByDescending { it.amountCents }.take(4)
                    .map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }

                return AiAssistantResponse(
                    answer = "You have spent ₹$catTotal on **${catName.replaceFirstChar { it.uppercase() }}** this month ($percent% of your total expenses across ${catDebits.size} transactions).\n\n" +
                            (if (topItems.isNotEmpty()) "Top items:\n" + topItems.joinToString("\n") else "No individual item notes recorded in this category."),
                    highlights = listOf(
                        "${catName.replaceFirstChar { it.uppercase() }} Total: ₹$catTotal",
                        "Share: $percent% of monthly spending",
                        "Transactions: ${catDebits.size}"
                    )
                )
            }
        }

        // 4. 50/30/20 Budgeting Rule & Savings Optimization
        if (q.contains("50/30/20") || q.contains("50 30 20") || q.contains("50-30-20") || q.contains("savings rate") || (q.contains("how") && q.contains("save")) || q.contains("budget rule") || q.contains("rule")) {
            val income = if (monthlyIncomeCents > 0) monthlyIncomeCents / 100 else 0L
            val needsCategories = setOf("Bills", "Groceries", "Health", "Education")
            val wantsCategories = setOf("Food", "Shopping", "Entertainment", "Others")

            val needsSpent = thisMonthDebits.filter { needsCategories.contains(it.categoryName) }.sumOf { it.amountCents } / 100
            val wantsSpent = thisMonthDebits.filter { wantsCategories.contains(it.categoryName) }.sumOf { it.amountCents } / 100
            val totalSpent = thisMonthTotalCents / 100

            val needsPercent = if (income > 0) (needsSpent * 100 / income) else if (totalSpent > 0) (needsSpent * 100 / totalSpent) else 0
            val wantsPercent = if (income > 0) (wantsSpent * 100 / income) else if (totalSpent > 0) (wantsSpent * 100 / totalSpent) else 0
            val savedAmount = if (income > totalSpent) income - totalSpent else 0L
            val savingsRate = if (income > 0) (savedAmount * 100 / income) else 0

            return AiAssistantResponse(
                answer = "50/30/20 Rule Analysis:\n\n" +
                        "• **Needs (Target 50%):** ₹$needsSpent ($needsPercent%)\n" +
                        "• **Wants (Target 30%):** ₹$wantsSpent ($wantsPercent%)\n" +
                        "• **Savings (Target 20%):** ₹$savedAmount ($savingsRate%)\n\n" +
                        if (wantsPercent > 30) "💡 Tip: Your discretionary 'Wants' are currently at $wantsPercent%. Reducing dining out and impulse shopping by 15% will help hit your 20% savings target."
                        else "✨ Great job! Your essential needs and discretionary spending are well-balanced within the 50/30/20 framework.",
                highlights = listOf(
                    "Needs: $needsPercent% (Target 50%)",
                    "Wants: $wantsPercent% (Target 30%)",
                    "Savings Rate: $savingsRate% (Target 20%)"
                )
            )
        }

        // 5. Where did most of my money go? / Top Spending
        if (q.contains("where") || q.contains("most") || q.contains("top") || q.contains("highest")) {
            val byCategory = thisMonthDebits.groupBy { it.categoryName }
                .mapValues { (_, items) -> items.sumOf { it.amountCents } }
                .toList()
                .sortedByDescending { it.second }

            if (byCategory.isEmpty()) {
                return AiAssistantResponse(
                    answer = "No expenses recorded this month yet. Start logging expenses by shaking your device or tapping the '+' button!",
                    highlights = listOf("No expenses logged yet")
                )
            }

            val topCat = byCategory.first()
            val topPercentage = if (thisMonthTotalCents > 0) (topCat.second * 100 / thisMonthTotalCents) else 0
            val topCategoryAmount = topCat.second / 100

            val topCategoryItems = thisMonthDebits.filter { it.categoryName == topCat.first }
                .sortedByDescending { it.amountCents }
                .take(3)
                .map { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}" }

            val singleHighest = thisMonthDebits.maxByOrNull { it.amountCents }

            return AiAssistantResponse(
                answer = "Most of your money went towards **${topCat.first}** this month: ₹$topCategoryAmount ($topPercentage% of your total spending).\n\n" +
                        "Top expenses in ${topCat.first}:\n" + topCategoryItems.joinToString("\n") +
                        if (singleHighest != null) "\n\n• Highest single transaction: ₹${singleHighest.amountCents / 100} (${singleHighest.customName ?: singleHighest.categoryName})" else "",
                highlights = listOf(
                    "Top Category: ${topCat.first} (₹$topCategoryAmount)",
                    "Share: $topPercentage% of monthly expenses",
                    "Highest Single: ₹${(singleHighest?.amountCents ?: 0) / 100}"
                )
            )
        }

        // 6. Subscriptions / Recurring Inquiries
        if (q.contains("subscription") || q.contains("recurring") || q.contains("annual burden")) {
            val recurringKeywords = listOf("netflix", "spotify", "prime", "hotstar", "youtube", "apple", "airtel", "jio", "wifi", "rent", "gym", "electricity")
            val subItems = allDebits.filter { item ->
                recurringKeywords.any { kw ->
                    item.customName?.lowercase()?.contains(kw) == true || item.bankRef?.lowercase()?.contains(kw) == true
                }
            }.distinctBy { (it.customName ?: it.categoryName).lowercase() }

            val subTotalMonthly = subItems.sumOf { it.amountCents } / 100
            val subTotalAnnual = subTotalMonthly * 12

            return AiAssistantResponse(
                answer = "Active recurring services detected:\n\n" +
                        (if (subItems.isNotEmpty()) subItems.joinToString("\n") { "• ${it.customName ?: it.categoryName}: ₹${it.amountCents / 100}/mo" } else "No active subscriptions detected.") +
                        "\n\n• Estimated Monthly Burden: ₹$subTotalMonthly/mo\n• Estimated Annual Burden: ₹$subTotalAnnual/yr",
                highlights = listOf(
                    "Subscriptions Detected: ${subItems.size}",
                    "Monthly Burden: ₹$subTotalMonthly",
                    "Annual Burden: ₹$subTotalAnnual"
                )
            )
        }

        // 7. Month-over-Month Comparison
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

        // 8. General Personal Finance Concepts (Emergency Fund, SIP, Credit Score, Compounding)
        if (q.contains("emergency fund") || q.contains("emergency")) {
            val target3Mo = (thisMonthTotalCents * 3) / 100
            val target6Mo = (thisMonthTotalCents * 6) / 100
            return AiAssistantResponse(
                answer = "An Emergency Fund is a cash safety net reserved strictly for unexpected medical, family, or employment events.\n\n" +
                        "• **3-Month Baseline:** ₹$target3Mo (based on your current monthly spend of ₹${thisMonthTotalCents / 100})\n" +
                        "• **6-Month Complete Shield:** ₹$target6Mo\n\n" +
                        "Store your emergency fund in a high-yield savings account or liquid mutual fund with instant 24/7 withdrawal access.",
                highlights = listOf(
                    "3-Month Target: ₹$target3Mo",
                    "6-Month Target: ₹$target6Mo",
                    "Recommended: Liquid / Savings Account"
                )
            )
        }

        if (q.contains("sip") || q.contains("invest") || q.contains("mutual fund") || q.contains("compound")) {
            return AiAssistantResponse(
                answer = "A Systematic Investment Plan (SIP) allows you to invest a fixed amount regularly into diversified index or mutual funds. By investing consistently, you benefit from rupee cost averaging and compounding returns over 5–10+ year horizons.",
                highlights = listOf(
                    "Concept: Rupee Cost Averaging",
                    "Horizon: 5+ Years recommended",
                    "Discipline: Automated monthly SIP"
                )
            )
        }

        // Default Overview
        return AiAssistantResponse(
            answer = "Here is your current financial snapshot:\n\n" +
                    "• **Total Spending This Month:** ₹${thisMonthTotalCents / 100} (${thisMonthDebits.size} transactions)\n" +
                    "• **Safe Daily Runway:** ₹${monthlySafeDailyCents / 100}/day\n" +
                    (if (monthlyIncomeCents > 0) "• **Monthly Income Configured:** ₹${monthlyIncomeCents / 100}\n" else "") +
                    "\nAsk me questions like:\n" +
                    "• 'How much did I spend on food this month?'\n" +
                    "• 'Can I afford ₹3,000 this week?'\n" +
                    "• 'How much did I spend today or this week?'\n" +
                    "• 'Analyze my 50/30/20 budget breakdown'\n" +
                    "• 'What are my subscriptions and annual burden?'\n" +
                    "• 'How much do I need for a 6-month emergency fund?'",
            highlights = listOf(
                "Month Total: ₹${thisMonthTotalCents / 100}",
                "Transactions: ${thisMonthDebits.size}",
                "Daily Safe Runway: ₹${monthlySafeDailyCents / 100}/day"
            )
        )
    }
}

