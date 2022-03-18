package org.tesira.mturba.civichelper;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
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
    private Context context;

    public MyAdvancesRecyclerViewAdapter(List<Advance> items, Context context) {
        mValues = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentAdvancesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

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
            switch (holder.mItem.getName()) {
                case "Engineering":
                    drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.engineering_background, null);
//                    drawable = context.getResources().getDrawable(R.drawable.engineering_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Mathematics":
                    drawable = context.getResources().getDrawable(R.drawable.mathematics_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Mysticism":
                    drawable = context.getResources().getDrawable(R.drawable.mysticism_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Written Record":
                    drawable = context.getResources().getDrawable(R.drawable.written_record_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Theocracy":
                    drawable = context.getResources().getDrawable(R.drawable.theocracy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Literacy":
                    drawable = context.getResources().getDrawable(R.drawable.literacy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Wonder of the World":
                    drawable = context.getResources().getDrawable(R.drawable.wonders_of_the_world_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
                case "Philosophy":
                    drawable = context.getResources().getDrawable(R.drawable.philosophy_background,null);
                    holder.mNameView.setBackground(drawable);
                    break;
            }
        } else {
            holder.mNameView.setBackgroundResource(backgroundColor);
        }

        holder.mPriceView.setText(Integer.toString(mValues.get(position).getPrice()));
//        holder.mPriceView.setBackgroundResource(backgroundColor);
//        holder.mConstraintView.setBackgroundResource(backgroundColor);
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
        public final ConstraintLayout mConstraintView;

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
            mConstraintView = binding.card;
        }

        @Override
        public String toString() {
            return mNameView.getText().toString();
//            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}