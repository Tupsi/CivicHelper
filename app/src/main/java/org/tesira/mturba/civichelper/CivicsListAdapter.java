package org.tesira.mturba.civichelper;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.tesira.mturba.civichelper.db.Card;

public class CivicsListAdapter extends ListAdapter<Card, CivicsViewHolder> {

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
            holder.bindName(current.getName());
            holder.bindPrice(current.getPrice());
            holder.bindBonus(current.getBonus());
            holder.bindBonusCard(current.getBonusCard());
    }

    static class CivicsDiff extends DiffUtil.ItemCallback<Card> {

        @Override
        public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
