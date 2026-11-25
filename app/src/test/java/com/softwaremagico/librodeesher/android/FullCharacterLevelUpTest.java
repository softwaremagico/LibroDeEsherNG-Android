package com.softwaremagico.librodeesher.android;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/** JVM checks of the rules engine; the instrumented suite stays strictly UI-only. */
public class FullCharacterLevelUpTest {
    @Test
    public void createAndLevelUpCharacter() throws Exception {
        RulesCatalog.getInstance();
        CharacterPlayer character = new CharacterPlayer();
        List<Race> races = RulesCatalog.getInstance().getRaces();
        List<Culture> cultures = RulesCatalog.getInstance().getCultures();
        List<Profession> professions = RulesCatalog.getInstance().getProfessions();
        if (!races.isEmpty()) character.setRaceId(races.get(0).getId());
        if (!cultures.isEmpty()) character.setCultureId(cultures.get(0).getId());
        if (!professions.isEmpty()) character.setProfessionId(professions.get(0).getId());
        new RandomCharacterPlayer(character).createRandomValues();
        int initialLevel = character.getLevel();
        character.increaseLevel();
        new RandomCharacterPlayer(character, character.getLevels().size()).createRandomValues();
        assertTrue(character.getLevel() >= initialLevel);
    }
}
