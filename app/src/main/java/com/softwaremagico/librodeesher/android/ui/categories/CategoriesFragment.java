package com.softwaremagico.librodeesher.android.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.android.ui.components.ElementAdapter;
import com.softwaremagico.librodeesher.category.Category;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private TextView remainingText;
    private TextView emptyText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        remainingText = view.findViewById(R.id.categories_remaining);
        emptyText = view.findViewById(R.id.categories_empty);
        container = view.findViewById(R.id.categories_container);
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
            emptyText.setVisibility(View.GONE);
            return;
        }
        final int remaining;
        try {
            remaining = character.getRemainingDevelopmentPoints();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load development points", exception);
            remainingText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            emptyText.setVisibility(View.GONE);
            return;
        }
        remainingText.setText(getString(R.string.skills_remaining, remaining));
        try {
            final List<Category> categories = new ArrayList<>(RulesCatalog.getInstance().getCategories());
            categories.sort(Comparator.comparing(ElementAdapter::displayName));
            boolean has = false;
            for (final Category category : categories) {
                try {
                    final int ranks = character.getCurrentLevelCategoryRanks(category.getId());
                    final Integer cost = character.getCategoryDevelopmentCost(category.getId(), ranks);
                    addRow(character, category, ranks, cost, remaining);
                    has = true;
                } catch (Exception e) {
                    // skip
                }
            }
            emptyText.setVisibility(has ? View.GONE : View.VISIBLE);
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load categories", exception);
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    private void addRow(CharacterPlayer character, Category category, int ranks, Integer cost, int remaining) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_skill_detailed, container, false);
        final TextView nameView = row.findViewById(R.id.item_skill_name);
        final TextView ranksView = row.findViewById(R.id.item_skill_ranks);
        final TextView costView = row.findViewById(R.id.item_skill_cost);
        final Button minus = row.findViewById(R.id.item_skill_minus);
        final Button plus = row.findViewById(R.id.item_skill_plus);

        nameView.setText(ElementAdapter.displayName(category));
        ranksView.setText(String.valueOf(ranks));
        costView.setText(cost == null ? "-" : String.valueOf(cost));

        minus.setEnabled(ranks > 0);
        minus.setOnClickListener(v -> changeRanks(category, ranks - 1));
        plus.setEnabled(cost != null && cost <= remaining);
        plus.setOnClickListener(v -> changeRanks(category, ranks + 1));
        container.addView(row);
    }

    private void changeRanks(Category category, int ranks) {
        if (ranks < 0) {
            return;
        }
        try {
            if (CharacterManager.getInstance().getSelectedCharacter().setCurrentLevelCategoryRanks(category.getId(), ranks)) {
                render();
            } else {
                Toast.makeText(requireContext(), R.string.skills_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot change category ranks", exception);
            Toast.makeText(requireContext(), R.string.skills_error, Toast.LENGTH_SHORT).show();
        }
    }
}
