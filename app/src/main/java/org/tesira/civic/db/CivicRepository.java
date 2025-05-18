package org.tesira.civic.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData; // Import für LiveData hinzufügen
import androidx.lifecycle.MutableLiveData;

import org.tesira.civic.Calamity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
// Import für ExecutorService, falls noch nicht vorhanden
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CivicRepository {

    private CivicHelperDao mCivicDao;

    // Füge LiveData-Variablen für die LiveData-Methoden der DAO hinzu
    // (Hier werden wir uns auf die Methoden konzentrieren, die du in deinem Original-Repo verwendet hast,
    // und die neuen LiveData-Methoden, die wir in der DAO hinzugefügt haben)

    // Beispiele für LiveData-Variablen, die du später im ViewModel nutzen könntest
    // private LiveData<List<Card>> mAllAdvancesNotBoughtLiveData;
    // private LiveData<Integer> mVpLiveData;
    // private LiveData<List<Card>> mAllPurchasesAsCardLiveData;


    // Definiere einen ExecutorService für Hintergrundaufgaben (schreibende Operationen)
    private static final int NUMBER_OF_THREADS = 4; // Beispiel: Anzahl der Threads
    private final ExecutorService repositoryExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);


    public CivicRepository(Application application) {
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();

        // Hier könntest du LiveData-Variablen initialisieren, falls du sie im Repository vorhalten willst.
        // Z.B.:
        // mAllAdvancesNotBoughtLiveData = mCivicDao.getAllAdvancesNotBoughtLiveData("name"); // Beispiel
        // mVpLiveData = mCivicDao.sumVpLiveData(); // Beispiel
        // mAllPurchasesAsCardLiveData = mCivicDao.getPurchasesAsCardLiveData(); // Beispiel
    }

    // *** Schreibende Operationen: Benutze databaseWriteExecutor ***

    // Deine ursprüngliche Methode, jetzt mit Executor
    public void deletePurchases() {
        repositoryExecutor.execute(() -> mCivicDao.deleteAllPurchases());
    }

    // Deine ursprüngliche Methode, jetzt mit Executor
    // Beachte: Deine DAO-Methode insertPurchase erwartet ein Purchase-Objekt.
    // Die Methode im Repository sollte auch ein Purchase-Objekt entgegennehmen.
    // Falls du nur den Namen einfügen möchtest, musst du in dieser Methode ein Purchase-Objekt erstellen.
//    public void insertPurchase(Purchase purchase) {
//        repositoryExecutor.execute(() -> mCivicDao.insertPurchase(purchase));
//    }

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

    // Update-Methoden aus der DAO (die in deinem Original-Repo fehlten) hinzufügen und Executor benutzen
    public void updateIsBuyable(int remaining) {
        repositoryExecutor.execute(() -> mCivicDao.updateIsBuyable(remaining));
    }

    public void updateCurrentPrice(String name, int current) {
        repositoryExecutor.execute(() -> mCivicDao.updateCurrentPrice(name, current));
    }

    public void resetCurrentPrice(){
        repositoryExecutor.execute(() -> mCivicDao.resetCurrentPrice());
    }

    public void updateBonusCard(String name, String newBonusCard) {
        repositoryExecutor.execute(() -> mCivicDao.updateBonusCard(name, newBonusCard));
    }

    public void updateBonus(String name, int newBonus) {
        repositoryExecutor.execute(() -> mCivicDao.updateBonus(name, newBonus));
    }

    public void insertEffect(Effect effect) {
        repositoryExecutor.execute(() -> mCivicDao.insertEffect(effect));
    }

    public void insertSpecialAbility(SpecialAbility ability) {
        repositoryExecutor.execute(() -> mCivicDao.insertSpecialAbility(ability));
    }

    public void insertImmunity(Immunity immunity) {
        repositoryExecutor.execute(() -> mCivicDao.insertImmunity(immunity));
    }

    // *** Leseoperationen: Füge LiveData-Versionen hinzu ***

    // Deine ursprüngliche synchrone Methode
    public Card getAdvanceByNameToCard(String name) {
        return mCivicDao.getAdvanceByNameToCard(name);
    }

    // Neue LiveData-Methode für getAdvanceByNameToCard
    public LiveData<Card> getAdvanceByNameToCardLiveData(String name) {
        return mCivicDao.getAdvanceByNameToCardLiveData(name);
    }

    // Deine ursprüngliche synchrone Methode
    public List<Card> getAllCards() {
        return mCivicDao.getAdvancesByName();
    }

    // Neue LiveData-Methode für getAllCards (entspricht getAdvancesByNameLiveData in DAO)
    public LiveData<List<Card>> getAllCardsLiveData() {
        return mCivicDao.getAdvancesByNameLiveData();
    }


    // Deine ursprüngliche synchrone Methode
    public List<String> getAnatomyCards(){
        return mCivicDao.getAnatomyCards();
    }

    // Neue LiveData-Methode für getAnatomyCards
    public LiveData<List<String>> getAnatomyCardsLiveData(){
        return mCivicDao.getAnatomyCardsLiveData();
    }

    // Deine ursprüngliche synchrone Methode
    public List<Effect> getEffect(String advance, String name) {
        return mCivicDao.getEffect(advance, name);
    }

    // Neue LiveData-Methode für getEffect
    public LiveData<List<Effect>> getEffectLiveData(String advance, String name) {
        return mCivicDao.getEffectLiveData(advance, name);
    }

    // Deine ursprüngliche synchrone Methode
    public List<Card> getPurchasesForBonus() {
        return mCivicDao.getPurchasesForBonus();
    }

    // Neue LiveData-Methode für getPurchasesForBonus
    public LiveData<List<Card>> getPurchasesForBonusLiveData() {
        return mCivicDao.getPurchasesForBonusLiveData();
    }


    // Deine ursprüngliche synchrone Methode
    public List<Card> getAllAdvancesNotBought(String sortingOrder) {
        return mCivicDao.getAllAdvancesNotBought(sortingOrder);
    }

    // Neue LiveData-Methode für getAllAdvancesNotBought
    public LiveData<List<Card>> getAllAdvancesNotBoughtLiveData(String sortingOrder) {
        return mCivicDao.getAllAdvancesNotBoughtLiveData(sortingOrder);
    }

    // Deine ursprüngliche synchrone Methode
    public List<Calamity> getCalamityBonus() {
        return mCivicDao.getCalamityBonus();
    }

    // Neue LiveData-Methode für getCalamityBonus
    public LiveData<List<Calamity>> getCalamityBonusLiveData() {
        return mCivicDao.getCalamityBonusLiveData();
    }


    // Deine ursprüngliche synchrone Methode
    public List<String> getSpecialAbilities() {
        return mCivicDao.getSpecialAbilities();
    }

    // Neue LiveData-Methode für getSpecialAbilities
    public LiveData<List<String>> getSpecialAbilitiesLiveData() {
        return mCivicDao.getSpecialAbilitiesLiveData();
    }


    // Deine ursprüngliche synchrone Methode
    public List<String> getImmunities() {
        return mCivicDao.getImmunities();
    }

    // Neue LiveData-Methode für getImmunities
    public LiveData<List<String>> getImmunitiesLiveData() {
        return mCivicDao.getImmunitiesLiveData();
    }

    // Deine ursprüngliche synchrone Methode
    public List<Card> getAllAdvancesSortedByName() {
        return mCivicDao.getAllAdvancesSortedByName();
    }

    // Neue LiveData-Methode für getAllAdvancesSortedByName
    public LiveData<List<Card>> getAllAdvancesSortedByNameLiveData() {
        return mCivicDao.getAllAdvancesSortedByNameLiveData();
    }

    // Deine ursprüngliche synchrone Methode
    public List<String> getPurchasesAsString() {
        return mCivicDao.getPurchasesAsString();
    }

    // Neue LiveData-Methode für getPurchasesAsString
    public LiveData<List<String>> getPurchasesAsStringLiveData() {
        return mCivicDao.getPurchasesAsStringLiveData();
    }

    // Deine ursprüngliche synchrone Methode
    public List<Card> getPurchasesAsCard() {
        return mCivicDao.getPurchasesAsCard();
    }

    // Neue LiveData-Methode für getPurchasesAsCard
    public LiveData<List<Card>> getPurchasesAsCardLiveData() {
        return mCivicDao.getPurchasesAsCardLiveData();
    }

    // Deine ursprüngliche synchrone Methode
    public int sumVp() {
        return mCivicDao.sumVp();
    }

    // Neue LiveData-Methode für sumVp

    /**
     * Returns the victory points of all bought cards in the database.
     */
    public LiveData<Integer> sumVpCardsLiveData() {
        return mCivicDao.sumVpCardsLiveData();
    }

    // Die asynchronen/Thread-Methoden, die in deinem Original auskommentiert waren,
    // zeigen, dass du bereits in diese Richtung gedacht hast.

    // Die folgenden Methoden waren nicht in deinem Original-Repository,
    // aber basierend auf der DAO könnten sie relevant sein.
    // Füge sie bei Bedarf hinzu (mit Executor für schreibende, LiveData für lesende):

    // public List<Card> getAdvancesByFamily(int familyNumber) { return mCivicDao.getAdvancesByFamily(familyNumber); }
    // public LiveData<List<Card>> getAdvancesByFamilyLiveData(int familyNumber) { return mCivicDao.getAdvancesByFamilyLiveData(familyNumber); }

    // public int getNumberOfCalamities() { return mCivicDao.getNumberOfCalamities(); } // Synchrone Methode
    // public List<Card> getEffects() { return mCivicDao.getEffects(); } // Synchrone Methode
    // public List<Card> getEffect(String effect) { return mCivicDao.getEffect(effect); } // Synchrone Methode
    // public int getNumberOfPurchases() { return mCivicDao.getNumberOfPurchases(); } // Synchrone Methode
    // public int getBonusCardCount(String bonusCard) { return mCivicDao.getBonusCardCount(bonusCard); } // Synchrone Methode

    public void recalculateCurrentPricesAsync(HashMap<CardColor, Integer> currentBonus) {
        repositoryExecutor.execute(() -> {
            Log.d("CivicRepository", "recalculateCurrentPricesAsync: Starting price recalculation.");
            List<Card> allCards = mCivicDao.getAdvancesByName(); // Synchron, da es im Hintergrund-Thread ist
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
                mCivicDao.updateCurrentPrice(adv.getName(), newCurrent);
            }

            // Special family bonus
            List<Card> purchasesForBonus = mCivicDao.getPurchasesForBonus(); // Synchron
            for (Card adv : purchasesForBonus) {
                Card bonusTo = mCivicDao.getAdvanceByNameToCard(adv.getBonusCard()); // Synchron
                if (bonusTo != null) {
                    int newCurrent = bonusTo.getCurrentPrice() - adv.getBonus();
                    if (newCurrent < 0) newCurrent = 0;
                    mCivicDao.updateCurrentPrice(adv.getBonusCard(), newCurrent);
                }
            }
            Log.d("CivicRepository", "recalculateCurrentPricesAsync: Price recalculation finished.");
            // Die LiveData in der DAO wird automatisch aktualisiert und das ViewModel benachrichtigt.
        });
    }


    public void addBonusAndUpdatePricesAsync(String cardName) {
        repositoryExecutor.execute(() -> {
            Log.d("CivicRepository", "addBonusAndUpdatePricesAsync: Adding bonus and updating prices for: " + cardName);
            Card adv = mCivicDao.getAdvanceByNameToCard(cardName); // Synchron
            if (adv != null) {
                // Annahme: updateBonus in DAO aktualisiert die Boni in der Datenbank oder einem persistenten Speicher
                // Wenn die Boni nur im ViewModel gehalten werden, muss diese Logik anders gehandhabt werden.
                // Da cardBonus im ViewModel ist, muss das ViewModel den Bonus aktualisieren.
                // Diese Methode im Repository sollte nur die Preise neu berechnen.

                // Option 1: Bonus wird im ViewModel gehalten (wie aktuell)
                // Diese Methode ist dann nicht im Repository, sondern im ViewModel und aktualisiert die Boni MutableLiveData
                // und triggert dann die Preisberechnung im Repository.

                // Option 2: Bonus wird im Repository oder Datenbank gehalten.
                // Wenn die Boni persistiert werden, kann diese Logik hier sein.
                // Nehmen wir an, die Boni werden im ViewModel gehalten und nur für die Preisberechnung hier übergeben.

                // Beispiel, wenn Boni im ViewModel gehalten werden:
                // Das ViewModel ruft erst updateBonus im ViewModel auf, DANN recalculateCurrentPricesAsync im Repository.
                // Diese Methode im Repository wäre dann nicht nötig in dieser Form.
            }
            Log.d("CivicRepository", "addBonusAndUpdatePricesAsync: Finished.");
        });
    }

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

        // Hole die aktuellen Boni basierend auf den gekauften Karten aus der Datenbank (synchron im Hintergrund-Thread)
//        HashMap<CardColor, Integer> currentBonus = getCalculatedBonusesFromPurchases(); // Neue Methode im Repository

        // Setze alle Preise zurück auf ihren ursprünglichen Wert
        mCivicDao.resetCurrentPrice(); // Synchron im Hintergrund-Thread

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
        // Die LiveData in der DAO wird automatisch aktualisiert.
    }

    private HashMap<CardColor, Integer> getCalculatedBonusesFromPurchases() {
        HashMap<CardColor, Integer> currentBonus = new HashMap<>();
        // TODO: Implementiere hier die Logik, um die Boni aus den gekauften Karten (aus der purchases-Tabelle) zu berechnen.
        // Dies könnte bedeuten, durch alle gekauften Karten zu iterieren
        // und ihre Boni (Credits Blue, Credits Red, etc.) zur HashMap hinzuzufügen.
        // Beispiel (vereinfacht):
        // List<Card> boughtCards = mCivicDao.getPurchasesAsCard(); // Synchron im Hintergrund-Thread
        // for (Card card : boughtCards) {
        //     // Füge Boni basierend auf card.getCreditsBlue(), card.getCreditsRed(), etc. zu currentBonus hinzu
        // }
        // Alternativ: Passe deine DAO an, um die Summe der Boni direkt zu berechnen.
        // Z.B.: @Query("SELECT group1, SUM(creditsBlue) FROM cards LEFT JOIN purchases ... GROUP BY group1")

        // Temporäre Implementierung (ersetze dies durch deine tatsächliche Bonusberechnungslogik):
        currentBonus.put(CardColor.BLUE, 0);
        currentBonus.put(CardColor.RED, 0);
        currentBonus.put(CardColor.YELLOW, 0);
        currentBonus.put(CardColor.GREEN, 0);
        // Fülle currentBonus basierend auf den tatsächlich gekauften Karten und ihren Effekten
        // Du hast bereits getCalamityBonus() in der DAO, das ist ein guter Startpunkt.
        // Passe diese Logik an, um die relevanten Boni (Credits) für die Preisberechnung zu erhalten.


        return currentBonus;
    }

}