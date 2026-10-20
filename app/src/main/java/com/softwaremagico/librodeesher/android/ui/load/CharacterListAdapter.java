package com.softwaremagico.librodeesher.android.ui.load;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;

import java.util.ArrayList;
import java.util.List;

public class CharacterListAdapter extends RecyclerView.Adapter<CharacterListAdapter.ViewHolder> {
    private final List<CharacterEntity> characters = new ArrayList<>();
    private final OnCharacterClickListener listener;

    public interface OnCharacterClickListener {
        void onCharacterClick(CharacterEntity character);
    }

    public CharacterListAdapter(OnCharacterClickListener listener) {
        this.listener = listener;
    }

    public void setCharacters(List<CharacterEntity> characters) {
        this.characters.clear();
        this.characters.addAll(characters);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterEntity entity = characters.get(position);
        holder.nameText.setText(entity.name);
        holder.summaryText.setText(entity.race + " - " + entity.profession);
        holder.itemView.setOnClickListener(v -> listener.onCharacterClick(entity));
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView summaryText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.character_name);
            summaryText = view.findViewById(R.id.character_summary);
        }
    }
}
