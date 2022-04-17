package org.tesira.mturba.civichelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import org.tesira.mturba.civichelper.card.Advance;
import java.util.List;

public class MyItemKeyProvider<S> extends ItemKeyProvider<String> {

    private final List<Advance> itemList;
    private final MyAdvancesRecyclerViewAdapter adapter;

    public MyItemKeyProvider(int scope, List<Advance> itemList, MyAdvancesRecyclerViewAdapter adapter) {
        super(scope);
        this.itemList = itemList;
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public String getKey(int position) {
        return itemList.get(position).getName();
    }

    @Override
    public int getPosition(@NonNull String key) {
        int pos = 0;
        for (Advance adv : itemList) {
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
