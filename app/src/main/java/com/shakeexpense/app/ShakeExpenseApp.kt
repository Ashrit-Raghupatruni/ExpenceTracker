package com.shakeexpense.app

import android.app.Application
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.sensor.ShakeSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ShakeExpenseApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this, applicationScope)
    }

    val spendingAlertNotificationManager: com.shakeexpense.app.notification.SpendingAlertNotificationManager by lazy {
        com.shakeexpense.app.notification.SpendingAlertNotificationManager(this)
    }

    val entitlementManager: com.shakeexpense.app.domain.usecase.EntitlementManager by lazy {
        com.shakeexpense.app.domain.usecase.EntitlementManager()
    }

    val transactionPipeline: com.shakeexpense.app.domain.pipeline.TransactionEventPipeline by lazy {
        com.shakeexpense.app.domain.pipeline.TransactionEventPipeline(
            expenseDao = database.expenseDao(),
            financialProfileDao = database.financialProfileDao(),
            familyGroupDao = database.familyGroupDao(),
            familyMemberDao = database.familyMemberDao(),
            familyBudgetDao = database.familyBudgetDao(),
            recurringPaymentDao = database.recurringPaymentDao(),
            notificationManager = spendingAlertNotificationManager,
            categoryDao = database.categoryDao(),
            pipelineScope = applicationScope
        )
    }

    val expenseRepository: com.shakeexpense.app.data.repository.ExpenseRepository by lazy {
        com.shakeexpense.app.data.repository.ExpenseRepositoryImpl(database.expenseDao(), transactionPipeline)
    }

    override fun onCreate() {
        super.onCreate()
        // Start background shake sensor service
        try {
            ShakeSensorService.startService(this)
        } catch (e: Exception) {
            // Ignore if background start restricted prior to permissions
        }
    }
}
