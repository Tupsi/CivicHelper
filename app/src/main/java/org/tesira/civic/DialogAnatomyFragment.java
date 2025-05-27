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

import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for the extra card when you buy Anatomy
 * Call the ExtraCreditsDialog if one chooses Written Record
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class DialogAnatomyFragment extends DialogFragment {
    private static final String ARG_GREEN_CARDS_LIST = "arg_green_cards_list";
    private static final String REQUEST_KEY = "anatomySelectionResult";
    private CivicViewModel mCivicViewModel;
    private String[] greenCardsArray;

    public DialogAnatomyFragment() {
    }

    public static DialogAnatomyFragment newInstance(List<String> greenCardNames) {
        DialogAnatomyFragment fragment = new DialogAnatomyFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_GREEN_CARDS_LIST, new ArrayList<>(greenCardNames));
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
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

        if (getActivity() != null) {
            mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        } else {
            Log.d("AnatomyDialogFragment", "Activity is null, cannot get ViewModel.");
            dismissAllowingStateLoss();
            return;
        }

        if (getArguments() != null) {
            ArrayList<String> greenCardsList = getArguments().getStringArrayList(ARG_GREEN_CARDS_LIST);
            if (greenCardsList != null && !greenCardsList.isEmpty()) {
                this.greenCardsArray = greenCardsList.toArray(new String[0]);
            } else {
                Log.d("AnatomyDialogFragment", "Green cards list is null or empty in arguments.");
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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.dialog_anatomy);
        builder.setItems(greenCardsArray, (dialogInterface, which) -> {
            String selectedGreenCard = greenCardsArray[which];

            mCivicViewModel.addBonus(selectedGreenCard);
            mCivicViewModel.insertPurchase(selectedGreenCard);
            mCivicViewModel.requestPriceRecalculation();

            Bundle result = new Bundle();
            result.putString("selected_card_name", selectedGreenCard);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });

        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        return dialog;
    }
}
