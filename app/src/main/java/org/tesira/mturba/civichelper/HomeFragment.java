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

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.ui.NavigationUI;

import org.tesira.mturba.civichelper.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String PURCHASED = "purchasedAdvances";

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        View view = binding.getRoot();
        binding.startBtn.setOnClickListener(v -> {
            onClickButton(v);
            //            Log.v("Button", "HomeFragment Start Clicked!");
//            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_advancesFragment);
        });
        binding.resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButton(v);
            }
        });
        Log.v("HomeFragment", "onCreateView");
        setHasOptionsMenu(true);
        return view;
    }

    public void onClickButton(View v) {
        Log.v("Button", ""+v.getId());
        switch (v.getId()) {
            case R.id.startBtn:
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_advancesFragment);
                break;
            case R.id.resetBtn:
                SharedPreferences prefs = getContext().getSharedPreferences(PURCHASED, Context.MODE_PRIVATE);
                prefs.edit().clear().commit();
                break;
        }
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
}