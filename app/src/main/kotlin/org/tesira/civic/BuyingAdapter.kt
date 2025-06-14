package org.tesira.civic

import androidx.recyclerview.selection.SelectionTracker
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.db.CivicViewModel

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
class BuyingAdapter(private val mCivicViewModel: CivicViewModel) : AllCardsAdapter() {
    private lateinit var tracker: SelectionTracker<String>

    fun setSelectionTracker(tracker: SelectionTracker<String>) {
        this.tracker = tracker
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item: CardWithDetails = getItem(position)
        val cardData = item.card
        val isSelected = tracker.isSelected(cardData.name)
        holder.itemView.isActivated = isSelected
        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && cardData.currentPrice == 0) {
            tracker.select(cardData.name)
        } else {
            // can we buy the card?
            if (!isSelected && mCivicViewModel.remaining.getValue()!! < cardData.currentPrice) {
                holder.binding.cardDetails.alpha = 0.25f
            } else {
                holder.binding.cardDetails.alpha = 1f
            }

        }

    }

    fun getKeyAtPosition(position: Int): String? {
        if (position >= 0 && position < itemCount) {
            return getItem(position)?.card?.name
        }
        return null
    }

}
