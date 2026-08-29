package com.shakeexpense.app.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shakeexpense.app.data.database.entity.FamilyBudgetEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class FamilyBudgetDao_Impl implements FamilyBudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FamilyBudgetEntity> __insertionAdapterOfFamilyBudgetEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBudget;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBudgetByCategory;

  private final SharedSQLiteStatement __preparedStmtOfClearBudgetsForFamily;

  public FamilyBudgetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFamilyBudgetEntity = new EntityInsertionAdapter<FamilyBudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `family_budgets` (`id`,`family_id`,`category_name`,`limit_cents`,`updated_at`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FamilyBudgetEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFamilyId());
        statement.bindString(3, entity.getCategoryName());
        statement.bindLong(4, entity.getLimitCents());
        statement.bindLong(5, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfDeleteBudget = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_budgets WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBudgetByCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_budgets WHERE family_id = ? AND category_name = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearBudgetsForFamily = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_budgets WHERE family_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateBudget(final FamilyBudgetEntity budget,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFamilyBudgetEntity.insert(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdateBudgets(final List<FamilyBudgetEntity> budgets,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFamilyBudgetEntity.insert(budgets);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBudget(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBudget.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteBudget.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBudgetByCategory(final String familyId, final String categoryName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBudgetByCategory.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, familyId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, categoryName);
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
          __preparedStmtOfDeleteBudgetByCategory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearBudgetsForFamily(final String familyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearBudgetsForFamily.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, familyId);
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
          __preparedStmtOfClearBudgetsForFamily.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FamilyBudgetEntity>> getFamilyBudgetsFlow(final String familyId) {
    final String _sql = "SELECT * FROM family_budgets WHERE family_id = ? ORDER BY category_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, familyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"family_budgets"}, new Callable<List<FamilyBudgetEntity>>() {
      @Override
      @NonNull
      public List<FamilyBudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "limit_cents");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<FamilyBudgetEntity> _result = new ArrayList<FamilyBudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FamilyBudgetEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final long _tmpLimitCents;
            _tmpLimitCents = _cursor.getLong(_cursorIndexOfLimitCents);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new FamilyBudgetEntity(_tmpId,_tmpFamilyId,_tmpCategoryName,_tmpLimitCents,_tmpUpdatedAt);
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
  public Object getFamilyBudgetsSync(final String familyId,
      final Continuation<? super List<FamilyBudgetEntity>> $completion) {
    final String _sql = "SELECT * FROM family_budgets WHERE family_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, familyId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FamilyBudgetEntity>>() {
      @Override
      @NonNull
      public List<FamilyBudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "limit_cents");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<FamilyBudgetEntity> _result = new ArrayList<FamilyBudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FamilyBudgetEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final long _tmpLimitCents;
            _tmpLimitCents = _cursor.getLong(_cursorIndexOfLimitCents);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new FamilyBudgetEntity(_tmpId,_tmpFamilyId,_tmpCategoryName,_tmpLimitCents,_tmpUpdatedAt);
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
  public Object getBudgetForCategory(final String familyId, final String categoryName,
      final Continuation<? super FamilyBudgetEntity> $completion) {
    final String _sql = "SELECT * FROM family_budgets WHERE family_id = ? AND category_name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, familyId);
    _argIndex = 2;
    _statement.bindString(_argIndex, categoryName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FamilyBudgetEntity>() {
      @Override
      @Nullable
      public FamilyBudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfCategoryName = CursorUtil.getColumnIndexOrThrow(_cursor, "category_name");
          final int _cursorIndexOfLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "limit_cents");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final FamilyBudgetEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpCategoryName;
            _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            final long _tmpLimitCents;
            _tmpLimitCents = _cursor.getLong(_cursorIndexOfLimitCents);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new FamilyBudgetEntity(_tmpId,_tmpFamilyId,_tmpCategoryName,_tmpLimitCents,_tmpUpdatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
