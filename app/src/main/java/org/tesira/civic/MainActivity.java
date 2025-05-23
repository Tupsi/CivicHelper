package org.tesira.civic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import org.tesira.civic.databinding.ActivityMainBinding;
import org.tesira.civic.db.CivicViewModel;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private CivicViewModel mCivicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myNavHostFragment);

        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            drawerLayout = binding.drawerLayout;

            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment, R.id.advancesFragment, R.id.purchasesFragment, R.id.settingsFragment, R.id.tipsFragment, R.id.aboutFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            //NavigationUI.setupWithNavController(binding.navView, navController);
            binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if (id == R.id.menu_newGame) {
                        if (drawerLayout != null) {
                            drawerLayout.closeDrawer(binding.navView);
                        }
                        // Show the confirmation dialog before starting a new game
                        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Yes button clicked, trigger the reset process in the ViewModel
                                    mCivicViewModel.requestNewGame();
                                    // The UI update will be handled by the resetEvent observer
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    // No button clicked
                                    break;
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                        return true; // Event als behandelt markieren
                    } else {
                        // Für alle anderen Menü-Items, die Standard-Navigation von NavigationUI verwenden
                        boolean handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
                        if (handled) {
                            if (drawerLayout != null) {
                                drawerLayout.closeDrawer(binding.navView);
                            }
                        }
                        return handled;
                    }
                }
            });

        } else {
            Log.e("MainActivity", "NavHostFragment nicht gefunden!");
        }
//        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);

        Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        Log.d("ScreenDetails", "screenWidthDp: " + screenWidthDp);
        mCivicViewModel.setScreenWidthDp(screenWidthDp);

        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        Log.d("ScreenDetails", "smallestScreenWidthDp: " + smallestScreenWidthDp);
        mCivicViewModel.setScreenWidthDp(screenWidthDp);

        mCivicViewModel.getNewGameStartedEvent().observe(this, new Observer<Event<Boolean>>() {
            @Override
            public void onChanged(Event<Boolean> resetCompletedEvent) {
                if (resetCompletedEvent != null) {
                    Boolean resetCompleted = resetCompletedEvent.getContentIfNotHandled();

                    if (resetCompleted != null && resetCompleted) {
                        // Keep purely Activity-related UI actions here
                        Toast.makeText(MainActivity.this, "Starting a New Game!", Toast.LENGTH_SHORT).show();
                        if (drawerLayout != null && drawerLayout.isDrawerOpen(binding.navView)) {
                            drawerLayout.closeDrawer(binding.navView);
                        }
                        navController.navigate(R.id.homeFragment);
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
//        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    public void onPause() {
        super.onPause();
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
        mCivicViewModel.saveData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}