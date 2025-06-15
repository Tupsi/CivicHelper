package org.tesira.civic

import androidx.recyclerview.selection.SelectionTracker
import org.tesira.civic.db.CardWithDetails

/**
 * Adapter for the buying process. Displays all cards which are still buyable with current price.
 * Checks if there is enough treasure, otherwise grays out the item. Sets a heart if user has
 * set a "what do I want?" preference.
 */
class BuyingAdapter() : AllCardsAdapter() {
    private lateinit var tracker: SelectionTracker<String>
    private var remaining = 0

    fun setSelectionTracker(tracker: SelectionTracker<String>) {
        this.tracker = tracker
    }

    fun setRemaining(remaining: Int) {
        this.remaining = remaining
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item: CardWithDetails = getItem(position)
        val cardData = item.card
        val isSelected = tracker.isSelected(cardData.name)
        holder.itemView.isActivated = isSelected
        // Baseclass uses price, so we need to overwrite this here
        holder.binding.price.text = "${cardData.currentPrice}"
        // auto select all gets which have costs are reduced from bonus to zero
        if (!isSelected && cardData.currentPrice == 0) {
            tracker.select(cardData.name)
        } else {
            // can we buy the card?
            if (!isSelected && remaining < cardData.currentPrice) {
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
