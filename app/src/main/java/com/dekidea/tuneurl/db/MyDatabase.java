package com.dekidea.tuneurl.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.dekidea.tuneurl.db.converter.DateConverter;
import com.dekidea.tuneurl.db.dao.MyDao;
import com.dekidea.tuneurl.db.entity.AudioItem;
import com.dekidea.tuneurl.db.entity.SavedInfo;
import com.dekidea.tuneurl.db.entity.PollItem;


@Database(entities = {SavedInfo.class, PollItem.class, AudioItem.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class MyDatabase extends RoomDatabase {

    // --- SINGLETON ---
    private static volatile MyDatabase INSTANCE;

    // --- DAO ---
    public abstract MyDao dao();
}
