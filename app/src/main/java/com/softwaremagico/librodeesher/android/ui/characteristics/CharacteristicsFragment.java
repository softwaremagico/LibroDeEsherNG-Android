package com.softwaremagico.librodeesher.android.ui.characteristics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;

/**
 * Editable view of the selected character's eleven characteristics following the creation rules.
 */
public class CharacteristicsFragment extends Fragment {

    private static final CharacteristicAbbreviation[] CHARACTERISTICS = {
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

    private static final String[] NAMES = {
            "Agilidad", "Constitución", "Memoria", "Razonamiento", "Autodisciplina",
            "Empatía", "Intuición", "Presencia", "Rapidez", "Fuerza", "Apariencia"
    };

    private TextView emptyText;
    private LinearLayout container;
    private TextView pointsText;
    private Button confirmButton;
    private boolean generating;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_characteristics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyText = view.findViewById(R.id.characteristics_empty);
        container = view.findViewById(R.id.characteristics_container);
        pointsText = view.findViewById(R.id.characteristics_points);
        confirmButton = view.findViewById(R.id.characteristics_confirm);
        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> confirmCharacteristics());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            emptyText.setVisibility(View.VISIBLE);
            if (pointsText != null) {
                pointsText.setText("");
            }
            if (confirmButton != null) {
                confirmButton.setVisibility(View.GONE);
            }
            container.setVisibility(View.GONE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
        updatePoints(character);

        addRow(getString(R.string.characteristics_abbr), getString(R.string.characteristics_temporal),
                getString(R.string.characteristics_potential), getString(R.string.characteristics_race),
                getString(R.string.characteristics_total), true);
        container.addView(divider());

        for (int i = 0; i < CHARACTERISTICS.length; i++) {
            final CharacteristicAbbreviation abbreviation = CHARACTERISTICS[i];
            final String label = NAMES[i] + " (" + abbreviation.getCode() + ")";
            try {
                addEditableRow(character, label, abbreviation);
            } catch (Exception exception) {
                AppLog.error("Cannot render characteristic " + abbreviation.getCode(), exception);
                addRow(label, "-", "-", "-", "-", false);
            }
        }
    }

    private void updatePoints(CharacterPlayer character) {
        try {
            final int spent = character.getCharacteristicsTemporalPointsSpent();
            final int total = character.getCharacteristicsTemporalTotalPoints();
            if (pointsText != null) {
                pointsText.setText(spent + " / " + total);
            }
            if (confirmButton != null) {
                confirmButton.setVisibility(View.VISIBLE);
                if (character.isCharacteristicsConfirmed()) {
                    confirmButton.setEnabled(false);
                } else {
                    confirmButton.setEnabled(spent <= total);
                }
            }
        } catch (Exception ignored) {
            if (pointsText != null) {
                pointsText.setText("");
            }
            if (confirmButton != null) {
                confirmButton.setVisibility(View.GONE);
            }
        }
    }

    private void confirmCharacteristics() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        try {
            final int spent = character.getCharacteristicsTemporalPointsSpent();
            final int total = character.getCharacteristicsTemporalTotalPoints();
            if (spent > total) {
                Toast.makeText(requireContext(), R.string.error_creating_character, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!character.isCharacteristicsConfirmed()) {
                character.setCharacteristicsAsConfirmed();
            }
            if (!generating) {
                generating = true;
                try {
                    final Profession profession = character.getProfession();
                    if (profession != null && (character.getRealmsOfMagic() == null || character.getRealmsOfMagic().isEmpty())) {
                        try {
                            new RandomCharacterPlayer(character).createRandomValues();
                        } catch (Exception exception) {
                            AppLog.error("Cannot complete character creation", exception);
                        }
                    }
                } finally {
                    generating = false;
                }
            }
            render();
            Toast.makeText(requireContext(), R.string.accept, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot confirm characteristics", exception);
            Toast.makeText(requireContext(), R.string.error_creating_character, Toast.LENGTH_SHORT).show();
        }
    }

    private void addEditableRow(CharacterPlayer character, String name, CharacteristicAbbreviation abbreviation) throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_characteristic, container, false);
        final TextView nameView = row.findViewById(R.id.item_characteristic_name);
        final TextView temporalView = row.findViewById(R.id.item_characteristic_temporal);
        final TextView potentialView = row.findViewById(R.id.item_characteristic_potential);
        final TextView raceView = row.findViewById(R.id.item_characteristic_race);
        final TextView totalView = row.findViewById(R.id.item_characteristic_total);
        final Button minus = row.findViewById(R.id.item_characteristic_minus);
        final Button plus = row.findViewById(R.id.item_characteristic_plus);
        nameView.setText(name);

        minus.setOnClickListener(v -> change(character, abbreviation, -1));
        plus.setOnClickListener(v -> change(character, abbreviation, 1));
        if (character.isCharacteristicsConfirmed()) {
            minus.setEnabled(false);
            plus.setEnabled(false);
        }

        updateRow(character, abbreviation, temporalView, potentialView, raceView, totalView);
        container.addView(row);
    }

    private void change(CharacterPlayer character, CharacteristicAbbreviation abbreviation, int delta) {
        if (character.isCharacteristicsConfirmed()) {
            return;
        }
        try {
            final Profession profession = character.getProfession();
            if (profession != null && profession.isPreferredCharacteristic(abbreviation) && delta > 0) {
                if ((character.getCharacteristicTemporalValue(abbreviation) == null
                        || character.getCharacteristicTemporalValue(abbreviation) < 90)) {
                    character.setCharacteristicTemporalValue(abbreviation, 90);
                    render();
                    return;
                }
            }
            final int current = character.getCharacteristicTemporalValue(abbreviation) == null ? 10 : character.getCharacteristicTemporalValue(abbreviation);
            final int next = current + delta;
            if (next < 10) {
                return;
            }
            final int max = 100;
            if (next > max) {
                return;
            }
            character.setCharacteristicTemporalValue(abbreviation, next);
            final int spent = character.getCharacteristicsTemporalPointsSpent();
            final int total = character.getCharacteristicsTemporalTotalPoints();
            if (spent > total) {
                character.setCharacteristicTemporalValue(abbreviation, current);
                render();
                return;
            }
            render();
        } catch (Exception exception) {
            AppLog.error("Cannot change characteristic", exception);
        }
    }

    private void updateRow(CharacterPlayer character, CharacteristicAbbreviation abbreviation,
                           TextView temporalView, TextView potentialView, TextView raceView,
                           TextView totalView) throws InvalidXmlElementException {
        temporalView.setText(String.valueOf(character.getCharacteristicTemporalValue(abbreviation)));
        potentialView.setText(String.valueOf(character.getCharacteristicPotentialValue(abbreviation)));
        raceView.setText(String.valueOf(character.getCharacteristicRaceBonus(abbreviation)));
        totalView.setText(String.valueOf(character.getCharacteristicTotalBonus(abbreviation)));
    }

    private void addRow(String name, String temporal, String potential, String race, String total,
                        boolean header) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_characteristic,
                container, false);
        final TextView nameView = row.findViewById(R.id.item_characteristic_name);
        final TextView temporalView = row.findViewById(R.id.item_characteristic_temporal);
        final TextView potentialView = row.findViewById(R.id.item_characteristic_potential);
        final TextView raceView = row.findViewById(R.id.item_characteristic_race);
        final TextView totalView = row.findViewById(R.id.item_characteristic_total);
        final Button minus = row.findViewById(R.id.item_characteristic_minus);
        final Button plus = row.findViewById(R.id.item_characteristic_plus);
        if (minus != null) minus.setVisibility(View.INVISIBLE);
        if (plus != null) plus.setVisibility(View.INVISIBLE);
        nameView.setText(name);
        temporalView.setText(temporal);
        potentialView.setText(potential);
        raceView.setText(race);
        totalView.setText(total);
        if (header) {
            final Typeface bold = Typeface.create(nameView.getTypeface(), Typeface.BOLD);
            nameView.setTypeface(bold);
            temporalView.setTypeface(bold);
            potentialView.setTypeface(bold);
            raceView.setTypeface(bold);
            totalView.setTypeface(bold);
        }
        container.addView(row);
    }

    private View divider() {
        final View divider = new View(requireContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(requireContext().getColor(R.color.md_theme_outlineVariant));
        return divider;
    }
}
