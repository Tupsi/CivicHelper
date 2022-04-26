package org.tesira.mturba.civichelper;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.mturba.civichelper.db.CivicViewModel;

public class MyItemDetailsLookup extends ItemDetailsLookup<String> {

    private final RecyclerView mRecyclerView;

    public MyItemDetailsLookup(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof CivicsViewHolder) {
                return ((CivicsViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}
