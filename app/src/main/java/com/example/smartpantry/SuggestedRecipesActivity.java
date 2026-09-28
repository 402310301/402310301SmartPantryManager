package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.RecipeAdapter;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.AppSettings;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * SCREEN 3: Suggested Recipes.
 * Runs the strict-matching rule against the current pantry and lists ONLY the
 * recipes the user can cook right now. "Almost There" recipes (bonus) are shown
 * in a clearly separate section underneath.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;

    private final List<Recipe> suggestedRecipes = new ArrayList<>();
    private final List<Recipe> almostThereRecipes = new ArrayList<>();
    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostThereAdapter;

    private TextView tvSummary;
    private TextView tvNoMatches;
    private TextView tvNoAlmostThere;
    private LinearLayout layoutAlmostThere;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle(R.string.title_suggested);

        dbHelper = new DatabaseHelper(this);

        tvSummary = findViewById(R.id.tvSummary);
        tvNoMatches = findViewById(R.id.tvNoMatches);
        tvNoAlmostThere = findViewById(R.id.tvNoAlmostThere);
        layoutAlmostThere = findViewById(R.id.layoutAlmostThere);
        bottomNav = findViewById(R.id.bottomNav);

        RecyclerView rvSuggested = findViewById(R.id.rvSuggested);
        suggestedAdapter = new RecipeAdapter(suggestedRecipes, this);
        rvSuggested.setLayoutManager(new LinearLayoutManager(this));
        rvSuggested.setAdapter(suggestedAdapter);

        RecyclerView rvAlmostThere = findViewById(R.id.rvAlmostThere);
        almostThereAdapter = new RecipeAdapter(almostThereRecipes, this);
        rvAlmostThere.setLayoutManager(new LinearLayoutManager(this));
        rvAlmostThere.setAdapter(almostThereAdapter);

        NavHelper.setupBottomNav(this, bottomNav, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_recipes).setChecked(true);
        loadSuggestions(); // pantry may have changed, so re-run the matching every time
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        // Run the strict-matching rule
        RecipeMatcher matcher = new RecipeMatcher(pantry);

        suggestedRecipes.clear();
        suggestedRecipes.addAll(matcher.getStrictMatches(allRecipes));
        suggestedAdapter.notifyDataSetChanged();

        tvSummary.setText(getString(R.string.summary_text,
                suggestedRecipes.size(), allRecipes.size(), pantry.size()));

        // Show a helpful message instead of an empty list
        tvNoMatches.setVisibility(suggestedRecipes.isEmpty() ? View.VISIBLE : View.GONE);

        // Bonus: "Almost There" list, kept separate from the strict suggestions
        if (AppSettings.isAlmostThereOn(this)) {
            layoutAlmostThere.setVisibility(View.VISIBLE);
            almostThereRecipes.clear();
            for (Recipe recipe : matcher.getAlmostThere(allRecipes)) {
                RecipeIngredient missing = matcher.getMissingIngredients(recipe).get(0);
                recipe.setMissingText(getString(R.string.missing_text,
                        IngredientUtils.formatQuantity(missing.getQuantity()),
                        missing.getUnit(), missing.getName()));
                almostThereRecipes.add(recipe);
            }
            almostThereAdapter.notifyDataSetChanged();
            tvNoAlmostThere.setVisibility(almostThereRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            layoutAlmostThere.setVisibility(View.GONE);
        }
    }

    /** Open the detail screen and pass the recipe id with the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
