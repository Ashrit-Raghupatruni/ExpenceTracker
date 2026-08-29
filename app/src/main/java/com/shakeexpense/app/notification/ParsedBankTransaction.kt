package com.shakeexpense.app.notification

import com.shakeexpense.app.domain.model.TransactionType

data class ParsedBankTransaction(
    val amountCents: Long,
    val type: TransactionType,
    val merchantOrPayee: String?,
    val suggestedCategoryId: Long,
    val timestamp: Long,
    val rawPackageName: String
)
