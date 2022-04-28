package org.tesira.mturba.civichelper;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import org.tesira.mturba.civichelper.card.CardColor;
import org.tesira.mturba.civichelper.databinding.FragmentHomeBinding;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import org.tesira.mturba.civichelper.db.Purchase;

import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CivicViewModel mCivicViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        binding.startBtn.setOnClickListener(v -> onClickButton(v));
        binding.resetBtn.setOnClickListener(v -> onClickButton(v));
        setHasOptionsMenu(true);
//        mCivicViewModel.getAllPurchases().observeForever(purchases -> {
//            for (Purchase name: purchases) {
//                Log.v("BUY", "observer : "+name.getName());
//            }
//        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClickButton(View v) {
        switch (v.getId()) {
            case R.id.startBtn:
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_advancesFragment);
                break;
            case R.id.resetBtn:
                ((MainActivity) getActivity()).newGame();
                loadBonus();
                break;
        }
    }

    public void loadBonus() {
        binding.bonusBlue.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.BLUE,0)));
        binding.bonusBlue.setBackgroundResource(R.color.arts);
        binding.bonusGreen.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.GREEN,0)));
        binding.bonusGreen.setBackgroundResource(R.color.science);
        binding.bonusOrange.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.ORANGE,0)));
        binding.bonusOrange.setBackgroundResource(R.color.crafts);
        binding.bonusRed.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.RED,0)));
        binding.bonusRed.setBackgroundResource(R.color.civic);
        binding.bonusYellow.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.YELLOW,0)));
        binding.bonusYellow.setBackgroundResource(R.color.religion);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
    }

    public void onPause() {
        super.onPause();
        Log.v("HOME","---> onPause() <--- ");
    }

    public void onStart() {
        super.onStart();
        Log.v("HOME","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("HOME","---> onResume() <--- ");
        loadBonus();
    }

    public void onStop() {
        super.onStop();
        Log.v("HOME","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.v("HOME","---> onDestroy() <--- ");
    }

}