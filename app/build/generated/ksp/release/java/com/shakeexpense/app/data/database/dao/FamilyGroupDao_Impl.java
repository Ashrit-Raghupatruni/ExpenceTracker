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
import com.shakeexpense.app.data.database.entity.FamilyGroupEntity;
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
public final class FamilyGroupDao_Impl implements FamilyGroupDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FamilyGroupEntity> __insertionAdapterOfFamilyGroupEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearFamilyGroups;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFamilyGroup;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSpendingLimit;

  public FamilyGroupDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFamilyGroupEntity = new EntityInsertionAdapter<FamilyGroupEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `family_groups` (`familyId`,`family_name`,`invite_code`,`owner_uid`,`monthly_spending_limit_cents`,`created_at`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FamilyGroupEntity entity) {
        statement.bindString(1, entity.getFamilyId());
        statement.bindString(2, entity.getFamilyName());
        statement.bindString(3, entity.getInviteCode());
        statement.bindString(4, entity.getOwnerUid());
        statement.bindLong(5, entity.getMonthlySpendingLimitCents());
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfClearFamilyGroups = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_groups";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFamilyGroup = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_groups WHERE familyId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSpendingLimit = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE family_groups SET monthly_spending_limit_cents = ? WHERE familyId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertFamilyGroup(final FamilyGroupEntity familyGroup,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFamilyGroupEntity.insert(familyGroup);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearFamilyGroups(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearFamilyGroups.acquire();
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
          __preparedStmtOfClearFamilyGroups.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFamilyGroup(final String familyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFamilyGroup.acquire();
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
          __preparedStmtOfDeleteFamilyGroup.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSpendingLimit(final String familyId, final long limitCents,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSpendingLimit.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, limitCents);
        _argIndex = 2;
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
          __preparedStmtOfUpdateSpendingLimit.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<FamilyGroupEntity> getActiveFamilyGroupFlow() {
    final String _sql = "SELECT * FROM family_groups LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"family_groups"}, new Callable<FamilyGroupEntity>() {
      @Override
      @Nullable
      public FamilyGroupEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFamilyName = CursorUtil.getColumnIndexOrThrow(_cursor, "family_name");
          final int _cursorIndexOfInviteCode = CursorUtil.getColumnIndexOrThrow(_cursor, "invite_code");
          final int _cursorIndexOfOwnerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "owner_uid");
          final int _cursorIndexOfMonthlySpendingLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_spending_limit_cents");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final FamilyGroupEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpFamilyName;
            _tmpFamilyName = _cursor.getString(_cursorIndexOfFamilyName);
            final String _tmpInviteCode;
            _tmpInviteCode = _cursor.getString(_cursorIndexOfInviteCode);
            final String _tmpOwnerUid;
            _tmpOwnerUid = _cursor.getString(_cursorIndexOfOwnerUid);
            final long _tmpMonthlySpendingLimitCents;
            _tmpMonthlySpendingLimitCents = _cursor.getLong(_cursorIndexOfMonthlySpendingLimitCents);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new FamilyGroupEntity(_tmpFamilyId,_tmpFamilyName,_tmpInviteCode,_tmpOwnerUid,_tmpMonthlySpendingLimitCents,_tmpCreatedAt);
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
  public Object getActiveFamilyGroup(final Continuation<? super FamilyGroupEntity> $completion) {
    final String _sql = "SELECT * FROM family_groups LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FamilyGroupEntity>() {
      @Override
      @Nullable
      public FamilyGroupEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "familyId");
          final int _cursorIndexOfFamilyName = CursorUtil.getColumnIndexOrThrow(_cursor, "family_name");
          final int _cursorIndexOfInviteCode = CursorUtil.getColumnIndexOrThrow(_cursor, "invite_code");
          final int _cursorIndexOfOwnerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "owner_uid");
          final int _cursorIndexOfMonthlySpendingLimitCents = CursorUtil.getColumnIndexOrThrow(_cursor, "monthly_spending_limit_cents");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final FamilyGroupEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpFamilyName;
            _tmpFamilyName = _cursor.getString(_cursorIndexOfFamilyName);
            final String _tmpInviteCode;
            _tmpInviteCode = _cursor.getString(_cursorIndexOfInviteCode);
            final String _tmpOwnerUid;
            _tmpOwnerUid = _cursor.getString(_cursorIndexOfOwnerUid);
            final long _tmpMonthlySpendingLimitCents;
            _tmpMonthlySpendingLimitCents = _cursor.getLong(_cursorIndexOfMonthlySpendingLimitCents);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new FamilyGroupEntity(_tmpFamilyId,_tmpFamilyName,_tmpInviteCode,_tmpOwnerUid,_tmpMonthlySpendingLimitCents,_tmpCreatedAt);
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
