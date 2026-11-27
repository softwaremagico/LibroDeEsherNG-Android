package com.softwaremagico.librodeesher.android;

import android.widget.ImageButton;

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
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/** UI-only coverage for rank editing and equipment dialogs after a manual level-up. */
@RunWith(AndroidJUnit4.class)
public class DevelopmentRanksUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void skillsSpellsAndCategoriesCanBeDeveloped() {
        createAndLevelUp();

        openTab("Habilidades");
        onView(withId(R.id.skills_container)).perform(UiViews.waitForChildCount(1, 60_000));
        assertRankIcons(R.id.skills_container);
        UiViews.safely(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirstEnabled(R.id.skills_container, R.id.item_skill_plus)));
        UiViews.safely(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirstEnabled(R.id.skills_container, R.id.item_skill_minus)));
        UiViews.safely(() -> onView(withId(R.id.skills_favourites)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                .inRoot(isDialog()).perform(UiViews.clickFirstDialogRow()));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.skills_container)).check(matches(isDisplayed()));

        openTab("Conjuros");
        onView(withId(R.id.spells_container))
                .perform(UiViews.waitForRowsOrEmpty(R.id.spells_empty, 60_000));
        UiViews.safely(() -> onView(withId(R.id.spells_container))
                .perform(UiViews.clickFirstEnabled(R.id.spells_container, R.id.item_skill_plus)));
        UiViews.safely(() -> onView(withId(R.id.spells_container))
                .perform(UiViews.clickFirstEnabled(R.id.spells_container, R.id.item_skill_minus)));

        openTab("Categorías");
        onView(withId(R.id.categories_container)).perform(UiViews.waitForChildCount(1, 60_000));
        assertRankIcons(R.id.categories_container);
        UiViews.safely(() -> onView(withId(R.id.categories_container))
                .perform(UiViews.clickFirstEnabled(R.id.categories_container, R.id.item_skill_plus)));
        UiViews.safely(() -> onView(withId(R.id.categories_container))
                .perform(UiViews.clickFirstEnabled(R.id.categories_container, R.id.item_skill_minus)));
        onView(withId(R.id.categories_container)).check(matches(isDisplayed()));
    }

    @Test
    public void magicEquipmentWithBonusCanBeAddedAndRemoved() {
        createAndLevelUp();

        openTab("Equipo");
        onView(withId(R.id.equipment_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withText(R.string.equipment_add_magic))
                .perform(scrollTo(), UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withId(R.id.dialog_equipment_name)).inRoot(isDialog())
                .perform(replaceText("Amuleto de prueba")));
        UiViews.safely(() -> onView(withId(R.id.dialog_equipment_description)).inRoot(isDialog())
                .perform(replaceText("Bonificación de prueba")));
        UiViews.safely(() -> onView(withId(R.id.dialog_equipment_bonus_name)).inRoot(isDialog())
                .perform(replaceText("arms")));
        UiViews.safely(() -> onView(withId(R.id.dialog_equipment_bonus_value)).inRoot(isDialog())
                .perform(replaceText("2")));
        UiViews.safely(() -> onView(withId(R.id.dialog_equipment_add_bonus)).inRoot(isDialog())
                .perform(UiViews.clickDirectly()));
        UiDialogs.clickDialogButtonIfPresent(R.string.add);
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.equipment_container)).perform(UiViews.waitForChildCount(1, 60_000));
        UiViews.safely(() -> onView(withId(R.id.equipment_container))
                .perform(UiViews.clickFirst(R.id.equipment_container, R.id.item_equipment_action)));
        onView(withId(R.id.equipment_container)).check(matches(isDisplayed()));
    }

    /** Row controls must be icon-only, matching the Advisor add/remove buttons. */
    private static void assertRankIcons(int containerId) {
        UiViews.assertFirstChild(containerId, R.id.item_skill_plus,
                allOf(isAssignableFrom(ImageButton.class), withContentDescription(R.string.add)));
        UiViews.assertFirstChild(containerId, R.id.item_skill_minus,
                allOf(isAssignableFrom(ImageButton.class), withContentDescription(R.string.remove)));
    }

    private void createAndLevelUp() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Desarrollo"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(
                UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(scrollTo(),
                androidx.test.espresso.action.ViewActions.click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());

        UiViews.safely(() -> onView(withId(R.id.sheet_level_up_button)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
                .inRoot(isDialog()).perform(UiViews.clickFirstDialogRow()));
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
}
