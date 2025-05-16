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
    private BuyingListAdapter adapter;
    private CivicViewModel mCivicViewModel;

    public MySelectionPredicate(BuyingListAdapter adapter, CivicViewModel model) {
        super();
        this.adapter = adapter;
        this.mCivicViewModel = model;
    }

    @Override
    public boolean canSetStateForKey(@NonNull String key, boolean nextState) {
        // check if there is still enough treasure to buy the new selected card

        List<Card> itemList = adapter.getItems(); // Greife auf die Liste vom Adapter zu
        Card adv = null;
        for (Card card : itemList) {
            if (key.equals(card.getName())) {
                adv = card;
                break;
            }
        }

        if (adv == null) {
            Log.w("MODEL", "Card not found for key: " + key);
            return false; // Kann den Zustand für eine unbekannte Karte nicht setzen
        }

        Log.v("MODEL", " Test if possible to select for key :" + key + " : State: " + nextState);
        int currentPrice = adv.getCurrentPrice();
        Integer treasureValue = mCivicViewModel.getTreasure().getValue();
        Integer remainingValue = mCivicViewModel.getRemaining().getValue();

        if (treasureValue == null || remainingValue == null) {
            // Werte sind noch nicht initialisiert, Auswahl vorerst nicht erlauben
            return false;
        }

        // Wenn die Karte abgewählt wird, ist dies immer erlaubt.
        if (!nextState) {
            return true;
        }

        // Check if there is enough remaining treasure to buy the card
        // Remaining = Treasure - Total Selected Cost
        // To buy a new card, the new remaining must be >= 0
        // New Remaining = Remaining - currentPrice
        // So, Remaining - currentPrice >= 0  => Remaining >= currentPrice

        if (remainingValue >= currentPrice) {
            return true;
        } else {
            // Optional: Zeige eine Toast-Nachricht an, wenn nicht genug Geld da ist.
            // Fragment kann dies übernehmen, indem es die remaining LiveData beobachtet.
        }

        return false; // Nicht genug Geld
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
