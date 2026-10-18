package com.softwaremagico.librodeesher.android.core;

import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.skill.Skill;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.List;

/**
 * Renders a short, human-readable summary of a {@link CharacterPlayer} for the UI.
 */
public final class CharacterSummaryBuilder {

    private static final CharacteristicAbbreviation[] REAL_CHARACTERISTICS = {
            CharacteristicAbbreviation.AGILITY,
            CharacteristicAbbreviation.CONSTITUTION,
            CharacteristicAbbreviation.MEMORY,
            CharacteristicAbbreviation.REASONING,
            CharacteristicAbbreviation.SELF_DISCIPLINE,
            CharacteristicAbbreviation.EMPATHY,
            CharacteristicAbbreviation.INTUITION,
            CharacteristicAbbreviation.PRESENCE,
            CharacteristicAbbreviation.QUICKNESS,
            CharacteristicAbbreviation.STRENGTH,
            CharacteristicAbbreviation.APPEARANCE
    };

    private CharacterSummaryBuilder() {
        // Utility class.
    }

    public static String build(CharacterPlayer character) {
        if (character == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        appendIdentity(builder, character);
        appendCharacteristics(builder, character);
        appendSkills(builder, character);
        return builder.toString().trim();
    }

    private static void appendIdentity(StringBuilder builder, CharacterPlayer character) {
        builder.append("Nombre: ").append(nullToDash(character.getName())).append('\n');
        builder.append("Sexo: ").append(character.getSex() == null ? "-" : character.getSex().getTag()).append('\n');
        builder.append("Raza: ").append(resolveRace(character)).append('\n');
        builder.append("Cultura: ").append(resolveCulture(character)).append('\n');
        builder.append("Profesión: ").append(resolveProfession(character)).append('\n');
        builder.append("Nivel: ").append(character.getLevel()).append('\n');
        builder.append("Edad: ").append(character.getCurrentAge()).append('\n');
    }

    private static void appendCharacteristics(StringBuilder builder, CharacterPlayer character) {
        builder.append('\n').append("Características:").append('\n');
        for (final CharacteristicAbbreviation abbreviation : REAL_CHARACTERISTICS) {
            final Integer temporal = character.getCharacteristicTemporalValue(abbreviation);
            builder.append("  ").append(abbreviation.getTag()).append(": ").append(temporal == null ? 0 : temporal)
                    .append('\n');
        }
    }

    private static void appendSkills(StringBuilder builder, CharacterPlayer character) {
        List<Skill> skills;
        try {
            skills = RulesCatalog.getInstance().getSkills();
        } catch (InvalidXmlElementException exception) {
            return;
        }
        final StringBuilder skillsBuilder = new StringBuilder();
        int total = 0;
        for (final Skill skill : skills) {
            final Integer ranks = character.getSkillTotalRanks(skill.getId());
            if (ranks != null && ranks > 0) {
                total++;
                skillsBuilder.append("  ").append(display(skill)).append(": ").append(ranks).append('\n');
            }
        }
        builder.append('\n').append("Habilidades (").append(total).append("):").append('\n');
        builder.append(skillsBuilder);
    }

    private static String resolveRace(CharacterPlayer character) {
        try {
            final Race race = character.getRace();
            return race == null ? "-" : display(race);
        } catch (InvalidXmlElementException exception) {
            return "-";
        }
    }

    private static String resolveCulture(CharacterPlayer character) {
        try {
            final Culture culture = character.getCulture();
            return culture == null ? "-" : display(culture);
        } catch (InvalidXmlElementException exception) {
            return "-";
        }
    }

    private static String resolveProfession(CharacterPlayer character) {
        try {
            final Profession profession = character.getProfession();
            return profession == null ? "-" : display(profession);
        } catch (InvalidXmlElementException exception) {
            return "-";
        }
    }

    private static String display(com.softwaremagico.librodeesher.Element element) {
        if (element == null) {
            return "-";
        }
        if (element.getName() != null && element.getName().getTranslatedText() != null) {
            return element.getName().getTranslatedText();
        }
        return nullToDash(element.getId());
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
