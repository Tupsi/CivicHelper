package org.tesira.civic

import androidx.lifecycle.LiveData
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.utils.BaseCardListFragment

class AllCardsFragment : BaseCardListFragment() {

    override fun getCardsLiveData(): LiveData<List<CardWithDetails>> {
        return civicViewModel.allCardsWithDetails
    }
}