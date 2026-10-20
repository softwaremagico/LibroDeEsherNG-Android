package com.softwaremagico.librodeesher.android.ui.skills;

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
import com.softwaremagico.librodeesher.category.Category;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.skill.Skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spends the selected character's remaining development points on the current level's skills,
 * grouped one category at a time (chosen with a spinner). Each row raises or lowers a skill's ranks
 * through {@link CharacterPlayer#setCurrentLevelSkillRanks}.
 */
public class SkillsFragment extends Fragment {

    private TextView remainingText;
    private Spinner categorySpinner;
    private TextView emptyText;
    private LinearLayout container;

    private final List<String> categoryIds = new ArrayList<>();
    private final Map<String, List<Skill>> skillsByCategory = new LinkedHashMap<>();
    private String selectedCategoryId;
    private boolean populatingSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_skills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.skills_remaining);
        categorySpinner = view.findViewById(R.id.skills_category_spinner);
        emptyText = view.findViewById(R.id.skills_empty);
        container = view.findViewById(R.id.skills_container);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                if (populatingSpinner || position < 0 || position >= categoryIds.size()) {
                    return;
                }
                selectedCategoryId = categoryIds.get(position);
                renderSkills();
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
            categorySpinner.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            return;
        }
        final int remaining;
        final List<Skill> editable;
        try {
            remaining = character.getRemainingDevelopmentPoints();
            editable = editableSkills(character);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load skills", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            categorySpinner.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            return;
        }
        remainingText.setText(getString(R.string.skills_remaining, remaining));
        group(editable);
        if (categoryIds.isEmpty()) {
            categorySpinner.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        categorySpinner.setVisibility(View.VISIBLE);
        if (selectedCategoryId == null || !categoryIds.contains(selectedCategoryId)) {
            selectedCategoryId = categoryIds.get(0);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        populatingSpinner = true;
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(categoryIds.indexOf(selectedCategoryId));
        populatingSpinner = false;
        renderSkills();
    }

    private void renderSkills() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        final List<Skill> skills = skillsByCategory.get(selectedCategoryId);
        if (skills == null) {
            return;
        }
        final int remaining;
        try {
            remaining = character.getRemainingDevelopmentPoints();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load development points", exception);
            return;
        }
        for (final Skill skill : skills) {
            try {
                addRow(character, skill, remaining);
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render skill " + skill.getId(), exception);
            }
        }
    }

    private List<Skill> editableSkills(CharacterPlayer character) throws InvalidXmlElementException {
        try {
            final Set<String> available = new HashSet<>(character.getAvailableSkillIds());
            final List<Skill> skills = new ArrayList<>();
            for (final Skill skill : RulesCatalog.getInstance().getSkills()) {
                if (available.contains(skill.getId()) || character.getSkillCurrentLevelRanks(skill.getId()) > 0
                        || (character.getProfession() != null)) {
                    skills.add(skill);
                }
            }
            if (skills.isEmpty()) {
                skills.addAll(RulesCatalog.getInstance().getSkills());
            }
            return skills;
        } catch (Exception e) {
            return new ArrayList<>(RulesCatalog.getInstance().getSkills());
        }
    }

    private void group(List<Skill> editable) {
        categoryIds.clear();
        skillsByCategory.clear();
        for (final Skill skill : editable) {
            skillsByCategory.computeIfAbsent(skill.getCategoryId(), key -> new ArrayList<>()).add(skill);
        }
        for (final List<Skill> skills : skillsByCategory.values()) {
            skills.sort(Comparator.comparing(ElementAdapter::displayName));
        }
        categoryIds.addAll(skillsByCategory.keySet());
        categoryIds.sort(Comparator.comparing(this::categoryName));
    }

    private List<String> categoryNames() {
        final List<String> names = new ArrayList<>();
        for (final String categoryId : categoryIds) {
            names.add(categoryName(categoryId));
        }
        return names;
    }

    private String categoryName(String categoryId) {
        try {
            final Category category = RulesCatalog.getInstance().getCategory(categoryId);
            if (category != null && category.getName() != null && category.getName().getTranslatedText() != null) {
                return category.getName().getTranslatedText();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve category " + categoryId, exception);
        }
        return categoryId;
    }

    private void addRow(CharacterPlayer character, Skill skill, int remaining) throws InvalidXmlElementException {
        final int ranks = character.getSkillCurrentLevelRanks(skill.getId());
        final Integer cost = character.getSkillDevelopmentCost(skill.getId(), ranks);

        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_skill, container, false);
        final TextView nameView = row.findViewById(R.id.item_skill_name);
        final TextView infoView = row.findViewById(R.id.item_skill_info);
        final TextView ranksView = row.findViewById(R.id.item_skill_ranks);
        final Button minus = row.findViewById(R.id.item_skill_minus);
        final Button plus = row.findViewById(R.id.item_skill_plus);

        nameView.setText(ElementAdapter.displayName(skill));
        infoView.setText(getString(R.string.skills_rank_cost, ranks, cost == null ? "-" : String.valueOf(cost)));
        ranksView.setText(String.valueOf(ranks));

        minus.setEnabled(ranks > 0);
        minus.setOnClickListener(v -> changeRanks(skill, ranks - 1));
        plus.setEnabled(cost != null && cost <= remaining);
        plus.setOnClickListener(v -> changeRanks(skill, ranks + 1));
        container.addView(row);
    }

    private void changeRanks(Skill skill, int ranks) {
        if (ranks < 0) {
            return;
        }
        try {
            if (CharacterManager.getInstance().getSelectedCharacter().setCurrentLevelSkillRanks(skill.getId(), ranks)) {
                render();
            } else {
                Toast.makeText(requireContext(), R.string.skills_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot change skill ranks", exception);
            Toast.makeText(requireContext(), R.string.skills_error, Toast.LENGTH_SHORT).show();
        }
    }
}
