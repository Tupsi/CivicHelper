package org.tesira.civic;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.tesira.civic.R;
import org.tesira.civic.databinding.DialogCreditsBinding;
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;

/**
 * Dialog for Advances Written Record, Monument, Library
 *
 * https://developer.android.com/guide/topics/ui/dialogs
 */
public class ExtraCreditsDialogFragment extends DialogFragment {

    private DialogCreditsBinding binding;
    String[] items;
    private int credits, blue, green, orange, red, yellow;
    private int oldblue, oldgreen, oldorange, oldred, oldyellow;
    private AlertDialog dialog;
    private CivicViewModel mCivicViewModel;
    private AdvancesFragment fragment;

    public ExtraCreditsDialogFragment(CivicViewModel mCivicViewModel, AdvancesFragment fragment,  int credits, int oldblue, int oldgreen, int oldorange, int oldred, int oldyellow) {
        super(R.layout.dialog_credits);
        setCancelable(false);
        this.mCivicViewModel = mCivicViewModel;
        this.fragment = fragment;
        this.credits = credits;
        this.oldblue = oldblue;
        this.oldgreen = oldgreen;
        this.oldorange = oldorange;
        this.oldred = oldred;
        this.oldyellow = oldyellow;

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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        binding.bonusblue.setText(String.valueOf(mCivicViewModel.getBlue()));
        binding.bonusgreen.setText(String.valueOf(mCivicViewModel.getGreen()));
        binding.bonusorange.setText(String.valueOf(mCivicViewModel.getOrange()));
        binding.bonusred.setText(String.valueOf(mCivicViewModel.getRed()));
        binding.bonusyellow.setText(String.valueOf(mCivicViewModel.getYellow()));

//        binding.bonusblue.setText(String.valueOf(oldblue));
//        binding.bonusgreen.setText(String.valueOf(oldgreen));
//        binding.bonusorange.setText(String.valueOf(oldorange));
//        binding.bonusred.setText(String.valueOf(oldred));
//        binding.bonusyellow.setText(String.valueOf(oldyellow));

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
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
