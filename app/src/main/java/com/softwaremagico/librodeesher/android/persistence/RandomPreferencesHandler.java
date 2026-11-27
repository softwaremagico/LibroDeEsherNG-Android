package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.perk.SelectedPerk;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stores the random-generation preferences that used to live in the legacy RandomWindow. */
public final class RandomPreferencesHandler {

    private static final String PREFERENCES = "LibroDeEsherRandomPreferences";
    private static final String SPECIALIZATION = "specializationLevel";
    private static final String FINAL_LEVEL = "finalLevel";
    private static final String PERKS_ENABLED = "perksEnabled";
    private static final String CATEGORY_RANKS = "categoryRanks";
    private static final String SKILL_RANKS = "skillRanks";
    private static final String TRAININGS = "trainings";
    private static final String SUGGESTED_PERKS = "suggestedPerks";
    private static final String RACE_ID = "raceId";
    private static final String CULTURE_ID = "cultureId";
    private static final String PROFESSION_ID = "professionId";
    private static final String CHARACTERISTIC_RANKS = "characteristicRanks";

    private static volatile RandomPreferencesHandler instance;

    private final SharedPreferences preferences;

    private RandomPreferencesHandler(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static RandomPreferencesHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (RandomPreferencesHandler.class) {
                if (instance == null) {
                    instance = new RandomPreferencesHandler(context);
                }
            }
        }
        return instance;
    }

    /** Specialization level in the [-3..3] range expected by the rules engine (0 = random). */
    public int getSpecializationLevel() {
        return Math.max(-3, Math.min(3, preferences.getInt(SPECIALIZATION, 0)));
    }

    public void setSpecializationLevel(int specializationLevel) {
        preferences.edit()
                .putInt(SPECIALIZATION, Math.max(-3, Math.min(3, specializationLevel)))
                .apply();
    }

    public int getFinalLevel() {
        return Math.max(1, Math.min(20, preferences.getInt(FINAL_LEVEL, 1)));
    }

    public void setFinalLevel(int finalLevel) {
        preferences.edit().putInt(FINAL_LEVEL, Math.max(1, Math.min(20, finalLevel))).apply();
    }

    public boolean isPerksEnabled() {
        return preferences.getBoolean(PERKS_ENABLED, true);
    }

    public void setPerksEnabled(boolean enabled) {
        preferences.edit().putBoolean(PERKS_ENABLED, enabled).apply();
    }

    public Map<String, Integer> getCategoryRanks() {
        return parseRanks(preferences.getString(CATEGORY_RANKS, ""));
    }

    public void setCategoryRanks(Map<String, Integer> ranks) {
        preferences.edit().putString(CATEGORY_RANKS, formatRanks(ranks)).apply();
    }

    public Map<String, Integer> getSkillRanks() {
        return parseRanks(preferences.getString(SKILL_RANKS, ""));
    }

    public void setSkillRanks(Map<String, Integer> ranks) {
        preferences.edit().putString(SKILL_RANKS, formatRanks(ranks)).apply();
    }

    public List<String> getTrainings() {
        return parseList(preferences.getString(TRAININGS, ""));
    }

    public void setTrainings(List<String> trainings) {
        preferences.edit().putString(TRAININGS, join(trainings)).apply();
    }

    public List<String> getSuggestedPerks() {
        return parseList(preferences.getString(SUGGESTED_PERKS, ""));
    }

    public void setSuggestedPerks(List<String> perks) {
        preferences.edit().putString(SUGGESTED_PERKS, join(perks)).apply();
    }

    /**
     * Preferred race id. An empty value (the default) means the generation inherits the race
     * selected in the session, exactly as the legacy "Aleatorio" action did.
     */
    public String getRaceId() {
        return preferences.getString(RACE_ID, "");
    }

    public void setRaceId(String raceId) {
        preferences.edit().putString(RACE_ID, valueOrEmpty(raceId)).apply();
    }

    /** Preferred culture id; empty inherits the culture selected in the session. */
    public String getCultureId() {
        return preferences.getString(CULTURE_ID, "");
    }

    public void setCultureId(String cultureId) {
        preferences.edit().putString(CULTURE_ID, valueOrEmpty(cultureId)).apply();
    }

    /** Preferred profession id; empty inherits the profession selected in the session. */
    public String getProfessionId() {
        return preferences.getString(PROFESSION_ID, "");
    }

    public void setProfessionId(String professionId) {
        preferences.edit().putString(PROFESSION_ID, valueOrEmpty(professionId)).apply();
    }

    /** Desired characteristic temporal ranks as {@code code:rank}, e.g. {@code AG:90,ST:85}. */
    public Map<String, Integer> getCharacteristicRanks() {
        return parseRanks(preferences.getString(CHARACTERISTIC_RANKS, ""));
    }

    public void setCharacteristicRanks(Map<String, Integer> ranks) {
        preferences.edit().putString(CHARACTERISTIC_RANKS, formatRanks(ranks)).apply();
    }

    /**
     * Seeds the configured race, culture and profession before random generation. Ids that are no
     * longer present in the enabled modules are ignored; blank preferences leave the values
     * inherited from the current session untouched.
     */
    public void applyOrigin(CharacterPlayer character) {
        if (character == null) {
            return;
        }
        final RulesCatalog catalog = RulesCatalog.getInstance();
        final String raceId = getRaceId().trim();
        if (!raceId.isEmpty() && exists(catalog, raceId, 4)) {
            character.setRaceId(raceId);
        }
        final String cultureId = getCultureId().trim();
        if (!cultureId.isEmpty() && exists(catalog, cultureId, 5)) {
            character.setCultureId(cultureId);
        }
        final String professionId = getProfessionId().trim();
        if (!professionId.isEmpty() && exists(catalog, professionId, 6)) {
            character.setProfessionId(professionId);
        }
    }

    /** Applies every persisted preference to a fresh generator before calling createRandomValues. */
    public void configure(RandomCharacterPlayer generator) {
        generator.setSpecializationLevel(getSpecializationLevel());
        final RulesCatalog catalog = RulesCatalog.getInstance();
        for (final Map.Entry<String, Integer> entry : getCategoryRanks().entrySet()) {
            if (exists(catalog, entry.getKey(), 0)) {
                generator.setSuggestedCategoryRanks(entry.getKey(), entry.getValue());
            }
        }
        for (final Map.Entry<String, Integer> entry : getSkillRanks().entrySet()) {
            if (exists(catalog, entry.getKey(), 1)) {
                generator.setSuggestedSkillRanks(entry.getKey(), entry.getValue());
            }
        }
        final List<String> trainings = new ArrayList<>();
        for (final String id : getTrainings()) {
            if (exists(catalog, id, 2)) {
                trainings.add(id);
            }
        }
        generator.setSuggestedTrainings(trainings);
        final List<String> perks = new ArrayList<>();
        for (final String id : getSuggestedPerks()) {
            if (exists(catalog, id, 3)) {
                perks.add(id);
            }
        }
        generator.setSuggestedPerks(perks);
    }

    /** Ignores stale ids that are no longer present in the rules catalog. */
    private static boolean exists(RulesCatalog catalog, String id, int kind) {
        try {
            switch (kind) {
                case 0:
                    return catalog.getCategory(id) != null;
                case 1:
                    return catalog.getSkill(id) != null;
                case 2:
                    return catalog.getTraining(id) != null;
                case 3:
                    return catalog.getPerk(id) != null;
                case 4:
                    return catalog.getRace(id) != null;
                case 5:
                    return catalog.getCulture(id) != null;
                default:
                    return catalog.getProfession(id) != null;
            }
        } catch (InvalidXmlElementException exception) {
            return false;
        }
    }

    /** Applies post-generation preferences that cannot be expressed in the generator itself. */
    public void afterGeneration(CharacterPlayer character) {
        if (!isPerksEnabled()) {
            for (final SelectedPerk perk : new ArrayList<>(character.getSelectedPerks())) {
                character.setPerkAsRandom(perk.getPerkId(), false);
                character.removePerk(perk.getPerkId());
            }
        }
        for (final Map.Entry<String, Integer> entry : getCharacteristicRanks().entrySet()) {
            final CharacteristicAbbreviation abbreviation = characteristic(entry.getKey());
            if (abbreviation != null) {
                character.setCharacteristicTemporalValue(abbreviation, entry.getValue());
            }
        }
    }

    /** Accepts the English code printed on the sheet (e.g. {@code AG}) and ignores unknown ones. */
    private static CharacteristicAbbreviation characteristic(String code) {
        if (code == null) {
            return null;
        }
        for (final CharacteristicAbbreviation abbreviation : CharacteristicAbbreviation.values()) {
            if (abbreviation != CharacteristicAbbreviation.NONE
                    && abbreviation != CharacteristicAbbreviation.REALM_OF_MAGIC
                    && abbreviation.getCode().equalsIgnoreCase(code.trim())) {
                return abbreviation;
            }
        }
        return null;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static Map<String, Integer> parseRanks(String raw) {
        final Map<String, Integer> ranks = new LinkedHashMap<>();
        if (raw == null || raw.trim().isEmpty()) {
            return ranks;
        }
        for (final String token : raw.split(",")) {
            final String[] parts = token.trim().split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            try {
                ranks.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException ignored) {
                // Ignore malformed user input.
            }
        }
        return ranks;
    }

    private static String formatRanks(Map<String, Integer> ranks) {
        final StringBuilder builder = new StringBuilder();
        if (ranks != null) {
            for (final Map.Entry<String, Integer> entry : ranks.entrySet()) {
                if (builder.length() > 0) {
                    builder.append(',');
                }
                builder.append(entry.getKey()).append(':').append(entry.getValue());
            }
        }
        return builder.toString();
    }

    private static List<String> parseList(String raw) {
        final List<String> values = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return values;
        }
        for (final String token : raw.split(",")) {
            final String value = token.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private static String join(List<String> values) {
        return values == null ? "" : String.join(",", values);
    }
}
