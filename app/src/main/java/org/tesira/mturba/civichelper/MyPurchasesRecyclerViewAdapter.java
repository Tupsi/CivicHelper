package org.tesira.mturba.civichelper;

import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.tesira.mturba.civichelper.databinding.ItemRowPurchasesBinding;
import org.tesira.mturba.civichelper.db.Card;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Card}.
 * Shows all Civilization Advances and highlights already purchases ones.
 */
public class MyPurchasesRecyclerViewAdapter extends RecyclerView.Adapter<MyPurchasesRecyclerViewAdapter.ViewHolder> {

    private final List<Card> mValues;

    public MyPurchasesRecyclerViewAdapter(List<Card> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(ItemRowPurchasesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mName.setText(mValues.get(position).getName());
        holder.mName.setBackground(CivicsListAdapter.getItemBackgroundColor(holder.mItem, holder.itemView.getResources()));
        holder.mPrice.setText(Integer.toString(mValues.get(position).getPrice()));
        holder.mVp.setText(Integer.toString(mValues.get(position).getVp()));
        holder.mCurrentPrice.setText(Integer.toString(mValues.get(position).getCurrentPrice()));

        if (holder.mItem.getBonus() > 0) {
            holder.mBonus.setVisibility(View.VISIBLE);
            String bonus = "+" + holder.mItem.getBonus() + " to " + holder.mItem.getBonusCard();
            holder.mBonus.setText(bonus);
        }
        else {
            holder.mBonus.setVisibility(View.INVISIBLE);
        }

        switch (mValues.get(position).getGroup1()) {
            case YELLOW:
            case GREEN:
                holder.mName.setTextColor(Color.BLACK);
                break;
            default:
                holder.mName.setTextColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mName;
        public final TextView mPrice;
        public Card mItem;
        public final View mCardView;
        public final TextView mBonus;
        public final TextView mVp;
        public final TextView mCurrentPrice;


        public ViewHolder(ItemRowPurchasesBinding binding) {
            super(binding.getRoot());
            mName = binding.name;
            mPrice = binding.price;
            mCardView = binding.card;
            mBonus = binding.familybonus;
            mVp = binding.vp;
            mCurrentPrice = binding.currentPrice;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}