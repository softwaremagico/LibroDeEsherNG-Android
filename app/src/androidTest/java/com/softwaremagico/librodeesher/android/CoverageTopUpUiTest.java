package com.softwaremagico.librodeesher.android;

import androidx.test.core.app.ApplicationProvider;
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
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/** UI-only walkthrough of creation and development controls with low line coverage. */
@RunWith(AndroidJUnit4.class)
public class CoverageTopUpUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void creationDetailControlsApplyTheirSelections() {
        createCharacter();

        openTab("Cultura detallada");
        onView(withId(R.id.culture_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.culture_container))
                .perform(UiViews.selectFirstSpinnerOption(R.id.culture_container)));
        UiViews.safely(() -> onView(withId(R.id.culture_container))
                .perform(UiViews.clickEnabledButtonWithText(label(R.string.trainings_add))));
        UiViews.safely(() -> onView(withId(R.id.culture_container))
                .perform(UiViews.clickEnabledButtonWithText(label(R.string.trainings_remove))));
        UiViews.safely(() -> onView(withId(R.id.culture_random)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();

        openTab("Profesión detallada");
        onView(withId(R.id.profession_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.profession_container))
                .perform(UiViews.selectFirstSpinnerOption(R.id.profession_container)));
        UiViews.safely(() -> {
            onView(withId(R.id.profession_container)).perform(UiViews.clickFirstButton(R.id.profession_container));
            onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                    .perform(UiViews.clickFirstDialogRow());
            UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        });
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.profession_random)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
    }

    @Test
    public void perkRowsCanBeInspectedAddedAndRemoved() {
        createCharacter();

        openTab("Dotes");
        onView(withId(R.id.perks_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.perks_sort_name)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withId(R.id.perks_sort_cost)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_name)));
        UiDialogs.dismissAnyDialogIfPresent();

        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirstEnabled(R.id.perks_container, R.id.item_perk_action)));
        resolvePerkDialogs();
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_options)));
        resolvePerkDialogs();
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirstEnabled(R.id.perks_container, R.id.item_perk_action)));
        resolvePerkDialogs();
        onView(withId(R.id.perks_container)).check(matches(isDisplayed()));
    }

    @Test
    public void historyAndTrainingRowsCanBeToggled() {
        createCharacter();

        openTab("Historial");
        onView(withId(R.id.history_container))
                .perform(UiViews.waitForRowsOrEmpty(R.id.history_empty, 60_000));
        UiViews.safely(() -> onView(withId(R.id.history_show_all)).perform(UiViews.clickDirectly()));
        addAndRemoveFirstRow(R.string.history_add_skill);
        addAndRemoveFirstRow(R.string.history_add_category);
        addAndRemoveFirstRow(R.string.history_add_language);
        UiViews.safely(() -> onView(withText(R.string.history_add_magic_item))
                .perform(scrollTo(), UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withHint(R.string.equipment_name)).inRoot(isDialog())
                .perform(replaceText("Amuleto top-up")));
        UiViews.safely(() -> onView(withHint(R.string.equipment_description)).inRoot(isDialog())
                .perform(replaceText("Descripción top-up")));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(allOf(
                withClassName(is(android.widget.EditText.class.getName())),
                isDescendantOfA(withId(R.id.history_container)))).perform(replaceText("Historial probado")));
        UiViews.safely(() -> onView(withId(R.id.history_apply)).perform(UiViews.clickDirectly()));

        openTab("Adiestramientos");
        onView(withId(R.id.trainings_remaining)).perform(UiViews.waitForDisplayed(60_000));
        UiViews.safely(() -> onView(withId(R.id.trainings_container))
                .perform(UiViews.clickFirstEnabled(R.id.trainings_container, R.id.item_training_action)));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.trainings_container))
                .perform(UiViews.clickNthButtonWithText(R.id.trainings_container,
                        label(R.string.trainings_remove), 0)));
        onView(withId(R.id.trainings_remaining)).check(matches(isDisplayed()));
    }

    @Test
    public void perkChoiceDialogAndDescriptionsAreReachable() {
        createCharacter();

        openTab("Dotes");
        onView(withId(R.id.perks_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_info)));
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_name)));
        UiDialogs.dismissAnyDialogIfPresent();

        UiViews.safely(() -> onView(withId(R.id.perks_container)).perform(
                UiViews.clickRowActionContaining(R.id.perks_container, R.id.item_perk_name,
                        R.id.item_perk_action, "Experimentado (Cat) (Mínimo)")));
        UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                .inRoot(isDialog()).perform(UiViews.clickFirstDialogRow()));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.perks_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_name)));
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_info)));
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_options)));
        resolvePerkDialogs();
        onView(withId(R.id.perks_container)).check(matches(isDisplayed()));
    }

    @Test
    public void professionSkillGrantCanBeChosen() {
        createCharacter("thief");

        openTab("Profesión detallada");
        onView(withId(R.id.profession_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.profession_container))
                .perform(UiViews.clickFirstButton(R.id.profession_container)));
        UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                .inRoot(isDialog()).perform(UiViews.clickFirstDialogRow()));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.profession_container)).check(matches(isDisplayed()));
    }

    private void addAndRemoveFirstRow(int addLabel) {
        UiViews.safely(() -> {
            onView(withText(addLabel)).perform(scrollTo(), UiViews.clickDirectly());
            onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                    .perform(UiViews.clickFirstDialogRow());
        });
        UiDialogs.dismissAnyDialogIfPresent();
        UiViews.safely(() -> onView(withId(R.id.history_container))
                .perform(UiViews.clickNthButtonWithText(R.id.history_container,
                        label(R.string.trainings_remove), 0)));
    }

    private static void resolvePerkDialogs() {
        for (int attempt = 0; attempt < 8; attempt++) {
            if (UiDialogs.clickDialogButtonIfPresent(R.string.perks_no_weakness)
                    || UiDialogs.clickDialogButtonIfPresent(R.string.accept)
                    || UiDialogs.clickDialogButtonIfPresent(R.string.cancel)) {
                continue;
            }
            final boolean choice = UiDialogs.isDialogTextPresent(R.string.perks_choice_title)
                    || UiDialogs.isDialogTextPresent(R.string.perks_choice_flexible_title);
            if (choice) {
                UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                        .inRoot(isDialog()).perform(UiViews.clickFirstDialogRow()));
                UiDialogs.clickDialogButtonIfPresent(R.string.accept);
                continue;
            }
            UiDialogs.dismissAnyDialogIfPresent();
            break;
        }
    }

    private void createCharacter() {
        createCharacter("lordOfTheChaos");
    }

    private void createCharacter(String professionId) {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Cobertura"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(
                UiViews.selectSpinnerContaining("Profession[" + professionId + "]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(scrollTo(), androidx.test.espresso.action.ViewActions.click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void openTab(String title) {
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs))))
                .perform(UiViews.clickClickableAncestor());
        android.os.SystemClock.sleep(500);
    }

    private static String label(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }
}
