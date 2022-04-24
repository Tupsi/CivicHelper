package org.tesira.mturba.civichelper.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

class CivicRepository {

    private CivilizationAdvanceDao mCivicDao;
    private PurchasedAdvanceDao mPurchaseDao;
    private LiveData<List<CivilizationAdvance>> mAllCivics;
    private LiveData<List<PurchasedAdvance>> mAllPurchases;

    CivicRepository(Application application) {
        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(application);
        mCivicDao = db.civicDao();
        mPurchaseDao = db.purchaseDao();
    }

    LiveData<List<CivilizationAdvance>> getAllCivics() {
        return mAllCivics;
    }

    LiveData<List<PurchasedAdvance>> getAllPurchases() {
        return mAllPurchases;
    }

    void insert(CivilizationAdvance civic) {
        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mCivicDao.insert(civic);});
    }

    void insert(PurchasedAdvance card) {
        CivicHelperDatabase.databaseWriteExecutor.execute(()-> {mPurchaseDao.insert(card);});
    }

}
