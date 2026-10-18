package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class CharacterEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(CharacterEntity entity);

    @Update
    public abstract void update(CharacterEntity entity);

    @Delete
    public abstract void delete(CharacterEntity entity);

    @Query("SELECT COUNT(*) FROM " + CharacterEntity.CHARACTER_PLAYER_TABLE)
    public abstract long getRowCount();

    @Query("SELECT * FROM " + CharacterEntity.CHARACTER_PLAYER_TABLE + " ORDER BY update_time DESC")
    public abstract List<CharacterEntity> getAll();

    @Query("SELECT * FROM " + CharacterEntity.CHARACTER_PLAYER_TABLE + " WHERE id=:id")
    public abstract CharacterEntity get(long id);
}
