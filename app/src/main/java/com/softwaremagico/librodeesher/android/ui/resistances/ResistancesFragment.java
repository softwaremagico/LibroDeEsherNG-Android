package com.softwaremagico.librodeesher.android.ui.resistances;

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
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.resistance.ResistanceType;

/**
 * Read-only view of the selected character's combat values (armour class, defensive bonus, movement)
 * and the nine resistance rolls.
 */
public class ResistancesFragment extends Fragment {

    private static final ResistanceType[] RESISTANCES = {
            ResistanceType.CHANNELING,
            ResistanceType.ESSENCE,
            ResistanceType.MENTALISM,
            ResistanceType.PSIONIC,
            ResistanceType.POISON,
            ResistanceType.DISEASE,
            ResistanceType.COLD,
            ResistanceType.HEAT,
            ResistanceType.FEAR
    };

    private TextView emptyText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resistances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyText = view.findViewById(R.id.resistances_empty);
        container = view.findViewById(R.id.resistances_container);
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null || character.getLevel() == 0) {
            emptyText.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        try {
            container.addView(header(getString(R.string.resistances_combat_header)));
            addRow(getString(R.string.resistances_armour_class), String.valueOf(character.getArmourClass()));
            addRow(getString(R.string.resistances_defensive_bonus), String.valueOf(character.getDefensiveBonus()));
            addRow(getString(R.string.resistances_movement), String.valueOf(character.getMovementCapacity()));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot render combat values", exception);
        }

        container.addView(header(getString(R.string.resistances_header)));
        for (final ResistanceType resistance : RESISTANCES) {
            try {
                addRow(resistance.getTag(), String.valueOf(character.getResistanceTotalBonus(resistance)));
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render resistance " + resistance.name(), exception);
                addRow(resistance.getTag(), "-");
            }
        }
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private void addRow(String name, String value) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
        final TextView nameView = row.findViewById(R.id.item_stat_name);
        final TextView valueView = row.findViewById(R.id.item_stat_value);
        nameView.setText(name);
        valueView.setText(value);
        container.addView(row);
    }
}
