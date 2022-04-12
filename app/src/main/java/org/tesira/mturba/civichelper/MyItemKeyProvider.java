package org.tesira.mturba.civichelper;

import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import org.tesira.mturba.civichelper.card.Advance;

import java.util.List;

public class MyItemKeyProvider extends ItemKeyProvider<String> {

    private final List<Advance> itemList;
    private final Adapter adapter;

    public MyItemKeyProvider(int scope, List<Advance> itemList, Adapter adapter) {
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
//        Advance card = (Advance) itemList.stream().filter(advance -> key.equals(advance.toString()));
//        return itemList.indexOf(card);

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
