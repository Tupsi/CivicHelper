package org.tesira.mturba.civichelper;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
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
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAdvancesRecyclerViewAdapter extends RecyclerView.Adapter<MyAdvancesRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final List<Advance> mValues;
    private final List<Advance> FullList;
    private Context context;
    private SelectionTracker<Long> tracker;

    public MyAdvancesRecyclerViewAdapter(List<Advance> items, Context context) {
        mValues = items;
        this.context = context;
        FullList = new ArrayList<>(items);
        // funktioniert nicht
        setHasStableIds(true);
    }

    public void setSelectionTracker(SelectionTracker<Long> tracker) {
        this.tracker = tracker;
    }

    @Override
    public long getItemId(int position) {
        return Integer.toUnsignedLong(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdvancesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Advance item = mValues.get(position);
        holder.mItem = item;
//        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(Integer.toString(mValues.get(position).getPrice()));
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);
//        holder.mContentView.setText(mValues.get(position).getName());
        holder.mNameView.setText(mValues.get(position).getName());
        int backgroundColor = holder.mItem.getColor();
        if (backgroundColor == 0) {
            // card with two colors
            Resources res = context.getResources();
            mixedBackground(holder, res);
        } else {
            holder.mNameView.setBackgroundResource(backgroundColor);
        }

        holder.mCardView.setActivated(tracker.isSelected(Integer.toUnsignedLong(position)));

        holder.mPriceView.setText(Integer.toString(mValues.get(position).getPrice()));
        holder.mCardView.setOnClickListener(v -> {
            // clicked on single card in list
            Toast.makeText(v.getContext(), holder.mNameView.getText().toString() + " clicked!",Toast.LENGTH_LONG).show();
            Log.v("INFO","card clicked");
            Log.v("INFO", holder.mNameView.getText().toString());
            Log.v("INFO", "LayoutPosition: "+holder.getLayoutPosition());
            Log.v("INFO", "AbsolutAdapter: "+holder.getAbsoluteAdapterPosition());
            Log.v("INFO", "AbsolutAdapter: "+holder.getAbsoluteAdapterPosition());
            Log.v("INFO", "       Adapter: "+holder.getAdapterPosition());
            Log.v("INFO", "Position      : "+position);
            Log.v("INFO", "getID   :" + getItemId(position));
        });
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
            mValues.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        // hier eventuell anpassen was angezeigt werden soll aus dem Advance Objekt!
//        public View mView;
        public View mCardView;
//        public final TextView mIdView;
//        public final TextView mContentView;
        public final TextView mNameView;
        public final TextView mPriceView;
        public final Group mGroupView;
//        public final ConstraintLayout mConstraintView;

        public Advance mItem;

        public ViewHolder(FragmentAdvancesBinding binding) {
            super(binding.getRoot());
            // needed for OnClickListener
//            mView = binding.getRoot();
            // das sind die R.id aus fragements_advances textviews
            // hier muessen mehr dazu falls mehr dateils angezeigt werden soll
//            mIdView = binding.itemNumber;
//            mContentView = binding.content;
            mNameView = binding.name;
            mPriceView = binding.price;
            mGroupView = binding.group;
            mCardView = binding.card;
//            mConstraintView = binding.card;

        }

//        public final void bind(Advance item, boolean isActive){
//            mCardView.setActivated();
//        }

        @NonNull
        @Override
        public String toString() {
            return mNameView.getText().toString();
//            return super.toString() + " '" + mNameView.getText() + "'";
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            Log.v ("INFO", "ItemDetails :" + getAdapterPosition());
            return new MyItemDetails(getAdapterPosition(), getItemId());
        }
    }
}