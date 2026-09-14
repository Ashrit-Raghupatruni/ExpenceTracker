package com.shakeexpense.app

import android.app.Application
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.sensor.ShakeSensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ShakeExpenseApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val container: com.shakeexpense.app.di.AppContainer by lazy {
        com.shakeexpense.app.di.AppContainer(this, applicationScope)
    }

    val database: AppDatabase get() = container.database
    val spendingAlertNotificationManager: com.shakeexpense.app.notification.SpendingAlertNotificationManager get() = container.spendingAlertNotificationManager
    val entitlementManager: com.shakeexpense.app.domain.usecase.EntitlementManager get() = container.entitlementManager
    val transactionPipeline: com.shakeexpense.app.domain.pipeline.TransactionEventPipeline get() = container.transactionPipeline
    val expenseRepository: com.shakeexpense.app.data.repository.ExpenseRepository get() = container.expenseRepository

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
