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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shakeexpense.app.data.database.entity.FamilyMemberEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class FamilyMemberDao_Impl implements FamilyMemberDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FamilyMemberEntity> __insertionAdapterOfFamilyMemberEntity;

  private final EntityDeletionOrUpdateAdapter<FamilyMemberEntity> __deletionAdapterOfFamilyMemberEntity;

  private final EntityDeletionOrUpdateAdapter<FamilyMemberEntity> __updateAdapterOfFamilyMemberEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllMembers;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMemberPrivacyMode;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePrivacySettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateExitRequested;

  public FamilyMemberDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFamilyMemberEntity = new EntityInsertionAdapter<FamilyMemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `family_members` (`id`,`family_id`,`name`,`role`,`device_id`,`privacy_mode`,`share_transactions`,`share_monthly_total`,`share_category_totals`,`receive_family_alerts`,`is_exit_requested`,`created_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FamilyMemberEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFamilyId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getRole());
        statement.bindString(5, entity.getDeviceId());
        statement.bindString(6, entity.getPrivacyMode());
        final int _tmp = entity.getShareTransactions() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getShareMonthlyTotal() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getShareCategoryTotals() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        final int _tmp_3 = entity.getReceiveFamilyAlerts() ? 1 : 0;
        statement.bindLong(10, _tmp_3);
        final int _tmp_4 = entity.isExitRequested() ? 1 : 0;
        statement.bindLong(11, _tmp_4);
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfFamilyMemberEntity = new EntityDeletionOrUpdateAdapter<FamilyMemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `family_members` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FamilyMemberEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfFamilyMemberEntity = new EntityDeletionOrUpdateAdapter<FamilyMemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `family_members` SET `id` = ?,`family_id` = ?,`name` = ?,`role` = ?,`device_id` = ?,`privacy_mode` = ?,`share_transactions` = ?,`share_monthly_total` = ?,`share_category_totals` = ?,`receive_family_alerts` = ?,`is_exit_requested` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FamilyMemberEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFamilyId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getRole());
        statement.bindString(5, entity.getDeviceId());
        statement.bindString(6, entity.getPrivacyMode());
        final int _tmp = entity.getShareTransactions() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.getShareMonthlyTotal() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getShareCategoryTotals() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        final int _tmp_3 = entity.getReceiveFamilyAlerts() ? 1 : 0;
        statement.bindLong(10, _tmp_3);
        final int _tmp_4 = entity.isExitRequested() ? 1 : 0;
        statement.bindLong(11, _tmp_4);
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindString(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllMembers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM family_members";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMemberPrivacyMode = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE family_members SET privacy_mode = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePrivacySettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE family_members \n"
                + "        SET share_transactions = ?,\n"
                + "            share_monthly_total = ?,\n"
                + "            share_category_totals = ?,\n"
                + "            receive_family_alerts = ?\n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateExitRequested = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE family_members SET is_exit_requested = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMember(final FamilyMemberEntity member,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFamilyMemberEntity.insert(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMembers(final List<FamilyMemberEntity> members,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFamilyMemberEntity.insert(members);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMember(final FamilyMemberEntity member,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFamilyMemberEntity.handle(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMember(final FamilyMemberEntity member,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFamilyMemberEntity.handle(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllMembers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllMembers.acquire();
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
          __preparedStmtOfDeleteAllMembers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMemberPrivacyMode(final String id, final String privacyMode,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMemberPrivacyMode.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, privacyMode);
        _argIndex = 2;
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
          __preparedStmtOfUpdateMemberPrivacyMode.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePrivacySettings(final String id, final boolean shareTransactions,
      final boolean shareMonthlyTotal, final boolean shareCategoryTotals,
      final boolean receiveFamilyAlerts, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePrivacySettings.acquire();
        int _argIndex = 1;
        final int _tmp = shareTransactions ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        final int _tmp_1 = shareMonthlyTotal ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 3;
        final int _tmp_2 = shareCategoryTotals ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_2);
        _argIndex = 4;
        final int _tmp_3 = receiveFamilyAlerts ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_3);
        _argIndex = 5;
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
          __preparedStmtOfUpdatePrivacySettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateExitRequested(final String id, final boolean requested,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateExitRequested.acquire();
        int _argIndex = 1;
        final int _tmp = requested ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
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
          __preparedStmtOfUpdateExitRequested.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FamilyMemberEntity>> getAllMembers() {
    final String _sql = "SELECT * FROM family_members ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"family_members"}, new Callable<List<FamilyMemberEntity>>() {
      @Override
      @NonNull
      public List<FamilyMemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final int _cursorIndexOfPrivacyMode = CursorUtil.getColumnIndexOrThrow(_cursor, "privacy_mode");
          final int _cursorIndexOfShareTransactions = CursorUtil.getColumnIndexOrThrow(_cursor, "share_transactions");
          final int _cursorIndexOfShareMonthlyTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "share_monthly_total");
          final int _cursorIndexOfShareCategoryTotals = CursorUtil.getColumnIndexOrThrow(_cursor, "share_category_totals");
          final int _cursorIndexOfReceiveFamilyAlerts = CursorUtil.getColumnIndexOrThrow(_cursor, "receive_family_alerts");
          final int _cursorIndexOfIsExitRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "is_exit_requested");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<FamilyMemberEntity> _result = new ArrayList<FamilyMemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FamilyMemberEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpPrivacyMode;
            _tmpPrivacyMode = _cursor.getString(_cursorIndexOfPrivacyMode);
            final boolean _tmpShareTransactions;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShareTransactions);
            _tmpShareTransactions = _tmp != 0;
            final boolean _tmpShareMonthlyTotal;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShareMonthlyTotal);
            _tmpShareMonthlyTotal = _tmp_1 != 0;
            final boolean _tmpShareCategoryTotals;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfShareCategoryTotals);
            _tmpShareCategoryTotals = _tmp_2 != 0;
            final boolean _tmpReceiveFamilyAlerts;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfReceiveFamilyAlerts);
            _tmpReceiveFamilyAlerts = _tmp_3 != 0;
            final boolean _tmpIsExitRequested;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsExitRequested);
            _tmpIsExitRequested = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new FamilyMemberEntity(_tmpId,_tmpFamilyId,_tmpName,_tmpRole,_tmpDeviceId,_tmpPrivacyMode,_tmpShareTransactions,_tmpShareMonthlyTotal,_tmpShareCategoryTotals,_tmpReceiveFamilyAlerts,_tmpIsExitRequested,_tmpCreatedAt);
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
  public Object getAllMembersSync(
      final Continuation<? super List<FamilyMemberEntity>> $completion) {
    final String _sql = "SELECT * FROM family_members ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FamilyMemberEntity>>() {
      @Override
      @NonNull
      public List<FamilyMemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final int _cursorIndexOfPrivacyMode = CursorUtil.getColumnIndexOrThrow(_cursor, "privacy_mode");
          final int _cursorIndexOfShareTransactions = CursorUtil.getColumnIndexOrThrow(_cursor, "share_transactions");
          final int _cursorIndexOfShareMonthlyTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "share_monthly_total");
          final int _cursorIndexOfShareCategoryTotals = CursorUtil.getColumnIndexOrThrow(_cursor, "share_category_totals");
          final int _cursorIndexOfReceiveFamilyAlerts = CursorUtil.getColumnIndexOrThrow(_cursor, "receive_family_alerts");
          final int _cursorIndexOfIsExitRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "is_exit_requested");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<FamilyMemberEntity> _result = new ArrayList<FamilyMemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FamilyMemberEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpPrivacyMode;
            _tmpPrivacyMode = _cursor.getString(_cursorIndexOfPrivacyMode);
            final boolean _tmpShareTransactions;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShareTransactions);
            _tmpShareTransactions = _tmp != 0;
            final boolean _tmpShareMonthlyTotal;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShareMonthlyTotal);
            _tmpShareMonthlyTotal = _tmp_1 != 0;
            final boolean _tmpShareCategoryTotals;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfShareCategoryTotals);
            _tmpShareCategoryTotals = _tmp_2 != 0;
            final boolean _tmpReceiveFamilyAlerts;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfReceiveFamilyAlerts);
            _tmpReceiveFamilyAlerts = _tmp_3 != 0;
            final boolean _tmpIsExitRequested;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsExitRequested);
            _tmpIsExitRequested = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new FamilyMemberEntity(_tmpId,_tmpFamilyId,_tmpName,_tmpRole,_tmpDeviceId,_tmpPrivacyMode,_tmpShareTransactions,_tmpShareMonthlyTotal,_tmpShareCategoryTotals,_tmpReceiveFamilyAlerts,_tmpIsExitRequested,_tmpCreatedAt);
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
  public Object getMemberById(final String id,
      final Continuation<? super FamilyMemberEntity> $completion) {
    final String _sql = "SELECT * FROM family_members WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FamilyMemberEntity>() {
      @Override
      @Nullable
      public FamilyMemberEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final int _cursorIndexOfPrivacyMode = CursorUtil.getColumnIndexOrThrow(_cursor, "privacy_mode");
          final int _cursorIndexOfShareTransactions = CursorUtil.getColumnIndexOrThrow(_cursor, "share_transactions");
          final int _cursorIndexOfShareMonthlyTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "share_monthly_total");
          final int _cursorIndexOfShareCategoryTotals = CursorUtil.getColumnIndexOrThrow(_cursor, "share_category_totals");
          final int _cursorIndexOfReceiveFamilyAlerts = CursorUtil.getColumnIndexOrThrow(_cursor, "receive_family_alerts");
          final int _cursorIndexOfIsExitRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "is_exit_requested");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final FamilyMemberEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpPrivacyMode;
            _tmpPrivacyMode = _cursor.getString(_cursorIndexOfPrivacyMode);
            final boolean _tmpShareTransactions;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShareTransactions);
            _tmpShareTransactions = _tmp != 0;
            final boolean _tmpShareMonthlyTotal;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShareMonthlyTotal);
            _tmpShareMonthlyTotal = _tmp_1 != 0;
            final boolean _tmpShareCategoryTotals;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfShareCategoryTotals);
            _tmpShareCategoryTotals = _tmp_2 != 0;
            final boolean _tmpReceiveFamilyAlerts;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfReceiveFamilyAlerts);
            _tmpReceiveFamilyAlerts = _tmp_3 != 0;
            final boolean _tmpIsExitRequested;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsExitRequested);
            _tmpIsExitRequested = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new FamilyMemberEntity(_tmpId,_tmpFamilyId,_tmpName,_tmpRole,_tmpDeviceId,_tmpPrivacyMode,_tmpShareTransactions,_tmpShareMonthlyTotal,_tmpShareCategoryTotals,_tmpReceiveFamilyAlerts,_tmpIsExitRequested,_tmpCreatedAt);
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
  public Flow<FamilyMemberEntity> getMemberByIdFlow(final String id) {
    final String _sql = "SELECT * FROM family_members WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"family_members"}, new Callable<FamilyMemberEntity>() {
      @Override
      @Nullable
      public FamilyMemberEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final int _cursorIndexOfPrivacyMode = CursorUtil.getColumnIndexOrThrow(_cursor, "privacy_mode");
          final int _cursorIndexOfShareTransactions = CursorUtil.getColumnIndexOrThrow(_cursor, "share_transactions");
          final int _cursorIndexOfShareMonthlyTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "share_monthly_total");
          final int _cursorIndexOfShareCategoryTotals = CursorUtil.getColumnIndexOrThrow(_cursor, "share_category_totals");
          final int _cursorIndexOfReceiveFamilyAlerts = CursorUtil.getColumnIndexOrThrow(_cursor, "receive_family_alerts");
          final int _cursorIndexOfIsExitRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "is_exit_requested");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final FamilyMemberEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpPrivacyMode;
            _tmpPrivacyMode = _cursor.getString(_cursorIndexOfPrivacyMode);
            final boolean _tmpShareTransactions;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShareTransactions);
            _tmpShareTransactions = _tmp != 0;
            final boolean _tmpShareMonthlyTotal;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShareMonthlyTotal);
            _tmpShareMonthlyTotal = _tmp_1 != 0;
            final boolean _tmpShareCategoryTotals;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfShareCategoryTotals);
            _tmpShareCategoryTotals = _tmp_2 != 0;
            final boolean _tmpReceiveFamilyAlerts;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfReceiveFamilyAlerts);
            _tmpReceiveFamilyAlerts = _tmp_3 != 0;
            final boolean _tmpIsExitRequested;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsExitRequested);
            _tmpIsExitRequested = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new FamilyMemberEntity(_tmpId,_tmpFamilyId,_tmpName,_tmpRole,_tmpDeviceId,_tmpPrivacyMode,_tmpShareTransactions,_tmpShareMonthlyTotal,_tmpShareCategoryTotals,_tmpReceiveFamilyAlerts,_tmpIsExitRequested,_tmpCreatedAt);
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
  public Flow<List<FamilyMemberEntity>> getMembersByFamilyId(final String familyId) {
    final String _sql = "SELECT * FROM family_members WHERE family_id = ? ORDER BY created_at ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, familyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"family_members"}, new Callable<List<FamilyMemberEntity>>() {
      @Override
      @NonNull
      public List<FamilyMemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFamilyId = CursorUtil.getColumnIndexOrThrow(_cursor, "family_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "device_id");
          final int _cursorIndexOfPrivacyMode = CursorUtil.getColumnIndexOrThrow(_cursor, "privacy_mode");
          final int _cursorIndexOfShareTransactions = CursorUtil.getColumnIndexOrThrow(_cursor, "share_transactions");
          final int _cursorIndexOfShareMonthlyTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "share_monthly_total");
          final int _cursorIndexOfShareCategoryTotals = CursorUtil.getColumnIndexOrThrow(_cursor, "share_category_totals");
          final int _cursorIndexOfReceiveFamilyAlerts = CursorUtil.getColumnIndexOrThrow(_cursor, "receive_family_alerts");
          final int _cursorIndexOfIsExitRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "is_exit_requested");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<FamilyMemberEntity> _result = new ArrayList<FamilyMemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FamilyMemberEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFamilyId;
            _tmpFamilyId = _cursor.getString(_cursorIndexOfFamilyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpDeviceId;
            _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            final String _tmpPrivacyMode;
            _tmpPrivacyMode = _cursor.getString(_cursorIndexOfPrivacyMode);
            final boolean _tmpShareTransactions;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfShareTransactions);
            _tmpShareTransactions = _tmp != 0;
            final boolean _tmpShareMonthlyTotal;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfShareMonthlyTotal);
            _tmpShareMonthlyTotal = _tmp_1 != 0;
            final boolean _tmpShareCategoryTotals;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfShareCategoryTotals);
            _tmpShareCategoryTotals = _tmp_2 != 0;
            final boolean _tmpReceiveFamilyAlerts;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfReceiveFamilyAlerts);
            _tmpReceiveFamilyAlerts = _tmp_3 != 0;
            final boolean _tmpIsExitRequested;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsExitRequested);
            _tmpIsExitRequested = _tmp_4 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new FamilyMemberEntity(_tmpId,_tmpFamilyId,_tmpName,_tmpRole,_tmpDeviceId,_tmpPrivacyMode,_tmpShareTransactions,_tmpShareMonthlyTotal,_tmpShareCategoryTotals,_tmpReceiveFamilyAlerts,_tmpIsExitRequested,_tmpCreatedAt);
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
  public Object getMemberCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM family_members";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
