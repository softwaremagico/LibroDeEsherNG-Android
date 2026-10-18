package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Shared Room entity columns (id + creation/update timestamps), matching the Advisor base entity.
 */
public abstract class BaseEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "creation_time", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(TimestampConverter.class)
    public Timestamp creationTime;

    @ColumnInfo(name = "update_time", defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(TimestampConverter.class)
    public Timestamp updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
