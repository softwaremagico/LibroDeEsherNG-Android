package com.softwaremagico.librodeesher.android.ui.assistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.core.CharacterSummaryBuilder;
import com.softwaremagico.librodeesher.android.core.CharacterValidator;
import com.softwaremagico.librodeesher.android.core.PdfSheetFactory;
import com.softwaremagico.librodeesher.android.persistence.CharacterSaveHelper;
import com.softwaremagico.librodeesher.android.persistence.RandomPreferencesHandler;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.character.SexType;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.pdf.CharacterPdfExporter;
import com.softwaremagico.librodeesher.persistence.CharacterData;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Guided character creation: every choice is kept while navigating back and forth. */
public class CharacterAssistantFragment extends Fragment {

    private static final String STATE_STEP = "assistantStep";
    private static final String STATE_CHARACTER = "assistantCharacter";
    private static final String STATE_GENERATED = "assistantGenerated";
    private static final int MAX_LEVEL = 20;

    private static final int[] STEP_LABELS = {
            R.string.assistant_step_name,
            R.string.assistant_step_sex,
            R.string.assistant_step_race,
            R.string.assistant_step_culture,
            R.string.assistant_step_profession,
            R.string.assistant_step_age,
            R.string.assistant_step_level,
            R.string.assistant_step_finish
    };

    private TextView stepLabel;
    private TextView summaryText;
    private Button prevButton;
    private Button nextButton;
    private Button finishButton;
    private Button saveButton;
    private Button exportButton;
    private Button shareButton;

    private int currentStep;
    private int finalLevel;
    private CharacterPlayer character;
    private boolean generated;
    private File lastPdfFile;

    private final List<Race> races = new ArrayList<>();
    private final List<Culture> cultures = new ArrayList<>();
    private final List<Profession> professions = new ArrayList<>();
    private final List<Selectable> raceOptions = new ArrayList<>();
    private final List<Selectable> cultureOptions = new ArrayList<>();
    private final List<Selectable> professionOptions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assistant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stepLabel = view.findViewById(R.id.assistant_step_label);
        summaryText = view.findViewById(R.id.assistant_summary_text);
        prevButton = view.findViewById(R.id.assistant_prev_button);
        nextButton = view.findViewById(R.id.assistant_next_button);
        finishButton = view.findViewById(R.id.assistant_finish_button);
        saveButton = view.findViewById(R.id.assistant_save_button);
        exportButton = view.findViewById(R.id.assistant_export_button);
        shareButton = view.findViewById(R.id.assistant_share_button);

        try {
            final RulesCatalog catalog = RulesCatalog.getInstance();
            races.addAll(catalog.getRaces());
            cultures.addAll(catalog.getCultures());
            professions.addAll(catalog.getProfessions());
            for (final Race race : races) {
                raceOptions.add(new RaceOption(race));
            }
            for (final Culture culture : cultures) {
                cultureOptions.add(new CultureOption(culture));
            }
            for (final Profession profession : professions) {
                professionOptions.add(new ProfessionOption(profession));
            }
        } catch (Exception exception) {
            AppLog.error("Cannot load rules", exception);
            summaryText.setText(R.string.error_no_rules);
            return;
        }

        finalLevel = RandomPreferencesHandler.getInstance(requireContext()).getFinalLevel();
        if (savedInstanceState == null) {
            character = CharacterManager.getInstance().newCharacter();
        } else {
            currentStep = savedInstanceState.getInt(STATE_STEP, 0);
            generated = savedInstanceState.getBoolean(STATE_GENERATED, false);
            try {
                final String json = savedInstanceState.getString(STATE_CHARACTER);
                if (json != null) {
                    final CharacterData data = CharacterJsonManager.fromJson(json);
                    character = data == null ? null : CharacterDataMapper.toCharacter(data);
                }
            } catch (Exception exception) {
                AppLog.error("Cannot restore assistant state", exception);
            }
            if (character == null) {
                character = CharacterManager.getInstance().newCharacter();
            }
        }
        CharacterManager.getInstance().setSelectedCharacter(character);
        requireActivity().invalidateOptionsMenu();
        updateUI();

        prevButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateUI();
            }
        });
        nextButton.setOnClickListener(v -> askCurrentStep(true));
        finishButton.setOnClickListener(v -> askCurrentStep(false));

        saveButton.setOnClickListener(v -> saveCharacter());
        exportButton.setOnClickListener(v -> exportPdf());
        shareButton.setOnClickListener(v -> sharePdf());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_STEP, currentStep);
        outState.putBoolean(STATE_GENERATED, generated);
        if (character != null) {
            try {
                outState.putString(STATE_CHARACTER,
                        CharacterJsonManager.toJson(CharacterDataMapper.toData(character)));
            } catch (Exception exception) {
                AppLog.error("Cannot save assistant state", exception);
            }
        }
    }

    private void askCurrentStep(boolean advance) {
        switch (currentStep) {
            case 0:
                showNameDialog(advance);
                break;
            case 1:
                showSexDialog(advance);
                break;
            case 2:
                showElementDialog(R.string.character_race, raceOptions,
                        element -> character.setRaceId(element.getId()), advance);
                break;
            case 3:
                showElementDialog(R.string.character_culture, cultureOptions,
                        element -> character.setCultureId(element.getId()), advance);
                break;
            case 4:
                showElementDialog(R.string.character_profession, professionOptions,
                        element -> character.setProfessionId(element.getId()), advance);
                break;
            case 5:
                showNumberDialog(R.string.assistant_step_age, 1, 500,
                        character.getCurrentAge() > 0 ? character.getCurrentAge() : 25, age -> {
                            character.setCurrentAge(age);
                            character.setFinalAge(age);
                        }, advance);
                break;
            case 6:
                showNumberDialog(R.string.assistant_step_level, 1, MAX_LEVEL, finalLevel, level -> {
                    generated = false;
                    finalLevel = Math.max(1, level);
                }, true);
                break;
            default:
                finishCharacter(finalLevel);
                break;
        }
    }

    private void showNameDialog(boolean advance) {
        final EditText input = new EditText(requireContext());
        input.setText(character.getName());
        input.setHint(R.string.character_name);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.assistant_step_name)
                .setView(input)
                .setPositiveButton(R.string.age_apply, (dialog, which) -> {
                    final String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.assistant_name_required,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    character.setName(name);
                    generated = false;
                    if (advance) {
                        goNext();
                    } else {
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSexDialog(boolean advance) {
        final SexType[] sexes = SexType.values();
        final String[] labels = new String[sexes.length];
        for (int i = 0; i < sexes.length; i++) {
            labels[i] = sexes[i].getTag();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.assistant_step_sex)
                .setItems(labels, (dialog, which) -> {
                    character.setSex(sexes[which]);
                    generated = false;
                    if (advance) {
                        goNext();
                    } else {
                        updateUI();
                    }
                })
                .show();
    }

    private interface Selectable {
        String getId();
        String displayName();
    }

    private void showElementDialog(int title, List<? extends Selectable> elements,
                                   java.util.function.Consumer<Selectable> selection, boolean advance) {
        final String[] labels = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            labels[i] = elements.get(i).displayName();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(labels, (dialog, which) -> {
                    selection.accept(elements.get(which));
                    generated = false;
                    if (advance) {
                        goNext();
                    } else {
                        updateUI();
                    }
                })
                .show();
    }

    private void showNumberDialog(int title, int min, int max, int initial,
                                  java.util.function.IntConsumer value, boolean advance) {
        final NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(min);
        picker.setMaxValue(Math.max(min + 1, max));
        picker.setValue(Math.max(min, Math.min(max, initial)));
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(picker)
                .setPositiveButton(R.string.age_apply, (dialog, which) -> {
                    value.accept(picker.getValue());
                    if (advance) {
                        goNext();
                    } else {
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void goNext() {
        if (currentStep < STEP_LABELS.length - 1) {
            currentStep++;
        }
        updateUI();
    }

    private void updateUI() {
        stepLabel.setText(getString(R.string.assistant_step, currentStep + 1, STEP_LABELS.length,
                getString(STEP_LABELS[currentStep])));
        prevButton.setEnabled(currentStep > 0);
        final boolean finished = currentStep == STEP_LABELS.length - 1;
        nextButton.setVisibility(finished || generated ? View.GONE : View.VISIBLE);
        finishButton.setVisibility(finished && !generated ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(generated ? View.VISIBLE : View.GONE);
        exportButton.setVisibility(generated ? View.VISIBLE : View.GONE);
        shareButton.setVisibility(generated ? View.VISIBLE : View.GONE);
        summaryText.setText(generated ? CharacterSummaryBuilder.build(character) : describeSelection());
    }

    private String describeSelection() {
        final StringBuilder builder = new StringBuilder();
        append(builder, R.string.character_name, character.getName());
        append(builder, R.string.character_sex, character.getSex() == null ? null : character.getSex().getTag());
        append(builder, R.string.character_race, idName(character.getRaceId(), raceOptions));
        append(builder, R.string.character_culture, idName(character.getCultureId(), cultureOptions));
        append(builder, R.string.character_profession, idName(character.getProfessionId(), professionOptions));
        append(builder, R.string.character_age, String.valueOf(character.getCurrentAge()));
        append(builder, R.string.assistant_step_level, String.valueOf(finalLevel));
        return builder.toString();
    }

    private void append(StringBuilder builder, int label, String value) {
        if (value != null && !value.isEmpty()) {
            builder.append(getString(label)).append(": ").append(value).append('\n');
        }
    }

    private String idName(String id, List<? extends Selectable> elements) {
        if (id == null) {
            return null;
        }
        for (final Selectable element : elements) {
            if (id.equals(element.getId())) {
                return element.displayName();
            }
        }
        return id;
    }

    private void finishCharacter(int finalLevel) {
        try {
            if (character.getName() == null || character.getName().isEmpty()) {
                Toast.makeText(requireContext(), R.string.assistant_name_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (character.getSex() == null) {
                Toast.makeText(requireContext(), R.string.assistant_sex_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (character.getRaceId() == null || character.getCultureId() == null
                    || character.getProfessionId() == null) {
                Toast.makeText(requireContext(), R.string.assistant_choices_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!generated) {
                final RandomPreferencesHandler preferences =
                        RandomPreferencesHandler.getInstance(requireContext());
                preferences.applyOrigin(character);
                final RandomCharacterPlayer generator = new RandomCharacterPlayer(character, finalLevel);
                preferences.configure(generator);
                generator.createRandomValues();
                preferences.afterGeneration(character);
                final int problem = CharacterValidator.problem(character);
                if (problem != 0) {
                    Toast.makeText(requireContext(), problem, Toast.LENGTH_SHORT).show();
                    return;
                }
                generated = true;
            }
            CharacterManager.getInstance().setSelectedCharacter(character);
            requireActivity().invalidateOptionsMenu();
            updateUI();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Error finishing character", exception);
            Toast.makeText(requireContext(), R.string.error_creating_character, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCharacter() {
        if (CharacterManager.getInstance().getSelectedCharacter() != character) {
            CharacterManager.getInstance().setSelectedCharacter(character);
            requireActivity().invalidateOptionsMenu();
        }
        CharacterSaveHelper.save(requireContext());
    }

    private void exportPdf() {
        try {
            final CharacterData data = CharacterDataMapper.toData(character);
            final String json = CharacterJsonManager.toJson(data);
            final File pdfDir = new File(requireContext().getCacheDir(), "pdf");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }
            final String fileName = "character_" + System.currentTimeMillis() + ".pdf";
            lastPdfFile = new File(pdfDir, fileName);
            CharacterPdfExporter.exportToFileFromJSON(json, PdfSheetFactory.create(requireContext()),
                    lastPdfFile.getAbsolutePath());
            Toast.makeText(getContext(), R.string.pdf_export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Error exporting PDF", exception);
            Toast.makeText(getContext(), R.string.pdf_export_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf() {
        if (lastPdfFile == null || !lastPdfFile.exists()) {
            exportPdf();
        }
        if (lastPdfFile == null || !lastPdfFile.exists()) {
            return;
        }
        try {
            final Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", lastPdfFile);
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_pdf)));
        } catch (Exception exception) {
            AppLog.error("Error sharing PDF", exception);
        }
    }

    // Adapters for the engine catalog objects, so dialogs show localized names.
    private static final class RaceOption implements Selectable {
        private final Race race;

        private RaceOption(Race race) {
            this.race = race;
        }

        @Override
        public String getId() {
            return race.getId();
        }

        @Override
        public String displayName() {
            return String.valueOf(race.getName());
        }
    }

    private static final class CultureOption implements Selectable {
        private final Culture culture;

        private CultureOption(Culture culture) {
            this.culture = culture;
        }

        @Override
        public String getId() {
            return culture.getId();
        }

        @Override
        public String displayName() {
            return String.valueOf(culture.getName());
        }
    }

    private static final class ProfessionOption implements Selectable {
        private final Profession profession;

        private ProfessionOption(Profession profession) {
            this.profession = profession;
        }

        @Override
        public String getId() {
            return profession.getId();
        }

        @Override
        public String displayName() {
            return String.valueOf(profession.getName());
        }
    }
}
