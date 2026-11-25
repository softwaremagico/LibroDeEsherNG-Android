package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

import android.view.View;
import android.widget.NumberPicker;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MorticiaEndToEndUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowTheSlowLevelTenGeneration() {
        // Creating a level ten spellcaster runs the whole random development on the main thread.
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void createMorticiaAtLevelTenAndVerifyEveryWindow() throws Exception {
        createMorticiaAtLevelTen();

        verifyCharacterTab();
        verifyCharacteristicsTab();
        verifySkillsTab();
        verifySpellsTab();
        verifyPerksTab();
        verifyHistoryTab();
        verifyEquipmentTab();
        verifyResistancesTab();
        verifyCategoriesTab();
        verifyWeaponTiersTab();
        verifyTrainingsTab();
        verifyCultureDetailsTab();
        verifyProfessionDetailsTab();
    }

    private void createMorticiaAtLevelTen() throws Exception {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Morticia Innodan"));

        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));

        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[ilourianos]"));

        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[woodland]"));

        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[elementalist]"));

        onView(withId(R.id.level_picker)).perform(setNumberPickerValue(10));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());

        onView(withId(R.id.summary_text)).check(matches(withSubstring("Nombre: Morticia Innodan")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Sexo: Mujer")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Raza: Ilourianos")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Cultura: Silvana")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Profesión: Elementalista")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Nivel: 10")));

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Morticia Innodan")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level 10")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Ilourianos")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Silvana")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Elementalista")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Resistance rolls")));
        PdfExportCheck.exportAndVerify("Morticia Innodan", 10, "Ilourianos", "Silvana", "Elementalista");

        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void verifyCharacterTab() {
        openTab("Personaje");
        onView(withId(R.id.name_input)).check(matches(withText("Morticia Innodan")));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Nivel: 10")));
    }

    private void verifyCharacteristicsTab() {
        openTab("Características");
        onView(withId(R.id.characteristics_container))
                .perform(UiViews.waitForChildCount(1, 60_000))
                .check(matches(hasMinimumChildCount(1)));
    }

    private void verifySkillsTab() {
        openTab("Habilidades");
        onView(withId(R.id.skills_remaining)).check(matches(withSubstring("Puntos de desarrollo restantes")));
        onView(withId(R.id.skills_container)).check(matches(hasMinimumChildCount(1)));
    }

    private void verifySpellsTab() {
        openTab("Conjuros");
        onView(withId(R.id.spells_remaining)).check(matches(withSubstring("Puntos de desarrollo restantes")));
        assertRenderedRowsOrEmpty(R.id.spells_container, R.id.spells_empty);
    }

    private void verifyPerksTab() {
        openTab("Dotes");
        onView(withId(R.id.perks_remaining)).check(matches(withSubstring("Puntos de dotes restantes")));
        onView(withId(R.id.perks_container)).check(matches(hasMinimumChildCount(0)));
    }

    private void verifyHistoryTab() {
        openTab("Historial");
        assertRenderedRowsOrEmpty(R.id.history_container, R.id.history_empty);
    }

    private void verifyEquipmentTab() {
        openTab("Equipo");
        assertRenderedRowsOrEmpty(R.id.equipment_container, R.id.equipment_empty);
    }

    private void verifyResistancesTab() {
        openTab("Resistencias");
        assertRenderedRowsOrEmpty(R.id.resistances_container, R.id.resistances_empty);
    }

    private void verifyCategoriesTab() {
        openTab("Categorías");
        onView(withId(R.id.categories_remaining)).check(matches(withSubstring("Puntos de desarrollo restantes")));
        assertRenderedRowsOrEmpty(R.id.categories_container, R.id.categories_empty);
    }

    private void verifyWeaponTiersTab() {
        openTab("Coste de armas");
        assertRenderedRowsOrEmpty(R.id.weapon_tiers_container, R.id.weapon_tiers_empty);
    }

    private static void assertRenderedRowsOrEmpty(int containerId, int emptyViewId) {
        onView(withId(containerId)).perform(UiViews.waitForRowsOrEmpty(emptyViewId, 60_000))
                .check(renderedRowsOrEmptyState(emptyViewId));
    }

    private void verifyTrainingsTab() {
        openTab("Adiestramientos");
        onView(withId(R.id.trainings_container)).check(matches(hasMinimumChildCount(2)));
    }

    private void verifyCultureDetailsTab() {
        openTab("Cultura detallada");
        onView(withId(R.id.culture_container)).check(matches(hasMinimumChildCount(1)));
    }

    private void verifyProfessionDetailsTab() {
        openTab("Profesión detallada");
        onView(withId(R.id.profession_container)).check(matches(hasMinimumChildCount(1)));
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
    }

    /**
     * A window is rendering correctly when it either shows rows or explicitly shows its empty
     * state; a randomly generated character does not always develop every kind of rank.
     */
    private static ViewAssertion renderedRowsOrEmptyState(int emptyViewId) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            final View emptyView = view.getRootView().findViewById(emptyViewId);
            final boolean hasRows = view instanceof android.view.ViewGroup
                    && ((android.view.ViewGroup) view).getChildCount() > 0;
            final boolean emptyShown = emptyView != null && emptyView.getVisibility() == View.VISIBLE;
            if (!hasRows && !emptyShown) {
                throw new AssertionError("Neither rows nor the empty state are shown for view " + emptyViewId);
            }
        };
    }

    private static ViewAction setNumberPickerValue(int value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(NumberPicker.class);
            }

            @Override
            public String getDescription() {
                return "set the level picker to " + value;
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((NumberPicker) view).setValue(value);
            }
        };
    }
}
