package org.tesira.mturba.civichelper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.Set;

public class AnatomyDialogFragment extends DialogFragment {

        private AdvancesFragment fragment;
        private String[] greenCards;

        public AnatomyDialogFragment(AdvancesFragment fragment, Set<String> greenCards) {
            this.fragment = fragment;
            Log.v("Anatomy", ""+greenCards.size());
            this.greenCards = greenCards.toArray(new String[greenCards.size()]);
            Arrays.sort(this.greenCards);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_anatomy);
//            Log.v("Anatomy", "size : " + greenCards.length);
            builder.setItems(greenCards, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    Log.v("Anatomy", "clicked: " + greenCards[which]);
                    fragment.addAnatomyFreeCard(greenCards[which]);
                    fragment.returnToDashboard();
                }
            });
            return builder.create();
        }

}
