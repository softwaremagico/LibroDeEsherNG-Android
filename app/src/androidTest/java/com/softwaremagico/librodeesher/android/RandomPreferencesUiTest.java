package com.softwaremagico.librodeesher.android;

import android.view.View;
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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
        onView(withId(R.id.random_apply_prefs)).perform(click());
        Espresso.pressBack();

        openPreferences();
        onView(withId(R.id.random_specialization_seekbar)).check(hasProgress(5));
        onView(withId(R.id.random_apply_prefs)).perform(click());
    }

    private void openPreferences() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText(R.string.random_config)).perform(click());
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
