package com.softwaremagico.librodeesher.android.ui.random;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.RandomPreferencesHandler;

/** Random-generation preferences that are applied the next time a character is generated. */
public class RandomPreferencesFragment extends Fragment {

    private SeekBar specializationSeekBar;
    private TextView specializationValue;
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
        final Button applyButton = view.findViewById(R.id.random_apply_prefs);

        specializationSeekBar.setProgress(preferences.getSpecializationLevel() + 3);
        specializationValue.setText(String.valueOf(preferences.getSpecializationLevel()));
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
                Toast.makeText(getContext(), R.string.random_preferences_saved, Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(getContext(), R.string.random_preferences_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
