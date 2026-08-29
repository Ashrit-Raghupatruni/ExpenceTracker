package com.shakeexpense.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.shakeexpense.app.ShakeExpenseApp
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.ProcessBankNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BankTransactionConfirmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONFIRM_TRANSACTION = "com.shakeexpense.app.ACTION_CONFIRM_TRANSACTION"
        const val ACTION_DISMISS_TRANSACTION = "com.shakeexpense.app.ACTION_DISMISS_TRANSACTION"
        const val EXTRA_AMOUNT_CENTS = "extra_amount_cents"
        const val EXTRA_TRANSACTION_TYPE = "extra_transaction_type"
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"
        const val EXTRA_MERCHANT = "extra_merchant"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, BankNotificationListenerService.NOTIFICATION_ID)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)

        if (intent.action == ACTION_DISMISS_TRANSACTION) {
            return
        }

        if (intent.action == ACTION_CONFIRM_TRANSACTION) {
            val amountCents = intent.getLongExtra(EXTRA_AMOUNT_CENTS, 0L)
            if (amountCents <= 0) return

            val typeStr = intent.getStringExtra(EXTRA_TRANSACTION_TYPE) ?: "DEBIT"
            val type = if (typeStr.equals("CREDIT", ignoreCase = true)) TransactionType.CREDIT else TransactionType.DEBIT
            val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, 1L)
            val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME) ?: "Expenses"
            val merchant = intent.getStringExtra(EXTRA_MERCHANT)
            val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())

            val app = context.applicationContext as? ShakeExpenseApp ?: return
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = app.database
                    val expenseRepo = app.expenseRepository
                    val authRepo = AuthRepository(context)
                    val currentUserId = authRepo.getCurrentProfile().userId
                    val processUseCase = ProcessBankNotificationUseCase(expenseRepo)

                    val parsed = ParsedBankTransaction(
                        amountCents = amountCents,
                        type = type,
                        merchantOrPayee = merchant,
                        suggestedCategoryId = categoryId,
                        timestamp = timestamp,
                        rawPackageName = ""
                    )

                    processUseCase(
                        transaction = parsed,
                        userId = currentUserId,
                        categoryId = categoryId
                    )

                    withContext(Dispatchers.Main) {
                        val amountFormatted = "₹${amountCents / 100}"
                        Toast.makeText(
                            context,
                            "Confirmed & Added $amountFormatted to $categoryName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    // Ignore errors during broadcast
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
