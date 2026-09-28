package com.example.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.IngredientUtils;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.DateUtils;

import java.util.List;

/**
 * Custom adapter that turns a list of PantryItem objects into rows on the screen.
 * The RecyclerView asks the adapter:
 *   onCreateViewHolder -> "make me a new empty row"
 *   onBindViewHolder   -> "fill this row with the data at position X"
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** The activity implements this so it knows when a row is tapped. */
    public interface OnPantryItemListener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items;
    private final OnPantryItemListener listener;
    private boolean expiryAlertsOn = true;
    private int expiryDays = 3;

    public PantryAdapter(List<PantryItem> items, OnPantryItemListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setExpirySettings(boolean alertsOn, int days) {
        this.expiryAlertsOn = alertsOn;
        this.expiryDays = days;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(IngredientUtils.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // Work out what to show for the expiry date, and in which colour
        int daysLeft = DateUtils.daysUntil(item.getExpiryDate());
        int colour = ContextCompat.getColor(context, R.color.text_secondary);

        if (daysLeft == DateUtils.NO_DATE) {
            holder.tvExpiry.setText(R.string.expiry_none);
        } else if (daysLeft < 0) {
            holder.tvExpiry.setText(context.getString(R.string.expiry_expired, item.getExpiryDate()));
            colour = ContextCompat.getColor(context, R.color.expired);
        } else if (expiryAlertsOn && daysLeft == 0) {
            holder.tvExpiry.setText(R.string.expiry_today);
            colour = ContextCompat.getColor(context, R.color.warning);
        } else if (expiryAlertsOn && daysLeft <= expiryDays) {
            holder.tvExpiry.setText(context.getString(R.string.expiry_soon, daysLeft));
            colour = ContextCompat.getColor(context, R.color.warning);
        } else {
            holder.tvExpiry.setText(context.getString(R.string.expiry_date, item.getExpiryDate()));
        }
        holder.tvExpiry.setTextColor(colour);

        // Tap the row to edit, tap the bin to delete
        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one row so we don't call findViewById every time. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvQuantity;
        TextView tvExpiry;
        ImageButton btnDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvExpiry = itemView.findViewById(R.id.tvItemExpiry);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
