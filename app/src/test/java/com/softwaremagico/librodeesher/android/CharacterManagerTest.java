package com.softwaremagico.librodeesher.android;

import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testCloneSelectedCharacter() throws Exception {
        CharacterManager manager = CharacterManager.getInstance();
        CharacterPlayer original = manager.newCharacter();
        original.setName("Original");
        original.setRaceId("laan");
        manager.setSelectedCharacter(original);

        manager.cloneSelectedCharacter();
        CharacterPlayer clone = manager.getSelectedCharacter();

        assertNotSame(original, clone);
        assertEquals("Original", clone.getName());
        assertTrue(manager.getOpenCharacters().size() >= 2);
        assertFalse(manager.isDirty());
    }

    @Test
    public void testSwitchCloseAndReopenSessionCharacters() {
        CharacterManager manager = CharacterManager.getInstance();
        CharacterPlayer first = manager.newCharacter();
        first.setName("Primero");
        CharacterPlayer second = manager.newCharacter();
        second.setName("Segundo");
        manager.openCharacter(first, 10L);
        manager.openCharacter(second, 20L);

        assertEquals(20L, manager.getSelectedCharacterId());
        assertTrue(manager.hasCharacter());
        int firstIndex = -1;
        int secondIndex = -1;
        for (int i = 0; i < manager.getOpenCharacters().size(); i++) {
            if (manager.getOpenCharacters().get(i).getCharacter() == first) {
                firstIndex = i;
            }
            if (manager.getOpenCharacters().get(i).getCharacter() == second) {
                secondIndex = i;
            }
        }
        manager.switchTo(firstIndex);
        assertEquals("Primero", manager.getSelectedCharacter().getName());
        assertEquals(10L, manager.getSelectedCharacterId());
        manager.switchTo(secondIndex);
        assertEquals("Segundo", manager.getSelectedCharacter().getName());
        manager.switchTo(10L);
        assertEquals("Primero", manager.getSelectedCharacter().getName());
        manager.switchTo(999L);
        assertEquals("Primero", manager.getSelectedCharacter().getName());

        manager.setSelectedCharacterId(99L);
        assertEquals(99L, manager.getSelectedCharacterId());
        manager.setSelectedCharacter(first);
        manager.closeSelected();
        for (CharacterManager.OpenCharacter open : manager.getOpenCharacters()) {
            assertNotSame(first, open.getCharacter());
        }
        manager.resetCharacter();
        assertEquals(CharacterManager.NO_ID, manager.getSelectedCharacterId());
    }

    @Test
    public void testUnsavedChangesTracking() {
        CharacterManager manager = CharacterManager.getInstance();
        CharacterPlayer character = manager.newCharacter();
        manager.setSelectedCharacter(character);
        assertFalse(manager.isDirty());

        character.setName("Personaje modificado");
        assertTrue(manager.isDirty());

        manager.markClean();
        assertFalse(manager.isDirty());
    }
}
