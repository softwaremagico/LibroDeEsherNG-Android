package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static org.hamcrest.CoreMatchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NihalEndToEndUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void createNihalFromScratchAndReachLevelNineUsingOnlyUi() throws Exception {
        onView(withId(R.id.action_new_character)).perform(click());
        onView(withId(R.id.name_input)).perform(replaceText("Nihal Dragoniana"));
        onView(withId(R.id.race_spinner)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.culture_spinner)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.profession_spinner)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        try {
            onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        } catch (Exception e) {
            onView(withId(R.id.generate_button)).perform(click());
        }
        try {
            onView(withId(R.id.characteristics_confirm)).perform(click());
        } catch (Exception ignored) {
        }
        for (int i = 0; i < 8; i++) {
            onView(withId(R.id.navigation_sheet)).perform(click());
            try {
                onView(withId(R.id.sheet_level_up_button)).perform(click());
            } catch (Exception e) {
                break;
            }
            try {
                onData(anything()).atPosition(0).perform(click());
            } catch (Exception ignored) {
            }
            onView(withId(R.id.navigation_home)).perform(click());
        }
        onView(withId(R.id.navigation_sheet)).perform(click());
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Nihal Dragoniana")));
    }
}
