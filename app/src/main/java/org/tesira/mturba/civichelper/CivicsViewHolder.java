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
    private final TextView bonusCardItemView;
    private final TextView bonusItemView;

    private CivicsViewHolder(View itemView) {
        super(itemView);
        nameItemView = itemView.findViewById(R.id.name);
        priceItemView = itemView.findViewById(R.id.price);
        bonusCardItemView = itemView.findViewById(R.id.familyname);
        bonusItemView = itemView.findViewById(R.id.familybonus);
    }

    public void bindName(String name) {
        nameItemView.setText(name);
    }
    public void bindPrice(int price) {
        priceItemView.setText(String.valueOf(price));
    }
    public void bindBonusCard(String cardName) {bonusCardItemView.setText(cardName);};
    public void bindBonus(int bonus) {bonusItemView.setText(String.valueOf(bonus));}

    static CivicsViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new CivicsViewHolder(view);
    }
}
