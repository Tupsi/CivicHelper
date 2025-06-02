package org.tesira.civic.db

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.tesira.civic.Calamity
import org.tesira.civic.db.CivicHelperDatabase.Companion.getDatabase
import org.tesira.civic.db.CivicHelperDatabase.Companion.importCivicsFromXML
import org.tesira.civic.db.CivicViewModel.PurchaseCompletionCallback
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max

class CivicRepository(application: Application) {
    private var mCivicDao: CivicHelperDao

    private val repositoryExecutor: ExecutorService = Executors.newFixedThreadPool(
        NUMBER_OF_THREADS
    )

    init {
        val db = getDatabase(application)
        mCivicDao = db!!.civicDao()
    }

    fun deleteInventory() {
        repositoryExecutor.execute { mCivicDao.deleteAllPurchases() }
    }

    // Methode zum Einfügen eines Kaufs nur mit Namen (wird nur für den extra Anatomy Kauf verwendet)
    fun insertPurchase(name: String) {
        repositoryExecutor.execute { mCivicDao.insertPurchase(Purchase(name)) }
    }

    fun resetDB(context: Context) {
        repositoryExecutor.execute {
            mCivicDao.deleteAllCards()
            mCivicDao.deleteAllEffects()
            mCivicDao.deleteAllSpecials()
            mCivicDao.deleteAllImmunities()
            // Stelle sicher, dass importCivicsFromXML eine synchrone Operation ist
            importCivicsFromXML(context)
        }
    }

    fun resetCurrentPrice() {
        repositoryExecutor.execute { mCivicDao.resetCurrentPrice() }
    }

    fun getAllAdvancesNotBoughtLiveData(sortingOrder: String): LiveData<List<Card>> {
        return mCivicDao.getAllAdvancesNotBoughtLiveData(sortingOrder)
    }

    val calamityBonusLiveData: LiveData<List<Calamity>>
        get() = mCivicDao.getCalamityBonusLiveData()
    val specialAbilitiesLiveData: LiveData<List<String>>
        get() = mCivicDao.getSpecialAbilitiesLiveData()
    val immunitiesLiveData: LiveData<List<String>>
        get() = mCivicDao.getImmunitiesLiveData()
    val inventoryAsCardLiveData: LiveData<List<Card>>
        get() = mCivicDao.getInventoryAsCardLiveData()
    val cardsVp: LiveData<Int>
        /**
         * Returns the victory points of all bought cards in the database.
         */
        get() = mCivicDao.cardsVp()

    fun recalculateCurrentPricesAsync(currentBonus: MutableLiveData<HashMap<CardColor, Int>>) {
        repositoryExecutor.execute {
            currentBonus.value?.let { recalculateCurrentPricesBasedOnInventory(it) }
        }
    }

    /**
     * recalculates the color bonus and adds the purchased cards to inventory.
     */
    fun processPurchasesAndRecalculatePricesAsync(
        selectedKeys: List<String>,
        currentBonus: MutableLiveData<HashMap<CardColor, Int>>,
        callback: PurchaseCompletionCallback
    ) {
        repositoryExecutor.execute {
            Log.d(
                "CivicRepository",
                "processPurchasesAndRecalculatePricesAsync: Starting processing purchases."
            )
            var totalExtraCredits = 0
            var boughtAnatomy = false
            var anatomyCardsToChoose: List<String> =
                ArrayList()

            try {
                for (name in selectedKeys) {
                    // 1. Kauf einfügen
                    mCivicDao.insertPurchase(Purchase(name)) // Synchron im Hintergrund-Thread

                    // 2. Extra Credits und spezielle Karten prüfen (Synchron im Hintergrund-Thread)
                    val creditEffects =
                        mCivicDao.getEffect(name, "Credits")
                    for (effect in creditEffects) {
                        totalExtraCredits += effect.value
                    }

                    if (name == "Anatomy") {
                        boughtAnatomy = true
                    }
                }
                // 3. Preise neu berechnen nach den Käufen und Boni
                // Boni basieren auf den aktuell in der Datenbank befindlichen Käufen
                recalculateCurrentPricesBasedOnInventory(currentBonus.value!!)

                // 4. Wenn Anatomy gekauft wurde, alle grünen gratis Karten holen
                if (boughtAnatomy) {
                    anatomyCardsToChoose = mCivicDao.getAnatomyCards()
                }
                // 4. Callback aufrufen, um das ViewModel zu benachrichtigen
                callback.onPurchaseCompleted(totalExtraCredits, anatomyCardsToChoose)
            } catch (e: Exception) {
                Log.e("CivicRepository", "Error processing purchases: " + e.message, e)
                callback.onPurchaseFailed(e.message)
            }
            Log.d(
                "CivicRepository",
                "processPurchasesAndRecalculatePricesAsync: Finished processing purchases."
            )
        }
    }

    private fun recalculateCurrentPricesBasedOnInventory(currentBonus: HashMap<CardColor, Int>) {
        Log.d(
            "CivicRepository",
            "recalculateCurrentPricesBasedOnPurchases: Starting price recalculation."
        )

        // Aktualisiere die Preise basierend auf den aktuellen Boni
        val allCards = mCivicDao.getAdvancesByName() // Synchron im Hintergrund-Thread
        for (card_loop in allCards) {
            var newCurrent: Int
            if (card_loop.group2 == null) {
                newCurrent = card_loop.price - currentBonus.getOrDefault(card_loop.group1, 0)
            } else {
                val group1 = currentBonus.getOrDefault(card_loop.group1, 0)
                val group2 = currentBonus.getOrDefault(card_loop.group2, 0)
                newCurrent = (card_loop.price - max(group1.toDouble(), group2.toDouble())).toInt()
            }
            if (newCurrent < 0) newCurrent = 0
            mCivicDao.updateCurrentPrice(card_loop.name, newCurrent)
        }

        // Special family bonus (prüft gekaufte Karten und aktualisiert Preise basierend auf deren bonusCard und bonus)
        // muss nur für Karten gemacht werden die 1 oder 3 VP geben. Karten mit 6 VP haben so einen Bonus nicht
        val purchasesForBonus = mCivicDao.getPurchasesForFamilyBonus()
        for (card_loop in purchasesForBonus) {
            val bonusTo = mCivicDao.getAdvanceByNameToCard(card_loop.bonusCard!!)
            var newCurrent = bonusTo.currentPrice - card_loop.bonus
            if (newCurrent < 0) newCurrent = 0
            mCivicDao.updateCurrentPrice(bonusTo.name, newCurrent)
        }
        Log.d(
            "CivicRepository",
            "recalculateCurrentPricesBasedOnPurchases: Price recalculation finished."
        )
    }

    interface RepositoryCallback {
        fun onComplete()
    }

    fun resetAllCardsHeartStatusAsync(callback: RepositoryCallback?) {
        repositoryExecutor.execute {
            mCivicDao.resetAllHearts()
            callback?.onComplete()
        }
    }

    fun setCardsAsHeartAsync(cardNames: List<String>, callback: RepositoryCallback?) {
        if (cardNames.isEmpty()) {
            if (callback != null) {
                repositoryExecutor.execute { callback.onComplete() }
            }
            return
        }
        repositoryExecutor.execute {
            mCivicDao.setHeartsForCards(cardNames)
            callback?.onComplete()
        }
    }

    companion object {
        private const val NUMBER_OF_THREADS = 4
    }
}