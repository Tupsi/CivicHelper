package org.tesira.civic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CivicViewModel;

import java.util.List;

public class MyItemKeyProvider extends ItemKeyProvider<String> {
    private BuyingListAdapter adapter;

    public MyItemKeyProvider(int scope, BuyingListAdapter adapter) {
        super(scope);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public String getKey(int position) {
        List<Card> itemList = adapter.getItems();
        if (position >= 0 && position < itemList.size()) {
            return itemList.get(position).getName();
        }
        return null;
    }

    @Override
    public int getPosition(@NonNull String key) {
        List<Card> itemList = adapter.getItems(); // Greife auf die Liste vom Adapter zu
        for (int i = 0; i < itemList.size(); i++) {
            if (key.equals(itemList.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
}
