package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;
import android.widget.Toast;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

/** Saves the character currently in session. Shared by the sheet and the global menu. */
public final class CharacterSaveHelper {

    private CharacterSaveHelper() {
    }

    public static boolean save(Context context) {
        final CharacterManager manager = CharacterManager.getInstance();
        final CharacterPlayer character = manager.getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(context, R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            final CharacterEntity entity = new CharacterEntity(character);
            if (manager.getSelectedCharacterId() != CharacterManager.NO_ID) {
                entity.setId(manager.getSelectedCharacterId());
            }
            final long id = AppDatabase.getInstance(context).getCharacterEntityDao().insert(entity);
            manager.setSelectedCharacterId(id);
            manager.markClean();
            Toast.makeText(context, R.string.sheet_saved, Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception exception) {
            AppLog.error("Cannot save character", exception);
            Toast.makeText(context, R.string.sheet_save_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
