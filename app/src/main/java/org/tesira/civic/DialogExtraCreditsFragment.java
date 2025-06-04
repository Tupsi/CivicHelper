package org.tesira.civic;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import org.tesira.civic.databinding.DialogCreditsBinding;
import org.tesira.civic.db.CivicViewModel;

/**
 * Dialog for Advances Written Record, Monument, Library
 *
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class DialogExtraCreditsFragment extends DialogFragment {
    private static final String ARG_CREDITS = "arg_credits";
    private static final String REQUEST_KEY = "extraCreditsDialogResult";
    private DialogCreditsBinding binding;
    private String[] items;
    private int initialCredits;
    private int blue, green, orange, red, yellow;
    private AlertDialog dialogInstance;
    private CivicViewModel mCivicViewModel;

    public DialogExtraCreditsFragment() {
    }

    public static DialogExtraCreditsFragment newInstance(int creditsAmount) {
        DialogExtraCreditsFragment fragment = new DialogExtraCreditsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CREDITS, creditsAmount);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        fragment.setStyle(STYLE_NORMAL, R.style.Theme_CivicHelper_Dialog);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ViewModel beziehen
        if (getActivity() != null) {
            mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        } else {
            Log.e("ExtraCreditsDialog", "Activity is null, cannot get ViewModel.");
            dismissAllowingStateLoss();
            return;
        }

        // 2. Argumente abrufen
        if (getArguments() != null) {
            this.initialCredits = getArguments().getInt(ARG_CREDITS, 0);
            if (this.initialCredits == 0 && getArguments().getInt(ARG_CREDITS, -1) == -1) {
                Log.e("ExtraCreditsDialog", "Credits argument is missing or zero, which might be unintended.");
                // Hier entscheiden, ob ein Wert von 0 gültig ist oder ein Fehler.
                // Wenn 0 ungültig ist:
                // dismissAllowingStateLoss();
                // return;
            }
        } else {
            Log.e("ExtraCreditsDialog", "Arguments are null.");
            dismissAllowingStateLoss();
            return;
        }

        switch (initialCredits) {
            case 10:
                items = new String[]{"0", "5", "10"};
                break;
            case 20:
                items = new String[]{"0", "5", "10", "15", "20"};
                break;
            case 30:
                items = new String[]{"0", "5", "10", "15", "20", "25", "30"};
                break;
            default:
                // Fallback, falls 'initialCredits' einen unerwarteten Wert hat
                Log.w("ExtraCreditsDialog", "Unexpected number of credits: " + initialCredits + ". Using default items.");
                items = new String[]{"0"}; // Oder eine andere sinnvolle Standardeinstellung
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog currentDialog = getDialog();
        if (currentDialog != null && currentDialog.getWindow() != null) {
            currentDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogCreditsBinding.inflate(getLayoutInflater());
        blue = 0;
        green = 0;
        orange = 0;
        red = 0;
        yellow = 0;

        if (items == null || items.length == 0) {
            Log.e("ExtraCreditsDialog", "Spinner items not initialized!");
            // Erstelle einen einfachen Fehlerdialog oder schließe diesen Dialog
            return new AlertDialog.Builder(requireActivity())
                    .setTitle("Error")
                    .setMessage("Could not initialize dialog options.")
                    .setPositiveButton(android.R.string.ok, (d, w) -> dismiss())
                    .create();
        }


//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_right, items);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_right);

        binding.spinnerblue.setAdapter(spinnerAdapter);
        binding.spinnerblue.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerblue.setSelection(0, false);

        binding.spinnerred.setAdapter(spinnerAdapter);
        binding.spinnerred.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerred.setSelection(0, false);

        binding.spinnerorange.setAdapter(spinnerAdapter);
        binding.spinnerorange.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerorange.setSelection(0, false);

        binding.spinneryellow.setAdapter(spinnerAdapter);
        binding.spinneryellow.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinneryellow.setSelection(0, false);

        binding.spinnergreen.setAdapter(spinnerAdapter);
        binding.spinnergreen.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnergreen.setSelection(0, false);

        binding.creditsremaining.setText(String.valueOf(initialCredits));
        // Aktuelle Boni aus dem ViewModel anzeigen
        binding.bonusblue.setText(String.valueOf(mCivicViewModel.getBlue()));
        binding.bonusgreen.setText(String.valueOf(mCivicViewModel.getGreen()));
        binding.bonusorange.setText(String.valueOf(mCivicViewModel.getOrange()));
        binding.bonusred.setText(String.valueOf(mCivicViewModel.getRed()));
        binding.bonusyellow.setText(String.valueOf(mCivicViewModel.getYellow()));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.dialog_extra_credits);
        View spinnerView = binding.getRoot();
        builder.setView(spinnerView);
        builder.setPositiveButton(R.string.ok, (dialogInterface, id) -> {
            mCivicViewModel.updateBonus(blue, green, orange, red, yellow);
            mCivicViewModel.requestPriceRecalculation();

            Bundle result = new Bundle();
            // Keine spezifischen Daten benötigt, leeres Bundle ist ok
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        dialogInstance = builder.create(); // Verwende dialogInstance
        if (dialogInstance.getWindow() != null) {
            dialogInstance.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }

        if (dialogInstance.isShowing()) {
            updateOkButtonState();
        } else {
            dialogInstance.setOnShowListener(dialogInterface -> updateOkButtonState());
        }

        binding.spinnerblue.post(() -> binding.spinnerblue.setSelection(0));
        return dialogInstance;
    }

    private void updateOkButtonState() {
        if (dialogInstance != null && binding != null) {
            int totalSelected = blue + green + orange + red + yellow;
            binding.creditsremaining.setText(String.valueOf(initialCredits - totalSelected));
            dialogInstance.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(totalSelected == initialCredits);
        }
    }

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (items == null || position >= items.length) {
                return;
            }
            int selectedValue = Integer.parseInt(items[position]);
            int parentId = parent.getId();

            if (parentId == R.id.spinnerblue) {
                blue = selectedValue;
            } else if (parentId == R.id.spinnergreen) {
                green = selectedValue;
            } else if (parentId == R.id.spinnerorange) {
                orange = selectedValue;
            } else if (parentId == R.id.spinnerred) {
                red = selectedValue;
            } else if (parentId == R.id.spinneryellow) {
                yellow = selectedValue;
            }
            updateOkButtonState();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}