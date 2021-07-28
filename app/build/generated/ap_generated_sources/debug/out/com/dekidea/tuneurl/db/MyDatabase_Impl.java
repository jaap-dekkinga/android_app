package com.dekidea.tuneurl.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomOpenHelper;
import android.arch.persistence.room.RoomOpenHelper.Delegate;
import android.arch.persistence.room.util.TableInfo;
import android.arch.persistence.room.util.TableInfo.Column;
import android.arch.persistence.room.util.TableInfo.ForeignKey;
import android.arch.persistence.room.util.TableInfo.Index;
import com.dekidea.tuneurl.db.dao.MyDao;
import com.dekidea.tuneurl.db.dao.MyDao_Impl;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unchecked")
public class MyDatabase_Impl extends MyDatabase {
  private volatile MyDao _myDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `SavedInfo` (`song_id` INTEGER NOT NULL, `title` TEXT, `url` TEXT, `image_url` TEXT, `type` INTEGER NOT NULL, `date` TEXT, `date_absolute` INTEGER NOT NULL, PRIMARY KEY(`song_id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `PollItem` (`song_id` INTEGER NOT NULL, `title` TEXT, `poll_response` TEXT, `date` TEXT, `date_absolute` INTEGER NOT NULL, PRIMARY KEY(`song_id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `AudioItem` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audio_file_path` TEXT, `user_response` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"f426d77450b02052e3d62947a3469dfa\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `SavedInfo`");
        _db.execSQL("DROP TABLE IF EXISTS `PollItem`");
        _db.execSQL("DROP TABLE IF EXISTS `AudioItem`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsSavedInfo = new HashMap<String, TableInfo.Column>(7);
        _columnsSavedInfo.put("song_id", new TableInfo.Column("song_id", "INTEGER", true, 1));
        _columnsSavedInfo.put("title", new TableInfo.Column("title", "TEXT", false, 0));
        _columnsSavedInfo.put("url", new TableInfo.Column("url", "TEXT", false, 0));
        _columnsSavedInfo.put("image_url", new TableInfo.Column("image_url", "TEXT", false, 0));
        _columnsSavedInfo.put("type", new TableInfo.Column("type", "INTEGER", true, 0));
        _columnsSavedInfo.put("date", new TableInfo.Column("date", "TEXT", false, 0));
        _columnsSavedInfo.put("date_absolute", new TableInfo.Column("date_absolute", "INTEGER", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavedInfo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavedInfo = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavedInfo = new TableInfo("SavedInfo", _columnsSavedInfo, _foreignKeysSavedInfo, _indicesSavedInfo);
        final TableInfo _existingSavedInfo = TableInfo.read(_db, "SavedInfo");
        if (! _infoSavedInfo.equals(_existingSavedInfo)) {
          throw new IllegalStateException("Migration didn't properly handle SavedInfo(com.dekidea.tuneurl.db.entity.SavedInfo).\n"
                  + " Expected:\n" + _infoSavedInfo + "\n"
                  + " Found:\n" + _existingSavedInfo);
        }
        final HashMap<String, TableInfo.Column> _columnsPollItem = new HashMap<String, TableInfo.Column>(5);
        _columnsPollItem.put("song_id", new TableInfo.Column("song_id", "INTEGER", true, 1));
        _columnsPollItem.put("title", new TableInfo.Column("title", "TEXT", false, 0));
        _columnsPollItem.put("poll_response", new TableInfo.Column("poll_response", "TEXT", false, 0));
        _columnsPollItem.put("date", new TableInfo.Column("date", "TEXT", false, 0));
        _columnsPollItem.put("date_absolute", new TableInfo.Column("date_absolute", "INTEGER", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPollItem = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPollItem = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPollItem = new TableInfo("PollItem", _columnsPollItem, _foreignKeysPollItem, _indicesPollItem);
        final TableInfo _existingPollItem = TableInfo.read(_db, "PollItem");
        if (! _infoPollItem.equals(_existingPollItem)) {
          throw new IllegalStateException("Migration didn't properly handle PollItem(com.dekidea.tuneurl.db.entity.PollItem).\n"
                  + " Expected:\n" + _infoPollItem + "\n"
                  + " Found:\n" + _existingPollItem);
        }
        final HashMap<String, TableInfo.Column> _columnsAudioItem = new HashMap<String, TableInfo.Column>(3);
        _columnsAudioItem.put("id", new TableInfo.Column("id", "INTEGER", true, 1));
        _columnsAudioItem.put("audio_file_path", new TableInfo.Column("audio_file_path", "TEXT", false, 0));
        _columnsAudioItem.put("user_response", new TableInfo.Column("user_response", "TEXT", false, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAudioItem = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAudioItem = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAudioItem = new TableInfo("AudioItem", _columnsAudioItem, _foreignKeysAudioItem, _indicesAudioItem);
        final TableInfo _existingAudioItem = TableInfo.read(_db, "AudioItem");
        if (! _infoAudioItem.equals(_existingAudioItem)) {
          throw new IllegalStateException("Migration didn't properly handle AudioItem(com.dekidea.tuneurl.db.entity.AudioItem).\n"
                  + " Expected:\n" + _infoAudioItem + "\n"
                  + " Found:\n" + _existingAudioItem);
        }
      }
    }, "f426d77450b02052e3d62947a3469dfa", "e3e21d9ebd76e2570e7497d3ecbb04df");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "SavedInfo","PollItem","AudioItem");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `SavedInfo`");
      _db.execSQL("DELETE FROM `PollItem`");
      _db.execSQL("DELETE FROM `AudioItem`");
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
  public MyDao dao() {
    if (_myDao != null) {
      return _myDao;
    } else {
      synchronized(this) {
        if(_myDao == null) {
          _myDao = new MyDao_Impl(this);
        }
        return _myDao;
      }
    }
  }
}
