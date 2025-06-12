package org.tesira.civic

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup

class BuyingItemDetails(private val adapterPosition: Int, private val itemId: String) :
    ItemDetailsLookup.ItemDetails<String>() {
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
    override fun inSelectionHotspot(e: MotionEvent): Boolean {
        return true
    }

    override fun getPosition(): Int {
        return adapterPosition
    }

    override fun getSelectionKey(): String {
        return itemId
    }
}