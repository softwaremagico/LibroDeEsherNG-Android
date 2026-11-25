package com.softwaremagico.librodeesher.android.ui.sheet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntityDao;
import com.softwaremagico.librodeesher.android.persistence.CharacterFileHelper;
import com.softwaremagico.librodeesher.android.persistence.CharacterSaveHelper;
import com.softwaremagico.librodeesher.android.core.CharacterValidator;
import com.softwaremagico.librodeesher.android.core.PdfSheetFactory;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.export.txt.TxtSheet;
import com.softwaremagico.librodeesher.persistence.CharacterDataMapper;
import com.softwaremagico.librodeesher.persistence.CharacterJsonManager;
import com.softwaremagico.librodeesher.pdf.CharacterPdfExporter;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;

import java.io.File;
import java.util.List;

/**
 * Character sheet screen: renders the whole selected character with the LibroDeEsher rules engine
 * ({@link TxtSheet}) and allows saving it to / loading it from the local database.
 */
public class SheetFragment extends Fragment {

    private static final int MAX_LEVEL = 20;

    private TextView sheetText;
    private CharacterEntityDao characterDao;
    private File lastPdfFile;

    private final ActivityResultLauncher<String> exportCharacterLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"), this::exportCharacterTo);
    private final ActivityResultLauncher<String> exportLevelLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"), this::exportLevelTo);
    private final ActivityResultLauncher<String[]> importCharacterLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> importFrom(uri, true));
    private final ActivityResultLauncher<String[]> importLevelLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> importFrom(uri, false));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sheetText = view.findViewById(R.id.sheet_text);
        characterDao = AppDatabase.getInstance(requireContext()).getCharacterEntityDao();
        final Button saveButton = view.findViewById(R.id.sheet_save_button);
        final Button loadButton = view.findViewById(R.id.sheet_load_button);
        final Button deleteButton = view.findViewById(R.id.sheet_delete_button);
        final Button shareButton = view.findViewById(R.id.sheet_share_button);
        final Button shareAbbreviatedButton = view.findViewById(R.id.sheet_share_abbreviated_button);
        final Button levelUpButton = view.findViewById(R.id.sheet_level_up_button);
        final Button exportPdfButton = view.findViewById(R.id.sheet_export_pdf_button);
        final Button sharePdfButton = view.findViewById(R.id.sheet_share_pdf_button);
        final Button exportCharacterButton = view.findViewById(R.id.sheet_export_character_button);
        final Button importCharacterButton = view.findViewById(R.id.sheet_import_character_button);
        final Button exportLevelButton = view.findViewById(R.id.sheet_export_level_button);
        final Button importLevelButton = view.findViewById(R.id.sheet_import_level_button);
        saveButton.setOnClickListener(v -> saveCharacter());
        loadButton.setOnClickListener(v -> showLoadDialog());
        deleteButton.setOnClickListener(v -> showDeleteDialog());
        shareButton.setOnClickListener(v -> shareCharacter());
        shareAbbreviatedButton.setOnClickListener(v -> shareAbbreviatedCharacter());
        levelUpButton.setOnClickListener(v -> showLevelUpDialog());
        exportPdfButton.setOnClickListener(v -> exportPdf());
        sharePdfButton.setOnClickListener(v -> sharePdf());
        exportCharacterButton.setOnClickListener(v -> launchExport(true));
        importCharacterButton.setOnClickListener(v -> importCharacterLauncher.launch(new String[]{"*/*"}));
        exportLevelButton.setOnClickListener(v -> launchExport(false));
        importLevelButton.setOnClickListener(v -> importLevelLauncher.launch(new String[]{"*/*"}));
    }

    /** Blocks save/export/level-up until the character is well formed, as in the desktop app. */
    private boolean requireWellFormedCharacter() {
        final int problem = CharacterValidator.problem(CharacterManager.getInstance().getSelectedCharacter());
        if (problem != 0) {
            Toast.makeText(requireContext(), problem, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void launchExport(boolean character) {
        if (!requireWellFormedCharacter()) {
            return;
        }
        final CharacterPlayer selected = CharacterManager.getInstance().getSelectedCharacter();
        if (selected.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        final String name = selected.getName() == null ? "personaje" : selected.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        if (character) {
            exportCharacterLauncher.launch(name + ".json");
        } else {
            exportLevelLauncher.launch(name + "_nivel.json");
        }
    }

    private void exportCharacterTo(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            CharacterFileHelper.exportCharacter(requireContext(), uri,
                    CharacterManager.getInstance().getSelectedCharacter());
            Toast.makeText(requireContext(), R.string.sheet_file_exported, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot export character", exception);
            Toast.makeText(requireContext(), R.string.sheet_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void exportLevelTo(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            CharacterFileHelper.exportLevel(requireContext(), uri,
                    CharacterManager.getInstance().getSelectedCharacter());
            Toast.makeText(requireContext(), R.string.sheet_file_exported, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot export level", exception);
            Toast.makeText(requireContext(), R.string.sheet_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void importFrom(Uri uri, boolean character) {
        if (uri == null) {
            return;
        }
        try {
            if (character) {
                final CharacterPlayer imported = CharacterFileHelper.importCharacter(requireContext(), uri);
                CharacterManager.getInstance().setSelectedCharacter(imported);
                requireActivity().invalidateOptionsMenu();
                Toast.makeText(requireContext(), R.string.sheet_file_imported, Toast.LENGTH_SHORT).show();
            } else {
                CharacterFileHelper.importLevel(requireContext(), uri,
                        CharacterManager.getInstance().getSelectedCharacter());
                Toast.makeText(requireContext(), R.string.sheet_file_imported, Toast.LENGTH_SHORT).show();
            }
            lastPdfFile = null;
            renderCharacter();
        } catch (Exception exception) {
            AppLog.error("Cannot import file", exception);
            Toast.makeText(requireContext(), character ? R.string.sheet_file_error
                    : R.string.sheet_level_import_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderCharacter();
    }

    private void renderCharacter() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            sheetText.setText(R.string.sheet_no_character);
            return;
        }
        try {
            sheetText.setText(TxtSheet.getCharacterStandardSheetAsText(character));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot render character sheet", exception);
            sheetText.setText(getString(R.string.error_creating_character, String.valueOf(exception.getMessage())));
        }
    }

    private void saveCharacter() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        CharacterSaveHelper.save(requireContext());
        requireActivity().invalidateOptionsMenu();
    }

    private void showLoadDialog() {
        final List<CharacterEntity> characters = characterDao.getAll();
        if (characters.isEmpty()) {
            Toast.makeText(requireContext(), R.string.sheet_no_saved_characters, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sheet_load)
                .setItems(labelsFor(characters), (dialog, which) -> loadCharacter(characters.get(which)))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void loadCharacter(CharacterEntity entity) {
        final CharacterPlayer character = entity.getCharacterPlayer();
        if (character == null) {
            Toast.makeText(requireContext(), R.string.sheet_load_error, Toast.LENGTH_SHORT).show();
            return;
        }
        CharacterManager.getInstance().openCharacter(character, entity.getId());
        requireActivity().invalidateOptionsMenu();
        renderCharacter();
    }

    private void showDeleteDialog() {
        final List<CharacterEntity> characters = characterDao.getAll();
        if (characters.isEmpty()) {
            Toast.makeText(requireContext(), R.string.sheet_no_saved_characters, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sheet_delete)
                .setItems(labelsFor(characters), (dialog, which) -> confirmDelete(characters.get(which)))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void confirmDelete(CharacterEntity entity) {
        final String name = entity.getName() == null ? "" : entity.getName();
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.sheet_delete_confirm, name))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteCharacter(entity))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteCharacter(CharacterEntity entity) {
        try {
            characterDao.delete(entity);
            if (CharacterManager.getInstance().getSelectedCharacterId() == entity.getId()) {
                CharacterManager.getInstance().setSelectedCharacterId(CharacterManager.NO_ID);
            }
            Toast.makeText(requireContext(), R.string.sheet_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot delete character", exception);
            Toast.makeText(requireContext(), R.string.sheet_delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareCharacter() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final String text = TxtSheet.getCharacterStandardSheetAsText(character);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, character.getName());
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, getString(R.string.sheet_share_chooser)));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot share character sheet", exception);
            Toast.makeText(requireContext(), R.string.sheet_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Shares the monster-style abbreviated summary of the selected character. */
    private void shareAbbreviatedCharacter() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final String text = TxtSheet.getCharacterAbbreviatureAsText(character);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, character.getName());
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, getString(R.string.sheet_share_chooser)));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot share abbreviated sheet", exception);
            Toast.makeText(requireContext(), R.string.sheet_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Exports the currently selected character using the configured PDF layout. */
    private void exportPdf() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final String json = CharacterJsonManager.toJson(CharacterDataMapper.toData(character));
            final File pdfDir = new File(requireContext().getCacheDir(), "pdf");
            if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                throw new IllegalStateException("Cannot create " + pdfDir);
            }
            lastPdfFile = new File(pdfDir, "character_" + System.currentTimeMillis() + ".pdf");
            CharacterPdfExporter.exportToFileFromJSON(json, PdfSheetFactory.create(requireContext()),
                    lastPdfFile.getAbsolutePath());
            Toast.makeText(requireContext(), R.string.pdf_export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Error exporting PDF", exception);
            Toast.makeText(requireContext(), R.string.pdf_export_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        if (lastPdfFile == null || !lastPdfFile.exists()) {
            exportPdf();
        }
        if (lastPdfFile == null || !lastPdfFile.exists()) {
            return;
        }
        try {
            final Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", lastPdfFile);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.share_pdf)));
        } catch (Exception exception) {
            AppLog.error("Error sharing PDF", exception);
            Toast.makeText(requireContext(), R.string.pdf_export_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLevelUpDialog() {
        if (!requireWellFormedCharacter()) {
            return;
        }
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        if (character.getLevel() >= MAX_LEVEL) {
            Toast.makeText(requireContext(), R.string.sheet_level_up_max, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sheet_level_up_title)
                .setItems(new CharSequence[]{getString(R.string.sheet_level_up_auto),
                        getString(R.string.sheet_level_up_manual)}, (dialog, which) -> {
                    if (which == 0) {
                        levelUpAuto();
                    } else {
                        levelUpManual();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void levelUpAuto() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        try {
            character.increaseLevel();
            new RandomCharacterPlayer(character).setDevelopmentPoints();
            renderCharacter();
            requireActivity().invalidateOptionsMenu();
            Toast.makeText(requireContext(), getString(R.string.sheet_level_up_done, character.getLevel()),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot level up character", exception);
            Toast.makeText(requireContext(), R.string.sheet_level_up_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void levelUpManual() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        try {
            character.increaseLevel();
            renderCharacter();
            requireActivity().invalidateOptionsMenu();
            Toast.makeText(requireContext(),
                    getString(R.string.sheet_level_up_manual_hint, character.getLevel()),
                    Toast.LENGTH_LONG).show();
        } catch (Exception exception) {
            AppLog.error("Cannot level up character", exception);
            Toast.makeText(requireContext(), R.string.sheet_level_up_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String[] labelsFor(List<CharacterEntity> characters) {
        final String[] labels = new String[characters.size()];
        for (int i = 0; i < characters.size(); i++) {
            final CharacterEntity entity = characters.get(i);
            labels[i] = entity.getName() + " (" + entity.getRace() + ", N" + entity.getLevel() + ")";
        }
        return labels;
    }
}
