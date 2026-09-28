package com.example.smartpantry.model;

/**
 * One ingredient that the user has at home, e.g. "Tomatoes, 3 pcs, expires 2026-10-01".
 * This is a simple model class (a "POJO") that matches one row in the pantry_items table.
 */
public class PantryItem {

    private int id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // format yyyy-MM-dd, or null if the user did not set one

    public PantryItem() {
    }

    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
