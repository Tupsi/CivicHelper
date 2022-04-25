package org.tesira.mturba.civichelper.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CivicRepository {

    private Application mApplication;
    private CardDao mCivicDao;
//    private PurchasedAdvanceDao mPurchaseDao;
    private LiveData<List<Card>> mAllCivics;
//    private LiveData<List<PurchasedAdvance>> mAllPurchases;

    public CivicRepository(Application application) {
        this.mApplication = application;
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
        mAllCivics = mCivicDao.getAdvancesByPrice();
//        mPurchaseDao = db.purchaseDao();
    }

    public LiveData<List<Card>> getAllCivics() {
        return mAllCivics;
    }

//    public LiveData<List<PurchasedAdvance>> getAllPurchases() {
//        return mAllPurchases;
//    }

    public void insertCard(Card civic) {
        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
    }

//    public void insert(PurchasedAdvance card) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
//    }

}
