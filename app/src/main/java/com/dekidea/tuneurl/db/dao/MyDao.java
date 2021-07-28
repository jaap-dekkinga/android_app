package com.dekidea.tuneurl.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.dekidea.tuneurl.db.entity.AudioItem;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.db.entity.PollItem;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MyDao {

    @Insert(onConflict = REPLACE)
    void saveSavedInfo(SavedInfo item);

    @Query("DELETE FROM SavedInfo WHERE date_absolute < :date_absolute")
    void deleteSavedInfo(long date_absolute);

    @Query("SELECT * FROM SavedInfo WHERE song_id = :song_id")
    SavedInfo loadSavedInfo(long song_id);

    @Query("SELECT * FROM SavedInfo")
    LiveData<List<SavedInfo>> loadSavedInfo();

    @Query("UPDATE SavedInfo SET type= :type WHERE song_id = :id")
    void updateSavedInfo(int type, long id);

    @Insert(onConflict = REPLACE)
    void savePollItem(PollItem item);

    @Query("SELECT * FROM `PollItem` WHERE song_id = :song_id")
    PollItem loadPollItem(long song_id);

    @Query("SELECT * FROM `PollItem`")
    LiveData<List<PollItem>> loadPollItems();

    @Insert(onConflict = REPLACE)
    void saveAudioItem(AudioItem item);

    @Delete
    void deleteAudioItem(AudioItem item);

    @Query("SELECT * FROM `AudioItem` WHERE id = :id")
    AudioItem loadAudioItem(long id);

    @Query("SELECT * FROM `AudioItem`")
    List<AudioItem> loadAudioItems();


}
