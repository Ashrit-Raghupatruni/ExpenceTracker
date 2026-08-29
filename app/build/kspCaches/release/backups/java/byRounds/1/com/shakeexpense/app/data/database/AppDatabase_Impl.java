package com.shakeexpense.app.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.shakeexpense.app.data.database.dao.CategoryDao;
import com.shakeexpense.app.data.database.dao.CategoryDao_Impl;
import com.shakeexpense.app.data.database.dao.ExpenseDao;
import com.shakeexpense.app.data.database.dao.ExpenseDao_Impl;
import com.shakeexpense.app.data.database.dao.FamilyBudgetDao;
import com.shakeexpense.app.data.database.dao.FamilyBudgetDao_Impl;
import com.shakeexpense.app.data.database.dao.FamilyGroupDao;
import com.shakeexpense.app.data.database.dao.FamilyGroupDao_Impl;
import com.shakeexpense.app.data.database.dao.FamilyMemberDao;
import com.shakeexpense.app.data.database.dao.FamilyMemberDao_Impl;
import com.shakeexpense.app.data.database.dao.FinancialProfileDao;
import com.shakeexpense.app.data.database.dao.FinancialProfileDao_Impl;
import com.shakeexpense.app.data.database.dao.RecurringPaymentDao;
import com.shakeexpense.app.data.database.dao.RecurringPaymentDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CategoryDao _categoryDao;

  private volatile FamilyMemberDao _familyMemberDao;

  private volatile FamilyGroupDao _familyGroupDao;

  private volatile FamilyBudgetDao _familyBudgetDao;

  private volatile ExpenseDao _expenseDao;

  private volatile FinancialProfileDao _financialProfileDao;

  private volatile RecurringPaymentDao _recurringPaymentDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color_hex` TEXT NOT NULL, `is_default` INTEGER NOT NULL DEFAULT 1, `display_order` INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `family_members` (`id` TEXT NOT NULL, `family_id` TEXT NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `device_id` TEXT NOT NULL, `privacy_mode` TEXT NOT NULL DEFAULT 'FULL_SHARED', `share_transactions` INTEGER NOT NULL DEFAULT 1, `share_monthly_total` INTEGER NOT NULL DEFAULT 1, `share_category_totals` INTEGER NOT NULL DEFAULT 1, `receive_family_alerts` INTEGER NOT NULL DEFAULT 1, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `family_groups` (`familyId` TEXT NOT NULL, `family_name` TEXT NOT NULL, `invite_code` TEXT NOT NULL, `owner_uid` TEXT NOT NULL, `monthly_spending_limit_cents` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, PRIMARY KEY(`familyId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `family_budgets` (`id` TEXT NOT NULL, `family_id` TEXT NOT NULL, `category_name` TEXT NOT NULL, `limit_cents` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` TEXT NOT NULL, `user_id` TEXT NOT NULL, `category_id` INTEGER NOT NULL, `amount_cents` INTEGER NOT NULL, `type` TEXT NOT NULL DEFAULT 'DEBIT', `source` TEXT NOT NULL DEFAULT 'MANUAL_SHAKE', `custom_name` TEXT, `bank_ref` TEXT, `timestamp` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `sync_status` TEXT NOT NULL DEFAULT 'PENDING')");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_expenses_uuid` ON `expenses` (`uuid`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_timestamp` ON `expenses` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_user_id` ON `expenses` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category_id` ON `expenses` (`category_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_sync_status` ON `expenses` (`sync_status`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `financial_profile` (`user_id` TEXT NOT NULL, `monthly_income_cents` INTEGER NOT NULL DEFAULT 0, `savings_target_cents` INTEGER NOT NULL DEFAULT 0, `billing_cycle_day` INTEGER NOT NULL DEFAULT 1, `tier` TEXT NOT NULL DEFAULT 'FREE', `safety_score` INTEGER NOT NULL DEFAULT 80, `monthly_spending_limit_cents` INTEGER NOT NULL DEFAULT 0, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`user_id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_id` TEXT NOT NULL, `name` TEXT NOT NULL, `amount_cents` INTEGER NOT NULL, `cadence` TEXT NOT NULL DEFAULT 'MONTHLY', `category_id` INTEGER NOT NULL DEFAULT 4, `last_charged_timestamp` INTEGER NOT NULL, `next_due_timestamp` INTEGER NOT NULL, `is_auto_detected` INTEGER NOT NULL DEFAULT 1, `is_active` INTEGER NOT NULL DEFAULT 1, `merchant_key` TEXT, `created_at` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_payments_user_id` ON `recurring_payments` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_payments_name` ON `recurring_payments` (`name`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f3472a6c095c35f688e661f61fdf0425')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `family_members`");
        db.execSQL("DROP TABLE IF EXISTS `family_groups`");
        db.execSQL("DROP TABLE IF EXISTS `family_budgets`");
        db.execSQL("DROP TABLE IF EXISTS `expenses`");
        db.execSQL("DROP TABLE IF EXISTS `financial_profile`");
        db.execSQL("DROP TABLE IF EXISTS `recurring_payments`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(5);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("color_hex", new TableInfo.Column("color_hex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("is_default", new TableInfo.Column("is_default", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("display_order", new TableInfo.Column("display_order", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(1);
        _indicesCategories.add(new TableInfo.Index("index_categories_name", true, Arrays.asList("name"), Arrays.asList("ASC")));
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.shakeexpense.app.data.database.entity.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsFamilyMembers = new HashMap<String, TableInfo.Column>(11);
        _columnsFamilyMembers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("family_id", new TableInfo.Column("family_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("device_id", new TableInfo.Column("device_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("privacy_mode", new TableInfo.Column("privacy_mode", "TEXT", true, 0, "'FULL_SHARED'", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("share_transactions", new TableInfo.Column("share_transactions", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("share_monthly_total", new TableInfo.Column("share_monthly_total", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("share_category_totals", new TableInfo.Column("share_category_totals", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("receive_family_alerts", new TableInfo.Column("receive_family_alerts", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyMembers.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFamilyMembers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFamilyMembers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFamilyMembers = new TableInfo("family_members", _columnsFamilyMembers, _foreignKeysFamilyMembers, _indicesFamilyMembers);
        final TableInfo _existingFamilyMembers = TableInfo.read(db, "family_members");
        if (!_infoFamilyMembers.equals(_existingFamilyMembers)) {
          return new RoomOpenHelper.ValidationResult(false, "family_members(com.shakeexpense.app.data.database.entity.FamilyMemberEntity).\n"
                  + " Expected:\n" + _infoFamilyMembers + "\n"
                  + " Found:\n" + _existingFamilyMembers);
        }
        final HashMap<String, TableInfo.Column> _columnsFamilyGroups = new HashMap<String, TableInfo.Column>(6);
        _columnsFamilyGroups.put("familyId", new TableInfo.Column("familyId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyGroups.put("family_name", new TableInfo.Column("family_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyGroups.put("invite_code", new TableInfo.Column("invite_code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyGroups.put("owner_uid", new TableInfo.Column("owner_uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyGroups.put("monthly_spending_limit_cents", new TableInfo.Column("monthly_spending_limit_cents", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyGroups.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFamilyGroups = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFamilyGroups = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFamilyGroups = new TableInfo("family_groups", _columnsFamilyGroups, _foreignKeysFamilyGroups, _indicesFamilyGroups);
        final TableInfo _existingFamilyGroups = TableInfo.read(db, "family_groups");
        if (!_infoFamilyGroups.equals(_existingFamilyGroups)) {
          return new RoomOpenHelper.ValidationResult(false, "family_groups(com.shakeexpense.app.data.database.entity.FamilyGroupEntity).\n"
                  + " Expected:\n" + _infoFamilyGroups + "\n"
                  + " Found:\n" + _existingFamilyGroups);
        }
        final HashMap<String, TableInfo.Column> _columnsFamilyBudgets = new HashMap<String, TableInfo.Column>(5);
        _columnsFamilyBudgets.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyBudgets.put("family_id", new TableInfo.Column("family_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyBudgets.put("category_name", new TableInfo.Column("category_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyBudgets.put("limit_cents", new TableInfo.Column("limit_cents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyBudgets.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFamilyBudgets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFamilyBudgets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFamilyBudgets = new TableInfo("family_budgets", _columnsFamilyBudgets, _foreignKeysFamilyBudgets, _indicesFamilyBudgets);
        final TableInfo _existingFamilyBudgets = TableInfo.read(db, "family_budgets");
        if (!_infoFamilyBudgets.equals(_existingFamilyBudgets)) {
          return new RoomOpenHelper.ValidationResult(false, "family_budgets(com.shakeexpense.app.data.database.entity.FamilyBudgetEntity).\n"
                  + " Expected:\n" + _infoFamilyBudgets + "\n"
                  + " Found:\n" + _existingFamilyBudgets);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(12);
        _columnsExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("uuid", new TableInfo.Column("uuid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("category_id", new TableInfo.Column("category_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount_cents", new TableInfo.Column("amount_cents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("type", new TableInfo.Column("type", "TEXT", true, 0, "'DEBIT'", TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("source", new TableInfo.Column("source", "TEXT", true, 0, "'MANUAL_SHAKE'", TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("custom_name", new TableInfo.Column("custom_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("bank_ref", new TableInfo.Column("bank_ref", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, "'PENDING'", TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(5);
        _indicesExpenses.add(new TableInfo.Index("index_expenses_uuid", true, Arrays.asList("uuid"), Arrays.asList("ASC")));
        _indicesExpenses.add(new TableInfo.Index("index_expenses_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesExpenses.add(new TableInfo.Index("index_expenses_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesExpenses.add(new TableInfo.Index("index_expenses_category_id", false, Arrays.asList("category_id"), Arrays.asList("ASC")));
        _indicesExpenses.add(new TableInfo.Index("index_expenses_sync_status", false, Arrays.asList("sync_status"), Arrays.asList("ASC")));
        final TableInfo _infoExpenses = new TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "expenses(com.shakeexpense.app.data.database.entity.ExpenseEntity).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsFinancialProfile = new HashMap<String, TableInfo.Column>(8);
        _columnsFinancialProfile.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("monthly_income_cents", new TableInfo.Column("monthly_income_cents", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("savings_target_cents", new TableInfo.Column("savings_target_cents", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("billing_cycle_day", new TableInfo.Column("billing_cycle_day", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("tier", new TableInfo.Column("tier", "TEXT", true, 0, "'FREE'", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("safety_score", new TableInfo.Column("safety_score", "INTEGER", true, 0, "80", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("monthly_spending_limit_cents", new TableInfo.Column("monthly_spending_limit_cents", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY));
        _columnsFinancialProfile.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFinancialProfile = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFinancialProfile = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFinancialProfile = new TableInfo("financial_profile", _columnsFinancialProfile, _foreignKeysFinancialProfile, _indicesFinancialProfile);
        final TableInfo _existingFinancialProfile = TableInfo.read(db, "financial_profile");
        if (!_infoFinancialProfile.equals(_existingFinancialProfile)) {
          return new RoomOpenHelper.ValidationResult(false, "financial_profile(com.shakeexpense.app.data.database.entity.FinancialProfileEntity).\n"
                  + " Expected:\n" + _infoFinancialProfile + "\n"
                  + " Found:\n" + _existingFinancialProfile);
        }
        final HashMap<String, TableInfo.Column> _columnsRecurringPayments = new HashMap<String, TableInfo.Column>(12);
        _columnsRecurringPayments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("amount_cents", new TableInfo.Column("amount_cents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("cadence", new TableInfo.Column("cadence", "TEXT", true, 0, "'MONTHLY'", TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("category_id", new TableInfo.Column("category_id", "INTEGER", true, 0, "4", TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("last_charged_timestamp", new TableInfo.Column("last_charged_timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("next_due_timestamp", new TableInfo.Column("next_due_timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("is_auto_detected", new TableInfo.Column("is_auto_detected", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, "1", TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("merchant_key", new TableInfo.Column("merchant_key", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringPayments.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecurringPayments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecurringPayments = new HashSet<TableInfo.Index>(2);
        _indicesRecurringPayments.add(new TableInfo.Index("index_recurring_payments_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesRecurringPayments.add(new TableInfo.Index("index_recurring_payments_name", false, Arrays.asList("name"), Arrays.asList("ASC")));
        final TableInfo _infoRecurringPayments = new TableInfo("recurring_payments", _columnsRecurringPayments, _foreignKeysRecurringPayments, _indicesRecurringPayments);
        final TableInfo _existingRecurringPayments = TableInfo.read(db, "recurring_payments");
        if (!_infoRecurringPayments.equals(_existingRecurringPayments)) {
          return new RoomOpenHelper.ValidationResult(false, "recurring_payments(com.shakeexpense.app.data.database.entity.RecurringPaymentEntity).\n"
                  + " Expected:\n" + _infoRecurringPayments + "\n"
                  + " Found:\n" + _existingRecurringPayments);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "f3472a6c095c35f688e661f61fdf0425", "84faeecdd78aeb0ee4227ff49e2212be");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "categories","family_members","family_groups","family_budgets","expenses","financial_profile","recurring_payments");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `family_members`");
      _db.execSQL("DELETE FROM `family_groups`");
      _db.execSQL("DELETE FROM `family_budgets`");
      _db.execSQL("DELETE FROM `expenses`");
      _db.execSQL("DELETE FROM `financial_profile`");
      _db.execSQL("DELETE FROM `recurring_payments`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FamilyMemberDao.class, FamilyMemberDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FamilyGroupDao.class, FamilyGroupDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FamilyBudgetDao.class, FamilyBudgetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FinancialProfileDao.class, FinancialProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RecurringPaymentDao.class, RecurringPaymentDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public FamilyMemberDao familyMemberDao() {
    if (_familyMemberDao != null) {
      return _familyMemberDao;
    } else {
      synchronized(this) {
        if(_familyMemberDao == null) {
          _familyMemberDao = new FamilyMemberDao_Impl(this);
        }
        return _familyMemberDao;
      }
    }
  }

  @Override
  public FamilyGroupDao familyGroupDao() {
    if (_familyGroupDao != null) {
      return _familyGroupDao;
    } else {
      synchronized(this) {
        if(_familyGroupDao == null) {
          _familyGroupDao = new FamilyGroupDao_Impl(this);
        }
        return _familyGroupDao;
      }
    }
  }

  @Override
  public FamilyBudgetDao familyBudgetDao() {
    if (_familyBudgetDao != null) {
      return _familyBudgetDao;
    } else {
      synchronized(this) {
        if(_familyBudgetDao == null) {
          _familyBudgetDao = new FamilyBudgetDao_Impl(this);
        }
        return _familyBudgetDao;
      }
    }
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public FinancialProfileDao financialProfileDao() {
    if (_financialProfileDao != null) {
      return _financialProfileDao;
    } else {
      synchronized(this) {
        if(_financialProfileDao == null) {
          _financialProfileDao = new FinancialProfileDao_Impl(this);
        }
        return _financialProfileDao;
      }
    }
  }

  @Override
  public RecurringPaymentDao recurringPaymentDao() {
    if (_recurringPaymentDao != null) {
      return _recurringPaymentDao;
    } else {
      synchronized(this) {
        if(_recurringPaymentDao == null) {
          _recurringPaymentDao = new RecurringPaymentDao_Impl(this);
        }
        return _recurringPaymentDao;
      }
    }
  }
}
