package org.tesira.mturba.civichelper;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.tesira.mturba.civichelper.databinding.ItemRowBinding;
import org.tesira.mturba.civichelper.databinding.ItemRowPurchasesBinding;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicRepository;

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

        if (holder.mItem.getBonus() > 0) {
            holder.mFamilyBox.setVisibility(View.VISIBLE);
        }
        else {
            holder.mFamilyBox.setVisibility(View.INVISIBLE);
        }
        String bonus = "+" + holder.mItem.getBonus() + " to " + holder.mItem.getBonusCard();
        holder.mBonus.setText(bonus);

//        if (mPurchases.contains(holder.mItem.getName())) {
////            holder.mCardView.setAlpha(1.0F);
//            holder.mCardView.setBackgroundResource(R.drawable.card_selected);
//            holder.mPrice.setVisibility(View.GONE);
//            holder.mFamilyBox.setVisibility(View.GONE);
//            ViewGroup.LayoutParams params = holder.mCardView.getLayoutParams();
//            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//            holder.mCardView.requestLayout();
//        } else {
////            holder.mCardView.setAlpha(0.5F);
//        }
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
        public final LinearLayout mFamilyBox;
        public final TextView mBonus;


        public ViewHolder(ItemRowPurchasesBinding binding) {
            super(binding.getRoot());
            mName = binding.name;
            mPrice = binding.price;
            mCardView = binding.card;
            mFamilyBox = binding.familylayout;
            mBonus = binding.familybonus;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}