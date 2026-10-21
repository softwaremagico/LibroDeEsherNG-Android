package com.softwaremagico.librodeesher.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FullCharacterCreationTest {
    @Test
    public void createCompleteRandomCharacter() throws Exception {
        RulesCatalog.getInstance();
        CharacterPlayer character = new CharacterPlayer();
        List<Race> races = RulesCatalog.getInstance().getRaces();
        List<Culture> cultures = RulesCatalog.getInstance().getCultures();
        List<Profession> professions = RulesCatalog.getInstance().getProfessions();
        if (!races.isEmpty()) character.setRaceId(races.get(0).getId());
        if (!cultures.isEmpty()) character.setCultureId(cultures.get(0).getId());
        if (!professions.isEmpty()) character.setProfessionId(professions.get(0).getId());
        new RandomCharacterPlayer(character).createRandomValues();
        assertNotNull(character);
        assertNotNull(character.getRaceId());
        assertNotNull(character.getCultureId());
        assertNotNull(character.getProfessionId());
        assertTrue(character.getLevel() >= 1);
    }

    @Test
    public void testCharacterHasSkills() throws Exception {
        RulesCatalog.getInstance();
        CharacterPlayer character = new CharacterPlayer();
        List<Race> races = RulesCatalog.getInstance().getRaces();
        List<Culture> cultures = RulesCatalog.getInstance().getCultures();
        List<Profession> professions = RulesCatalog.getInstance().getProfessions();
        if (!races.isEmpty()) character.setRaceId(races.get(0).getId());
        if (!cultures.isEmpty()) character.setCultureId(cultures.get(0).getId());
        if (!professions.isEmpty()) character.setProfessionId(professions.get(0).getId());
        new RandomCharacterPlayer(character).createRandomValues();
        assertNotNull(character);
    }
}
