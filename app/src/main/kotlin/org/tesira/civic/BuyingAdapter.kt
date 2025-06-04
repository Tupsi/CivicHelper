package org.tesira.civic

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.db.Card
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
class BuyingAdapter(
    /**
     * Returns the current list of cards displayed by the adapter.
     * This is needed by MyItemKeyProvider.
     * @return The current list of Card objects.
     */
    val items: MutableList<Card>, private val mCivicViewModel: CivicViewModel
) : RecyclerView.Adapter<BuyingAdapter.ViewHolder?>() {
    private lateinit var tracker: SelectionTracker<String>

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameItemView: TextView = itemView.findViewById<TextView>(R.id.name)
        val priceItemView: TextView = itemView.findViewById<TextView>(R.id.price)
        val bonusItemView: TextView = itemView.findViewById<TextView>(R.id.familybonus)
        val vpItemView: TextView = itemView.findViewById<TextView>(R.id.vp)
        val mCardView: View = itemView.findViewById<View>(R.id.card)
        val mHeartView: ImageView = itemView.findViewById<ImageView>(R.id.heart)

        val itemDetails: ItemDetailsLookup.ItemDetails<String>
            get() = BuyingItemDetails(
                bindingAdapterPosition,
                nameItemView.text.toString()
            )
    }

    fun setSelectionTracker(tracker: SelectionTracker<String>) {
        this.tracker = tracker
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_row_purchasables, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val current = items[position]
        val name = current.name
        val price = current.currentPrice
        val res = viewHolder.itemView.resources
        val isSelected = tracker.isSelected(name)
        viewHolder.nameItemView.text = name
        viewHolder.nameItemView.background =
            CivicViewModel.Companion.getItemBackgroundColor(current, res)
        viewHolder.priceItemView.text = current.currentPrice.toString()
        viewHolder.vpItemView.text = current.vp.toString()
        viewHolder.mCardView.isActivated = isSelected
        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && price == 0) {
            tracker.select(name)
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.remaining.getValue()!! < price) {
                viewHolder.mCardView.alpha = 0.25f
            } else {
                viewHolder.mCardView.alpha = 1f
            }
        }

        // adjust color of name depending on background, so it is readable
        when (current.group1) {
            CardColor.YELLOW, CardColor.GREEN -> viewHolder.nameItemView.setTextColor(
                Color.BLACK
            )

            else -> viewHolder.nameItemView.setTextColor(Color.WHITE)
        }

        // put the family bonus on the card
        if (current.bonus > 0) {
            viewHolder.bonusItemView.visibility = View.VISIBLE
            val bonus = "+" + current.bonus + " to " + current.bonusCard
            viewHolder.bonusItemView.text = bonus
        } else {
            viewHolder.bonusItemView.visibility = View.INVISIBLE
        }
        viewHolder.mHeartView.visibility =
            if (items[position].hasHeart) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * This updates the adapter with a new list and restores the already selected cards to the
     * tracker if any.
     * @param newList new sorted list of cards.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun changeList(newList: MutableList<Card>) {
        val saveSelection = Bundle()
        tracker.onSaveInstanceState(saveSelection)
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
        tracker.onRestoreInstanceState(saveSelection)
    }
}