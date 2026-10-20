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
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;

public class RandomPreferencesFragment extends Fragment {

    private SeekBar specializationSeekBar;
    private TextView specializationValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        specializationSeekBar = view.findViewById(R.id.random_specialization_seekbar);
        specializationValue = view.findViewById(R.id.random_specialization_value);
        final Button applyButton = view.findViewById(R.id.random_apply_prefs);

        specializationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int value = progress - 3;
                specializationValue.setText(String.valueOf(value));
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
                final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
                final RandomCharacterPlayer generator = new RandomCharacterPlayer(character);
                final int value = specializationSeekBar.getProgress() - 3;
                generator.setSpecializationLevel(value);
                generator.createRandomValues();
                CharacterManager.getInstance().setSelectedCharacter(character);
                Toast.makeText(getContext(), "Preferencias aplicadas", Toast.LENGTH_SHORT).show();
            } catch (Exception exception) {
                Toast.makeText(getContext(), "Error al aplicar preferencias", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
