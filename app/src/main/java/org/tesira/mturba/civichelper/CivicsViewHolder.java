package org.tesira.mturba.civichelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

class CivicsViewHolder extends RecyclerView.ViewHolder {

    private final TextView nameItemView;
    private final TextView priceItemView;

    private CivicsViewHolder(View itemView) {
        super(itemView);
        nameItemView = itemView.findViewById(R.id.name);
        priceItemView = itemView.findViewById(R.id.price);
    }

    public void bindName(String name) {
        nameItemView.setText(name);
    }
    public void bindPrice(int price) {
        priceItemView.setText(String.valueOf(price));
    }

    static CivicsViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new CivicsViewHolder(view);
    }
}
