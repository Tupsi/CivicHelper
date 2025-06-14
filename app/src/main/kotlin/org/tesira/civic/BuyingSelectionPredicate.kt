package org.tesira.civic

import androidx.recyclerview.selection.SelectionTracker
import org.tesira.civic.db.Card
import org.tesira.civic.db.CivicViewModel

/**
 * SelectionPredicate to check if there is still enough treasure to select the clicked row
 * in the list of the advances fragment.
 * @param <String> Name of the civilization advance.
</String> */
class BuyingSelectionPredicate(
    private val adapter: BuyingAdapter,
    private val mCivicViewModel: CivicViewModel
) : SelectionTracker.SelectionPredicate<String>() {
    override fun canSetStateForKey(key: String, nextState: Boolean): Boolean {
        // check if there is still enough treasure to buy the new selected card

        val itemList = adapter.currentList // Greife auf die Liste vom Adapter zu
        var adv: Card? = null
        for (item in itemList) {
            if (key == item.card.name) {
                adv = item.card
                break
            }
        }

        if (adv == null) {
            return false // Kann den Zustand für eine unbekannte Karte nicht setzen
        }

        val currentPrice = adv.currentPrice
        val treasureValue = mCivicViewModel.treasure.getValue()
        val remainingValue = mCivicViewModel.remaining.getValue()

        if (treasureValue == null || remainingValue == null) {
            // Werte sind noch nicht initialisiert, Auswahl vorerst nicht erlauben
            return false
        }

        // Wenn die Karte abgewählt wird, ist dies immer erlaubt.
        if (!nextState) {
            return true
        }

        // Check if there is enough remaining treasure to buy the card
        // Remaining = Treasure - Total Selected Cost
        // To buy a new card, the new remaining must be >= 0
        // New Remaining = Remaining - currentPrice
        // So, Remaining - currentPrice >= 0  => Remaining >= currentPrice
        if (remainingValue >= currentPrice) {
            return true
        }

        return false // Nicht genug Geld
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        return true
    }

    override fun canSelectMultiple(): Boolean {
        return true
    }
}