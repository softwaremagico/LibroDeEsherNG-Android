package com.softwaremagico.librodeesher.android.ui.character;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.core.CharacterSummaryBuilder;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.character.SexType;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Character creation screen: pick a name, sex, race, culture and profession, choose a level and
 * generate a complete character using the LibroDeEsher rules engine.
 */
public class CharacterCreationFragment extends Fragment {

    private static final int MAX_LEVEL = 20;

    private EditText nameInput;
    private EditText ageInput;
    private Spinner sexSpinner;
    private Spinner raceSpinner;
    private Spinner cultureSpinner;
    private Spinner professionSpinner;
    private NumberPicker levelPicker;
    private TextView summaryText;
    private Button generateButton;
    private Button randomButton;
    private Button resetButton;

    private List<SexType> sexes = new ArrayList<>();
    private final List<Race> races = new ArrayList<>();
    private final List<Culture> cultures = new ArrayList<>();
    private final List<Profession> professions = new ArrayList<>();

    private final Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        levelPicker.setMinValue(1);
        levelPicker.setMaxValue(MAX_LEVEL);
        levelPicker.setWrapSelectorWheel(false);
        levelPicker.setValue(1);
        sexes = Arrays.asList(SexType.values());

        try {
            loadRules();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load rules", exception);
            summaryText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            setControlsEnabled(false);
            return;
        }

        populateSpinners();
        restoreFromSession();
        attachListeners();
    }

    private void bindViews(View view) {
        nameInput = view.findViewById(R.id.name_input);
        ageInput = view.findViewById(R.id.age_input);
        sexSpinner = view.findViewById(R.id.sex_spinner);
        raceSpinner = view.findViewById(R.id.race_spinner);
        cultureSpinner = view.findViewById(R.id.culture_spinner);
        professionSpinner = view.findViewById(R.id.profession_spinner);
        levelPicker = view.findViewById(R.id.level_picker);
        summaryText = view.findViewById(R.id.summary_text);
        generateButton = view.findViewById(R.id.generate_button);
        randomButton = view.findViewById(R.id.random_button);
        resetButton = view.findViewById(R.id.reset_button);
    }

    private void loadRules() throws InvalidXmlElementException {
        final RulesCatalog catalog = RulesCatalog.getInstance();
        races.clear();
        races.addAll(catalog.getRaces());
        cultures.clear();
        cultures.addAll(catalog.getCultures());
        professions.clear();
        professions.addAll(catalog.getProfessions());
        races.sort(Comparator.comparing((Race race) -> ElementAdapter.displayName(race)));
        cultures.sort(Comparator.comparing((Culture culture) -> ElementAdapter.displayName(culture)));
        professions.sort(Comparator.comparing((Profession profession) -> ElementAdapter.displayName(profession)));
    }

    private void populateSpinners() {
        sexSpinner.setAdapter(stringAdapter(sexLabels()));
        raceSpinner.setAdapter(new ElementAdapter<>(requireContext(), races));
        cultureSpinner.setAdapter(new ElementAdapter<>(requireContext(), cultures));
        professionSpinner.setAdapter(new ElementAdapter<>(requireContext(), professions));
    }

    private List<String> sexLabels() {
        final List<String> labels = new ArrayList<>();
        for (final SexType sex : sexes) {
            labels.add(sex.getTag());
        }
        return labels;
    }

    private ArrayAdapter<String> stringAdapter(List<String> labels) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void restoreFromSession() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        nameInput.setText(character.getName());
        ageInput.setText(String.valueOf(character.getCurrentAge()));
        levelPicker.setValue(Math.max(1, Math.min(MAX_LEVEL, character.getLevel())));
        selectById(raceSpinner, races, character.getRaceId());
        selectById(cultureSpinner, cultures, character.getCultureId());
        selectById(professionSpinner, professions, character.getProfessionId());
        if (character.getSex() != null) {
            sexSpinner.setSelection(sexes.indexOf(character.getSex()));
        }
    }

    private <T extends com.softwaremagico.librodeesher.Element> void selectById(Spinner spinner, List<T> elements,
                                                                               String id) {
        if (id == null) {
            return;
        }
        for (int i = 0; i < elements.size(); i++) {
            if (id.equals(elements.get(i).getId())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void attachListeners() {
        final AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSessionFromInputs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do.
            }
        };
        sexSpinner.setOnItemSelectedListener(selectionListener);
        raceSpinner.setOnItemSelectedListener(selectionListener);
        cultureSpinner.setOnItemSelectedListener(selectionListener);
        professionSpinner.setOnItemSelectedListener(selectionListener);

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSessionFromInputs();
            }
        };
        nameInput.addTextChangedListener(textWatcher);
        ageInput.addTextChangedListener(textWatcher);

        generateButton.setOnClickListener(v -> generateSelected());
        randomButton.setOnClickListener(v -> generateRandom());
        resetButton.setOnClickListener(v -> resetCharacter());
    }

    private void updateSessionFromInputs() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        final String name = nameInput.getText().toString().trim();
        character.setName(name.isEmpty() ? null : name);
        if (sexSpinner.getSelectedItemPosition() >= 0) {
            character.setSex(sexes.get(sexSpinner.getSelectedItemPosition()));
        }
        final Race race = (Race) raceSpinner.getSelectedItem();
        if (race != null) {
            character.setRaceId(race.getId());
        }
        final Culture culture = (Culture) cultureSpinner.getSelectedItem();
        if (culture != null) {
            character.setCultureId(culture.getId());
        }
        final Profession profession = (Profession) professionSpinner.getSelectedItem();
        if (profession != null) {
            character.setProfessionId(profession.getId());
        }
        final int age = parseAge();
        if (age > 0) {
            character.setCurrentAge(age);
            character.setFinalAge(age);
        }
    }

    private void generateSelected() {
        try {
            final CharacterPlayer character = new CharacterPlayer();
            applySelections(character, true);
            new RandomCharacterPlayer(character, levelPicker.getValue()).createRandomValues();
            afterGeneration(character);
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void generateRandom() {
        if (races.isEmpty() || cultures.isEmpty() || professions.isEmpty()) {
            return;
        }
        try {
            final CharacterPlayer character = new CharacterPlayer();
            final Race race = pickRandom(races);
            final Culture culture = pickRandom(cultures);
            final Profession profession = pickRandom(professions);
            character.setRaceId(race.getId());
            character.setCultureId(culture.getId());
            character.setProfessionId(profession.getId());
            new RandomCharacterPlayer(character, levelPicker.getValue()).createRandomValues();
            selectById(raceSpinner, races, race.getId());
            selectById(cultureSpinner, cultures, culture.getId());
            selectById(professionSpinner, professions, profession.getId());
            afterGeneration(character);
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void applySelections(CharacterPlayer character, boolean includeNameAndSex) {
        if (includeNameAndSex) {
            final String name = nameInput.getText().toString().trim();
            character.setName(name.isEmpty() ? null : name);
            if (sexSpinner.getSelectedItemPosition() >= 0) {
                character.setSex(sexes.get(sexSpinner.getSelectedItemPosition()));
            }
        }
        final Race race = (Race) raceSpinner.getSelectedItem();
        if (race != null) {
            character.setRaceId(race.getId());
        }
        final Culture culture = (Culture) cultureSpinner.getSelectedItem();
        if (culture != null) {
            character.setCultureId(culture.getId());
        }
        final Profession profession = (Profession) professionSpinner.getSelectedItem();
        if (profession != null) {
            character.setProfessionId(profession.getId());
        }
    }

    private void afterGeneration(CharacterPlayer character) {
        final int age = parseAge();
        if (age > 0) {
            character.setCurrentAge(age);
            character.setFinalAge(age);
        }
        CharacterManager.getInstance().setSelectedCharacter(character);
        nameInput.setText(character.getName());
        summaryText.setText(CharacterSummaryBuilder.build(character));
    }

    private void resetCharacter() {
        CharacterManager.getInstance().resetCharacter();
        nameInput.setText("");
        ageInput.setText("");
        levelPicker.setValue(1);
        if (raceSpinner.getAdapter() != null && raceSpinner.getAdapter().getCount() > 0) {
            raceSpinner.setSelection(0);
        }
        if (cultureSpinner.getAdapter() != null && cultureSpinner.getAdapter().getCount() > 0) {
            cultureSpinner.setSelection(0);
        }
        if (professionSpinner.getAdapter() != null && professionSpinner.getAdapter().getCount() > 0) {
            professionSpinner.setSelection(0);
        }
        summaryText.setText(R.string.character_not_created);
    }

    private int parseAge() {
        try {
            return Integer.parseInt(ageInput.getText().toString().trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private <T> T pickRandom(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }

    private void showError(Exception exception) {
        AppLog.error("Cannot create character", exception);
        summaryText.setText(getString(R.string.error_creating_character, String.valueOf(exception.getMessage())));
    }

    private void setControlsEnabled(boolean enabled) {
        generateButton.setEnabled(enabled);
        randomButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }
}
