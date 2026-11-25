package com.softwaremagico.librodeesher.android;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
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
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
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
        onView(withId(R.id.random_apply_prefs)).check(matches(withText(R.string.random_apply_preferences)));
        onView(withId(R.id.random_specialization_seekbar)).perform(setProgress(5));
        onView(withId(R.id.random_specialization_value)).check(matches(withText("2")));
        scrollToApply();
        onView(withId(R.id.random_apply_prefs)).perform(click());
        Espresso.pressBack();

        openPreferences();
        onView(withId(R.id.random_specialization_seekbar)).check(hasProgress(5));
        scrollToApply();
        onView(withId(R.id.random_apply_prefs)).perform(click());
    }

    @Test
    public void fullPreferencesAreSavedAndShownAgain() {
        openPreferences();
        onView(withId(R.id.random_final_level)).perform(scrollTo(), replaceText("5"));
        onView(withId(R.id.random_perks_enabled)).perform(scrollTo(), setChecked(false));
        onView(withId(R.id.random_category_ranks)).perform(scrollTo(), replaceText("arms:5,combat:3"));
        onView(withId(R.id.random_skill_ranks)).perform(scrollTo(), replaceText("attack:4"));
        onView(withId(R.id.random_trainings)).perform(scrollTo(), replaceText("trainingOne,trainingTwo"));
        onView(withId(R.id.random_suggested_perks)).perform(scrollTo(), replaceText("ambidextrous"));
        scrollToApply();
        onView(withId(R.id.random_apply_prefs)).perform(click());
        onView(withId(R.id.random_progress)).check(matches(isDisplayed()));
        Espresso.pressBack();

        openPreferences();
        onView(withId(R.id.random_final_level)).check(matches(withText("5")));
        onView(withId(R.id.random_perks_enabled)).check(matches(isNotChecked()));
        onView(withId(R.id.random_category_ranks)).check(matches(withText("arms:5,combat:3")));
        onView(withId(R.id.random_skill_ranks)).check(matches(withText("attack:4")));
        onView(withId(R.id.random_trainings)).check(matches(withText("trainingOne,trainingTwo")));
        onView(withId(R.id.random_suggested_perks)).check(matches(withText("ambidextrous")));
        onView(withId(R.id.random_perks_enabled)).perform(scrollTo(), setChecked(true));
        onView(withId(R.id.random_perks_enabled)).check(matches(isChecked()));
    }

    private void scrollToApply() {
        onView(withId(R.id.random_preferences_scroll)).perform(swipeUp(), swipeUp(), swipeUp(), swipeUp());
    }

    private void openPreferences() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText(R.string.random_config)).inRoot(isPlatformPopup()).perform(click());
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
