package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * UI-only coverage for screens that are not part of the character tabs: the standalone skills
 * screen (only reachable from the "Desarrollo" submenu) and the character list used to load a
 * saved character. No library API is called from the test.
 */
@RunWith(AndroidJUnit4.class)
public class StandaloneScreensUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void standaloneSkillsAndLoadScreensAreUsable() {
        createLevelOneCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());

        // The bottom tab opens SkillsDetailedFragment; only the submenu opens SkillsFragment.
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        UiMenus.clickNested(R.string.development, R.string.navigation_skills);
        onView(withId(R.id.skills_remaining)).perform(UiViews.waitForDisplayed(60_000));
        onView(withId(R.id.skills_category_spinner)).perform(UiViews.selectSpinnerPosition(1));
        UiViews.safely(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.waitForChildCount(1, 60_000)));

        UiViews.safely(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirstEnabled(R.id.skills_container, R.id.item_skill_plus)));
        UiViews.safely(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirst(R.id.skills_container, R.id.item_skill_minus)));
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.skills_favourites)).perform(UiViews.clickDirectly());
        UiViews.safely(() -> onView(ViewMatchers.isRoot()).inRoot(isDialog())
                .perform(UiViews.clickFirstDialogRow()));
        UiViews.safely(() -> onView(withText(R.string.age_apply)).inRoot(isDialog())
                .perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.skills_recommended)).perform(UiViews.clickDirectly());
        onView(withId(R.id.skills_recommended)).perform(UiViews.clickDirectly());

        onView(withId(R.id.skills_generalize)).perform(UiViews.clickDirectly());
        UiViews.safely(() -> onView(ViewMatchers.isRoot()).inRoot(isDialog())
                .perform(UiViews.clickFirstDialogRow()));
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.skills_specialize)).perform(UiViews.clickDirectly());
        UiViews.safely(() -> onView(ViewMatchers.isRoot()).inRoot(isDialog())
                .perform(UiViews.clickFirstDialogRow()));
        UiViews.safely(() -> onView(withHint(R.string.skills_specialization_hint))
                .inRoot(isDialog()).perform(replaceText("Prueba")));
        UiViews.safely(() -> onView(withText(R.string.add)).inRoot(isDialog())
                .perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.skills_bulk)).perform(UiViews.clickDirectly());
        UiViews.safely(() -> onView(withHint(R.string.skills_ranks_hint))
                .inRoot(isDialog()).perform(replaceText("1")));
        UiViews.safely(() -> onView(withText(R.string.add)).inRoot(isDialog())
                .perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();

        onView(withId(R.id.skills_random)).perform(UiViews.clickDirectly());
        onView(withId(R.id.skills_remaining)).check(matches(isDisplayed()));

        // The standalone skills destination is not in the bottom bar: pressing BACK returns to the
        // selected sheet tab, while clicking the already-selected tab would be a no-op.
        UiViews.pressBackDirectly();
        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());
        openLoadScreen();
        onView(withId(R.id.characters_list)).perform(UiViews.waitForChildCount(1, 60_000));
        onView(withId(R.id.characters_list)).check(matches(isDisplayed()));
        UiViews.safely(() -> onView(withId(R.id.character_card)).perform(UiViews.clickDirectly()));
        UiDialogs.discardUnsavedChangesIfPrompted();

        // Every development entry is handled by MainActivity, while the tab bar uses different
        // fragments. Visiting them from the submenu covers both navigation paths.
        final int[] children = {
                R.string.navigation_culture_details, R.string.navigation_profession_details,
                R.string.navigation_trainings, R.string.navigation_skills, R.string.navigation_spells,
                R.string.navigation_perks, R.string.navigation_resistances, R.string.navigation_history,
                R.string.navigation_equipment, R.string.navigation_categories, R.string.navigation_weapon_tiers
        };
        for (final int child : children) {
            UiDialogs.dismissAnyDialogIfPresent();
            onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
            UiViews.safely(() -> UiMenus.clickNested(R.string.development, child));
            UiViews.pressBackDirectly();
        }
        UiDialogs.dismissAnyDialogIfPresent();
    }

    private void openLoadScreen() {
        try {
            UiMenus.click(R.string.sheet_load);
        } catch (AssertionError ignored) {
            onView(withText(R.string.sheet_load)).perform(UiViews.clickDirectly());
        }
    }

    private void createLevelOneCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Menú"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[bard]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }
}
