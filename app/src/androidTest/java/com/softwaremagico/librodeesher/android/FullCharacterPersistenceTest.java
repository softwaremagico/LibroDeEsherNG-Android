package com.softwaremagico.librodeesher.android;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FullCharacterPersistenceTest {
    @Test
    public void createSaveLoadAndVerifyCharacter() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        RulesCatalog.getInstance();

        CharacterPlayer character = new CharacterPlayer();
        List<com.softwaremagico.librodeesher.race.Race> races = RulesCatalog.getInstance().getRaces();
        List<com.softwaremagico.librodeesher.culture.Culture> cultures = RulesCatalog.getInstance().getCultures();
        List<com.softwaremagico.librodeesher.profession.Profession> professions = RulesCatalog.getInstance().getProfessions();
        if (!races.isEmpty()) character.setRaceId(races.get(0).getId());
        if (!cultures.isEmpty()) character.setCultureId(cultures.get(0).getId());
        if (!professions.isEmpty()) character.setProfessionId(professions.get(0).getId());
        new RandomCharacterPlayer(character).createRandomValues();

        assertNotNull(character.getName());
        assertNotNull(character.getRaceId());
        assertNotNull(character.getCultureId());
        assertNotNull(character.getProfessionId());
        assertTrue(character.getLevel() >= 1);

        CharacterEntity entity = new CharacterEntity(character);
        long id = AppDatabase.getInstance(context).getCharacterEntityDao().insert(entity);
        assertTrue(id > 0);

        List<CharacterEntity> characters = AppDatabase.getInstance(context).getCharacterEntityDao().getAll();
        assertTrue(characters.size() > 0);

        CharacterEntity loadedEntity = null;
        for (CharacterEntity candidate : characters) {
            if (candidate.getId() == id) {
                loadedEntity = candidate;
                break;
            }
        }
        assertNotNull(loadedEntity);

        CharacterPlayer loaded = loadedEntity.getCharacterPlayer();
        assertNotNull(loaded);
        assertEquals(character.getName(), loaded.getName());
        assertEquals(character.getRaceId(), loaded.getRaceId());
        assertEquals(character.getCultureId(), loaded.getCultureId());
        assertEquals(character.getProfessionId(), loaded.getProfessionId());
        assertEquals(character.getLevel(), loaded.getLevel());
        assertNotNull(loaded.getLevels());

        CharacterManager.getInstance().setSelectedCharacter(loaded);
        CharacterManager.getInstance().setSelectedCharacterId(id);
        assertEquals(id, CharacterManager.getInstance().getSelectedCharacterId());
        assertNotNull(CharacterManager.getInstance().getSelectedCharacter());
        assertEquals(character.getName(), CharacterManager.getInstance().getSelectedCharacter().getName());
    }
}
