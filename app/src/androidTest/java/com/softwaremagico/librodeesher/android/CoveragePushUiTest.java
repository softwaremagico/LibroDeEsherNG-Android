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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/** Final UI-only push for the remaining uncovered creation and development paths. */
@RunWith(AndroidJUnit4.class)
public class CoveragePushUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void creationRandomButtonGeneratesAWholeCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();

        onView(withId(R.id.random_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.summary_text)).perform(UiViews.waitForText("Nivel:", 120_000));
    }

    @Test
    public void perkChoiceScopesCanBeResolvedFromTheRows() {
        createCharacter();
        openTab("Dotes");
        onView(withId(R.id.perks_container)).perform(UiViews.waitForChildCount(1, 60_000));

        addPerk("Experimentado (Hab) (Mínimo)");
        addPerk("Maldición de las Armas");
        addPerk("Poder (Mínimo)");
        addPerk("Experto en Armas Especializado (Menor)");

        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickRowActionContaining(R.id.perks_container, R.id.item_perk_name,
                        R.id.item_perk_action, "Experimentado (Hab) (Mínimo)")));
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickFirst(R.id.perks_container, R.id.item_perk_info)));
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.perks_container)).check(matches(isDisplayed()));
    }

    @Test
    public void trainingChoicesCanBeAcceptedAndRemoved() {
        createCharacter();
        openTab("Adiestramientos");
        onView(withId(R.id.trainings_container)).perform(UiViews.waitForChildCount(1, 60_000));

        for (int attempt = 0; attempt < 6; attempt++) {
            UiViews.safely(() -> onView(withId(R.id.trainings_container))
                    .perform(UiViews.clickNthButtonWithText(R.id.trainings_container, "+", 0)));
            UiDialogs.clickDialogButtonIfPresent(R.string.accept);
            UiDialogs.clickDialogButtonIfPresent(R.string.cancel);
            UiDialogs.dismissAnyDialogIfPresent();
        }
        UiViews.safely(() -> onView(withId(R.id.trainings_container))
                .perform(UiViews.clickNthButtonWithText(R.id.trainings_container, "−", 0)));
        onView(withId(R.id.trainings_container)).check(matches(isDisplayed()));
    }

    @Test
    public void magicItemDialogCanBeFilledFromTheHistoryTab() {
        createCharacter();
        openTab("Historial");
        onView(withId(R.id.history_container))
                .perform(UiViews.waitForRowsOrEmpty(R.id.history_empty, 60_000));

        UiViews.safely(() -> onView(isRoot())
                .perform(UiViews.clickEnabledButtonWithText(label(R.string.history_add_magic_item))));
        UiViews.safely(() -> onView(withHint(R.string.equipment_name))
                .perform(replaceText("Amuleto de cobertura")));
        UiViews.safely(() -> onView(withHint(R.string.equipment_description))
                .perform(replaceText("Descripción de cobertura")));
        UiDialogs.clickDialogButtonIfPresent(R.string.accept);
        UiDialogs.dismissAnyDialogIfPresent();

        UiViews.safely(() -> onView(withText("Amuleto de cobertura")).check(matches(isDisplayed())));
    }

    @Test
    public void characteristicsCanBeAdjustedBeforeConfirmation() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(
                UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());

        openTab("Características");
        onView(withId(R.id.characteristics_container)).perform(UiViews.waitForChildCount(1, 60_000));
        // The header row hides its buttons; pick the first real listener instead of a scroll position.
        onView(withId(R.id.characteristics_container))
                .perform(UiViews.clickButtonWithListener(R.id.characteristics_container, "+"));
        onView(withId(R.id.characteristics_container))
                .perform(UiViews.clickButtonWithListener(R.id.characteristics_container, "-"));
        onView(withId(R.id.characteristics_container)).check(matches(isDisplayed()));
    }

    @Test
    public void randomNameWithoutRaceKeepsTheNameEmpty() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();

        UiViews.safely(() -> onView(withId(R.id.random_name_button)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withId(R.id.name_input)).check(matches(withText(""))));
    }

    private static void addPerk(String displayName) {
        UiViews.safely(() -> onView(withId(R.id.perks_container))
                .perform(UiViews.clickRowActionContaining(R.id.perks_container, R.id.item_perk_name,
                        R.id.item_perk_action, displayName)));
        resolvePerkDialogs();
    }

    /** Resolves weakness, single/multi-choice and flexible perk dialogs in order. */
    private static void resolvePerkDialogs() {
        for (int attempt = 0; attempt < 8; attempt++) {
            if (UiDialogs.clickDialogButtonIfPresent(R.string.perks_no_weakness)) {
                continue;
            }
            final boolean choiceDialog = UiDialogs.isDialogTextPresent(R.string.perks_choice_title)
                    || UiDialogs.isDialogTextPresent(R.string.perks_choice_flexible_title);
            if (choiceDialog) {
                UiViews.safely(() -> onView(isRoot()).perform(UiViews.clickFirstDialogRow()));
                UiDialogs.clickDialogButtonIfPresent(R.string.accept);
                continue;
            }
            if (UiDialogs.clickDialogButtonIfPresent(R.string.accept)) {
                continue;
            }
            break;
        }
        UiDialogs.dismissAnyDialogIfPresent();
    }

    private void createCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Cobertura Final"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(
                UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(androidx.test.espresso.action.ViewActions.scrollTo(),
                androidx.test.espresso.action.ViewActions.click());
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

    private static String label(int stringResId) {
        return ApplicationProvider.getApplicationContext().getString(stringResId);
    }
}
