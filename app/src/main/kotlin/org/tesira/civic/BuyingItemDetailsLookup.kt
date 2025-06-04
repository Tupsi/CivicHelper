package org.tesira.civic

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class BuyingItemDetailsLookup(private val mRecyclerView: RecyclerView) :
    ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = mRecyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val viewHolder = mRecyclerView.getChildViewHolder(view)
            if (viewHolder is BuyingAdapter.ViewHolder) {
                return viewHolder.itemDetails
            }
        }
        return null
    }
}