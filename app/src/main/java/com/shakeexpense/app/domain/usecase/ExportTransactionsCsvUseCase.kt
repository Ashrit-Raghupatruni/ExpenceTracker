package com.shakeexpense.app.domain.usecase

import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.domain.model.FeatureCapability
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportTransactionsCsvUseCase(
    private val entitlementManager: EntitlementManager
) {

    fun generateCsv(
        expenses: List<ExpenseEntity>,
        categoryNameMap: Map<Long, String> = emptyMap()
    ): String? {
        if (!entitlementManager.hasFeature(FeatureCapability.CSV_EXPORT)) {
            // Strictly restricted on Free tier
            return null
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,UUID,Date,Type,Source,Category,Amount (INR),Custom Name,Bank Ref\n")

        for (expense in expenses) {
            val dateStr = dateFormat.format(Date(expense.timestamp))
            val category = categoryNameMap[expense.categoryId] ?: "General"
            val amount = expense.amountCents / 100.0
            val name = expense.customName ?: ""
            val bankRef = expense.bankRef ?: ""

            sb.append("${expense.id},")
            sb.append("\"${escapeCsv(expense.uuid)}\",")
            sb.append("\"${escapeCsv(dateStr)}\",")
            sb.append("\"${escapeCsv(expense.type)}\",")
            sb.append("\"${escapeCsv(expense.source)}\",")
            sb.append("\"${escapeCsv(category)}\",")
            sb.append(String.format(Locale.US, "%.2f,", amount))
            sb.append("\"${escapeCsv(name)}\",")
            sb.append("\"${escapeCsv(bankRef)}\"\n")
        }

        return sb.toString()
    }

    fun generateCsvFromRecords(
        records: List<com.shakeexpense.app.domain.model.ExpenseRecordItem>,
        plan: com.shakeexpense.app.domain.model.SubscriptionPlan = com.shakeexpense.app.domain.model.SubscriptionPlan.FREE
    ): String? {
        if (!entitlementManager.canAccess(plan, FeatureCapability.CSV_EXPORT)) {
            return null
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("UUID,Date,Type,Source,Category,Amount (INR),Name/Merchant,Bank Ref\n")

        for (item in records) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            val amount = item.amountCents / 100.0
            val name = item.customName ?: ""
            val bankRef = item.bankRef ?: ""

            sb.append("\"${escapeCsv(item.expenseUuid)}\",")
            sb.append("\"${escapeCsv(dateStr)}\",")
            sb.append("\"${escapeCsv(item.transactionType)}\",")
            sb.append("\"${escapeCsv(item.transactionSource)}\",")
            sb.append("\"${escapeCsv(item.categoryName)}\",")
            sb.append(String.format(Locale.US, "%.2f,", amount))
            sb.append("\"${escapeCsv(name)}\",")
            sb.append("\"${escapeCsv(bankRef)}\"\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
