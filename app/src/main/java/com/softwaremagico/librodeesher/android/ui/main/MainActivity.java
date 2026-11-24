package com.softwaremagico.librodeesher.android.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.softwaremagico.librodeesher.android.AppLog;
import com.softwaremagico.librodeesher.android.BuildConfig;
import com.softwaremagico.librodeesher.android.core.CharacterValidator;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.OptionsHandler;
import com.softwaremagico.librodeesher.android.persistence.SettingsHandler;
import com.softwaremagico.librodeesher.android.session.CharacterManager;
import com.softwaremagico.librodeesher.character.CharacterPlayer;
import com.softwaremagico.librodeesher.exceptions.InvalidXmlElementException;
import com.softwaremagico.librodeesher.language.Translator;
import com.softwaremagico.librodeesher.rules.RulesCatalog;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MenuItem developmentMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        // Apply the persisted language before any screen loads rules.
        Translator.setLanguage(SettingsHandler.getInstance(getApplicationContext()).getLanguage());
        final OptionsHandler optionsHandler = OptionsHandler.getInstance(getApplicationContext());
        optionsHandler.applyModules();
        AppLog.setDebugMode(optionsHandler.isDebugMode());
        CharacterManager.getInstance().setOptionsHandler(optionsHandler);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        final NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController);
            NavigationUI.setupWithNavController(navView, navController);
            navController.addOnDestinationChangedListener(
                    (controller, destination, arguments) -> updateDevelopmentMenu(destination));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        developmentMenu = menu.findItem(R.id.menu_development);
        if (navController != null) {
            updateDevelopmentMenu(navController.getCurrentDestination());
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean levelOne = CharacterManager.getInstance().getSelectedCharacter().getLevel() <= 1;
        for (final int id : new int[]{R.id.action_culture_details, R.id.action_profession_details,
                R.id.action_perks}) {
            final MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setEnabled(levelOne);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_new_character) {
            final CharacterManager manager = CharacterManager.getInstance();
            manager.openCharacter(manager.newCharacter(), CharacterManager.NO_ID);
            if (navController != null) {
                navController.navigate(R.id.navigation_home);
            }
            return true;
        }
        if (id == R.id.action_switch_character) {
            showSwitchCharacterDialog();
            return true;
        }
        if (id == R.id.action_close_character) {
            showCloseCharacterDialog();
            return true;
        }
        if (id == R.id.action_load_character) {
            if (navController != null) {
                navController.navigate(R.id.navigation_load);
            }
            return true;
        }
        if (id == R.id.action_save_character) {
            final int problem = CharacterValidator.problem(CharacterManager.getInstance().getSelectedCharacter());
            if (problem != 0) {
                Toast.makeText(this, problem, Toast.LENGTH_SHORT).show();
            } else {
                com.softwaremagico.librodeesher.android.persistence.CharacterSaveHelper.save(this);
            }
            return true;
        }
        if (id == R.id.action_options) {
            return navigate(R.id.navigation_options);
        }
        if (id == R.id.action_random_preferences) {
            return navigate(R.id.navigation_random_preferences);
        }
        if (id == R.id.action_insert_age) {
            showInsertAgeDialog();
            return true;
        }
        if (id == R.id.action_settings) {
            showLanguageDialog();
            return true;
        }
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        if (id == R.id.action_culture_details) {
            return navigate(R.id.navigation_culture_details);
        }
        if (id == R.id.action_profession_details) {
            return navigate(R.id.navigation_profession_details);
        }
        if (id == R.id.action_trainings) {
            return navigate(R.id.navigation_trainings);
        }
        if (id == R.id.action_skills) {
            return navigate(R.id.navigation_skills);
        }
        if (id == R.id.action_spells) {
            return navigate(R.id.navigation_spells);
        }
        if (id == R.id.action_perks) {
            return navigate(R.id.navigation_perks);
        }
        if (id == R.id.action_resistances) {
            return navigate(R.id.navigation_resistances);
        }
        if (id == R.id.action_history) {
            return navigate(R.id.navigation_history);
        }
        if (id == R.id.action_equipment) {
            return navigate(R.id.navigation_equipment);
        }
        if (id == R.id.action_categories) {
            return navigate(R.id.navigation_categories);
        }
        if (id == R.id.action_weapon_tiers) {
            return navigate(R.id.navigation_weapon_tiers);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }

    /** The development screens hang off the sheet, so their menu is only shown there. */
    private void updateDevelopmentMenu(NavDestination destination) {
        if (developmentMenu != null) {
            developmentMenu.setVisible(destination != null
                    && destination.getId() == R.id.navigation_sheet);
        }
    }

    private boolean navigate(int destinationId) {
        if (navController == null) {
            return false;
        }
        navController.navigate(destinationId);
        return true;
    }

    private void showSwitchCharacterDialog() {
        final List<CharacterManager.OpenCharacter> openCharacters =
                CharacterManager.getInstance().getOpenCharacters();
        if (openCharacters.size() < 2) {
            Toast.makeText(this, R.string.character_no_open, Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] labels = new String[openCharacters.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = openCharacters.get(i).displayName();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.character_switch)
                .setItems(labels, (dialog, which) -> {
                    CharacterManager.getInstance().switchTo(which);
                    navigate(R.id.navigation_home);
                })
                .show();
    }

    private void showCloseCharacterDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.character_close)
                .setMessage(R.string.character_close_confirm)
                .setPositiveButton(R.string.close, (dialog, which) -> {
                    CharacterManager.getInstance().closeSelected();
                    navigate(R.id.navigation_home);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Raises the final age after creation, warning when it exceeds the racial lifespan. */
    private void showInsertAgeDialog() {
        final CharacterPlayer character = CharacterManager.getInstance().getSelectedCharacter();
        if (character.getRaceId() == null) {
            Toast.makeText(this, R.string.age_select_race_first, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final Integer expectedAge = RulesCatalog.getInstance().getRace(character.getRaceId())
                    .getExpectedLifeYears();
            final int lifespan = expectedAge == null ? 100 : expectedAge;
            final int minimum = character.getCurrentAge();
            final NumberPicker picker = new NumberPicker(this);
            picker.setMinValue(minimum);
            picker.setMaxValue(Math.max(minimum + 1, lifespan + 50));
            picker.setValue(Math.max(character.getFinalAge(), minimum));

            final TextView warning = new TextView(this);
            warning.setTextColor(Color.RED);
            picker.setOnValueChangedListener((widget, oldValue, value) -> {
                if (value > lifespan) {
                    warning.setText(getString(R.string.age_expectancy_warning, lifespan));
                } else {
                    warning.setText("");
                }
            });
            if (picker.getValue() > lifespan) {
                warning.setText(getString(R.string.age_expectancy_warning, lifespan));
            }

            final LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(48, 16, 48, 16);
            content.addView(picker);
            content.addView(warning);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.character_insert_age)
                    .setView(content)
                    .setPositiveButton(R.string.age_apply, (dialog, which) -> {
                        character.setFinalAge(picker.getValue());
                        Toast.makeText(this, getString(R.string.age_updated, picker.getValue()),
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.close, null)
                    .show();
        } catch (InvalidXmlElementException exception) {
            AppLog.error("Cannot insert age", exception);
            Toast.makeText(this, R.string.age_select_race_first, Toast.LENGTH_SHORT).show();
        }
    }

    /** Lets the user switch the rules language; the activity is recreated to apply it everywhere. */
    private void showLanguageDialog() {
        final String[] names = {"Español", "English"};
        final String[] codes = {Translator.SPANISH, Translator.ENGLISH};
        final String current = SettingsHandler.getInstance(getApplicationContext()).getLanguage();
        int checked = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) {
                checked = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    SettingsHandler.getInstance(getApplicationContext()).setLanguage(codes[which]);
                    Translator.setLanguage(codes[which]);
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about_title, getString(R.string.app_name)))
                .setMessage(getString(R.string.about_version, BuildConfig.VERSION_NAME)
                        + "\n\n" + getString(R.string.about_message))
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
