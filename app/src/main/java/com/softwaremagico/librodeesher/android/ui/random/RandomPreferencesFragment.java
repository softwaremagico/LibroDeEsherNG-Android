package com.softwaremagico.librodeesher.android.ui.random;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.RandomPreferencesHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Random-generation preferences that are applied the next time a character is generated. */
public class RandomPreferencesFragment extends Fragment {

    private SeekBar specializationSeekBar;
    private TextView specializationValue;
    private EditText finalLevelField;
    private CheckBox perksEnabled;
    private EditText categoryRanksField;
    private EditText skillRanksField;
    private EditText trainingsField;
    private EditText perksField;
    private TextView progressText;
    private RandomPreferencesHandler preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferences = RandomPreferencesHandler.getInstance(requireContext());
        specializationSeekBar = view.findViewById(R.id.random_specialization_seekbar);
        specializationValue = view.findViewById(R.id.random_specialization_value);
        finalLevelField = view.findViewById(R.id.random_final_level);
        perksEnabled = view.findViewById(R.id.random_perks_enabled);
        categoryRanksField = view.findViewById(R.id.random_category_ranks);
        skillRanksField = view.findViewById(R.id.random_skill_ranks);
        trainingsField = view.findViewById(R.id.random_trainings);
        perksField = view.findViewById(R.id.random_suggested_perks);
        progressText = view.findViewById(R.id.random_progress);
        final Button applyButton = view.findViewById(R.id.random_apply_prefs);

        specializationSeekBar.setProgress(preferences.getSpecializationLevel() + 3);
        specializationValue.setText(String.valueOf(preferences.getSpecializationLevel()));
        finalLevelField.setText(String.valueOf(preferences.getFinalLevel()));
        perksEnabled.setChecked(preferences.isPerksEnabled());
        categoryRanksField.setText(formatRanks(preferences.getCategoryRanks()));
        skillRanksField.setText(formatRanks(preferences.getSkillRanks()));
        trainingsField.setText(String.join(",", preferences.getTrainings()));
        perksField.setText(String.join(",", preferences.getSuggestedPerks()));
        showProgress();

        specializationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                specializationValue.setText(String.valueOf(progress - 3));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        applyButton.setOnClickListener(v -> {
            try {
                preferences.setSpecializationLevel(specializationSeekBar.getProgress() - 3);
                final String level = finalLevelField.getText().toString().trim();
                if (!level.isEmpty()) {
                    preferences.setFinalLevel(Integer.parseInt(level));
                }
                preferences.setPerksEnabled(perksEnabled.isChecked());
                preferences.setCategoryRanks(parseRanks(categoryRanksField.getText().toString()));
                preferences.setSkillRanks(parseRanks(skillRanksField.getText().toString()));
                preferences.setTrainings(parseList(trainingsField.getText().toString()));
                preferences.setSuggestedPerks(parseList(perksField.getText().toString()));
                showProgress();
                Toast.makeText(getContext(), R.string.random_preferences_saved, Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(getContext(), R.string.random_preferences_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress() {
        progressText.setText(getString(R.string.random_progress_summary, preferences.getFinalLevel(),
                preferences.getSpecializationLevel(),
                getString(preferences.isPerksEnabled() ? R.string.random_yes : R.string.random_no)));
    }

    private static String formatRanks(java.util.Map<String, Integer> ranks) {
        return ranks.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    private static java.util.Map<String, Integer> parseRanks(String raw) {
        final java.util.Map<String, Integer> ranks = new java.util.LinkedHashMap<>();
        for (final String token : raw.split(",")) {
            final String[] parts = token.trim().split(":", 2);
            if (parts.length == 2) {
                try {
                    ranks.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed user input.
                }
            }
        }
        return ranks;
    }

    private static List<String> parseList(String raw) {
        final List<String> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        return values;
    }
}
