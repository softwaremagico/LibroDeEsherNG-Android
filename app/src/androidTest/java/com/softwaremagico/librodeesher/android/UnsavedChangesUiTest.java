package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** UI-only coverage of the unsaved-changes marker and confirmation gates (issue #24). */
@RunWith(AndroidJUnit4.class)
public class UnsavedChangesUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void titleMarksUnsavedChangesAndClearsThemOnSave() {
        createCompleteCharacter();

        assertTitleHasMarker(false);

        renameSelectedCharacter("Cambios sin guardar");
        assertTitleHasMarker(true);

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        assertTitleHasMarker(false);
    }

    @Test
    public void newCharacterOnlyAsksForConfirmationWhenDirty() {
        createCompleteCharacter();

        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));

        completeWizard();
        makeSelectedCharacterDirty();

        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        onView(withText(R.string.unsaved_changes_message)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.unsaved_changes_continue)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.name_input)).check(matches(isDisplayed()));
    }

    @Test
    public void closeCharacterOnlyAsksForConfirmationWhenDirty() {
        createCompleteCharacter();

        clickMenu(R.string.character_close);
        onView(withText(R.string.character_close_confirm)).check(doesNotExist());

        createCompleteCharacter();
        makeSelectedCharacterDirty();

        clickMenu(R.string.character_close);
        onView(withText(R.string.character_close_confirm)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.close)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.character_close_confirm)).check(doesNotExist());
    }

    @Test
    public void loadingFromSheetConfirmsBeforeDiscardingEdits() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());

        renameSelectedCharacter("Cambios que deben conservarse");
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_load_button)).perform(click());
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        onView(withText(R.string.unsaved_changes_message)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Cambios que deben conservarse")));
    }

    @Test
    public void historyEditsUpdateUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Historial");
        onView(withId(R.id.history_container)).check(matches(isDisplayed()));
        onView(allOf(ViewMatchers.isAssignableFrom(android.widget.EditText.class),
                isDescendantOfA(withId(R.id.history_container))))
                .perform(scrollTo(), replaceText("Historia de prueba"));
        Espresso.closeSoftKeyboard();
        onView(allOf(withText(R.string.accept), isDescendantOfA(withId(R.id.history_container))))
                .perform(scrollTo(), click());
        assertTitleHasMarker(true);
    }

    @Test
    public void equipmentEditsUpdateUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Equipo");
        onView(withId(R.id.equipment_container)).check(matches(isDisplayed()));
        onView(withText(R.string.equipment_add)).perform(scrollTo(), click());
        onView(withId(R.id.dialog_equipment_name)).inRoot(isDialog()).perform(replaceText("Mochila"));
        onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
        assertTitleHasMarker(true);
    }

    @Test
    public void skillRankUpdatesUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Habilidades");
        onView(withId(R.id.skills_category_spinner)).perform(
                UiViews.selectCategoryAndClickEnabled(R.id.skills_container, R.id.item_skill_minus));
        assertTitleHasMarker(true);
    }

    @Test
    public void categoryRankUpdatesUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Categorías");
        onView(withId(R.id.categories_container)).perform(
                UiViews.clickFirstEnabled(R.id.categories_container, R.id.item_skill_minus));
        assertTitleHasMarker(true);
    }

    @Test
    public void perkUpdatesUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Dotes");
        // The available perks depend on the generated character, so use the first enabled action.
        onView(withId(R.id.perks_container)).perform(
                UiViews.clickFirstEnabled(R.id.perks_container, R.id.item_perk_action));
        // Adding a perk can chain weakness/choice dialogs; the marker is read from the toolbar.
        UiDialogs.dismissAnyDialogIfPresent();
        assertTitleHasMarker(true);
    }

    @Test
    public void weaponTierUpdatesUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Coste de armas");
        onView(withId(R.id.weapon_tiers_container)).perform(
                UiViews.clickFirstEnabled(R.id.weapon_tiers_container, R.id.item_weapon_tier_action));
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        assertTitleHasMarker(true);
    }

    @Test
    public void levelUpAndEditUpdateUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter(1);
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_level_up_button)).perform(click());
        onView(withText(R.string.sheet_level_up_manual)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        // Training actions depend on the generated character; a culture edit is always available
        // and still verifies that an edit after a level-up marks the character as dirty.
        openTab("Cultura detallada");
        final String remove = ApplicationProvider.getApplicationContext().getString(R.string.trainings_remove);
        onView(withId(R.id.culture_container)).perform(UiViews.clickEnabledButtonWithText(remove));
        assertTitleHasMarker(true);
    }

    @Test
    public void cultureHobbyUpdatesUnsavedMarkerWithoutOpeningMenu() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Cultura detallada");
        final String remove = ApplicationProvider.getApplicationContext().getString(R.string.trainings_remove);
        onView(withId(R.id.culture_container)).perform(UiViews.clickEnabledButtonWithText(remove));
        assertTitleHasMarker(true);
    }

    @Test
    public void characterOptionUpdatesUnsavedMarker() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(click());
        UiMenus.click(R.string.navigation_options);
        final AtomicReference<Boolean> original = new AtomicReference<>();
        onView(withId(R.id.option_sort_pdf_skills)).perform(scrollTo(), new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(android.widget.CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "toggle PDF sorting option";
            }

            @Override
            public void perform(UiController controller, View view) {
                final android.widget.CompoundButton button = (android.widget.CompoundButton) view;
                original.set(button.isChecked());
                button.performClick();
            }
        });
        try {
            onView(withId(R.id.option_save)).perform(scrollTo(), click());
            assertTitleHasMarker(true);
        } finally {
            onView(withId(R.id.option_sort_pdf_skills)).perform(scrollTo(), UiViews.setChecked(original.get()));
            onView(withId(R.id.option_save)).perform(scrollTo(), click());
        }
    }

    @Test
    public void spellRankUpdatesUnsavedMarkerWithoutOpeningMenu() {
        UiMenus.click(R.string.navigation_options);
        final AtomicReference<Boolean> previousMagic = new AtomicReference<>();
        onView(withId(R.id.option_magic)).perform(scrollTo(), new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(android.widget.CompoundButton.class);
            }

            @Override
            public String getDescription() {
                return "read magic option";
            }

            @Override
            public void perform(UiController controller, View view) {
                previousMagic.set(((android.widget.CompoundButton) view).isChecked());
            }
        }, UiViews.setChecked(true));
        onView(withId(R.id.option_save)).perform(scrollTo(), click());
        Espresso.pressBack();
        try {
            createCompleteCharacter(1);
            onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
            onView(withId(R.id.sheet_level_up_button)).perform(click());
            onView(withText(R.string.sheet_level_up_manual)).inRoot(isDialog()).perform(click());
            onView(withId(R.id.sheet_save_button)).perform(click());
            onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
            openTab("Conjuros");
            onView(withId(R.id.spells_type_spinner)).perform(
                    UiViews.selectCategoryAndClickEnabled(R.id.spells_container, R.id.item_spell_plus));
            assertTitleHasMarker(true);
        } finally {
            UiMenus.click(R.string.navigation_options);
            onView(withId(R.id.option_magic)).perform(scrollTo(), UiViews.setChecked(previousMagic.get()));
            onView(withId(R.id.option_save)).perform(scrollTo(), click());
            Espresso.pressBack();
        }
    }

    private void makeSelectedCharacterDirty() {
        renameSelectedCharacter("Cambios sin guardar");
    }

    private void renameSelectedCharacter(String name) {
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        onView(withId(R.id.name_input)).perform(replaceText(name));
    }

    private void readTitleAfterClosingMenu(boolean expectedMarker) {
        openMenu();
        Espresso.pressBack();
        assertTitleHasMarker(expectedMarker);
    }

    private void assertTitleHasMarker(boolean expected) {
        final String title = toolbarTitle();
        final String marker = ApplicationProvider.getApplicationContext().getString(R.string.unsaved_marker);
        if (expected) {
            assertTrue("El título debe avisar de cambios sin guardar: " + title, title.endsWith(" " + marker));
        } else {
            assertFalse("El título no debe avisar de cambios sin guardar: " + title, title.endsWith(" " + marker));
        }
    }

    /** Reads the toolbar through Android UI APIs only; no library internals are involved. */
    private static String toolbarTitle() {
        final AtomicReference<String> title = new AtomicReference<>();
        onView(withId(R.id.toolbar)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(Toolbar.class);
            }

            @Override
            public String getDescription() {
                return "read toolbar title";
            }

            @Override
            public void perform(UiController controller, View view) {
                title.set(String.valueOf(((Toolbar) view).getTitle()));
            }
        });
        return title.get();
    }

    private void createCompleteCharacter() {
        createCompleteCharacter(2);
    }

    private void createCompleteCharacter(int level) {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        completeWizard(level);
    }

    private void completeWizard() {
        completeWizard(2);
    }

    private void completeWizard(int level) {
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Guardado"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[bard]"));
        onView(withId(R.id.level_picker)).perform(scrollTo(), UiViews.setNumberPickerValue(level));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs))))
                .perform(scrollTo(), UiViews.clickClickableAncestor());
        android.os.SystemClock.sleep(500);
    }

    private void openMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
    }

    private void clickMenu(int label) {
        UiMenus.click(label);
    }
}
