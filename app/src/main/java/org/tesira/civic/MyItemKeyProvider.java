package org.tesira.civic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CivicViewModel;

import java.util.List;

public class MyItemKeyProvider<S> extends ItemKeyProvider<String> {

    private List<Card> itemList;
    private final CivicViewModel mCivicViewModel;

    public MyItemKeyProvider(int scope, CivicViewModel model) {
        super(scope);
        this.mCivicViewModel = model;
        itemList = model.cachedCards;
    }

    @Nullable
    @Override
    public String getKey(int position) {
        return itemList.get(position).getName();
    }

    @Override
    public int getPosition(@NonNull String key) {
        int pos = 0;
        for (Card adv : itemList) {
            if (key.equals(adv.getName())) {
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
    }
}
