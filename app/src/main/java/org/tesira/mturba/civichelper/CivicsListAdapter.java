package org.tesira.mturba.civichelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.tesira.mturba.civichelper.db.Card;

public class CivicsListAdapter extends ListAdapter<Card, CivicsViewHolder> {

    private SelectionTracker<String> tracker;

    public CivicsListAdapter(@NonNull DiffUtil.ItemCallback<Card> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public CivicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CivicsViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CivicsViewHolder holder, int position) {
        Card current = getItem(position);
        String name = current.getName();
        Resources res = holder.itemView.getResources();

        holder.bindName(name, getItemBackgroundColor(current, res));
        holder.bindPrice(current.getPrice());
        holder.bindBonus(current.getBonus());
        holder.bindBonusCard(current.getBonusCard());

        boolean isActivated = tracker.isSelected(name);

        holder.bindIsActive(isActivated);
        holder.itemView.setOnClickListener(v -> {
            // clicked on single card in list
            tracker.select(name);
            Toast.makeText(v.getContext(), name
                    + " clicked. \nYou can select more advances if you have the treasure.",Toast.LENGTH_SHORT).show();
        });
//        int price = mValues.get(position).getPrice();
//        if (!isActivated && remainingTreasure < price) {
//            holder.mCardView.setBackgroundResource(R.color.dark_grey);
//            holder.mCardView.setAlpha(0.5F);
//        } else {
//            holder.mCardView.setBackgroundResource(R.drawable.item_background);
//            holder.mCardView.setAlpha(1F);
//        }
//        int bonus = mValues.get(position).getFamilybonus();
//        if (bonus > 0) {
//            holder.mFamilybox.setVisibility(View.VISIBLE);
//            holder.mFamilyBonus.setText(String.valueOf(bonus));
//            holder.mFamilyName.setText(mValues.get(position).getFamilyname());
//        }
//        else {
//            holder.mFamilybox.setVisibility(View.INVISIBLE);
//        }



    }

    static class CivicsDiff extends DiffUtil.ItemCallback<Card> {

        @Override
        public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            return oldItem.getIsBuyable() == oldItem.getIsBuyable();
        }
    }
    public void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    public static Drawable getItemBackgroundColor(Card card, Resources res) {
        int backgroundColor = 0;
        if (card.getGroup2() == null) {
            switch (card.getGroup1()) {
                case ORANGE:
                    backgroundColor = R.color.crafts;
                    break;
                case YELLOW:
                    backgroundColor = R.color.religion;
                    break;
                case RED:
                    backgroundColor = R.color.civic;
                    break;
                case GREEN:
                    backgroundColor = R.color.science;
                    break;
                case BLUE:
                    backgroundColor = R.color.arts;
                    break;
                default:
                    backgroundColor = R.color.purple_700;
                    break;
            }
        } else {
            switch (card.getName()) {
                case "Engineering":
                    backgroundColor = R.drawable.engineering_background;
                break;
                case "Mathematics":
                    backgroundColor = R.drawable.mathematics_background;
                break;
                case "Mysticism":
                    backgroundColor = R.drawable.mysticism_background;
                break;
                case "Written Record":
                    backgroundColor = R.drawable.written_record_background;
                break;
                case "Theocracy":
                    backgroundColor = R.drawable.theocracy_background;
                break;
                case "Literacy":
                    backgroundColor = R.drawable.literacy_background;
                break;
                case "Wonder of the World":
                    backgroundColor = R.drawable.wonders_of_the_world_background;
                break;
                case "Philosophy":
                    backgroundColor = R.drawable.philosophy_background;
                break;
            }
        }
        return ResourcesCompat.getDrawable(res,backgroundColor, null);
    }
}
