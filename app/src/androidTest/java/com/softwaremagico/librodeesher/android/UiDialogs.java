package com.softwaremagico.librodeesher.android;

import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.IdlingPolicies;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** Shared UI-only helpers for dialogs that may or may not appear between test steps. */
public final class UiDialogs {

    private UiDialogs() {
        // Utility class.
    }

    /**
     * Confirms discarding edits only when the guard actually asked for confirmation. The lookup uses
     * a short Espresso timeout because the global policy is deliberately long for slow rule loading.
     */
    public static void discardUnsavedChangesIfPrompted() {
        android.os.SystemClock.sleep(500);
        clickDialogButtonIfPresent(R.string.unsaved_changes_continue);
    }

    /** Clicks a dialog button only when it is present; returns whether it was clicked. */
    public static boolean clickDialogButtonIfPresent(int textResId) {
        final boolean[] failed = {false};
        final FailureHandler handler = (error, viewMatcher) -> failed[0] = true;
        withShortTimeout(() -> onView(withText(textResId)).inRoot(isDialog())
                .withFailureHandler(handler).perform(click()));
        return !failed[0];
    }

    /** Checks whether a labelled dialog is visible without waiting for the global idle timeout. */
    public static boolean isDialogTextPresent(int textResId) {
        final boolean[] failed = {false};
        final FailureHandler handler = (error, viewMatcher) -> failed[0] = true;
        withShortTimeout(() -> onView(withText(textResId)).inRoot(isDialog())
                .withFailureHandler(handler).check(matches(isDisplayed())));
        return !failed[0];
    }

    private static void withShortTimeout(Runnable interaction) {
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.SECONDS);
        try {
            interaction.run();
        } catch (RuntimeException ignored) {
            // The dialog is not present.
        } finally {
            IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
            IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        }
    }
}
