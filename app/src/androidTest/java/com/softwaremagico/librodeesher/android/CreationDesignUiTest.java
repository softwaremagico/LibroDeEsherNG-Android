package com.softwaremagico.librodeesher.android;

import android.widget.ImageButton;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/** UI-only checks for the Advisor-like icon controls in the character creation screen. */
@RunWith(AndroidJUnit4.class)
public class CreationDesignUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        openCharacterTab();
    }

    @Test
    public void randomNameIsAnInlineDiceIcon() {
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.random_name_button)).check(matches(isDisplayed()));
        onView(withId(R.id.random_name_button)).check(matches(isAssignableFrom(ImageButton.class)));
        onView(withId(R.id.random_name_button))
                .check(matches(withContentDescription(R.string.character_random_name)));
    }

    @Test
    public void randomCharacterIsABottomRightFab() {
        onView(withId(R.id.random_button)).check(matches(isDisplayed()));
        onView(withId(R.id.random_button)).check(matches(isAssignableFrom(FloatingActionButton.class)));
        onView(withId(R.id.random_button)).check(matches(withContentDescription(R.string.character_random)));
    }

    @Test
    public void generationActionsAreIconOnly() {
        onView(withId(R.id.generate_button)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.generate_button)).check(matches(isAssignableFrom(ImageButton.class)));
        onView(withId(R.id.generate_button)).check(matches(withContentDescription(R.string.character_generate)));
        onView(withId(R.id.reset_button)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.reset_button)).check(matches(isAssignableFrom(ImageButton.class)));
        onView(withId(R.id.reset_button)).check(matches(withContentDescription(R.string.character_reset)));
    }

    private void openCharacterTab() {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        onView(Matchers.allOf(withText(label(R.string.navigation_character)),
                isDescendantOfA(withId(R.id.tabs))))
                .perform(scrollTo(), UiViews.clickClickableAncestor());
    }

    private static String label(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }
}
