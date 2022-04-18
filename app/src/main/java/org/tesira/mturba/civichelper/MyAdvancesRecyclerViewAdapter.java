package org.tesira.mturba.civichelper;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.placeholder.PlaceholderContent.PlaceholderItem;
import org.tesira.mturba.civichelper.databinding.ItemRowBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Advance}.
 *
 */
public class MyAdvancesRecyclerViewAdapter extends RecyclerView.Adapter<MyAdvancesRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final List<Advance> mValues;
    private final List<Advance> FullList;
    private final Context context;
    private SelectionTracker<String> tracker;
    private int remainingTreasure;

    public MyAdvancesRecyclerViewAdapter(List<Advance> items, Context context) {
        mValues = items;
        this.context = context;
        FullList = new ArrayList<>(items);
        setHasStableIds(false);
    }

    public void setRemainingTreasure(int rest) {
        this.remainingTreasure = rest;
    }

    public void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String name = mValues.get(position).getName();
        holder.mNameView.setText(name);
        int backgroundColor = holder.mItem.getColor();
        if (backgroundColor == 0) {
            // card with two colors
            Resources res = context.getResources();
            mixedBackground(holder, res);
        } else {
            holder.mNameView.setBackgroundResource(backgroundColor);
        }

        boolean isActivated = tracker.isSelected(name);
        holder.mCardView.setActivated(isActivated);
        holder.mPriceView.setText(Integer.toString(mValues.get(position).getPrice()));
        holder.mCardView.setOnClickListener(v -> {
            // clicked on single card in list
            tracker.select(name);
            Toast.makeText(v.getContext(), holder.mNameView.getText().toString()
                    + " clicked. \nYou can select more advances if you have the treasure.",Toast.LENGTH_SHORT).show();
        });
        int price = mValues.get(position).getPrice();
        if (!isActivated && remainingTreasure < price) {
            holder.mCardView.setBackgroundResource(R.color.dark_grey);
            holder.mCardView.setAlpha(0.5F);
        } else {
            holder.mCardView.setBackgroundResource(R.drawable.item_background);
            holder.mCardView.setAlpha(1F);
        }
    }

    private void mixedBackground(@NonNull ViewHolder holder, Resources res) {
        Drawable drawable = null;
        switch (holder.mItem.getName()) {
            case "Engineering":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.engineering_background, null);
                break;
            case "Mathematics":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.mathematics_background,null);
                break;
            case "Mysticism":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.mysticism_background,null);
                break;
            case "Written Record":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.written_record_background,null);
                break;
            case "Theocracy":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.theocracy_background,null);
                break;
            case "Literacy":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.literacy_background,null);
                break;
            case "Wonder of the World":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.wonders_of_the_world_background,null);
                break;
            case "Philosophy":
                drawable = ResourcesCompat.getDrawable(res, R.drawable.philosophy_background,null);
                break;
        }
        holder.mNameView.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public Filter getFilter() {
        return Searched_Filter;
    }

    private Filter Searched_Filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Advance> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(FullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Advance item : FullList) {
                    if (item.toString().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mValues.clear();
            mValues.addAll((ArrayList<Advance>) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        // hier eventuell anpassen was angezeigt werden soll aus dem Advance Objekt!
        // Constraint Layout whole row
        public View mCardView;
        public final TextView mNameView;
        public final TextView mPriceView;

        public Advance mItem;

        public ViewHolder(ItemRowBinding binding) {
            super(binding.getRoot());
            // das sind die R.id aus item_row.xml textviews
            // hier muessen mehr dazu falls mehr dateils angezeigt werden soll
            mNameView = binding.name;
            mPriceView = binding.price;
            mCardView = binding.card;
        }

        @NonNull
        @Override
        public String toString() {
            return mNameView.getText().toString();
//            return super.toString() + " '" + mNameView.getText() + "'";
        }

        public ItemDetailsLookup.ItemDetails<String> getItemDetails() {
//            Log.v ("INFOI", "ItemDetails getAdapter        :" + getAdapterPosition());
//            Log.v ("INFOI", "ItemDetails getBindingAdapter :" + getBindingAdapterPosition());
//            Log.v ("INFOI", "ItemDetails getBindingAdapter :" + getAbsoluteAdapterPosition());
            return new MyItemDetails(getBindingAdapterPosition(), mItem.getName());
        }
    }
}