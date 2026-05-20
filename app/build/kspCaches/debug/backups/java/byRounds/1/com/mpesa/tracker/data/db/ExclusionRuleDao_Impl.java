package com.mpesa.tracker.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mpesa.tracker.data.model.ExclusionRule;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class ExclusionRuleDao_Impl implements ExclusionRuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExclusionRule> __insertionAdapterOfExclusionRule;

  private final ExclusionConverters __exclusionConverters = new ExclusionConverters();

  private final EntityDeletionOrUpdateAdapter<ExclusionRule> __deletionAdapterOfExclusionRule;

  private final EntityDeletionOrUpdateAdapter<ExclusionRule> __updateAdapterOfExclusionRule;

  private final SharedSQLiteStatement __preparedStmtOfSetEnabled;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllCustomRules;

  public ExclusionRuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExclusionRule = new EntityInsertionAdapter<ExclusionRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `exclusion_rules` (`id`,`keyword`,`matchType`,`isEnabled`,`isPreset`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExclusionRule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        final String _tmp = __exclusionConverters.fromMatchType(entity.getMatchType());
        statement.bindString(3, _tmp);
        final int _tmp_1 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        final int _tmp_2 = entity.isPreset() ? 1 : 0;
        statement.bindLong(5, _tmp_2);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfExclusionRule = new EntityDeletionOrUpdateAdapter<ExclusionRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `exclusion_rules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExclusionRule entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfExclusionRule = new EntityDeletionOrUpdateAdapter<ExclusionRule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `exclusion_rules` SET `id` = ?,`keyword` = ?,`matchType` = ?,`isEnabled` = ?,`isPreset` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExclusionRule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKeyword());
        final String _tmp = __exclusionConverters.fromMatchType(entity.getMatchType());
        statement.bindString(3, _tmp);
        final int _tmp_1 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp_1);
        final int _tmp_2 = entity.isPreset() ? 1 : 0;
        statement.bindLong(5, _tmp_2);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfSetEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE exclusion_rules SET isEnabled = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllCustomRules = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exclusion_rules WHERE isPreset = 0";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ExclusionRule rule, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfExclusionRule.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ExclusionRule rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExclusionRule.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ExclusionRule rule, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExclusionRule.handle(rule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setEnabled(final long id, final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
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
          __preparedStmtOfSetEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllCustomRules(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllCustomRules.acquire();
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
          __preparedStmtOfDeleteAllCustomRules.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExclusionRule>> getAllRules() {
    final String _sql = "SELECT * FROM exclusion_rules ORDER BY isPreset DESC, keyword ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exclusion_rules"}, new Callable<List<ExclusionRule>>() {
      @Override
      @NonNull
      public List<ExclusionRule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfMatchType = CursorUtil.getColumnIndexOrThrow(_cursor, "matchType");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPreset = CursorUtil.getColumnIndexOrThrow(_cursor, "isPreset");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExclusionRule> _result = new ArrayList<ExclusionRule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExclusionRule _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final ExclusionRule.MatchType _tmpMatchType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMatchType);
            _tmpMatchType = __exclusionConverters.toMatchType(_tmp);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final boolean _tmpIsPreset;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPreset);
            _tmpIsPreset = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExclusionRule(_tmpId,_tmpKeyword,_tmpMatchType,_tmpIsEnabled,_tmpIsPreset,_tmpCreatedAt);
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
  public Object getEnabledRules(final Continuation<? super List<ExclusionRule>> $completion) {
    final String _sql = "SELECT * FROM exclusion_rules WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExclusionRule>>() {
      @Override
      @NonNull
      public List<ExclusionRule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKeyword = CursorUtil.getColumnIndexOrThrow(_cursor, "keyword");
          final int _cursorIndexOfMatchType = CursorUtil.getColumnIndexOrThrow(_cursor, "matchType");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsPreset = CursorUtil.getColumnIndexOrThrow(_cursor, "isPreset");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExclusionRule> _result = new ArrayList<ExclusionRule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExclusionRule _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKeyword;
            _tmpKeyword = _cursor.getString(_cursorIndexOfKeyword);
            final ExclusionRule.MatchType _tmpMatchType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfMatchType);
            _tmpMatchType = __exclusionConverters.toMatchType(_tmp);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final boolean _tmpIsPreset;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPreset);
            _tmpIsPreset = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExclusionRule(_tmpId,_tmpKeyword,_tmpMatchType,_tmpIsEnabled,_tmpIsPreset,_tmpCreatedAt);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM exclusion_rules";
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
