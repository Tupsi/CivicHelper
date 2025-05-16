package org.tesira.civic;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.tesira.civic.R;
import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
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

    // Factory-Methode, die die Liste der grünen Karten als Argument akzeptiert
    public static AnatomyDialogFragment newInstance(List<String> greenCardNames) {
        AnatomyDialogFragment fragment = new AnatomyDialogFragment();
        Bundle args = new Bundle();
        // Wichtig: Übergebe eine ArrayList<String>, da Bundle dies direkt unterstützt
        args.putStringArrayList(ARG_GREEN_CARDS_LIST, new ArrayList<>(greenCardNames));
        fragment.setArguments(args);
        fragment.setCancelable(false); // Kann auch hier gesetzt werden
        return fragment;
    }
    public static AnatomyDialogFragment newInstance(CivicViewModel viewModel, List<String> greenCards) {
        // Eine bessere Methode wäre, Argumente über setArguments zu übergeben
        // und ViewModel über shared ViewModel oder Injektion zu erhalten.
        return new AnatomyDialogFragment(viewModel, greenCards);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ViewModel beziehen (empfohlener Weg über Activity/Parent Lifecycle)
        if (getActivity() != null) { // Oder requireActivity(), wenn du sicher bist, dass es nicht null sein kann
            mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        } else {
            Log.d("AnatomyDialogFragment", "Activity is null, cannot get ViewModel.");
            dismissAllowingStateLoss(); // Sicherstellen, dass der Dialog geschlossen wird, wenn das ViewModel fehlt
            return;
        }

        // 2. Argumente (die Liste der grünen Karten) abrufen
        if (getArguments() != null) {
            ArrayList<String> greenCardsList = getArguments().getStringArrayList(ARG_GREEN_CARDS_LIST);
            if (greenCardsList != null && !greenCardsList.isEmpty()) {
                this.greenCardsArray = greenCardsList.toArray(new String[0]);
            } else {
                Log.d("AnatomyDialogFragment", "Green cards list is null or empty in arguments.");
                // Handle den Fall, dass keine Karten übergeben wurden (vielleicht Fehler anzeigen oder Dialog schließen)
                dismissAllowingStateLoss();
                return;
            }
        } else {
            Log.d("AnatomyDialogFragment", "Arguments are null.");
            dismissAllowingStateLoss();
            return;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // mCivicViewModel und greenCardsArray sollten jetzt in onCreate initialisiert sein.
        // Wenn einer von beiden null ist, wurde bereits in onCreate ein dismiss() ausgelöst.

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.dialog_anatomy);
        // Verwende das greenCardsArray, das aus den Argumenten erstellt wurde
        builder.setItems(greenCardsArray, (dialogInterface, which) -> {
            String selectedGreenCard = greenCardsArray[which];

            // ViewModel-Logik ausführen
            mCivicViewModel.addBonus(selectedGreenCard);
            mCivicViewModel.saveBonus(); // Überlege, ob dies hier oder im ViewModel nach erfolgreichem Kauf sein soll
            mCivicViewModel.insertPurchase(selectedGreenCard);
            mCivicViewModel.requestPriceRecalculation();

            // Ergebnis zurücksenden
            Bundle result = new Bundle();
            result.putString("selected_card_name", selectedGreenCard);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss(); // Dialog schließen
        });

        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) { // Null-Prüfung
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return dialog;
    }
}
