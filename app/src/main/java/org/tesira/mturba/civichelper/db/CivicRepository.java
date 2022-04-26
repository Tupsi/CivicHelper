package org.tesira.mturba.civichelper.db;

import android.app.Application;
import android.service.autofill.UserData;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CivicRepository {

    private Application mApplication;
    private CardDao mCivicDao;
//    private PurchasedAdvanceDao mPurchaseDao;
    private LiveData<List<Card>> mAllCivics;
//    private LiveData<List<PurchasedAdvance>> mAllPurchases;
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

//        mPurchaseDao = db.purchaseDao();
    }

    public Card getAdvanceByNameToCard(String name) {

        return mCivicDao.getAdvanceByNameToCard(name);
    }

    // Asynch, needed if DB is created without .allowMainThreadQueries()
    public Card getAdvanceByNameAsync(String name) throws ExecutionException, InterruptedException {

        Callable<Card> callable = new Callable<Card>() {
            @Override
            public Card call() throws Exception {
                return mCivicDao.getAdvanceByNameToCard(name);
            }
        };
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

//    public LiveData<List<PurchasedAdvance>> getAllPurchases() {
//        return mAllPurchases;
//    }

    public void insertCard(Card civic) {
        Log.v("MODEL","vor Executor");
        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
        Log.v("MODEL","nach Executor");
    }

//    public void insert(PurchasedAdvance card) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
//    }


    public List<Card> getAllCards() throws ExecutionException, InterruptedException {

        Callable<List<Card>> callable = new Callable<List<Card>>() {
            @Override
            public List<Card> call() throws Exception {
                return mCivicDao.getAdvancesByName();
            }
        };
        Future<List<Card>> future = CivicHelperDatabase.databaseWriteExecutor.submit(callable);
        return future.get();
    }

    public void updateIsBuyable(int remaining) {
        mCivicDao.updateIsBuyable(remaining);
    }

}
