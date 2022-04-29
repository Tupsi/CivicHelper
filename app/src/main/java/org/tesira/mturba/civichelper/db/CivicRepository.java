package org.tesira.mturba.civichelper.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CivicRepository {

    private Application mApplication;
    private CivicHelperDao mCivicDao;
    private LiveData<List<Card>> mAllCivics;
    private LiveData<List<Purchase>> mAllPurchases;
    private List<Card> cachedCards;

    public CivicRepository(Application application) {
        this.mApplication = application;
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
        mAllCivics = mCivicDao.getAdvancesByPrice();
        CivicHelperDatabase.databaseWriteExecutor.execute(() -> {
            Log.v("DB", "creating");
            cachedCards = mCivicDao.getAdvancesByName();
        });
    }

    public void deletePurchases() {mCivicDao.deleteAllPurchases();}
    public LiveData<List<Purchase>> getAllPurchases() { return mCivicDao.getPurchases(); }
    public void insertPurchase(String name) {

         CivicHelperDatabase.databaseWriteExecutor.execute(() -> {
            // Populate the database in the background.
            // If you want to start with more words, just add them.
             mCivicDao.insertPurchase(new Purchase(name));
            Log.v("BUY", "writing in executur :" + name);
        });
    }


    public Card getAdvanceByNameToCard(String name) {

        return mCivicDao.getAdvanceByNameToCard(name);
    }

    // Asynch, needed if DB is created without .allowMainThreadQueries()
    public Card getAdvanceByNameAsync(String name) throws ExecutionException, InterruptedException {

        Callable<Card> callable = () -> mCivicDao.getAdvanceByNameToCard(name);
        Future<Card> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }

    public List<Card> getCachedCards() { return cachedCards;}

    public LiveData<List<Card>> getAdvanceByName(String name) {
        return mCivicDao.getAdvanceByName(name);
    }

    public LiveData<List<Card>> getAllCivics() {
        return mAllCivics;
    }

// Examples of I need to switch to asynch calls
//    public void insertCard(Card civic) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
//    }
//    public void insert(Purchase card) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
//    }


    public List<Card> getAllCards() throws ExecutionException, InterruptedException {

        Callable<List<Card>> callable = () -> mCivicDao.getAdvancesByName();
        Future<List<Card>> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }
    public List<Card> getAllCardsForFree() throws ExecutionException, InterruptedException {

        Callable<List<Card>> callable = () -> mCivicDao.getAdvancesForFree();
        Future<List<Card>> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }

    public void updateIsBuyable(int remaining) {
        mCivicDao.updateIsBuyable(remaining);
    }
    public void updateCurrentPrice(String name, int current) { mCivicDao.updateCurrentPrice(name, current);}
    public void resetCurrentPrice(){mCivicDao.resetCurrentPrice();}
    public List<String> getAnatomyCards(){ return mCivicDao.getAnatomyCards();}
    public List<Effect> getEffect(String advance, String name) {return mCivicDao.getEffect(advance, name);}
    public List<Card> getPurchasesForBonus() {return mCivicDao.getPurchasesForBonus();}
    public List<Card> getAdvancesForFree() {return mCivicDao.getAdvancesForFree();}
    public LiveData<List<Card>> getAdvancesLive(String order) {return mCivicDao.getAdvancesLive(order);}
}
