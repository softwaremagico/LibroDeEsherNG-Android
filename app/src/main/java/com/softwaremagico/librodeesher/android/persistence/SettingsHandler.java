package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;

import com.softwaremagico.librodeesher.language.Translator;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Reads/writes the single-row {@link SettingsEntity} and exposes the persisted language.
 */
public final class SettingsHandler {

    private static volatile SettingsHandler instance;

    private final SettingsEntityDao settingsDao;

    private SettingsHandler(Context context) {
        this.settingsDao = AppDatabase.getInstance(context).getSettingsEntityDao();
    }

    public static SettingsHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (SettingsHandler.class) {
                if (instance == null) {
                    instance = new SettingsHandler(context);
                }
            }
        }
        return instance;
    }

    /** The stored settings, creating the default row on first access. */
    public SettingsEntity getSettings() {
        SettingsEntity settings = settingsDao.getSettings();
        if (settings == null) {
            settings = new SettingsEntity();
            settings.setLanguage(Translator.DEFAULT_LANGUAGE);
            final Timestamp now = new Timestamp(new Date().getTime());
            settings.setCreationTime(now);
            settings.setUpdateTime(now);
            settingsDao.insert(settings);
        }
        return settings;
    }

    public String getLanguage() {
        final String language = getSettings().getLanguage();
        return language == null ? Translator.DEFAULT_LANGUAGE : language;
    }

    public void setLanguage(String language) {
        final SettingsEntity settings = getSettings();
        settings.setLanguage(language);
        settings.setUpdateTime(new Timestamp(new Date().getTime()));
        settingsDao.update(settings);
    }
}
