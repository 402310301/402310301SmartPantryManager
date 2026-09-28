package com.example.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.Recipe;

import java.util.List;

/**
 * Adapter for a list of recipes. It is used twice on the Suggested Recipes screen:
 * once for the strict suggestions and once for the "Almost There" list.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(recipe.getName());

        if (recipe.getMissingText() != null) {
            // "Almost There" recipe - show what is missing in orange
            holder.tvInfo.setText(recipe.getMissingText());
            holder.tvInfo.setTextColor(ContextCompat.getColor(context, R.color.warning));
        } else {
            // Strict match - show the ingredients it uses
            holder.tvInfo.setText(context.getString(R.string.uses_text, recipe.getIngredientNamesText()));
            holder.tvInfo.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvInfo;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvInfo = itemView.findViewById(R.id.tvRecipeInfo);
        }
    }
}
