package com.example.smartpantry.logic;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (the core business logic of the app).
 *
 * A recipe is only "suggested" if EVERY ingredient it needs is in the pantry
 * in AT LEAST the required quantity. If even one ingredient is missing, or
 * there is not enough of it, the recipe is NOT suggested.
 *
 * How it works:
 * 1. When a RecipeMatcher is created, it adds up everything in the pantry into a
 *    "stock" map.  key = cleaned name + base unit (e.g. "tomato|pcs")
 *                  value = total amount in the base unit (e.g. 5.0)
 *    So "Tomatoes 2 pcs" and "tomato 3 pcs" become one entry: tomato|pcs = 5.
 * 2. For each recipe ingredient we build the same kind of key and check if the
 *    stock has enough.
 */
public class RecipeMatcher {

    // A tiny amount to avoid problems with decimal rounding (e.g. 0.30000000004)
    private static final double TOLERANCE = 0.0001;

    private final Map<String, Double> stock = new HashMap<>();

    public RecipeMatcher(List<PantryItem> pantryItems) {
        for (PantryItem item : pantryItems) {
            String key = makeKey(item.getName(), item.getUnit());
            double amount = IngredientUtils.toBaseQuantity(item.getQuantity(), item.getUnit());

            Double current = stock.get(key);
            if (current == null) {
                stock.put(key, amount);
            } else {
                stock.put(key, current + amount); // same ingredient added twice -> add them together
            }
        }
    }

    /** Builds the lookup key, e.g. ("Tomatoes", "pcs") -> "tomato|pcs". */
    public static String makeKey(String name, String unit) {
        return IngredientUtils.normalizeName(name) + "|" + IngredientUtils.getBaseUnit(unit);
    }

    /** True if the pantry has this ingredient in at least the required quantity. */
    public boolean hasIngredient(RecipeIngredient ingredient) {
        Double have = stock.get(makeKey(ingredient.getName(), ingredient.getUnit()));
        if (have == null) {
            return false; // not in the pantry at all
        }
        double need = IngredientUtils.toBaseQuantity(ingredient.getQuantity(), ingredient.getUnit());
        return have + TOLERANCE >= need;
    }

    /** Returns every ingredient of the recipe that the user does not have (enough of). */
    public List<RecipeIngredient> getMissingIngredients(Recipe recipe) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (!hasIngredient(ingredient)) {
                missing.add(ingredient);
            }
        }
        return missing;
    }

    /** STRICT rule: the recipe can be made only if nothing is missing. */
    public boolean canMake(Recipe recipe) {
        if (recipe.getIngredients().isEmpty()) {
            return false; // a recipe with no ingredients is treated as invalid
        }
        return getMissingIngredients(recipe).isEmpty();
    }

    /** Returns ONLY the recipes the user can cook right now. */
    public List<Recipe> getStrictMatches(List<Recipe> allRecipes) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe)) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * Bonus feature: recipes missing EXACTLY one ingredient.
     * These are shown in a separate list and are never mixed with the strict suggestions.
     */
    public List<Recipe> getAlmostThere(List<Recipe> allRecipes) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (getMissingIngredients(recipe).size() == 1) {
                result.add(recipe);
            }
        }
        return result;
    }
}
