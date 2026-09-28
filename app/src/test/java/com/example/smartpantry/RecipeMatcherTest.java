package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the strict-matching rule.
 * Run them in Android Studio: right-click this file -> Run 'RecipeMatcherTest'.
 */
public class RecipeMatcherTest {

    private Recipe makePasta() {
        Recipe recipe = new Recipe(1, "Tomato Pasta", "Cook it");
        recipe.addIngredient(new RecipeIngredient("pasta", 200, "g"));
        recipe.addIngredient(new RecipeIngredient("tomato", 3, "pcs"));
        recipe.addIngredient(new RecipeIngredient("onion", 1, "pcs"));
        recipe.addIngredient(new RecipeIngredient("garlic", 2, "pcs"));
        recipe.addIngredient(new RecipeIngredient("olive oil", 15, "ml"));
        return recipe;
    }

    private List<PantryItem> fullPantry() {
        List<PantryItem> pantry = new ArrayList<>();
        pantry.add(new PantryItem("Pasta", 500, "g", null));
        pantry.add(new PantryItem("Tomatoes", 4, "pcs", null));
        pantry.add(new PantryItem("Onions", 2, "pcs", null));
        pantry.add(new PantryItem("Garlic", 5, "pcs", null));
        pantry.add(new PantryItem("Olive Oil", 1, "l", null));
        return pantry;
    }

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        assertTrue(new RecipeMatcher(fullPantry()).canMake(makePasta()));
    }

    @Test
    public void fourOfFiveIngredients_recipeIsNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(3); // remove garlic
        assertFalse(new RecipeMatcher(pantry).canMake(makePasta()));
    }

    @Test
    public void notEnoughQuantity_recipeIsNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.get(1).setQuantity(2); // only 2 tomatoes, recipe needs 3
        assertFalse(new RecipeMatcher(pantry).canMake(makePasta()));
    }

    @Test
    public void pluralAndCase_stillMatch() {
        assertEquals("tomato", IngredientUtils.normalizeName("  Tomatoes "));
        assertEquals("egg", IngredientUtils.normalizeName("EGGS"));
        assertEquals("berry", IngredientUtils.normalizeName("berries"));
        assertEquals("spring onion", IngredientUtils.normalizeName("Scallions"));
    }

    @Test
    public void unitConversion_kgCountsAsGrams() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(0, new PantryItem("pasta", 0.25, "kg", null)); // 0.25 kg = 250 g >= 200 g
        assertTrue(new RecipeMatcher(pantry).canMake(makePasta()));
    }

    @Test
    public void sameIngredientTwice_quantitiesAreAddedTogether() {
        List<PantryItem> pantry = fullPantry();
        pantry.get(1).setQuantity(2);
        pantry.add(new PantryItem("tomato", 1, "pcs", null)); // 2 + 1 = 3 tomatoes
        assertTrue(new RecipeMatcher(pantry).canMake(makePasta()));
    }

    @Test
    public void missingOne_appearsInAlmostThereOnly() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(3); // remove garlic
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(makePasta());

        RecipeMatcher matcher = new RecipeMatcher(pantry);
        assertEquals(0, matcher.getStrictMatches(recipes).size());
        assertEquals(1, matcher.getAlmostThere(recipes).size());
    }
}
