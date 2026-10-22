package com.softwaremagico.librodeesher.android;

import android.content.Context;

import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntityDao;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.persistence.LegacyCharacterJsonImporter;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Shared helpers to bring the reference Nihal level nine save into the instrumented tests.
 *
 * <p>The reference is the legacy 2.0.0 snapshot packaged with the rules jar, so on the device it is
 * read through the class loader and imported with the same legacy importer the app uses to read
 * its own saves.</p>
 */
final class NihalFixture {

    static final String REFERENCE_SNAPSHOT = "/Nihal_level9_2_0_0.json";

    static final String CHARACTER_NAME = "Nihal Dragoniana";

    private NihalFixture() {
        // Utility class.
    }

    /** The raw legacy snapshot, as stored in the rules jar. */
    static String referenceJson() throws IOException {
        try (final InputStream in = NihalFixture.class.getResourceAsStream(REFERENCE_SNAPSHOT)) {
            if (in == null) {
                throw new IOException("Missing reference snapshot " + REFERENCE_SNAPSHOT);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** The reference character, imported through the legacy importer. */
    static CharacterPlayer referenceCharacter() throws Exception {
        RulesCatalog.getInstance();
        return CharacterDataMapper.toCharacter(LegacyCharacterJsonImporter.importLegacyJson(referenceJson()));
    }

    /** The export the app must produce for the reference character. */
    static String expectedExport() throws Exception {
        RulesCatalog.getInstance();
        return CharacterJsonManager.toJson(LegacyCharacterJsonImporter.importLegacyJson(referenceJson()));
    }

    static CharacterEntityDao dao(Context context) {
        return AppDatabase.getInstance(context).getCharacterEntityDao();
    }

    /** Removes every saved character, so the load dialogs only show the seeded one. */
    static void clearSavedCharacters(Context context) {
        final CharacterEntityDao dao = dao(context);
        for (final CharacterEntity entity : dao.getAll()) {
            dao.delete(entity);
        }
    }

    /** Saves the reference character as the only character in the database. */
    static CharacterEntity insertReferenceCharacter(Context context) throws Exception {
        clearSavedCharacters(context);
        final CharacterEntity entity = new CharacterEntity(referenceCharacter());
        final long id = dao(context).insert(entity);
        return dao(context).get(id);
    }
}
