package org.tesira.mturba.civichelper;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import java.util.List;

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
public class BuyingListAdapter extends RecyclerView.Adapter<BuyingListAdapter.ViewHolder> {

    private List<Card> mValues;
    private SelectionTracker<String> tracker;
    private CivicViewModel mCivicViewModel;

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
            return new MyItemDetails(getBindingAdapterPosition(), nameItemView.getText().toString());
        }
    }

    public BuyingListAdapter(List<Card> dataSet, CivicViewModel mCivicViewModel) {
        this.mValues = dataSet;
        this.mCivicViewModel = mCivicViewModel;
    }

    public void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row_advances, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        Card current = mValues.get(position);
        String name = current.getName();
        int price = current.getCurrentPrice();
        Resources res = viewHolder.itemView.getResources();
        Selection<String> allClicked = tracker.getSelection();
        for (String selected: tracker.getSelection()
             ) {
            Log.v("TRACKER", selected);
        }
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
            if (!isSelected && mCivicViewModel.getRemaining().getValue() < price) {
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
        List<String> hearts = mCivicViewModel.getChooserCards();
        viewHolder.mHeartView.setVisibility(View.INVISIBLE);
        if (hearts != null && hearts.contains(viewHolder.nameItemView.getText().toString())) {
            viewHolder.mHeartView.setVisibility(View.VISIBLE);
        }
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
    public void changeList(List<Card> newList) {
        Bundle saveSelection = new Bundle();
        tracker.onSaveInstanceState(saveSelection);
        mValues.clear();
        mValues.addAll(newList);
        notifyDataSetChanged();
        tracker.onRestoreInstanceState(saveSelection);
    }

}
