package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.domain.model.CategorySubtotal
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.FamilyBudget
import com.shakeexpense.app.domain.model.FamilyMember
import com.shakeexpense.app.domain.model.MemberSpendingSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FamilyAiReport(
    val monthTitle: String,
    val totalSpendingCents: Long,
    val familyLimitCents: Long,
    val remainingLimitCents: Long,
    val limitUsagePercentage: Double,
    val totalIncomeCents: Long,
    val estimatedSavingsCents: Long,
    val topCategories: List<CategorySubtotal>,
    val budgetHealth: List<FamilyBudgetHealthItem>,
    val aiInsights: List<String>,
    val memberContributions: List<FamilyMemberContribution>,
    val hasEnoughData: Boolean
)

data class FamilyBudgetHealthItem(
    val categoryName: String,
    val spentCents: Long,
    val limitCents: Long,
    val usagePercentage: Double,
    val status: String // "UNDER_BUDGET" | "NEARING_LIMIT" | "EXCEEDED"
)

data class FamilyMemberContribution(
    val memberName: String,
    val role: String,
    val spentCents: Long,
    val percentageOfFamily: Double,
    val isConfidential: Boolean = false
)

class FamilyAiReportGeneratorUseCase {

    operator fun invoke(
        currentMonthExpenses: List<ExpenseRecordItem>,
        previousMonthExpenses: List<ExpenseRecordItem> = emptyList(),
        members: List<FamilyMember>,
        familyLimitCents: Long,
        sharedBudgets: List<FamilyBudget> = emptyList(),
        familyMonthlyIncomeCents: Long = 0L,
        currentViewerUserId: String = ""
    ): FamilyAiReport {
        val currentMonthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        val debits = currentMonthExpenses.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }
        val prevDebits = previousMonthExpenses.filter { it.transactionType.equals("DEBIT", ignoreCase = true) }

        if (debits.isEmpty()) {
            return FamilyAiReport(
                monthTitle = currentMonthTitle,
                totalSpendingCents = 0L,
                familyLimitCents = familyLimitCents,
                remainingLimitCents = familyLimitCents,
                limitUsagePercentage = 0.0,
                totalIncomeCents = familyMonthlyIncomeCents,
                estimatedSavingsCents = familyMonthlyIncomeCents,
                topCategories = emptyList(),
                budgetHealth = emptyList(),
                aiInsights = listOf("Not enough family financial data to generate a report."),
                memberContributions = emptyList(),
                hasEnoughData = false
            )
        }

        val totalSpentCents = debits.sumOf { it.amountCents }
        val prevTotalSpentCents = prevDebits.sumOf { it.amountCents }
        val remainingCents = (familyLimitCents - totalSpentCents).coerceAtLeast(0L)
        val limitUsage = if (familyLimitCents > 0L) (totalSpentCents.toDouble() / familyLimitCents.toDouble() * 100.0) else 0.0
        val savingsCents = (familyMonthlyIncomeCents - totalSpentCents).coerceAtLeast(0L)

        // 1. Top Categories
        val byCat = debits.groupBy { it.categoryName }
            .map { (catName, items) ->
                CategorySubtotal(
                    categoryId = items.firstOrNull()?.categoryId ?: 1L,
                    categoryName = catName,
                    colorHex = items.firstOrNull()?.categoryColor ?: "#3B82F6",
                    totalCents = items.sumOf { it.amountCents },
                    count = items.size
                )
            }
            .sortedByDescending { it.totalCents }

        // 2. Budget Health for Shared Budgets
        val budgetHealthList = sharedBudgets.map { b ->
            val spent = debits.filter { it.categoryName.equals(b.categoryName, ignoreCase = true) }.sumOf { it.amountCents }
            val pct = if (b.limitCents > 0L) (spent.toDouble() / b.limitCents.toDouble() * 100.0) else 0.0
            val status = when {
                pct >= 100.0 -> "EXCEEDED"
                pct >= 80.0 -> "NEARING_LIMIT"
                else -> "UNDER_BUDGET"
            }
            FamilyBudgetHealthItem(
                categoryName = b.categoryName,
                spentCents = spent,
                limitCents = b.limitCents,
                usagePercentage = pct,
                status = status
            )
        }

        // 3. Member Contributions respecting privacy
        val memberContributions = members.map { member ->
            val memberDebits = debits.filter { it.userId == member.id }
            val memberTotal = memberDebits.sumOf { it.amountCents }
            val pct = if (totalSpentCents > 0L) (memberTotal.toDouble() / totalSpentCents.toDouble() * 100.0) else 0.0

            val isConfidential = member.id != currentViewerUserId &&
                (!member.privacySettings.shareMonthlyTotal || member.privacyMode == com.shakeexpense.app.domain.model.PrivacyMode.PRIVATE)

            FamilyMemberContribution(
                memberName = member.name,
                role = member.role.name,
                spentCents = if (isConfidential) 0L else memberTotal,
                percentageOfFamily = if (isConfidential) 0.0 else pct,
                isConfidential = isConfidential
            )
        }

        // 4. Mathematical AI Insights from Real Data (strictly zero fake numbers)
        val insights = mutableListOf<String>()

        // Trend insight
        if (prevTotalSpentCents > 0L) {
            val diffCents = totalSpentCents - prevTotalSpentCents
            val growthPct = Math.abs((diffCents.toDouble() / prevTotalSpentCents.toDouble() * 100.0).toInt())
            if (diffCents > 0) {
                insights.add("Family spending increased $growthPct% compared with last month (+₹${diffCents / 100}).")
            } else if (diffCents < 0) {
                insights.add("Family spending decreased $growthPct% compared with last month (-₹${Math.abs(diffCents) / 100}).")
            } else {
                insights.add("Family spending is identical to last month.")
            }
        } else {
            insights.add("First month of recorded family data. Tracking baseline spending habits.")
        }

        // Largest category insight
        val topCat = byCat.firstOrNull()
        if (topCat != null) {
            val topPct = (topCat.totalCents.toDouble() / totalSpentCents.toDouble() * 100.0).toInt()
            insights.add("${topCat.categoryName} is the largest category this month (₹${topCat.totalCents / 100}, $topPct% of family spending).")
        }

        // Limit pacing insight
        if (familyLimitCents > 0L) {
            insights.add("The family is currently using ${String.format(Locale.US, "%.1f", limitUsage)}% of its monthly limit (₹${remainingCents / 100} remaining).")
        }

        // Category budget alert insight
        val exceededBudget = budgetHealthList.firstOrNull { it.status == "EXCEEDED" }
        if (exceededBudget != null) {
            insights.add("Family ${exceededBudget.categoryName} budget exceeded limit by ₹${(exceededBudget.spentCents - exceededBudget.limitCents) / 100}.")
        }

        return FamilyAiReport(
            monthTitle = currentMonthTitle,
            totalSpendingCents = totalSpentCents,
            familyLimitCents = familyLimitCents,
            remainingLimitCents = remainingCents,
            limitUsagePercentage = limitUsage,
            totalIncomeCents = familyMonthlyIncomeCents,
            estimatedSavingsCents = savingsCents,
            topCategories = byCat.take(5),
            budgetHealth = budgetHealthList,
            aiInsights = insights,
            memberContributions = memberContributions,
            hasEnoughData = true
        )
    }
}
