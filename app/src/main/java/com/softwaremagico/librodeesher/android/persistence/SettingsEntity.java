package com.softwaremagico.librodeesher.android.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Global application settings. Kept minimal for the scaffold; more options can be added as new
 * columns with a Room migration.
 */
@Entity(tableName = SettingsEntity.SETTINGS_TABLE)
public class SettingsEntity extends BaseEntity {
    public static final String SETTINGS_TABLE = "settings";

    @ColumnInfo(name = "language")
    public String language;

    @ColumnInfo(name = "debug_mode_enabled")
    public boolean debugModeEnabled;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }

    public void setDebugModeEnabled(boolean debugModeEnabled) {
        this.debugModeEnabled = debugModeEnabled;
    }
}
