package org.tesira.civic

import androidx.lifecycle.LiveData
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.utils.BaseCardListFragment

class InventoryFragment : BaseCardListFragment() {

    override fun getCardsLiveData(): LiveData<List<CardWithDetails>> {
        return civicViewModel.allPurchasedCardsWithDetails
    }
}