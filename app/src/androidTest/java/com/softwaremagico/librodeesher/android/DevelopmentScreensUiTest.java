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
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;

/** UI-only walkthrough of the development screens that were not exercised by the AWT suite. */
@RunWith(AndroidJUnit4.class)
public class DevelopmentScreensUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowRuleLoading() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void skillsCanBeDevelopedFromTheUi() {
        createCompleteCharacter();

        development(R.string.navigation_skills);
        onView(withId(R.id.skills_remaining)).check(matches(isDisplayed()));

        safe(() -> onView(withId(R.id.skills_recommended)).perform(click()));
        safe(() -> {
            onView(withId(R.id.skills_favourites)).perform(click());
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
            onView(withText(R.string.age_apply)).inRoot(isDialog()).perform(click());
        });
        safe(() -> {
            onView(withId(R.id.skills_generalize)).perform(click());
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        });
        safe(() -> {
            onView(withId(R.id.skills_specialize)).perform(click());
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
            onView(withHint(R.string.skills_specialization_hint)).inRoot(isDialog())
                    .perform(replaceText("Especialista"));
            onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
        });
        safe(() -> {
            onView(withId(R.id.skills_bulk)).perform(click());
            onView(withHint(R.string.skills_ranks_hint)).inRoot(isDialog()).perform(replaceText("1"));
            onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
        });
        safe(() -> onView(withId(R.id.skills_random)).perform(click()));
        safe(() -> {
            onView(withId(R.id.skills_category_spinner)).perform(UiViews.selectSpinnerPosition(1));
        });
        safe(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirstEnabled(R.id.skills_container, R.id.item_skill_plus)));
        safe(() -> onView(withId(R.id.skills_container))
                .perform(UiViews.clickFirst(R.id.skills_container, R.id.item_skill_minus)));
    }

    @Test
    public void backgroundEquipmentAndOtherDevelopmentScreensAreEditable() {
        createCompleteCharacter();

        development(R.string.navigation_history);
        onView(withId(R.id.history_container)).check(matches(isDisplayed()));
        safe(() -> onView(withId(R.id.history_random)).perform(click()));
        safe(() -> {
            onView(withText(R.string.history_add_skill)).perform(scrollTo(), click());
            chooseFirstDialogItem();
        });
        safe(() -> {
            onView(withText(R.string.history_add_category)).perform(scrollTo(), click());
            chooseFirstDialogItem();
        });
        safe(() -> {
            onView(withText(R.string.history_add_language)).perform(scrollTo(), click());
            chooseFirstDialogItem();
        });
        safe(() -> onView(withText(label(R.string.history_up_characteristic, "CO")))
                .perform(scrollTo(), click()));
        safe(() -> {
            onView(withText(R.string.history_add_magic_item)).perform(scrollTo(), click());
            onView(withHint(R.string.equipment_name)).inRoot(isDialog()).perform(replaceText("Amuleto"));
            onView(withHint(R.string.equipment_description)).inRoot(isDialog())
                    .perform(replaceText("+1 a todas las tiradas"));
            onView(withText(R.string.accept)).inRoot(isDialog()).perform(click());
        });
        safe(() -> onView(withText(R.string.accept)).perform(scrollTo(), click()));

        development(R.string.navigation_equipment);
        safe(() -> {
            onView(withText(R.string.equipment_add)).perform(scrollTo(), click());
            fillEquipmentDialog("Espada", "Acero");
        });
        safe(() -> {
            onView(withText(R.string.equipment_add_magic)).perform(scrollTo(), click());
            fillEquipmentDialog("Anillo", "Protección");
            onView(withId(R.id.dialog_equipment_bonus_name)).inRoot(isDialog())
                    .perform(replaceText("bono"));
            onView(withId(R.id.dialog_equipment_bonus_value)).inRoot(isDialog()).perform(replaceText("2"));
            onView(withId(R.id.dialog_equipment_add_bonus)).inRoot(isDialog()).perform(click());
            onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
        });

        development(R.string.navigation_resistances);
        onView(withId(R.id.resistances_container)).check(matches(isDisplayed()));
        development(R.string.navigation_categories);
        safe(() -> onView(withId(R.id.categories_container))
                .perform(UiViews.clickFirstEnabled(R.id.categories_container, R.id.item_skill_plus)));
        development(R.string.navigation_weapon_tiers);
        safe(() -> {
            onView(withId(R.id.weapon_tiers_container))
                    .perform(UiViews.clickFirst(R.id.weapon_tiers_container, R.id.item_weapon_tier_action));
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
        });
        development(R.string.navigation_trainings);
        safe(() -> {
            onView(withId(R.id.trainings_container))
                    .perform(UiViews.clickFirstEnabled(R.id.trainings_container, R.id.item_training_action));
            onView(withText(R.string.accept)).inRoot(isDialog()).perform(click());
        });
        safe(() -> onView(withId(R.id.trainings_container))
                .perform(UiViews.clickFirst(R.id.trainings_container, R.id.item_training_action)));
        development(R.string.navigation_culture_details);
        development(R.string.navigation_profession_details);
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Habilidades");
        onView(withId(R.id.skills_container)).check(matches(isDisplayed()));
    }

    @Test
    public void spellsOptionsAndCreationExtrasAreReachable() {
        createCompleteCharacter();

        development(R.string.navigation_spells);
        safe(() -> {
            onView(withId(R.id.spells_type_spinner)).perform(UiViews.selectSpinnerPosition(1));
        });
        safe(() -> onView(withId(R.id.spells_container))
                .perform(UiViews.clickFirstEnabled(R.id.spells_container, R.id.item_spell_plus)));

        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Personaje");
        safe(() -> onView(withId(R.id.random_name_button)).perform(scrollTo(), click()));
        safe(() -> {
            menuItem(R.string.character_insert_age);
            onView(withText(R.string.age_apply)).inRoot(isDialog()).perform(click());
        });

        menuItem(R.string.navigation_options);
        onView(withId(R.id.option_save)).perform(scrollTo()).check(matches(isDisplayed()));
        safe(() -> onView(withId(R.id.option_magic)).perform(scrollTo(), click()));
        safe(() -> onView(withId(R.id.option_save)).perform(UiViews.clickDirectly()));
        Espresso.pressBack();

        safe(() -> {
            menuItem(R.string.settings);
            Espresso.pressBack();
        });
        safe(() -> {
            menuItem(R.string.about);
            Espresso.pressBack();
        });

        // Reset replaces the active character and triggers spinner/menu refreshes. Do it after
        // menu checks so a pending refresh cannot dismiss the popup between opening and selection.
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        openTab("Personaje");
        onView(withId(R.id.reset_button)).perform(scrollTo(), click());
        onView(withId(R.id.summary_text)).check(matches(withText(R.string.character_not_created)));
    }

    private void menuItem(int title) {
        final String text = label(title);
        final android.app.UiAutomation automation =
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getUiAutomation();
        final android.view.accessibility.AccessibilityNodeInfo initial = automation.getRootInActiveWindow();
        final String overflowDescription = ApplicationProvider.getApplicationContext().getString(
                androidx.appcompat.R.string.abc_action_menu_overflow_description);
        if (initial == null || !clickOverflow(initial, overflowDescription)) {
            throw new AssertionError("Overflow menu button not visible");
        }
        final long deadline = android.os.SystemClock.uptimeMillis() + 10_000;
        do {
            final android.view.accessibility.AccessibilityNodeInfo root = automation.getRootInActiveWindow();
            if (root != null) {
                for (android.view.accessibility.AccessibilityNodeInfo node
                        : root.findAccessibilityNodeInfosByText(text)) {
                    if (text.contentEquals(node.getText()) && node.isVisibleToUser()) {
                        for (android.view.accessibility.AccessibilityNodeInfo target = node;
                             target != null; target = target.getParent()) {
                            if (target.isClickable()
                                    && target.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) {
                                return;
                            }
                        }
                    }
                }
            }
            android.os.SystemClock.sleep(200);
        } while (android.os.SystemClock.uptimeMillis() < deadline);
        throw new AssertionError("Overflow menu item not visible: " + text);
    }

    private static boolean clickOverflow(android.view.accessibility.AccessibilityNodeInfo node,
                                         String overflowDescription) {
        final CharSequence className = node.getClassName();
        final CharSequence description = node.getContentDescription();
        if ((overflowDescription.contentEquals(description == null ? "" : description)
                || (className != null && className.toString().endsWith("OverflowMenuButton")))
                && node.isVisibleToUser()
                && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            final android.view.accessibility.AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && clickOverflow(child, overflowDescription)) {
                return true;
            }
        }
        return false;
    }

    private void createCompleteCharacter() {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText("Prueba Desarrollo"));
        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerPosition(1));
        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[laan]"));
        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[coastalUrbanClassHigh]"));
        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[lordOfTheChaos]"));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());
        safe(() -> onView(withId(R.id.characteristics_confirm)).perform(click()));
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
    }

    private void development(int title) {
        // Every development screen is also a tab of the character view; using the tab avoids
        // depending on the ActionBar submenu visibility during long UI sweeps.
        UiDialogs.dismissAnyDialogIfPresent();
        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        android.os.SystemClock.sleep(300);
        openTab(label(title));
    }

    private void fillEquipmentDialog(String name, String description) {
        onView(withId(R.id.dialog_equipment_name)).inRoot(isDialog()).perform(replaceText(name));
        onView(withId(R.id.dialog_equipment_description)).inRoot(isDialog())
                .perform(replaceText(description));
        onView(withText(R.string.add)).inRoot(isDialog()).perform(click());
    }

    private void chooseFirstDialogItem() {
        onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
    }

    private void openTab(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
        android.os.SystemClock.sleep(500);
    }

    private static String label(int id) {
        return ApplicationProvider.getApplicationContext().getString(id);
    }

    private static String label(int id, String argument) {
        return ApplicationProvider.getApplicationContext().getString(id, argument);
    }

    /** Optional interactions should not hide a regression of the screen itself. */
    private static void safe(Runnable interaction) {
        UiViews.safely(interaction);
    }
}
