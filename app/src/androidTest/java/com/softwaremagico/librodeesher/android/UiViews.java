package com.softwaremagico.librodeesher.android;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** UI-only helpers for the dynamically-built rows used by the development screens. */
public final class UiViews {

    private UiViews() {
        // Utility class.
    }

    /** Waits for a TextView to contain expected text while allowing the UI thread to process work. */
    public static ViewAction waitForText(String expected, long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(android.widget.TextView.class);
            }

            @Override
            public String getDescription() {
                return "wait for text containing " + expected;
            }

            @Override
            public void perform(UiController controller, View view) {
                final long deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis;
                do {
                    final CharSequence text = ((android.widget.TextView) view).getText();
                    if (text != null && text.toString().contains(expected)) {
                        return;
                    }
                    controller.loopMainThreadForAtLeast(100);
                } while (android.os.SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out waiting for text containing " + expected);
            }
        };
    }

    /** Waits until a dynamically-populated container reaches the requested number of children. */
    public static ViewAction waitForChildCount(int minimum, long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "wait for at least " + minimum + " children";
            }

            @Override
            public void perform(UiController controller, View view) {
                final long deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis;
                do {
                    if (((ViewGroup) view).getChildCount() >= minimum) {
                        return;
                    }
                    controller.loopMainThreadForAtLeast(100);
                } while (android.os.SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out waiting for " + minimum + " children");
            }
        };
    }

    /** Waits until a container has an enabled child with {@code childId}. */
    public static ViewAction waitForEnabledChild(int containerId, int childId, long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "wait for enabled child " + childId + " in " + containerId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final long deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis;
                final List<View> matches = new ArrayList<>();
                do {
                    matches.clear();
                    collect(view, childId, matches);
                    for (final View match : matches) {
                        if (match.isEnabled()) {
                            return;
                        }
                    }
                    controller.loopMainThreadForAtLeast(100);
                } while (android.os.SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out waiting for enabled child " + childId);
            }
        };
    }

    /** Waits until a container shows rows or its explicit empty state. */
    public static ViewAction waitForRowsOrEmpty(int emptyViewId, long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "wait for rows or empty state " + emptyViewId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final long deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis;
                do {
                    final View emptyView = findInAncestors(view, emptyViewId);
                    final boolean hasRows = ((ViewGroup) view).getChildCount() > 0;
                    final boolean emptyShown = emptyView != null
                            && emptyView.getVisibility() == View.VISIBLE;
                    if (hasRows || emptyShown) {
                        return;
                    }
                    controller.loopMainThreadForAtLeast(100);
                } while (android.os.SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out waiting for rows or empty state " + emptyViewId);
            }
        };
    }

    /** Waits until the viewPager finishes scrolling and the view becomes visible on screen. */
    public static ViewAction waitForDisplayed(long timeoutMillis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "wait until the view is displayed";
            }

            @Override
            public void perform(UiController controller, View view) {
                final long deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis;
                final android.graphics.Rect rect = new android.graphics.Rect();
                do {
                    if (view.getVisibility() == View.VISIBLE && view.getGlobalVisibleRect(rect)) {
                        return;
                    }
                    controller.loopMainThreadForAtLeast(100);
                } while (android.os.SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out waiting for the view to be displayed");
            }
        };
    }

    /** Finds a sibling view in the closest ancestor, avoiding duplicates from other tabbed pages. */
    private static View findInAncestors(View view, int viewId) {
        View current = view;
        while (current != null) {
            if (current instanceof ViewGroup) {
                final View found = ((ViewGroup) current).findViewById(viewId);
                if (found != null) {
                    return found;
                }
            }
            current = current.getParent() instanceof View ? (View) current.getParent() : null;
        }
        return null;
    }

    /**
     * Runs an optional interaction with a short timeout. A missing optional view must not wait for
     * the 15-minute policy that slow rule loading needs for required views.
     */
    public static void safely(Runnable interaction) {
        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.SECONDS);
        try {
            interaction.run();
        } catch (RuntimeException | AssertionError ignored) {
            // The option was not available for this character/module combination.
        } finally {
            IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
            IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        }
    }

    /** Sends a real BACK key without waiting for Espresso's idle conditions. */
    public static void pressBackDirectly() {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
    }

    /** Clicks a view directly, without requiring it to be scrolled into view first. */
    public static ViewAction clickDirectly() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "direct click";
            }

            @Override
            public void perform(UiController controller, View view) {
                view.performClick();
            }
        };
    }

    /**
     * Clicks the nearest clickable ancestor of a view. Material tab labels are plain TextViews,
     * while the click listener lives on their TabView parent.
     */
    public static ViewAction clickClickableAncestor() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "click clickable ancestor";
            }

            @Override
            public void perform(UiController controller, View view) {
                View current = view;
                while (current != null) {
                    if (current.isClickable()) {
                        current.performClick();
                        return;
                    }
                    current = current.getParent() instanceof View ? (View) current.getParent() : null;
                }
                throw new AssertionError("No clickable ancestor for tab");
            }
        };
    }

    /** Clicks the first row of a dialog without emulating a motion event or waiting for idle. */
    public static ViewAction clickFirstDialogRow() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override
            public String getDescription() {
                return "click first dialog row";
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> rows = new ArrayList<>();
                collect(view, android.R.id.text1, rows);
                for (final View row : rows) {
                    if (!row.isEnabled() || !row.getGlobalVisibleRect(new android.graphics.Rect())) {
                        continue;
                    }
                    View child = row;
                    while (child != null) {
                        final android.view.ViewParent parent = child.getParent();
                        if (parent instanceof android.widget.AdapterView) {
                            final android.widget.AdapterView<?> list = (android.widget.AdapterView<?>) parent;
                            final int position = list.indexOfChild(child);
                            if (list.getAdapter() != null && position >= 0) {
                                list.performItemClick(child, position, list.getAdapter().getItemId(position));
                                return;
                            }
                        }
                        if (child.isClickable() && child.performClick()) {
                            return;
                        }
                        child = parent instanceof View ? (View) parent : null;
                    }
                    row.performClick();
                    return;
                }
                throw new AssertionError("Dialog has no visible row to click");
            }
        };
    }

    /** Sets text directly on an EditText, without depending on scroll position or the IME. */
    public static ViewAction setText(String value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(android.widget.EditText.class);
            }

            @Override
            public String getDescription() {
                return "set text to " + value;
            }

            @Override
            public void perform(UiController controller, View view) {
                ((android.widget.EditText) view).setText(value);
            }
        };
    }

    /** Sets a NumberPicker value without emulating many scroll gestures. */
    public static ViewAction setNumberPickerValue(int value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "set number picker to " + value;
            }

            @Override
            public void perform(UiController controller, View view) {
                ((NumberPicker) view).setValue(value);
            }
        };
    }

    /** Sets a checkbox without depending on its current state or scroll position. */
    public static ViewAction setChecked(boolean checked) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "set checkbox to " + checked;
            }

            @Override
            public void perform(UiController controller, View view) {
                final CompoundButton button = (CompoundButton) view;
                if (button.isChecked() != checked) {
                    button.performClick();
                }
            }
        };
    }

    /** Clicks the first enabled child with {@code childId} inside {@code containerId}. */
    public static ViewAction clickFirstEnabled(int containerId, int childId) {
        return clickChild(containerId, childId, true);
    }

    /** Selects an adapter entry on a Spinner without opening the system popup. */
    public static ViewAction selectSpinnerContaining(String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(Spinner.class);
            }

            @Override
            public String getDescription() {
                return "select spinner entry containing " + text;
            }

            @Override
            public void perform(UiController controller, View view) {
                final Spinner spinner = (Spinner) view;
                final android.widget.SpinnerAdapter adapter = spinner.getAdapter();
                if (adapter == null) {
                    throw new AssertionError("Spinner has no adapter");
                }
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (String.valueOf(adapter.getItem(i)).contains(text)) {
                        spinner.setSelection(i);
                        return;
                    }
                }
                throw new AssertionError("Spinner entry not found: " + text);
            }
        };
    }

    /** Selects an adapter position on a Spinner without opening the system popup. */
    public static ViewAction selectSpinnerPosition(int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(Spinner.class);
            }

            @Override
            public String getDescription() {
                return "select spinner position " + position;
            }

            @Override
            public void perform(UiController controller, View view) {
                ((Spinner) view).setSelection(position);
            }
        };
    }

    /** Selects a category containing an enabled row action and clicks that action. */
    public static ViewAction selectCategoryAndClickEnabled(int containerId, int actionId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(Spinner.class);
            }

            @Override
            public String getDescription() {
                return "select a category with an enabled row action";
            }

            @Override
            public void perform(UiController controller, View view) {
                final Spinner spinner = (Spinner) view;
                android.widget.SpinnerAdapter adapter = spinner.getAdapter();
                final long deadline = android.os.SystemClock.uptimeMillis() + 10_000;
                while (adapter == null && android.os.SystemClock.uptimeMillis() < deadline) {
                    controller.loopMainThreadForAtLeast(100);
                    adapter = spinner.getAdapter();
                }
                if (adapter == null) {
                    final View empty = view.getRootView().findViewById(R.id.spells_empty);
                    final String state = empty instanceof android.widget.TextView
                            ? String.valueOf(((android.widget.TextView) empty).getText()) : "not present";
                    throw new AssertionError("Category spinner has no adapter; spells empty state: " + state);
                }
                for (int i = 0; i < adapter.getCount(); i++) {
                    spinner.setSelection(i);
                    controller.loopMainThreadForAtLeast(150);
                    final View container = view.getRootView().findViewById(containerId);
                    if (container == null) {
                        continue;
                    }
                    final List<View> actions = new ArrayList<>();
                    collect(container, actionId, actions);
                    for (final View action : actions) {
                        if (action.isEnabled() && action.getVisibility() == View.VISIBLE) {
                            action.performClick();
                            return;
                        }
                    }
                }
                throw new AssertionError("No category has an enabled row action " + actionId);
            }
        };
    }

    /** Clicks the action button of the row whose name contains {@code textPart}. */
    public static ViewAction clickRowActionContaining(int containerId, int nameId, int actionId,
                                                      String textPart) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "action for row containing " + textPart;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> names = new ArrayList<>();
                collect(view, nameId, names);
                for (final View name : names) {
                    final CharSequence label = ((android.widget.TextView) name).getText();
                    if (label != null && label.toString().contains(textPart)) {
                        View row = name;
                        while (row != null && row.findViewById(actionId) == null) {
                            row = row.getParent() instanceof View ? (View) row.getParent() : null;
                        }
                        if (row != null) {
                            row.findViewById(actionId).performClick();
                            return;
                        }
                    }
                }
                throw new AssertionError("No row containing " + textPart + " in container " + containerId);
            }
        };
    }

    /** Clicks the nth visible, enabled Button with the given label inside a container. */
    public static ViewAction clickNthButtonWithText(int containerId, String text, int index) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "button " + index + " labelled " + text;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> buttons = new ArrayList<>();
                collectOfType(view, Button.class, buttons);
                final StringBuilder available = new StringBuilder();
                for (final View button : buttons) {
                    available.append(((Button) button).getText()).append('/')
                            .append(button.isEnabled() ? "enabled" : "disabled").append(' ');
                }
                int found = 0;
                for (final View button : buttons) {
                    if (text.equals(((Button) button).getText().toString())
                            && button.isEnabled() && button.getGlobalVisibleRect(new android.graphics.Rect())) {
                        if (found++ == index) {
                            button.performClick();
                            return;
                        }
                    }
                }
                final List<View> names = new ArrayList<>();
                collect(view, R.id.item_perk_name, names);
                final StringBuilder labels = new StringBuilder();
                for (final View name : names) {
                    labels.append(((android.widget.TextView) name).getText()).append(' ');
                }
                throw new AssertionError("No visible enabled button " + text + " at index " + index
                        + " in container " + containerId + "; buttons=" + available + "; names=" + labels);
            }
        };
    }

    /** Clicks the first labelled button that has a listener, even if a render moved it off-screen. */
    public static ViewAction clickButtonWithListener(int containerId, String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "button with listener labelled " + text;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> buttons = new ArrayList<>();
                collectOfType(view, Button.class, buttons);
                for (final View button : buttons) {
                    if (text.equals(((Button) button).getText().toString())
                            && button.isEnabled() && button.hasOnClickListeners()) {
                        button.performClick();
                        return;
                    }
                }
                throw new AssertionError("No enabled button with listener " + text
                        + " in container " + containerId);
            }
        };
    }

    /** Clicks an enabled button with the requested label inside a dynamic container. */
    public static ViewAction clickEnabledButtonWithText(String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "click enabled button labelled " + text;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> buttons = new ArrayList<>();
                collectOfType(view, Button.class, buttons);
                for (final View candidate : buttons) {
                    final Button button = (Button) candidate;
                    if (button.isEnabled() && button.getVisibility() == View.VISIBLE
                            && text.contentEquals(button.getText()) && button.performClick()) {
                        return;
                    }
                }
                throw new AssertionError("No enabled button labelled " + text);
            }
        };
    }

    /** Clicks the first Button inside a dynamically-populated container. */
    public static ViewAction clickFirstButton(int containerId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "first button in container " + containerId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> matches = new ArrayList<>();
                collectOfType(view, Button.class, matches);
                if (matches.isEmpty()) {
                    throw new AssertionError("No button in container " + containerId);
                }
                matches.get(0).performClick();
            }
        };
    }

    /** Selects the second option of the first Spinner inside a dynamically-populated container. */
    public static ViewAction selectFirstSpinnerOption(int containerId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "first spinner option in container " + containerId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> matches = new ArrayList<>();
                collectOfType(view, Spinner.class, matches);
                if (matches.isEmpty() || ((Spinner) matches.get(0)).getAdapter() == null
                        || ((Spinner) matches.get(0)).getAdapter().getCount() < 2) {
                    throw new AssertionError("No selectable spinner in container " + containerId);
                }
                ((Spinner) matches.get(0)).setSelection(1);
            }
        };
    }

    /** Clicks the first child with {@code childId} inside {@code containerId}. */
    public static ViewAction clickFirst(int containerId, int childId) {
        return clickChild(containerId, childId, false);
    }

    /** Clicks the first visible child with {@code childId} inside {@code containerId}. */
    public static ViewAction clickFirstVisible(int containerId, int childId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "first visible child " + childId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> matches = new ArrayList<>();
                collect(view, childId, matches);
                for (final View match : matches) {
                    if (match.getGlobalVisibleRect(new android.graphics.Rect()) && match.isEnabled()) {
                        match.performClick();
                        return;
                    }
                }
                throw new AssertionError("No visible child " + childId + " in container " + containerId);
            }
        };
    }

    private static ViewAction clickChild(int containerId, int childId, boolean enabledOnly) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ViewGroup.class);
            }

            @Override
            public String getDescription() {
                return "first " + (enabledOnly ? "enabled " : "") + "child " + childId;
            }

            @Override
            public void perform(UiController controller, View view) {
                final List<View> matches = new ArrayList<>();
                collect(view, childId, matches);
                for (final View match : matches) {
                    if (!enabledOnly || match.isEnabled()) {
                        match.performClick();
                        return;
                    }
                }
                throw new AssertionError("No matching child " + childId + " in container " + containerId);
            }
        };
    }

    private static void collectOfType(View view, Class<?> type, List<View> matches) {
        if (type.isInstance(view)) {
            matches.add(view);
        }
        if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectOfType(group.getChildAt(i), type, matches);
            }
        }
    }

    private static void collect(View view, int childId, List<View> matches) {
        if (view.getId() == childId) {
            matches.add(view);
        }
        if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collect(group.getChildAt(i), childId, matches);
            }
        }
    }
}
