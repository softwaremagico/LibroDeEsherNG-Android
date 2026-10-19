package com.softwaremagico.librodeesher.android.ui.spells;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.softwaremagico.librodeesher.magic.MagicListType;
import com.softwaremagico.librodeesher.magic.MagicSpellList;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spends the selected character's remaining development points on the current level's spell lists,
 * grouped one {@link MagicListType} at a time (chosen with a spinner). Each row raises or lowers a
 * spell list's ranks through {@link CharacterPlayer#setCurrentLevelSpellListRanks}.
 */
public class SpellsFragment extends Fragment {

    private TextView remainingText;
    private Spinner typeSpinner;
    private TextView emptyText;
    private LinearLayout container;

    private final List<MagicListType> types = new ArrayList<>();
    private final Map<MagicListType, List<MagicSpellList>> spellListsByType = new LinkedHashMap<>();
    private MagicListType selectedType;
    private boolean populatingSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spells, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.spells_remaining);
        typeSpinner = view.findViewById(R.id.spells_type_spinner);
        emptyText = view.findViewById(R.id.spells_empty);
        container = view.findViewById(R.id.spells_container);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                if (populatingSpinner || position < 0 || position >= types.size()) {
                    return;
                }
                selectedType = types.get(position);
                renderSpells();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do.
            }
        });
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
            typeSpinner.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            return;
        }
        final int remaining;
        final List<MagicSpellList> editable;
        try {
            remaining = character.getRemainingDevelopmentPoints();
            editable = editableSpellLists(character);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load spell lists", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            typeSpinner.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            return;
        }
        remainingText.setText(getString(R.string.spells_remaining, remaining));
        try {
            group(character, editable);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot classify spell lists", exception);
        }
        if (types.isEmpty()) {
            typeSpinner.setVisibility(View.GONE);
            emptyText.setText(character.isMagicAllowed() ? R.string.spells_empty : R.string.spells_no_magic);
            emptyText.setVisibility(View.VISIBLE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        typeSpinner.setVisibility(View.VISIBLE);
        if (selectedType == null || !types.contains(selectedType)) {
            selectedType = types.get(0);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, typeNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        populatingSpinner = true;
        typeSpinner.setAdapter(adapter);
        typeSpinner.setSelection(types.indexOf(selectedType));
        populatingSpinner = false;
        renderSpells();
    }

    private void renderSpells() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        final List<MagicSpellList> spellLists = spellListsByType.get(selectedType);
        if (spellLists == null) {
            return;
        }
        final int remaining;
        try {
            remaining = character.getRemainingDevelopmentPoints();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load development points", exception);
            return;
        }
        for (final MagicSpellList spellList : spellLists) {
            try {
                addRow(character, spellList, remaining);
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render spell list " + spellList.getId(), exception);
            }
        }
    }

    private List<MagicSpellList> editableSpellLists(CharacterPlayer character) throws InvalidXmlElementException {
        final Set<String> available = new HashSet<>(character.getAvailableSpellListIds());
        final List<MagicSpellList> spellLists = new ArrayList<>();
        for (final MagicSpellList spellList : RulesCatalog.getInstance().getSpellLists()) {
            if (available.contains(spellList.getId()) || currentLevelRanks(character, spellList.getId()) > 0) {
                spellLists.add(spellList);
            }
        }
        return spellLists;
    }

    private void group(CharacterPlayer character, List<MagicSpellList> editable) throws InvalidXmlElementException {
        types.clear();
        spellListsByType.clear();
        for (final MagicSpellList spellList : editable) {
            final MagicListType type = character.classifySpellList(spellList.getId());
            if (type != null) {
                spellListsByType.computeIfAbsent(type, key -> new ArrayList<>()).add(spellList);
            }
        }
        for (final List<MagicSpellList> spellLists : spellListsByType.values()) {
            spellLists.sort(Comparator.comparing(ElementAdapter::displayName));
        }
        types.addAll(spellListsByType.keySet());
        types.sort(Comparator.comparing(this::typeName));
    }

    private List<String> typeNames() {
        final List<String> names = new ArrayList<>();
        for (final MagicListType type : types) {
            names.add(typeName(type));
        }
        return names;
    }

    private String typeName(MagicListType type) {
        switch (type) {
            case BASIC:
                return getString(R.string.spells_type_basic);
            case OPEN:
                return getString(R.string.spells_type_open);
            case CLOSED:
                return getString(R.string.spells_type_closed);
            case OTHER_PROFESSION:
                return getString(R.string.spells_type_other_profession);
            case OTHER_REALM_OPEN:
                return getString(R.string.spells_type_other_realm_open);
            case OTHER_REALM_CLOSED:
                return getString(R.string.spells_type_other_realm_closed);
            case OTHER_REALM_OTHER_PROFESSION:
                return getString(R.string.spells_type_other_realm_other_profession);
            case ARCHANUM:
                return getString(R.string.spells_type_archanum);
            case TRIAD:
                return getString(R.string.spells_type_triad);
            case COMPLEMENTARY_TRIAD:
                return getString(R.string.spells_type_complementary_triad);
            case TRAINING:
                return getString(R.string.spells_type_training);
            case OTHER_REALM_TRAINING:
                return getString(R.string.spells_type_other_realm_training);
            default:
                return type.name();
        }
    }

    private int currentLevelRanks(CharacterPlayer character, String spellListId) {
        final Integer ranks = character.getCurrentLevel().getSpellListRanks(spellListId);
        return ranks == null ? 0 : ranks;
    }

    private void addRow(CharacterPlayer character, MagicSpellList spellList, int remaining)
            throws InvalidXmlElementException {
        final int ranks = currentLevelRanks(character, spellList.getId());
        final Integer totalRanks = character.getSpellListTotalRanks(spellList.getId());
        final Integer cost = character.getSpellListDevelopmentCost(spellList.getId(),
                totalRanks == null ? 0 : totalRanks, ranks);

        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_spell, container, false);
        final TextView nameView = row.findViewById(R.id.item_spell_name);
        final TextView infoView = row.findViewById(R.id.item_spell_info);
        final TextView ranksView = row.findViewById(R.id.item_spell_ranks);
        final Button minus = row.findViewById(R.id.item_spell_minus);
        final Button plus = row.findViewById(R.id.item_spell_plus);

        nameView.setText(ElementAdapter.displayName(spellList));
        infoView.setText(getString(R.string.spells_rank_cost, ranks, cost == null ? "-" : String.valueOf(cost)));
        ranksView.setText(String.valueOf(ranks));

        minus.setEnabled(ranks > 0);
        minus.setOnClickListener(v -> changeRanks(spellList, ranks - 1));
        plus.setEnabled(cost != null && cost <= remaining);
        plus.setOnClickListener(v -> changeRanks(spellList, ranks + 1));
        container.addView(row);
    }

    private void changeRanks(MagicSpellList spellList, int ranks) {
        if (ranks < 0) {
            return;
        }
        try {
            if (CharacterManager.getInstance().getSelectedCharacter()
                    .setCurrentLevelSpellListRanks(spellList.getId(), ranks)) {
                render();
            } else {
                Toast.makeText(requireContext(), R.string.spells_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot change spell list ranks", exception);
            Toast.makeText(requireContext(), R.string.spells_error, Toast.LENGTH_SHORT).show();
        }
    }
}
