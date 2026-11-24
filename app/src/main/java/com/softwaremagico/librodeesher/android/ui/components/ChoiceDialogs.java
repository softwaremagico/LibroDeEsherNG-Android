package com.softwaremagico.librodeesher.android.ui.components;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.softwaremagico.librodeesher.android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Small selection dialogs shared by the guided creation screens. */
public final class ChoiceDialogs {

    private ChoiceDialogs() {
    }

    public static void selectOne(Context context, String title, List<String> options,
                                 Consumer<String> onSelected) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(options.toArray(new String[0]), (dialog, which) -> onSelected.accept(options.get(which)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Multi-selection dialog that enforces {@code count} chosen items before accepting. */
    public static void selectMany(Context context, String title, List<String> options, int count,
                                  Consumer<List<String>> onSelected) {
        final boolean[] checked = new boolean[options.size()];
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMultiChoiceItems(options.toArray(new String[0]), checked,
                        (source, which, isChecked) -> checked[which] = isChecked)
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.accept),
                (source, which) -> {
                    final List<String> selected = new ArrayList<>();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            selected.add(options.get(i));
                        }
                    }
                    if (selected.size() == count) {
                        onSelected.accept(selected);
                    }
                });
        dialog.show();
    }
}
