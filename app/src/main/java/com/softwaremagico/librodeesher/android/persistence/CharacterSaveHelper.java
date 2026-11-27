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

    /** Saves a named copy without changing which character is currently selected. */
    public static boolean saveAs(Context context, String name) {
        final CharacterManager manager = CharacterManager.getInstance();
        final CharacterPlayer selected = manager.getSelectedCharacter();
        if (selected == null || selected.getRaceId() == null) {
            Toast.makeText(context, R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return false;
        }
        final String newName = name == null || name.trim().isEmpty() ? selected.getName() : name.trim();
        try {
            final CharacterPlayer copy = CharacterFileHelper.importCharacterFromJson(
                    CharacterFileHelper.toJson(selected));
            copy.setName(newName);
            final CharacterEntity entity = new CharacterEntity(copy);
            AppDatabase.getInstance(context).getCharacterEntityDao().insert(entity);
            Toast.makeText(context, context.getString(R.string.sheet_saved_as, newName), Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception exception) {
            AppLog.error("Cannot save character as " + newName, exception);
            Toast.makeText(context, R.string.sheet_save_error, Toast.LENGTH_SHORT).show();
            return false;
        }
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
