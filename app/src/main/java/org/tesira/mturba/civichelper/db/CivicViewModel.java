package org.tesira.mturba.civichelper.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class CivicViewModel extends AndroidViewModel {

    private CivicRepository mRepository;
    private final LiveData<List<CivilizationAdvance>> mAllCivics;
    private final LiveData<List<PurchasedAdvance>> mAllPurchases;

    public CivicViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CivicRepository(application);
        mAllCivics = mRepository.getAllCivics();
        mAllPurchases = mRepository.getAllPurchases();
    }

    LiveData<List<CivilizationAdvance>> getAllCivics() {return mAllCivics;}
    LiveData<List<PurchasedAdvance>> getAllPurchases() {return mAllPurchases;}

    public void insert(CivilizationAdvance civic) {mRepository.insert(civic);}
    public void insert(PurchasedAdvance purchases) {mRepository.insert(purchases);}
}
