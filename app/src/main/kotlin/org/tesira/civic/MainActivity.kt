package org.tesira.civic

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import org.tesira.civic.databinding.ActivityMainBinding
import org.tesira.civic.db.CivicViewModel

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: DrawerLayout? = null
    private lateinit var navController: NavController
    private val mCivicViewModel: CivicViewModel by viewModels()

    private fun getLabelOrId(destination: NavDestination): String {
        if (destination.label != null && !destination.label.toString().isEmpty()) {
            return destination.label.toString()
        }
        // Fallback zur Ressourcen-ID als Hex-String, wenn kein Label vorhanden
        return "ID:0x" + Integer.toHexString(destination.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val toolbar: Toolbar = binding!!.toolbar
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBarsInsets.top, v.paddingRight, v.paddingBottom)
            WindowInsetsCompat.CONSUMED
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.myNavHostFragment) as? NavHostFragment

        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment nicht gefunden! App-Navigation wird nicht funktionieren.")
        } else {
            navController = navHostFragment.navController

            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                // Dein Logging Code bleibt hier logisch gleich.
                // Kotlin String Templates können es etwas lesbarer machen:
                Log.d("Nav_DEST_CHANGED", "----------------------------------------------------")
                val destLabelOrId = getLabelOrId(destination) // Annahme: getLabelOrId ist eine Methode in dieser Klasse
                val destIdHex = Integer.toHexString(destination.id)
                Log.d("Nav_DEST_CHANGED", "Navigated TO: $destLabelOrId (ID: 0x$destIdHex)")
                // ... restlicher Listener-Code ...
                Log.d("Nav_DEST_CHANGED", "----------------------------------------------------")
            }

            drawerLayout = binding!!.drawerLayout

            val currentDrawerLayout = drawerLayout
            if (currentDrawerLayout != null) {
                appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.homeFragment, R.id.buyingFragment),
                    currentDrawerLayout
                )
                // NavigationUI.setupActionBarWithNavController hier drinnen aufrufen,
                // da es appBarConfiguration benötigt.
                setupActionBarWithNavController(this, navController, appBarConfiguration) // this ist implizit
            } else {
                Log.e("MainActivity", "DrawerLayout ist null, AppBarConfiguration kann nicht vollständig initialisiert werden.")
                // Fallback: AppBarConfiguration ohne Drawer initialisieren, wenn das sinnvoll ist
                // appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.buyingFragment))
                // setupActionBarWithNavController(navController, appBarConfiguration)
                // Oder einen Fehler signalisieren.
            }

            binding?.navView?.setNavigationItemSelectedListener { menuItem: MenuItem ->
                val id = menuItem.itemId
                // ----- Logging -----
                Log.d(
                    "NavDrawer",
                    "Item selected: " + menuItem.title + ", Item ID: " + id + " (Hex: " + Integer.toHexString(
                        id
                    ) + ")"
                )

                val currentDestination = navController!!.currentDestination
                val currentDestinationId = currentDestination?.id ?: 0
                if (id == R.id.menu_newGame) {
                    if (drawerLayout != null && drawerLayout!!.isDrawerOpen(binding!!.navView)) {
                        drawerLayout!!.closeDrawer(binding!!.navView)
                    }
                    val dialogClickListener =
                        DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> mCivicViewModel!!.requestNewGame() // ViewModel kümmert sich um Reset und Navigation via Observer
                                DialogInterface.BUTTON_NEGATIVE -> {}
                            }
                        }
                    val builder =
                        AlertDialog.Builder(this@MainActivity)
                    builder.setMessage("Are you sure you want to start a new game? All progress will be lost.")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show()
                    return@setNavigationItemSelectedListener true // Wichtig: Du behandelst menu_newGame hier
                } else if (id == R.id.homeFragment) {
                    // SPEZIALFALL: Navigation zum HomeFragment.
                    // Wir wollen immer zum HomeFragment navigieren und den Backstack darüber bereinigen.
                    Log.i("NavDrawer_Home", "Manually navigating to HomeFragment.")

                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(navController!!.graph.startDestinationId, false, true)
                        .setLaunchSingleTop(true)
                        .build()
                    try {
                        navController!!.navigate(R.id.homeFragment, null, navOptions)
                    } catch (e: IllegalArgumentException) {
                        Log.w(
                            "NavDrawer_Home",
                            "Already at Home or cannot pop to Home: " + e.message
                        )
                        if (navController!!.currentDestination == null || navController!!.currentDestination!!.id != R.id.homeFragment) {
                            navController!!.navigate(R.id.homeFragment) // Fallback
                        }
                    }

                    if (drawerLayout != null && drawerLayout!!.isDrawerOpen(binding!!.navView)) {
                        drawerLayout!!.closeDrawer(binding!!.navView)
                        Log.d("NavDrawer_Home", "Drawer closed for Home navigation.")
                    }
                    return@setNavigationItemSelectedListener true // Wichtig: true zurückgeben
                } else if (currentDestinationId == R.id.buyingFragment &&
                    (id == R.id.purchasesFragment || id == R.id.tipsFragment || id == R.id.aboutFragment || id == R.id.settingsFragment)
                ) {
                    // SPEZIALFALL: Navigation von BuyingFragment zu Purchases, Tips, About oder Settings.
                    // Wir navigieren manuell, um sicherzustellen, dass BuyingFragment im Backstack bleibt.
                    Log.i(
                        "NavDrawer_SpecialCase",
                        "Manually navigating from BuyingFragment to " + menuItem.title
                    )
                    navController!!.navigate(id) // Navigiere zur ID des angeklickten Menü-Items

                    if (drawerLayout != null && drawerLayout!!.isDrawerOpen(binding!!.navView)) {
                        drawerLayout!!.closeDrawer(binding!!.navView)
                        Log.d(
                            "NavDrawer_SpecialCase",
                            "Drawer closed for " + menuItem.title + " from BuyingFragment."
                        )
                    }
                    return@setNavigationItemSelectedListener true // Wichtig: true zurückgeben
                } else {
                    // Für alle anderen Menü-Items oder wenn nicht im BuyingFragment oder nicht Home,
                    // die Standard-Navigation von NavigationUI verwenden
                    val handled = onNavDestinationSelected(menuItem, navController!!)
                    Log.d(
                        "NavDrawer",
                        "NavigationUI.onNavDestinationSelected for " + menuItem.title + " handled: " + handled
                    )

                    if (handled) {
                        if (navController!!.currentDestination != null) {
                            // Verwende deine getLabelOrId Methode, wenn du sie hast, ansonsten menuItem.getTitle() oder die ID
                            Log.d(
                                "NavDrawer", "Navigated to destination: " + getLabelOrId(
                                    navController!!.currentDestination!!
                                ) + " (ID: " + navController!!.currentDestination!!.id + ")"
                            )
                        } else {
                            Log.d(
                                "NavDrawer",
                                "Navigated, but current destination is null."
                            )
                        }
                    } else {
                        Log.w(
                            "NavDrawer",
                            "Item '" + menuItem.title + "' (ID: " + id + ") NOT handled by NavigationUI. Current NavController graph: " + (if (navController!!.graph != null) Integer.toHexString(
                                navController!!.graph.id
                            ) else "null")
                        )
                    }

                    // Drawer schließen, basierend auf 'handled' für diesen allgemeinen Fall
                    if (drawerLayout != null && drawerLayout!!.isDrawerOpen(binding!!.navView)) {
                        if (handled) { // Schließe nur, wenn NavigationUI es gehandhabt hat und es kein Spezialfall oben war
                            drawerLayout!!.closeDrawer(binding!!.navView)
                            Log.d(
                                "NavDrawer",
                                "Drawer closed for handled item: " + menuItem.title
                            )
                        } else {
                            // Drawer NICHT schließen, wenn das Item nicht von NavigationUI behandelt wurde
                            // UND es keiner unserer manuellen Fälle war.
                            // Dieses Verhalten ist konsistent mit dem vorherigen Stand.
                            Log.w(
                                "NavDrawer",
                                "Drawer NOT closed for unhandled item: " + menuItem.title
                            )
                        }
                    }
                    return@setNavigationItemSelectedListener handled
                }
            }
        }

        //        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout);
        val configuration = this.resources.configuration
        val screenWidthDp =
            configuration.screenWidthDp //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        Log.d("ScreenDetails", "screenWidthDp: $screenWidthDp")
        mCivicViewModel!!.screenWidthDp = screenWidthDp

        val smallestScreenWidthDp =
            configuration.smallestScreenWidthDp //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        Log.d("ScreenDetails", "smallestScreenWidthDp: $smallestScreenWidthDp")
        mCivicViewModel!!.screenWidthDp = screenWidthDp

        mCivicViewModel!!.newGameStartedEvent.observe(this,
            Observer<Event<Boolean?>?> { resetCompletedEvent -> // Hier den Lambda-Parameter benennen
                if (resetCompletedEvent != null) {
                    val resetCompleted = resetCompletedEvent.getContentIfNotHandled()

                    if (resetCompleted != null && resetCompleted) {
                        // Keep purely Activity-related UI actions here
                        Toast.makeText(
                            this@MainActivity,
                            "Starting a New Game!",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (drawerLayout != null && drawerLayout!!.isDrawerOpen(binding!!.navView)) {
                            drawerLayout!!.closeDrawer(binding!!.navView)
                        }
                        navController!!.navigate(R.id.homeFragment)
                    }
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp(navController!!, appBarConfiguration!!)
                || super.onSupportNavigateUp()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onStop() {
        super.onStop()
        mCivicViewModel!!.saveData()
    }

    public override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}