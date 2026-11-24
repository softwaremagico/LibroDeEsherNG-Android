package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.widget.TextView;

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
import java.util.concurrent.atomic.AtomicReference;

/** UI-only coverage for the interactive background and perks screens (issue #6). */
@RunWith(AndroidJUnit4.class)
public class HistoryPerksUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void backgroundSpendingAndPerkActionsAreReflectedInTheUi() {
        createCharacterWithUi();

        openTab("Dotes");
        onView(withId(R.id.perks_history_points)).check(matches(withSubstring("Puntos de Historial")));
        final String initialPoints = textOf(R.id.perks_history_points);

        openTab("Historial");
        onView(allOf(withText("Mejorar AG"), isDescendantOfA(withId(R.id.history_container))))
                .perform(scrollTo(), click());

        openTab("Dotes");
        final String afterUpgrade = textOf(R.id.perks_history_points);
        org.junit.Assert.assertFalse("El gasto de trasfondo debe descontarse en Dotes",
                initialPoints.equals(afterUpgrade));

        onView(withId(R.id.perks_sort_cost)).perform(click());
        onView(withId(R.id.perks_random)).perform(click());
        onView(withId(R.id.perks_container)).check(matches(hasMinimumChildCount(1)));
    }

    private void createCharacterWithUi() {
        onView(withId(R.id.action_new_character)).perform(click());
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Historial"));

        onView(withId(R.id.sex_spinner)).perform(click());
        onData(is("Mujer")).perform(click());

        onView(withId(R.id.race_spinner)).perform(click());
        onData(hasToString("Race[laan]")).perform(click());

        onView(withId(R.id.culture_spinner)).perform(click());
        onData(hasToString("Culture[coastalUrbanClassHigh]")).perform(click());

        onView(withId(R.id.profession_spinner)).perform(click());
        onData(hasToString("Profession[lordOfTheChaos]")).perform(click());

        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        try {
            onView(withId(R.id.characteristics_confirm)).perform(click());
        } catch (Exception ignored) {
            // Some builds confirm the characteristics automatically.
        }
        onView(withId(R.id.navigation_home)).perform(click());
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
    }

    private static String textOf(int viewId) {
        final AtomicReference<String> result = new AtomicReference<>();
        onView(withId(viewId)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "read text";
            }

            @Override
            public void perform(UiController controller, View view) {
                result.set(((TextView) view).getText().toString());
            }
        });
        return result.get();
    }
}
