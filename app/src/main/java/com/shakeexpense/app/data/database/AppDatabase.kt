package com.shakeexpense.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shakeexpense.app.data.database.dao.CategoryDao
import com.shakeexpense.app.data.database.dao.ExpenseDao
import com.shakeexpense.app.data.database.dao.FamilyBudgetDao
import com.shakeexpense.app.data.database.dao.FamilyGroupDao
import com.shakeexpense.app.data.database.dao.FamilyMemberDao
import com.shakeexpense.app.data.database.dao.FinancialProfileDao
import com.shakeexpense.app.data.database.dao.RecurringPaymentDao
import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.data.database.entity.ExpenseEntity
import com.shakeexpense.app.data.database.entity.FamilyBudgetEntity
import com.shakeexpense.app.data.database.entity.FamilyGroupEntity
import com.shakeexpense.app.data.database.entity.FamilyMemberEntity
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        FamilyMemberEntity::class,
        FamilyGroupEntity::class,
        FamilyBudgetEntity::class,
        ExpenseEntity::class,
        FinancialProfileEntity::class,
        RecurringPaymentEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun familyBudgetDao(): FamilyBudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun financialProfileDao(): FinancialProfileDao
    abstract fun recurringPaymentDao(): RecurringPaymentDao

    companion object {
        const val DATABASE_NAME = "shake_expense.db"
        const val DEFAULT_USER_ID = "default_local_user"
        const val DEFAULT_FAMILY_ID = "default_family"

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE family_groups ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE family_members ADD COLUMN privacy_mode TEXT NOT NULL DEFAULT 'FULL_SHARED'")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS family_budgets (
                        id TEXT PRIMARY KEY NOT NULL,
                        family_id TEXT NOT NULL,
                        category_name TEXT NOT NULL,
                        limit_cents INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE family_members ADD COLUMN share_transactions INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE family_members ADD COLUMN share_monthly_total INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE family_members ADD COLUMN share_category_totals INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE family_members ADD COLUMN receive_family_alerts INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE family_members ADD COLUMN is_exit_requested INTEGER NOT NULL DEFAULT 0")
            }
        }

        val SEED_CATEGORIES = listOf(
            CategoryEntity(id = 1, name = "Food", colorHex = "#F59E0B", isDefault = true, displayOrder = 1),
            CategoryEntity(id = 2, name = "Transport", colorHex = "#3B82F6", isDefault = true, displayOrder = 2),
            CategoryEntity(id = 3, name = "Groceries", colorHex = "#10B981", isDefault = true, displayOrder = 3),
            CategoryEntity(id = 4, name = "Bills", colorHex = "#8B5CF6", isDefault = true, displayOrder = 4),
            CategoryEntity(id = 5, name = "Shopping", colorHex = "#F43F5E", isDefault = true, displayOrder = 5),
            CategoryEntity(id = 6, name = "Entertainment", colorHex = "#A855F7", isDefault = true, displayOrder = 6),
            CategoryEntity(id = 7, name = "Others", colorHex = "#64748B", isDefault = true, displayOrder = 99)
        )

        val DEFAULT_LOCAL_USER = FamilyMemberEntity(
            id = DEFAULT_USER_ID,
            familyId = DEFAULT_FAMILY_ID,
            name = "Me",
            role = "PARENT",
            deviceId = "local_device",
            createdAt = 1700000000000L
        )

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch {
                            val database = getInstance(context, scope)
                            populateInitialData(database)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        scope.launch {
                            val database = getInstance(context, scope)
                            val catCount = database.categoryDao().getCategoryCount()
                            if (catCount == 0) {
                                database.categoryDao().insertCategories(SEED_CATEGORIES)
                            }
                            if (database.financialProfileDao().getProfile(DEFAULT_USER_ID) == null) {
                                database.financialProfileDao().insertOrUpdateProfile(
                                    FinancialProfileEntity(
                                        userId = DEFAULT_USER_ID,
                                        monthlyIncomeCents = 0L,
                                        savingsTargetCents = 0L,
                                        billingCycleDay = 1,
                                        tier = "FREE",
                                        safetyScore = 0
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    database.categoryDao().insertCategories(AppDatabase.SEED_CATEGORIES)
    database.familyMemberDao().insertMember(AppDatabase.DEFAULT_LOCAL_USER)
    database.financialProfileDao().insertOrUpdateProfile(
        FinancialProfileEntity(
            userId = AppDatabase.DEFAULT_USER_ID,
            monthlyIncomeCents = 0L,
            savingsTargetCents = 0L,
            billingCycleDay = 1,
            tier = "FREE",
            safetyScore = 0
        )
    )
}
