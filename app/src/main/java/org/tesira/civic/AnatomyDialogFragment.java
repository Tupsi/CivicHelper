package org.tesira.civic;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.tesira.civic.R;
import org.tesira.civic.db.CivicViewModel;

import java.util.List;

/**
 * Dialog for the extra card when you buy Anatomy
 * Call the ExtraCreditsDialog if one chooses Written Record
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class AnatomyDialogFragment extends DialogFragment {

    private static final String ARG_GREEN_CARDS_LIST = "arg_green_cards_list";
    private static final String REQUEST_KEY = "anatomySelectionResult";
    private String[] greenCards;
    private CivicViewModel mCivicViewModel;
    private String[] greenCardsArray;

    public AnatomyDialogFragment() {
    }
    public AnatomyDialogFragment(CivicViewModel model, List<String> greenCards) {
            setCancelable(false);
            this.mCivicViewModel = model;
            this.greenCards = greenCards.toArray(new String[0]);
    }
    // Factory-Methode (empfohlen)
    public static AnatomyDialogFragment newInstance(CivicViewModel viewModel, List<String> greenCards) {
        // Eine bessere Methode wäre, Argumente über setArguments zu übergeben
        // und ViewModel über shared ViewModel oder Injektion zu erhalten.
        return new AnatomyDialogFragment(viewModel, greenCards);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_anatomy);
        builder.setItems(greenCards, (dialogInterface, which) -> { // Ändere den Parameternamen, um Verwirrung zu vermeiden
            String selectedGreenCard = greenCards[which]; // Die vom Benutzer ausgewählte Karte

            // Führe die ViewModel-Logik für den Kauf und Bonus aus
            mCivicViewModel.addBonus(selectedGreenCard);
            mCivicViewModel.saveBonus();
            mCivicViewModel.insertPurchase(selectedGreenCard);
            mCivicViewModel.requestPriceRecalculation();
            // Sende das Ergebnis zurück an das aufrufende Fragment (AdvancesFragment)
            Bundle result = new Bundle();
            result.putString("selected_card_name", selectedGreenCard); // Sende den Namen der ausgewählten Karte
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        return dialog;
    }

}
