package org.tesira.mturba.civichelper;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class MyItemDetails extends ItemDetailsLookup.ItemDetails<String> {

    private final int adapterPosition;
    private final String itemId;

    public MyItemDetails(int adapterPosition, String itemId) {
        this.adapterPosition = adapterPosition;
        this.itemId = itemId;
    }

    @Override
    public int getPosition() {
        return adapterPosition;
    }

    @Nullable
    @Override
    public String getSelectionKey() {
        return itemId;
    }
}
