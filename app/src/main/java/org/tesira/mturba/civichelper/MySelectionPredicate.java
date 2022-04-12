package org.tesira.mturba.civichelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

import org.tesira.mturba.civichelper.card.Advance;

import java.util.List;

public class MySelectionPredicate<Long> extends SelectionTracker.SelectionPredicate<Long> {

    private final List<Advance> myList;
    private AdvancesFragment fragment;
    private Integer treasure, total;

    public MySelectionPredicate(AdvancesFragment advancesFragment, List<Advance> myList) {
        this.fragment = advancesFragment;
        this.myList = myList;
    }

    @Override
    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
        if (!nextState) return true;
        int idx = Math.toIntExact((java.lang.Long) key);
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
        Log.v("INFO", "Predicate canSetStatePosition : " + position + " : " + nextState);
        return true;
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}
