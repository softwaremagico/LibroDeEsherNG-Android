package com.softwaremagico.librodeesher.android;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

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

/**
 * Recreates every remaining legacy reference character through the Android creation UI (no rules
 * engine calls) and checks that all character windows render the generated character.
 */
@RunWith(AndroidJUnit4.class)
public class ReferenceCharactersUiTest {

    private static final String[] TABS = {"Personaje", "Características", "Habilidades", "Conjuros",
            "Dotes", "Historial", "Equipo", "Resistencias", "Categorías", "Coste de armas",
            "Adiestramientos", "Cultura detallada", "Profesión detallada"};

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void allowSlowCharacterGeneration() {
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.MINUTES);
    }

    @Test
    public void createOphiliagliiHumbitonAtLevelSeven() throws Exception {
        createAndVerify(new CharacterSpec("Ophiliaglii Humbiton", "Varón", "jhordi", "plains", "monkZen", 7,
                "Jhordi", "Llanuras", "Monje Zen"));
    }

    @Test
    public void createKaraTraceAtLevelSix() throws Exception {
        createAndVerify(new CharacterSpec("Kara Trace", "Mujer", "halfElf", "woodland", "magente", 6,
                "Medio-elfo", "Silvana", "Magente"));
    }

    @Test
    public void createFthagtliMilupaAtLevelFive() throws Exception {
        createAndVerify(new CharacterSpec("Fthagtli Milupa", "Varón", "garks", "woodlandBarbarian", "monkWarrior", 5,
                "Garks", "Silvana Bárbara", "Monje Guerrero"));
    }

    @Test
    public void createGorjuPingAtLevelFive() throws Exception {
        createAndVerify(new CharacterSpec("Gorju Ping", "Varón", "kobolds", "undergroundUrbanClassLow", "layman", 5,
                "Kobolds", "Subterránea Urbana (Clase Baja)", "Lego"));
    }

    @Test
    public void createSkaagElCarniceroAtLevelEight() throws Exception {
        createAndVerify(new CharacterSpec("Skaag El Carnicero", "Varón", "warTroll", "underground", "fighter", 8,
                "Troll de Guerra", "Subterránea", "Luchador"));
    }

    private void createAndVerify(CharacterSpec character) throws Exception {
        onView(withId(R.id.action_new_character)).perform(UiViews.clickDirectly());
        UiDialogs.discardUnsavedChangesIfPrompted();
        onView(withId(R.id.name_input)).perform(replaceText(character.name));

        onView(withId(R.id.sex_spinner)).perform(UiViews.selectSpinnerContaining(character.sex));

        onView(withId(R.id.race_spinner)).perform(UiViews.selectSpinnerContaining("Race[" + character.raceId + "]"));

        onView(withId(R.id.culture_spinner)).perform(UiViews.selectSpinnerContaining("Culture[" + character.cultureId + "]"));

        onView(withId(R.id.profession_spinner)).perform(UiViews.selectSpinnerContaining("Profession[" + character.professionId + "]"));

        onView(withId(R.id.level_picker)).perform(setNumberPickerValue(character.level));
        onView(withId(R.id.generate_button)).perform(scrollTo(), click());

        onView(withId(R.id.summary_text)).check(matches(withSubstring("Nombre: " + character.name)));
        onView(withId(R.id.summary_text)).check(matches(withSubstring("Nivel: " + character.level)));

        onView(withId(R.id.navigation_sheet)).perform(UiViews.clickDirectly());
        onView(withId(R.id.sheet_text)).check(matches(withSubstring(character.name)));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring("Level " + character.level)));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring(character.raceName)));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring(character.cultureName)));
        onView(withId(R.id.sheet_text)).check(matches(withSubstring(character.professionName)));
        PdfExportCheck.exportAndVerify(character.name, character.level, character.raceName,
                character.cultureName, character.professionName);

        onView(withId(R.id.navigation_home)).perform(UiViews.clickDirectly());
        verifyEveryWindow(character);
    }

    private void verifyEveryWindow(CharacterSpec character) {
        for (final String title : TABS) {
            onView(allOf(withText(title), isDescendantOfA(withId(R.id.tabs)))).perform(scrollTo(), click());
            if ("Personaje".equals(title)) {
                onView(withId(R.id.name_input)).check(matches(withText(character.name)));
                onView(withId(R.id.summary_text)).check(matches(withSubstring("Nivel: " + character.level)));
                continue;
            }
            final int containerId = containerIdFor(title);
            onView(withId(containerId)).check(renderedRowsOrEmptyState(emptyIdFor(title)));
        }
    }

    private static int containerIdFor(String title) {
        switch (title) {
            case "Personaje":
                return R.id.name_input;
            case "Características":
                return R.id.characteristics_container;
            case "Habilidades":
                return R.id.skills_container;
            case "Conjuros":
                return R.id.spells_container;
            case "Dotes":
                return R.id.perks_container;
            case "Historial":
                return R.id.history_container;
            case "Equipo":
                return R.id.equipment_container;
            case "Resistencias":
                return R.id.resistances_container;
            case "Categorías":
                return R.id.categories_container;
            case "Coste de armas":
                return R.id.weapon_tiers_container;
            case "Adiestramientos":
                return R.id.trainings_container;
            case "Cultura detallada":
                return R.id.culture_container;
            case "Profesión detallada":
                return R.id.profession_container;
            default:
                throw new IllegalArgumentException("Unknown tab: " + title);
        }
    }

    private static int emptyIdFor(String title) {
        switch (title) {
            case "Conjuros":
                return R.id.spells_empty;
            case "Historial":
                return R.id.history_empty;
            case "Equipo":
                return R.id.equipment_empty;
            case "Resistencias":
                return R.id.resistances_empty;
            case "Categorías":
                return R.id.categories_empty;
            case "Coste de armas":
                return R.id.weapon_tiers_empty;
            default:
                return -1;
        }
    }

    /** Rows, a non-empty label, or an explicit empty state all count as a rendered window. */
    private static ViewAssertion renderedRowsOrEmptyState(int emptyViewId) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            if (emptyViewId == -1) {
                // Windows such as Dotes always render their own header and may legitimately be empty.
                return;
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

    private static final class CharacterSpec {
        private final String name;
        private final String sex;
        private final String raceId;
        private final String cultureId;
        private final String professionId;
        private final int level;
        private final String raceName;
        private final String cultureName;
        private final String professionName;

        private CharacterSpec(String name, String sex, String raceId, String cultureId, String professionId,
                              int level, String raceName, String cultureName, String professionName) {
            this.name = name;
            this.sex = sex;
            this.raceId = raceId;
            this.cultureId = cultureId;
            this.professionId = professionId;
            this.level = level;
            this.raceName = raceName;
            this.cultureName = cultureName;
            this.professionName = professionName;
        }
    }
}
