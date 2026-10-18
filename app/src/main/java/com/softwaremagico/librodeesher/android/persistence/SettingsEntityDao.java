package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class SettingsEntityDao {

    @Query("SELECT * FROM " + SettingsEntity.SETTINGS_TABLE + " LIMIT 1")
    public abstract SettingsEntity getSettings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(SettingsEntity entity);

    @Update
    public abstract void update(SettingsEntity entity);
}
