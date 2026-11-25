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

        clickMenu(R.string.sheet_save);
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
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        completeWizard();
    }

    private void completeWizard() {
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Guardado"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[bard]"));
        onView(withId(R.id.level_picker)).perform(scrollTo(), UiViews.setNumberPickerValue(2));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
    }

    private void openMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
    }

    private void clickMenu(int label) {
        final String text = ApplicationProvider.getApplicationContext().getString(label);
        openMenu();
        onView(withText(text)).inRoot(isPlatformPopup()).perform(click());
    }
}
