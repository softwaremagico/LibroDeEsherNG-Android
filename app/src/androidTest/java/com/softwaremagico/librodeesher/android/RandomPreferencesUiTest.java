package com.softwaremagico.librodeesher.android;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** UI-only coverage of the random-generation preferences screen and its persistence. */
@RunWith(AndroidJUnit4.class)
public class RandomPreferencesUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void specializationIsSavedAndShownAgain() {
        openPreferences();
        onView(withId(R.id.random_apply_prefs))
                .check(matches(withContentDescription(R.string.random_apply_description)));
        onView(withId(R.id.random_specialization_seekbar)).perform(setProgress(5));
        onView(withId(R.id.random_specialization_value)).check(matches(withText("2")));
        applyPreferences();
        UiViews.pressBackDirectly();

        openPreferences();
        onView(withId(R.id.random_specialization_seekbar)).check(hasProgress(5));
        applyPreferences();
    }

    @Test
    public void fullPreferencesAreSavedAndShownAgain() {
        openPreferences();
        onView(withId(R.id.random_final_level)).perform(setText("5"));
        onView(withId(R.id.random_perks_enabled)).perform(setChecked(false));
        onView(withId(R.id.random_category_ranks)).perform(setText("arms:5,combat:3"));
        onView(withId(R.id.random_skill_ranks)).perform(setText("attack:4"));
        onView(withId(R.id.random_trainings)).perform(setText("trainingOne,trainingTwo"));
        onView(withId(R.id.random_suggested_perks)).perform(setText("ambidextrous"));
        applyPreferences();
        onView(withId(R.id.random_progress)).check(matches(isDisplayed()));
        UiViews.pressBackDirectly();

        openPreferences();
        onView(withId(R.id.random_final_level)).check(matches(withText("5")));
        onView(withId(R.id.random_perks_enabled)).check(matches(isNotChecked()));
        onView(withId(R.id.random_category_ranks)).check(matches(withText("arms:5,combat:3")));
        onView(withId(R.id.random_skill_ranks)).check(matches(withText("attack:4")));
        onView(withId(R.id.random_trainings)).check(matches(withText("trainingOne,trainingTwo")));
        onView(withId(R.id.random_suggested_perks)).check(matches(withText("ambidextrous")));
        onView(withId(R.id.random_perks_enabled)).perform(setChecked(true));
        onView(withId(R.id.random_perks_enabled)).check(matches(isChecked()));
    }

    @Test
    public void changesAreSavedWithoutExplicitApply() {
        openPreferences();
        onView(withId(R.id.random_final_level)).perform(setText("7"));
        onView(withId(R.id.random_perks_enabled)).perform(setChecked(false));
        UiViews.pressBackDirectly();

        openPreferences();
        onView(withId(R.id.random_final_level)).check(matches(withText("7")));
        onView(withId(R.id.random_perks_enabled)).check(matches(isNotChecked()));
        onView(withId(R.id.random_perks_enabled)).perform(setChecked(true));
    }

    /** The Material FAB is pinned to the bottom; click it directly instead of scrolling. */
    private void applyPreferences() {
        onView(withId(R.id.random_apply_prefs)).perform(UiViews.clickDirectly());
    }

    private void openPreferences() {
        UiMenus.click(R.string.random_config);
    }

    /** Sets text without scrolling: scrollTo() wakes animations that never let Espresso idle. */
    private static ViewAction setText(String value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(EditText.class);
            }

            @Override
            public String getDescription() {
                return "set text to " + value;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((EditText) view).setText(value);
                uiController.loopMainThreadForAtLeast(50);
            }
        };
    }

    /** Toggles only when needed, so the test does not depend on preferences left by earlier runs. */
    private static ViewAction setChecked(boolean checked) {
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
            public void perform(UiController uiController, View view) {
                final CompoundButton button = (CompoundButton) view;
                if (button.isChecked() != checked) {
                    button.performClick();
                }
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private static ViewAction setProgress(int progress) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }

            @Override
            public String getDescription() {
                return "set SeekBar progress to " + progress;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((SeekBar) view).setProgress(progress);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    private static ViewAssertion hasProgress(int expected) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            final int actual = ((SeekBar) view).getProgress();
            if (actual != expected) {
                throw new AssertionError("Expected SeekBar progress " + expected + " but was " + actual);
            }
        };
    }
}
