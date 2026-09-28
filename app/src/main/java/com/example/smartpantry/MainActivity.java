package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.AppSettings;
import com.example.smartpantry.util.DateUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * SCREEN 1: Pantry List (the launcher screen).
 * Shows every ingredient the user has at home in a RecyclerView.
 * Add = "+" button, Edit = tap a row, Delete = tap the bin icon.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemListener {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private final List<PantryItem> pantryItems = new ArrayList<>();

    private TextView tvEmptyPantry;
    private TextView tvExpiryBanner;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_pantry);

        dbHelper = new DatabaseHelper(this);

        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        tvExpiryBanner = findViewById(R.id.tvExpiryBanner);
        RecyclerView rvPantry = findViewById(R.id.rvPantry);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        bottomNav = findViewById(R.id.bottomNav);

        // Connect the RecyclerView to our custom adapter
        adapter = new PantryAdapter(pantryItems, this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        // "+" button opens the Add screen (no extra = add mode)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        NavHelper.setupBottomNav(this, bottomNav, R.id.nav_pantry);
    }

    /**
     * onResume runs every time this screen becomes visible again (for example after
     * saving an item on the Add/Edit screen), so we reload the list from the database here.
     */
    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_pantry).setChecked(true);
        loadPantry();
    }

    private void loadPantry() {
        pantryItems.clear();
        pantryItems.addAll(dbHelper.getAllPantryItems());

        boolean alertsOn = AppSettings.isExpiryAlertsOn(this);
        int days = AppSettings.getExpiryDays(this);
        adapter.setExpirySettings(alertsOn, days);
        adapter.notifyDataSetChanged();

        // Friendly message instead of a blank screen
        tvEmptyPantry.setVisibility(pantryItems.isEmpty() ? View.VISIBLE : View.GONE);
        updateExpiryBanner(alertsOn, days);
    }

    /** Shows a warning bar at the top if items are expired or expiring soon. */
    private void updateExpiryBanner(boolean alertsOn, int days) {
        if (!alertsOn) {
            tvExpiryBanner.setVisibility(View.GONE);
            return;
        }

        int count = 0;
        for (PantryItem item : pantryItems) {
            int daysLeft = DateUtils.daysUntil(item.getExpiryDate());
            if (daysLeft != DateUtils.NO_DATE && daysLeft <= days) {
                count++;
            }
        }

        if (count > 0) {
            tvExpiryBanner.setText(getString(R.string.expiry_banner, count, days));
            tvExpiryBanner.setVisibility(View.VISIBLE);
        } else {
            tvExpiryBanner.setVisibility(View.GONE);
        }
    }

    /** Called by the adapter when a row is tapped: open the Edit screen for that item. */
    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId()); // pass the id with the Intent
        startActivity(intent);
    }

    /** Called by the adapter when the bin icon is tapped. Ask first, then delete. */
    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    Toast.makeText(this, getString(R.string.item_deleted, item.getName()),
                            Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
