package com.shakeexpense.app.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shakeexpense.app.data.database.entity.ExpenseEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExpenseDao_Impl implements ExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExpenseEntity> __insertionAdapterOfExpenseEntity;

  private final EntityDeletionOrUpdateAdapter<ExpenseEntity> __deletionAdapterOfExpenseEntity;

  private final EntityDeletionOrUpdateAdapter<ExpenseEntity> __updateAdapterOfExpenseEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpenseByUuid;

  private final SharedSQLiteStatement __preparedStmtOfMigrateUserExpenses;

  public ExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExpenseEntity = new EntityInsertionAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `expenses` (`id`,`uuid`,`user_id`,`category_id`,`amount_cents`,`type`,`source`,`custom_name`,`bank_ref`,`timestamp`,`updated_at`,`sync_status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUuid());
        statement.bindString(3, entity.getUserId());
        statement.bindLong(4, entity.getCategoryId());
        statement.bindLong(5, entity.getAmountCents());
        statement.bindString(6, entity.getType());
        statement.bindString(7, entity.getSource());
        if (entity.getCustomName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCustomName());
        }
        if (entity.getBankRef() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getBankRef());
        }
        statement.bindLong(10, entity.getTimestamp());
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindString(12, entity.getSyncStatus());
      }
    };
    this.__deletionAdapterOfExpenseEntity = new EntityDeletionOrUpdateAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `expenses` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfExpenseEntity = new EntityDeletionOrUpdateAdapter<ExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `expenses` SET `id` = ?,`uuid` = ?,`user_id` = ?,`category_id` = ?,`amount_cents` = ?,`type` = ?,`source` = ?,`custom_name` = ?,`bank_ref` = ?,`timestamp` = ?,`updated_at` = ?,`sync_status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExpenseEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUuid());
        statement.bindString(3, entity.getUserId());
        statement.bindLong(4, entity.getCategoryId());
        statement.bindLong(5, entity.getAmountCents());
        statement.bindString(6, entity.getType());
        statement.bindString(7, entity.getSource());
        if (entity.getCustomName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCustomName());
        }
        if (entity.getBankRef() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getBankRef());
        }
        statement.bindLong(10, entity.getTimestamp());
        statement.bindLong(11, entity.getUpdatedAt());
        statement.bindString(12, entity.getSyncStatus());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteExpenseByUuid = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM expenses WHERE uuid = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMigrateUserExpenses = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE expenses SET user_id = ? WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertExpense(final ExpenseEntity expense,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExpenseEntity.insertAndReturnId(expense);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertExpenses(final List<ExpenseEntity> expenses,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfExpenseEntity.insertAndReturnIdsList(expenses);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpense(final ExpenseEntity expense,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExpenseEntity.handle(expense);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExpense(final ExpenseEntity expense,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExpenseEntity.handle(expense);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpenseByUuid(final String uuid,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpenseByUuid.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, uuid);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteExpenseByUuid.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object migrateUserExpenses(final String fromUserId, final String toUserId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMigrateUserExpenses.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, toUserId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, fromUserId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMigrateUserExpenses.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExpenseWithDetailsRaw>> getSpreadsheetStream() {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            e.id AS expense_id,\n"
            + "            e.uuid AS expense_uuid,\n"
            + "            e.user_id AS user_id,\n"
            + "            COALESCE(m.name, 'You') AS user_name,\n"
            + "            e.amount_cents AS amount_cents,\n"
            + "            e.type AS transaction_type,\n"
            + "            e.source AS transaction_source,\n"
            + "            e.timestamp AS timestamp,\n"
            + "            e.custom_name AS custom_name,\n"
            + "            e.bank_ref AS bank_ref,\n"
            + "            COALESCE(c.id, e.category_id) AS category_id,\n"
            + "            COALESCE(c.name, 'Expense') AS category_name,\n"
            + "            COALESCE(c.color_hex, '#F59E0B') AS category_color,\n"
            + "            e.sync_status AS sync_status\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN categories c ON e.category_id = c.id\n"
            + "        LEFT JOIN family_members m ON e.user_id = m.id\n"
            + "        ORDER BY e.timestamp DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses", "categories",
        "family_members"}, new Callable<List<ExpenseWithDetailsRaw>>() {
      @Override
      @NonNull
      public List<ExpenseWithDetailsRaw> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExpenseId = 0;
          final int _cursorIndexOfExpenseUuid = 1;
          final int _cursorIndexOfUserId = 2;
          final int _cursorIndexOfUserName = 3;
          final int _cursorIndexOfAmountCents = 4;
          final int _cursorIndexOfTransactionType = 5;
          final int _cursorIndexOfTransactionSource = 6;
          final int _cursorIndexOfTimestamp = 7;
          final int _cursorIndexOfCustomName = 8;
          final int _cursorIndexOfBankRef = 9;
          final int _cursorIndexOfCategoryId = 10;
          final int _cursorIndexOfCategoryName = 11;
          final int _cursorIndexOfCategoryColor = 12;
          final int _cursorIndexOfSyncStatus = 13;
          final List<ExpenseWithDetailsRaw> _result = new ArrayList<ExpenseWithDetailsRaw>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseWithDetailsRaw _item;
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final String _tmpExpenseUuid;
            _tmpExpenseUuid = _cursor.getString(_cursorIndexOfExpenseUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpTransactionType;
            _tmpTransactionType = _cursor.getString(_cursorIndexOfTransactionType);
            final String _tmpTransactionSource;
            _tmpTransactionSource = _cursor.getString(_cursorIndexOfTransactionSource);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _item = new ExpenseWithDetailsRaw(_tmpExpenseId,_tmpExpenseUuid,_tmpUserId,_tmpUserName,_tmpAmountCents,_tmpTransactionType,_tmpTransactionSource,_tmpTimestamp,_tmpCustomName,_tmpBankRef,_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSpreadsheetStreamSync(
      final Continuation<? super List<ExpenseWithDetailsRaw>> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            e.id AS expense_id,\n"
            + "            e.uuid AS expense_uuid,\n"
            + "            e.user_id AS user_id,\n"
            + "            COALESCE(m.name, 'You') AS user_name,\n"
            + "            e.amount_cents AS amount_cents,\n"
            + "            e.type AS transaction_type,\n"
            + "            e.source AS transaction_source,\n"
            + "            e.timestamp AS timestamp,\n"
            + "            e.custom_name AS custom_name,\n"
            + "            e.bank_ref AS bank_ref,\n"
            + "            COALESCE(c.id, e.category_id) AS category_id,\n"
            + "            COALESCE(c.name, 'Expense') AS category_name,\n"
            + "            COALESCE(c.color_hex, '#F59E0B') AS category_color,\n"
            + "            e.sync_status AS sync_status\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN categories c ON e.category_id = c.id\n"
            + "        LEFT JOIN family_members m ON e.user_id = m.id\n"
            + "        ORDER BY e.timestamp DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExpenseWithDetailsRaw>>() {
      @Override
      @NonNull
      public List<ExpenseWithDetailsRaw> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExpenseId = 0;
          final int _cursorIndexOfExpenseUuid = 1;
          final int _cursorIndexOfUserId = 2;
          final int _cursorIndexOfUserName = 3;
          final int _cursorIndexOfAmountCents = 4;
          final int _cursorIndexOfTransactionType = 5;
          final int _cursorIndexOfTransactionSource = 6;
          final int _cursorIndexOfTimestamp = 7;
          final int _cursorIndexOfCustomName = 8;
          final int _cursorIndexOfBankRef = 9;
          final int _cursorIndexOfCategoryId = 10;
          final int _cursorIndexOfCategoryName = 11;
          final int _cursorIndexOfCategoryColor = 12;
          final int _cursorIndexOfSyncStatus = 13;
          final List<ExpenseWithDetailsRaw> _result = new ArrayList<ExpenseWithDetailsRaw>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseWithDetailsRaw _item;
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final String _tmpExpenseUuid;
            _tmpExpenseUuid = _cursor.getString(_cursorIndexOfExpenseUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpTransactionType;
            _tmpTransactionType = _cursor.getString(_cursorIndexOfTransactionType);
            final String _tmpTransactionSource;
            _tmpTransactionSource = _cursor.getString(_cursorIndexOfTransactionSource);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _item = new ExpenseWithDetailsRaw(_tmpExpenseId,_tmpExpenseUuid,_tmpUserId,_tmpUserName,_tmpAmountCents,_tmpTransactionType,_tmpTransactionSource,_tmpTimestamp,_tmpCustomName,_tmpBankRef,_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExpenseWithDetailsRaw>> getExpensesByUserId(final String targetUserId) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            e.id AS expense_id,\n"
            + "            e.uuid AS expense_uuid,\n"
            + "            e.user_id AS user_id,\n"
            + "            COALESCE(m.name, 'You') AS user_name,\n"
            + "            e.amount_cents AS amount_cents,\n"
            + "            e.type AS transaction_type,\n"
            + "            e.source AS transaction_source,\n"
            + "            e.timestamp AS timestamp,\n"
            + "            e.custom_name AS custom_name,\n"
            + "            e.bank_ref AS bank_ref,\n"
            + "            COALESCE(c.id, e.category_id) AS category_id,\n"
            + "            COALESCE(c.name, 'Expense') AS category_name,\n"
            + "            COALESCE(c.color_hex, '#F59E0B') AS category_color,\n"
            + "            e.sync_status AS sync_status\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN categories c ON e.category_id = c.id\n"
            + "        LEFT JOIN family_members m ON e.user_id = m.id\n"
            + "        WHERE e.user_id = ?\n"
            + "        ORDER BY e.timestamp DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, targetUserId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses", "categories",
        "family_members"}, new Callable<List<ExpenseWithDetailsRaw>>() {
      @Override
      @NonNull
      public List<ExpenseWithDetailsRaw> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExpenseId = 0;
          final int _cursorIndexOfExpenseUuid = 1;
          final int _cursorIndexOfUserId = 2;
          final int _cursorIndexOfUserName = 3;
          final int _cursorIndexOfAmountCents = 4;
          final int _cursorIndexOfTransactionType = 5;
          final int _cursorIndexOfTransactionSource = 6;
          final int _cursorIndexOfTimestamp = 7;
          final int _cursorIndexOfCustomName = 8;
          final int _cursorIndexOfBankRef = 9;
          final int _cursorIndexOfCategoryId = 10;
          final int _cursorIndexOfCategoryName = 11;
          final int _cursorIndexOfCategoryColor = 12;
          final int _cursorIndexOfSyncStatus = 13;
          final List<ExpenseWithDetailsRaw> _result = new ArrayList<ExpenseWithDetailsRaw>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseWithDetailsRaw _item;
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final String _tmpExpenseUuid;
            _tmpExpenseUuid = _cursor.getString(_cursorIndexOfExpenseUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpTransactionType;
            _tmpTransactionType = _cursor.getString(_cursorIndexOfTransactionType);
            final String _tmpTransactionSource;
            _tmpTransactionSource = _cursor.getString(_cursorIndexOfTransactionSource);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _item = new ExpenseWithDetailsRaw(_tmpExpenseId,_tmpExpenseUuid,_tmpUserId,_tmpUserName,_tmpAmountCents,_tmpTransactionType,_tmpTransactionSource,_tmpTimestamp,_tmpCustomName,_tmpBankRef,_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<MemberExpenseSummaryRaw> getMemberSpendingSummary(final String targetUserId) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            e.user_id AS user_id,\n"
            + "            COALESCE(m.name, 'You') AS user_name,\n"
            + "            COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount_cents ELSE 0 END), 0) AS total_debit_cents,\n"
            + "            COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount_cents ELSE 0 END), 0) AS total_credit_cents,\n"
            + "            COUNT(e.id) AS transaction_count\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN family_members m ON e.user_id = m.id\n"
            + "        WHERE e.user_id = ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, targetUserId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses",
        "family_members"}, new Callable<MemberExpenseSummaryRaw>() {
      @Override
      @Nullable
      public MemberExpenseSummaryRaw call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = 0;
          final int _cursorIndexOfUserName = 1;
          final int _cursorIndexOfTotalDebitCents = 2;
          final int _cursorIndexOfTotalCreditCents = 3;
          final int _cursorIndexOfTransactionCount = 4;
          final MemberExpenseSummaryRaw _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final long _tmpTotalDebitCents;
            _tmpTotalDebitCents = _cursor.getLong(_cursorIndexOfTotalDebitCents);
            final long _tmpTotalCreditCents;
            _tmpTotalCreditCents = _cursor.getLong(_cursorIndexOfTotalCreditCents);
            final int _tmpTransactionCount;
            _tmpTransactionCount = _cursor.getInt(_cursorIndexOfTransactionCount);
            _result = new MemberExpenseSummaryRaw(_tmpUserId,_tmpUserName,_tmpTotalDebitCents,_tmpTotalCreditCents,_tmpTransactionCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMemberSpendingSummarySync(final String targetUserId,
      final Continuation<? super MemberExpenseSummaryRaw> $completion) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            e.user_id AS user_id,\n"
            + "            COALESCE(m.name, 'You') AS user_name,\n"
            + "            COALESCE(SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount_cents ELSE 0 END), 0) AS total_debit_cents,\n"
            + "            COALESCE(SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount_cents ELSE 0 END), 0) AS total_credit_cents,\n"
            + "            COUNT(e.id) AS transaction_count\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN family_members m ON e.user_id = m.id\n"
            + "        WHERE e.user_id = ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, targetUserId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MemberExpenseSummaryRaw>() {
      @Override
      @Nullable
      public MemberExpenseSummaryRaw call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = 0;
          final int _cursorIndexOfUserName = 1;
          final int _cursorIndexOfTotalDebitCents = 2;
          final int _cursorIndexOfTotalCreditCents = 3;
          final int _cursorIndexOfTransactionCount = 4;
          final MemberExpenseSummaryRaw _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final long _tmpTotalDebitCents;
            _tmpTotalDebitCents = _cursor.getLong(_cursorIndexOfTotalDebitCents);
            final long _tmpTotalCreditCents;
            _tmpTotalCreditCents = _cursor.getLong(_cursorIndexOfTotalCreditCents);
            final int _tmpTransactionCount;
            _tmpTransactionCount = _cursor.getInt(_cursorIndexOfTransactionCount);
            _result = new MemberExpenseSummaryRaw(_tmpUserId,_tmpUserName,_tmpTotalDebitCents,_tmpTotalCreditCents,_tmpTransactionCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CategoryBreakdownRaw>> getMemberCategoryBreakdown(final String targetUserId) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            COALESCE(c.id, e.category_id) AS category_id,\n"
            + "            COALESCE(c.name, 'Expense') AS category_name,\n"
            + "            COALESCE(c.color_hex, '#F59E0B') AS category_color,\n"
            + "            COALESCE(SUM(e.amount_cents), 0) AS category_total_cents,\n"
            + "            COUNT(e.id) AS transaction_count\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN categories c ON e.category_id = c.id\n"
            + "        WHERE e.user_id = ? AND e.type = 'DEBIT'\n"
            + "        GROUP BY COALESCE(c.id, e.category_id)\n"
            + "        ORDER BY category_total_cents DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, targetUserId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses",
        "categories"}, new Callable<List<CategoryBreakdownRaw>>() {
      @Override
      @NonNull
      public List<CategoryBreakdownRaw> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategoryId = 0;
          final int _cursorIndexOfCategoryName = 1;
          final int _cursorIndexOfCategoryColor = 2;
          final int _cursorIndexOfCategoryTotalCents = 3;
          final int _cursorIndexOfTransactionCount = 4;
          final List<CategoryBreakdownRaw> _result = new ArrayList<CategoryBreakdownRaw>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryBreakdownRaw _item;
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final long _tmpCategoryTotalCents;
            _tmpCategoryTotalCents = _cursor.getLong(_cursorIndexOfCategoryTotalCents);
            final int _tmpTransactionCount;
            _tmpTransactionCount = _cursor.getInt(_cursorIndexOfTransactionCount);
            _item = new CategoryBreakdownRaw(_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpCategoryTotalCents,_tmpTransactionCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CategoryBreakdownRaw>> getAllCategoryBreakdown() {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            COALESCE(c.id, e.category_id) AS category_id,\n"
            + "            COALESCE(c.name, 'Expense') AS category_name,\n"
            + "            COALESCE(c.color_hex, '#F59E0B') AS category_color,\n"
            + "            COALESCE(SUM(e.amount_cents), 0) AS category_total_cents,\n"
            + "            COUNT(e.id) AS transaction_count\n"
            + "        FROM expenses e\n"
            + "        LEFT JOIN categories c ON e.category_id = c.id\n"
            + "        WHERE e.type = 'DEBIT'\n"
            + "        GROUP BY COALESCE(c.id, e.category_id)\n"
            + "        ORDER BY category_total_cents DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses",
        "categories"}, new Callable<List<CategoryBreakdownRaw>>() {
      @Override
      @NonNull
      public List<CategoryBreakdownRaw> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategoryId = 0;
          final int _cursorIndexOfCategoryName = 1;
          final int _cursorIndexOfCategoryColor = 2;
          final int _cursorIndexOfCategoryTotalCents = 3;
          final int _cursorIndexOfTransactionCount = 4;
          final List<CategoryBreakdownRaw> _result = new ArrayList<CategoryBreakdownRaw>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryBreakdownRaw _item;
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final String _tmpCategoryColor;
            _tmpCategoryColor = _cursor.getString(_cursorIndexOfCategoryColor);
            final long _tmpCategoryTotalCents;
            _tmpCategoryTotalCents = _cursor.getLong(_cursorIndexOfCategoryTotalCents);
            final int _tmpTransactionCount;
            _tmpTransactionCount = _cursor.getInt(_cursorIndexOfTransactionCount);
            _item = new CategoryBreakdownRaw(_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpCategoryTotalCents,_tmpTransactionCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPendingSyncExpenses(
      final Continuation<? super List<ExpenseEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM expenses \n"
            + "        WHERE sync_status = 'PENDING' \n"
            + "        ORDER BY updated_at ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCustomName = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_name");
          final int _cursorIndexOfBankRef = CursorUtil.getColumnIndexOrThrow(_cursor, "bank_ref");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _item = new ExpenseEntity(_tmpId,_tmpUuid,_tmpUserId,_tmpCategoryId,_tmpAmountCents,_tmpType,_tmpSource,_tmpCustomName,_tmpBankRef,_tmpTimestamp,_tmpUpdatedAt,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getExpenseById(final long id,
      final Continuation<? super ExpenseEntity> $completion) {
    final String _sql = "SELECT * FROM expenses WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExpenseEntity>() {
      @Override
      @Nullable
      public ExpenseEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCustomName = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_name");
          final int _cursorIndexOfBankRef = CursorUtil.getColumnIndexOrThrow(_cursor, "bank_ref");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final ExpenseEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _result = new ExpenseEntity(_tmpId,_tmpUuid,_tmpUserId,_tmpCategoryId,_tmpAmountCents,_tmpType,_tmpSource,_tmpCustomName,_tmpBankRef,_tmpTimestamp,_tmpUpdatedAt,_tmpSyncStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getExpenseByUuid(final String uuid,
      final Continuation<? super ExpenseEntity> $completion) {
    final String _sql = "SELECT * FROM expenses WHERE uuid = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uuid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExpenseEntity>() {
      @Override
      @Nullable
      public ExpenseEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCustomName = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_name");
          final int _cursorIndexOfBankRef = CursorUtil.getColumnIndexOrThrow(_cursor, "bank_ref");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final ExpenseEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _result = new ExpenseEntity(_tmpId,_tmpUuid,_tmpUserId,_tmpCategoryId,_tmpAmountCents,_tmpType,_tmpSource,_tmpCustomName,_tmpBankRef,_tmpTimestamp,_tmpUpdatedAt,_tmpSyncStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findPotentialDuplicate(final long amountCents, final long minTimestamp,
      final long maxTimestamp, final String bankRef, final String userId,
      final Continuation<? super ExpenseEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM expenses\n"
            + "        WHERE (? IS NULL OR user_id = ?)\n"
            + "          AND amount_cents = ?\n"
            + "          AND timestamp BETWEEN ? AND ?\n"
            + "          AND (? IS NULL OR bank_ref = ?)\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 7);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 3;
    _statement.bindLong(_argIndex, amountCents);
    _argIndex = 4;
    _statement.bindLong(_argIndex, minTimestamp);
    _argIndex = 5;
    _statement.bindLong(_argIndex, maxTimestamp);
    _argIndex = 6;
    if (bankRef == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, bankRef);
    }
    _argIndex = 7;
    if (bankRef == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, bankRef);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExpenseEntity>() {
      @Override
      @Nullable
      public ExpenseEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCustomName = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_name");
          final int _cursorIndexOfBankRef = CursorUtil.getColumnIndexOrThrow(_cursor, "bank_ref");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final ExpenseEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _result = new ExpenseEntity(_tmpId,_tmpUuid,_tmpUserId,_tmpCategoryId,_tmpAmountCents,_tmpType,_tmpSource,_tmpCustomName,_tmpBankRef,_tmpTimestamp,_tmpUpdatedAt,_tmpSyncStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getExpenseCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM expenses";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Long> getTotalSpentSince(final long sinceTimestamp) {
    final String _sql = "SELECT COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount_cents ELSE 0 END), 0) FROM expenses WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceTimestamp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Long> getAllTimeTotalSpent() {
    final String _sql = "SELECT COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount_cents ELSE 0 END), 0) FROM expenses";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getPendingSyncCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM expenses WHERE sync_status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"expenses"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getExpensesInTimeRange(final String userId, final long startTime,
      final long endTime, final Continuation<? super List<ExpenseEntity>> $completion) {
    final String _sql = "SELECT * FROM expenses WHERE user_id = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExpenseEntity>>() {
      @Override
      @NonNull
      public List<ExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCustomName = CursorUtil.getColumnIndexOrThrow(_cursor, "custom_name");
          final int _cursorIndexOfBankRef = CursorUtil.getColumnIndexOrThrow(_cursor, "bank_ref");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final List<ExpenseEntity> _result = new ArrayList<ExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExpenseEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUuid;
            _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpCustomName;
            if (_cursor.isNull(_cursorIndexOfCustomName)) {
              _tmpCustomName = null;
            } else {
              _tmpCustomName = _cursor.getString(_cursorIndexOfCustomName);
            }
            final String _tmpBankRef;
            if (_cursor.isNull(_cursorIndexOfBankRef)) {
              _tmpBankRef = null;
            } else {
              _tmpBankRef = _cursor.getString(_cursorIndexOfBankRef);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            _item = new ExpenseEntity(_tmpId,_tmpUuid,_tmpUserId,_tmpCategoryId,_tmpAmountCents,_tmpType,_tmpSource,_tmpCustomName,_tmpBankRef,_tmpTimestamp,_tmpUpdatedAt,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSyncStatus(final List<String> uuids, final String syncStatus,
      final long updatedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE expenses SET sync_status = ");
        _stringBuilder.append("?");
        _stringBuilder.append(", updated_at = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE uuid IN (");
        final int _inputSize = uuids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindString(_argIndex, syncStatus);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        for (String _item : uuids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteExpensesByUuids(final List<String> uuids,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM expenses WHERE uuid IN (");
        final int _inputSize = uuids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : uuids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          final Integer _result = _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
