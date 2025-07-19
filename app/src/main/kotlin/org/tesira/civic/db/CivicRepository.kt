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
    private var civicDao: CivicHelperDao

    private val repositoryExecutor: ExecutorService = Executors.newFixedThreadPool(
        NUMBER_OF_THREADS
    )

    init {
        val db = getDatabase(application)
        civicDao = db!!.civicDao()
    }

    fun deleteInventory() {
        repositoryExecutor.execute { civicDao.deleteAllPurchases() }
    }

    // Methode zum Einfügen eines Kaufs nur mit Namen (wird nur für den extra Anatomy Kauf verwendet)
    fun insertPurchase(name: String) {
        repositoryExecutor.execute { civicDao.insertPurchase(Purchase(name)) }
    }

    fun resetDB(context: Context) {
        repositoryExecutor.execute {
            civicDao.deleteAllCards()
            civicDao.deleteAllEffects()
            civicDao.deleteAllSpecials()
            civicDao.deleteAllImmunities()
            // Stelle sicher, dass importCivicsFromXML eine synchrone Operation ist
            importCivicsFromXML(context)
        }
    }

    fun resetCurrentPrice() {
        repositoryExecutor.execute { civicDao.resetCurrentPrice() }
    }

    fun getAllAdvancesNotBoughtLiveData(sortingOrder: String): LiveData<List<Card>> {
        return civicDao.getAllAdvancesNotBoughtLiveData(sortingOrder)
    }

    fun getAllCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>> {
        return civicDao.getAllCardsWithDetailsUnsorted()
    }

    fun getAllPurchasedCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>> {
        return civicDao.getAllPurchasedCardsWithDetailsUnsorted()
    }

    fun getAllPurchasableCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>> {
        return civicDao.getAllPurchasableCardsWithDetailsUnsorted()
    }

    val calamityBonusLiveData: LiveData<List<Calamity>>
        get() = civicDao.getCalamityBonusLiveData()
    val specialAbilitiesLiveData: LiveData<List<String>>
        get() = civicDao.getSpecialAbilitiesLiveData()
    val immunitiesLiveData: LiveData<List<String>>
        get() = civicDao.getImmunitiesLiveData()
    val inventoryAsCardLiveData: LiveData<List<Card>>
        get() = civicDao.getInventoryAsCardLiveData()
    val cardsVp: LiveData<Int>
        /**
         * Returns the victory points of all bought cards in the database.
         */
        get() = civicDao.cardsVp()

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
            var anatomyCardsToChoose: List<String> = ArrayList()

            try {
                for (name in selectedKeys) {
                    // 1. Kauf einfügen
                    civicDao.insertPurchase(Purchase(name)) // Synchron im Hintergrund-Thread

                    // 2. Extra Credits und spezielle Karten prüfen (Synchron im Hintergrund-Thread)
                    val creditEffects =
                        civicDao.getEffect(name, "Credits")
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
                    anatomyCardsToChoose = civicDao.getAnatomyCards()
                }
                // 4. Callback aufrufen, um das ViewModel zu benachrichtigen
                callback.onPurchaseCompleted(totalExtraCredits, anatomyCardsToChoose)
            } catch (e: Exception) {
                Log.e("CivicRepository", "Error processing purchases: " + e.message, e)
                callback.onPurchaseFailed(e.message!!)
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
        var allCards = civicDao.getAdvancesByName() // Synchron im Hintergrund-Thread
        for (cardLoop in allCards) {
            var newCurrent: Int
            if (cardLoop.group2 == null) {
                newCurrent = cardLoop.price - currentBonus.getOrDefault(cardLoop.group1, 0)
            } else {
                val group1 = currentBonus.getOrDefault(cardLoop.group1, 0)
                val group2 = currentBonus.getOrDefault(cardLoop.group2, 0)
                newCurrent = (cardLoop.price - max(group1.toDouble(), group2.toDouble())).toInt()
            }
            if (newCurrent < 0) newCurrent = 0
            civicDao.updateCurrentPrice(cardLoop.name, newCurrent)
        }

        // Special family bonus (prüft gekaufte Karten und aktualisiert Preise basierend auf deren bonusCard und bonus)
        // muss nur für Karten gemacht werden die 1 oder 3 VP geben. Karten mit 6 VP haben so einen Bonus nicht
        val purchasesForBonus = civicDao.getPurchasesForFamilyBonus()
        allCards = civicDao.getAdvancesByName()
        for (cardLoop in purchasesForBonus) {
            val bonusTo = allCards.find { it.name == cardLoop.bonusCard }
            var newCurrent = bonusTo!!.currentPrice - cardLoop.bonus
            if (newCurrent < 0) newCurrent = 0
            civicDao.updateCurrentPrice(bonusTo.name, newCurrent)
        }
        Log.d(
            "CivicRepository",
            "recalculateCurrentPricesBasedOnPurchases: Price recalculation finished."
        )
    }

    fun interface RepositoryCallback {
        fun onComplete()
    }

    fun resetAllCardsHeartStatusAsync(callback: RepositoryCallback?) {
        repositoryExecutor.execute {
            civicDao.resetAllHearts()
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
            civicDao.setHeartsForCards(cardNames)
            callback?.onComplete()
        }
    }

    fun insertCard(card: Card) {
        repositoryExecutor.execute { civicDao.insertCard(card) }
    }

    suspend fun deletePurchase(cardName: String) {
        civicDao.deletePurchase(cardName)
    }

    suspend fun deleteCardByName(cardName: String) {
        civicDao.deleteCardByName(cardName)
    }

    suspend fun getCardByName(cardName: String): Card? {
        return civicDao.getCardByName(cardName)
    }

    companion object {
        private const val NUMBER_OF_THREADS = 4
    }
}