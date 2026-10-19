package com.softwaremagico.librodeesher.android.ui.weapontiers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.category.Category;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.profession.ProfessionWeaponCostTier;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lets the user assign the profession's weapon-category cost tiers to the character's weapon
 * categories, cheapest tier first, through
 * {@link CharacterPlayer#assignAvailableWeaponCategoryCostTier}.
 */
public class WeaponTiersFragment extends Fragment {

    private TextView emptyText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weapon_tiers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyText = view.findViewById(R.id.weapon_tiers_empty);
        container = view.findViewById(R.id.weapon_tiers_container);
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
            emptyText.setText(R.string.sheet_no_character);
            emptyText.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            return;
        }
        try {
            final int tierCount = character.getWeaponCategoryCostTierCount();
            if (tierCount == 0) {
                emptyText.setText(R.string.weapon_tiers_none);
                emptyText.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                return;
            }
            emptyText.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            addHelp();

            final Profession profession = character.getProfession();
            final List<ProfessionWeaponCostTier> professionTiers =
                    profession == null ? new ArrayList<>() : profession.getWeaponCategoryCostTiers();
            final Map<ProfessionWeaponCostTier, String> assignedByTier = assignedCategories(character);
            final Set<String> used = new HashSet<>();

            for (int tierIndex = 0; tierIndex < tierCount; tierIndex++) {
                final ProfessionWeaponCostTier tier = tierOf(professionTiers, tierIndex);
                String assigned = assignedByTier.get(tier);
                if (assigned != null && !used.add(assigned)) {
                    assigned = null;
                }
                addTierRow(character, tierIndex, tier, assigned);
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load weapon cost tiers", exception);
            emptyText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            emptyText.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        }
    }

    private Map<ProfessionWeaponCostTier, String> assignedCategories(CharacterPlayer character)
            throws InvalidXmlElementException {
        final Map<ProfessionWeaponCostTier, String> assigned = new HashMap<>();
        for (final Category category : RulesCatalog.getInstance().getCategories()) {
            if (CharacterPlayer.isWeaponCategoryId(category.getId())) {
                final ProfessionWeaponCostTier tier = character.getAssignedWeaponCategoryCostTier(category.getId());
                if (tier != null) {
                    assigned.put(tier, category.getId());
                }
            }
        }
        return assigned;
    }

    private ProfessionWeaponCostTier tierOf(List<ProfessionWeaponCostTier> tiers, int tierIndex) {
        if (tiers.isEmpty()) {
            return null;
        }
        return tiers.get(Math.min(tierIndex, tiers.size() - 1));
    }

    private void addTierRow(CharacterPlayer character, int tierIndex, ProfessionWeaponCostTier tier,
                            String assignedCategoryId) throws InvalidXmlElementException {
        final View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_weapon_tier, container, false);
        final TextView nameView = row.findViewById(R.id.item_weapon_tier_name);
        final TextView infoView = row.findViewById(R.id.item_weapon_tier_info);
        final Button action = row.findViewById(R.id.item_weapon_tier_action);

        nameView.setText(getString(R.string.weapon_tiers_level, tierIndex + 1));
        infoView.setText(getString(R.string.weapon_tiers_cost, formatCosts(tier)));
        action.setText(assignedCategoryId == null
                ? getString(R.string.weapon_tiers_unassigned)
                : categoryName(assignedCategoryId));
        action.setOnClickListener(v -> showPickDialog(character, tierIndex));
        container.addView(row);
    }

    private void showPickDialog(CharacterPlayer character, int tierIndex) {
        final List<String> available;
        try {
            available = character.getAvailableWeaponCategoriesForCostTier(tierIndex);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load available weapon categories", exception);
            return;
        }
        if (available.isEmpty()) {
            return;
        }
        final String[] names = new String[available.size()];
        for (int i = 0; i < available.size(); i++) {
            names[i] = categoryName(available.get(i));
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.weapon_tiers_level, tierIndex + 1))
                .setItems(names, (dialog, which) -> {
                    try {
                        character.assignAvailableWeaponCategoryCostTier(tierIndex, available.get(which));
                    } catch (InvalidXmlElementException exception) {
                        AppLog.error("Cannot assign weapon category", exception);
                    }
                    render();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String formatCosts(ProfessionWeaponCostTier tier) {
        if (tier == null) {
            return "-";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Integer cost : tier.getRankCosts()) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append(cost);
        }
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private String categoryName(String categoryId) {
        try {
            final Category category = RulesCatalog.getInstance().getCategory(categoryId);
            if (category != null && category.getName() != null
                    && category.getName().getTranslatedText() != null) {
                return category.getName().getTranslatedText();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve category " + categoryId, exception);
        }
        return categoryId;
    }

    private void addHelp() {
        final TextView help = new TextView(requireContext());
        help.setText(R.string.weapon_tiers_help);
        help.setPadding(0, 0, 0, 8);
        container.addView(help);
    }
}
