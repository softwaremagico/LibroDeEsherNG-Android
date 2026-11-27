package com.softwaremagico.librodeesher.android;

import android.widget.EditText;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

/** UI-only proof that the guided assistant ends in a review of the generated character. */
@RunWith(AndroidJUnit4.class)
public class AssistantReviewUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void assistantReviewsChoicesBeforeFinishingAndShowsTheGeneratedSheet() {
        onView(withId(R.id.navigation_assistant)).perform(UiViews.clickDirectly());

        onView(withId(R.id.assistant_next_button)).perform(click());
        onView(withClassName(is(EditText.class.getName()))).inRoot(isDialog())
                .perform(replaceText("Revisión Test"));
        apply();
        chooseFirst(R.string.assistant_step_sex);
        chooseFirst(R.string.character_race);
        chooseFirst(R.string.character_culture);
        chooseFirst(R.string.character_profession);
        onView(withId(R.id.assistant_next_button)).perform(click());
        apply();
        onView(withId(R.id.assistant_next_button)).perform(click());
        apply();

        onView(withId(R.id.assistant_step_label))
                .check(matches(withSubstring("Revisar y finalizar")));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Revisión Test")));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Nivel final")));
        onView(withId(R.id.assistant_finish_button)).check(matches(isDisplayed()));
        onView(withId(R.id.assistant_save_button)).check(matches(org.hamcrest.Matchers.not(isDisplayed())));

        onView(withId(R.id.assistant_finish_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Características:")));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Habilidades (")));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Equipo:")));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Objetos mágicos:")));
        onView(withId(R.id.assistant_save_button)).check(matches(isDisplayed()));
        onView(withId(R.id.assistant_export_button)).check(matches(isDisplayed()));
        onView(withId(R.id.assistant_share_button)).check(matches(isDisplayed()));
    }

    private void chooseFirst(int dialogTitle) {
        onView(withId(R.id.assistant_next_button)).perform(click());
        onView(withText(dialogTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
    }

    private static void apply() {
        onView(withText(R.string.age_apply)).inRoot(isDialog()).perform(click());
    }
}
