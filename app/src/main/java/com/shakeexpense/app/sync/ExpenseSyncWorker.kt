package com.shakeexpense.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shakeexpense.app.ShakeExpenseApp
import com.shakeexpense.app.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = applicationContext as? ShakeExpenseApp ?: return@withContext Result.failure()
            val db = app.database
            val expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
            val syncEngine = SyncEngine(
                expenseRepository = expenseRepo,
                syncApiClient = BackendSyncApiClient(),
                context = applicationContext
            )

            syncEngine.performOnlineTwoWaySync()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
