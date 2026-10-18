package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

public final class TimestampConverter {

    private TimestampConverter() {
        // Utility class.
    }

    @TypeConverter
    public static Timestamp toTimestamp(Long timestamp) {
        return timestamp == null ? null : new Timestamp(timestamp);
    }

    @TypeConverter
    public static Long toLong(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.getTime();
    }
}
