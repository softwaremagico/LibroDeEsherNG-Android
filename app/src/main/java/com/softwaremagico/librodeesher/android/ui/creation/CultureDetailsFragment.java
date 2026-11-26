package com.softwaremagico.librodeesher.android.ui.creation;

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
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.race.RaceLanguage;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.training.TrainingCategoryGrant;
import com.softwaremagico.librodeesher.training.TrainingSkillGrant;
import com.softwaremagico.librodeesher.training.TrainingSpellListGrant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Guided culture screen: adolescence ranks, hobbies, languages and typical weapon order. */
public class CultureDetailsFragment extends Fragment {

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_culture_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.culture_container);
        view.findViewById(R.id.culture_random).setOnClickListener(v -> generateAll());
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null || character.getCultureId() == null) {
            container.addView(label(getString(R.string.sheet_no_character)));
            return;
        }
        final Culture culture;
        final Race race;
        try {
            culture = character.getCulture();
            race = character.getRace();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load culture", exception);
            container.addView(label(getString(R.string.error_no_rules, String.valueOf(exception.getMessage()))));
            return;
        }
        if (culture == null) {
            container.addView(label(getString(R.string.sheet_no_character)));
            return;
        }
        container.addView(header(ElementAdapter.displayName(culture)));
        if (culture.getTypicalWeaponIds() != null && !culture.getTypicalWeaponIds().isEmpty()) {
            container.addView(header(getString(R.string.culture_weapons)));
            container.addView(label(String.join(" → ", culture.getTypicalWeaponIds())));
        }

        final List<TrainingCategoryGrant> grants = culture.getAdolescenceRanks();
        if (grants != null && !grants.isEmpty()) {
            container.addView(header(getString(R.string.culture_adolescence)));
            final List<Spinner> spinners = new ArrayList<>();
            for (int i = 0; i < grants.size(); i++) {
                final List<String> options = grants.get(i).getCategoryOptions();
                if (options == null || options.isEmpty()) {
                    continue;
                }
                container.addView(label(getString(R.string.culture_grant, i + 1)));
                if (options.size() > 1) {
                    final Spinner spinner = new Spinner(requireContext());
                    spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, options));
                    spinners.add(spinner);
                    container.addView(spinner);
                } else {
                    container.addView(label(options.get(0)));
                }
            }
            // Advisor-like flow: selecting a spinner applies the decision immediately.
            for (final Spinner spinner : spinners) {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        applyAdolescence(character, culture, grants, spinners);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Keep the last applied selection.
                    }
                });
            }
            if (spinners.isEmpty()) {
                applyAdolescence(character, culture, grants, spinners);
            }
        }

        container.addView(header(getString(R.string.culture_languages)));
        final List<String> languages;
        try {
            languages = languageIds(character, race, culture);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot list languages", exception);
            container.addView(label(getString(R.string.error_no_rules, String.valueOf(exception.getMessage()))));
            return;
        }
        addLanguageSpinners(character, race, culture, languages);
        if (languages.isEmpty()) {
            container.addView(label(getString(R.string.culture_no_languages)));
        }

        container.addView(header(getString(R.string.culture_hobbies)));
        addHobbies(character, culture);
    }

    private void applyAdolescence(CharacterPlayer character, Culture culture,
                                  List<TrainingCategoryGrant> grants, List<Spinner> spinners) {
        final Map<Integer, String> categories = new HashMap<>();
        int index = 0;
        for (int i = 0; i < grants.size(); i++) {
            final List<String> options = grants.get(i).getCategoryOptions();
            if (options == null || options.isEmpty()) {
                continue;
            }
            categories.put(i, options.size() > 1 && index < spinners.size()
                    ? (String) spinners.get(index++).getSelectedItem() : options.get(0));
        }
        try {
            character.applyCultureAdolescenceRanks(culture, categories, null, null);
            requireActivity().invalidateOptionsMenu();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot apply adolescence ranks", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void addLanguageSpinners(CharacterPlayer character, Race race, Culture culture,
                                     List<String> languages) {
        if (languages.isEmpty()) {
            return;
        }
        if (race != null && race.getOptionalRaceLanguages() != null) {
            for (int i = 0; i < race.getOptionalRaceLanguages().size(); i++) {
                final int slot = i;
                addLanguageRow(getString(R.string.culture_race_language, i + 1), languages,
                        character.getOptionalRaceLanguageAssignment(i),
                        selected -> character.assignOptionalRaceLanguage(slot, selected));
            }
        }
        if (culture.getOptionalLanguages() != null) {
            for (int i = 0; i < culture.getOptionalLanguages().size(); i++) {
                final int slot = i;
                addLanguageRow(getString(R.string.culture_culture_language, i + 1), languages,
                        character.getOptionalCultureLanguageAssignment(i),
                        selected -> character.assignOptionalCultureLanguage(slot, selected));
            }
        }
    }

    private void addLanguageRow(String title, List<String> languages, String current,
                                java.util.function.Consumer<String> assignment) {
        container.addView(label(title));
        final Spinner spinner = new Spinner(requireContext());
        final List<String> options = new ArrayList<>(languages);
        if (current != null && !options.contains(current)) {
            options.add(0, current);
        }
        spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, options));
        if (current != null) {
            spinner.setSelection(options.indexOf(current));
        }
        container.addView(spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                assignment.accept((String) spinner.getSelectedItem());
                requireActivity().invalidateOptionsMenu();
                Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Keep the last applied selection.
            }
        });
    }

    private void addHobbies(CharacterPlayer character, Culture culture) {
        final List<String> hobbies = culture.getHobbyIds();
        if (hobbies == null || hobbies.isEmpty()) {
            container.addView(label(getString(R.string.culture_no_hobbies)));
            return;
        }
        final int maxRanks = culture.getHobbyRanks() == null ? 0 : culture.getHobbyRanks();
        for (final String hobbyId : hobbies) {
            final boolean spellList = isSpellList(hobbyId);
            final int current = spellList ? character.getHobbySpellListRank(hobbyId)
                    : character.getHobbySkillRank(hobbyId);
            final TextView value = label(getString(R.string.culture_hobby_rank,
                    ElementAdapter.displayName(resolve(hobbyId, spellList)), current));
            container.addView(value);
            final LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            final Button minus = new Button(requireContext());
            minus.setText(R.string.trainings_remove);
            final Button plus = new Button(requireContext());
            plus.setText(R.string.trainings_add);
            row.addView(minus);
            row.addView(plus);
            container.addView(row);
            minus.setOnClickListener(v -> {
                changeHobby(character, hobbyId, spellList, current - 1);
                render();
            });
            plus.setOnClickListener(v -> {
                changeHobby(character, hobbyId, spellList, current + 1);
                render();
            });
            plus.setEnabled(current < maxRanks);
            minus.setEnabled(current > 0);
        }
    }

    private void changeHobby(CharacterPlayer character, String hobbyId, boolean spellList, int ranks) {
        if (spellList) {
            character.setHobbySpellListRank(hobbyId, Math.max(0, ranks));
        } else {
            character.setHobbySkillRank(hobbyId, Math.max(0, ranks));
        }
        requireActivity().invalidateOptionsMenu();
    }

    private com.softwaremagico.librodeesher.Element resolve(String id, boolean spellList) {
        try {
            return spellList ? RulesCatalog.getInstance().getSpellList(id)
                    : RulesCatalog.getInstance().getSkill(id);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve hobby " + id, exception);
            return null;
        }
    }

    private boolean isSpellList(String id) {
        try {
            return RulesCatalog.getInstance().getSpellList(id) != null;
        } catch (InvalidXmlElementException exception) {
            return false;
        }
    }

    private List<String> languageIds(CharacterPlayer character, Race race, Culture culture) throws InvalidXmlElementException {
        final Set<String> ids = new LinkedHashSet<>();
        for (Race candidate : RulesCatalog.getInstance().getRaces()) {
            if (candidate.getRaceLanguages() != null) {
                for (RaceLanguage language : candidate.getRaceLanguages()) {
                    ids.add(language.getLanguageId());
                }
            }
            if (candidate.getBackgroundLanguages() != null) {
                for (RaceLanguage language : candidate.getBackgroundLanguages()) {
                    ids.add(language.getLanguageId());
                }
            }
        }
        if (culture.getLanguageMaxRanks() != null) {
            for (com.softwaremagico.librodeesher.culture.CultureLanguageRank rank : culture.getLanguageMaxRanks()) {
                ids.add(rank.getLanguageId());
            }
        }
        return new ArrayList<>(ids);
    }

    private void generateAll() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getCultureId() == null) {
            return;
        }
        try {
            final Culture culture = character.getCulture();
            final List<TrainingCategoryGrant> grants = culture.getAdolescenceRanks();
            final Map<Integer, String> categories = new HashMap<>();
            final Map<Integer, List<String>> skills = new HashMap<>();
            final Map<Integer, List<String>> spellLists = new HashMap<>();
            if (grants != null) {
                for (int i = 0; i < grants.size(); i++) {
                    final TrainingCategoryGrant grant = grants.get(i);
                    if (grant.getCategoryOptions() != null && !grant.getCategoryOptions().isEmpty()) {
                        categories.put(i, grant.getCategoryOptions().get(0));
                    }
                    if (grant.getSkills() != null) {
                        for (int j = 0; j < grant.getSkills().size(); j++) {
                            final TrainingSkillGrant skill = grant.getSkills().get(j);
                            if (skill.getSkillOptions() != null && !skill.getSkillOptions().isEmpty()) {
                                skills.computeIfAbsent(i, key -> new ArrayList<>()).add(skill.getSkillOptions().get(0));
                            }
                        }
                    }
                    if (grant.getSpellLists() != null) {
                        for (int j = 0; j < grant.getSpellLists().size(); j++) {
                            final TrainingSpellListGrant list = grant.getSpellLists().get(j);
                            if (list.getSpellListOptions() != null && !list.getSpellListOptions().isEmpty()) {
                                spellLists.computeIfAbsent(i, key -> new ArrayList<>())
                                        .add(list.getSpellListOptions().get(0));
                            }
                        }
                    }
                }
            }
            character.applyCultureAdolescenceRanks(culture, categories, skills, null);
            final List<String> languages = languageIds(character, character.getRace(), culture);
            int languageIndex = 0;
            final Race race = character.getRace();
            if (race != null && race.getOptionalRaceLanguages() != null && !languages.isEmpty()) {
                for (int i = 0; i < race.getOptionalRaceLanguages().size() && languageIndex < languages.size(); i++) {
                    if (character.getOptionalRaceLanguageAssignment(i) == null) {
                        character.assignOptionalRaceLanguage(i, languages.get(languageIndex++));
                    }
                }
            }
            if (culture.getOptionalLanguages() != null) {
                for (int i = 0; i < culture.getOptionalLanguages().size() && languageIndex < languages.size(); i++) {
                    if (character.getOptionalCultureLanguageAssignment(i) == null) {
                        character.assignOptionalCultureLanguage(i, languages.get(languageIndex++));
                    }
                }
            }
            final List<String> hobbies = culture.getHobbyIds();
            final int maxRanks = culture.getHobbyRanks() == null ? 0 : culture.getHobbyRanks();
            if (hobbies != null) {
                for (final String hobbyId : hobbies) {
                    changeHobby(character, hobbyId, isSpellList(hobbyId), maxRanks);
                }
            }
            requireActivity().invalidateOptionsMenu();
            render();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot randomize culture", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private TextView label(String text) {
        final TextView label = new TextView(requireContext());
        label.setText(text);
        label.setPadding(0, 8, 0, 8);
        return label;
    }
}
