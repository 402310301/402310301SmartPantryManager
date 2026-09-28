package com.example.smartpantry;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.util.AppSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * SCREEN 5: Settings.
 * Every change is saved straight away with SharedPreferences (see AppSettings).
 */
public class SettingsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.title_settings);

        SwitchCompat switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        SwitchCompat switchAlmostThere = findViewById(R.id.switchAlmostThere);
        Spinner spExpiryDays = findViewById(R.id.spExpiryDays);
        Spinner spDefaultUnit = findViewById(R.id.spDefaultUnit);
        bottomNav = findViewById(R.id.bottomNav);

        // --- Expiring-soon alerts on/off ---
        switchExpiryAlerts.setChecked(AppSettings.isExpiryAlertsOn(this));
        switchExpiryAlerts.setOnCheckedChangeListener((button, isChecked) -> {
            AppSettings.setExpiryAlertsOn(this, isChecked);
            spExpiryDays.setEnabled(isChecked);
        });

        // --- How many days before expiry to warn ---
        String[] dayLabels = new String[AppSettings.EXPIRY_DAY_OPTIONS.length];
        int selectedDayPosition = 0;
        for (int i = 0; i < AppSettings.EXPIRY_DAY_OPTIONS.length; i++) {
            int days = AppSettings.EXPIRY_DAY_OPTIONS[i];
            dayLabels[i] = days + (days == 1 ? " day" : " days");
            if (days == AppSettings.getExpiryDays(this)) {
                selectedDayPosition = i;
            }
        }
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dayLabels);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpiryDays.setAdapter(daysAdapter);
        spExpiryDays.setSelection(selectedDayPosition);
        spExpiryDays.setEnabled(switchExpiryAlerts.isChecked());
        spExpiryDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.setExpiryDays(SettingsActivity.this, AppSettings.EXPIRY_DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- Show the bonus "Almost There" list ---
        switchAlmostThere.setChecked(AppSettings.isAlmostThereOn(this));
        switchAlmostThere.setOnCheckedChangeListener((button, isChecked) ->
                AppSettings.setAlmostThereOn(this, isChecked));

        // --- Default unit for new ingredients (units preference) ---
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, IngredientUtils.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDefaultUnit.setAdapter(unitAdapter);
        String savedUnit = AppSettings.getDefaultUnit(this);
        for (int i = 0; i < IngredientUtils.UNITS.length; i++) {
            if (IngredientUtils.UNITS[i].equals(savedUnit)) {
                spDefaultUnit.setSelection(i);
            }
        }
        spDefaultUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppSettings.setDefaultUnit(SettingsActivity.this, IngredientUtils.UNITS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        NavHelper.setupBottomNav(this, bottomNav, R.id.nav_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_settings).setChecked(true);
    }
}
