package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Creates and levels Nihal using only app UI interactions.
 */
@RunWith(AndroidJUnit4.class)
public class NihalEndToEndUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowTheRepeatedLevelUps() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void createNihalFromScratchAndReachLevelNineUsingOnlyUi() throws Exception {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Nihal Dragoniana"));

        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));

        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));

        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));

        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));

        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        for (int level = 2; level <= 9; level++) {
            onView(withId(R.id.sheet_level_up_button)).perform(click());
            onView(withText(R.string.sheet_level_up_auto)).inRoot(isDialog()).perform(click());
            onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level " + level)));
        }

        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Nihal Dragoniana")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level 9")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Laan")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Litoral Urbana (Clase Alta)")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Lord del Caos")));
        PdfExportCheck.exportAndVerify("Nihal Dragoniana", 9, "Laan", "Litoral Urbana (Clase Alta)",
                "Lord del Caos");
    }
}
