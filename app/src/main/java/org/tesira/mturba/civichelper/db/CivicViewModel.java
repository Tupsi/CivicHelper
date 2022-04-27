package org.tesira.mturba.civichelper.db;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CivicViewModel extends AndroidViewModel {

    private CivicRepository mRepository;
    private final LiveData<List<Card>> mAllCivics;
    List<Card> allCards;
    public int test;
    public List<Card> cachedCards;
    private Application application;
    private MutableLiveData<Integer> treasure;
    private MutableLiveData<Integer> total;
    private MutableLiveData<Integer> remaining;

//    private final LiveData<List<Purchase>> mAllPurchases;

    public CivicViewModel(@NonNull Application application) throws ExecutionException, InterruptedException {
        super(application);
        mRepository = new CivicRepository(application);
        mAllCivics = mRepository.getAllCivics();
        allCards = mRepository.getAllCivics().getValue();
//        mAllPurchases = mRepository.getAllPurchases();
        test = 5;
        Log.v("MODEL", "inside CivicModel Constructor");
        cachedCards = mRepository.getAllCards();
        treasure = new MutableLiveData<>();
        total = new MutableLiveData<>(0);
        remaining = new MutableLiveData<>();
    }
    public CivicRepository getRepository() {return mRepository;}

    public LiveData<List<Purchase>> getAllPurchases() {return mRepository.getAllPurchases();}
    public void insertPurchase(String purchase) {mRepository.insertPurchase(purchase);}
    public void deletePurchases() {mRepository.deletePurchases();}

    public LiveData<List<Card>> getAllCivics( String sortingOrder) {return mAllCivics;}

//    public void insert(Card civic) {mRepository.insertCard(civic);}

    public Card getAdvanceByName(String name) { return mRepository.getAdvanceByNameToCard(name);}

    public void updateIsBuyable() {
        mRepository.updateIsBuyable(remaining.getValue());
    }

    public MutableLiveData<Integer> getTreasure() {
        return treasure;
    }

    public void setTreasure(int treasure) {
        this.treasure.setValue(treasure);
        this.remaining.setValue(treasure - this.total.getValue());
    }

    public MutableLiveData<Integer> getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total.setValue(total);
        this.remaining.setValue(this.treasure.getValue() - this.total.getValue());
    }

    public MutableLiveData<Integer> getRemaining() {
        return remaining;
    }

    public void setRemaining(MutableLiveData<Integer> remaining) {
        this.remaining = remaining;
    }
}
