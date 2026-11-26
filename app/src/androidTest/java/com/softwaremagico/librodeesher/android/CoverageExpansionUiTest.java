package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/** UI-only sweep of the random controls and sheet actions not reached by the tab-only tests. */
@RunWith(AndroidJUnit4.class)
public class CoverageExpansionUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly()));
    }

    @Test
    public void randomControlsAndSheetActionsAreUsable() {
        createLevelOneCharacterWithoutConfirming();
        onView(withId(R.id.navigation_characteristics)).perform(UiViews.clickDirectly());
        onView(withId(R.id.characteristics_points)).perform(UiViews.waitForDisplayed(60_000));
        UiViews.safely(() -> onView(withId(R.id.characteristics_container))
                .perform(UiViews.clickFirstVisible(R.id.characteristics_container,
                        R.id.item_characteristic_plus)));
        UiViews.safely(() -> onView(withId(R.id.characteristics_container))
                .perform(UiViews.clickFirstVisible(R.id.characteristics_container,
                        R.id.item_characteristic_minus)));
        onView(withId(R.id.characteristics_random)).perform(UiViews.clickDirectly());
        onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly());
        UiDialogs.dismissAnyDialogIfPresent();

        randomControlInTab("Cultura detallada", R.id.culture_random);
        randomControlInTab("Profesión detallada", R.id.profession_random);
        randomControlInTab("Historial", R.id.history_random);
        randomControlInTab("Dotes", R.id.perks_random);
        randomControlInTab("Dotes", R.id.perks_sort_name);
        randomControlInTab("Dotes", R.id.perks_sort_cost);

        openTab("Equipo");
        UiViews.safely(() -> {
            onView(withText(R.string.equipment_add_magic)).perform(UiViews.clickDirectly());
            onView(withId(R.id.dialog_equipment_name)).inRoot(isDialog()).perform(replaceText("Amuleto"));
            onView(withId(R.id.dialog_equipment_description)).inRoot(isDialog())
                    .perform(replaceText("Descripción de prueba"));
            onView(withId(R.id.dialog_equipment_bonus_name)).inRoot(isDialog()).perform(replaceText("arms"));
            onView(withId(R.id.dialog_equipment_bonus_value)).inRoot(isDialog()).perform(replaceText("2"));
            onView(withId(R.id.dialog_equipment_add_bonus)).inRoot(isDialog()).perform(click());
            onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
        });
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));
        clickSheetActionAndDismiss(R.id.sheet_export_pdf_button);
        clickSheetActionAndDismiss(R.id.sheet_share_button);
        clickSheetActionAndDismiss(R.id.sheet_share_abbreviated_button);
        clickSheetActionAndDismiss(R.id.sheet_share_pdf_button);
    }

    private void randomControlInTab(String tabTitle, int controlId) {
        openTab(tabTitle);
        UiViews.safely(() -> onView(withId(controlId)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
    }

    private void clickSheetActionAndDismiss(int buttonId) {
        UiViews.safely(() -> onView(withId(buttonId)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
    }

    private void openTab(String title) {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs))))
                .perform(UiViews.clickClickableAncestor());
        android.os.SystemClock.sleep(500);
    }

    private void createLevelOneCharacterWithoutConfirming() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Aleatoria"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[bard]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(UiViews.clickDirectly());
    }
}
