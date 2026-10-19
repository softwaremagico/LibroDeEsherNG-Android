package com.softwaremagico.librodeesher.android.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.softwaremagico.librodeesher.android.R;
import com.softwaremagico.librodeesher.android.persistence.SettingsHandler;
import com.softwaremagico.librodeesher.language.Translator;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        // Apply the persisted language before any screen loads rules.
        Translator.setLanguage(SettingsHandler.getInstance(getApplicationContext()).getLanguage());
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showLanguageDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
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
}
