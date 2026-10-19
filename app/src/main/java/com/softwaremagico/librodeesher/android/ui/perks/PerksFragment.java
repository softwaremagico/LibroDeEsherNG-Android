package com.softwaremagico.librodeesher.android.ui.perks;

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
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.perk.Perk;
import com.softwaremagico.librodeesher.perk.SelectedPerk;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the selected character's perks: lists the perks already chosen (removable unless they were
 * assigned at random) and the ones still available, adding or removing them through
 * {@link CharacterPlayer#addAvailablePerk} / {@link CharacterPlayer#removePerk}.
 */
public class PerksFragment extends Fragment {

    private TextView remainingText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.perks_remaining);
        container = view.findViewById(R.id.perks_container);
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
            remainingText.setText(R.string.sheet_no_character);
            return;
        }
        final Integer remaining;
        final List<SelectedPerk> selected;
        final List<String> available;
        try {
            remaining = character.getRemainingPerksPoints();
            selected = new ArrayList<>(character.getSelectedPerks());
            available = character.getAvailablePerkIds();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load perks", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            return;
        }
        remainingText.setText(getString(R.string.perks_remaining, remaining == null ? 0 : remaining));

        if (!selected.isEmpty()) {
            container.addView(header(getString(R.string.perks_selected_header)));
            for (final SelectedPerk selectedPerk : selected) {
                try {
                    addSelectedRow(character, selectedPerk);
                } catch (InvalidXmlElementException exception) {
                    AppLog.error("Cannot render perk " + selectedPerk.getPerkId(), exception);
                }
            }
        }

        container.addView(header(getString(R.string.perks_available_header)));
        if (available.isEmpty()) {
            final TextView empty = new TextView(requireContext());
            empty.setText(R.string.perks_empty);
            empty.setPadding(0, 8, 0, 8);
            container.addView(empty);
            return;
        }
        available.sort(Comparator.comparing(this::perkName));
        for (final String perkId : available) {
            try {
                addAvailableRow(character, perkId, remaining == null ? 0 : remaining);
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render available perk " + perkId, exception);
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

    private String perkName(String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            if (perk != null) {
                return ElementAdapter.displayName(perk);
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve perk " + perkId, exception);
        }
        return perkId;
    }

    private void addSelectedRow(CharacterPlayer character, SelectedPerk selectedPerk)
            throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_perk, container, false);
        final TextView nameView = row.findViewById(R.id.item_perk_name);
        final TextView infoView = row.findViewById(R.id.item_perk_info);
        final Button action = row.findViewById(R.id.item_perk_action);

        nameView.setText(perkName(selectedPerk.getPerkId()));
        infoView.setText(perkInfo(selectedPerk.getPerkId(), selectedPerk.isRandom()));

        action.setText("-");
        action.setEnabled(!selectedPerk.isRandom());
        action.setOnClickListener(v -> removePerk(selectedPerk.getPerkId()));
        container.addView(row);
    }

    private void addAvailableRow(CharacterPlayer character, String perkId, int remaining)
            throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_perk, container, false);
        final TextView nameView = row.findViewById(R.id.item_perk_name);
        final TextView infoView = row.findViewById(R.id.item_perk_info);
        final Button action = row.findViewById(R.id.item_perk_action);

        final Integer cost = perkCost(perkId);
        nameView.setText(perkName(perkId));
        infoView.setText(perkInfo(perkId, false));

        action.setText("+");
        action.setEnabled(cost == null || cost <= remaining);
        action.setOnClickListener(v -> addPerk(perkId));
        container.addView(row);
    }

    private String perkInfo(String perkId, boolean random) {
        final Integer cost = perkCost(perkId);
        final String base = getString(R.string.perks_cost, cost == null ? 0 : cost);
        return random ? base + " · " + getString(R.string.perks_random) : base;
    }

    private Integer perkCost(String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            return perk == null ? null : perk.getCost();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve perk cost " + perkId, exception);
            return null;
        }
    }

    private void addPerk(String perkId) {
        try {
            if (CharacterManager.getInstance().getSelectedCharacter().addAvailablePerk(perkId)) {
                render();
            } else {
                Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot add perk", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void removePerk(String perkId) {
        try {
            CharacterManager.getInstance().getSelectedCharacter().removePerk(perkId);
            render();
        } catch (RuntimeException exception) {
            AppLog.error("Cannot remove perk", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }
}
