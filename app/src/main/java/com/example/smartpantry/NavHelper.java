package com.example.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Sets up the bottom navigation bar that appears on the Pantry, Recipes and Settings screens.
 * Each tab opens its screen with an explicit Intent.
 */
public class NavHelper {

    public static void setupBottomNav(Activity activity, BottomNavigationView bottomNav, int currentItemId) {
        // Highlight the tab for the screen we are on (done BEFORE adding the listener)
        bottomNav.setSelectedItemId(currentItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentItemId) {
                return true; // already on this screen
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            Intent intent = new Intent(activity, target);
            // Re-use the screen if it is already open instead of creating a new copy
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0); // no animation, feels like tabs
            return true;
        });
    }
}
