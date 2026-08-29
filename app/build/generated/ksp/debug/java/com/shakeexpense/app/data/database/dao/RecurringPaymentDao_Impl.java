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
import com.shakeexpense.app.data.database.entity.RecurringPaymentEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class RecurringPaymentDao_Impl implements RecurringPaymentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecurringPaymentEntity> __insertionAdapterOfRecurringPaymentEntity;

  private final EntityDeletionOrUpdateAdapter<RecurringPaymentEntity> __updateAdapterOfRecurringPaymentEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetPaymentActiveState;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllForUser;

  public RecurringPaymentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecurringPaymentEntity = new EntityInsertionAdapter<RecurringPaymentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recurring_payments` (`id`,`user_id`,`name`,`amount_cents`,`cadence`,`category_id`,`last_charged_timestamp`,`next_due_timestamp`,`is_auto_detected`,`is_active`,`merchant_key`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringPaymentEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getAmountCents());
        statement.bindString(5, entity.getCadence());
        statement.bindLong(6, entity.getCategoryId());
        statement.bindLong(7, entity.getLastChargedTimestamp());
        statement.bindLong(8, entity.getNextDueTimestamp());
        final int _tmp = entity.isAutoDetected() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        if (entity.getMerchantKey() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMerchantKey());
        }
        statement.bindLong(12, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfRecurringPaymentEntity = new EntityDeletionOrUpdateAdapter<RecurringPaymentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `recurring_payments` SET `id` = ?,`user_id` = ?,`name` = ?,`amount_cents` = ?,`cadence` = ?,`category_id` = ?,`last_charged_timestamp` = ?,`next_due_timestamp` = ?,`is_auto_detected` = ?,`is_active` = ?,`merchant_key` = ?,`created_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringPaymentEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getAmountCents());
        statement.bindString(5, entity.getCadence());
        statement.bindLong(6, entity.getCategoryId());
        statement.bindLong(7, entity.getLastChargedTimestamp());
        statement.bindLong(8, entity.getNextDueTimestamp());
        final int _tmp = entity.isAutoDetected() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        if (entity.getMerchantKey() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMerchantKey());
        }
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfSetPaymentActiveState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recurring_payments SET is_active = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recurring_payments WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recurring_payments WHERE user_id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecurringPayment(final RecurringPaymentEntity payment,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecurringPaymentEntity.insertAndReturnId(payment);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertRecurringPayments(final List<RecurringPaymentEntity> payments,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRecurringPaymentEntity.insert(payments);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecurringPayment(final RecurringPaymentEntity payment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRecurringPaymentEntity.handle(payment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setPaymentActiveState(final long id, final boolean isActive,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetPaymentActiveState.acquire();
        int _argIndex = 1;
        final int _tmp = isActive ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfSetPaymentActiveState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllForUser(final String userId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllForUser.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeleteAllForUser.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RecurringPaymentEntity>> getActiveRecurringPaymentsFlow(final String userId) {
    final String _sql = "SELECT * FROM recurring_payments WHERE user_id = ? AND is_active = 1 ORDER BY next_due_timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recurring_payments"}, new Callable<List<RecurringPaymentEntity>>() {
      @Override
      @NonNull
      public List<RecurringPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfLastChargedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "last_charged_timestamp");
          final int _cursorIndexOfNextDueTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "next_due_timestamp");
          final int _cursorIndexOfIsAutoDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "is_auto_detected");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMerchantKey = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant_key");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<RecurringPaymentEntity> _result = new ArrayList<RecurringPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpLastChargedTimestamp;
            _tmpLastChargedTimestamp = _cursor.getLong(_cursorIndexOfLastChargedTimestamp);
            final long _tmpNextDueTimestamp;
            _tmpNextDueTimestamp = _cursor.getLong(_cursorIndexOfNextDueTimestamp);
            final boolean _tmpIsAutoDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAutoDetected);
            _tmpIsAutoDetected = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final String _tmpMerchantKey;
            if (_cursor.isNull(_cursorIndexOfMerchantKey)) {
              _tmpMerchantKey = null;
            } else {
              _tmpMerchantKey = _cursor.getString(_cursorIndexOfMerchantKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RecurringPaymentEntity(_tmpId,_tmpUserId,_tmpName,_tmpAmountCents,_tmpCadence,_tmpCategoryId,_tmpLastChargedTimestamp,_tmpNextDueTimestamp,_tmpIsAutoDetected,_tmpIsActive,_tmpMerchantKey,_tmpCreatedAt);
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
  public Object getActiveRecurringPayments(final String userId,
      final Continuation<? super List<RecurringPaymentEntity>> $completion) {
    final String _sql = "SELECT * FROM recurring_payments WHERE user_id = ? AND is_active = 1 ORDER BY next_due_timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecurringPaymentEntity>>() {
      @Override
      @NonNull
      public List<RecurringPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfLastChargedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "last_charged_timestamp");
          final int _cursorIndexOfNextDueTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "next_due_timestamp");
          final int _cursorIndexOfIsAutoDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "is_auto_detected");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMerchantKey = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant_key");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<RecurringPaymentEntity> _result = new ArrayList<RecurringPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpLastChargedTimestamp;
            _tmpLastChargedTimestamp = _cursor.getLong(_cursorIndexOfLastChargedTimestamp);
            final long _tmpNextDueTimestamp;
            _tmpNextDueTimestamp = _cursor.getLong(_cursorIndexOfNextDueTimestamp);
            final boolean _tmpIsAutoDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAutoDetected);
            _tmpIsAutoDetected = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final String _tmpMerchantKey;
            if (_cursor.isNull(_cursorIndexOfMerchantKey)) {
              _tmpMerchantKey = null;
            } else {
              _tmpMerchantKey = _cursor.getString(_cursorIndexOfMerchantKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RecurringPaymentEntity(_tmpId,_tmpUserId,_tmpName,_tmpAmountCents,_tmpCadence,_tmpCategoryId,_tmpLastChargedTimestamp,_tmpNextDueTimestamp,_tmpIsAutoDetected,_tmpIsActive,_tmpMerchantKey,_tmpCreatedAt);
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
  public Flow<List<RecurringPaymentEntity>> getAllRecurringPaymentsFlow(final String userId) {
    final String _sql = "SELECT * FROM recurring_payments WHERE user_id = ? ORDER BY created_at DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recurring_payments"}, new Callable<List<RecurringPaymentEntity>>() {
      @Override
      @NonNull
      public List<RecurringPaymentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfLastChargedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "last_charged_timestamp");
          final int _cursorIndexOfNextDueTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "next_due_timestamp");
          final int _cursorIndexOfIsAutoDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "is_auto_detected");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMerchantKey = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant_key");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final List<RecurringPaymentEntity> _result = new ArrayList<RecurringPaymentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringPaymentEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpLastChargedTimestamp;
            _tmpLastChargedTimestamp = _cursor.getLong(_cursorIndexOfLastChargedTimestamp);
            final long _tmpNextDueTimestamp;
            _tmpNextDueTimestamp = _cursor.getLong(_cursorIndexOfNextDueTimestamp);
            final boolean _tmpIsAutoDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAutoDetected);
            _tmpIsAutoDetected = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final String _tmpMerchantKey;
            if (_cursor.isNull(_cursorIndexOfMerchantKey)) {
              _tmpMerchantKey = null;
            } else {
              _tmpMerchantKey = _cursor.getString(_cursorIndexOfMerchantKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RecurringPaymentEntity(_tmpId,_tmpUserId,_tmpName,_tmpAmountCents,_tmpCadence,_tmpCategoryId,_tmpLastChargedTimestamp,_tmpNextDueTimestamp,_tmpIsAutoDetected,_tmpIsActive,_tmpMerchantKey,_tmpCreatedAt);
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
  public Object getRecurringPaymentById(final long id,
      final Continuation<? super RecurringPaymentEntity> $completion) {
    final String _sql = "SELECT * FROM recurring_payments WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecurringPaymentEntity>() {
      @Override
      @Nullable
      public RecurringPaymentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfLastChargedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "last_charged_timestamp");
          final int _cursorIndexOfNextDueTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "next_due_timestamp");
          final int _cursorIndexOfIsAutoDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "is_auto_detected");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMerchantKey = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant_key");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final RecurringPaymentEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpLastChargedTimestamp;
            _tmpLastChargedTimestamp = _cursor.getLong(_cursorIndexOfLastChargedTimestamp);
            final long _tmpNextDueTimestamp;
            _tmpNextDueTimestamp = _cursor.getLong(_cursorIndexOfNextDueTimestamp);
            final boolean _tmpIsAutoDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAutoDetected);
            _tmpIsAutoDetected = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final String _tmpMerchantKey;
            if (_cursor.isNull(_cursorIndexOfMerchantKey)) {
              _tmpMerchantKey = null;
            } else {
              _tmpMerchantKey = _cursor.getString(_cursorIndexOfMerchantKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new RecurringPaymentEntity(_tmpId,_tmpUserId,_tmpName,_tmpAmountCents,_tmpCadence,_tmpCategoryId,_tmpLastChargedTimestamp,_tmpNextDueTimestamp,_tmpIsAutoDetected,_tmpIsActive,_tmpMerchantKey,_tmpCreatedAt);
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
  public Object findMatchingRecurring(final String userId, final String query,
      final Continuation<? super RecurringPaymentEntity> $completion) {
    final String _sql = "SELECT * FROM recurring_payments WHERE user_id = ? AND (name LIKE '%' || ? || '%' OR merchant_key LIKE '%' || ? || '%') LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecurringPaymentEntity>() {
      @Override
      @Nullable
      public RecurringPaymentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmountCents = CursorUtil.getColumnIndexOrThrow(_cursor, "amount_cents");
          final int _cursorIndexOfCadence = CursorUtil.getColumnIndexOrThrow(_cursor, "cadence");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "category_id");
          final int _cursorIndexOfLastChargedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "last_charged_timestamp");
          final int _cursorIndexOfNextDueTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "next_due_timestamp");
          final int _cursorIndexOfIsAutoDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "is_auto_detected");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfMerchantKey = CursorUtil.getColumnIndexOrThrow(_cursor, "merchant_key");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final RecurringPaymentEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmountCents;
            _tmpAmountCents = _cursor.getLong(_cursorIndexOfAmountCents);
            final String _tmpCadence;
            _tmpCadence = _cursor.getString(_cursorIndexOfCadence);
            final long _tmpCategoryId;
            _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            final long _tmpLastChargedTimestamp;
            _tmpLastChargedTimestamp = _cursor.getLong(_cursorIndexOfLastChargedTimestamp);
            final long _tmpNextDueTimestamp;
            _tmpNextDueTimestamp = _cursor.getLong(_cursorIndexOfNextDueTimestamp);
            final boolean _tmpIsAutoDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAutoDetected);
            _tmpIsAutoDetected = _tmp != 0;
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final String _tmpMerchantKey;
            if (_cursor.isNull(_cursorIndexOfMerchantKey)) {
              _tmpMerchantKey = null;
            } else {
              _tmpMerchantKey = _cursor.getString(_cursorIndexOfMerchantKey);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new RecurringPaymentEntity(_tmpId,_tmpUserId,_tmpName,_tmpAmountCents,_tmpCadence,_tmpCategoryId,_tmpLastChargedTimestamp,_tmpNextDueTimestamp,_tmpIsAutoDetected,_tmpIsActive,_tmpMerchantKey,_tmpCreatedAt);
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
