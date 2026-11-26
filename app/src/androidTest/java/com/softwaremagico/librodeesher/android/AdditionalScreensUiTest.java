package com.softwaremagico.librodeesher.android;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
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

/** UI-only coverage for the guided assistant and the rules wiki. */
@RunWith(AndroidJUnit4.class)
public class AdditionalScreensUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void assistantCreatesSavesAndExportsACharacter() {
        onView(withId(R.id.navigation_assistant)).perform(UiViews.clickDirectly());

        onView(withId(R.id.assistant_next_button)).perform(click());
        onView(withClassName(is(EditText.class.getName()))).inRoot(isDialog())
                .perform(replaceText("Asistente Test"));
        applyDialog();

        chooseFirst(R.string.assistant_step_sex);

        chooseFirst(R.string.character_race);
        chooseFirst(R.string.character_culture);
        chooseFirst(R.string.character_profession);

        onView(withId(R.id.assistant_next_button)).perform(click());
        applyDialog();
        onView(withId(R.id.assistant_next_button)).perform(click());
        applyDialog();

        onView(withId(R.id.assistant_save_button)).check(matches(isDisplayed()));
        onView(withId(R.id.assistant_summary_text)).check(matches(withSubstring("Asistente Test")));
        onView(withId(R.id.assistant_export_button)).perform(click());
        onView(withId(R.id.assistant_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    @Test
    public void randomScreenGeneratesSavesAndExportsACharacter() {
        // Generation must work with whatever preferences a previous test persisted (issue #26).
        onView(withId(R.id.navigation_random)).perform(UiViews.clickDirectly());
        onView(withId(R.id.random_generate_button)).check(matches(isDisplayed()));
        onView(withId(R.id.random_generate_button)).perform(click());
        onView(withId(R.id.random_summary_text)).check(matches(withSubstring("Nombre:")));
        onView(withId(R.id.random_save_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.random_export_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.random_share_button)).perform(UiViews.clickDirectly());
        // ACTION_SEND choosers may finish the single-activity task on a headless emulator.
        try {
            Espresso.pressBackUnconditionally();
        } catch (RuntimeException ignored) {
            // The chooser already closed the test activity.
        }
    }

    @Test
    public void wikiListsEverySectionAndShowsDetails() {
        onView(withId(R.id.navigation_wiki)).perform(UiViews.clickDirectly());
        for (int position = 0; position < 8; position++) {
            onView(withId(R.id.wiki_type_spinner)).perform(UiViews.selectSpinnerPosition(position));
            onView(withId(R.id.wiki_count_text)).check(matches(withSubstring("elementos")));
        }
        onData(anything()).inAdapterView(withId(R.id.wiki_list)).atPosition(0).perform(click());
        onView(withText(R.string.close)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void resetRandomPreferences() {
        for (int attempt = 0; attempt < 3 && !clickRandomPreferencesMenu(); attempt++) {
            android.os.SystemClock.sleep(500);
            Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        }
        onView(withId(R.id.random_specialization_seekbar)).perform(setProgress(3));
        field(R.id.random_final_level, "1");
        field(R.id.random_category_ranks, "");
        field(R.id.random_skill_ranks, "");
        field(R.id.random_trainings, "");
        field(R.id.random_suggested_perks, "");
        onView(withId(R.id.random_perks_enabled)).perform(setChecked(true));
        onView(withId(R.id.random_apply_prefs)).perform(UiViews.clickDirectly());
        Espresso.pressBack();
    }

    private static void field(int id, String value) {
        onView(withId(id)).perform(replaceText(value));
    }

    private static ViewAction setProgress(int progress) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }

            @Override
            public String getDescription() {
                return "set seekbar progress";
            }

            @Override
            public void perform(UiController controller, View view) {
                ((SeekBar) view).setProgress(progress);
            }
        };
    }

    private static ViewAction setChecked(boolean checked) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "set checkbox";
            }

            @Override
            public void perform(UiController controller, View view) {
                final CompoundButton button = (CompoundButton) view;
                if (button.isChecked() != checked) {
                    button.performClick();
                }
            }
        };
    }

    private boolean clickRandomPreferencesMenu() {
        final boolean[] failed = {false};
        final androidx.test.espresso.FailureHandler handler = (error, matcher) -> failed[0] = true;
        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.SECONDS);
        try {
            onView(withText(R.string.random_config)).inRoot(isPlatformPopup())
                    .withFailureHandler(handler).perform(click());
        } finally {
            IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
            IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        }
        return !failed[0];
    }

    private void chooseFirst(int dialogTitle) {
        onView(withId(R.id.assistant_next_button)).perform(click());
        onView(withText(dialogTitle)).inRoot(isDialog()).check(matches(isDisplayed()));
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
    }

    private static void applyDialog() {
        onView(withText(R.string.age_apply)).inRoot(isDialog()).perform(click());
    }
}
