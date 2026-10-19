package com.softwaremagico.librodeesher.android.ui.sheet;

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

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntityDao;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.export.txt.TxtSheet;

import java.util.List;

/**
 * Character sheet screen: renders the whole selected character with the LibroDeEsher rules engine
 * ({@link TxtSheet}) and allows saving it to / loading it from the local database.
 */
public class SheetFragment extends Fragment {

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
        saveButton.setOnClickListener(v -> saveCharacter());
        loadButton.setOnClickListener(v -> showLoadDialog());
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
        final String[] labels = new String[characters.size()];
        for (int i = 0; i < characters.size(); i++) {
            final CharacterEntity entity = characters.get(i);
            labels[i] = entity.getName() + " (" + entity.getRace() + ", N" + entity.getLevel() + ")";
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sheet_load)
                .setItems(labels, (dialog, which) -> loadCharacter(characters.get(which)))
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
}
