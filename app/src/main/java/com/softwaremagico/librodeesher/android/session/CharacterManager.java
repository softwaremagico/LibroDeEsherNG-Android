package com.softwaremagico.librodeesher.android.session;

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

    /** The character being edited, creating an empty one on first access. */
    public CharacterPlayer getSelectedCharacter() {
        if (selectedCharacter == null) {
            selectedCharacter = new CharacterPlayer();
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
        this.selectedCharacter = new CharacterPlayer();
        this.selectedCharacterId = NO_ID;
    }

    public boolean hasCharacter() {
        return selectedCharacter != null;
    }
}
