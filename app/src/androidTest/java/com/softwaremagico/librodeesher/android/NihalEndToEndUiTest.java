package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.persistence.CharacterEntity;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Drives the reference level-9 character through the application's own screens on the device: it is
 * saved in the database, loaded from the character sheet screen, rendered, saved again and levelled
 * up, always from the real widgets.
 */
@RunWith(AndroidJUnit4.class)
public class NihalEndToEndUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private Context context;

    @Before
    public void seedDatabase() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        NihalFixture.insertReferenceCharacter(context);
        // The sheet must start empty, so the load dialog is the only way to reach Nihal.
        CharacterManager.getInstance().resetCharacter();
    }

    @Test
    public void loadsNihalFromTheSheetScreenAndRendersIt() {
        openSheetScreen();
        loadNihalFromTheDialog();

        onView(withId(R.id.sheet_text)).check(matches(withSubstring(NihalFixture.CHARACTER_NAME)));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level 9")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Laan")));
    }

    @Test
    public void savingFromTheSheetScreenKeepsTheExportedContent() throws Exception {
        final String expected = NihalFixture.expectedExport();

        openSheetScreen();
        loadNihalFromTheDialog();
        onView(withId(R.id.sheet_save_button)).perform(click());

        final List<CharacterEntity> saved = NihalFixture.dao(context).getAll();
        assertEquals("Saving must update the loaded character, not duplicate it", 1, saved.size());
        assertEquals(NihalFixture.CHARACTER_NAME, saved.get(0).getName());
        assertEquals(9, saved.get(0).getLevel());
        assertEquals("The saved content must be the reference content",
                expected, saved.get(0).getJson());
    }

    @Test
    public void levellingUpFromTheSheetScreenAddsTheTenthLevel() {
        openSheetScreen();
        loadNihalFromTheDialog();
        onView(withId(R.id.sheet_level_up_button)).perform(click());
        // The first option is the automatic development.
        onData(instanceOf(String.class)).atPosition(0).perform(click());

        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level 10")));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring(NihalFixture.CHARACTER_NAME)));
    }

    private void openSheetScreen() {
        onView(withId(R.id.navigation_sheet)).perform(click());
    }

    private void loadNihalFromTheDialog() {
        onView(withId(R.id.sheet_load_button)).perform(click());
        onData(allOf(instanceOf(String.class), containsString(NihalFixture.CHARACTER_NAME))).perform(click());
    }
}
