package com.example.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles everything to do with the local SQLite database.
 *
 * Tables:
 *   pantry_items        (id, name, quantity, unit, expiry_date)
 *   recipes             (id, name, steps)
 *   recipe_ingredients  (id, recipe_id, name, quantity, unit)  -> many rows per recipe
 *
 * The recipes are added ("seeded") automatically the first time the app runs,
 * inside onCreate().
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Shared column names
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_UNIT = "unit";

    // pantry_items table
    private static final String TABLE_PANTRY = "pantry_items";
    private static final String COL_EXPIRY = "expiry_date";

    // recipes table
    private static final String TABLE_RECIPES = "recipes";
    private static final String COL_STEPS = "steps";

    // recipe_ingredients table
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String COL_RECIPE_ID = "recipe_id";

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Runs only once: the first time the database is created on the device. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_ID + "))");

        seedRecipes(db);
    }

    /** Runs when DATABASE_VERSION is increased. For this project we simply start fresh. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    //  PANTRY CRUD (Create, Read, Update, Delete)
    // =====================================================================

    /** CREATE: adds a new pantry item. Returns the new row id, or -1 if it failed. */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, pantryItemToValues(item));
    }

    /** READ: returns all pantry items sorted A-Z. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC");
        while (cursor.moveToNext()) {
            items.add(cursorToPantryItem(cursor));
        }
        cursor.close();
        return items;
    }

    /** READ: returns one pantry item, or null if it does not exist. */
    public PantryItem getPantryItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToPantryItem(cursor);
        }
        cursor.close();
        return item;
    }

    /** UPDATE: saves changes to an existing pantry item. Returns the number of rows changed. */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, pantryItemToValues(item), COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes a pantry item. Returns the number of rows deleted. */
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues pantryItemToValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        if (item.getExpiryDate() == null) {
            values.putNull(COL_EXPIRY);
        } else {
            values.put(COL_EXPIRY, item.getExpiryDate());
        }
        return values;
    }

    private PantryItem cursorToPantryItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)));
        int expiryIndex = cursor.getColumnIndexOrThrow(COL_EXPIRY);
        item.setExpiryDate(cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
        return item;
    }

    // =====================================================================
    //  RECIPES (read only - they are seeded on first run)
    // =====================================================================

    /** Returns every recipe, each with its list of ingredients. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC");
        while (cursor.moveToNext()) {
            recipes.add(cursorToRecipe(cursor));
        }
        cursor.close();

        for (Recipe recipe : recipes) {
            recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
        }
        return recipes;
    }

    /** Returns one recipe with its ingredients, or null if not found. */
    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
        }
        cursor.close();

        if (recipe != null) {
            recipe.setIngredients(getIngredientsForRecipe(db, recipe.getId()));
        }
        return recipe;
    }

    private Recipe cursorToRecipe(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS));
        return new Recipe(id, name, steps);
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, COL_ID + " ASC");
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT));
            ingredients.add(new RecipeIngredient(name, quantity, unit));
        }
        cursor.close();
        return ingredients;
    }

    // =====================================================================
    //  SEED DATA (20 recipes, added the first time the app runs)
    // =====================================================================

    /**
     * Inserts one recipe and its ingredients.
     * Each ingredient is written as {name, quantity, unit}.
     */
    private void addSeedRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_NAME, name);
        recipeValues.put(COL_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (String[] ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put(COL_RECIPE_ID, recipeId);
            values.put(COL_NAME, ingredient[0]);
            values.put(COL_QUANTITY, Double.parseDouble(ingredient[1]));
            values.put(COL_UNIT, ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, values);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addSeedRecipe(db, "Scrambled Eggs",
                "Whisk the eggs and milk together.\n"
                        + "Melt the butter in a pan on low heat.\n"
                        + "Add the eggs and stir slowly until just set.",
                new String[][]{{"egg", "3", "pcs"}, {"milk", "50", "ml"}, {"butter", "10", "g"}});

        addSeedRecipe(db, "French Toast",
                "Whisk the eggs, milk and sugar in a bowl.\n"
                        + "Dip each slice of bread into the mixture.\n"
                        + "Fry in a hot pan until golden on both sides.",
                new String[][]{{"bread", "2", "pcs"}, {"egg", "2", "pcs"}, {"milk", "100", "ml"},
                        {"sugar", "10", "g"}});

        addSeedRecipe(db, "Tomato Pasta",
                "Boil the pasta in salted water until soft.\n"
                        + "Fry the chopped onion and garlic in olive oil.\n"
                        + "Add the chopped tomatoes and cook for 10 minutes.\n"
                        + "Mix the sauce with the drained pasta.",
                new String[][]{{"pasta", "200", "g"}, {"tomato", "3", "pcs"}, {"onion", "1", "pcs"},
                        {"garlic", "2", "pcs"}, {"olive oil", "15", "ml"}});

        addSeedRecipe(db, "Cheese Omelette",
                "Beat the eggs in a bowl.\n"
                        + "Melt the butter in a pan and pour in the eggs.\n"
                        + "Sprinkle the grated cheese over one half.\n"
                        + "Fold the omelette over and cook for 1 more minute.",
                new String[][]{{"egg", "3", "pcs"}, {"cheese", "50", "g"}, {"butter", "10", "g"}});

        addSeedRecipe(db, "Pancakes",
                "Mix the flour and sugar in a bowl.\n"
                        + "Add the eggs and milk and whisk until smooth.\n"
                        + "Pour small amounts into a hot greased pan.\n"
                        + "Flip when bubbles appear and cook the other side.",
                new String[][]{{"flour", "200", "g"}, {"egg", "2", "pcs"}, {"milk", "300", "ml"},
                        {"sugar", "20", "g"}});

        addSeedRecipe(db, "Vegetable Stir Fry",
                "Cook the rice.\n"
                        + "Slice the carrots, onion and bell pepper.\n"
                        + "Stir fry the vegetables on high heat for 5 minutes.\n"
                        + "Add the soy sauce and serve on the rice.",
                new String[][]{{"rice", "150", "g"}, {"carrot", "2", "pcs"}, {"onion", "1", "pcs"},
                        {"bell pepper", "1", "pcs"}, {"soy sauce", "30", "ml"}});

        addSeedRecipe(db, "Chicken and Rice",
                "Cut the chicken into cubes and brown it in a pot.\n"
                        + "Add the chopped onion and garlic and fry for 2 minutes.\n"
                        + "Add the rice and 400 ml of water.\n"
                        + "Cover and simmer for 20 minutes.",
                new String[][]{{"chicken breast", "2", "pcs"}, {"rice", "200", "g"},
                        {"onion", "1", "pcs"}, {"garlic", "2", "pcs"}});

        addSeedRecipe(db, "Potato Soup",
                "Peel and dice the potatoes and onion.\n"
                        + "Fry the onion in butter until soft.\n"
                        + "Add the potatoes and cover with water, cook for 20 minutes.\n"
                        + "Add the milk and blend until smooth.",
                new String[][]{{"potato", "4", "pcs"}, {"onion", "1", "pcs"}, {"milk", "250", "ml"},
                        {"butter", "20", "g"}});

        addSeedRecipe(db, "Grilled Cheese Sandwich",
                "Butter one side of each slice of bread.\n"
                        + "Put the cheese between the unbuttered sides.\n"
                        + "Fry in a pan until golden and the cheese melts.",
                new String[][]{{"bread", "2", "pcs"}, {"cheese", "60", "g"}, {"butter", "15", "g"}});

        addSeedRecipe(db, "Banana Smoothie",
                "Peel the bananas.\n"
                        + "Blend the bananas, milk and yoghurt until smooth.\n"
                        + "Serve cold.",
                new String[][]{{"banana", "2", "pcs"}, {"milk", "250", "ml"}, {"yoghurt", "100", "g"}});

        addSeedRecipe(db, "Egg Fried Rice",
                "Cook the rice and let it cool.\n"
                        + "Fry the chopped onion and grated carrot.\n"
                        + "Push to the side, scramble the eggs in the pan.\n"
                        + "Add the rice and soy sauce and stir fry for 3 minutes.",
                new String[][]{{"rice", "200", "g"}, {"egg", "2", "pcs"}, {"carrot", "1", "pcs"},
                        {"soy sauce", "20", "ml"}, {"onion", "1", "pcs"}});

        addSeedRecipe(db, "Tuna Pasta Salad",
                "Boil the pasta and let it cool.\n"
                        + "Drain the tuna and dice the cucumber.\n"
                        + "Mix everything with the mayonnaise.",
                new String[][]{{"pasta", "150", "g"}, {"tuna", "170", "g"}, {"mayonnaise", "40", "g"},
                        {"cucumber", "1", "pcs"}});

        addSeedRecipe(db, "Garden Salad",
                "Wash and chop the lettuce.\n"
                        + "Slice the tomatoes and onion.\n"
                        + "Toss everything together with olive oil.",
                new String[][]{{"lettuce", "1", "pcs"}, {"tomato", "2", "pcs"}, {"onion", "1", "pcs"},
                        {"olive oil", "15", "ml"}});

        addSeedRecipe(db, "Mashed Potatoes",
                "Peel and boil the potatoes until soft.\n"
                        + "Drain and mash them.\n"
                        + "Stir in the butter and warm milk until creamy.",
                new String[][]{{"potato", "5", "pcs"}, {"butter", "30", "g"}, {"milk", "100", "ml"}});

        addSeedRecipe(db, "Spaghetti Bolognese",
                "Brown the beef mince in a pot.\n"
                        + "Add the chopped onion and garlic and fry for 2 minutes.\n"
                        + "Add the chopped tomatoes and simmer for 20 minutes.\n"
                        + "Serve over the cooked pasta.",
                new String[][]{{"beef mince", "500", "g"}, {"pasta", "250", "g"}, {"tomato", "4", "pcs"},
                        {"onion", "1", "pcs"}, {"garlic", "2", "pcs"}});

        addSeedRecipe(db, "Pap with Tomato Relish",
                "Boil 750 ml of water and slowly stir in the maize meal.\n"
                        + "Cover and cook on low heat for 20 minutes, stirring now and then.\n"
                        + "Fry the chopped onion in the oil, add the tomatoes and cook down.\n"
                        + "Serve the relish on top of the pap.",
                new String[][]{{"maize meal", "250", "g"}, {"tomato", "3", "pcs"}, {"onion", "1", "pcs"},
                        {"sunflower oil", "15", "ml"}});

        addSeedRecipe(db, "Apple Crumble",
                "Peel and slice the apples and put them in a baking dish.\n"
                        + "Rub the flour, butter and sugar together to make crumbs.\n"
                        + "Sprinkle the crumbs over the apples.\n"
                        + "Bake at 180 degrees C for 30 minutes.",
                new String[][]{{"apple", "4", "pcs"}, {"flour", "100", "g"}, {"butter", "60", "g"},
                        {"sugar", "80", "g"}});

        addSeedRecipe(db, "Chicken Wraps",
                "Cook the chicken breast and slice it.\n"
                        + "Shred the lettuce and slice the tomato.\n"
                        + "Spread mayonnaise on the tortillas, add the filling and roll up.",
                new String[][]{{"tortilla", "2", "pcs"}, {"chicken breast", "1", "pcs"},
                        {"lettuce", "1", "pcs"}, {"tomato", "1", "pcs"}, {"mayonnaise", "20", "g"}});

        addSeedRecipe(db, "Oats Porridge",
                "Put the oats and milk in a pot.\n"
                        + "Cook on medium heat for 5 minutes, stirring.\n"
                        + "Sweeten with the sugar and serve warm.",
                new String[][]{{"oats", "80", "g"}, {"milk", "250", "ml"}, {"sugar", "10", "g"}});

        addSeedRecipe(db, "Garlic Butter Mushrooms",
                "Slice the mushrooms and crush the garlic.\n"
                        + "Melt the butter in a pan.\n"
                        + "Fry the mushrooms and garlic until golden, about 6 minutes.",
                new String[][]{{"mushroom", "250", "g"}, {"butter", "30", "g"}, {"garlic", "2", "pcs"}});
    }
}
