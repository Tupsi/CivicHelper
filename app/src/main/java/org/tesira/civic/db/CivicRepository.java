package org.tesira.civic.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tesira.civic.Calamity;

public class CivicRepository {

    private CivicHelperDao mCivicDao;

    private static final int NUMBER_OF_THREADS = 4;
    private final ExecutorService repositoryExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public CivicRepository(Application application) {
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
    }

    public void deletePurchases() {
        repositoryExecutor.execute(() -> mCivicDao.deleteAllPurchases());
    }

    // Methode zum Einfügen eines Kaufs nur mit Namen (wird nur für den extra Anatomy Kauf verwendet)
    public void insertPurchase(String name) {
        repositoryExecutor.execute(() -> {
            mCivicDao.insertPurchase(new Purchase(name));
        });
    }

    public void resetDB() {
        repositoryExecutor.execute(() ->{
            mCivicDao.deleteAllCards();
            mCivicDao.deleteAllEffects();
            mCivicDao.deleteAllSpecials();
            mCivicDao.deleteAllImmunities();
            // Stelle sicher, dass importCivicsFromXML eine synchrone Operation ist
            CivicHelperDatabase.importCivicsFromXML();
        });
    }

    public void resetCurrentPrice(){
        repositoryExecutor.execute(() -> mCivicDao.resetCurrentPrice());
    }
    public LiveData<List<Card>> getAllAdvancesNotBoughtLiveData(String sortingOrder) {
        return mCivicDao.getAllAdvancesNotBoughtLiveData(sortingOrder);
    }
    public LiveData<List<Calamity>> getCalamityBonusLiveData() {
        return mCivicDao.getCalamityBonusLiveData();
    }
    public LiveData<List<String>> getSpecialAbilitiesLiveData() {
        return mCivicDao.getSpecialAbilitiesLiveData();
    }
    public LiveData<List<String>> getImmunitiesLiveData() {
        return mCivicDao.getImmunitiesLiveData();
    }
    public LiveData<List<Card>> getPurchasesAsCardLiveData() {
        return mCivicDao.getPurchasesAsCardLiveData();
    }
    /**
     * Returns the victory points of all bought cards in the database.
     */
    public LiveData<Integer> getCardsVp() {
        return mCivicDao.cardsVp();
    }
    public void recalculateCurrentPricesAsync(MutableLiveData<HashMap<CardColor, Integer>> currentBonus) {
        repositoryExecutor.execute(() -> {
            recalculateCurrentPricesBasedOnPurchases(currentBonus.getValue());
        });
    }

    /**
     * recalculates the color bonus and adds the purchased cards to inventory.
     */
    public void processPurchasesAndRecalculatePricesAsync(List<String> selectedKeys, MutableLiveData<HashMap<CardColor, Integer>> currentBonus, CivicViewModel.PurchaseCompletionCallback callback) {
        repositoryExecutor.execute(() -> {
            Log.d("CivicRepository", "processPurchasesAndRecalculatePricesAsync: Starting processing purchases.");

            int totalExtraCredits = 0;
            boolean boughtAnatomy = false;
            List<String> anatomyCardsToChoose = new ArrayList<>();

            try {
                for (String name : selectedKeys) {
                    // 1. Kauf einfügen
                    mCivicDao.insertPurchase(new Purchase(name)); // Synchron im Hintergrund-Thread

                    // 2. Extra Credits und spezielle Karten prüfen (Synchron im Hintergrund-Thread)
                    List<Effect> creditEffects = mCivicDao.getEffect(name, "Credits");
                    for (Effect effect : creditEffects) {
                        totalExtraCredits += effect.getValue();
                    }

                    if (name.equals("Anatomy")) {
                        boughtAnatomy = true;

                    }
                }
                // 3. Preise neu berechnen nach den Käufen und Boni
                // Boni basieren auf den aktuell in der Datenbank befindlichen Käufen
                recalculateCurrentPricesBasedOnPurchases(currentBonus.getValue());

                // 4. Wenn Anatomy gekauft wurde, alle grünen gratis Karten holen
                if (boughtAnatomy) {
                    anatomyCardsToChoose = mCivicDao.getAnatomyCards();
                }
                // 4. Callback aufrufen, um das ViewModel zu benachrichtigen
                callback.onPurchaseCompleted(totalExtraCredits, anatomyCardsToChoose);

            } catch (Exception e) {
                Log.e("CivicRepository", "Error processing purchases: " + e.getMessage(), e);
                callback.onPurchaseFailed(e.getMessage());
            }
            Log.d("CivicRepository", "processPurchasesAndRecalculatePricesAsync: Finished processing purchases.");
        });

    }
    private void recalculateCurrentPricesBasedOnPurchases(HashMap<CardColor, Integer> currentBonus) {
        Log.d("CivicRepository", "recalculateCurrentPricesBasedOnPurchases: Starting price recalculation.");

        // Aktualisiere die Preise basierend auf den aktuellen Boni
        List<Card> allCards = mCivicDao.getAdvancesByName(); // Synchron im Hintergrund-Thread
        for (Card adv : allCards) {
            int newCurrent;
            if (adv.getGroup2() == null) {
                newCurrent = adv.getPrice() - currentBonus.getOrDefault(adv.getGroup1(), 0);
            } else {
                int group1 = currentBonus.getOrDefault(adv.getGroup1(), 0);
                int group2 = currentBonus.getOrDefault(adv.getGroup2(), 0);
                newCurrent = adv.getPrice() - Math.max(group1, group2);
            }
            if (newCurrent < 0) newCurrent = 0;
            mCivicDao.updateCurrentPrice(adv.getName(), newCurrent); // Synchron im Hintergrund-Thread
        }

        // Special family bonus (prüft gekaufte Karten und aktualisiert Preise basierend auf deren bonusCard und bonus)
        List<Card> purchasesForBonus = mCivicDao.getPurchasesForBonus(); // Synchron im Hintergrund-Thread
        for (Card adv : purchasesForBonus) {
            Card bonusTo = mCivicDao.getAdvanceByNameToCard(adv.getBonusCard()); // Synchron im Hintergrund-Thread
            if (bonusTo != null) {
                int newCurrent = bonusTo.getCurrentPrice() - adv.getBonus();
                if (newCurrent < 0) newCurrent = 0;
                mCivicDao.updateCurrentPrice(bonusTo.getName(), newCurrent); // Synchron im Hintergrund-Thread
            }
        }
        Log.d("CivicRepository", "recalculateCurrentPricesBasedOnPurchases: Price recalculation finished.");
    }

    public interface RepositoryCallback {
        void onComplete();
    }

    public void resetAllCardsHeartStatusAsync(RepositoryCallback callback) {
        repositoryExecutor.execute(() -> {
            mCivicDao.resetAllHearts();
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

    public void setCardsAsHeartAsync(List<String> cardNames, RepositoryCallback callback) {
        if (cardNames == null || cardNames.isEmpty()) {
            if (callback != null) {
                repositoryExecutor.execute(callback::onComplete);
            }
            return;
        }
        repositoryExecutor.execute(() -> {
            mCivicDao.setHeartsForCards(cardNames);
            if (callback != null) {
                callback.onComplete();
            }
        });
    }

}