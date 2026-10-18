package com.softwaremagico.librodeesher.android.core;

import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
        // Utility class.
    }

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(timestamp.getTime()));
    }
}
