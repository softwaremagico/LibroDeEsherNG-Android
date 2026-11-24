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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
        clickMenu(R.string.character_new);
        clickMenu(R.string.character_new);

        clickMenu(R.string.character_switch);
        onView(withText(R.string.character_switch)).check(matches(isDisplayed()));
        onData(anything()).atPosition(0).perform(click());

        clickMenu(R.string.character_close);
        onView(withText(R.string.character_close_confirm)).check(matches(isDisplayed()));
        onView(withText(R.string.close)).perform(click());
    }

    private void clickMenu(int label) {
        final String text = ApplicationProvider.getApplicationContext().getString(label);
        openMenu();
        onData(hasToString(text)).perform(click());
    }

    private void openMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
    }
}
