package org.tesira.civic;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class BuyingItemDetails extends ItemDetailsLookup.ItemDetails<String> {

    private final int adapterPosition;
    private final String itemId;

    public BuyingItemDetails(int adapterPosition, String itemId) {
        this.adapterPosition = adapterPosition;
        this.itemId = itemId;
    }

    /**
     * Areas are often included in a view that behave similar to checkboxes, such
     * as the icon to the left of an email message. "selection
     * hotspot" provides a mechanism to identify such regions, and for the
     * library to directly translate taps in these regions into a change
     * in selection state.
     *
     * @param e
     * @return true if the event is in an area of the item that should be
     * directly interpreted as a user wishing to select the item. This
     * is useful for checkboxes and other UI affordances focused on enabling
     * selection.
     */
    @Override
    public boolean inSelectionHotspot(@NonNull MotionEvent e) {
        return true;
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
