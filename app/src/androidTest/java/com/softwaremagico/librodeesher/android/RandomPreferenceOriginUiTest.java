package com.softwaremagico.librodeesher.android;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** UI-only proof that configured origin and characteristic ranks drive random generation. */
@RunWith(AndroidJUnit4.class)
public class RandomPreferenceOriginUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void configuredOriginAndCharacteristicRanksAreApplied() {
        openPreferences();
        onView(withId(R.id.random_race_preference)).perform(UiViews.setText("horseCentaur"));
        onView(withId(R.id.random_culture_preference)).perform(UiViews.setText("aquaticMilitarista"));
        onView(withId(R.id.random_profession_preference)).perform(UiViews.setText("bard"));
        onView(withId(R.id.random_characteristic_ranks)).perform(UiViews.setText("AG:95,ST:88"));
        applyPreferences();
        UiViews.pressBackDirectly();

        try {
            openPreferences();
            onView(withId(R.id.random_race_preference)).check(matches(withText("horseCentaur")));
            onView(withId(R.id.random_culture_preference)).check(matches(withText("aquaticMilitarista")));
            onView(withId(R.id.random_profession_preference)).check(matches(withText("bard")));
            onView(withId(R.id.random_characteristic_ranks)).check(matches(withText("AG:95,ST:88")));
            UiViews.pressBackDirectly();

            onView(withId(R.id.navigation_random)).perform(UiViews.clickDirectly());
            onView(withId(R.id.random_generate_button)).perform(click());
            onView(withId(R.id.random_summary_text)).check(matches(isDisplayed()));
            onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Raza: Centauro Caballo")));
            onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Cultura: Acuática Militarista")));
            onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Profesión: Bardo")));
            onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Ag: 95")));
            onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Fu: 88")));
        } finally {
            // An empty preference inherits the session selection, which is the default behaviour.
            openPreferences();
            onView(withId(R.id.random_race_preference)).perform(UiViews.setText(""));
            onView(withId(R.id.random_culture_preference)).perform(UiViews.setText(""));
            onView(withId(R.id.random_profession_preference)).perform(UiViews.setText(""));
            onView(withId(R.id.random_characteristic_ranks)).perform(UiViews.setText(""));
            applyPreferences();
        }
    }

    private void openPreferences() {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        UiMenus.click(R.string.random_config);
    }

    private void applyPreferences() {
        onView(withId(R.id.random_apply_prefs)).perform(UiViews.clickDirectly());
    }
}
