package com.softwaremagico.librodeesher.android.ui.history;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.android.ui.components.ChoiceDialogs;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.background.Background;
import com.softwaremagico.librodeesher.category.Category;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicAbbreviation;
import com.softwaremagico.librodeesher.characteristic.CharacteristicRoll;
import com.softwaremagico.librodeesher.dice.Roll;
import com.softwaremagico.librodeesher.language.TranslatedText;
import com.softwaremagico.librodeesher.equipment.MagicObject;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.skill.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Editable background: history points, languages, characteristic rolls and magic items. */
public class HistoryFragment extends Fragment {

    private TextView emptyText;
    private LinearLayout container;
    private boolean showAllSkills;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyText = view.findViewById(R.id.history_empty);
        container = view.findViewById(R.id.history_container);
        final Button randomButton = view.findViewById(R.id.history_random);
        final CheckBox showAll = view.findViewById(R.id.history_show_all);
        randomButton.setOnClickListener(v -> generateRandom());
        showAll.setOnCheckedChangeListener((button, checked) -> {
            showAllSkills = checked;
            render();
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
            emptyText.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        final Background background = character.getBackground();
        container.addView(header(getString(R.string.history_points_header)));
        addRow(getString(R.string.history_points_spent), String.valueOf(background.getSpentBackgroundPoints()));
        try {
            addRow(getString(R.string.history_points_remaining),
                    String.valueOf(character.getRemainingBackgroundPoints()));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot render background points", exception);
        }

        addSkillSection(character, background);
        addCategorySection(character, background);
        addLanguageSection(character, background);
        addCharacteristicSection(character);
        addMagicItemsSection(character);
        addHistoryText(character);
    }

    private void addSkillSection(CharacterPlayer character, Background background) {
        container.addView(header(getString(R.string.history_skills_header)));
        for (final String skillId : background.getSkillIds()) {
            final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
            ((TextView) row.findViewById(R.id.item_stat_name)).setText(elementName(skillId, true));
            ((TextView) row.findViewById(R.id.item_stat_value))
                    .setText(getString(R.string.history_bonus, background.getSkillBonus(skillId)));
            final Button remove = new Button(requireContext());
            remove.setText(R.string.trainings_remove);
            remove.setOnClickListener(v -> {
                background.setSkillPoint(skillId, false);
                render();
            });
            ((LinearLayout) row).addView(remove);
            container.addView(row);
        }
        final Button add = new Button(requireContext());
        add.setText(R.string.history_add_skill);
        add.setOnClickListener(v -> {
            final List<String> ids = new ArrayList<>();
            final List<String> names = new ArrayList<>();
            try {
                for (final Skill skill : RulesCatalog.getInstance().getSkills()) {
                    if (!showAllSkills && !background.isSkillPointSelected(skill.getId())) {
                        continue;
                    }
                    if (character.isSkillEnabled(skill)) {
                        ids.add(skill.getId());
                        names.add(ElementAdapter.displayName(skill));
                    }
                }
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot list history skills", exception);
            }
            if (ids.isEmpty()) {
                Toast.makeText(requireContext(), R.string.history_no_options, Toast.LENGTH_SHORT).show();
                return;
            }
            ChoiceDialogs.selectOne(requireContext(), getString(R.string.history_add_skill), names,
                    selected -> {
                        background.setSkillPoint(ids.get(names.indexOf(selected)), true);
                        render();
                    });
        });
        container.addView(add);
    }

    private void addCategorySection(CharacterPlayer character, Background background) {
        container.addView(header(getString(R.string.history_categories_header)));
        for (final String categoryId : background.getCategoryIds()) {
            final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
            ((TextView) row.findViewById(R.id.item_stat_name)).setText(elementName(categoryId, false));
            ((TextView) row.findViewById(R.id.item_stat_value))
                    .setText(getString(R.string.history_bonus, background.getCategoryBonus(categoryId)));
            final Button remove = new Button(requireContext());
            remove.setText(R.string.trainings_remove);
            remove.setOnClickListener(v -> {
                background.setCategoryPoint(categoryId, false);
                render();
            });
            ((LinearLayout) row).addView(remove);
            container.addView(row);
        }
        final Button add = new Button(requireContext());
        add.setText(R.string.history_add_category);
        add.setOnClickListener(v -> {
            final List<String> ids = new ArrayList<>();
            final List<String> names = new ArrayList<>();
            try {
                for (final Category category : RulesCatalog.getInstance().getCategories()) {
                    if (!showAllSkills && !background.isCategoryPointSelected(category.getId())) {
                        continue;
                    }
                    if (character.isCategoryEnabledByOptions(category)) {
                        ids.add(category.getId());
                        names.add(ElementAdapter.displayName(category));
                    }
                }
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot list history categories", exception);
            }
            if (ids.isEmpty()) {
                Toast.makeText(requireContext(), R.string.history_no_options, Toast.LENGTH_SHORT).show();
                return;
            }
            ChoiceDialogs.selectOne(requireContext(), getString(R.string.history_add_category), names,
                    selected -> {
                        background.setCategoryPoint(ids.get(names.indexOf(selected)), true);
                        render();
                    });
        });
        container.addView(add);
    }

    private void addLanguageSection(CharacterPlayer character, Background background) {
        container.addView(header(getString(R.string.history_languages_header)));
        for (final Map.Entry<String, Integer> entry : background.getLanguageRanks().entrySet()) {
            final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
            ((TextView) row.findViewById(R.id.item_stat_name)).setText(entry.getKey());
            ((TextView) row.findViewById(R.id.item_stat_value)).setText(String.valueOf(entry.getValue()));
            final Button remove = new Button(requireContext());
            remove.setText(R.string.trainings_remove);
            remove.setOnClickListener(v -> {
                setLanguageRank(character, entry.getKey(), Math.max(0, entry.getValue() - 20));
                render();
            });
            ((LinearLayout) row).addView(remove);
            container.addView(row);
        }
        final Button add = new Button(requireContext());
        add.setText(R.string.history_add_language);
        add.setOnClickListener(v -> {
            final List<String> languages = languageIds(character);
            if (languages.isEmpty()) {
                Toast.makeText(requireContext(), R.string.history_no_options, Toast.LENGTH_SHORT).show();
                return;
            }
            ChoiceDialogs.selectOne(requireContext(), getString(R.string.history_add_language), languages,
                    selected -> {
                        setLanguageRank(character, selected,
                                background.getHistoryLanguageRank(selected) + 20);
                        render();
                    });
        });
        container.addView(add);
    }

    private void setLanguageRank(CharacterPlayer character, String languageId, int ranks) {
        try {
            character.setBackgroundLanguageRank(languageId, ranks);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot set background language rank", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> languageIds(CharacterPlayer character) {
        final List<String> languages = new ArrayList<>();
        try {
            if (character.getRace() != null) {
                for (final com.softwaremagico.librodeesher.race.RaceLanguage language
                        : character.getRace().getRaceLanguages()) {
                    if (!languages.contains(language.getLanguageId())) {
                        languages.add(language.getLanguageId());
                    }
                }
                for (final com.softwaremagico.librodeesher.race.RaceLanguage language
                        : character.getRace().getBackgroundLanguages()) {
                    if (!languages.contains(language.getLanguageId())) {
                        languages.add(language.getLanguageId());
                    }
                }
            }
            if (character.getCulture() != null && character.getCulture().getLanguageMaxRanks() != null) {
                for (final com.softwaremagico.librodeesher.culture.CultureLanguageRank language
                        : character.getCulture().getLanguageMaxRanks()) {
                    if (!languages.contains(language.getLanguageId())) {
                        languages.add(language.getLanguageId());
                    }
                }
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot list languages", exception);
        }
        return languages;
    }

    private void addCharacteristicSection(CharacterPlayer character) {
        container.addView(header(getString(R.string.history_characteristics_header)));
        for (final CharacteristicRoll roll : character.getBackground().getCharacteristicUpdates()) {
            addRow(roll.getCharacteristicAbbreviation().getCode(),
                    getString(R.string.history_roll, roll.getCharacteristicTemporalValue(),
                            roll.getCharacteristicPotentialValue()));
        }
        for (final CharacteristicAbbreviation abbreviation : CharacteristicAbbreviation.values()) {
            final Button upgrade = new Button(requireContext());
            upgrade.setText(getString(R.string.history_up_characteristic, abbreviation.getCode()));
            upgrade.setOnClickListener(v -> {
                character.applyBackgroundCharacteristicUpdate(abbreviation, new Roll());
                render();
            });
            container.addView(upgrade);
        }
    }

    private void addMagicItemsSection(CharacterPlayer character) {
        container.addView(header(getString(R.string.history_magic_items_header)));
        for (final MagicObject item : character.getAllMagicItems()) {
            final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
            ((TextView) row.findViewById(R.id.item_stat_name))
                    .setText(item.getName() == null ? "" : item.getName().getTranslatedText());
            ((TextView) row.findViewById(R.id.item_stat_value))
                    .setText(item.getDescription() == null ? "" : item.getDescription().getTranslatedText());
            final Button remove = new Button(requireContext());
            remove.setText(R.string.trainings_remove);
            remove.setOnClickListener(v -> {
                character.removeMagicItem(item);
                render();
            });
            ((LinearLayout) row).addView(remove);
            container.addView(row);
        }
        final Button add = new Button(requireContext());
        add.setText(R.string.history_add_magic_item);
        add.setOnClickListener(v -> showMagicItemDialog(character));
        container.addView(add);
    }

    private void showMagicItemDialog(CharacterPlayer character) {
        final EditText name = new EditText(requireContext());
        name.setHint(R.string.equipment_name);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        final EditText description = new EditText(requireContext());
        description.setHint(R.string.equipment_description);
        final LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(description);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.history_add_magic_item)
                .setView(layout)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    final MagicObject item = new MagicObject();
                    item.setName(new TranslatedText(
                            name.getText().toString(), name.getText().toString()));
                    item.setDescription(new TranslatedText(
                            description.getText().toString(), description.getText().toString()));
                    character.addMagicItem(item);
                    render();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addHistoryText(CharacterPlayer character) {
        container.addView(header(getString(R.string.history_text_header)));
        final EditText text = new EditText(requireContext());
        text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        text.setText(character.getHistoryText());
        final Button apply = new Button(requireContext());
        apply.setText(R.string.accept);
        apply.setOnClickListener(v -> {
            character.setHistoryText(text.getText().toString());
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        });
        container.addView(text);
        container.addView(apply);
    }

    private void generateRandom() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        final Background background = character.getBackground();
        try {
            int remaining = character.getRemainingBackgroundPoints();
            for (final Skill skill : RulesCatalog.getInstance().getSkills()) {
                if (remaining <= 0) {
                    break;
                }
                if (character.isSkillEnabled(skill) && !background.isSkillPointSelected(skill.getId())) {
                    background.setSkillPoint(skill.getId(), true);
                    remaining--;
                }
            }
            final List<String> languages = languageIds(character);
            if (!languages.isEmpty() && character.getRemainingBackgroundPoints() > 0) {
                final String language = languages.get((int) (Math.random() * languages.size()));
                setLanguageRank(character, language, background.getHistoryLanguageRank(language) + 20);
            }
            if (character.getRemainingBackgroundPoints() > 0) {
                final CharacteristicAbbreviation[] values = CharacteristicAbbreviation.values();
                character.applyBackgroundCharacteristicUpdate(values[(int) (Math.random() * values.length)], new Roll());
            }
            render();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot randomize background", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String elementName(String id, boolean skill) {
        try {
            if (skill) {
                final Skill resolved = RulesCatalog.getInstance().getSkill(id);
                if (resolved != null) {
                    return ElementAdapter.displayName(resolved);
                }
            } else {
                final Category resolved = RulesCatalog.getInstance().getCategory(id);
                if (resolved != null) {
                    return ElementAdapter.displayName(resolved);
                }
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve element " + id, exception);
        }
        return id;
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
        ((TextView) row.findViewById(R.id.item_stat_name)).setText(name);
        ((TextView) row.findViewById(R.id.item_stat_value)).setText(value);
        container.addView(row);
    }
}
