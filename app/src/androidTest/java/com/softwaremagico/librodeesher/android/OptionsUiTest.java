package com.softwaremagico.librodeesher.android;

import android.view.View;
import android.widget.CheckBox;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** UI-only coverage of the legacy options window. */
@RunWith(AndroidJUnit4.class)
public class OptionsUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void optionsArePersistedAcrossVisits() {
        openOptions();
        setChecked(R.id.option_debug_mode, false);
        onView(withId(R.id.option_save)).perform(scrollTo(), click());
        Espresso.pressBack();

        openOptions();
        onView(withId(R.id.option_debug_mode)).check(matches(isNotChecked()));
        setChecked(R.id.option_debug_mode, true);
        onView(withId(R.id.option_save)).perform(scrollTo(), click());
        Espresso.pressBack();

        openOptions();
        onView(withId(R.id.option_debug_mode)).check(matches(isChecked()));
        setChecked(R.id.option_debug_mode, false);
        onView(withId(R.id.option_save)).perform(scrollTo(), click());
    }

    private void openOptions() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText(R.string.navigation_options)).perform(click());
    }

    /** Clicks the checkbox only when needed, so the test never depends on a previous run's state. */
    private static void setChecked(int id, boolean checked) {
        onView(withId(id)).perform(scrollTo(), new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CheckBox.class);
            }

            @Override
            public String getDescription() {
                return "set checkbox state";
            }

            @Override
            public void perform(UiController controller, View view) {
                final CheckBox box = (CheckBox) view;
                if (box.isChecked() != checked) {
                    box.performClick();
                }
            }
        });
    }
}
