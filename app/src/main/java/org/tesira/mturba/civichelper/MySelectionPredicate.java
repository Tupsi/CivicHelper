package org.tesira.mturba.civichelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;

public class MySelectionPredicate<String> extends SelectionTracker.SelectionPredicate<String> {

    private AdvancesFragment fragment;
    private int treasure, total;
    private CivicViewModel mCivicViewModel;

    public MySelectionPredicate(AdvancesFragment advancesFragment, CivicViewModel model) {
        super();
        this.mCivicViewModel = model;
        this.fragment = advancesFragment;
    }

    @Override
    public boolean canSetStateForKey(@NonNull String key, boolean nextState) {
        // check if there is still enough treasure to buy the new selected card

        Card adv = mCivicViewModel.getAdvanceByName((java.lang.String) key);
        Log.v("Model", "key :" +key);
        int current = adv.getPrice();
        treasure = mCivicViewModel.getTreasure().getValue();
        total = mCivicViewModel.getTotal().getValue();

//        Integer effect = adv.getEffects().get("CreditsOnce");
//        Integer effect = null;
        if (!nextState) {
//            if (effect != null) {
//                mCivicViewModel.setTreasure(treasure -= 40);
//                fragment.showToast("Library deselected, removing the temporary 40 treasure.");
//            }
            return true;
        }

        if (treasure >= total + current) {
//            if (effect != null) {
//                mCivicViewModel.setTreasure(treasure += 40);
//                fragment.showToast("Library selected, temporary adding 40 treasure.");
//            }
            mCivicViewModel.setTotal(total + current);
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
