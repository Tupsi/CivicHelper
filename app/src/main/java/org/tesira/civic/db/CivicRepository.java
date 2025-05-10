package org.tesira.civic.db;

import android.app.Application;
import android.util.Log;

import org.tesira.civic.Calamity;
import java.util.List;
public class CivicRepository {

    private CivicHelperDao mCivicDao;

    public CivicRepository(Application application) {
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
    }

    public void deletePurchases() {mCivicDao.deleteAllPurchases();}
    public void insertPurchase(String name) {
         CivicHelperDatabase.databaseWriteExecutor.execute(() -> mCivicDao.insertPurchase(new Purchase(name)));
    }


    public Card getAdvanceByNameToCard(String name) {
        return mCivicDao.getAdvanceByNameToCard(name);
    }

// Asynch, needed if DB is created without .allowMainThreadQueries()
//    public Card getAdvanceByNameAsync(String name) throws ExecutionException, InterruptedException {
//
//        Callable<Card> callable = () -> mCivicDao.getAdvanceByNameToCard(name);
//        Future<Card> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
//        return future.get();
//    }

// Examples of I need to switch to asynch calls
//    public void insertCard(Card civic) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
//    }
//    public void insert(Purchase card) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
//    }


//    public List<Card> getAllCards() throws ExecutionException, InterruptedException {
//        Callable<List<Card>> callable = () -> mCivicDao.getAdvancesByName();
//        Future<List<Card>> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
//        return future.get();
//    }

    public List<Card> getAllCards() {return mCivicDao.getAdvancesByName();}
    public void updateIsBuyable(int remaining) {
        mCivicDao.updateIsBuyable(remaining);
    }
    public void updateCurrentPrice(String name, int current) { mCivicDao.updateCurrentPrice(name, current);}
    public void resetCurrentPrice(){mCivicDao.resetCurrentPrice();}
    public List<String> getAnatomyCards(){ return mCivicDao.getAnatomyCards();}
    public List<Effect> getEffect(String advance, String name) {return mCivicDao.getEffect(advance, name);}
    public List<Card> getPurchasesForBonus() {return mCivicDao.getPurchasesForBonus();}

    public List<Card> getAllAdvancesNotBought(String sortingOrder) {
        return mCivicDao.getAllAdvancesNotBought(sortingOrder);}
    public List<Calamity> getCalamityBonus() {return mCivicDao.getCalamityBonus();}
    public List<String> getSpecialAbilities() {return mCivicDao.getSpecialAbilities();}
    public List<String> getImmunities() {return mCivicDao.getImmunities();}
    public List<Card> getAllAdvancesSortedByName() {return mCivicDao.getAllAdvancesSortedByName();}
    public List<String> getPurchasesAsString() {return mCivicDao.getPurchasesAsString();}
    public List<Card> getPurchasesAsCard() {return mCivicDao.getPurchasesAsCard();}
    public int sumVp() {return mCivicDao.sumVp();}

    public void resetDB() {
        CivicHelperDatabase.databaseWriteExecutor.execute(() ->{
            mCivicDao.deleteAllCards();
            mCivicDao.deleteAllEffects();
            mCivicDao.deleteAllSpecials();
            mCivicDao.deleteAllImmunities();
            CivicHelperDatabase.importCivicsFromXML();
        });
    }
}
