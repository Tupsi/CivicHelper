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
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import org.tesira.mturba.civichelper.databinding.ActivityMainBinding;
import org.tesira.mturba.civichelper.db.CardColor;
import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String PREF_TREASURE_BOX = "treasure";
    private static final String PREF_CITIES = "cities";
    private static final String PREF_TIME = "time";
    private static final String PREF_FILE_BONUS = "purchasedAdvancesBonus";

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private CivicViewModel mCivicViewModel;
    private SharedPreferences prefs, savedBonus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        savedBonus = this.getSharedPreferences(PREF_FILE_BONUS, Context.MODE_PRIVATE );
        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);
        mCivicViewModel.setCities(savedBonus.getInt(PREF_CITIES, 0));
        mCivicViewModel.setTimeVp(savedBonus.getInt(PREF_TIME,0));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        drawerLayout = binding.drawerLayout;
        setContentView(binding.getRoot());
        navController = Navigation.findNavController(this, R.id.myNavHostFragment);
        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);
        NavigationUI.setupWithNavController(binding.navView, navController);
        mCivicViewModel.setTreasure(prefs.getInt(PREF_TREASURE_BOX,0));
        mCivicViewModel.setRemaining(prefs.getInt(PREF_TREASURE_BOX,0));
        loadBonus();

        Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        mCivicViewModel.setScreenWidthDp(screenWidthDp);
        Log.v("DIMENSION", "" + screenWidthDp + " : " + smallestScreenWidthDp);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    public void saveVars() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_TREASURE_BOX, mCivicViewModel.getTreasure().getValue());
        editor.apply();
    }

    public void loadBonus() {
        int blue, green, orange, red, yellow;
        blue = savedBonus.getInt(CardColor.BLUE.getName(), 0);
        green = savedBonus.getInt(CardColor.GREEN.getName(), 0);
        orange = savedBonus.getInt(CardColor.ORANGE.getName(), 0);
        red = savedBonus.getInt(CardColor.RED.getName(), 0);
        yellow = savedBonus.getInt(CardColor.YELLOW.getName(), 0);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.BLUE, blue);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.GREEN, green);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.ORANGE, orange);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.RED, red);
        mCivicViewModel.getCardBonus().getValue().put(CardColor.YELLOW, yellow);
        mCivicViewModel.setHeart(prefs.getString("heart", "custom"));
    }

    public void saveBonus() {
        SharedPreferences.Editor editor = savedBonus.edit();
        // HashMap Save
        for (Map.Entry<CardColor, Integer> entry: mCivicViewModel.getCardBonus().getValue().entrySet()){
            editor.putInt(entry.getKey().getName(), entry.getValue());
        }
        editor.apply();
    }

    public void newGame() {
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
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        Log.v("MAIN","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("MAIN","---> onResume() <--- ");
    }

    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor edit = savedBonus.edit();
        edit.putInt(PREF_CITIES, mCivicViewModel.getCities());
        edit.putInt(PREF_TIME, mCivicViewModel.getTimeVp());
        edit.apply();
        Log.v("MAIN","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        Log.v("MAIN","---> onDestroy() <--- ");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.compareTo("heart") == 0) {
            mCivicViewModel.setHeart(sharedPreferences.getString("heart", "custom"));
        }
        // not needed atm
//        Log.v("PREFS", "changed in Main Activity");
//        switch (key){
//            case "sort" :
//                String sortingOrder = sharedPreferences.getString("sort", "name");
//                if (sortingOrder.equals("family")) {
//                    sharedPreferences.edit().putString("columns","3").apply();
//                }
//            case "chooser":
//            case "name" :
//            default:
//                break;
//        }
    }
}