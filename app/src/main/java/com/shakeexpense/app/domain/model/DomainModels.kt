package com.shakeexpense.app.domain.model

enum class TransactionType {
    DEBIT,
    CREDIT
}

enum class TransactionSource {
    MANUAL_SHAKE,
    BANK_NOTIF,
    SYNC
}

enum class SyncStatus {
    PENDING,
    SYNCED
}

enum class FamilyRole {
    PARENT,
    CHILD
}

enum class PrivacyMode {
    PRIVATE,
    SHARED_SUMMARY,
    FULL_SHARED
}

data class FamilyPrivacySettings(
    val shareTransactions: Boolean = true,
    val shareMonthlyTotal: Boolean = true,
    val shareCategoryTotals: Boolean = true,
    val receiveFamilyAlerts: Boolean = true
)

data class FamilyMember(
    val id: String,
    val familyId: String,
    val name: String,
    val role: FamilyRole,
    val deviceId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val privacyMode: PrivacyMode = PrivacyMode.FULL_SHARED,
    val privacySettings: FamilyPrivacySettings = FamilyPrivacySettings(),
    val isExitRequested: Boolean = false
)

data class FamilyBudget(
    val id: String,
    val familyId: String,
    val categoryName: String,
    val limitCents: Long,
    val spentCents: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val usagePercentage: Double
        get() = if (limitCents > 0L) (spentCents.toDouble() / limitCents.toDouble() * 100.0) else 0.0

    val remainingCents: Long
        get() = (limitCents - spentCents).coerceAtLeast(0L)

    val isExceeded: Boolean
        get() = spentCents > limitCents
}

data class Expense(
    val id: Long = 0,
    val uuid: String,
    val userId: String,
    val userName: String? = null,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val amountCents: Long,
    val type: TransactionType = TransactionType.DEBIT,
    val source: TransactionSource = TransactionSource.MANUAL_SHAKE,
    val customName: String? = null,
    val bankRef: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    val displayCategory: String
        get() = if (categoryName.equals("Others", ignoreCase = true) && !customName.isNullOrBlank()) {
            customName
        } else {
            categoryName
        }

    val formattedAmount: Double
        get() = amountCents / 100.0
}

data class MemberSpendingSummary(
    val userId: String,
    val userName: String,
    val role: FamilyRole,
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val transactionCount: Int,
    val categoryBreakdown: List<CategorySubtotal> = emptyList()
)

data class CategorySubtotal(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val totalCents: Long,
    val count: Int
)

data class ExpenseRecordItem(
    val expenseId: Long,
    val expenseUuid: String,
    val userId: String,
    val userName: String? = null,
    val amountCents: Long,
    val transactionType: String,
    val transactionSource: String,
    val timestamp: Long,
    val customName: String? = null,
    val bankRef: String? = null,
    val categoryId: Long = 1L,
    val categoryName: String,
    val categoryColor: String = "#3B82F6",
    val syncStatus: String = "SYNCED"
) {
    val displayCategory: String
        get() = if (categoryName.equals("Others", ignoreCase = true) && !customName.isNullOrBlank()) {
            customName
        } else {
            categoryName
        }

    val formattedAmount: Double
        get() = amountCents / 100.0
}
