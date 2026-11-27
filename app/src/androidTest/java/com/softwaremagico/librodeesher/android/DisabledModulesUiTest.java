package com.softwaremagico.librodeesher.android;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** UI-only proof that disabling a rule module removes its entries from the rules browser. */
@RunWith(AndroidJUnit4.class)
public class DisabledModulesUiTest {

    private static final String PULP_CATEGORY = "Ciencia/Analítica·Médica";
    private static final String PULP_MODULE = "Pulp";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void disablingPulpRemovesItsCategoriesFromTheCatalog() {
        openOptions();
        final boolean original = UiViews.isModuleChecked(PULP_MODULE);
        UiViews.setModuleChecked(PULP_MODULE, true);
        saveOptions();
        openOptions();
        if (!UiViews.isModuleChecked(PULP_MODULE)) {
            throw new AssertionError("Pulp checkbox did not persist as checked");
        }
        UiViews.pressBackDirectly();
        // A fresh activity applies the module selection before any catalog screen reads it.
        activityRule.getScenario().recreate();
        try {
            showCategories();
            assertTrue("Pulp category is missing with the module enabled",
                    visibleEntries().contains(PULP_CATEGORY));
            final int baseline = categoryCount();

            setPulpEnabled(false);
            openOptions();
            if (UiViews.isModuleChecked(PULP_MODULE)) {
                throw new AssertionError("Pulp checkbox did not persist as unchecked");
            }
            UiViews.pressBackDirectly();
            activityRule.getScenario().recreate();
            showCategories();
            assertFalse("Pulp category is still shown with the module disabled",
                    visibleEntries().contains(PULP_CATEGORY));
            onView(withId(R.id.wiki_count_text)).check(matches(fewerThan(baseline)));
        } finally {
            // Shared preferences survive the test: always leave the module as we found it.
            setPulpEnabled(original);
        }
    }

    private void showCategories() {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        onView(withId(R.id.navigation_wiki)).perform(UiViews.clickDirectly());
        // The fragment may be reused with the spinner already on "Categorías": force a reload.
        onView(withId(R.id.wiki_type_spinner)).perform(UiViews.selectSpinnerPosition(2));
        onView(withId(R.id.wiki_type_spinner)).perform(UiViews.selectSpinnerPosition(3));
    }

    private void setPulpEnabled(boolean enabled) {
        openOptions();
        onView(withId(R.id.option_modules_container))
                .perform(UiViews.setModuleChecked(PULP_MODULE, enabled));
        saveOptions();
    }

    private void openOptions() {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        UiMenus.click(R.string.navigation_options);
    }

    private void saveOptions() {
        onView(withId(R.id.option_save)).perform(scrollTo(), click());
        UiViews.pressBackDirectly();
    }

    /** Reads what the browser actually renders, without touching the rules library. */
    private static List<String> visibleEntries() {
        final AtomicReference<List<String>> entries = new AtomicReference<>();
        onView(withId(R.id.wiki_list)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(ListView.class);
            }

            @Override
            public String getDescription() {
                return "read the visible catalog entries";
            }

            @Override
            public void perform(UiController controller, View view) {
                final ListView list = (ListView) view;
                final List<String> values = new ArrayList<>();
                for (int position = 0; position < list.getAdapter().getCount(); position++) {
                    values.add(String.valueOf(list.getAdapter().getItem(position)));
                }
                entries.set(values);
            }
        });
        return entries.get();
    }

    private static int categoryCount() {
        final AtomicReference<String> text = new AtomicReference<>();
        onView(withId(R.id.wiki_count_text)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "read the visible element count";
            }

            @Override
            public void perform(UiController controller, View view) {
                text.set(((TextView) view).getText().toString());
            }
        });
        final java.util.regex.Matcher digits = Pattern.compile("\\d+").matcher(text.get());
        if (!digits.find()) {
            throw new AssertionError("No element count shown: " + text.get());
        }
        return Integer.parseInt(digits.group());
    }

    private static Matcher<View> fewerThan(int baseline) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                final String text = ((TextView) view).getText().toString();
                final java.util.regex.Matcher digits = Pattern.compile("\\d+").matcher(text);
                return digits.find() && Integer.parseInt(digits.group()) < baseline;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("category count below " + baseline);
            }
        };
    }
}
