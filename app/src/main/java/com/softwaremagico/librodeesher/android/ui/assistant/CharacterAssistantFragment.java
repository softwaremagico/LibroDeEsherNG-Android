package com.softwaremagico.librodeesher.android.ui.assistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.core.CharacterSummaryBuilder;
import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.pdf.CharacterPdfExporter;
import com.softwaremagico.librodeesher.pdf.StandardCharacterSheet;
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
import java.util.Random;

public class CharacterAssistantFragment extends Fragment {

    private static final String[] STEPS = {
            "Paso 1: Seleccionar Raza",
            "Paso 2: Seleccionar Cultura",
            "Paso 3: Seleccionar Profesión",
            "Paso 4: Generar Características",
            "Paso 5: Completar y Guardar"
    };

    private TextView stepLabel;
    private TextView summaryText;
    private Button prevButton;
    private Button nextButton;
    private Button finishButton;
    private Button saveButton;
    private Button exportButton;
    private Button shareButton;

    private int currentStep = 0;
    private CharacterPlayer character;
    private File lastPdfFile;

    private final List<Race> races = new ArrayList<>();
    private final List<Culture> cultures = new ArrayList<>();
    private final List<Profession> professions = new ArrayList<>();
    private final Random random = new Random();

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
        } catch (Exception exception) {
            AppLog.error("Cannot load rules", exception);
            summaryText.setText("Error cargando reglas");
            return;
        }

        character = new CharacterPlayer();
        CharacterManager.getInstance().setSelectedCharacter(character);

        updateUI();

        prevButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateUI();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentStep < STEPS.length - 1) {
                applyStep();
                currentStep++;
                updateUI();
            }
        });

        finishButton.setOnClickListener(v -> {
            applyStep();
            finishCharacter();
        });

        saveButton.setOnClickListener(v -> saveCharacter());
        exportButton.setOnClickListener(v -> exportPdf());
        shareButton.setOnClickListener(v -> sharePdf());
    }

    private void updateUI() {
        stepLabel.setText(STEPS[currentStep]);
        prevButton.setEnabled(currentStep > 0);
        nextButton.setEnabled(currentStep < STEPS.length - 1);
        finishButton.setVisibility(currentStep == STEPS.length - 1 ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(View.GONE);
        exportButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
        if (currentStep == 0) {
            summaryText.setText("Seleccione una raza para su personaje.");
        } else if (currentStep == 1) {
            summaryText.setText("Seleccione una cultura para su personaje.");
        } else if (currentStep == 2) {
            summaryText.setText("Seleccione una profesión para su personaje.");
        } else if (currentStep == 3) {
            summaryText.setText("Las características se generarán automáticamente.");
        } else if (currentStep == 4) {
            summaryText.setText(CharacterSummaryBuilder.build(character));
        }
    }

    private void applyStep() {
        try {
            if (currentStep == 0 && !races.isEmpty()) {
                character.setRaceId(races.get(random.nextInt(races.size())).getId());
            } else if (currentStep == 1 && !cultures.isEmpty()) {
                character.setCultureId(cultures.get(random.nextInt(cultures.size())).getId());
            } else if (currentStep == 2 && !professions.isEmpty()) {
                character.setProfessionId(professions.get(random.nextInt(professions.size())).getId());
            } else if (currentStep == 3) {
                new RandomCharacterPlayer(character, 1).createRandomValues();
            }
        } catch (Exception exception) {
            AppLog.error("Error applying step", exception);
            Toast.makeText(getContext(), "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void finishCharacter() {
        try {
            if (character.getRaceId() == null && !races.isEmpty()) {
                character.setRaceId(races.get(random.nextInt(races.size())).getId());
            }
            if (character.getCultureId() == null && !cultures.isEmpty()) {
                character.setCultureId(cultures.get(random.nextInt(cultures.size())).getId());
            }
            if (character.getProfessionId() == null && !professions.isEmpty()) {
                character.setProfessionId(professions.get(random.nextInt(professions.size())).getId());
            }
            new RandomCharacterPlayer(character, 1).createRandomValues();
            CharacterManager.getInstance().setSelectedCharacter(character);
            summaryText.setText(CharacterSummaryBuilder.build(character));
            saveButton.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);
            finishButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            prevButton.setVisibility(View.GONE);
        } catch (Exception exception) {
            AppLog.error("Error finishing character", exception);
            Toast.makeText(getContext(), R.string.error_creating_character, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCharacter() {
        try {
            final CharacterData data = CharacterDataMapper.toData(character);
            final String json = CharacterJsonManager.toJson(data);
            final CharacterEntity entity = new CharacterEntity(character);
            entity.json = json;
            AppDatabase.getInstance(requireContext()).getCharacterEntityDao().insert(entity);
            Toast.makeText(getContext(), R.string.character_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Error saving character", exception);
            Toast.makeText(getContext(), R.string.error_no_rules, Toast.LENGTH_SHORT).show();
        }
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
            CharacterPdfExporter.exportToFileFromJSON(json, new StandardCharacterSheet(), lastPdfFile.getAbsolutePath());
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
}
