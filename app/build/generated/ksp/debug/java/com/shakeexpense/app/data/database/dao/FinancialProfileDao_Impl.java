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
import com.shakeexpense.app.data.database.entity.FinancialProfileEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FinancialProfileDao_Impl implements FinancialProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FinancialProfileEntity> __insertionAdapterOfFinancialProfileEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBudgetParameters;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSafetyScore;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTier;

  public FinancialProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFinancialProfileEntity = new EntityInsertionAdapter<FinancialProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `financial_profile` (`user_id`,`monthly_income_cents`,`savings_target_cents`,`billing_cycle_day`,`tier`,`safety_score`,`monthly_spending_limit_cents`,`updated_at`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FinancialProfileEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindLong(2, entity.getMonthlyIncomeCents());
        statement.bindLong(3, entity.getSavingsTargetCents());
        statement.bindLong(4, entity.getBillingCycleDay());
        statement.bindString(5, entity.getTier());
        statement.bindLong(6, entity.getSafetyScore());
        statement.bindLong(7, entity.getMonthlySpendingLimitCents());
        statement.bindLong(8, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfUpdateBudgetParameters = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE financial_profile SET monthly_income_cents = ?, savings_target_cents = ?, billing_cycle_day = ?, updated_at = ? WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSafetyScore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE financial_profile SET safety_score = ?, updated_at = ? WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTier = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE financial_profile SET tier = ?, updated_at = ? WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateProfile(final FinancialProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFinancialProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBudgetParameters(final String userId, final long incomeCents,
      final long savingsTargetCents, final int cycleDay, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBudgetParameters.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, incomeCents);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, savingsTargetCents);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, cycleDay);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 5;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfUpdateBudgetParameters.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSafetyScore(final String userId, final int score, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSafetyScore.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, score);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfUpdateSafetyScore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTier(final String userId, final String tier, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTier.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, tier);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfUpdateTier.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<FinancialProfileEntity> getProfileFlow(final String userId) {
    final String _sql = "SELECT * FROM financial_profile WHERE user_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"financial_profile"}, new Callable<FinancialProfileEntity>() {
      @Override
      @Nullable
      public FinancialProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfMonthlyIncomeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_income_cents");
          final int _cursorIndexOfSavingsTargetCents = CursorUtil.getColumnIndexOrThrow(_cursor, "savings_target_cents");
          final int _cursorIndexOfBillingCycleDay = CursorUtil.getColumnIndexOrThrow(_cursor, "billing_cycle_day");
          final int _cursorIndexOfTier = CursorUtil.getColumnIndexOrThrow(_cursor, "tier");
          final int _cursorIndexOfSafetyScore = CursorUtil.getColumnIndexOrThrow(_cursor, "safety_score");
          final int _cursorIndexOfMonthlySpendingLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_spending_limit_cents");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final FinancialProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpMonthlyIncomeCents;
            _tmpMonthlyIncomeCents = _cursor.getLong(_cursorIndexOfMonthlyIncomeCents);
            final long _tmpSavingsTargetCents;
            _tmpSavingsTargetCents = _cursor.getLong(_cursorIndexOfSavingsTargetCents);
            final int _tmpBillingCycleDay;
            _tmpBillingCycleDay = _cursor.getInt(_cursorIndexOfBillingCycleDay);
            final String _tmpTier;
            _tmpTier = _cursor.getString(_cursorIndexOfTier);
            final int _tmpSafetyScore;
            _tmpSafetyScore = _cursor.getInt(_cursorIndexOfSafetyScore);
            final long _tmpMonthlySpendingLimitCents;
            _tmpMonthlySpendingLimitCents = _cursor.getLong(_cursorIndexOfMonthlySpendingLimitCents);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new FinancialProfileEntity(_tmpUserId,_tmpMonthlyIncomeCents,_tmpSavingsTargetCents,_tmpBillingCycleDay,_tmpTier,_tmpSafetyScore,_tmpMonthlySpendingLimitCents,_tmpUpdatedAt);
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
  public Object getProfile(final String userId,
      final Continuation<? super FinancialProfileEntity> $completion) {
    final String _sql = "SELECT * FROM financial_profile WHERE user_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FinancialProfileEntity>() {
      @Override
      @Nullable
      public FinancialProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfMonthlyIncomeCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_income_cents");
          final int _cursorIndexOfSavingsTargetCents = CursorUtil.getColumnIndexOrThrow(_cursor, "savings_target_cents");
          final int _cursorIndexOfBillingCycleDay = CursorUtil.getColumnIndexOrThrow(_cursor, "billing_cycle_day");
          final int _cursorIndexOfTier = CursorUtil.getColumnIndexOrThrow(_cursor, "tier");
          final int _cursorIndexOfSafetyScore = CursorUtil.getColumnIndexOrThrow(_cursor, "safety_score");
          final int _cursorIndexOfMonthlySpendingLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_spending_limit_cents");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final FinancialProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpMonthlyIncomeCents;
            _tmpMonthlyIncomeCents = _cursor.getLong(_cursorIndexOfMonthlyIncomeCents);
            final long _tmpSavingsTargetCents;
            _tmpSavingsTargetCents = _cursor.getLong(_cursorIndexOfSavingsTargetCents);
            final int _tmpBillingCycleDay;
            _tmpBillingCycleDay = _cursor.getInt(_cursorIndexOfBillingCycleDay);
            final String _tmpTier;
            _tmpTier = _cursor.getString(_cursorIndexOfTier);
            final int _tmpSafetyScore;
            _tmpSafetyScore = _cursor.getInt(_cursorIndexOfSafetyScore);
            final long _tmpMonthlySpendingLimitCents;
            _tmpMonthlySpendingLimitCents = _cursor.getLong(_cursorIndexOfMonthlySpendingLimitCents);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new FinancialProfileEntity(_tmpUserId,_tmpMonthlyIncomeCents,_tmpSavingsTargetCents,_tmpBillingCycleDay,_tmpTier,_tmpSafetyScore,_tmpMonthlySpendingLimitCents,_tmpUpdatedAt);
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
