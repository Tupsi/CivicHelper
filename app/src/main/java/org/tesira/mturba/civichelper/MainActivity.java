package org.tesira.mturba.civichelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import org.tesira.mturba.civichelper.databinding.ActivityMainBinding;
import org.tesira.mturba.civichelper.db.CivicViewModel;

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
//        LiveData<List<Card>> all = mCivicViewModel.getAllCivics();
//        for (Card adv: all.getValue()) {
//            Log.v("DB", adv.getName());
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        return NavigationUI.navigateUp(navController, drawerLayout);
    }
}