package com.softwaremagico.librodeesher.android;

import com.softwaremagico.librodeesher.Element;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.background.Background;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.character.SexType;
import com.softwaremagico.librodeesher.characteristic.Appearance;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.characteristic.CharacteristicRoll;
import com.softwaremagico.librodeesher.decision.Decision;
import com.softwaremagico.librodeesher.equipment.Equipment;
import com.softwaremagico.librodeesher.equipment.MagicObject;
import com.softwaremagico.librodeesher.level.LevelUp;
import com.softwaremagico.librodeesher.magic.RealmOfMagic;
import com.softwaremagico.librodeesher.perk.SelectedPerk;
import com.softwaremagico.librodeesher.persistence.CharacterData;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.persistence.DecisionData;
import com.softwaremagico.librodeesher.persistence.LegacyCharacterJsonImporter;
import com.softwaremagico.librodeesher.persistence.LevelData;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * End-to-end parity of the reference level-9 save through the Android application's own flow.
 *
 * <p>Nihal is built from an empty character (a level one {@link CharacterPlayer}) with the
 * reference's choices, taken through every level-up with {@link CharacterPlayer#increaseLevel()}
 * until level 9, exported through the app's save path ({@link CharacterEntity}) and compared
 * against the reference {@code .rlm} snapshot; the export is then imported again through the app's
 * load path to check the result does not change.</p>
 *
 * <p>The reference is a legacy 2.0.0 save, so its content expressed in the app's save format is
 * {@link LegacyCharacterJsonImporter}'s canonical {@code CharacterData}: that importer is the piece
 * validated against the raw file by {@code LegacyCharacterJsonImporterTest} in librodeesher-rules,
 * while the totals asserted here are the ones the reference character sheet bookkeeps.</p>
 */
public class NihalLevelNineEndToEndTest {

    private static final String REFERENCE_SNAPSHOT = "/Nihal_level9_2_0_0.json";

    @Test
    public void createsNihalFromScratchAndReachesLevelNine() throws Exception {
        final CharacterPlayer character = createNihalFromScratch();

        assertEquals(9, character.getLevel());
        assertEquals(9, character.getLevels().size());
        // Nihal is ten years old at every level: leveling up never aged her.
        assertEquals(10, character.getAgeAtLevel(9));
        assertIdentityAndCreationSteps(character);
        assertReferenceBookTotals(character);
    }

    @Test
    public void exportedJsonMatchesTheReferenceContent() throws Exception {
        final CharacterEntity saved = new CharacterEntity(createNihalFromScratch());

        assertEquals("Nihal Dragoniana", saved.getName());
        assertEquals(9, saved.getLevel());
        assertNotNull(saved.getRace());
        assertNotNull(saved.getCulture());
        assertNotNull(saved.getProfession());
        assertJsonEquals(CharacterJsonManager.toJson(reference()), saved.getJson());
    }

    @Test
    public void reimportingTheExportKeepsTheAppResultUnchanged() throws Exception {
        final CharacterEntity saved = new CharacterEntity(createNihalFromScratch());

        final CharacterPlayer reloaded = saved.getCharacterPlayer();
        assertNotNull(reloaded);
        assertIdentityAndCreationSteps(reloaded);
        assertReferenceBookTotals(reloaded);

        final CharacterEntity reexported = new CharacterEntity(reloaded);
        assertJsonEquals(saved.getJson(), reexported.getJson());

        // The app's own session round trip (load, then save what the app is showing) is stable too.
        final CharacterManager manager = CharacterManager.getInstance();
        manager.setSelectedCharacter(reloaded);
        final CharacterEntity fromSession = new CharacterEntity(manager.getSelectedCharacter());
        assertJsonEquals(saved.getJson(), fromSession.getJson());
    }

    /**
     * Builds Nihal from an empty character through the same model API the creation screens use:
     * identity and selections, the rule options, the hand-assigned characteristics, the background,
     * the creation decisions and perks, and then one {@link CharacterPlayer#increaseLevel()} per
     * level up to level nine, filling each new level with the development the reference bought.
     */
    private static CharacterPlayer createNihalFromScratch() throws Exception {
        final CharacterData reference = reference();
        final CharacterPlayer character = new CharacterPlayer();

        // Creation screen: name, sex, race, culture and profession.
        character.setName(reference.getName());
        character.setSex(reference.getSex());
        character.setRaceId(reference.getRaceId());
        character.setCultureId(reference.getCultureId());
        character.setProfessionId(reference.getProfessionId());
        character.setHistoryText(reference.getHistoryText());

        // Rule options come before the state they gate: enabling/disabling a rule can wipe what
        // that rule already bought (see CharacterDataMapper#toCharacter).
        character.setFirearmsAllowed(reference.isFirearmsAllowed());
        character.setChiPowersAllowed(reference.isChiPowersAllowed());
        character.setOtherRealmTrainingSpellsAllowed(reference.isOtherRealmTrainingSpellsAllowed());
        character.setMagicAllowed(reference.isMagicAllowed());
        character.setDarkSpellsAsBasicListsAllowed(reference.isDarkSpellsAsBasicListsAllowed());
        character.setRecommendedFavouriteSkillsIncluded(reference.isRecommendedFavouriteSkillsIncluded());
        character.setHandWritingFont(reference.isHandWritingFont());
        character.setSortPdfSkills(reference.isSortPdfSkills());
        character.setPerksCostBackgroundPoints(reference.isPerksCostBackgroundPoints());
        character.setCategoryMaxCost(reference.getCategoryMaxCost());

        // Characteristics screen: the hand-assigned creation values, then confirm.
        for (final Map.Entry<String, Integer> entry : reference.getCharacteristicTemporalValues().entrySet()) {
            character.setCharacteristicTemporalValue(CharacteristicAbbreviation.valueOf(entry.getKey()),
                    entry.getValue());
        }
        for (final Map.Entry<String, Integer> entry : reference.getCharacteristicPotentialValues().entrySet()) {
            character.setCharacteristicPotentialValue(CharacteristicAbbreviation.valueOf(entry.getKey()),
                    entry.getValue());
        }
        if (reference.isCharacteristicsConfirmed()) {
            character.setCharacteristicsAsConfirmed();
        }
        character.setAppearance(new Appearance(reference.getAppearance()));
        character.setCurrentAge(reference.getCurrentAge());
        character.setFinalAge(reference.getFinalAge());

        // History screen: the background chosen with the character's creation points.
        final Background background = character.getBackground();
        background.setCategoryIds(new ArrayList<>(reference.getBackground().getCategoryIds()));
        background.setSkillIds(new ArrayList<>(reference.getBackground().getSkillIds()));
        for (final CharacteristicRoll roll : reference.getBackground().getCharacteristicUpdates()) {
            background.addCharacteristicUpdate(roll.getCharacteristicAbbreviation(),
                    roll.getCharacteristicTemporalValue(), roll.getCharacteristicPotentialValue(), roll.getRoll());
        }
        background.getLanguageRanks().putAll(reference.getBackground().getLanguageRanks());

        // Creation decisions (weapon cost tiers, profession realm, perk choices...) and their
        // outcomes, recorded the same way the model records them when the screens take them.
        for (final DecisionData decisionData : reference.getDecisions().values()) {
            final Decision decision;
            if (decisionData.getSelectedOptions().size() == 1) {
                decision = Decision.select(decisionData.getOfferedOptions(), decisionData.getSelectedOptions().get(0));
            } else {
                decision = Decision.selectMultiple(decisionData.getOfferedOptions(),
                        decisionData.getSelectedOptions(), decisionData.getSelectedOptions().size());
            }
            final int recordedAtLevel = decisionData.getRecordedAtLevel() >= 1
                    ? decisionData.getRecordedAtLevel() : character.getLevel();
            character.getDecisions().set(decisionData.getKey().toDecisionKey(),
                    decision.recordedAtLevel(recordedAtLevel));
        }

        character.getSelectedPerks().clear();
        for (final SelectedPerk perk : reference.getSelectedPerks()) {
            final SelectedPerk selectedPerk = new SelectedPerk();
            selectedPerk.setPerkId(perk.getPerkId());
            selectedPerk.setWeaknessId(perk.getWeaknessId());
            selectedPerk.setRandom(perk.isRandom());
            character.getSelectedPerks().add(selectedPerk);
        }

        for (final Map.Entry<String, Integer> entry : reference.getHobbySkillRanks().entrySet()) {
            character.setHobbySkillRank(entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, Integer> entry : reference.getHobbySpellListRanks().entrySet()) {
            character.setHobbySpellListRank(entry.getKey(), entry.getValue());
        }
        for (final MagicObject magicObject : reference.getMagicItems()) {
            character.addMagicItem(magicObject);
        }
        for (final Equipment equipment : reference.getStandardEquipment()) {
            character.addStandardEquipment(equipment);
        }

        // Level-ups: the character starts at level one, then every further level is a level-up the
        // app takes (SheetFragment's entry point) and fills with the development bought there.
        final List<LevelData> levels = reference.getLevels();
        assertTrue("The reference is a level 9 character", levels.size() == 9);
        fillLevel(character.getLevels().get(0), levels.get(0));
        for (int level = 1; level < levels.size(); level++) {
            fillLevel(character.increaseLevel(), levels.get(level));
        }
        return character;
    }

    /** Writes one level's bookkeeping onto a level-up, as taking that level-up would. */
    private static void fillLevel(LevelUp levelUp, LevelData levelData) {
        levelUp.setCategoryRanks(new HashMap<>(levelData.getCategoryRanks()));
        levelUp.setDevelopmentPointExemptCategoryRanks(
                new HashMap<>(levelData.getDevelopmentPointExemptCategoryRanks()));
        levelUp.setTrainingCategoryRanks(new HashMap<>(levelData.getTrainingCategoryRanks()));
        levelUp.setSkillRanks(new HashMap<>(levelData.getSkillRanks()));
        levelUp.setDevelopmentPointExemptSkillRanks(new HashMap<>(levelData.getDevelopmentPointExemptSkillRanks()));
        levelUp.setTrainingSkillRanks(new HashMap<>(levelData.getTrainingSkillRanks()));
        levelUp.setSpellListRanks(new HashMap<>(levelData.getSpellListRanks()));
        levelUp.setDevelopmentPointExemptSpellListRanks(
                new HashMap<>(levelData.getDevelopmentPointExemptSpellListRanks()));
        levelUp.setTrainingSpellListRanks(new HashMap<>(levelData.getTrainingSpellListRanks()));
        levelUp.setTrainings(new ArrayList<>(levelData.getTrainings()));
        levelUp.setSpellsUpdated(new ArrayList<>(levelData.getSpellsUpdated()));
        levelUp.setGeneralizedSkills(new HashSet<>(levelData.getGeneralizedSkills()));
        levelUp.setSkillSpecializations(new HashSet<>(levelData.getSkillSpecializations()));
        levelUp.setFavouriteSkills(new HashSet<>(levelData.getFavouriteSkills()));
        levelUp.setFavouriteSpellLists(new HashSet<>(levelData.getFavouriteSpellLists()));
        for (final CharacteristicRoll roll : levelData.getCharacteristicUpdates()) {
            levelUp.addCharacteristicUpdate(roll.getCharacteristicAbbreviation(), roll.getCharacteristicTemporalValue(),
                    roll.getCharacteristicPotentialValue(), roll.getRoll());
        }
        levelUp.getAgeModifications().addAll(levelData.getAgeModifications());
        levelUp.setAge(levelData.getAge());
    }

    /** The reference's identity, creation steps and choices, as the legacy snapshot stores them. */
    private static void assertIdentityAndCreationSteps(CharacterPlayer character) throws Exception {
        assertEquals("Nihal Dragoniana", character.getName());
        assertEquals(SexType.FEMALE, character.getSex());
        assertEquals("laan", character.getRaceId());
        assertEquals("coastalUrbanClassHigh", character.getCultureId());
        assertEquals("lordOfTheChaos", character.getProfessionId());
        // The catalog still resolves the reference file's own Spanish names.
        assertEquals("Laan", character.getRace().getName().getSpanish());
        assertEquals("Litoral Urbana (Clase Alta)", character.getCulture().getName().getSpanish());
        assertEquals("Lord del Caos", character.getProfession().getName().getSpanish());

        // The creation temporal values are the ones the reference took to level 9.
        assertEquals(Integer.valueOf(93), character.getCharacteristicTemporalValue(CharacteristicAbbreviation.AGILITY));
        assertEquals(Integer.valueOf(100),
                character.getCharacteristicTemporalValue(CharacteristicAbbreviation.STRENGTH));
        assertEquals(Integer.valueOf(97),
                character.getCharacteristicTemporalValue(CharacteristicAbbreviation.SELF_DISCIPLINE));
        assertEquals(Integer.valueOf(0), character.getCharacteristicTemporalValue(CharacteristicAbbreviation.APPEARANCE));
        assertEquals(Integer.valueOf(97),
                character.getCharacteristicPotentialValue(CharacteristicAbbreviation.CONSTITUTION));
        assertTrue(character.isCharacteristicsConfirmed());
        assertEquals(29, character.getAppearance().getDicesResult());

        assertEquals(10, character.getCurrentAge());
        assertEquals(10, character.getFinalAge());
        for (final LevelUp level : character.getLevels()) {
            assertEquals(10, level.getAge());
        }

        assertEquals(List.of("influence"), character.getBackground().getCategoryIds());
        assertEquals(List.of("bolas", "sword", "physicalDevelopment", "powerPointDevelopment"),
                character.getBackground().getSkillIds());
        assertEquals(List.of("herbExpert", "timeSkills", "intolerant", "leadershipSkills", "survivalInstinct"),
                character.getSelectedPerks().stream().map(SelectedPerk::getPerkId).toList());
        assertEquals(3, character.getAllMagicItems().size());
        assertEquals("Espada", character.getAllMagicItems().get(0).getName().getSpanish());
        assertEquals(10, character.getDecisions().getAll().size());
        assertEquals(9, character.getWeaponCategoryCostTierCount());
        assertEquals(List.of(RealmOfMagic.ESSENCE), character.getRealmsOfMagic());
    }

    /** The rank totals the reference character sheet bookkeeps for Nihal at level nine. */
    private static void assertReferenceBookTotals(CharacterPlayer character) throws Exception {
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
        assertEquals(Integer.valueOf(10), character.getCategoryTotalRanks("weaponsThrown"));
        assertEquals(Integer.valueOf(9), character.getCategoryTotalRanks("loreDark"));
        assertEquals(Integer.valueOf(9), character.getSkillTotalRanks("sword"));
        assertEquals(Integer.valueOf(10), character.getSkillTotalRanks("bolas"));
        assertEquals(Integer.valueOf(11), character.getSkillTotalRanks("physicalDevelopment"));
        assertEquals(Integer.valueOf(6), character.getSkillTotalRanks("demonology"));
        assertEquals(Integer.valueOf(14), character.getSpellListTotalRanks("essenceArmorOfTheChaos"));
        assertEquals(Integer.valueOf(8), character.getSpellListTotalRanks("essenceSummoningsOscuras"));

        assertEquals(9, character.getWeaponCategoryCostTierCount());
        assertEquals(10, character.getDecisions().getAll().size());
        assertEquals(List.of(RealmOfMagic.ESSENCE), character.getRealmsOfMagic());
        assertTrue(character.getPowerPoints() > 0);
        assertFalse(character.getSelectedPerks().isEmpty());
    }

    private static CharacterData reference() throws Exception {
        return LegacyCharacterJsonImporter.importLegacyJson(readReferenceSnapshot());
    }

    private static String readReferenceSnapshot() throws IOException {
        try (final InputStream in = NihalLevelNineEndToEndTest.class.getResourceAsStream(REFERENCE_SNAPSHOT)) {
            assertNotNull("Missing reference snapshot " + REFERENCE_SNAPSHOT, in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Compares both exports, reporting the first differing offset instead of two huge blobs. */
    private static void assertJsonEquals(String expected, String actual) {
        if (expected.equals(actual)) {
            return;
        }
        int offset = 0;
        while (offset < expected.length() && offset < actual.length()
                && expected.charAt(offset) == actual.charAt(offset)) {
            offset++;
        }
        fail("Exported JSON differs from the reference at offset " + offset + ".\nExpected: "
                + excerpt(expected, offset) + "\nActual:   " + excerpt(actual, offset));
    }

    private static String excerpt(String json, int offset) {
        final int from = Math.max(0, offset - 60);
        final int to = Math.min(json.length(), offset + 60);
        return (from > 0 ? "..." : "") + json.substring(from, to) + (to < json.length() ? "..." : "");
    }
}
