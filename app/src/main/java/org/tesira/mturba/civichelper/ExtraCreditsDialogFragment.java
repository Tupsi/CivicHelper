package org.tesira.mturba.civichelper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.tesira.mturba.civichelper.databinding.DialogCreditsBinding;

public class ExtraCreditsDialogFragment extends DialogFragment {

    private DialogCreditsBinding binding;
    String items[] = {"0", "5", "10", "15", "20"};
    private int credits, blue, green, yellow, orange, red;
    private AlertDialog dialog;
    private AdvancesFragment fragment;

    public ExtraCreditsDialogFragment(AdvancesFragment fragment, int credits) {
        this.fragment = fragment;
        this.credits = credits;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogCreditsBinding.inflate(getLayoutInflater());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        ArrayAdapter<String> spinnerAdapterBlue = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapterBlue.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerblue.setAdapter(spinnerAdapter);
        binding.spinnerblue.setOnItemSelectedListener(new MyOnItemSelectedListener());
//        ArrayAdapter<String> spinnerAdapterRed = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapterRed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerred.setAdapter(spinnerAdapter);
        binding.spinnerred.setOnItemSelectedListener(new MyOnItemSelectedListener());
//        ArrayAdapter<String> spinnerAdapterOrange = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapterOrange.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerorange.setAdapter(spinnerAdapter);
        binding.spinnerorange.setOnItemSelectedListener(new MyOnItemSelectedListener());
//        ArrayAdapter<String> spinnerAdapterYellow = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapterYellow.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinneryellow.setAdapter(spinnerAdapter);
        binding.spinneryellow.setOnItemSelectedListener(new MyOnItemSelectedListener());
//        ArrayAdapter<String> spinnerAdapterGreen = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
//        spinnerAdapterGreen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnergreen.setAdapter(spinnerAdapter);
        binding.spinnergreen.setOnItemSelectedListener(new MyOnItemSelectedListener());

        binding.creditsremaining.setText(String.valueOf(credits));

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_extra_credits);
        View spinnerView = binding.getRoot();
        builder.setView(spinnerView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fragment.updateBonus(blue, green, orange, red, yellow);
                    }
                });
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
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
            Log.v("SPINNER", "nothing selected");
        }

        public void calculateCredits() {
            int total;
            total = blue + green + orange + red + yellow;
            binding.creditsremaining.setText(String.valueOf(credits - total));
            if (total != credits) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
            else {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }
    }
}
