package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/** UI-only coverage for the legacy actions restored by the AWT parity audit (issue #22). */
@RunWith(AndroidJUnit4.class)
public class AwtParityUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void characteristicsCanBeRandomizedFromTheUi() {
        onView(withId(R.id.action_new_character)).perform(click());
        selectRaceCultureProfession();

        openTab("Características");
        android.os.SystemClock.sleep(700);
        onView(withId(R.id.characteristics_random)).check(matches(allOf(isDisplayed(), isEnabled())));

        onView(withId(R.id.characteristics_random)).perform(click());
        onView(withId(R.id.characteristics_random)).check(matches(not(isEnabled())));
        onView(withId(R.id.characteristics_confirm)).check(matches(not(isEnabled())));
        onView(withId(R.id.characteristics_points)).check(matches(withSubstring("/")));
    }

    @Test
    public void perkOptionsAndWeaknessCanBeChosenFromTheUi() {
        createCompleteCharacter();

        openTab("Dotes");
        android.os.SystemClock.sleep(700);
        onView(allOf(withId(R.id.item_perk_name), withText("Experimentado (Cat) (Mínimo)")))
                .perform(scrollTo(), clickPerkAction());

        // With "Las dotes consumen puntos de trasfondo" enabled, the legacy flow offers a weakness.
        dismissWeaknessDialogIfPresent();

        onView(withText(R.string.perks_choice_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        onView(withText(R.string.accept)).inRoot(isDialog()).perform(click());

        android.os.SystemClock.sleep(700);
        onView(allOf(withId(R.id.item_perk_name), withText("Experimentado (Cat) (Mínimo)")))
                .perform(scrollTo()).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.item_perk_options), isDisplayed())).check(matches(isDisplayed()));
    }

    private void selectRaceCultureProfession() {
        onView(withId(R.id.sex_spinner)).perform(click());
        onData(is("Mujer")).perform(click());

        onView(withId(R.id.race_spinner)).perform(click());
        onData(hasToString("Race[laan]")).perform(click());

        onView(withId(R.id.culture_spinner)).perform(click());
        onData(hasToString("Culture[coastalUrbanClassHigh]")).perform(click());

        onView(withId(R.id.profession_spinner)).perform(click());
        onData(hasToString("Profession[lordOfTheChaos]")).perform(click());
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(click());
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Auditoría"));
        selectRaceCultureProfession();
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        try {
            onView(withId(R.id.characteristics_confirm)).perform(click());
        } catch (Exception ignored) {
            // Some builds confirm the characteristics automatically.
        }
        onView(withId(R.id.navigation_home)).perform(click());
    }

    /** Clicks the +/- action of the row that contains the matched perk name. */
    private static ViewAction clickPerkAction() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(android.widget.TextView.class);
            }

            @Override
            public String getDescription() {
                return "click perk row action";
            }

            @Override
            public void perform(UiController controller, View view) {
                final View row = view.getParent() instanceof View
                        ? (View) view.getParent().getParent() : view;
                final View action = row.findViewById(R.id.item_perk_action);
                if (action == null) {
                    throw new AssertionError("No action button in perk row");
                }
                action.performClick();
            }
        };
    }

    private void dismissWeaknessDialogIfPresent() {
        try {
            onView(allOf(withText(R.string.perks_no_weakness), isDisplayed()))
                    .inRoot(isDialog()).perform(click());
        } catch (RuntimeException ignored) {
            // No eligible weakness for this perk in the enabled modules.
        }
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
    }
}
