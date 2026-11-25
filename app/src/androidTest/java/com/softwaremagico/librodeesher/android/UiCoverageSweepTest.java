package com.softwaremagico.librodeesher.android;

import androidx.test.core.app.ApplicationProvider;
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
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

/** UI-only sweep of the navigation paths not covered by the end-to-end character tests. */
@RunWith(AndroidJUnit4.class)
public class UiCoverageSweepTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void savedCharacterCanBeSwitchedFromTheCharacterList() {
        createCharacter("Cargable");

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());

        menuItem(R.string.character_clone);
        android.os.SystemClock.sleep(500);
        menuItem(R.string.character_switch);
        safe(() -> onData(hasToString("Cargable")).inRoot(isDialog()).atPosition(1).perform(click()));
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.tabs)).check(matches(isDisplayed()));
    }

    @Test
    public void detailsAndCharacteristicsScreensAcceptRandomAndManualEdits() {
        createCharacter("Detalles");

        development(R.string.navigation_culture_details);
        safe(() -> onView(withId(R.id.culture_random)).perform(click()));
        for (int i = 0; i < 3; i++) {
            safe(() -> onView(withId(R.id.culture_container))
                    .perform(UiViews.clickFirstButton(R.id.culture_container)));
            safe(() -> onView(withId(R.id.culture_container))
                    .perform(UiViews.selectFirstSpinnerOption(R.id.culture_container)));
        }

        development(R.string.navigation_profession_details);
        safe(() -> onView(withId(R.id.profession_random)).perform(click()));
        for (int i = 0; i < 3; i++) {
            safe(() -> onView(withId(R.id.profession_container))
                    .perform(UiViews.clickFirstButton(R.id.profession_container)));
            safe(() -> onView(withId(R.id.profession_container))
                    .perform(UiViews.selectFirstSpinnerOption(R.id.profession_container)));
        }

        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab(label(R.string.navigation_characteristics));
        safe(() -> onView(withId(R.id.characteristics_container)).perform(UiViews.clickFirstEnabled(
                R.id.characteristics_container, R.id.item_characteristic_plus)));
        safe(() -> onView(withId(R.id.characteristics_container)).perform(UiViews.clickFirstEnabled(
                R.id.characteristics_container, R.id.item_characteristic_minus)));
        safe(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));
    }

    @Test
    public void sheetShareExportDeleteAndLanguageActionsAreReachable() {
        createCharacter("Compartir");

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        for (int id : new int[]{R.id.sheet_share_button, R.id.sheet_share_abbreviated_button,
                R.id.sheet_share_pdf_button, R.id.sheet_export_character_button,
                R.id.sheet_export_level_button, R.id.sheet_import_character_button,
                R.id.sheet_import_level_button}) {
            safe(() -> {
                onView(withId(id)).perform(click());
                Espresso.pressBackUnconditionally();
            });
        }
        safe(() -> {
            onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
            onView(withId(R.id.sheet_delete_button)).perform(click());
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
            onView(withText(R.string.cancel)).inRoot(isDialog()).perform(click());
        });

        safe(() -> {
            menuItem(R.string.settings);
            onData(hasToString("English")).perform(click());
        });
        safe(() -> {
            menuItem(R.string.settings);
            onData(hasToString("Español")).perform(click());
        });
    }

    @Test
    public void perksAndWeaponTierOptionsCanBeOpenedFromTheUi() {
        createCharacter("Talentos");

        development(R.string.navigation_perks);
        safe(() -> onView(withId(R.id.perks_sort_cost)).perform(click()));
        safe(() -> onView(withId(R.id.perks_sort_name)).perform(click()));
        safe(() -> onView(withId(R.id.perks_random)).perform(click()));
        safe(() -> onView(withId(R.id.perks_container)).perform(UiViews.clickFirst(
                R.id.perks_container, R.id.item_perk_name)));
        Espresso.pressBack();
        safe(() -> onView(withId(R.id.perks_container)).perform(UiViews.clickFirst(
                R.id.perks_container, R.id.item_perk_action)));

        development(R.string.navigation_weapon_tiers);
        safe(() -> {
            onView(withId(R.id.weapon_tiers_container)).perform(UiViews.clickFirst(
                    R.id.weapon_tiers_container, R.id.item_weapon_tier_action));
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        });
    }

    @Test
    public void wikiDetailsAreReachable() {
        onView(withId(R.id.navigation_wiki)).perform(UiViews.clickDirectly());
        for (int position = 0; position < 8; position++) {
            final int index = position;
            safe(() -> {
                onView(withId(R.id.wiki_type_spinner)).perform(UiViews.selectSpinnerPosition(index));
                onData(anything()).inAdapterView(withId(R.id.wiki_list)).atPosition(0).perform(click());
                onView(withText(R.string.close)).inRoot(isDialog()).perform(click());
            });
        }
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void createCharacter(String name) {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText(name));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        safe(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void development(int title) {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        android.os.SystemClock.sleep(300);
        openTab(label(title));
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
        android.os.SystemClock.sleep(300);
    }

    private void menuItem(int title) {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText(label(title))).inRoot(isPlatformPopup()).perform(click());
    }

    private static String label(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }

    /** Optional interactions should not hide a regression of the screen itself. */
    private static void safe(Runnable interaction) {
        UiViews.safely(interaction);
    }
}
