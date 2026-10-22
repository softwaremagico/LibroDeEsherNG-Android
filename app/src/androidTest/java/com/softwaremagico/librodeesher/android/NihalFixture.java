package com.softwaremagico.librodeesher.android;

import android.content.Context;

import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntityDao;

import java.util.List;

final class NihalFixture {

    static final String CHARACTER_NAME = "Nihal Dragoniana";

    private NihalFixture() {
    }

    static CharacterEntityDao dao(Context context) {
        return AppDatabase.getInstance(context).getCharacterEntityDao();
    }

    static void clearSavedCharacters(Context context) {
        final CharacterEntityDao dao = dao(context);
        for (final CharacterEntity entity : dao.getAll()) {
            dao.delete(entity);
        }
    }

    static CharacterEntity insertReferenceCharacter(Context context) throws Exception {
        // Keep for backward compatibility if needed, but tests should use UI only
        clearSavedCharacters(context);
        // Can't use library directly? We'll handle differently
        return null;
    }
}
