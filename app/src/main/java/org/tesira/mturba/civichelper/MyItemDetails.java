package org.tesira.mturba.civichelper;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class MyItemDetails extends ItemDetailsLookup.ItemDetails<Long> {

    private final int adapterPosition;
    private final Long itemId;

    public MyItemDetails(int adapterPosition, Long itemId) {
        this.adapterPosition = adapterPosition;
        this.itemId = itemId;
    }

    @Override
    public int getPosition() {
        return adapterPosition;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return itemId;
    }
}
