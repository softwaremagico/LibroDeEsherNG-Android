package com.softwaremagico.librodeesher.android.ui.random;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.core.CharacterSummaryBuilder;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.culture.Culture;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.profession.Profession;
import com.softwaremagico.librodeesher.race.Race;
import com.softwaremagico.librodeesher.random.RandomCharacterPlayer;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fully random character generator built on {@link RandomCharacterPlayer}.
 */
public class RandomCharacterFragment extends Fragment {

    private static final int MAX_LEVEL = 20;

    private TextView summaryText;
    private Button generateButton;

    private final List<Race> races = new ArrayList<>();
    private final List<Culture> cultures = new ArrayList<>();
    private final List<Profession> professions = new ArrayList<>();
    private final Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_character, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        summaryText = view.findViewById(R.id.random_summary_text);
        generateButton = view.findViewById(R.id.random_generate_button);

        try {
            final RulesCatalog catalog = RulesCatalog.getInstance();
            races.addAll(catalog.getRaces());
            cultures.addAll(catalog.getCultures());
            professions.addAll(catalog.getProfessions());
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot load rules", exception);
            summaryText.setText(getString(R.string.error_no_rules, String.valueOf(exception.getMessage())));
            generateButton.setEnabled(false);
            return;
        }

        generateButton.setOnClickListener(v -> generateRandomCharacter());
    }

    private void generateRandomCharacter() {
        if (races.isEmpty() || cultures.isEmpty() || professions.isEmpty()) {
            return;
        }
        try {
            final CharacterPlayer character = new CharacterPlayer();
            character.setRaceId(pickRandom(races).getId());
            character.setCultureId(pickRandom(cultures).getId());
            character.setProfessionId(pickRandom(professions).getId());
            final int level = 1 + random.nextInt(MAX_LEVEL);
            new RandomCharacterPlayer(character, level).createRandomValues();
            CharacterManager.getInstance().setSelectedCharacter(character);
            summaryText.setText(CharacterSummaryBuilder.build(character));
        } catch (Exception exception) {
            AppLog.error("Cannot generate random character", exception);
            summaryText.setText(getString(R.string.error_creating_character, String.valueOf(exception.getMessage())));
        }
    }

    private <T> T pickRandom(List<T> items) {
        return items.get(random.nextInt(items.size()));
    }
}
