package org.tesira.civic

import android.view.View
import androidx.lifecycle.LiveData
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.utils.BaseCardListFragment

class InventoryFragment : BaseCardListFragment() {

    override fun getCardsLiveData(): LiveData<List<CardWithDetails>> {
        return civicViewModel.allPurchasedCardsWithDetails
    }

    override fun getButton(): View {
        return binding.btnSortAllCards
    }
}