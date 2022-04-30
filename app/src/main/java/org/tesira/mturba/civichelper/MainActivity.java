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
import org.tesira.mturba.civichelper.db.CardColor;
import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.Map;

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
        navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);
        NavigationUI.setupWithNavController(binding.navView, navController);
        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);
        mCivicViewModel.setTreasure(prefs.getInt(TREASURE_BOX,0));
        loadBonus();
    }

    @Override
    public boolean onSupportNavigateUp() {
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
        blue = savedBonus.getInt(CardColor.BLUE.getName(), 0);
        green = savedBonus.getInt(CardColor.GREEN.getName(), 0);
        orange = savedBonus.getInt(CardColor.ORANGE.getName(), 0);
        red = savedBonus.getInt(CardColor.RED.getName(), 0);
        yellow = savedBonus.getInt(CardColor.YELLOW.getName(), 0);
        Log.v("MAIN"," : " + blue + " : " + green + " : " + orange + " : " + red + " : " + yellow);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.BLUE, blue);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.GREEN, green);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.ORANGE, orange);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.RED, red);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.YELLOW, yellow);
    }

    public void saveBonus() {
        SharedPreferences.Editor editor = savedBonus.edit();

        // HashMap Save
        for (Map.Entry<CardColor, Integer> entry: mCivicViewModel.getCardBonus().getValue().entrySet()){
            editor.putInt(entry.getKey().getName(), entry.getValue());
        }
        editor.apply();
        Log.v("MAIN", "saveBonus in Main");
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
//        saveBonus();
        Log.v("MAIN","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.v("MAIN","---> onDestroy() <--- ");
    }
}