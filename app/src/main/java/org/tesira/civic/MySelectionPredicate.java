package org.tesira.civic;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;

import org.tesira.civic.db.Card;
import org.tesira.civic.db.CivicViewModel;
import org.tesira.civic.db.Effect;

import java.util.List;

/**
 * SelectionPredicate to check if there is still enough treasure to select the clicked row
 * in the list of the advances fragment.
 * @param <String> Name of the civilization advance.
 */
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
        Log.v("MODEL", " Test if possible to select for key :" + key + " : State: " + nextState);
        int current = adv.getCurrentPrice();
        treasure = mCivicViewModel.getTreasure().getValue();
        total = treasure - mCivicViewModel.getRemaining().getValue();

        // check for library effect which gives you +40 treasure this round
        List<Effect> effect = mCivicViewModel.getEffect(adv.getName(), "CreditsOnce");
        if (!nextState) {
            return true;
        }

        if (treasure >= total + current) {
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
