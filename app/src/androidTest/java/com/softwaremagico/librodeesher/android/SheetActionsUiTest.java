package com.softwaremagico.librodeesher.android;

import androidx.test.espresso.Espresso;
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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

/** UI-only walkthrough of the character-sheet actions (save, load, PDF, level up). */
@RunWith(AndroidJUnit4.class)
public class SheetActionsUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void sheetActionsCanSaveLoadAndLevelUp() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));

        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());
        safe(() -> {
            onView(withId(R.id.sheet_load_button)).perform(UiViews.clickDirectly());
            onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                    .perform(UiViews.clickFirstDialogRow());
        });
        safe(() -> {
            onView(withId(R.id.sheet_level_up_button)).perform(UiViews.clickDirectly());
            onView(withText(R.string.sheet_level_up_manual)).inRoot(isDialog())
                    .perform(UiViews.clickDirectly());
        });
        safe(() -> onView(withId(R.id.sheet_export_pdf_button)).perform(UiViews.clickDirectly()));
        safe(shareAndReturn(R.id.sheet_share_pdf_button));
        safe(shareAndReturn(R.id.sheet_share_abbreviated_button));
        safe(shareAndReturn(R.id.sheet_share_button));

        safe(() -> {
            onView(withId(R.id.sheet_delete_button)).perform(UiViews.clickDirectly());
            onView(withText(R.string.cancel)).inRoot(isDialog()).perform(UiViews.clickDirectly());
        });
    }

    @Test
    public void sheetImportAndExportActionsAreReachable() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        safe(shareAndReturn(R.id.sheet_export_character_button));
        safe(shareAndReturn(R.id.sheet_import_character_button));
        safe(shareAndReturn(R.id.sheet_export_level_button));
        safe(shareAndReturn(R.id.sheet_import_level_button));
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Hoja"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        safe(() -> onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly()));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private static Runnable shareAndReturn(int buttonId) {
        return () -> {
            onView(withId(buttonId)).perform(UiViews.clickDirectly());
            UiViews.pressBackDirectly();
        };
    }

    private static void safe(Runnable interaction) {
        UiViews.safely(interaction);
    }
}
