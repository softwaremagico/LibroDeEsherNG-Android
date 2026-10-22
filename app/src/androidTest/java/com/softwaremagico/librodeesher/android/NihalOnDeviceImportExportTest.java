package com.softwaremagico.librodeesher.android;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.Element;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.magic.RealmOfMagic;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Checks the reference level-9 save against the application on the device itself: the legacy
 * snapshot is imported, saved with the app's own save path and read back with the app's own load
 * path, always producing the same export.
 *
 * <p>The JVM counterpart of this test is {@code NihalLevelNineEndToEndTest}, which builds the
 * character from scratch; here the parity is verified on the Android runtime, where the rules
 * catalog, the JSON mapper and the database are the ones the application really uses.</p>
 */
@RunWith(AndroidJUnit4.class)
public class NihalOnDeviceImportExportTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        RulesCatalog.getInstance();
    }

    @Test
    public void referenceSurvivesTheApplicationSaveAndLoadPaths() throws Exception {
        final String expected = NihalFixture.expectedExport();

        final CharacterEntity stored = NihalFixture.insertReferenceCharacter(context);
        assertNotNull(stored);
        assertEquals(NihalFixture.CHARACTER_NAME, stored.getName());
        assertEquals(9, stored.getLevel());
        assertEquals("The app's save path must export the reference content", expected, stored.getJson());

        final CharacterPlayer reloaded = stored.getCharacterPlayer();
        assertNotNull("The app's load path must rebuild the character", reloaded);
        assertEquals("Nihal Dragoniana", reloaded.getName());
        assertEquals(9, reloaded.getLevel());
        assertEquals(9, reloaded.getLevels().size());
        assertEquals(expected, CharacterJsonManager.toJson(CharacterDataMapper.toData(reloaded)));
    }

    @Test
    public void referenceKeepsItsBookkeptTotalsOnDevice() throws Exception {
        final CharacterEntity stored = NihalFixture.insertReferenceCharacter(context);
        final CharacterPlayer character = stored.getCharacterPlayer();
        assertNotNull(character);

        int categoriesWithRanks = 0;
        int categoryRanks = 0;
        for (final Element category : RulesCatalog.getInstance().getCategories()) {
            final int ranks = character.getCategoryTotalRanks(category.getId());
            if (ranks > 0) {
                categoriesWithRanks++;
                categoryRanks += ranks;
            }
        }
        assertEquals(22, categoriesWithRanks);
        assertEquals(97, categoryRanks);

        int skillsWithRanks = 0;
        int skillRanks = 0;
        for (final Element skill : RulesCatalog.getInstance().getSkills()) {
            final int ranks = character.getSkillTotalRanks(skill.getId());
            if (ranks > 0) {
                skillsWithRanks++;
                skillRanks += ranks;
            }
        }
        assertEquals(63, skillsWithRanks);
        assertEquals(159, skillRanks);

        int listsWithRanks = 0;
        int listRanks = 0;
        for (final Element list : RulesCatalog.getInstance().getSpellLists()) {
            final int ranks = character.getSpellListTotalRanks(list.getId());
            if (ranks > 0) {
                listsWithRanks++;
                listRanks += ranks;
            }
        }
        assertEquals(9, listsWithRanks);
        assertEquals(58, listRanks);

        assertEquals(Integer.valueOf(10), character.getCategoryTotalRanks("weaponsEdged"));
        assertEquals(Integer.valueOf(9), character.getSkillTotalRanks("sword"));
        assertEquals(Integer.valueOf(14), character.getSpellListTotalRanks("essenceArmorOfTheChaos"));
        assertEquals(9, character.getWeaponCategoryCostTierCount());
        assertEquals(10, character.getDecisions().getAll().size());
        assertEquals(List.of(RealmOfMagic.ESSENCE), character.getRealmsOfMagic());
        assertTrue(character.getPowerPoints() > 0);
    }
}
