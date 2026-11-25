package com.softwaremagico.librodeesher.android;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

/** UI-only coverage of opening, switching and closing characters in one session. */
@RunWith(AndroidJUnit4.class)
public class MultiCharacterSessionUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void canOpenSwitchAndCloseSeveralCharacters() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();

        clickMenu(R.string.character_switch);
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withText(R.string.character_switch)).check(matches(isDisplayed()));
        onData(anything()).atPosition(0).perform(click());

        clickMenu(R.string.character_close);
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withText(R.string.character_close_confirm)).check(matches(isDisplayed()));
        onView(withText(R.string.close)).perform(click());
    }

    @Test
    public void selectedCharacterCanBeCloned() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Original"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[bard]"));
        onView(withId(R.id.level_picker)).perform(scrollTo(), UiViews.setNumberPickerValue(2));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        Espresso.closeSoftKeyboard();

        clickMenu(R.string.character_clone);
        clickMenu(R.string.character_switch);
        UiDialogs.discardUnsavedChangesIfPrompted();
        // The second entry only exists when the clone was added to the session.
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.navigation_home)).check(matches(isDisplayed()));
    }

    private void clickMenu(int label) {
        UiMenus.click(label);
    }

    private void openMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
    }
}
