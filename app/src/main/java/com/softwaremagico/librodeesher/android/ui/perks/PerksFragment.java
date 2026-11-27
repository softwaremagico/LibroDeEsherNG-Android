package com.softwaremagico.librodeesher.android.ui.perks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.Element;
import com.softwaremagico.librodeesher.android.ui.components.ChoiceDialogs;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.perk.Perk;
import com.softwaremagico.librodeesher.perk.PerkChoiceGrant;
import com.softwaremagico.librodeesher.perk.PerkChoiceScope;
import com.softwaremagico.librodeesher.perk.SelectedPerk;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the selected character's perks: lists the perks already chosen (removable unless they were
 * assigned at random) and the ones still available, adding or removing them through
 * {@link CharacterPlayer#addAvailablePerk} / {@link CharacterPlayer#removePerk}.
 */
public class PerksFragment extends Fragment {

    private TextView remainingText;
    private TextView historyPointsText;
    private LinearLayout container;
    private boolean sortByCost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.perks_remaining);
        historyPointsText = view.findViewById(R.id.perks_history_points);
        container = view.findViewById(R.id.perks_container);
        view.findViewById(R.id.perks_sort_name).setOnClickListener(v -> {
            sortByCost = false;
            render();
        });
        view.findViewById(R.id.perks_sort_cost).setOnClickListener(v -> {
            sortByCost = true;
            render();
        });
        view.findViewById(R.id.perks_random).setOnClickListener(v -> addRandomPerks());
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null || character.getLevel() == 0) {
            remainingText.setText(R.string.sheet_no_character);
            return;
        }
        final Integer remaining;
        final List<SelectedPerk> selected;
        final List<String> available;
        try {
            remaining = character.getRemainingPerksPoints();
            selected = new ArrayList<>(character.getSelectedPerks());
            available = character.getAvailablePerkIds();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load perks", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            return;
        }
        remainingText.setText(getString(R.string.perks_remaining, remaining == null ? 0 : remaining));
        try {
            historyPointsText.setText(getString(R.string.perks_history_points,
                    character.getRemainingBackgroundPoints()));
        } catch (InvalidXmlElementException exception) {
            historyPointsText.setText(R.string.sheet_no_character);
        }

        if (!selected.isEmpty()) {
            container.addView(header(getString(R.string.perks_selected_header)));
            for (final SelectedPerk selectedPerk : selected) {
                try {
                    addSelectedRow(character, selectedPerk);
                } catch (InvalidXmlElementException exception) {
                    AppLog.error("Cannot render perk " + selectedPerk.getPerkId(), exception);
                }
            }
        }

        container.addView(header(getString(R.string.perks_available_header)));
        if (available.isEmpty()) {
            final TextView empty = new TextView(requireContext());
            empty.setText(R.string.perks_empty);
            empty.setPadding(0, 8, 0, 8);
            container.addView(empty);
            return;
        }
        available.sort(sortByCost
                ? Comparator.comparing(this::perkCost, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(this::perkName)
                : Comparator.comparing(this::perkName));
        for (final String perkId : available) {
            try {
                addAvailableRow(character, perkId, remaining == null ? 0 : remaining);
            } catch (InvalidXmlElementException exception) {
                AppLog.error("Cannot render available perk " + perkId, exception);
            }
        }
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private String perkName(String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            if (perk != null) {
                return ElementAdapter.displayName(perk);
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve perk " + perkId, exception);
        }
        return perkId;
    }

    private void addSelectedRow(CharacterPlayer character, SelectedPerk selectedPerk)
            throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_perk, container, false);
        final TextView nameView = row.findViewById(R.id.item_perk_name);
        final TextView infoView = row.findViewById(R.id.item_perk_info);
        final ImageButton optionsButton = row.findViewById(R.id.item_perk_options);
        final ImageButton action = row.findViewById(R.id.item_perk_action);
        final Perk perk = RulesCatalog.getInstance().getPerk(selectedPerk.getPerkId());

        String info = perkInfo(selectedPerk.getPerkId(), selectedPerk.isRandom());
        if (selectedPerk.getWeaknessId() != null) {
            info += " · " + getString(R.string.perks_with_weakness, displayNameForId(selectedPerk.getWeaknessId()));
        }
        nameView.setText(perkName(selectedPerk.getPerkId()));
        infoView.setText(info);
        nameView.setOnClickListener(v -> showDescription(selectedPerk.getPerkId()));
        infoView.setOnClickListener(v -> showDescription(selectedPerk.getPerkId()));

        final boolean choiceGrants = perk != null && !perk.getChoiceGrants().isEmpty();
        final boolean weaknessPossible = canPairWeakness(perk) && !character.hasWeakness(selectedPerk.getPerkId());
        if (choiceGrants || weaknessPossible) {
            optionsButton.setVisibility(View.VISIBLE);
            optionsButton.setOnClickListener(v -> resolvePerkRequirements(selectedPerk.getPerkId()));
        }

        action.setImageResource(R.drawable.remove);
        action.setContentDescription(getString(R.string.remove));
        action.setEnabled(!selectedPerk.isRandom());
        action.setOnClickListener(v -> removePerk(selectedPerk.getPerkId()));
        container.addView(row);
    }

    private void addAvailableRow(CharacterPlayer character, String perkId, int remaining)
            throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_perk, container, false);
        final TextView nameView = row.findViewById(R.id.item_perk_name);
        final TextView infoView = row.findViewById(R.id.item_perk_info);
        final ImageButton optionsButton = row.findViewById(R.id.item_perk_options);
        final ImageButton action = row.findViewById(R.id.item_perk_action);
        optionsButton.setVisibility(View.GONE);

        final Integer cost = perkCost(perkId);
        nameView.setText(perkName(perkId));
        infoView.setText(perkInfo(perkId, false));
        nameView.setOnClickListener(v -> showDescription(perkId));
        infoView.setOnClickListener(v -> showDescription(perkId));

        action.setImageResource(R.drawable.add);
        action.setContentDescription(getString(R.string.add));
        action.setEnabled(cost == null || cost <= remaining);
        action.setOnClickListener(v -> addPerk(perkId));
        container.addView(row);
    }

    private String perkInfo(String perkId, boolean random) {
        final Integer cost = perkCost(perkId);
        final String base = getString(R.string.perks_cost, cost == null ? 0 : cost);
        return random ? base + " · " + getString(R.string.perks_random) : base;
    }

    private Integer perkCost(String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            return perk == null ? null : perk.getCost();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve perk cost " + perkId, exception);
            return null;
        }
    }

    private void addPerk(String perkId) {
        try {
            if (CharacterManager.getInstance().getSelectedCharacter().addAvailablePerk(perkId)) {
                requireActivity().invalidateOptionsMenu();
                resolvePerkRequirements(perkId);
            } else {
                Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot add perk", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Legacy "Opciones" flow: pair a weakness, then resolve every choice grant of the perk. */
    private void resolvePerkRequirements(String perkId) {
        resolvePerkRequirements(perkId, null);
    }

    /**
     * Resolves a perk's weakness and choice grants, then invokes {@code onDone}. The callback lets
     * the random-perk flow chain several perks without opening overlapping dialogs.
     */
    private void resolvePerkRequirements(String perkId, Runnable onDone) {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            if (perk == null) {
                finishRequirements(onDone);
                return;
            }
            if (canPairWeakness(perk) && !character.hasWeakness(perkId)) {
                final List<String> weaknessIds = character.getAvailableWeaknessIds(perkId);
                if (!weaknessIds.isEmpty()) {
                    final List<String> labels = new ArrayList<>();
                    labels.add(getString(R.string.perks_no_weakness));
                    labels.addAll(displayNames(weaknessIds));
                    ChoiceDialogs.selectOne(requireContext(), getString(R.string.perks_weakness_title), labels,
                            label -> {
                                if (!getString(R.string.perks_no_weakness).equals(label)) {
                                    final int index = labels.indexOf(label) - 1;
                                    try {
                                        character.addWeakness(perkId, weaknessIds.get(index));
                                    } catch (InvalidXmlElementException exception) {
                                        AppLog.error("Cannot pair weakness", exception);
                                    }
                                }
                                resolveChoiceGrants(character, perkId, perk.getChoiceGrants(), 0, onDone);
                            });
                    return;
                }
            }
            resolveChoiceGrants(character, perkId, perk.getChoiceGrants(), 0, onDone);
        } catch (Exception exception) {
            AppLog.error("Cannot resolve perk options", exception);
            finishRequirements(onDone);
        }
    }

    private void finishRequirements(Runnable onDone) {
        requireActivity().invalidateOptionsMenu();
        if (onDone != null) {
            onDone.run();
        } else {
            render();
        }
    }

    private boolean canPairWeakness(Perk perk) {
        return perk != null && OptionsHandler.getInstance(requireContext()).isPerksCostBackgroundPoints()
                && perk.getCost() != null && perk.getCost() > 0;
    }

    /** Resolves grants sequentially so only one selection dialog is visible at a time. */
    private void resolveChoiceGrants(CharacterPlayer character, String perkId, List<PerkChoiceGrant> grants,
                                     int index, Runnable onDone) {
        if (index >= grants.size()) {
            finishRequirements(onDone);
            return;
        }
        final PerkChoiceGrant grant = grants.get(index);
        try {
            if (grant.getCategoryId() != null || grant.getSkillId() != null) {
                character.applyPerkChoiceGrant(perkId, index, grant,
                        grant.getCategoryId() != null ? grant.getCategoryId() : grant.getSkillId());
                resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
                return;
            }
            // A successful call with no candidate means the grant was already resolved; an
            // unresolved choice rejects a null candidate without recording a decision.
            try {
                character.applyPerkChoiceGrant(perkId, index, grant, (String) null);
                resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
                return;
            } catch (RuntimeException alreadyUnresolved) {
                // The player still has to choose below.
            }
            final List<String> optionIds = choiceScopeIds(character, grant.getScope());
            if (optionIds.isEmpty()) {
                resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
                return;
            }
            final List<String> optionLabels = displayNames(optionIds);
            final int choices = grant.getOptionsToChoose() == null ? 1 : grant.getOptionsToChoose();
            if (choices == -1) {
                showFlexibleChoiceDialog(character, perkId, grants, index, grant, optionIds, optionLabels, onDone);
            } else {
                ChoiceDialogs.selectMany(requireContext(), getString(R.string.perks_choice_title),
                        optionLabels, choices, selectedLabels -> {
                            try {
                                character.applyPerkChoiceGrant(perkId, index, grant,
                                        idsFromLabels(optionIds, optionLabels, selectedLabels));
                                resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
                            } catch (Exception exception) {
                                AppLog.error("Cannot apply perk choice", exception);
                                Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
                                finishRequirements(onDone);
                            }
                        });
            }
        } catch (Exception exception) {
            AppLog.error("Cannot resolve perk choice grant", exception);
            resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
        }
    }

    private void showFlexibleChoiceDialog(CharacterPlayer character, String perkId,
                                          List<PerkChoiceGrant> grants, int index, PerkChoiceGrant grant,
                                          List<String> optionIds, List<String> optionLabels, Runnable onDone) {
        final boolean[] checked = new boolean[optionIds.size()];
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.perks_choice_flexible_title)
                .setMultiChoiceItems(optionLabels.toArray(new String[0]), checked,
                        (source, which, isChecked) -> checked[which] = isChecked)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.accept, (source, which) -> {
                    final List<String> selectedIds = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            selectedIds.add(optionIds.get(i));
                        }
                    }
                    if (selectedIds.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        character.applyPerkChoiceGrant(perkId, index, grant, selectedIds);
                        resolveChoiceGrants(character, perkId, grants, index + 1, onDone);
                    } catch (Exception exception) {
                        AppLog.error("Cannot apply flexible perk choice", exception);
                        Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
                        finishRequirements(onDone);
                    }
                })
                .show();
    }

    private List<String> choiceScopeIds(CharacterPlayer character, PerkChoiceScope scope)
            throws InvalidXmlElementException {
        final List<String> ids = new ArrayList<>();
        if (scope == null) {
            return ids;
        }
        switch (scope) {
            case ANY_CATEGORY:
                for (final com.softwaremagico.librodeesher.category.Category category
                        : RulesCatalog.getInstance().getCategories()) {
                    ids.add(category.getId());
                }
                break;
            case ANY_WEAPON_CATEGORY:
                for (final com.softwaremagico.librodeesher.category.Category category
                        : RulesCatalog.getInstance().getCategories()) {
                    if (CharacterPlayer.isWeaponCategoryId(category.getId())) {
                        ids.add(category.getId());
                    }
                }
                break;
            case ANY_SKILL:
                for (final com.softwaremagico.librodeesher.skill.Skill skill
                        : RulesCatalog.getInstance().getSkills()) {
                    ids.add(skill.getId());
                }
                break;
            case ANY_WEAPON_SKILL:
                for (final com.softwaremagico.librodeesher.skill.Skill skill
                        : RulesCatalog.getInstance().getSkills()) {
                    if (skill.getCategoryId() != null && skill.getCategoryId().startsWith("weapons")) {
                        ids.add(skill.getId());
                    }
                }
                break;
            default:
                break;
        }
        return ids;
    }

    private List<String> displayNames(List<String> ids) {
        final List<String> labels = new ArrayList<>();
        for (final String id : ids) {
            labels.add(displayNameForId(id));
        }
        return labels;
    }

    private String displayNameForId(String id) {
        final Element[] candidates = new Element[3];
        try {
            candidates[0] = RulesCatalog.getInstance().getCategory(id);
        } catch (Exception ignored) {
            // Not a category id.
        }
        try {
            candidates[1] = RulesCatalog.getInstance().getSkill(id);
        } catch (Exception ignored) {
            // Not a skill id.
        }
        try {
            candidates[2] = RulesCatalog.getInstance().getPerk(id);
        } catch (Exception ignored) {
            // Not a perk id.
        }
        for (final Element candidate : candidates) {
            if (candidate != null) {
                final String name = ElementAdapter.displayName(candidate);
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        }
        return id;
    }

    private List<String> idsFromLabels(List<String> ids, List<String> labels, List<String> selectedLabels) {
        final List<String> remainingLabels = new ArrayList<>(labels);
        final List<String> selectedIds = new ArrayList<>();
        for (final String label : selectedLabels) {
            final int index = remainingLabels.indexOf(label);
            if (index >= 0) {
                selectedIds.add(ids.get(index));
                remainingLabels.remove(index);
            }
        }
        return selectedIds;
    }

    private void showDescription(String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            final String description = perk == null || perk.getDescription() == null
                    ? "" : perk.getDescription().getTranslatedText();
            new AlertDialog.Builder(requireContext())
                    .setTitle(perkName(perkId))
                    .setMessage(description == null || description.isEmpty()
                            ? getString(R.string.perks_cost, perkCost(perkId) == null ? 0 : perkCost(perkId))
                            : description)
                    .setPositiveButton(R.string.accept, null)
                    .show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot show perk description", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Selects a few affordable perks exactly as the legacy "Dotes aleatorias" button did. */
    private void addRandomPerks() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        try {
            final List<String> candidates = new ArrayList<>(character.getAvailablePerkIds());
            Collections.shuffle(candidates);
            final boolean backgroundBudget = OptionsHandler.getInstance(requireContext())
                    .isPerksCostBackgroundPoints();
            final List<String> pendingRequirements = new ArrayList<>();
            int added = 0;
            for (final String perkId : candidates) {
                if (added >= 3) {
                    break;
                }
                final int before = character.getRemainingBackgroundPoints();
                character.addPerk(perkId);
                if (character.getSelectedPerks().stream()
                        .noneMatch(selected -> selected.getPerkId().equals(perkId))) {
                    continue;
                }
                if (backgroundBudget && character.getRemainingBackgroundPoints() < 0) {
                    character.removePerk(perkId);
                    continue;
                }
                character.setPerkAsRandom(perkId, true);
                if (hasPendingRequirements(character, perkId)) {
                    pendingRequirements.add(perkId);
                }
                added++;
                if (before <= 0) {
                    break;
                }
            }
            if (added > 0) {
                requireActivity().invalidateOptionsMenu();
            }
            render();
            if (added == 0) {
                Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
            } else if (!pendingRequirements.isEmpty()) {
                resolvePendingPerks(pendingRequirements, 0);
            }
        } catch (Exception exception) {
            AppLog.error("Cannot add random perks", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Whether a randomly added perk still needs a weakness or a choice-grant decision. */
    private boolean hasPendingRequirements(CharacterPlayer character, String perkId) {
        try {
            final Perk perk = RulesCatalog.getInstance().getPerk(perkId);
            if (perk == null) {
                return false;
            }
            if (!perk.getChoiceGrants().isEmpty()) {
                return true;
            }
            return canPairWeakness(perk) && !character.hasWeakness(perkId)
                    && !character.getAvailableWeaknessIds(perkId).isEmpty();
        } catch (Exception exception) {
            AppLog.error("Cannot inspect random perk requirements", exception);
            return false;
        }
    }

    /** Chains the resolution dialogs of the randomly selected perks one at a time. */
    private void resolvePendingPerks(List<String> perkIds, int index) {
        if (index >= perkIds.size()) {
            render();
            return;
        }
        resolvePerkRequirements(perkIds.get(index), () -> resolvePendingPerks(perkIds, index + 1));
    }

    private void removePerk(String perkId) {
        try {
            CharacterManager.getInstance().getSelectedCharacter().removePerk(perkId);
            requireActivity().invalidateOptionsMenu();
            render();
        } catch (RuntimeException exception) {
            AppLog.error("Cannot remove perk", exception);
            Toast.makeText(requireContext(), R.string.perks_error, Toast.LENGTH_SHORT).show();
        }
    }
}
