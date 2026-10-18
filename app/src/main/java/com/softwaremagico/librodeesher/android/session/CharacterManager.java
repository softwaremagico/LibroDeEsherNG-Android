package com.softwaremagico.librodeesher.android.session;

import com.softwaremagico.librodeesher.character.CharacterPlayer;

/**
 * Keeps the character currently being edited across fragments (mirrors the Advisor
 * {@code CharacterManager} session singleton).
 */
public final class CharacterManager {

    private static volatile CharacterManager instance;

    private CharacterPlayer selectedCharacter;

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
    }

    /** Replaces the current character with a brand new empty one. */
    public void resetCharacter() {
        this.selectedCharacter = new CharacterPlayer();
    }

    public boolean hasCharacter() {
        return selectedCharacter != null;
    }
}
