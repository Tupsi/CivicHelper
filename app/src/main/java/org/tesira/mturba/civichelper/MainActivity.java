package org.tesira.mturba.civichelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import org.tesira.mturba.civichelper.databinding.ActivityMainBinding;
import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TREASURE_BOX = "treasure";
    private static final String BONUS = "purchasedAdvancesBonus";

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private CivicViewModel mCivicViewModel;
    private SharedPreferences prefs, savedBonus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        savedBonus = this.getSharedPreferences(BONUS, Context.MODE_PRIVATE );
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        drawerLayout = binding.drawerLayout;
        setContentView(binding.getRoot());
//        NavController navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);
        NavigationUI.setupWithNavController(binding.navView, navController);
        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);
        mCivicViewModel.setTreasure(prefs.getInt(TREASURE_BOX,0));
        loadBonus();
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    public void saveVars() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(TREASURE_BOX, mCivicViewModel.getTreasure().getValue());
        editor.apply();
    }

    public void loadBonus() {
        int blue, green, orange, red, yellow;
        Log.v("MAIN", "loadBonus inside Main");
        blue = savedBonus.getInt("bonusBlue", 0);
        green = savedBonus.getInt("bonusGreen", 0);
        orange = savedBonus.getInt("bonusOrange", 0);
        red = savedBonus.getInt("bonusRed", 0);
        yellow = savedBonus.getInt("bonusYellow", 0);
        Log.v("MAIN"," : " + blue + " : " + green + " : " + orange + " : " + red + " : " + yellow);
        mCivicViewModel.getCardBonus().getValue().put("blue", blue);
        mCivicViewModel.getCardBonus().getValue().put("green", green);
        mCivicViewModel.getCardBonus().getValue().put("orange", orange);
        mCivicViewModel.getCardBonus().getValue().put("red", red);
        mCivicViewModel.getCardBonus().getValue().put("yellow", yellow);
        mCivicViewModel.setAllBonus(blue, green, orange, red, yellow);
        Log.v("MAIN", "loading Bonus inside MainActiviy : "+mCivicViewModel.getBonusBlue());

    }

    public void saveBonus() {
        SharedPreferences.Editor editor = savedBonus.edit();

        // HashMap Save

        editor.putInt("bonusBlue", mCivicViewModel.getBonusBlue());
        editor.putInt("bonusGreen", mCivicViewModel.getBonusGreen());
        editor.putInt("bonusOrange", mCivicViewModel.getBonusOrange());
        editor.putInt("bonusRed", mCivicViewModel.getBonusRed());
        editor.putInt("bonusYellow", mCivicViewModel.getBonusYellow());
        editor.commit();
        Log.v("MAIN", "" + mCivicViewModel.getBonusBlue() + " : " + mCivicViewModel.getBonusGreen());
        Log.v("MAIN", "saveBonus in Advanced");
    }
    public void newGame() {
        Log.v("MAIN","clearing sharedPrefs from MAIN...");
        savedBonus.edit().clear().apply();
        mCivicViewModel.deletePurchases();
    }

    public void onPause() {
        super.onPause();
        Log.v("MAIN","---> onPause() <--- ");
        saveVars();
        saveBonus();
    }

    public void onStart() {
        super.onStart();
        Log.v("MAIN","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("MAIN","---> onResume() <--- ");
    }

    public void onStop() {
        super.onStop();
        saveBonus();
        Log.v("MAIN","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.v("MAIN","---> onDestroy() <--- ");
    }
}