package com.softwaremagico.librodeesher.android;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** Shared UI-only helpers for dialogs that may or may not appear between test steps. */
public final class UiDialogs {

    private UiDialogs() {
        // Utility class.
    }

    /**
     * Espresso's {@code inRoot(isDialog())} lookup waits a hardcoded 60 seconds when no dialog is
     * present. The default root matcher returns immediately (the Activity root is always there),
     * so inspecting the window type of every root avoids that timeout without relying on
     * transient window-focus changes.
     */
    private static boolean hasVisibleDialog() {
        final boolean[] found = {false};
        final ViewAction inspectRoot = new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "inspect root window type";
            }

            @Override
            public void perform(UiController controller, View view) {
                final ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params instanceof WindowManager.LayoutParams
                        && ((WindowManager.LayoutParams) params).type
                                == WindowManager.LayoutParams.TYPE_APPLICATION) {
                    found[0] = true;
                }
            }
        };
        withShortTimeout(() -> onView(isRoot()).perform(inspectRoot));
        return found[0];
    }

    private static boolean waitForDialog(long timeoutMillis) {
        final long deadline = SystemClock.uptimeMillis() + timeoutMillis;
        while (SystemClock.uptimeMillis() < deadline) {
            if (hasVisibleDialog()) {
                return true;
            }
            SystemClock.sleep(50);
        }
        return false;
    }

    private static void waitForDialogToClose() {
        final long deadline = SystemClock.uptimeMillis() + 1500;
        while (SystemClock.uptimeMillis() < deadline && hasVisibleDialog()) {
            SystemClock.sleep(50);
        }
    }

    /** Confirms discarding edits only when the guard actually asked for confirmation. */
    public static void discardUnsavedChangesIfPrompted() {
        if (clickDialogButtonIfPresent(R.string.unsaved_changes_continue)) {
            waitForDialogToClose();
        }
    }

    /** Clicks a dialog button only when it is present; returns whether it was clicked. */
    public static boolean clickDialogButtonIfPresent(int textResId) {
        if (!waitForDialog(1500)) {
            return false;
        }
        final boolean[] failed = {false};
        final FailureHandler handler = (error, viewMatcher) -> failed[0] = true;
        boolean completed = withShortTimeout(() -> onView(withText(textResId))
                .withFailureHandler(handler).perform(click()));
        return completed && !failed[0];
    }

    /** Checks whether a labelled dialog is visible without waiting for the global idle timeout. */
    public static boolean isDialogTextPresent(int textResId) {
        if (!waitForDialog(1500)) {
            return false;
        }
        final boolean[] failed = {false};
        final FailureHandler handler = (error, viewMatcher) -> failed[0] = true;
        boolean completed = withShortTimeout(() -> onView(withText(textResId))
                .withFailureHandler(handler).check(matches(isDisplayed())));
        return completed && !failed[0];
    }

    /** Closes a leftover dialog before navigating to another screen. */
    public static void dismissAnyDialogIfPresent() {
        if (hasVisibleDialog()) {
            UiViews.pressBackDirectly();
            waitForDialogToClose();
        }
    }

    private static boolean withShortTimeout(Runnable interaction) {
        androidx.test.espresso.IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS);
        androidx.test.espresso.IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.SECONDS);
        try {
            interaction.run();
            return true;
        } catch (RuntimeException ignored) {
            // The dialog is not present.
            return false;
        } finally {
            androidx.test.espresso.IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
            androidx.test.espresso.IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        }
    }
}
