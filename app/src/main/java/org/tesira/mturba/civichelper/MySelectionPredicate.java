package org.tesira.mturba.civichelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

import org.tesira.mturba.civichelper.card.Advance;

import java.util.List;

public class MySelectionPredicate<String> extends SelectionTracker.SelectionPredicate<String> {

    private final List<Advance> myList;
    private AdvancesFragment fragment;
    private Integer treasure, total;

    public MySelectionPredicate(AdvancesFragment advancesFragment, List<Advance> myList) {
        super();
        this.fragment = advancesFragment;
        this.myList = myList;
    }

    @Override
    public boolean canSetStateForKey(@NonNull String key, boolean nextState) {
        if (!nextState) return true;
        int idx = myList.get(0).getIndexFromName(myList, (java.lang.String) key);
        int current = myList.get(idx).getPrice();
        treasure = fragment.getTreasure();
        total = fragment.calculateTotal();
        if (treasure >= total + current) {
            fragment.setTotal(total + current);
            return true;
        }
        return false;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        return true;
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}
