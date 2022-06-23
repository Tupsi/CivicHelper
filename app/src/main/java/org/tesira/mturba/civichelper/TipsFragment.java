package org.tesira.mturba.civichelper;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import org.tesira.mturba.civichelper.databinding.FragmentTipsBinding;
import org.tesira.mturba.civichelper.db.CivicViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TipsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TipsFragment extends Fragment {

    private FragmentTipsBinding binding;
    private CivicViewModel mCivicViewModel;
    private String[] tips;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public TipsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TipsFragment.
     */
    public static TipsFragment newInstance(String param1, String param2) {
        TipsFragment fragment = new TipsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        tips = getResources().getStringArray(R.array.tips);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTipsBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
//        String[] civics = getResources().getStringArray(R.array.civilizations_entries);
        ArrayAdapter<CharSequence> civicsAdapter = ArrayAdapter.createFromResource(getContext(), R.array.civilizations_entries, android.R.layout.simple_spinner_dropdown_item);
        binding.tipsSpinner.setAdapter(civicsAdapter);
        binding.tipsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selCivic = (String) parent.getItemAtPosition(position);
                String out = tips[parent.getSelectedItemPosition()];
                binding.tipsTextView.setText(out);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        int civicNumber = Integer.parseInt(prefs.getString("civilization", "1"));
        binding.tipsSpinner.setSelection(civicNumber-1);
        return rootView;
    }
}