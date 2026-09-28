package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.AppSettings;

import java.util.Calendar;
import java.util.Locale;

/**
 * SCREEN 2: Add / Edit Ingredient.
 * If the Intent has an EXTRA_ITEM_ID we are editing an existing item,
 * otherwise we are adding a new one. The same layout is used for both.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.example.smartpantry.EXTRA_ITEM_ID";

    private EditText etName;
    private EditText etQuantity;
    private Spinner spUnit;
    private TextView tvExpiryDate;

    private DatabaseHelper dbHelper;
    private PantryItem editingItem;      // null when adding a new item
    private String selectedExpiry = null; // yyyy-MM-dd or null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back arrow in the toolbar
        }

        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnClearDate = findViewById(R.id.btnClearDate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        // Fill the unit spinner
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, IngredientUtils.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // Read the id that MainActivity passed in the Intent (-1 means "not given")
        int itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = dbHelper.getPantryItem(itemId);
        }

        if (editingItem != null) {
            // EDIT mode: show the existing values
            setTitle(R.string.title_edit_ingredient);
            etName.setText(editingItem.getName());
            etQuantity.setText(IngredientUtils.formatQuantity(editingItem.getQuantity()));
            spUnit.setSelection(getUnitPosition(editingItem.getUnit()));
            selectedExpiry = editingItem.getExpiryDate();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // ADD mode: start with the default unit from Settings
            setTitle(R.string.title_add_ingredient);
            spUnit.setSelection(getUnitPosition(AppSettings.getDefaultUnit(this)));
            btnDelete.setVisibility(View.GONE);
        }
        showExpiryDate();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            selectedExpiry = null;
            showExpiryDate();
        });
        btnSave.setOnClickListener(v -> saveItem());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private int getUnitPosition(String unit) {
        for (int i = 0; i < IngredientUtils.UNITS.length; i++) {
            if (IngredientUtils.UNITS[i].equals(unit)) {
                return i;
            }
        }
        return 0;
    }

    private void showExpiryDate() {
        if (selectedExpiry == null) {
            tvExpiryDate.setText(R.string.no_expiry);
        } else {
            tvExpiryDate.setText(selectedExpiry);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            // month starts at 0 in Java, so add 1
            selectedExpiry = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            showExpiryDate();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /** Checks the input, then adds or updates the item in the database. */
    private void saveItem() {
        String name = etName.getText().toString().trim();
        // Some keyboards (e.g. South African locale) type a comma for decimals, so swap it for a dot
        String quantityText = etQuantity.getText().toString().trim().replace(',', '.');
        String unit = spUnit.getSelectedItem().toString();

        // ----- Input validation -----
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_name_required));
            etName.requestFocus();
            return;
        }
        if (name.length() > 40) {
            etName.setError(getString(R.string.error_name_too_long));
            etName.requestFocus();
            return;
        }
        // Must start with a letter; may contain letters, spaces, hyphens and apostrophes
        if (!name.matches("\\p{L}[\\p{L} '\\-]*")) {
            etName.setError(getString(R.string.error_name_letters));
            etName.requestFocus();
            return;
        }
        if (quantityText.isEmpty()) {
            etQuantity.setError(getString(R.string.error_quantity_required));
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.error_quantity_invalid));
            etQuantity.requestFocus();
            return;
        }
        if (quantity <= 0) {
            etQuantity.setError(getString(R.string.error_quantity_positive));
            etQuantity.requestFocus();
            return;
        }
        if (quantity > 100000) {
            etQuantity.setError(getString(R.string.error_quantity_too_big));
            etQuantity.requestFocus();
            return;
        }

        // ----- Save -----
        if (editingItem == null) {
            PantryItem newItem = new PantryItem(name, quantity, unit, selectedExpiry);
            long newId = dbHelper.addPantryItem(newItem);
            if (newId == -1) {
                Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, getString(R.string.item_added, name), Toast.LENGTH_SHORT).show();
        } else {
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(selectedExpiry);
            dbHelper.updatePantryItem(editingItem);
            Toast.makeText(this, getString(R.string.item_updated, name), Toast.LENGTH_SHORT).show();
        }
        finish(); // go back to the pantry list
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, editingItem.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deletePantryItem(editingItem.getId());
                    Toast.makeText(this, getString(R.string.item_deleted, editingItem.getName()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Back arrow in the toolbar. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
