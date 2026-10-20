package com.softwaremagico.librodeesher.android;

import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CharacterManagerTest {
    @Test
    public void testGetInstance() {
        CharacterManager instance = CharacterManager.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void testResetCharacter() {
        CharacterManager manager = CharacterManager.getInstance();
        manager.resetCharacter();
        CharacterPlayer character = manager.getSelectedCharacter();
        assertNotNull(character);
    }

    @Test
    public void testGetSelectedCharacter() {
        CharacterManager manager = CharacterManager.getInstance();
        CharacterPlayer character = manager.getSelectedCharacter();
        assertNotNull(character);
    }
}
