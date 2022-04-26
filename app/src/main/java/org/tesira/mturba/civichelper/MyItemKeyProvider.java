package org.tesira.mturba.civichelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.selection.ItemKeyProvider;
import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.db.Card;

import java.util.List;

public class MyItemKeyProvider<S> extends ItemKeyProvider<String> {

//    private final List<Advance> itemList;
    private final LiveData<List<Card>> itemList;

    public MyItemKeyProvider(int scope, LiveData<List<Card>> itemList) {
        super(scope);
        this.itemList = itemList;
    }

    @Nullable
    @Override
    public String getKey(int position) {

        Log.v("MODEL", "inside getKey von MyItemKeyProvider :" + position);
        return itemList.getValue().get(position).getName();
    }

    @Override
    public int getPosition(@NonNull String key) {
        int pos = 0;
        for (Card adv : itemList.getValue()) {
            if (key.equals(adv.getName())) {
                return pos;
            }
            else {
                pos++;
            }
        }
        return -1;
    }
}
