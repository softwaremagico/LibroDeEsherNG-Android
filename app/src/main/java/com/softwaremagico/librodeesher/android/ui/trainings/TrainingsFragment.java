package com.softwaremagico.librodeesher.android.ui.trainings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.decision.DecisionKey;
import com.softwaremagico.librodeesher.decision.DecisionKind;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.training.ChoiceGroup;
import com.softwaremagico.librodeesher.training.Training;
import com.softwaremagico.librodeesher.training.TrainingCategoryGrant;
import com.softwaremagico.librodeesher.training.TrainingSkillGrant;
import com.softwaremagico.librodeesher.training.TrainingSpellListGrant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Legacy-style training selector: picks the trainings of the character's current level. */
public class TrainingsFragment extends Fragment {

    private TextView remainingText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.trainings_remaining);
        container = view.findViewById(R.id.trainings_container);
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
        final List<String> selected;
        final List<String> available;
        final Integer remaining;
        try {
            selected = new ArrayList<>(character.getSelectedTrainingIds());
            available = new ArrayList<>(character.getAvailableTrainingIds());
            remaining = character.getRemainingDevelopmentPoints();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load trainings", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            return;
        }
        selected.sort(Comparator.comparing(this::trainingName));
        available.sort(Comparator.comparing(this::trainingName));
        remainingText.setText(getString(R.string.trainings_remaining, remaining == null ? 0 : remaining));

        container.addView(header(getString(R.string.trainings_selected_header)));
        if (selected.isEmpty()) {
            container.addView(empty(getString(R.string.trainings_none_selected)));
        }
        for (final String trainingId : selected) {
            addRow(character, trainingId,
                    character.getCurrentLevel().getTrainings().contains(trainingId));
        }

        container.addView(header(getString(R.string.trainings_available_header)));
        if (available.isEmpty()) {
            container.addView(empty(getString(R.string.trainings_empty)));
        }
        for (final String trainingId : available) {
            addRow(character, trainingId, false);
        }
    }

    private void addRow(CharacterPlayer character, String trainingId, boolean removable) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_training, container, false);
        final TextView name = row.findViewById(R.id.item_training_name);
        final TextView info = row.findViewById(R.id.item_training_info);
        final Button action = row.findViewById(R.id.item_training_action);
        name.setText(trainingName(trainingId));
        info.setText(trainingInfo(character, trainingId));
        if (removable) {
            action.setText(R.string.trainings_remove);
            action.setOnClickListener(v -> {
                if (character.removeCurrentLevelTraining(trainingId)) {
                    render();
                } else {
                    Toast.makeText(requireContext(), R.string.trainings_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            action.setText(R.string.trainings_add);
            action.setEnabled(false);
            action.setOnClickListener(v -> addTraining(character, trainingId, action));
            try {
                action.setEnabled(character.getAvailableTrainingIds().contains(trainingId));
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot check training availability", exception);
            }
        }
        container.addView(row);
    }

    private void addTraining(CharacterPlayer character, String trainingId, Button action) {
        try {
            if (character.addTraining(trainingId)) {
                showChoices(character, trainingId);
            } else {
                Toast.makeText(requireContext(), R.string.trainings_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot add training", exception);
            Toast.makeText(requireContext(), R.string.trainings_error, Toast.LENGTH_SHORT).show();
            action.setEnabled(false);
        }
    }

    /** Asks the user for every choice the training needs before recording its grants. */
    private void showChoices(CharacterPlayer character, String trainingId) {
        final Training training;
        try {
            training = RulesCatalog.getInstance().getTraining(trainingId);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve training choices", exception);
            render();
            return;
        }
        if (training == null) {
            render();
            return;
        }
        final List<Choice> choices = new ArrayList<>();
        final List<TrainingCategoryGrant> grants = training.getCategories();
        final boolean hasGrants = grants != null && !grants.isEmpty();
        if (hasGrants) {
            for (int i = 0; i < grants.size(); i++) {
                final TrainingCategoryGrant grant = grants.get(i);
                if (grant.getCategoryOptions() != null && !grant.getCategoryOptions().isEmpty()) {
                    choices.add(new Choice(getString(R.string.trainings_category, i + 1),
                            grant.getCategoryOptions(), "category", i, -1, null));
                }
                if (grant.getSkills() != null) {
                    for (int j = 0; j < grant.getSkills().size(); j++) {
                        final TrainingSkillGrant skill = grant.getSkills().get(j);
                        if (skill.isChoice() && skill.getSkillOptions() != null) {
                            choices.add(new Choice(getString(R.string.trainings_skill, i + 1, j + 1),
                                    skill.getSkillOptions(), "skill", i, j, null));
                        }
                    }
                }
                if (grant.getSpellLists() != null) {
                    for (int j = 0; j < grant.getSpellLists().size(); j++) {
                        final TrainingSpellListGrant spellList = grant.getSpellLists().get(j);
                        if (spellList.isChoice() && spellList.getSpellListOptions() != null) {
                            choices.add(new Choice(getString(R.string.trainings_spell_list, i + 1, j + 1),
                                    spellList.getSpellListOptions(), "spellList", i, j, null));
                        }
                    }
                }
            }
        }
        addSkillChoices(choices, training.getLifeSkills(), "life");
        addSkillChoices(choices, training.getCommonSkills(), "common");
        addSkillChoices(choices, training.getProfessionalSkills(), "professional");
        addSkillChoices(choices, training.getRestrictedSkills(), "restricted");
        if (training.getCharacteristicUpgrades() != null) {
            for (int i = 0; i < training.getCharacteristicUpgrades().size(); i++) {
                final ChoiceGroup group = training.getCharacteristicUpgrades().get(i);
                if (group.getOptions() != null && !group.getOptions().isEmpty()) {
                    choices.add(new Choice(getString(R.string.trainings_characteristic, i + 1),
                            group.getOptions(), "characteristic", i, -1, group));
                }
            }
        }
        if (choices.isEmpty()) {
            if (hasGrants) {
                applyChoices(character, training, choices, new ArrayList<>(), hasGrants);
            } else {
                render();
            }
            return;
        }

        final LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        final int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        final List<Spinner> spinners = new ArrayList<>();
        for (final Choice choice : choices) {
            final TextView label = new TextView(requireContext());
            label.setText(choice.label);
            layout.addView(label);
            if (!choice.showSpinner()) {
                continue;
            }
            final Spinner spinner = new Spinner(requireContext());
            spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, choice.options));
            spinners.add(spinner);
            layout.addView(spinner);
        }
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.trainings_choices, trainingName(trainingId)))
                .setView(layout)
                .setPositiveButton(R.string.accept,
                        (dialog, which) -> applyChoices(character, training, choices, spinners, hasGrants))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addSkillChoices(List<Choice> choices, List<ChoiceGroup> groups, String kind) {
        if (groups == null) {
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            final ChoiceGroup group = groups.get(i);
            if (group.getOptions() != null && !group.getOptions().isEmpty()) {
                choices.add(new Choice(getString(R.string.trainings_choice, kind, i + 1),
                        group.getOptions(), kind, i, -1, group));
            }
        }
    }

    private void applyChoices(CharacterPlayer character, Training training, List<Choice> choices,
                              List<Spinner> spinners, boolean hasGrants) {
        final Map<Integer, String> life = new HashMap<>();
        final Map<Integer, String> common = new HashMap<>();
        final Map<Integer, String> professional = new HashMap<>();
        final Map<Integer, String> restricted = new HashMap<>();
        final Map<Integer, String> categories = new HashMap<>();
        final Map<Integer, List<String>> skills = new HashMap<>();
        final Map<Integer, List<String>> spellLists = new HashMap<>();
        final List<int[]> characteristicIndexes = new ArrayList<>();
        final List<String> characteristicValues = new ArrayList<>();
        int spinnerIndex = 0;
        for (final Choice choice : choices) {
            final String selected = choice.showSpinner() && spinnerIndex < spinners.size()
                    ? choice.options.get(spinners.get(spinnerIndex++).getSelectedItemPosition())
                    : choice.options.get(0);
            switch (choice.kind) {
                case "life":
                    life.put(choice.index, selected);
                    break;
                case "common":
                    common.put(choice.index, selected);
                    break;
                case "professional":
                    professional.put(choice.index, selected);
                    break;
                case "restricted":
                    restricted.put(choice.index, selected);
                    break;
                case "category":
                    categories.put(choice.index, selected);
                    break;
                case "skill":
                    skills.computeIfAbsent(choice.index, key -> new ArrayList<>()).add(selected);
                    break;
                case "spellList":
                    spellLists.computeIfAbsent(choice.index, key -> new ArrayList<>()).add(selected);
                    break;
                case "characteristic":
                    characteristicIndexes.add(new int[]{choice.index});
                    characteristicValues.add(selected);
                    break;
                default:
                    break;
            }
        }
        try {
            character.applyTrainingSkillChoices(training, life, common, professional, restricted);
            if (hasGrants) {
                character.applyTrainingCategories(training, categories, skills, null, spellLists, null);
            }
            for (int i = 0; i < characteristicIndexes.size(); i++) {
                final int index = characteristicIndexes.get(i).length == 0 ? 0 : characteristicIndexes.get(i)[0];
                character.applyCharacteristicUpgrade(
                        DecisionKey.atLevel(DecisionKind.TRAINING_CHARACTERISTIC, training.getId(), index,
                                character.getLevel()),
                        training.getCharacteristicUpgrades().get(index),
                        CharacteristicAbbreviation.valueOf(characteristicValues.get(i)));
            }
        } catch (Exception exception) {
            AppLog.error("Cannot apply training choices", exception);
            Toast.makeText(requireContext(), R.string.trainings_error, Toast.LENGTH_LONG).show();
        }
        render();
    }

    private static final class Choice {
        private final String label;
        private final List<String> options;
        private final String kind;
        private final int index;
        @SuppressWarnings("unused")
        private final int subIndex;
        @SuppressWarnings("unused")
        private final ChoiceGroup group;

        private Choice(String label, List<String> options, String kind, int index, int subIndex,
                       ChoiceGroup group) {
            this.label = label;
            this.options = options;
            this.kind = kind;
            this.index = index;
            this.subIndex = subIndex;
            this.group = group;
        }

        private boolean showSpinner() {
            return options.size() > 1;
        }
    }

    private String trainingInfo(CharacterPlayer character, String trainingId) {
        final StringBuilder info = new StringBuilder();
        try {
            final Integer cost = character.getTrainingDevelopmentCost(trainingId);
            info.append(getString(R.string.trainings_cost, cost == null ? 0 : cost));
            if (character.isTrainingFavouredByProfession(trainingId)) {
                info.append(" · ").append(getString(R.string.trainings_favoured));
            }
            final Training training = RulesCatalog.getInstance().getTraining(trainingId);
            if (training != null && training.getTrainingTimeInMonths() != null) {
                info.append(" · ").append(getString(R.string.trainings_months,
                        training.getTrainingTimeInMonths()));
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot describe training " + trainingId, exception);
        }
        return info.toString();
    }

    private String trainingName(String trainingId) {
        try {
            final Training training = RulesCatalog.getInstance().getTraining(trainingId);
            if (training != null) {
                return ElementAdapter.displayName(training);
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve training " + trainingId, exception);
        }
        return trainingId;
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private TextView empty(String text) {
        final TextView empty = new TextView(requireContext());
        empty.setText(text);
        empty.setPadding(0, 8, 0, 8);
        return empty;
    }
}
