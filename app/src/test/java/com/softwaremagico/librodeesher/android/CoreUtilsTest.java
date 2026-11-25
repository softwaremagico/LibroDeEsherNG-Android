package com.softwaremagico.librodeesher.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.core.CharacterSummaryBuilder;
import com.softwaremagico.librodeesher.android.core.CharacterValidator;
import com.softwaremagico.librodeesher.android.core.DateUtils;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import org.junit.Test;

import java.sql.Timestamp;

/** JVM coverage for the pure helpers used by the Android UI. */
public class CoreUtilsTest {

    @Test
    public void timestampFormattingHandlesNullAndValues() {
        assertEquals("", DateUtils.formatTimestamp(null));
        assertEquals("2026-11-24 10:11:12",
                DateUtils.formatTimestamp(Timestamp.valueOf("2026-11-24 10:11:12")));
    }

    @Test
    public void validatorRejectsIncompleteCharacters() {
        assertEquals(R.string.sheet_no_character, CharacterValidator.problem(null));
        assertEquals(R.string.sheet_no_character, CharacterValidator.problem(new CharacterPlayer()));
    }

    @Test
    public void validatorRequiresConfirmedCharacteristicsOnGeneratedCharacters() throws Exception {
        final CharacterPlayer character = new CharacterPlayer();
        character.setRaceId("laan");
        character.increaseLevel();
        assertEquals(R.string.character_confirm_characteristics_first,
                CharacterValidator.problem(character));
        character.setCharacteristicsAsConfirmed();
        assertEquals(0, CharacterValidator.problem(character));
    }

    @Test
    public void summaryHandlesEmptyAndPopulatedCharacters() {
        assertEquals("", CharacterSummaryBuilder.build(null));

        final CharacterPlayer character = new CharacterPlayer();
        final String summary = CharacterSummaryBuilder.build(character);
        assertTrue(summary.contains("Nombre: -"));
        assertTrue(summary.contains("Características:"));
        assertTrue(summary.contains("Habilidades (0):"));
        assertFalse(summary.contains("null"));

        character.setName("Resumen");
        assertTrue(CharacterSummaryBuilder.build(character).contains("Nombre: Resumen"));
    }

    @Test
    public void summaryResolvesCatalogNamesForGeneratedCharacters() throws Exception {
        final CharacterPlayer character = new CharacterPlayer();
        character.setName("Nihal");
        character.setRaceId("laan");
        character.setCultureId("coastalUrbanClassHigh");
        character.setProfessionId("lordOfTheChaos");
        character.increaseLevel();
        character.setCharacteristicsAsConfirmed();
        final String summary = CharacterSummaryBuilder.build(character);
        assertTrue(summary.contains("Nivel: "));
        assertTrue(summary.contains("Raza: "));
        assertTrue(summary.contains("Cultura: "));
        assertTrue(summary.contains("Profesión: "));
        assertFalse(summary.contains("null"));
    }
}
