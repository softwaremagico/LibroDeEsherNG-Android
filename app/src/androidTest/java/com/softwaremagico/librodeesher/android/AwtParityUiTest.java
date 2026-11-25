package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/** UI-only coverage for the legacy actions restored by the AWT parity audit (issue #22). */
@RunWith(AndroidJUnit4.class)
public class AwtParityUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void characteristicsCanBeRandomizedFromTheUi() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        selectRaceCultureProfession();

        openTab("Características");
        android.os.SystemClock.sleep(300);
        onView(withId(R.id.characteristics_random)).perform(scrollTo())
                .check(matches(allOf(isDisplayed(), isEnabled())));

        onView(withId(R.id.characteristics_random)).perform(click());
        onView(withId(R.id.characteristics_random)).check(matches(not(isEnabled())));
        onView(withId(R.id.characteristics_confirm)).check(matches(not(isEnabled())));
        onView(withId(R.id.characteristics_points)).check(matches(withSubstring("/")));
    }

    @Test
    public void perkOptionsAndWeaknessCanBeChosenFromTheUi() {
        createCompleteCharacter();

        openTab("Dotes");
        android.os.SystemClock.sleep(1000);
        onView(withId(R.id.perks_container)).check(matches(isDisplayed()));

        // Weapon Curse is an available catalog perk with a choice grant. Adding it through the UI
        // must open the legacy "Opciones de la dote" dialog.
        onView(withId(R.id.perks_container)).perform(UiViews.clickRowActionContaining(
                R.id.perks_container, R.id.item_perk_name, R.id.item_perk_action, "Maldición de las Armas"));
        android.os.SystemClock.sleep(500);
        if (UiDialogs.clickDialogButtonIfPresent(R.string.perks_no_weakness)) {
            android.os.SystemClock.sleep(500);
        }
        final boolean fixedChoice = UiDialogs.isDialogTextPresent(R.string.perks_choice_title);
        final boolean flexibleChoice = UiDialogs.isDialogTextPresent(R.string.perks_choice_flexible_title);
        org.junit.Assert.assertTrue("The perk choice dialog was not shown", fixedChoice || flexibleChoice);
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        onView(withText(R.string.accept)).inRoot(isDialog()).perform(click());

        android.os.SystemClock.sleep(300);
        onView(withId(R.id.perks_container)).check(matches(isDisplayed()));
    }

    private void selectRaceCultureProfession() {
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));

        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));

        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));

        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Auditoría"));
        selectRaceCultureProfession();
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        android.os.SystemClock.sleep(300);
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs))))
                .perform(scrollTo(), UiViews.clickClickableAncestor());
        android.os.SystemClock.sleep(500);
    }
}
