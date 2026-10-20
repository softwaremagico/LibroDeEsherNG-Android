package com.softwaremagico.librodeesher.android.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button newCharacter = view.findViewById(R.id.home_new_character);
        final Button assistant = view.findViewById(R.id.home_assistant);
        final Button random = view.findViewById(R.id.home_random);
        newCharacter.setOnClickListener(v -> {
            CharacterManager.getInstance().resetCharacter();
            Navigation.findNavController(view).navigate(R.id.navigation_home);
        });
        assistant.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.navigation_assistant));
        random.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.navigation_random));
    }
}
