package com.softwaremagico.librodeesher.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.softwaremagico.librodeesher.android.ui.main.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/** UI-only coverage of the sheet's document-picker callbacks and remaining sheet buttons. */
@RunWith(AndroidJUnit4.class)
public class SheetFileResultsUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
        Intents.init();
    }

    @After
    public void releaseIntents() {
        Intents.release();
    }

    @Test
    public void exportedDocumentsCanBeImportedThroughTheSheet() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));

        final Uri characterUri = exportedUri("character.json");
        final Uri levelUri = exportedUri("level.json");

        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(ok(characterUri));
        onView(withId(R.id.sheet_export_character_button)).perform(UiViews.clickDirectly());
        intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(ok(levelUri));
        onView(withId(R.id.sheet_export_level_button)).perform(UiViews.clickDirectly());

        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(ok(characterUri));
        onView(withId(R.id.sheet_import_character_button)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));

        UiViews.safely(() -> {
            onView(withId(R.id.sheet_level_up_button)).perform(UiViews.clickDirectly());
            onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                    .perform(UiViews.clickFirstDialogRow());
        });
        UiDialogs.dismissAnyDialogIfPresent();
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(ok(levelUri));
        onView(withId(R.id.sheet_import_level_button)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));
    }

    @Test
    public void loadDeleteAndShareButtonsReachTheirDialogs() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly());

        UiViews.safely(() -> {
            onView(withId(R.id.sheet_load_button)).perform(UiViews.clickDirectly());
            onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                    .perform(UiViews.clickFirstDialogRow());
        });
        UiDialogs.discardUnsavedChangesIfPrompted();

        onView(withId(R.id.sheet_delete_button)).perform(UiViews.clickDirectly());
        UiViews.safely(() -> onView(androidx.test.espresso.matcher.ViewMatchers.isRoot()).inRoot(isDialog())
                .perform(UiViews.clickFirstDialogRow()));
        UiDialogs.clickDialogButtonIfPresent(R.string.delete);
        UiDialogs.dismissAnyDialogIfPresent();
    }

    @Test
    public void abbreviatedSheetCanBeShared() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        shareAndReturn(R.id.sheet_share_abbreviated_button);
    }

    @Test
    public void pdfCanBeExportedAndShared() {
        createCompleteCharacter();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        shareAndReturn(R.id.sheet_share_pdf_button);
    }

    @Test
    public void sheetActionsExplainMissingOrUnconfirmedCharacters() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(isDisplayed()));
        UiViews.safely(() -> onView(withId(R.id.sheet_save_button)).perform(UiViews.clickDirectly()));
        UiViews.safely(() -> onView(withId(R.id.sheet_export_character_button))
                .perform(UiViews.clickDirectly()));
    }

    private static void shareAndReturn(int buttonId) {
        UiViews.safely(() -> {
            onView(withId(buttonId)).perform(UiViews.clickDirectly());
            UiViews.pressBackDirectly();
        });
    }

    private static Instrumentation.ActivityResult ok(Uri uri) {
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, new Intent().setData(uri));
    }

    private static Uri exportedUri(String name) {
        final Context context = ApplicationProvider.getApplicationContext();
        final File directory = new File(context.getCacheDir(), "export");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new AssertionError("Cannot create export directory");
        }
        final File file = new File(directory, name);
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new AssertionError("Cannot create export file " + name);
            }
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Documentos"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(
                UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(
                UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.level_picker)).perform(UiViews.setNumberPickerValue(1));
        onView(withId(R.id.generate_button)).perform(scrollTo(),
                androidx.test.espresso.action.ViewActions.click());
        UiViews.safely(() -> onView(withId(R.id.characteristics_confirm)).perform(UiViews.clickDirectly()));
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }
}
