package com.example.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe with a name, a list of required ingredients and preparation steps.
 * The steps are stored as one piece of text, with each step on a new line.
 */
public class Recipe {

    private int id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    // Only used for display on the "Almost There" list, e.g. "Missing: 2 pcs garlic".
    // It is NOT saved in the database.
    private String missingText;

    public Recipe() {
    }

    public Recipe(int id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(RecipeIngredient ingredient) { ingredients.add(ingredient); }

    public String getMissingText() { return missingText; }
    public void setMissingText(String missingText) { this.missingText = missingText; }

    /** Returns the ingredient names as one line, e.g. "egg, milk, butter". */
    public String getIngredientNamesText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ingredients.get(i).getName());
        }
        return sb.toString();
    }
}
