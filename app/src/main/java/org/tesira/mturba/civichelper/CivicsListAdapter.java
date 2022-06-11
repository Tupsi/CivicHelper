package org.tesira.mturba.civichelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;

public class CivicsListAdapter extends ListAdapter<Card, CivicsViewHolder> {

    private SelectionTracker<String> tracker;
    private CivicViewModel mCivicViewModel;
    private LinearLayoutManager mLayout;
    private AdvancesFragment mFragment;
    private RecyclerView.LayoutManager manager;

    public CivicsListAdapter(@NonNull DiffUtil.ItemCallback<Card> diffCallback, LinearLayoutManager layout, AdvancesFragment advancesFragment) {
        super(diffCallback);
        mFragment = advancesFragment;
        this.mLayout = layout;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        manager = recyclerView.getLayoutManager();
    }

    @NonNull
    @Override
    public CivicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.v("HOLDER", "inside onCreateViewHolder :" + viewType);
        return CivicsViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CivicsViewHolder holder, int position) {

        Card current = getItem(position);
        String name = current.getName();
        int price = current.getCurrentPrice();
        Resources res = holder.itemView.getResources();
        boolean isSelected = tracker.isSelected(name);

        holder.bindName(name, getItemBackgroundColor(current, res));
        holder.bindPrice(current.getCurrentPrice());
        holder.bindBonus(current.getBonus());
        holder.bindBonusCard(current.getBonusCard());

        if (tracker.hasSelection()) {
            Log.v("HOLDER", "inside onBindViewHolder :" + name + " : Position: " + position);
        }

        holder.bindIsActive(isSelected);
        holder.itemView.setOnClickListener(v -> {
            // clicked on single card in list
            Log.v("HOLDER", "inside onBindViewHolder onClickListener");
            tracker.select(name);
//            Toast.makeText(v.getContext(), name
//                    + " clicked. \nYou can select more advances if you have the treasure.",Toast.LENGTH_SHORT).show();
        });
        if (!isSelected && price == 0) {
            tracker.select(name);
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.getRemaining().getValue() < price) {
//                holder.mCardView.setBackgroundResource(R.color.dark_grey);
                holder.mCardView.setAlpha(0.5F);
            } else {
//                holder.mCardView.setBackgroundResource(R.drawable.item_background);
                holder.mCardView.setAlpha(1F);
            }
        }

        if (current.getBonus() > 0) {
            holder.mFamilyBox.setVisibility(View.VISIBLE);
        }
        else {
            holder.mFamilyBox.setVisibility(View.INVISIBLE);
        }
    }

    static class CivicsDiff extends DiffUtil.ItemCallback<Card> {

        @Override
        public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            Log.v("DIFF", "inside are ItemsTheSame");
            return oldItem.getIsBuyable() == newItem.getIsBuyable();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            Log.v("DIFF", "inside are ContentTheSame");
            return oldItem.getIsBuyable() == oldItem.getIsBuyable();
        }
    }
    public void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }
    public void setCivicViewModel(CivicViewModel model) {
        this.mCivicViewModel = model;
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

    public void checkBuyable(Integer value) {
        int first = mLayout.findFirstVisibleItemPosition();
        int last = mLayout.findLastVisibleItemPosition();
        Log.v("VISIBLE", " first : " + first + " : last : " + last);
        TextView priceText;
        for (int i = first; i < last; i++) {
            View child = mLayout.getChildAt(i);
            if (child != null) {
                priceText = mLayout.getChildAt(i).findViewById(R.id.price);
                int price = Integer.parseInt((String) priceText.getText());
                if (price > value) {
                    Log.v("VISIBLE", "needs notifyItemChanged");
                }

            }
//            notifyItemChanged(i);
        }
    }

    public void checkVisibility(){
        Log.v("VIS", "getChildCount: " + manager.getChildCount());
        for (int i=0; i < manager.getChildCount(); i++  ) {
            if (manager.isViewPartiallyVisible(manager.getChildAt(i),false, true)) {
                View child = manager.getChildAt(i);
                Card currentCard = getItem(i);
                Log.v("VIS", "checkVis :" + currentCard.getName() + " : " + i);
            }
        }

    }
}
