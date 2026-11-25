package com.softwaremagico.librodeesher.android.ui.options;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.file.ModuleManager;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Legacy-style options window: rules, PDF output, debug mode and rulebook modules. */
public class OptionsFragment extends Fragment {

    private final Map<String, CheckBox> moduleBoxes = new LinkedHashMap<>();
    private CheckBox firearms;
    private CheckBox chi;
    private CheckBox otherRealmTraining;
    private CheckBox magic;
    private CheckBox darkSpells;
    private CheckBox perksCostBackground;
    private CheckBox handwriting;
    private CheckBox sortPdfSkills;
    private CheckBox debugMode;
    private EditText categoryMaxCost;
    private Spinner pdfMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firearms = view.findViewById(R.id.option_firearms);
        chi = view.findViewById(R.id.option_chi);
        otherRealmTraining = view.findViewById(R.id.option_other_realm_training);
        magic = view.findViewById(R.id.option_magic);
        darkSpells = view.findViewById(R.id.option_dark_spells);
        perksCostBackground = view.findViewById(R.id.option_perks_cost_background);
        handwriting = view.findViewById(R.id.option_handwriting);
        sortPdfSkills = view.findViewById(R.id.option_sort_pdf_skills);
        debugMode = view.findViewById(R.id.option_debug_mode);
        categoryMaxCost = view.findViewById(R.id.option_category_max_cost);
        pdfMode = view.findViewById(R.id.option_pdf_mode);

        final OptionsHandler handler = OptionsHandler.getInstance(requireContext());
        // The legacy OptionsWindow reflected the open character, not just the global defaults, so
        // an imported/edited sheet does not silently lose its own flags when options are saved.
        final com.softwaremagico.librodeesher.character.CharacterPlayer character =
                CharacterManager.getInstance().getSelectedCharacter();
        firearms.setChecked(character.isFirearmsAllowed());
        chi.setChecked(character.isChiPowersAllowed());
        otherRealmTraining.setChecked(character.isOtherRealmTrainingSpellsAllowed());
        magic.setChecked(character.isMagicAllowed());
        darkSpells.setChecked(character.isDarkSpellsAsBasicListsAllowed());
        perksCostBackground.setChecked(character.isPerksCostBackgroundPoints());
        handwriting.setChecked(character.isHandWritingFont());
        sortPdfSkills.setChecked(character.isSortPdfSkills());
        debugMode.setChecked(handler.isDebugMode());
        categoryMaxCost.setText(String.valueOf(character.getCategoryMaxCost()));
        pdfMode.setSelection(handler.getPdfMode());

        final LinearLayout modulesContainer = view.findViewById(R.id.option_modules_container);
        final Set<String> disabled = handler.getDisabledModules();
        for (final String module : ModuleManager.getAllModules()) {
            if (ModuleManager.CORE.equals(module)) {
                continue;
            }
            final CheckBox box = new CheckBox(requireContext());
            box.setText(module);
            box.setChecked(!disabled.contains(module));
            moduleBoxes.put(module, box);
            modulesContainer.addView(box);
        }
        view.findViewById(R.id.option_save).setOnClickListener(v -> save(handler));
    }

    private void save(OptionsHandler handler) {
        final int cost = parseCost();
        final Set<String> disabled = new HashSet<>();
        for (final Map.Entry<String, CheckBox> entry : moduleBoxes.entrySet()) {
            if (!entry.getValue().isChecked()) {
                disabled.add(entry.getKey());
            }
        }
        handler.store(firearms.isChecked(), chi.isChecked(), otherRealmTraining.isChecked(),
                magic.isChecked(), darkSpells.isChecked(), handwriting.isChecked(),
                sortPdfSkills.isChecked(), perksCostBackground.isChecked(), cost,
                pdfMode.getSelectedItemPosition(), debugMode.isChecked(), disabled);
        CharacterManager.getInstance().setOptionsHandler(handler);
        handler.applyTo(CharacterManager.getInstance().getSelectedCharacter());
        AppLog.setDebugMode(debugMode.isChecked());
        Toast.makeText(requireContext(), R.string.options_applied, Toast.LENGTH_LONG).show();
    }

    private int parseCost() {
        try {
            return Integer.parseInt(categoryMaxCost.getText().toString().trim());
        } catch (NumberFormatException exception) {
            return com.softwaremagico.librodeesher.character.CharacterPlayer.CATEGORY_MAX_COST;
        }
    }
}
