package com.shakeexpense.app.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.shakeexpense.app.R
import com.shakeexpense.app.ShakeExpenseApp
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.ProcessBankNotificationResult
import com.shakeexpense.app.domain.usecase.ProcessBankNotificationUseCase
import com.shakeexpense.app.ui.tracker.MainActivity
import kotlinx.coroutines.launch

class BankNotificationListenerService : NotificationListenerService() {

    private val parser = BankNotificationParser()
    private var processUseCase: ProcessBankNotificationUseCase? = null

    companion object {
        const val CHANNEL_ID = "bank_detection_channel"
        const val CHANNEL_NAME = "Bank & UPI Transaction Alerts"
        const val NOTIFICATION_ID = 4001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val app = application as? ShakeExpenseApp
        if (app != null) {
            val db = app.database
            val expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
            processUseCase = ProcessBankNotificationUseCase(expenseRepo)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: ""
        // Ignore notifications posted by our own app
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val rawText = if (bigText.isNotBlank()) bigText else text

        // Parse notification
        val parsed = parser.parse(
            text = rawText,
            title = title,
            packageName = packageName,
            timestamp = sbn.postTime
        ) ?: return

        // Strict Reliability Check: reject non-financial alerts, OTPs, promotions
        if (!parsed.isReliableFinancialTransaction) return

        val app = application as? ShakeExpenseApp ?: return
        val useCase = processUseCase ?: run {
            val db = app.database
            val expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
            ProcessBankNotificationUseCase(expenseRepo).also { processUseCase = it }
        }

        app.applicationScope.launch {
            val authRepo = com.shakeexpense.app.data.repository.AuthRepository(applicationContext)
            val currentUserId = authRepo.getCurrentProfile().userId

            // Check if duplicate transaction within 180s
            val isDuplicate = useCase.checkIsDuplicate(parsed, currentUserId)
            if (!isDuplicate) {
                showTransactionConfirmationNotification(parsed)
            }
        }
    }

    private fun showTransactionConfirmationNotification(transaction: ParsedBankTransaction) {
        val amountInRupees = transaction.amountCents / 100.0
        val formattedAmount = if (amountInRupees % 1.0 == 0.0) {
            "₹%.0f".format(amountInRupees)
        } else {
            "₹%.2f".format(amountInRupees)
        }

        val typeLabel = if (transaction.type == TransactionType.CREDIT) "Received" else "Paid"
        val merchantLabel = if (!transaction.merchantOrPayee.isNullOrBlank()) {
            if (transaction.type == TransactionType.CREDIT) "from ${transaction.merchantOrPayee}" else "at ${transaction.merchantOrPayee}"
        } else ""

        val sourcePrefix = when {
            !transaction.bankName.isNullOrBlank() && !transaction.accountLastDigits.isNullOrBlank() ->
                "${transaction.bankName} (..${transaction.accountLastDigits}): "
            !transaction.bankName.isNullOrBlank() ->
                "${transaction.bankName}: "
            !transaction.accountLastDigits.isNullOrBlank() ->
                "A/c ..${transaction.accountLastDigits}: "
            else -> ""
        }

        val title = "$sourcePrefix$formattedAmount $typeLabel $merchantLabel".trim()
        val categoryName = when (transaction.suggestedCategoryId) {
            1L -> "Food"
            2L -> "Transport"
            3L -> "Groceries"
            4L -> "Bills"
            5L -> "Shopping"
            6L -> "Entertainment"
            else -> "Others"
        }
        val content = "Suggested: $categoryName • Tap to confirm or choose category"

        val notificationId = (NOTIFICATION_ID + (transaction.timestamp % 1000).toInt())

        // 1. Direct Confirm & Save Action Intent
        val confirmIntent = Intent(this, BankTransactionConfirmReceiver::class.java).apply {
            action = BankTransactionConfirmReceiver.ACTION_CONFIRM_TRANSACTION
            putExtra(BankTransactionConfirmReceiver.EXTRA_AMOUNT_CENTS, transaction.amountCents)
            putExtra(BankTransactionConfirmReceiver.EXTRA_TRANSACTION_TYPE, transaction.type.name)
            putExtra(BankTransactionConfirmReceiver.EXTRA_CATEGORY_ID, transaction.suggestedCategoryId)
            putExtra(BankTransactionConfirmReceiver.EXTRA_CATEGORY_NAME, categoryName)
            putExtra(BankTransactionConfirmReceiver.EXTRA_MERCHANT, transaction.merchantOrPayee)
            putExtra(BankTransactionConfirmReceiver.EXTRA_TIMESTAMP, transaction.timestamp)
            putExtra(BankTransactionConfirmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Open Quick Entry Overlay for Review / Category Selection
        val editIntent = Intent(this, com.shakeexpense.app.ui.overlay.QuickEntryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("prefill_amount_cents", transaction.amountCents)
            putExtra("prefill_type", transaction.type.name)
            putExtra("prefill_category_id", transaction.suggestedCategoryId)
            putExtra("prefill_custom_name", transaction.merchantOrPayee)
        }
        val editPendingIntent = PendingIntent.getActivity(
            this,
            notificationId + 1,
            editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Dismiss Action Intent
        val dismissIntent = Intent(this, BankTransactionConfirmReceiver::class.java).apply {
            action = BankTransactionConfirmReceiver.ACTION_DISMISS_TRANSACTION
            putExtra(BankTransactionConfirmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(editPendingIntent)
            .addAction(R.mipmap.ic_launcher_round, "Confirm ($categoryName)", confirmPendingIntent)
            .addAction(R.mipmap.ic_launcher_round, "Choose Category", editPendingIntent)
            .addAction(R.mipmap.ic_launcher_round, "Dismiss", dismissPendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a bank or UPI transaction is automatically captured"
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
