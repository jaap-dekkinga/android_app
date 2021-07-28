package com.dekidea.tuneurl.db.dao;

import android.arch.lifecycle.ComputableLiveData;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityDeletionOrUpdateAdapter;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.InvalidationTracker.Observer;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.arch.persistence.room.SharedSQLiteStatement;
import android.database.Cursor;
import android.support.annotation.NonNull;
import com.dekidea.tuneurl.db.entity.AudioItem;
import com.dekidea.tuneurl.db.entity.PollItem;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class MyDao_Impl implements MyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfSavedInfo;

  private final EntityInsertionAdapter __insertionAdapterOfPollItem;

  private final EntityInsertionAdapter __insertionAdapterOfAudioItem;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfAudioItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSavedInfo;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSavedInfo;

  public MyDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSavedInfo = new EntityInsertionAdapter<SavedInfo>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `SavedInfo`(`song_id`,`title`,`url`,`image_url`,`type`,`date`,`date_absolute`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SavedInfo value) {
        stmt.bindLong(1, value.getSong_id());
        if (value.getTitle() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTitle());
        }
        if (value.getUrl() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getUrl());
        }
        if (value.getImage_url() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getImage_url());
        }
        stmt.bindLong(5, value.getType());
        if (value.getDate() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getDate());
        }
        stmt.bindLong(7, value.getDate_absolute());
      }
    };
    this.__insertionAdapterOfPollItem = new EntityInsertionAdapter<PollItem>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `PollItem`(`song_id`,`title`,`poll_response`,`date`,`date_absolute`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, PollItem value) {
        stmt.bindLong(1, value.getSong_id());
        if (value.getTitle() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTitle());
        }
        if (value.getPoll_response() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getPoll_response());
        }
        if (value.getDate() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getDate());
        }
        stmt.bindLong(5, value.getDate_absolute());
      }
    };
    this.__insertionAdapterOfAudioItem = new EntityInsertionAdapter<AudioItem>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `AudioItem`(`id`,`audio_file_path`,`user_response`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AudioItem value) {
        stmt.bindLong(1, value.getId());
        if (value.getAudio_file_path() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getAudio_file_path());
        }
        if (value.getUser_response() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getUser_response());
        }
      }
    };
    this.__deletionAdapterOfAudioItem = new EntityDeletionOrUpdateAdapter<AudioItem>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `AudioItem` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AudioItem value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__preparedStmtOfDeleteSavedInfo = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM SavedInfo WHERE date_absolute < ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSavedInfo = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE SavedInfo SET type= ? WHERE song_id = ?";
        return _query;
      }
    };
  }

  @Override
  public void saveSavedInfo(SavedInfo item) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfSavedInfo.insert(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void savePollItem(PollItem item) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfPollItem.insert(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void saveAudioItem(AudioItem item) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfAudioItem.insert(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAudioItem(AudioItem item) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfAudioItem.handle(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteSavedInfo(long date_absolute) {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSavedInfo.acquire();
    __db.beginTransaction();
    try {
      int _argIndex = 1;
      _stmt.bindLong(_argIndex, date_absolute);
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteSavedInfo.release(_stmt);
    }
  }

  @Override
  public void updateSavedInfo(int type, long id) {
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSavedInfo.acquire();
    __db.beginTransaction();
    try {
      int _argIndex = 1;
      _stmt.bindLong(_argIndex, type);
      _argIndex = 2;
      _stmt.bindLong(_argIndex, id);
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfUpdateSavedInfo.release(_stmt);
    }
  }

  @Override
  public SavedInfo loadSavedInfo(long song_id) {
    final String _sql = "SELECT * FROM SavedInfo WHERE song_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, song_id);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfSongId = _cursor.getColumnIndexOrThrow("song_id");
      final int _cursorIndexOfTitle = _cursor.getColumnIndexOrThrow("title");
      final int _cursorIndexOfUrl = _cursor.getColumnIndexOrThrow("url");
      final int _cursorIndexOfImageUrl = _cursor.getColumnIndexOrThrow("image_url");
      final int _cursorIndexOfType = _cursor.getColumnIndexOrThrow("type");
      final int _cursorIndexOfDate = _cursor.getColumnIndexOrThrow("date");
      final int _cursorIndexOfDateAbsolute = _cursor.getColumnIndexOrThrow("date_absolute");
      final SavedInfo _result;
      if(_cursor.moveToFirst()) {
        final long _tmpSong_id;
        _tmpSong_id = _cursor.getLong(_cursorIndexOfSongId);
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final String _tmpUrl;
        _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
        final String _tmpImage_url;
        _tmpImage_url = _cursor.getString(_cursorIndexOfImageUrl);
        final int _tmpType;
        _tmpType = _cursor.getInt(_cursorIndexOfType);
        final String _tmpDate;
        _tmpDate = _cursor.getString(_cursorIndexOfDate);
        final long _tmpDate_absolute;
        _tmpDate_absolute = _cursor.getLong(_cursorIndexOfDateAbsolute);
        _result = new SavedInfo(_tmpSong_id,_tmpTitle,_tmpUrl,_tmpImage_url,_tmpType,_tmpDate,_tmpDate_absolute);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<SavedInfo>> loadSavedInfo() {
    final String _sql = "SELECT * FROM SavedInfo";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new ComputableLiveData<List<SavedInfo>>() {
      private Observer _observer;

      @Override
      protected List<SavedInfo> compute() {
        if (_observer == null) {
          _observer = new Observer("SavedInfo") {
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
              invalidate();
            }
          };
          __db.getInvalidationTracker().addWeakObserver(_observer);
        }
        final Cursor _cursor = __db.query(_statement);
        try {
          final int _cursorIndexOfSongId = _cursor.getColumnIndexOrThrow("song_id");
          final int _cursorIndexOfTitle = _cursor.getColumnIndexOrThrow("title");
          final int _cursorIndexOfUrl = _cursor.getColumnIndexOrThrow("url");
          final int _cursorIndexOfImageUrl = _cursor.getColumnIndexOrThrow("image_url");
          final int _cursorIndexOfType = _cursor.getColumnIndexOrThrow("type");
          final int _cursorIndexOfDate = _cursor.getColumnIndexOrThrow("date");
          final int _cursorIndexOfDateAbsolute = _cursor.getColumnIndexOrThrow("date_absolute");
          final List<SavedInfo> _result = new ArrayList<SavedInfo>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final SavedInfo _item;
            final long _tmpSong_id;
            _tmpSong_id = _cursor.getLong(_cursorIndexOfSongId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpImage_url;
            _tmpImage_url = _cursor.getString(_cursorIndexOfImageUrl);
            final int _tmpType;
            _tmpType = _cursor.getInt(_cursorIndexOfType);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpDate_absolute;
            _tmpDate_absolute = _cursor.getLong(_cursorIndexOfDateAbsolute);
            _item = new SavedInfo(_tmpSong_id,_tmpTitle,_tmpUrl,_tmpImage_url,_tmpType,_tmpDate,_tmpDate_absolute);
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
    }.getLiveData();
  }

  @Override
  public PollItem loadPollItem(long song_id) {
    final String _sql = "SELECT * FROM `PollItem` WHERE song_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, song_id);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfSongId = _cursor.getColumnIndexOrThrow("song_id");
      final int _cursorIndexOfTitle = _cursor.getColumnIndexOrThrow("title");
      final int _cursorIndexOfPollResponse = _cursor.getColumnIndexOrThrow("poll_response");
      final int _cursorIndexOfDate = _cursor.getColumnIndexOrThrow("date");
      final int _cursorIndexOfDateAbsolute = _cursor.getColumnIndexOrThrow("date_absolute");
      final PollItem _result;
      if(_cursor.moveToFirst()) {
        final long _tmpSong_id;
        _tmpSong_id = _cursor.getLong(_cursorIndexOfSongId);
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final String _tmpPoll_response;
        _tmpPoll_response = _cursor.getString(_cursorIndexOfPollResponse);
        final String _tmpDate;
        _tmpDate = _cursor.getString(_cursorIndexOfDate);
        final long _tmpDate_absolute;
        _tmpDate_absolute = _cursor.getLong(_cursorIndexOfDateAbsolute);
        _result = new PollItem(_tmpSong_id,_tmpTitle,_tmpPoll_response,_tmpDate,_tmpDate_absolute);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<PollItem>> loadPollItems() {
    final String _sql = "SELECT * FROM `PollItem`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new ComputableLiveData<List<PollItem>>() {
      private Observer _observer;

      @Override
      protected List<PollItem> compute() {
        if (_observer == null) {
          _observer = new Observer("PollItem") {
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
              invalidate();
            }
          };
          __db.getInvalidationTracker().addWeakObserver(_observer);
        }
        final Cursor _cursor = __db.query(_statement);
        try {
          final int _cursorIndexOfSongId = _cursor.getColumnIndexOrThrow("song_id");
          final int _cursorIndexOfTitle = _cursor.getColumnIndexOrThrow("title");
          final int _cursorIndexOfPollResponse = _cursor.getColumnIndexOrThrow("poll_response");
          final int _cursorIndexOfDate = _cursor.getColumnIndexOrThrow("date");
          final int _cursorIndexOfDateAbsolute = _cursor.getColumnIndexOrThrow("date_absolute");
          final List<PollItem> _result = new ArrayList<PollItem>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final PollItem _item;
            final long _tmpSong_id;
            _tmpSong_id = _cursor.getLong(_cursorIndexOfSongId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpPoll_response;
            _tmpPoll_response = _cursor.getString(_cursorIndexOfPollResponse);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpDate_absolute;
            _tmpDate_absolute = _cursor.getLong(_cursorIndexOfDateAbsolute);
            _item = new PollItem(_tmpSong_id,_tmpTitle,_tmpPoll_response,_tmpDate,_tmpDate_absolute);
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
    }.getLiveData();
  }

  @Override
  public AudioItem loadAudioItem(long id) {
    final String _sql = "SELECT * FROM `AudioItem` WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfAudioFilePath = _cursor.getColumnIndexOrThrow("audio_file_path");
      final int _cursorIndexOfUserResponse = _cursor.getColumnIndexOrThrow("user_response");
      final AudioItem _result;
      if(_cursor.moveToFirst()) {
        final String _tmpAudio_file_path;
        _tmpAudio_file_path = _cursor.getString(_cursorIndexOfAudioFilePath);
        final String _tmpUser_response;
        _tmpUser_response = _cursor.getString(_cursorIndexOfUserResponse);
        _result = new AudioItem(_tmpAudio_file_path,_tmpUser_response);
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<AudioItem> loadAudioItems() {
    final String _sql = "SELECT * FROM `AudioItem`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfAudioFilePath = _cursor.getColumnIndexOrThrow("audio_file_path");
      final int _cursorIndexOfUserResponse = _cursor.getColumnIndexOrThrow("user_response");
      final List<AudioItem> _result = new ArrayList<AudioItem>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final AudioItem _item;
        final String _tmpAudio_file_path;
        _tmpAudio_file_path = _cursor.getString(_cursorIndexOfAudioFilePath);
        final String _tmpUser_response;
        _tmpUser_response = _cursor.getString(_cursorIndexOfUserResponse);
        _item = new AudioItem(_tmpAudio_file_path,_tmpUser_response);
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
