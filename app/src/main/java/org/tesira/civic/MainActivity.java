package org.tesira.civic;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tesira.civic.databinding.ActivityMainBinding;
import org.tesira.civic.db.CivicViewModel;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private CivicViewModel mCivicViewModel;

    private String getLabelOrId(NavDestination destination) {
        if (destination.getLabel() != null && !destination.getLabel().toString().isEmpty()) {
            return destination.getLabel().toString();
        }
        // Fallback zur Ressourcen-ID als Hex-String, wenn kein Label vorhanden
        return "ID:0x" + Integer.toHexString(destination.getId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBarsInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams params = v.getLayoutParams();
            return WindowInsetsCompat.CONSUMED;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.myNavHostFragment);

        mCivicViewModel = new ViewModelProvider(this).get(CivicViewModel.class);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                Log.d("Nav_DEST_CHANGED", "----------------------------------------------------");
                Log.d("Nav_DEST_CHANGED", "Navigated TO: " + getLabelOrId(destination) +
                        " (ID: 0x" + Integer.toHexString(destination.getId()) + ")");

                // Versuch, den vorherigen Backstack-Eintrag zu bekommen (kann null sein)
                NavBackStackEntry previousEntry = null;
                try {
                    // Die BackQueue ist nicht direkt zugänglich, aber wir können versuchen,
                    // das Element vor dem aktuellen Ziel zu bekommen, wenn wir wissen, dass eines da ist.
                    // Dies ist ein Workaround und nicht ideal, aber für Debugging-Zwecke.
                    // Eine robustere Methode wäre, die Stack-Einträge manuell zu verfolgen.

                    // Da wir getBackQueue() nicht zuverlässig nutzen können,
                    // loggen wir einfach das aktuelle Ziel. Die Sequenz der Logs wird den Pfad zeigen.
                    // Wenn du von A nach B nach C navigierst, siehst du die Logs in dieser Reihenfolge.
                    // Wenn du dann Back drückst, siehst du die Navigation zurück zu B, dann zu A.

                } catch (Exception e) {
                    Log.w("Nav_DEST_CHANGED", "Could not determine previous entry easily: " + e.getMessage());
                }
                Log.d("Nav_DEST_CHANGED", "----------------------------------------------------");
            });

            drawerLayout = binding.drawerLayout;

            appBarConfiguration = new AppBarConfiguration.Builder(
                    // Fragmente die BurgerMenu behalten, Rest bekommt den Zurückpfeil
                    R.id.homeFragment, R.id.buyingFragment)
                    .setOpenableLayout(drawerLayout)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            binding.navView.setNavigationItemSelectedListener(menuItem -> {
                int id = menuItem.getItemId();
                // ----- Logging -----
                Log.d("NavDrawer", "Item selected: " + menuItem.getTitle() + ", Item ID: " + id + " (Hex: " + Integer.toHexString(id) + ")");
                // Du kannst hier weitere erwartete IDs loggen, falls nötig für Debugging
                // Log.d("NavDrawer", "Expected Home ID: " + R.id.homeFragment + " (Hex: " + Integer.toHexString(R.id.homeFragment) + ")");
                // Log.d("NavDrawer", "Expected Buying ID: " + R.id.buyingFragment + " (Hex: " + Integer.toHexString(R.id.buyingFragment) + ")");

                NavDestination currentDestination = navController.getCurrentDestination();
                int currentDestinationId = (currentDestination != null) ? currentDestination.getId() : 0;

                if (id == R.id.menu_newGame) {
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(binding.navView)) {
                        drawerLayout.closeDrawer(binding.navView);
                    }
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                mCivicViewModel.requestNewGame(); // ViewModel kümmert sich um Reset und Navigation via Observer
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                // No button clicked
                                break;
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Are you sure you want to start a new game? All progress will be lost.")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                    return true; // Wichtig: Du behandelst menu_newGame hier

                } else if (id == R.id.homeFragment) {
                    // SPEZIALFALL: Navigation zum HomeFragment.
                    // Wir wollen immer zum HomeFragment navigieren und den Backstack darüber bereinigen.
                    Log.i("NavDrawer_Home", "Manually navigating to HomeFragment.");

                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                            .setLaunchSingleTop(true)
                            .build();
                    try {
                        navController.navigate(R.id.homeFragment, null, navOptions);
                    } catch (IllegalArgumentException e) {
                        Log.w("NavDrawer_Home", "Already at Home or cannot pop to Home: " + e.getMessage());
                        if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != R.id.homeFragment) {
                            navController.navigate(R.id.homeFragment); // Fallback
                        }
                    }

                    if (drawerLayout != null && drawerLayout.isDrawerOpen(binding.navView)) {
                        drawerLayout.closeDrawer(binding.navView);
                        Log.d("NavDrawer_Home", "Drawer closed for Home navigation.");
                    }
                    return true; // Wichtig: true zurückgeben

                } else if (currentDestinationId == R.id.buyingFragment &&
                        (id == R.id.purchasesFragment || id == R.id.tipsFragment || id == R.id.aboutFragment || id == R.id.settingsFragment)) {
                    // SPEZIALFALL: Navigation von BuyingFragment zu Purchases, Tips, About oder Settings.
                    // Wir navigieren manuell, um sicherzustellen, dass BuyingFragment im Backstack bleibt.
                    Log.i("NavDrawer_SpecialCase", "Manually navigating from BuyingFragment to " + menuItem.getTitle());
                    navController.navigate(id); // Navigiere zur ID des angeklickten Menü-Items

                    if (drawerLayout != null && drawerLayout.isDrawerOpen(binding.navView)) {
                        drawerLayout.closeDrawer(binding.navView);
                        Log.d("NavDrawer_SpecialCase", "Drawer closed for " + menuItem.getTitle() + " from BuyingFragment.");
                    }
                    return true; // Wichtig: true zurückgeben

                } else {
                    // Für alle anderen Menü-Items oder wenn nicht im BuyingFragment oder nicht Home,
                    // die Standard-Navigation von NavigationUI verwenden
                    boolean handled = NavigationUI.onNavDestinationSelected(menuItem, navController);
                    Log.d("NavDrawer", "NavigationUI.onNavDestinationSelected for " + menuItem.getTitle() + " handled: " + handled);

                    if (handled) {
                        if (navController.getCurrentDestination() != null) {
                            // Verwende deine getLabelOrId Methode, wenn du sie hast, ansonsten menuItem.getTitle() oder die ID
                            Log.d("NavDrawer", "Navigated to destination: " + getLabelOrId(navController.getCurrentDestination()) + " (ID: " + navController.getCurrentDestination().getId() + ")");
                        } else {
                            Log.d("NavDrawer", "Navigated, but current destination is null.");
                        }
                    } else {
                        Log.w("NavDrawer", "Item '" + menuItem.getTitle() + "' (ID: " + id + ") NOT handled by NavigationUI. Current NavController graph: " + (navController.getGraph() != null ? Integer.toHexString(navController.getGraph().getId()) : "null"));
                    }

                    // Drawer schließen, basierend auf 'handled' für diesen allgemeinen Fall
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(binding.navView)) {
                        if (handled) { // Schließe nur, wenn NavigationUI es gehandhabt hat und es kein Spezialfall oben war
                            drawerLayout.closeDrawer(binding.navView);
                            Log.d("NavDrawer", "Drawer closed for handled item: " + menuItem.getTitle());
                        } else {
                            // Drawer NICHT schließen, wenn das Item nicht von NavigationUI behandelt wurde
                            // UND es keiner unserer manuellen Fälle war.
                            // Dieses Verhalten ist konsistent mit dem vorherigen Stand.
                            Log.w("NavDrawer", "Drawer NOT closed for unhandled item: " + menuItem.getTitle());
                        }
                    }
                    return handled;
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