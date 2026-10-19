package com.softwaremagico.librodeesher.android.ui.characteristics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;

/**
 * Read-only view of the selected character's eleven characteristics: temporal value, potential
 * value, race bonus and total (with bonuses) value.
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
            container.setVisibility(View.GONE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        addRow(getString(R.string.characteristics_abbr), getString(R.string.characteristics_temporal),
                getString(R.string.characteristics_potential), getString(R.string.characteristics_race),
                getString(R.string.characteristics_total), true);
        container.addView(divider());

        for (int i = 0; i < CHARACTERISTICS.length; i++) {
            final CharacteristicAbbreviation abbreviation = CHARACTERISTICS[i];
            final String label = NAMES[i] + " (" + abbreviation.getCode() + ")";
            try {
                addRow(label,
                        String.valueOf(character.getCharacteristicTemporalValue(abbreviation)),
                        String.valueOf(character.getCharacteristicPotentialValue(abbreviation)),
                        String.valueOf(character.getCharacteristicRaceBonus(abbreviation)),
                        String.valueOf(character.getCharacteristicTotalBonus(abbreviation)),
                        false);
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render characteristic " + abbreviation.getCode(), exception);
                addRow(label, "-", "-", "-", "-", false);
            }
        }
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
