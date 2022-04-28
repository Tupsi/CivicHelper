package org.tesira.mturba.civichelper.db;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.tesira.mturba.civichelper.card.CardColor;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CivicViewModel extends AndroidViewModel {

    private CivicRepository mRepository;
    private final LiveData<List<Card>> mAllCivics;
    List<Card> allCards;
    public List<Card> cachedCards;
    private Application application;
    private MutableLiveData<Integer> treasure;
    private MutableLiveData<Integer> total;
    private MutableLiveData<Integer> remaining;

    public MutableLiveData<HashMap<CardColor, Integer>> cardBonus;

    public CivicViewModel(@NonNull Application application) throws ExecutionException, InterruptedException {
        super(application);
        cardBonus = new MutableLiveData<>(new HashMap<>());
        mRepository = new CivicRepository(application);
        mAllCivics = mRepository.getAllCivics();
        allCards = mRepository.getAllCivics().getValue();
        cachedCards = mRepository.getAllCards();
        treasure = new MutableLiveData<>();
        total = new MutableLiveData<>(0);
        remaining = new MutableLiveData<>();
    }
    public CivicRepository getRepository() {return mRepository;}

    public LiveData<List<Purchase>> getAllPurchases() {return mRepository.getAllPurchases();}
    public void insertPurchase(String purchase) {mRepository.insertPurchase(purchase);}
    public void deletePurchases() {
        mRepository.deletePurchases();
        mRepository.resetCurrentPrice();
        cardBonus.setValue(new HashMap<>());
    }

    public LiveData<List<Card>> getAllCivics( String sortingOrder) {return mAllCivics;}

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

    public MutableLiveData<HashMap<CardColor, Integer>> getCardBonus() {
        return cardBonus;
    }

    public void setCardBonus(MutableLiveData<HashMap<CardColor, Integer>> cardBonus) {
        this.cardBonus = cardBonus;
    }

    public void calculateCurrentPrice() {
        for (Card adv: mAllCivics.getValue()) {
            int newCurrent;
            if (adv.getGroup2() == null) {
                newCurrent = adv.getPrice() - cardBonus.getValue().getOrDefault(adv.getGroup1(),0);
                mRepository.updateCurrentPrice(adv.getName(), newCurrent);
            } else {
                int group1 = cardBonus.getValue().getOrDefault(adv.getGroup1(),0);
                int group2 = cardBonus.getValue().getOrDefault(adv.getGroup2(),0);
                newCurrent = adv.getPrice() - Math.max(group1, group2);
                mRepository.updateCurrentPrice(adv.getName(), newCurrent);
            }
        }
    }

    public List<String> getAnatomyCards(){return mRepository.getAnatomyCards();}
}
