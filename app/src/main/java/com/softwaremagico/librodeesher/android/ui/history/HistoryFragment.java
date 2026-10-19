package com.softwaremagico.librodeesher.android.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.background.Background;
import com.softwaremagico.librodeesher.category.Category;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.characteristic.CharacteristicRoll;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.rules.RulesCatalog;
import com.softwaremagico.librodeesher.skill.Skill;

import java.util.Map;

/**
 * Read-only view of the selected character's background: spent/remaining background points, the
 * skills and categories that received a background bonus, the history languages, the characteristic
 * rolls and the free-form history text.
 */
public class HistoryFragment extends Fragment {

    private TextView emptyText;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyText = view.findViewById(R.id.history_empty);
        container = view.findViewById(R.id.history_container);
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
            emptyText.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            return;
        }
        emptyText.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        final Background background = character.getBackground();
        try {
            container.addView(header(getString(R.string.history_points_header)));
            addRow(getString(R.string.history_points_spent), String.valueOf(background.getSpentBackgroundPoints()));
            addRow(getString(R.string.history_points_remaining),
                    String.valueOf(character.getRemainingBackgroundPoints()));
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot render background points", exception);
        }

        if (!background.getSkillIds().isEmpty()) {
            container.addView(header(getString(R.string.history_skills_header)));
            for (final String skillId : background.getSkillIds()) {
                addRow(elementName(skillId, true), getString(R.string.history_bonus, background.getSkillBonus(skillId)));
            }
        }

        if (!background.getCategoryIds().isEmpty()) {
            container.addView(header(getString(R.string.history_categories_header)));
            for (final String categoryId : background.getCategoryIds()) {
                addRow(elementName(categoryId, false),
                        getString(R.string.history_bonus, background.getCategoryBonus(categoryId)));
            }
        }

        if (!background.getLanguageRanks().isEmpty()) {
            container.addView(header(getString(R.string.history_languages_header)));
            for (final Map.Entry<String, Integer> entry : background.getLanguageRanks().entrySet()) {
                addRow(elementName(entry.getKey(), true), String.valueOf(entry.getValue()));
            }
        }

        if (!background.getCharacteristicUpdates().isEmpty()) {
            container.addView(header(getString(R.string.history_characteristics_header)));
            for (final CharacteristicRoll roll : background.getCharacteristicUpdates()) {
                addRow(roll.getCharacteristicAbbreviation().getCode(),
                        getString(R.string.history_roll, roll.getCharacteristicTemporalValue(),
                                roll.getCharacteristicPotentialValue()));
            }
        }

        final String historyText = character.getHistoryText();
        if (historyText != null && !historyText.trim().isEmpty()) {
            container.addView(header(getString(R.string.history_text_header)));
            final TextView text = new TextView(requireContext());
            text.setText(historyText);
            text.setPadding(0, 4, 0, 4);
            container.addView(text);
        }
    }

    private String elementName(String id, boolean skill) {
        try {
            if (skill) {
                final Skill resolved = RulesCatalog.getInstance().getSkill(id);
                if (resolved != null && resolved.getName() != null) {
                    return resolved.getName().getTranslatedText();
                }
            } else {
                final Category resolved = RulesCatalog.getInstance().getCategory(id);
                if (resolved != null && resolved.getName() != null) {
                    return resolved.getName().getTranslatedText();
                }
            }
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot resolve element " + id, exception);
        }
        return id;
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private void addRow(String name, String value) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat, container, false);
        final TextView nameView = row.findViewById(R.id.item_stat_name);
        final TextView valueView = row.findViewById(R.id.item_stat_value);
        nameView.setText(name);
        valueView.setText(value);
        container.addView(row);
    }
}
