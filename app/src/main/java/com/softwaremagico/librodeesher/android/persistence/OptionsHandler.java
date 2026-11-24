package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.file.ModuleManager;

import java.util.HashSet;
import java.util.Set;

/** Persists the rule/PDF options that used to live in the legacy OptionsWindow. */
public final class OptionsHandler {

    private static final String PREFERENCES = "LibroDeEsherOptions";
    private static final String MODULES = "disabledModules";
    private static final String PDF_MODE = "pdfMode";
    private static final String DEBUG = "debugMode";

    private static volatile OptionsHandler instance;

    private final SharedPreferences preferences;

    private OptionsHandler(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static OptionsHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (OptionsHandler.class) {
                if (instance == null) {
                    instance = new OptionsHandler(context);
                }
            }
        }
        return instance;
    }

    public boolean isFirearmsAllowed() {
        return preferences.getBoolean("firearms", false);
    }

    public boolean isChiAllowed() {
        return preferences.getBoolean("chi", false);
    }

    public boolean isOtherRealmTrainingAllowed() {
        return preferences.getBoolean("otherRealmTraining", false);
    }

    public boolean isMagicAllowed() {
        return preferences.getBoolean("magic", true);
    }

    public boolean isDarkSpellsAsBasic() {
        return preferences.getBoolean("darkSpells", false);
    }

    public boolean isHandwritingFont() {
        return preferences.getBoolean("handwriting", true);
    }

    public boolean isSortPdfSkills() {
        return preferences.getBoolean("sortPdfSkills", false);
    }

    public boolean isPerksCostBackgroundPoints() {
        return preferences.getBoolean("perksCostBackgroundPoints", true);
    }

    public int getCategoryMaxCost() {
        return preferences.getInt("categoryMaxCost", CharacterPlayer.CATEGORY_MAX_COST);
    }

    public int getPdfMode() {
        return preferences.getInt(PDF_MODE, 0);
    }

    public boolean isDebugMode() {
        return preferences.getBoolean(DEBUG, false);
    }

    public Set<String> getDisabledModules() {
        return new HashSet<>(preferences.getStringSet(MODULES, new HashSet<>()));
    }

    public void store(boolean firearms, boolean chi, boolean otherRealmTraining, boolean magic,
                      boolean darkSpells, boolean handwriting, boolean sortPdfSkills,
                      boolean perksCostBackgroundPoints, int categoryMaxCost, int pdfMode,
                      boolean debugMode, Set<String> disabledModules) {
        final int safeCost = Math.max(5, Math.min(999, categoryMaxCost));
        preferences.edit()
                .putBoolean("firearms", firearms)
                .putBoolean("chi", chi)
                .putBoolean("otherRealmTraining", otherRealmTraining)
                .putBoolean("magic", magic)
                .putBoolean("darkSpells", darkSpells)
                .putBoolean("handwriting", handwriting)
                .putBoolean("sortPdfSkills", sortPdfSkills)
                .putBoolean("perksCostBackgroundPoints", perksCostBackgroundPoints)
                .putInt("categoryMaxCost", safeCost)
                .putInt(PDF_MODE, Math.max(0, Math.min(3, pdfMode)))
                .putBoolean(DEBUG, debugMode)
                .putStringSet(MODULES, new HashSet<>(disabledModules))
                .apply();
        applyModules();
    }

    /** Applies the stored module selection to the rules engine on startup. */
    public void applyModules() {
        final Set<String> disabled = getDisabledModules();
        for (final String module : ModuleManager.getAllModules()) {
            if (disabled.contains(module)) {
                ModuleManager.disableModule(module);
            } else {
                ModuleManager.enableModule(module);
            }
        }
        ModuleManager.resetModules();
    }

    /** Applies the stored options to a character, including one created by another screen. */
    public void applyTo(CharacterPlayer character) {
        if (character == null) {
            return;
        }
        try {
            character.setFirearmsAllowed(isFirearmsAllowed());
        } catch (Exception exception) {
            AppLog.error("Cannot apply firearms option", exception);
        }
        character.setChiPowersAllowed(isChiAllowed());
        character.setOtherRealmTrainingSpellsAllowed(isOtherRealmTrainingAllowed());
        character.setMagicAllowed(isMagicAllowed());
        character.setDarkSpellsAsBasicListsAllowed(isDarkSpellsAsBasic());
        character.setHandWritingFont(isHandwritingFont());
        character.setSortPdfSkills(isSortPdfSkills());
        character.setPerksCostBackgroundPoints(isPerksCostBackgroundPoints());
        character.setCategoryMaxCost(getCategoryMaxCost());
    }
}
