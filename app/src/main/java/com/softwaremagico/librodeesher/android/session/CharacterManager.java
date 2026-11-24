package com.softwaremagico.librodeesher.android.session;

import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the characters currently open across fragments (mirrors the Advisor
 * {@code CharacterManager} session singleton).
 */
public final class CharacterManager {

    /** Sentinel returned when the current character has not been persisted yet. */
    public static final long NO_ID = -1;

    /** One character open in this session, with its database id when it has been saved. */
    public static final class OpenCharacter {
        private final CharacterPlayer character;
        private long id;

        private OpenCharacter(CharacterPlayer character, long id) {
            this.character = character;
            this.id = id;
        }

        public CharacterPlayer getCharacter() {
            return character;
        }

        public long getId() {
            return id;
        }

        public String displayName() {
            final String name = character.getName();
            return name == null || name.trim().isEmpty() ? "Sin nombre" : name;
        }
    }

    private static volatile CharacterManager instance;

    private final List<OpenCharacter> openCharacters = new ArrayList<>();
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
            openCharacter(newCharacter(), NO_ID);
        }
        return selectedCharacter;
    }

    public void setSelectedCharacter(CharacterPlayer selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
        this.selectedCharacterId = NO_ID;
        boolean known = false;
        for (final OpenCharacter open : openCharacters) {
            if (open.getCharacter() == selectedCharacter) {
                known = true;
                break;
            }
        }
        if (!known) {
            openCharacters.add(new OpenCharacter(selectedCharacter, NO_ID));
        }
    }

    /** Adds a character to the session (or replaces the same saved one) and selects it. */
    public void openCharacter(CharacterPlayer character, long id) {
        if (id != NO_ID) {
            openCharacters.removeIf(open -> open.getId() == id);
        }
        openCharacters.add(new OpenCharacter(character, id));
        selectedCharacter = character;
        selectedCharacterId = id;
    }

    /** Switches the selection to one of the already open characters. */
    public void switchTo(int index) {
        if (index >= 0 && index < openCharacters.size()) {
            selectedCharacter = openCharacters.get(index).getCharacter();
            selectedCharacterId = openCharacters.get(index).getId();
        }
    }

    public void switchTo(long id) {
        for (final OpenCharacter open : openCharacters) {
            if (open.getId() == id) {
                selectedCharacter = open.getCharacter();
                selectedCharacterId = id;
                return;
            }
        }
    }

    public List<OpenCharacter> getOpenCharacters() {
        return Collections.unmodifiableList(openCharacters);
    }

    /** Closes the selected character and selects another open one, or a fresh empty one. */
    public void closeSelected() {
        openCharacters.removeIf(open -> open.getCharacter() == selectedCharacter);
        if (openCharacters.isEmpty()) {
            resetCharacter();
        } else {
            selectedCharacter = openCharacters.get(0).getCharacter();
            selectedCharacterId = openCharacters.get(0).getId();
        }
    }

    /** The database row id of the selected character, or {@link #NO_ID} when not persisted. */
    public long getSelectedCharacterId() {
        return selectedCharacterId;
    }

    public void setSelectedCharacterId(long selectedCharacterId) {
        this.selectedCharacterId = selectedCharacterId;
        for (final OpenCharacter open : openCharacters) {
            if (open.getCharacter() == selectedCharacter) {
                open.id = selectedCharacterId;
                break;
            }
        }
    }

    /** Replaces the current character with a brand new empty one. */
    public void resetCharacter() {
        openCharacter(newCharacter(), NO_ID);
    }

    public boolean hasCharacter() {
        return selectedCharacter != null;
    }
}
