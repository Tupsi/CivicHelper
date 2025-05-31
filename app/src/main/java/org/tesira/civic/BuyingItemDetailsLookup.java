package org.tesira.civic;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class BuyingItemDetailsLookup extends ItemDetailsLookup<String> {

    private final RecyclerView mRecyclerView;

    public BuyingItemDetailsLookup(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof BuyingAdapter.ViewHolder) {
                return ((BuyingAdapter.ViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}
