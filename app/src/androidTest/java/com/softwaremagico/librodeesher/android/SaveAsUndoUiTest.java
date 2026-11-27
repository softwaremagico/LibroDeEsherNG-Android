package com.softwaremagico.librodeesher.android;

import android.widget.EditText;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/** UI-only parity with Advisor: "Guardar como" and undoing a deletion. */
@RunWith(AndroidJUnit4.class)
public class SaveAsUndoUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private String copyName;

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        // A unique name keeps the test repeatable even when earlier runs left saved copies behind.
        copyName = "Copia Auditoría " + System.currentTimeMillis();
    }

    @Test
    public void saveAsCreatesANamedCopyAndUndoRestoresTheDeletedRow() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());

        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        UiMenus.click(R.string.sheet_save_as);
        onView(withClassName(is(EditText.class.getName()))).inRoot(isDialog())
                .perform(replaceText(copyName));
        onView(withText(R.string.sheet_save)).inRoot(isDialog()).perform(UiViews.clickDirectly());

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        openLoadDialog();
        onView(withText(Matchers.containsString(copyName))).inRoot(isDialog())
                .check(matches(isDisplayed()));
        closeDialog();

        onView(withId(R.id.sheet_delete_button)).perform(UiViews.clickDirectly());
        onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                .perform(UiViews.clickDialogRow(copyName));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(UiViews.clickDirectly());
        onView(withText(Matchers.equalToIgnoringCase("Deshacer"))).check(matches(isDisplayed()));
        onView(withText(Matchers.equalToIgnoringCase("Deshacer"))).perform(UiViews.clickDirectly());

        openLoadDialog();
        onView(withText(Matchers.containsString(copyName))).inRoot(isDialog())
                .check(matches(isDisplayed()));
        closeDialog();
    }

    private void openLoadDialog() {
        onView(withId(R.id.sheet_load_button)).perform(UiViews.clickDirectly());
    }

    private void closeDialog() {
        onView(withText(R.string.close)).inRoot(isDialog()).perform(UiViews.clickDirectly());
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Guardar Como"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }
}
