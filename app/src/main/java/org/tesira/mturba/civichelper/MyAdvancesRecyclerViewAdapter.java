package org.tesira.mturba.civichelper;

import static org.tesira.mturba.civichelper.R.color.purple_200;

import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.placeholder.PlaceholderContent.PlaceholderItem;
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAdvancesRecyclerViewAdapter extends RecyclerView.Adapter<MyAdvancesRecyclerViewAdapter.ViewHolder> {

    // Hier PlaceholderItem mit Advance ersetzen !!!

    private final List<Advance> mValues;

    public MyAdvancesRecyclerViewAdapter(List<Advance> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentAdvancesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(Integer.toString(mValues.get(position).getPrice()));
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);
//        holder.mContentView.setText(mValues.get(position).getName());
        holder.mNameView.setText(mValues.get(position).getName());
        int backgroundColor = holder.mItem.getColor();
        holder.mNameView.setBackgroundResource(backgroundColor);
//        holder.mGroupView.setBackgroundResource(backgroundColor);
        holder.mPriceView.setText(Integer.toString(mValues.get(position).getPrice()));
        holder.mPriceView.setBackgroundResource(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // hier eventuell anpassen was angezeigt werden soll aus dem Advance Objekt!

//        public final TextView mIdView;
//        public final TextView mContentView;
        public final TextView mNameView;
        public final TextView mPriceView;
        public final Group mGroupView;

        public Advance mItem;

        public ViewHolder(FragmentAdvancesBinding binding) {
            super(binding.getRoot());
            // das sind die R.id aus fragements_advances textviews
            // hier muessen mehr dazu falls mehr dateils angezeigt werden soll
//            mIdView = binding.itemNumber;
//            mContentView = binding.content;
            mNameView = binding.name;
            mPriceView = binding.price;
            mGroupView = binding.group;
        }

        @Override
        public String toString() {
            return mNameView.getText().toString();
//            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}