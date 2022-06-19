package org.tesira.mturba.civichelper;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.tesira.mturba.civichelper.databinding.DialogCreditsBinding;
import org.tesira.mturba.civichelper.db.CivicViewModel;

/**
 * Dialog for Advances Written Record, Monument, Library
 *
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class ExtraCreditsDialogFragment extends DialogFragment {

    private DialogCreditsBinding binding;
    String[] items;
    private int credits, blue, green, yellow, orange, red;
    private AlertDialog dialog;
    private CivicViewModel mCivicViewModel;
    private AdvancesFragment fragment;

    public ExtraCreditsDialogFragment(CivicViewModel mCivicViewModel, AdvancesFragment fragment,  int credits) {
        super(R.layout.dialog_credits);
        this.mCivicViewModel = mCivicViewModel;
        this.fragment = fragment;
        this.credits = credits;
        switch (credits) {
            case 10 :
                items = new String[] {"0", "5", "10"};
                break;
            case 20:
                items = new String[] {"0", "5", "10", "15", "20"};
                break;
            case 30:
                items = new String[] {"0", "5", "10", "15", "20", "25", "30"};
                break;
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogCreditsBinding.inflate(getLayoutInflater());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerblue.setAdapter(spinnerAdapter);
        binding.spinnerblue.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerred.setAdapter(spinnerAdapter);
        binding.spinnerred.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnerorange.setAdapter(spinnerAdapter);
        binding.spinnerorange.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinneryellow.setAdapter(spinnerAdapter);
        binding.spinneryellow.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.spinnergreen.setAdapter(spinnerAdapter);
        binding.spinnergreen.setOnItemSelectedListener(new MyOnItemSelectedListener());
        binding.creditsremaining.setText(String.valueOf(credits));

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_extra_credits);
        View spinnerView = binding.getRoot();
        builder.setView(spinnerView);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            mCivicViewModel.updateBonus(blue, green, orange, red, yellow);
            // save to prefs
            ((MainActivity) getActivity()).saveBonus();
            if (fragment != null) {
                fragment.returnToDashboard(false);
            } else {
                requireActivity().getSupportFragmentManager().setFragmentResult("extraCredits", new Bundle());
            }
        });
        // Create the AlertDialog object and return it
        dialog = builder.create();
        return dialog;
    }

    private class MyOnItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            switch (parent.getId()) {
                case R.id.spinnerblue:
                    blue = Integer.parseInt(items[position]);
                    break;
                case R.id.spinnergreen:
                    green = Integer.parseInt(items[position]);
                    break;
                case R.id.spinnerorange:
                    orange = Integer.parseInt(items[position]);
                    break;
                case R.id.spinnerred:
                    red = Integer.parseInt(items[position]);
                    break;
                case R.id.spinneryellow:
                    yellow = Integer.parseInt(items[position]);
                    break;
            }
            calculateCredits();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        public void calculateCredits() {
            int total;
            total = blue + green + orange + red + yellow;
            binding.creditsremaining.setText(String.valueOf(credits - total));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(total == credits);
        }
    }
}
