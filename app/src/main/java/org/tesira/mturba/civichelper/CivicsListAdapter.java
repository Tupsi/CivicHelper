package org.tesira.mturba.civichelper;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.tesira.mturba.civichelper.db.CivilizationAdvance;

public class CivicsListAdapter extends ListAdapter<CivilizationAdvance, CivicsViewHolder> {

    public CivicsListAdapter(@NonNull DiffUtil.ItemCallback<CivilizationAdvance> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public CivicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return CivicsViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull CivicsViewHolder holder, int position) {
            CivilizationAdvance current = getItem(position);
            holder.bindName(current.getName());
            holder.bindPrice(current.getPrice());
    }

    static class CivicsDiff extends DiffUtil.ItemCallback<CivilizationAdvance> {

        @Override
        public boolean areItemsTheSame(@NonNull CivilizationAdvance oldItem, @NonNull CivilizationAdvance newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CivilizationAdvance oldItem, @NonNull CivilizationAdvance newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    }
}
