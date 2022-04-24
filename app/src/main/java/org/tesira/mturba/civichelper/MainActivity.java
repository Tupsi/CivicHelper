package org.tesira.mturba.civichelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import org.tesira.mturba.civichelper.databinding.ActivityMainBinding;
import org.tesira.mturba.civichelper.db.CivicHelperDatabase;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import org.tesira.mturba.civichelper.db.CivilizationAdvance;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private CivicViewModel mCivicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        CivicHelperDatabase db = CivicHelperDatabase.getDatabase(this);
//        if (db == null){
//            Log.v("DB", "is null");
//        } else
//        {
//            Log.v("DB", "is NOT null");
//
//        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        drawerLayout = binding.drawerLayout;
        setContentView(binding.getRoot());
//        NavController navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);
        NavigationUI.setupWithNavController(binding.navView, navController);
        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);
//        LiveData<List<CivilizationAdvance>> all = mCivicViewModel.getAllCivics();
//        for (CivilizationAdvance adv: all.getValue()) {
//            Log.v("DB", adv.getName());
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        return NavigationUI.navigateUp(navController, drawerLayout);
    }
}