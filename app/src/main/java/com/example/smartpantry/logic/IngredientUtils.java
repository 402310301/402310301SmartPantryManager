package com.example.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper methods that clean up ingredient names and units, so that small
 * real-world differences do not break the recipe matching. For example:
 *   "Tomatoes" and "tomato"   -> both become "tomato"
 *   "1 kg" and "1000 g"       -> both become 1000 (in grams)
 *
 * IMPORTANT: the SAME cleaning is done to pantry items AND recipe ingredients,
 * so both sides always end up in the same format before they are compared.
 *
 * This class has no Android code in it, which means it can be unit tested easily.
 */
public class IngredientUtils {

    // The units the user can pick from on the Add/Edit screen
    public static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    // A few common alternative names that mean the same ingredient.
    // The key is the alternative name (already singular), the value is the name we use.
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        ALIASES.put("scallion", "spring onion");
        ALIASES.put("green onion", "spring onion");
        ALIASES.put("capsicum", "bell pepper");
        ALIASES.put("green pepper", "bell pepper");
        ALIASES.put("red pepper", "bell pepper");
        ALIASES.put("mielie meal", "maize meal");
        ALIASES.put("mealie meal", "maize meal");
        ALIASES.put("minced beef", "beef mince");
        ALIASES.put("ground beef", "beef mince");
        ALIASES.put("yogurt", "yoghurt");
        ALIASES.put("wrap", "tortilla");
        ALIASES.put("garlic clove", "garlic");
        ALIASES.put("spaghetti", "pasta");
        ALIASES.put("macaroni", "pasta");
        ALIASES.put("penne", "pasta");
    }

    /**
     * Cleans an ingredient name so it can be compared with other names.
     * Steps: lower case -> remove extra spaces -> make singular -> swap aliases.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        // 1. Lower case and remove extra spaces ("  Cherry   Tomatoes " -> "cherry tomatoes")
        String clean = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (clean.isEmpty()) {
            return "";
        }

        // 2. Make the LAST word singular ("cherry tomatoes" -> "cherry tomato")
        int lastSpace = clean.lastIndexOf(' ');
        String firstPart = clean.substring(0, lastSpace + 1);
        String lastWord = clean.substring(lastSpace + 1);
        clean = firstPart + toSingular(lastWord);

        // 3. Swap known alternative names ("scallion" -> "spring onion")
        if (ALIASES.containsKey(clean)) {
            clean = ALIASES.get(clean);
        }
        return clean;
    }

    /**
     * Very simple English plural -> singular rules.
     * It is not perfect, but because both sides use the same rules it still matches correctly.
     */
    static String toSingular(String word) {
        if (word.length() <= 3) {
            return word; // short words like "oil", "egg"
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";   // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);         // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);         // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us")) {
            return word;                                         // hummus, asparagus
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);         // eggs -> egg
        }
        return word;
    }

    /** Makes unit spellings the same, e.g. "Grams" -> "g", "pieces" -> "pcs". */
    private static String cleanUnit(String unit) {
        if (unit == null) {
            return "pcs";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "":
            case "pc":
            case "piece":
            case "pieces":
            case "each":
                return "pcs";
            case "gram":
            case "grams":
                return "g";
            case "kilogram":
            case "kilograms":
            case "kgs":
                return "kg";
            case "litre":
            case "liter":
            case "litres":
            case "liters":
                return "l";
            case "cups":
                return "cup";
            default:
                return u;
        }
    }

    /**
     * Returns the "base unit" for a unit. Units with the same base unit can be converted:
     *   weight -> "g", volume -> "ml", counted items -> "pcs".
     * (We cannot convert weight to volume, because that depends on the ingredient.)
     */
    public static String getBaseUnit(String unit) {
        String u = cleanUnit(unit);
        switch (u) {
            case "g":
            case "kg":
                return "g";
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return "ml";
            default:
                return u; // "pcs" or any unknown unit stays as it is
        }
    }

    /** How many base units are in ONE of this unit, e.g. 1 kg = 1000 g. */
    public static double getFactor(String unit) {
        String u = cleanUnit(unit);
        switch (u) {
            case "kg":
            case "l":
                return 1000;
            case "cup":
                return 250;
            case "tbsp":
                return 15;
            case "tsp":
                return 5;
            default:
                return 1;
        }
    }

    /** Converts a quantity to its base unit, e.g. 2 kg -> 2000 (g). */
    public static double toBaseQuantity(double quantity, String unit) {
        return quantity * getFactor(unit);
    }

    /** Shows 3.0 as "3" and 0.25 as "0.25" so the screen looks neat. */
    public static String formatQuantity(double quantity) {
        double rounded = Math.round(quantity * 100) / 100.0;
        if (rounded == Math.floor(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}
