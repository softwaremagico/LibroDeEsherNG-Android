package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Creates and levels Nihal using only app UI interactions.
 */
@RunWith(AndroidJUnit4.class)
public class NihalEndToEndUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void createNihalFromScratchAndReachLevelNineUsingOnlyUi() throws Exception {
        // Start fresh
        onView(withId(R.id.action_new_character)).perform(click());
        // Go to creation tab is default; fill fields
        onView(withId(R.id.name_input)).perform(replaceText("Nihal Dragoniana"));
        // Sex: Female - select by index (Varón/Hembra typically)
        // Race: Laan
        onView(withId(R.id.race_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), containsString("Laan"))).perform(click());
        // Culture: Litoral Urbana (Clase Alta)
        onView(withId(R.id.culture_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), containsString("Litoral Urbana"))).perform(click());
        // Profession: Lord del Caos
        onView(withId(R.id.profession_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), containsString("Lord del Caos"))).perform(click());
        // Level 9
        // NumberPicker - use swipe or set
        // Generate
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        // Confirm characteristics if needed
        try {
            onView(withId(R.id.characteristics_confirm)).perform(click());
        } catch (Exception ignored) {}
        // Level up 8 more times to reach 9 (start at 1)
        for (int i = 0; i < 8; i++) {
            onView(withId(R.id.navigation_sheet)).perform(click());
            onView(withId(R.id.sheet_level_up_button)).perform(click());
            onData(instanceOf(String.class)).atPosition(0).perform(click());
            // Return to character tab
            onView(withId(R.id.navigation_home)).perform(click());
        }
        // Check sheet shows level 9
        onView(withId(R.id.navigation_sheet)).perform(click());
        onView(withId(R.id.sheet_text)).check(matches(withText(containsString("Level 9"))));
        onView(withId(R.id.sheet_text)).check(matches(withText(containsString("Nihal Dragoniana"))));
        onView(withId(R.id.sheet_text)).check(matches(withText(containsString("Laan"))));
    }
}
