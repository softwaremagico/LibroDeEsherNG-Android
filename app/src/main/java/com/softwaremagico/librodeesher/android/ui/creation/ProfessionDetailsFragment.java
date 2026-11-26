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
import com.softwaremagico.librodeesher.android.ui.components.ChoiceDialogs;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.decision.DecisionKind;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.magic.RealmOfMagic;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.profession.RealmOfMagicGrant;
import com.softwaremagico.librodeesher.profession.ProfessionSkillGrant;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.skill.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Guided profession screen: magic realm and the common/professional/restricted skill choices. */
public class ProfessionDetailsFragment extends Fragment {

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profession_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.profession_container);
        view.findViewById(R.id.profession_random).setOnClickListener(v -> generateAll());
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null || character.getProfessionId() == null) {
            container.addView(label(getString(R.string.sheet_no_character)));
            return;
        }
        final Profession profession;
        try {
            profession = character.getProfession();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load profession", exception);
            container.addView(label(getString(R.string.error_no_rules, String.valueOf(exception.getMessage()))));
            return;
        }
        if (profession == null) {
            container.addView(label(getString(R.string.sheet_no_character)));
            return;
        }
        container.addView(header(ElementAdapter.displayName(profession)));

        final List<RealmOfMagicGrant> realms = profession.getMagicRealms();
        if (!realms.isEmpty()) {
            container.addView(header(getString(R.string.profession_realms)));
            final List<Spinner> realmSpinners = new ArrayList<>();
            for (int i = 0; i < realms.size(); i++) {
                final List<String> names = new ArrayList<>();
                for (final RealmOfMagic realm : realms.get(i).getOptions()) {
                    names.add(realm.name());
                }
                final TextView label = label(getString(R.string.profession_realm, i + 1));
                container.addView(label);
                if (names.size() > 1) {
                    final Spinner spinner = new Spinner(requireContext());
                    spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                    realmSpinners.add(spinner);
                    container.addView(spinner);
                } else if (!names.isEmpty()) {
                    container.addView(label(names.get(0)));
                }
            }
            for (final Spinner spinner : realmSpinners) {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        applyRealms(character, realms, realmSpinners);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Keep the last applied selection.
                    }
                });
            }
            if (realmSpinners.isEmpty()) {
                applyRealms(character, realms, realmSpinners);
            }
        }

        addSkillSection(character, getString(R.string.profession_common),
                DecisionKind.PROFESSION_COMMON_SKILL, profession.getCommonSkillChoices());
        addSkillSection(character, getString(R.string.profession_professional),
                DecisionKind.PROFESSION_PROFESSIONAL_SKILL, profession.getProfessionalSkillChoices());
        addSkillSection(character, getString(R.string.profession_restricted),
                DecisionKind.PROFESSION_RESTRICTED_SKILL, profession.getRestrictedSkillChoices());
    }

    private void applyRealms(CharacterPlayer character, List<RealmOfMagicGrant> realms,
                             List<Spinner> realmSpinners) {
        final Map<Integer, RealmOfMagic> selections = new HashMap<>();
        int index = 0;
        for (int i = 0; i < realms.size(); i++) {
            if (realms.get(i).getOptions().size() > 1 && index < realmSpinners.size()) {
                final String selected = (String) realmSpinners.get(index++).getSelectedItem();
                selections.put(i, RealmOfMagic.valueOf(selected));
            }
        }
        try {
            character.applyProfessionMagicRealms(selections);
            requireActivity().invalidateOptionsMenu();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot apply magic realm", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void addSkillSection(CharacterPlayer character, String title, DecisionKind kind,
                                 List<ProfessionSkillGrant> grants) {
        if (grants.isEmpty()) {
            return;
        }
        container.addView(header(title));
        for (int i = 0; i < grants.size(); i++) {
            final ProfessionSkillGrant grant = grants.get(i);
            final int index = i;
            final int count = grant.getRanksToChoose() == null ? 1 : grant.getRanksToChoose();
            final Button button = new Button(requireContext());
            button.setText(getString(R.string.profession_choose, count,
                    grant.getSkillOptions() == null ? getString(R.string.profession_category)
                            : String.join(", ", grant.getSkillOptions())));
            button.setOnClickListener(v -> {
                final List<String> options = optionsFor(grant);
                if (options.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                ChoiceDialogs.selectMany(requireContext(), title, options, Math.min(count, options.size()),
                        selected -> applyGrant(character, kind, index, grant, selected));
            });
            container.addView(button);
        }
    }

    private List<String> optionsFor(ProfessionSkillGrant grant) {
        if (grant.getSkillOptions() != null && !grant.getSkillOptions().isEmpty()) {
            return new ArrayList<>(grant.getSkillOptions());
        }
        final List<String> options = new ArrayList<>();
        if (grant.getCategoryId() != null) {
            try {
                for (final Skill skill : RulesCatalog.getInstance().getSkills()) {
                    if (grant.getCategoryId().equals(skill.getCategoryId())) {
                        options.add(skill.getId());
                    }
                }
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot list category skills", exception);
            }
        }
        return options;
    }

    private void applyGrant(CharacterPlayer character, DecisionKind kind, int index,
                            ProfessionSkillGrant grant, List<String> selected) {
        try {
            character.applyProfessionSkillGrant(character.getProfessionId(), kind, index, grant, selected);
            requireActivity().invalidateOptionsMenu();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot apply profession skill choice", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Resolves every pending choice at once, mirroring the original "Aleatorio" buttons. */
    private void generateAll() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getProfessionId() == null) {
            return;
        }
        try {
            final Profession profession = character.getProfession();
            if (profession == null) {
                return;
            }
            final List<RealmOfMagicGrant> realms = profession.getMagicRealms();
            final Map<Integer, RealmOfMagic> selections = new HashMap<>();
            for (int i = 0; i < realms.size(); i++) {
                final List<RealmOfMagic> options = realms.get(i).getOptions();
                if (!options.isEmpty()) {
                    selections.put(i, options.get((int) (Math.random() * options.size())));
                }
            }
            character.applyProfessionMagicRealms(selections);
            addRandomGrants(character, DecisionKind.PROFESSION_COMMON_SKILL, profession.getCommonSkillChoices());
            addRandomGrants(character, DecisionKind.PROFESSION_PROFESSIONAL_SKILL,
                    profession.getProfessionalSkillChoices());
            addRandomGrants(character, DecisionKind.PROFESSION_RESTRICTED_SKILL,
                    profession.getRestrictedSkillChoices());
            requireActivity().invalidateOptionsMenu();
            render();
            Toast.makeText(requireContext(), R.string.creation_applied, Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            AppLog.error("Cannot randomize profession", exception);
            Toast.makeText(requireContext(), R.string.creation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void addRandomGrants(CharacterPlayer character, DecisionKind kind,
                                 List<ProfessionSkillGrant> grants) throws InvalidXmlElementException {
        for (int i = 0; i < grants.size(); i++) {
            final ProfessionSkillGrant grant = grants.get(i);
            final List<String> options = optionsFor(grant);
            final int count = Math.min(grant.getRanksToChoose() == null ? 1 : grant.getRanksToChoose(),
                    options.size());
            final List<String> selected = new ArrayList<>();
            for (int j = 0; j < count; j++) {
                selected.add(options.remove((int) (Math.random() * options.size())));
            }
            character.applyProfessionSkillGrant(character.getProfessionId(), kind, i, grant, selected);
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
