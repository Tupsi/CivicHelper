package org.tesira.mturba.civichelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.selection.ItemKeyProvider;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.List;

public class MyItemKeyProvider<S> extends ItemKeyProvider<String> {

    private List<Card> itemList;
    private final CivicViewModel mCivicViewModel;

    public MyItemKeyProvider(int scope, List<Card> list, CivicViewModel model) {
        super(scope);
//        this.itemList = itemList;
        this.mCivicViewModel = model;
        itemList = model.cachedCards;
        mCivicViewModel.getAllCivics("name").observeForever(new Observer<List<Card>>() {
            @Override
            public void onChanged(List<Card> cards) {
                itemList = cards;
            }
        });
        Log.v("TAG44", "size of itemList in MyItemKeyProv :" + itemList.size());
        Log.v("TAG44", "1.Karte itemList in MyItemKeyProv :" + itemList.get(0).getName());

    }

    @Nullable
    @Override
    public String getKey(int position) {

        Log.v("MODEL", "inside getKey von MyItemKeyProvider :" + position);
        return itemList.get(position).getName();
    }

    @Override
    public int getPosition(@NonNull String key) {
        Log.v("MODEL", "inside getPosition von MyItemKeyProvider :" + key);
        int pos = 0;
        for (Card adv : itemList) {
            if (key.equals(adv.getName())) {
                Log.v("MODEL", "inside getPosition von MyItemKeyProvider Position :" + pos);
                return pos;
            }
            else {
                pos++;
            }
        }
        return -1;
    }

    public void setItemList(List<Card> itemList) {
        this.itemList = itemList;
        Log.v("TAG44", "1.Karte itemList in setItemList :" + this.itemList.get(0).getName());

    }
}
