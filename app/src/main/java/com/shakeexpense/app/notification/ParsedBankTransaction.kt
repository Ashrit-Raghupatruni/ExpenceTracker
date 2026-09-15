package com.shakeexpense.app.notification

import com.shakeexpense.app.domain.model.TransactionType

data class ParsedBankTransaction(
    val amountCents: Long,
    val type: TransactionType,
    val merchantOrPayee: String?,
    val suggestedCategoryId: Long,
    val timestamp: Long,
    val rawPackageName: String,
    val bankName: String? = null,
    val accountLastDigits: String? = null,
    val upiRefNumber: String? = null,
    val upiVpa: String? = null,
    val isReliableFinancialTransaction: Boolean = true
)
