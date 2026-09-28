package com.example.smartpantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.List;

/**
 * SCREEN 4: Recipe Detail.
 * Shows the full ingredient list (with a tick or cross for each one) and the method.
 * The "I cooked this" button subtracts the used ingredients from the pantry,
 * which helps the user keep their pantry accurate.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.example.smartpantry.EXTRA_RECIPE_ID";

    private DatabaseHelper dbHelper;
    private Recipe recipe;

    private TextView tvStatus;
    private TextView tvIngredients;
    private Button btnCook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        // Get the recipe id that was passed in the Intent
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        recipe = dbHelper.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getName());
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvSteps = findViewById(R.id.tvDetailSteps);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvIngredients = findViewById(R.id.tvDetailIngredients);
        btnCook = findViewById(R.id.btnCook);

        tvName.setText(recipe.getName());
        tvSteps.setText(buildNumberedSteps(recipe.getSteps()));
        btnCook.setOnClickListener(v -> confirmCook());

        showIngredients();
    }

    /** Turns "step one\nstep two" into "1. step one\n\n2. step two". */
    private String buildNumberedSteps(String steps) {
        String[] lines = steps.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(i + 1).append(". ").append(lines[i].trim());
            if (i < lines.length - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /** Shows each ingredient with a tick (have it) or cross (missing). */
    private void showIngredients() {
        RecipeMatcher matcher = new RecipeMatcher(dbHelper.getAllPantryItems());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            sb.append(matcher.hasIngredient(ingredient) ? "\u2714  " : "\u2718  ")
                    .append(IngredientUtils.formatQuantity(ingredient.getQuantity()))
                    .append(" ").append(ingredient.getUnit())
                    .append("  ").append(ingredient.getName())
                    .append("\n");
        }
        tvIngredients.setText(sb.toString().trim());

        int missingCount = matcher.getMissingIngredients(recipe).size();
        if (missingCount == 0) {
            tvStatus.setText(R.string.status_ready);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_700));
            btnCook.setEnabled(true);
        } else {
            tvStatus.setText(getString(R.string.status_missing, missingCount));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.expired));
            btnCook.setEnabled(false);
        }
    }

    private void confirmCook() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cook_title)
                .setMessage(R.string.cook_message)
                .setPositiveButton(R.string.cook_confirm, (dialog, which) -> cookRecipe())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Subtracts every ingredient of the recipe from the pantry.
     * If a pantry item is used up completely it is deleted, otherwise its quantity is updated.
     */
    private void cookRecipe() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            String key = RecipeMatcher.makeKey(ingredient.getName(), ingredient.getUnit());
            double stillNeeded = IngredientUtils.toBaseQuantity(ingredient.getQuantity(), ingredient.getUnit());

            for (PantryItem item : pantry) {
                if (stillNeeded <= 0) {
                    break;
                }
                if (!RecipeMatcher.makeKey(item.getName(), item.getUnit()).equals(key)) {
                    continue; // not the same ingredient
                }

                double available = IngredientUtils.toBaseQuantity(item.getQuantity(), item.getUnit());
                if (available <= 0) {
                    continue;
                }
                double used = Math.min(available, stillNeeded);
                stillNeeded -= used;
                double left = available - used;

                if (left <= 0.0001) {
                    dbHelper.deletePantryItem(item.getId());
                    item.setQuantity(0);
                } else {
                    // convert back to the item's own unit and round to 2 decimals
                    double newQuantity = left / IngredientUtils.getFactor(item.getUnit());
                    item.setQuantity(Math.round(newQuantity * 100) / 100.0);
                    dbHelper.updatePantryItem(item);
                }
            }
        }

        Toast.makeText(this, R.string.cook_done, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
