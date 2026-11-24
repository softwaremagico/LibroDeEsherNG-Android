package com.softwaremagico.librodeesher.android.session;

import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

/**
 * Keeps the character currently being edited across fragments (mirrors the Advisor
 * {@code CharacterManager} session singleton).
 */
public final class CharacterManager {

    /** Sentinel returned when the current character has not been persisted yet. */
    public static final long NO_ID = -1;

    private static volatile CharacterManager instance;

    private CharacterPlayer selectedCharacter;

    private long selectedCharacterId = NO_ID;

    private OptionsHandler optionsHandler;

    private CharacterManager() {
        // Singleton.
    }

    public static CharacterManager getInstance() {
        if (instance == null) {
            synchronized (CharacterManager.class) {
                if (instance == null) {
                    instance = new CharacterManager();
                }
            }
        }
        return instance;
    }

    /** Called on startup so every newly generated character inherits the configured rules. */
    public void setOptionsHandler(OptionsHandler optionsHandler) {
        this.optionsHandler = optionsHandler;
    }

    /** Creates a character with the global rule/PDF options already applied. */
    public CharacterPlayer newCharacter() {
        final CharacterPlayer character = new CharacterPlayer();
        if (optionsHandler != null) {
            optionsHandler.applyTo(character);
        }
        return character;
    }

    /** The character being edited, creating an empty one on first access. */
    public CharacterPlayer getSelectedCharacter() {
        if (selectedCharacter == null) {
            selectedCharacter = newCharacter();
        }
        return selectedCharacter;
    }

    public void setSelectedCharacter(CharacterPlayer selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
        this.selectedCharacterId = NO_ID;
    }

    /** The database row id of the selected character, or {@link #NO_ID} when not persisted. */
    public long getSelectedCharacterId() {
        return selectedCharacterId;
    }

    public void setSelectedCharacterId(long selectedCharacterId) {
        this.selectedCharacterId = selectedCharacterId;
    }

    /** Replaces the current character with a brand new empty one. */
    public void resetCharacter() {
        this.selectedCharacter = newCharacter();
        this.selectedCharacterId = NO_ID;
    }

    public boolean hasCharacter() {
        return selectedCharacter != null;
    }
}
