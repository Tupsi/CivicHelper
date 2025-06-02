package org.tesira.civic;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.db.Card;
import org.tesira.civic.db.CivicViewModel;
import java.util.List;

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
public class BuyingAdapter extends RecyclerView.Adapter<BuyingAdapter.ViewHolder> {

    private final  List<Card> mValues;
    private SelectionTracker<String> tracker;
    private final CivicViewModel mCivicViewModel;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameItemView;
        private final TextView priceItemView;
        private final TextView bonusItemView;
        private final TextView vpItemView;
        private final View mCardView;
        private final ImageView mHeartView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameItemView = itemView.findViewById(R.id.name);
            priceItemView = itemView.findViewById(R.id.price);
            vpItemView = itemView.findViewById(R.id.vp);
            bonusItemView = itemView.findViewById(R.id.familybonus);
            mCardView = itemView.findViewById(R.id.card);
            mHeartView = itemView.findViewById(R.id.heart);
        }
        public ItemDetailsLookup.ItemDetails<String> getItemDetails() {
            return new BuyingItemDetails(getBindingAdapterPosition(), nameItemView.getText().toString());
        }
    }

    public BuyingAdapter(List<Card> dataSet, CivicViewModel mCivicViewModel) {
        this.mValues = dataSet;
        this.mCivicViewModel = mCivicViewModel;
    }

    public void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row_purchasables, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        Card current = mValues.get(position);
        String name = current.getName();
        int price = current.getCurrentPrice();
        Resources res = viewHolder.itemView.getResources();
        boolean isSelected = tracker.isSelected(name);
        viewHolder.nameItemView.setText(name);
        viewHolder.nameItemView.setBackground(CivicViewModel.getItemBackgroundColor(current, res));
        viewHolder.priceItemView.setText(String.valueOf(current.getCurrentPrice()));
        viewHolder.vpItemView.setText(String.valueOf(current.getVp()));
        viewHolder.mCardView.setActivated(isSelected);
        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && price == 0) {
            tracker.select(name);
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.remaining.getValue() < price) {
                viewHolder.mCardView.setAlpha(0.25F);
            } else {
                viewHolder.mCardView.setAlpha(1F);
            }
        }

        // adjust color of name depending on background, so it is readable
        switch (current.getGroup1()) {
            case YELLOW:
            case GREEN:
                viewHolder.nameItemView.setTextColor(Color.BLACK);
                break;
            default:
                viewHolder.nameItemView.setTextColor(Color.WHITE);
                break;
        }

        // put the family bonus on the card
        if (current.getBonus() > 0) {
            viewHolder.bonusItemView.setVisibility(View.VISIBLE);
            String bonus = "+" + current.getBonus() + " to " + current.getBonusCard();
            viewHolder.bonusItemView.setText(bonus);
        }
        else {
            viewHolder.bonusItemView.setVisibility(View.INVISIBLE);
        }
        viewHolder.mHeartView.setVisibility(mValues.get(position).getHasHeart() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * This updates the adapter with a new list and restores the already selected cards to the
     * tracker if any.
     * @param newList new sorted list of cards.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void changeList(List<Card> newList) {
        Bundle saveSelection = new Bundle();
        if (tracker != null) {
            tracker.onSaveInstanceState(saveSelection);
        }
        mValues.clear();
        mValues.addAll(newList);
        notifyDataSetChanged();
        if (tracker != null) {
            tracker.onRestoreInstanceState(saveSelection);
        }
    }

    /**
     * Returns the current list of cards displayed by the adapter.
     * This is needed by MyItemKeyProvider.
     * @return The current list of Card objects.
     */
    public List<Card> getItems() {
        return mValues;
    }
}
