package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;
import android.net.Uri;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicRoll;
import com.softwaremagico.librodeesher.persistence.CharacterData;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.persistence.LegacyCharacterJsonImporter;
import com.softwaremagico.librodeesher.persistence.LevelJsonManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Imports and exports characters and single levels using the NG/Legacy JSON formats. */
public final class CharacterFileHelper {

    private CharacterFileHelper() {
    }

    public static String toJson(CharacterPlayer character) {
        return CharacterJsonManager.toJson(CharacterDataMapper.toData(character));
    }

    public static void exportCharacter(Context context, Uri uri, CharacterPlayer character) throws IOException {
        write(context, uri, toJson(character));
    }

    /** Accepts both NG JSON exports and legacy {@code .rlm} JSON snapshots. */
    public static CharacterPlayer importCharacter(Context context, Uri uri) throws Exception {
        return importCharacterFromJson(read(context, uri));
    }

    public static CharacterPlayer importCharacterFromJson(String json) throws Exception {
        CharacterData data = CharacterJsonManager.fromJson(json);
        if (data == null || data.getRaceId() == null) {
            data = LegacyCharacterJsonImporter.importLegacyJson(json);
        }
        return CharacterDataMapper.toCharacter(data);
    }

    public static void exportLevel(Context context, Uri uri, CharacterPlayer character) throws IOException {
        write(context, uri, LevelJsonManager.toJson(character));
    }

    /**
     * Imports a shared level onto the selected character. The file is validated against a snapshot
     * of the character one level behind, as in the original "secure level import" workflow.
     */
    public static void importLevel(Context context, Uri uri, CharacterPlayer character) throws Exception {
        if (character.getLevel() < 2) {
            throw new IllegalStateException("A level can only be imported onto a level 2+ character.");
        }
        final CharacterData previousData = CharacterDataMapper.toDataAtLevel(character, character.getLevel() - 1);
        final CharacterPlayer previous = CharacterDataMapper.toCharacter(previousData);
        final com.softwaremagico.librodeesher.level.LevelUp imported =
                LevelJsonManager.fromJson(previous, read(context, uri));
        final com.softwaremagico.librodeesher.level.LevelUp replayed = character.increaseLevel();
        replayed.setCategoryRanks(imported.getCategoryRanks());
        replayed.setSkillRanks(imported.getSkillRanks());
        replayed.setTrainingSkillRanks(imported.getTrainingSkillRanks());
        replayed.setSpellListRanks(imported.getSpellListRanks());
        replayed.setSpellsUpdated(imported.getSpellsUpdated());
        replayed.setGeneralizedSkills(imported.getGeneralizedSkills());
        replayed.setSkillSpecializations(imported.getSkillSpecializations());
        replayed.setFavouriteSkills(imported.getFavouriteSkills());
        replayed.setFavouriteSpellLists(imported.getFavouriteSpellLists());
        replayed.setTrainings(imported.getTrainings());
        replayed.setAge(imported.getAge());
        for (final CharacteristicRoll roll : imported.getCharacteristicUpdates()) {
            replayed.addCharacteristicUpdate(roll.getCharacteristicAbbreviation(),
                    roll.getCharacteristicTemporalValue(), roll.getCharacteristicPotentialValue(), roll.getRoll());
        }
        imported.getAgeModifications().forEach(replayed::addAgeModification);
    }

    private static void write(Context context, Uri uri, String content) throws IOException {
        try (OutputStream stream = context.getContentResolver().openOutputStream(uri)) {
            if (stream == null) {
                throw new IOException("Cannot open " + uri);
            }
            stream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String read(Context context, Uri uri) throws IOException {
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null) {
                throw new IOException("Cannot open " + uri);
            }
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
