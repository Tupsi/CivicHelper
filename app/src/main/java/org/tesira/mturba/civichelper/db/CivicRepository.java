package org.tesira.mturba.civichelper.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CivicRepository {

    private CivilizationAdvanceDao mCivicDao;
//    private PurchasedAdvanceDao mPurchaseDao;
    private LiveData<List<CivilizationAdvance>> mAllCivics;
//    private LiveData<List<PurchasedAdvance>> mAllPurchases;

    public CivicRepository(Application application) {
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
        mAllCivics = mCivicDao.getAdvancesByPrice();
//        mPurchaseDao = db.purchaseDao();
    }

    public LiveData<List<CivilizationAdvance>> getAllCivics() {
        return mAllCivics;
    }

//    public LiveData<List<PurchasedAdvance>> getAllPurchases() {
//        return mAllPurchases;
//    }

    public void insert(CivilizationAdvance civic) {
        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
    }

//    public void insert(PurchasedAdvance card) {
//        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
//    }

}
