package org.tesira.mturba.civichelper;

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

        Advance adv = Advance.getAdvanceFromName(myList, (java.lang.String) key);
        int current = adv.getPrice();
        Integer effect = adv.getEffects().get("CreditsOnce");
        treasure = fragment.getTreasure();
        total = fragment.calculateTotal();
        if (!nextState) {
            if (effect != null) {
                fragment.setTreasure(treasure -= 40);
                fragment.showToast("Library deselected, removing the temporary 40 treasure.");
            }
            return true;
        }

        if (treasure >= total + current) {
            if (effect != null) {
                fragment.setTreasure(treasure += 40);
                fragment.showToast("Library selected, temporary adding 40 treasure.");
            }
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
