package com.softwaremagico.librodeesher.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.softwaremagico.librodeesher.android.persistence.CharacterFileHelper;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import org.junit.Test;

/** JVM coverage for the JSON bridge used by save/load, import/export and cloning. */
public class CharacterFileHelperTest {

    @Test
    public void characterJsonRoundTrips() throws Exception {
        final CharacterPlayer original = new CharacterPlayer();
        original.setName("Serializado");
        original.setRaceId("laan");
        original.setCultureId("coastalUrbanClassHigh");
        original.setProfessionId("lordOfTheChaos");

        final String json = CharacterFileHelper.toJson(original);
        assertNotNull(json);
        assertTrue(json.contains("Serializado"));

        final CharacterPlayer restored = CharacterFileHelper.importCharacterFromJson(json);
        assertEquals("Serializado", restored.getName());
        assertEquals("laan", restored.getRaceId());
        assertEquals("coastalUrbanClassHigh", restored.getCultureId());
        assertEquals("lordOfTheChaos", restored.getProfessionId());
    }

    @Test(expected = Exception.class)
    public void invalidJsonIsRejected() throws Exception {
        CharacterFileHelper.importCharacterFromJson("{not-a-character");
    }
}
