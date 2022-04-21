package org.tesira.mturba.civichelper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.Set;

/**
 * Dialog for the extra card when you buy Anatomy
 * Call the ExtraCreditsDialog if one chooses Written Record
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class AnatomyDialogFragment extends DialogFragment {

        private AdvancesFragment fragment;
        private String[] greenCards;

        public AnatomyDialogFragment(AdvancesFragment fragment, Set<String> greenCards) {
            this.fragment = fragment;
            this.greenCards = greenCards.toArray(new String[0]);
            Arrays.sort(this.greenCards);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_anatomy);
            builder.setItems(greenCards, (dialog, which) -> {
                fragment.addAnatomyFreeCard(greenCards[which]);
                fragment.returnToDashboard(greenCards[which].equals("Written Record"));
            });
            return builder.create();
        }

}
