package com.softwaremagico.librodeesher.android.core;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

/** Android-side composition of the legacy "character is well formed" gate. */
public final class CharacterValidator {

    private CharacterValidator() {
    }

    /** Returns a string resource describing the problem, or {@code 0} when the character is valid. */
    public static int problem(CharacterPlayer character) {
        if (character == null || character.getRaceId() == null || character.getLevel() == 0) {
            return R.string.sheet_no_character;
        }
        if (!character.isCharacteristicsConfirmed()) {
            return R.string.character_confirm_characteristics_first;
        }
        try {
            if (character.getRemainingDevelopmentPoints() < 0) {
                return R.string.character_overspent_development_points;
            }
        } catch (Exception exception) {
            return R.string.character_validation_error;
        }
        return 0;
    }
}
