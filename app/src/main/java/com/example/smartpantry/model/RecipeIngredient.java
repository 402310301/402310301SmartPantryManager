package com.example.smartpantry.model;

/**
 * One ingredient that a recipe needs, e.g. "2 pcs egg" or "200 g pasta".
 * Matches one row in the recipe_ingredients table.
 */
public class RecipeIngredient {

    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
