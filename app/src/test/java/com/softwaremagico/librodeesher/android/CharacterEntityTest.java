package com.softwaremagico.librodeesher.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.TimestampConverter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import org.junit.Test;

import java.sql.Timestamp;

/** JVM coverage for the persistence value objects that do not need Android. */
public class CharacterEntityTest {

    @Test
    public void entitySerializesAndRestoresTheCharacter() {
        final CharacterPlayer character = new CharacterPlayer();
        character.setName("Entidad");
        final CharacterEntity entity = new CharacterEntity(character);

        assertEquals("Entidad", entity.getName());
        assertNotNull(entity.getJson());
        assertTrue(entity.getJson().contains("Entidad"));
        assertNotNull(entity.getCreationTime());
        assertEquals(entity.getCreationTime(), entity.getUpdateTime());

        final CharacterEntity empty = new CharacterEntity();
        empty.setCharacterPlayer(null);
        assertEquals(0, empty.getLevel());
    }

    @Test
    public void timestampConverterRoundTrips() {
        final Timestamp timestamp = Timestamp.valueOf("2026-11-24 12:34:56");
        assertEquals(timestamp.getTime(), TimestampConverter.toTimestamp(timestamp.getTime()).getTime());
        assertEquals(timestamp.getTime(), TimestampConverter.toLong(timestamp).longValue());
        assertEquals(null, TimestampConverter.toTimestamp(null));
        assertEquals(null, TimestampConverter.toLong(null));
    }
}
