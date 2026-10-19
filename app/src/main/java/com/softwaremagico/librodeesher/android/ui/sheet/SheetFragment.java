package com.softwaremagico.librodeesher.android.ui.sheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntityDao;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.export.txt.TxtSheet;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;

import java.util.List;

/**
 * Character sheet screen: renders the whole selected character with the LibroDeEsher rules engine
 * ({@link TxtSheet}) and allows saving it to / loading it from the local database.
 */
public class SheetFragment extends Fragment {

    private static final int MAX_LEVEL = 20;

    private TextView sheetText;
    private CharacterEntityDao characterDao;

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
        final Button levelUpButton = view.findViewById(R.id.sheet_level_up_button);
        final Button skillsButton = view.findViewById(R.id.sheet_skills_button);
        final Button spellsButton = view.findViewById(R.id.sheet_spells_button);
        final Button perksButton = view.findViewById(R.id.sheet_perks_button);
        saveButton.setOnClickListener(v -> saveCharacter());
        loadButton.setOnClickListener(v -> showLoadDialog());
        deleteButton.setOnClickListener(v -> showDeleteDialog());
        shareButton.setOnClickListener(v -> shareCharacter());
        levelUpButton.setOnClickListener(v -> showLevelUpDialog());
        skillsButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_sheet_to_skills));
        spellsButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_sheet_to_spells));
        perksButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_sheet_to_perks));
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
        final CharacterManager manager = CharacterManager.getInstance();
        final CharacterPlayer character = manager.getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(requireContext(), R.string.sheet_no_character, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final CharacterEntity entity = new CharacterEntity(character);
            if (manager.getSelectedCharacterId() != CharacterManager.NO_ID) {
                entity.setId(manager.getSelectedCharacterId());
            }
            final long id = characterDao.insert(entity);
            manager.setSelectedCharacterId(id);
            Toast.makeText(requireContext(), R.string.sheet_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot save character", exception);
            Toast.makeText(requireContext(), R.string.sheet_save_error, Toast.LENGTH_SHORT).show();
        }
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
        final CharacterManager manager = CharacterManager.getInstance();
        manager.setSelectedCharacter(character);
        manager.setSelectedCharacterId(entity.getId());
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
                .setPositiveButton(R.string.accept, (dialog, which) -> deleteCharacter(entity))
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

    private void showLevelUpDialog() {
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
