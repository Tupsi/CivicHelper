package org.tesira.mturba.civichelper;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private Context context;

    public MyAdvancesRecyclerViewAdapter(List<Advance> items, Context context) {
        mValues = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdvancesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(Integer.toString(mValues.get(position).getPrice()));
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);
//        holder.mContentView.setText(mValues.get(position).getName());
        holder.mNameView.setText(mValues.get(position).getName());
        int backgroundColor = holder.mItem.getColor();
        if (backgroundColor == 0) {
            // card with two colors
            Drawable drawable;
            Resources res = context.getResources();
            switch (holder.mItem.getName()) {
                case "Engineering":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.engineering_background, null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Mathematics":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.mathematics_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Mysticism":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.mysticism_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Written Record":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.written_record_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Theocracy":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.theocracy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Literacy":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.literacy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Wonder of the World":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.wonders_of_the_world_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Philosophy":
                    drawable = ResourcesCompat.getDrawable(res, R.drawable.philosophy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
            }
        } else {
            holder.mNameView.setBackgroundResource(backgroundColor);
        }

        holder.mPriceView.setText(Integer.toString(mValues.get(position).getPrice()));
//        holder.mPriceView.setBackgroundResource(backgroundColor);
//        holder.mConstraintView.setBackgroundResource(backgroundColor);
        holder.mCardView.setOnClickListener(v -> {
            // clicked on single card in list
            Toast.makeText(v.getContext(), holder.mNameView.getText().toString() + " clicked!",Toast.LENGTH_LONG).show();
            Log.v("INFO","card clicked");
            Log.v("INFO", holder.mNameView.getText().toString());
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

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

        @NonNull
        @Override
        public String toString() {
            return mNameView.getText().toString();
//            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}