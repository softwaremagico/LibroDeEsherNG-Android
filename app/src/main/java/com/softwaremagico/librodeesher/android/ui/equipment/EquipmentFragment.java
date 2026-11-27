package com.softwaremagico.librodeesher.android.ui.equipment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.equipment.BonusType;
import com.softwaremagico.librodeesher.equipment.Equipment;
import com.softwaremagico.librodeesher.equipment.MagicObject;
import com.softwaremagico.librodeesher.equipment.ObjectBonus;
import com.softwaremagico.librodeesher.language.TranslatedText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lets the user manage the selected character's equipment: plain (non-magic) items and magic items,
 * added as free text (the rules engine has no equipment catalogue) through
 * {@link CharacterPlayer#addStandardEquipment} / {@link CharacterPlayer#addMagicItem}.
 */
public class EquipmentFragment extends Fragment {

    private LinearLayout container;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.equipment_container);
        emptyText = view.findViewById(R.id.equipment_empty);
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

        container.addView(header(getString(R.string.equipment_standard_header)));
        container.addView(addButton(getString(R.string.equipment_add), v -> addItemDialog(false)));
        final List<Equipment> equipment = new ArrayList<>(character.getStandardEquipment());
        equipment.sort(Comparator.comparing(item -> text(item.getName())));
        if (equipment.isEmpty()) {
            container.addView(message(getString(R.string.equipment_none)));
        }
        for (final Equipment item : equipment) {
            addRow(text(item.getName()), text(item.getDescription()), v -> {
                character.removeStandardEquipment(item);
                requireActivity().invalidateOptionsMenu();
                render();
            });
        }

        container.addView(header(getString(R.string.equipment_magic_header)));
        container.addView(addButton(getString(R.string.equipment_add_magic), v -> addItemDialog(true)));
        final List<MagicObject> magicItems = new ArrayList<>(character.getAllMagicItems());
        magicItems.sort(Comparator.comparing(item -> text(item.getName())));
        if (magicItems.isEmpty()) {
            container.addView(message(getString(R.string.equipment_none)));
        }
        for (final MagicObject item : magicItems) {
            final StringBuilder info = new StringBuilder(text(item.getDescription()));
            final List<ObjectBonus> itemBonuses = item.getBonuses() == null
                    ? new ArrayList<>() : item.getBonuses();
            for (final ObjectBonus bonus : itemBonuses) {
                if (info.length() > 0) {
                    info.append('\n');
                }
                info.append(bonus.getType()).append(": ").append(bonus.getBonusName())
                        .append(' ').append(bonus.getBonus());
            }
            addRow(text(item.getName()), info.toString(), v -> {
                character.removeMagicItem(item);
                requireActivity().invalidateOptionsMenu();
                render();
            });
        }
    }

    private void addItemDialog(boolean magic) {
        final View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_equipment, container, false);
        final EditText nameField = view.findViewById(R.id.dialog_equipment_name);
        final EditText descriptionField = view.findViewById(R.id.dialog_equipment_description);
        final List<ObjectBonus> bonuses = new ArrayList<>();
        final TextView bonusList = view.findViewById(R.id.dialog_equipment_bonus_list);

        if (magic) {
            view.findViewById(R.id.dialog_equipment_bonus_section).setVisibility(View.VISIBLE);
            final Spinner typeSpinner = view.findViewById(R.id.dialog_equipment_bonus_type);
            typeSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, BonusType.values()));
            final EditText bonusNameField = view.findViewById(R.id.dialog_equipment_bonus_name);
            final EditText bonusValueField = view.findViewById(R.id.dialog_equipment_bonus_value);
            view.findViewById(R.id.dialog_equipment_add_bonus).setOnClickListener(v -> {
                try {
                    final int value = Integer.parseInt(bonusValueField.getText().toString().trim());
                    final String bonusName = bonusNameField.getText().toString().trim();
                    bonuses.add(new ObjectBonus((BonusType) typeSpinner.getSelectedItem(),
                            bonusName, value));
                    bonusFieldUpdated(bonusList, bonuses);
                    bonusNameField.setText("");
                    bonusValueField.setText("");
                } catch (NumberFormatException exception) {
                    bonusList.setText(R.string.equipment_bonus_value);
                }
            });
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(magic ? R.string.equipment_add_magic : R.string.equipment_add)
                .setView(view)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    final String name = nameField.getText().toString().trim();
                    if (name.isEmpty()) {
                        return;
                    }
                    final String description = descriptionField.getText().toString().trim();
                    final TranslatedText nameText = new TranslatedText(name, name);
                    final TranslatedText descriptionText = description.isEmpty()
                            ? null : new TranslatedText(description, description);
                    if (magic) {
                        final MagicObject item = new MagicObject(nameText, descriptionText,
                                new ArrayList<>(bonuses));
                        CharacterManager.getInstance().getSelectedCharacter().addMagicItem(item);
                    } else {
                        CharacterManager.getInstance().getSelectedCharacter().addStandardEquipment(
                                new Equipment(nameText, descriptionText));
                    }
                    requireActivity().invalidateOptionsMenu();
                    render();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void bonusFieldUpdated(TextView bonusList, List<ObjectBonus> bonuses) {
        final StringBuilder builder = new StringBuilder();
        for (final ObjectBonus bonus : bonuses) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(bonus.getType()).append(": ").append(bonus.getBonusName())
                    .append(' ').append(bonus.getBonus());
        }
        bonusList.setText(builder.toString());
    }

    private String text(TranslatedText translated) {
        if (translated == null) {
            return "";
        }
        final String value = translated.getTranslatedText();
        return value == null ? "" : value;
    }

    private TextView header(String text) {
        final TextView header = new TextView(requireContext());
        header.setText(text);
        header.setPadding(0, 12, 0, 4);
        header.setAllCaps(true);
        return header;
    }

    private TextView message(String text) {
        final TextView message = new TextView(requireContext());
        message.setText(text);
        message.setPadding(0, 4, 0, 4);
        return message;
    }

    private Button addButton(String text, View.OnClickListener listener) {
        final Button button = new Button(requireContext());
        button.setText(text);
        button.setOnClickListener(listener);
        return button;
    }

    private void addRow(String name, String info, View.OnClickListener remove) {
        final View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_equipment, container, false);
        final TextView nameView = row.findViewById(R.id.item_equipment_name);
        final TextView infoView = row.findViewById(R.id.item_equipment_info);
        final ImageButton action = row.findViewById(R.id.item_equipment_action);
        nameView.setText(name);
        infoView.setText(info);
        infoView.setVisibility(info.isEmpty() ? View.GONE : View.VISIBLE);
        action.setOnClickListener(remove);
        container.addView(row);
    }
}
