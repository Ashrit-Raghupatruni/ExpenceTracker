package com.shakeexpense.app.domain.pipeline

import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.database.dao.FamilyBudgetDao
import com.shakeexpense.app.data.database.dao.FamilyGroupDao
import com.shakeexpense.app.data.database.dao.FamilyMemberDao
import com.shakeexpense.app.data.database.dao.FinancialProfileDao
import com.shakeexpense.app.data.database.dao.RecurringPaymentDao
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import com.shakeexpense.app.domain.model.ExpenseRecordItem
import com.shakeexpense.app.domain.model.SubscriptionPlan
import com.shakeexpense.app.domain.usecase.FinancialSafetyScoreEngine
import com.shakeexpense.app.domain.usecase.RecurringPaymentDetector
import com.shakeexpense.app.domain.usecase.UnusualSpendingDetector
import com.shakeexpense.app.notification.SpendingAlertNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class TransactionEvent {
    data class Created(val expense: ExpenseEntity) : TransactionEvent()
    data class Updated(val expense: ExpenseEntity) : TransactionEvent()
    data class Deleted(val expenseUuid: String, val userId: String) : TransactionEvent()
}

class TransactionEventPipeline(
    private val expenseDao: ExpenseDao,
    private val financialProfileDao: FinancialProfileDao,
    private val familyGroupDao: FamilyGroupDao?,
    private val familyMemberDao: FamilyMemberDao?,
    private val familyBudgetDao: FamilyBudgetDao?,
    private val recurringPaymentDao: RecurringPaymentDao?,
    private val notificationManager: SpendingAlertNotificationManager?,
    private val categoryDao: com.shakeexpense.app.data.database.dao.CategoryDao? = null,
    private val recurringDetector: RecurringPaymentDetector = RecurringPaymentDetector(),
    private val unusualDetector: UnusualSpendingDetector = UnusualSpendingDetector(),
    private val safetyScoreEngine: FinancialSafetyScoreEngine = FinancialSafetyScoreEngine(),
    private val pipelineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    fun onTransactionEvent(event: TransactionEvent) {
        pipelineScope.launch {
            processEvent(event)
        }
    }

    suspend fun processEvent(event: TransactionEvent) {
        val targetUserId = when (event) {
            is TransactionEvent.Created -> event.expense.userId
            is TransactionEvent.Updated -> event.expense.userId
            is TransactionEvent.Deleted -> event.userId
        }

        // 1. Calculate current month boundaries
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val endOfMonth = cal.timeInMillis

        // 2. Fetch current month user expenses
        val monthExpenses = expenseDao.getExpensesInTimeRange(targetUserId, startOfMonth, endOfMonth)
        val currentMonthDebits = monthExpenses.filter { it.type.equals("DEBIT", ignoreCase = true) }
        val currentMonthDebitCents = currentMonthDebits.sumOf { it.amountCents }

        // 3. Monitor Personal Monthly Spending Limit
        val profile = financialProfileDao.getProfile(targetUserId)
            ?: financialProfileDao.getProfile("default_local_user")
        val personalLimitCents = profile?.monthlySpendingLimitCents ?: 0L

        if (personalLimitCents > 0L && notificationManager != null) {
            notificationManager.checkAndNotifyPersonalLimit(
                userId = targetUserId,
                currentSpentCents = currentMonthDebitCents,
                limitCents = personalLimitCents
            )
        }

        // 4. Monitor Family Monthly Spending Limit & Shared Family Budgets
        if (familyGroupDao != null && familyMemberDao != null && notificationManager != null) {
            val activeFamily = familyGroupDao.getActiveFamilyGroup()
            if (activeFamily != null) {
                // Fetch all family members who have receive_family_alerts enabled
                val members = familyMemberDao.getAllMembersSync()
                val familyMemberIds = members.map { it.id }.toSet()

                // Calculate collective family spending in current month
                var collectiveFamilySpendCents = 0L
                val categorySpendingMap = mutableMapOf<String, Long>()

                for (mId in familyMemberIds) {
                    val mExpenses = expenseDao.getExpensesInTimeRange(mId, startOfMonth, endOfMonth)
                    for (e in mExpenses.filter { it.type.equals("DEBIT", ignoreCase = true) }) {
                        collectiveFamilySpendCents += e.amountCents
                        val catName = e.customName ?: "General"
                        categorySpendingMap[catName] = (categorySpendingMap[catName] ?: 0L) + e.amountCents
                    }
                }

                // Check family overall limit
                if (activeFamily.monthlySpendingLimitCents > 0L) {
                    notificationManager.checkAndNotifyFamilyLimit(
                        familyId = activeFamily.familyId,
                        familyName = activeFamily.familyName,
                        currentSpentCents = collectiveFamilySpendCents,
                        limitCents = activeFamily.monthlySpendingLimitCents
                    )
                }

                // Check shared category budgets
                if (familyBudgetDao != null) {
                    val budgets = familyBudgetDao.getFamilyBudgetsSync(activeFamily.familyId)
                    for (b in budgets) {
                        val spent = categorySpendingMap[b.categoryName] ?: 0L
                        if (b.limitCents > 0L) {
                            notificationManager.checkAndNotifyFamilyCategoryBudget(
                                familyId = activeFamily.familyId,
                                familyName = activeFamily.familyName,
                                categoryName = b.categoryName,
                                spentCents = spent,
                                limitCents = b.limitCents
                            )
                        }
                    }
                }
            }
        }

        // 5. Automatic Recurring/Subscription Detection & Room Seeding
        val categoryMap = try {
            categoryDao?.getAllCategoriesSync()?.associateBy { it.id } ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
        val domainRecordItems = monthExpenses.map { entity ->
            val cat = categoryMap[entity.categoryId]
            ExpenseRecordItem(
                expenseId = entity.id,
                expenseUuid = entity.uuid,
                userId = entity.userId,
                userName = null,
                amountCents = entity.amountCents,
                transactionType = entity.type,
                transactionSource = entity.source,
                timestamp = entity.timestamp,
                customName = entity.customName,
                bankRef = entity.bankRef,
                categoryId = entity.categoryId,
                categoryName = cat?.name ?: entity.customName ?: "Expense",
                categoryColor = cat?.colorHex ?: "#3B82F6",
                syncStatus = entity.syncStatus
            )
        }

        if (recurringPaymentDao != null) {
            val detected = recurringDetector.detectRecurring(domainRecordItems)
            for (sub in detected) {
                // Upsert detected recurring subscription into Room if not already manually customized
                val existing = recurringPaymentDao.getRecurringPaymentById(sub.name.lowercase().hashCode().toLong())
                if (existing == null) {
                    val entity = RecurringPaymentEntity(
                        id = sub.name.lowercase().hashCode().toLong(),
                        userId = targetUserId,
                        name = sub.name,
                        amountCents = sub.amountCents,
                        cadence = sub.frequency,
                        lastChargedTimestamp = System.currentTimeMillis(),
                        isActive = true,
                        isAutoDetected = true
                    )
                    recurringPaymentDao.insertRecurringPayment(entity)
                }
            }
        }

        // 6. Recalculate Financial Safety Score & Update Profile
        if (profile != null) {
            val safetyResult = safetyScoreEngine.calculateScore(
                monthlyIncomeCents = profile.monthlyIncomeCents,
                savingsTargetCents = profile.savingsTargetCents,
                currentMonthExpensesCents = currentMonthDebitCents,
                records = domainRecordItems
            )
            if (safetyResult != null && safetyResult.score != profile.safetyScore) {
                financialProfileDao.updateSafetyScore(targetUserId, safetyResult.score)
            }
        }
    }
}
