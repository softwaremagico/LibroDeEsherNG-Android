package com.softwaremagico.librodeesher.android.core;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.session.CharacterManager;

/**
 * Confirms only the actions that would discard edits. Mirrors the "unsaved changes" behaviour of
 * ThinkMachine-Advisor without bothering the user when the session is already clean.
 */
public final class UnsavedChangesGuard {

    private UnsavedChangesGuard() {
    }

    public static void run(Context context, Runnable action) {
        if (!CharacterManager.getInstance().isDirty()) {
            action.run();
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.unsaved_changes_title)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(R.string.unsaved_changes_continue, (dialog, which) -> action.run())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
