package org.tesira.mturba.civichelper;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

class CivicsViewHolder extends RecyclerView.ViewHolder {

    public final TextView nameItemView;
    private final TextView priceItemView;
//    private final TextView bonusCardItemView;
    public final TextView bonusItemView;
    public final TextView vpItemView;
    public final View mCardView;
//    public final LinearLayout mFamilyBox;

    private CivicsViewHolder(View itemView) {
        super(itemView);
        nameItemView = itemView.findViewById(R.id.name);
        priceItemView = itemView.findViewById(R.id.price);
        vpItemView = itemView.findViewById(R.id.vp);
//        bonusCardItemView = itemView.findViewById(R.id.familyname);
        bonusItemView = itemView.findViewById(R.id.familybonus);
        mCardView = itemView.findViewById(R.id.card);
//        mFamilyBox = itemView.findViewById(R.id.familylayout);

    }

    public void bindName(String name, Drawable drawable) {
        nameItemView.setText(name);
        nameItemView.setBackground(drawable);
    }
    public void bindPrice(int price) {
        priceItemView.setText(String.valueOf(price));
    }
//    public void bindBonusCard(String cardName) {bonusCardItemView.setText(cardName);};
//    public void bindBonus(int bonus) {bonusItemView.setText(String.valueOf(bonus));}
    public void bindIsActive(boolean isActive) {mCardView.setActivated(isActive);}

    static CivicsViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_advances, parent, false);
        return new CivicsViewHolder(view);
    }

    public ItemDetailsLookup.ItemDetails<String> getItemDetails() {
        return new MyItemDetails(getBindingAdapterPosition(), nameItemView.getText().toString());
    }
}
