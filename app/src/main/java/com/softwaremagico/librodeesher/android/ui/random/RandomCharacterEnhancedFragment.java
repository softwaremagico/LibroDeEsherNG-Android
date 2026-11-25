package com.softwaremagico.librodeesher.android.ui.random;

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
import com.softwaremagico.librodeesher.android.core.PdfSheetFactory;
import com.softwaremagico.librodeesher.android.persistence.CharacterSaveHelper;
import com.softwaremagico.librodeesher.android.persistence.RandomPreferencesHandler;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.pdf.CharacterPdfExporter;
import com.softwaremagico.librodeesher.persistence.CharacterData;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.io.File;
import java.util.List;
import java.util.Random;

public class RandomCharacterEnhancedFragment extends Fragment {

    private TextView summaryText;
    private Button generateButton;
    private Button saveButton;
    private Button exportButton;
    private Button shareButton;
    private CharacterPlayer lastCharacter;
    private File lastPdfFile;
    private final Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_character_enhanced, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        summaryText = view.findViewById(R.id.random_summary_text);
        generateButton = view.findViewById(R.id.random_generate_button);
        saveButton = view.findViewById(R.id.random_save_button);
        exportButton = view.findViewById(R.id.random_export_button);
        shareButton = view.findViewById(R.id.random_share_button);

        generateButton.setOnClickListener(v -> generateRandomCharacter());
        saveButton.setOnClickListener(v -> saveCharacter());
        exportButton.setOnClickListener(v -> exportPdf());
        shareButton.setOnClickListener(v -> sharePdf());
    }

    private void generateRandomCharacter() {
        try {
            final CharacterPlayer character = CharacterManager.getInstance().newCharacter();
            // The AWT Controller only enabled "Aleatorio" once race, culture and profession were
            // selected. RandomCharacterPlayer#createRandomValues() still expects that precondition,
            // so seed the fresh character with the current selections or with random catalog ones.
            seedSelections(character);
            final RandomPreferencesHandler preferences =
                    RandomPreferencesHandler.getInstance(requireContext());
            final RandomCharacterPlayer generator =
                    new RandomCharacterPlayer(character, preferences.getFinalLevel());
            preferences.configure(generator);
            generator.createRandomValues();
            preferences.afterGeneration(character);
            lastCharacter = character;
            CharacterManager.getInstance().setSelectedCharacter(character);
            requireActivity().invalidateOptionsMenu();
            summaryText.setText(CharacterSummaryBuilder.build(character));
            saveButton.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);
        } catch (Exception exception) {
            AppLog.error("Cannot generate random character", exception);
            summaryText.setText(getString(R.string.error_creating_character, String.valueOf(exception.getMessage())));
        }
    }

    /** Mirrors the legacy precondition check: never hand an unseeded character to the generator. */
    private void seedSelections(CharacterPlayer character) throws Exception {
        final CharacterPlayer current = CharacterManager.getInstance().getSelectedCharacter();
        final RulesCatalog catalog = RulesCatalog.getInstance();
        final String raceId = current != null && current.getRaceId() != null
                ? current.getRaceId() : pick(catalog.getRaces()).getId();
        final String cultureId = current != null && current.getCultureId() != null
                ? current.getCultureId() : pick(catalog.getCultures()).getId();
        final String professionId = current != null && current.getProfessionId() != null
                ? current.getProfessionId() : pick(catalog.getProfessions()).getId();
        character.setRaceId(raceId);
        character.setCultureId(cultureId);
        character.setProfessionId(professionId);
    }

    private <T> T pick(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }

    private void saveCharacter() {
        if (lastCharacter == null) {
            return;
        }
        if (CharacterManager.getInstance().getSelectedCharacter() != lastCharacter) {
            CharacterManager.getInstance().setSelectedCharacter(lastCharacter);
        }
        CharacterSaveHelper.save(requireContext());
    }

    private void exportPdf() {
        if (lastCharacter == null) {
            return;
        }
        try {
            final CharacterData data = CharacterDataMapper.toData(lastCharacter);
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
}
