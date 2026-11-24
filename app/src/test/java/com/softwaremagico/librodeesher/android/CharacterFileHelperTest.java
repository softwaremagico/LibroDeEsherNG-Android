package com.softwaremagico.librodeesher.android;

import com.softwaremagico.librodeesher.android.persistence.CharacterFileHelper;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CharacterFileHelperTest {

    @Test
    public void exportedCharacterCanBeImportedBack() throws Exception {
        final CharacterPlayer original = randomCharacter();
        final String json = CharacterFileHelper.toJson(original);

        final CharacterPlayer restored = CharacterFileHelper.importCharacterFromJson(json);

        assertNotNull(restored);
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getRaceId(), restored.getRaceId());
        assertEquals(original.getCultureId(), restored.getCultureId());
        assertEquals(original.getProfessionId(), restored.getProfessionId());
        assertEquals(original.getLevel(), restored.getLevel());
    }

    @Test
    public void levelFileIsValidatedAgainstThePreviousLevelSnapshot() throws Exception {
        final CharacterPlayer original = randomCharacter();
        assertEquals(1, original.getLevel());

        original.increaseLevel();
        final String levelJson = com.softwaremagico.librodeesher.persistence.LevelJsonManager.toJson(original);
        final CharacterPlayer previous = CharacterDataMapper.toCharacter(
                CharacterDataMapper.toDataAtLevel(original, 1));
        final com.softwaremagico.librodeesher.level.LevelUp imported =
                com.softwaremagico.librodeesher.persistence.LevelJsonManager.fromJson(previous, levelJson);

        assertNotNull(imported);
        assertEquals(original.getCurrentLevel().getCategoryRanks(), imported.getCategoryRanks());
        assertEquals(original.getCurrentLevel().getSkillRanks(), imported.getSkillRanks());
    }

    private static CharacterPlayer randomCharacter() throws Exception {
        RulesCatalog.getInstance();
        final CharacterPlayer character = new CharacterPlayer();
        character.setRaceId(RulesCatalog.getInstance().getRaces().get(0).getId());
        character.setCultureId(RulesCatalog.getInstance().getCultures().get(0).getId());
        character.setProfessionId(RulesCatalog.getInstance().getProfessions().get(0).getId());
        character.setName("Import Export Test");
        new RandomCharacterPlayer(character, 1).createRandomValues();
        return character;
    }
}
