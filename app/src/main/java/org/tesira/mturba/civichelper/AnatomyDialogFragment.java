package org.tesira.mturba.civichelper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Dialog for the extra card when you buy Anatomy
 * Call the ExtraCreditsDialog if one chooses Written Record
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class AnatomyDialogFragment extends DialogFragment {

        private AdvancesFragment fragment;
        private String[] greenCards;
        private CivicViewModel mCivicViewModel;

        public AnatomyDialogFragment(CivicViewModel model,AdvancesFragment fragment, List<String> greenCards) {
            this.mCivicViewModel = model;
            this.fragment = fragment;
            this.greenCards = greenCards.toArray(new String[0]);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_anatomy);
            builder.setItems(greenCards, (dialog, which) -> {
                mCivicViewModel.insertPurchase(greenCards[which]);
                mCivicViewModel.addBonus(greenCards[which]);
//                fragment.addAnatomyFreeCard(greenCards[which]);
                fragment.returnToDashboard(greenCards[which].equals("Written Record"));
            });
            return builder.create();
        }

}
