package com.softwaremagico.librodeesher.android.ui.load;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.AppDatabase;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;

import java.util.List;

public class CharacterLoadFragment extends Fragment implements CharacterListAdapter.OnCharacterClickListener {
    private CharacterListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.characters_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CharacterListAdapter(this);
        recyclerView.setAdapter(adapter);
        loadCharacters();
    }

    private void loadCharacters() {
        new Thread(() -> {
            List<CharacterEntity> characters = AppDatabase.getInstance(requireContext()).getCharacterEntityDao().getAll();
            requireActivity().runOnUiThread(() -> adapter.setCharacters(characters));
        }).start();
    }

    @Override
    public void onCharacterClick(CharacterEntity entity) {
        try {
            CharacterPlayer loaded = entity.getCharacterPlayer();
            CharacterManager.getInstance().setSelectedCharacter(loaded);
            CharacterManager.getInstance().setSelectedCharacterId(entity.getId());
            Toast.makeText(getContext(), R.string.character_loaded, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.sheet_load_error, Toast.LENGTH_SHORT).show();
        }
    }
}
