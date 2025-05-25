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

            binding?.navView?.setNavigationItemSelectedListener { menuItem ->
                val id = menuItem.itemId
                Log.d("NavDrawer_Kotlin", "Item selected: ${menuItem.title}, ID: $id (Hex: ${Integer.toHexString(id)})")

                val currentDestination = navController.currentDestination // navController sollte hier non-null sein
                val currentDestinationId = currentDestination?.id ?: 0

                // Der when-Ausdruck ersetzt die if-else if-Kette.
                // Der Wert des ausgewählten when-Zweigs wird zum Rückgabewert des Lambdas.
                when (id) {
                    R.id.menu_newGame -> {
                        if (drawerLayout?.isDrawerOpen(binding!!.navView) == true) { // Sicherer Zugriff auf drawerLayout
                            drawerLayout?.closeDrawer(binding!!.navView)
                        }
                        val dialogClickListener =
                            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> mCivicViewModel.requestNewGame()
                                    DialogInterface.BUTTON_NEGATIVE -> { /* No button clicked - tu nichts */ }
                                }
                            }
                        AlertDialog.Builder(this@MainActivity) // Explizit this@MainActivity für den Kontext
                            .setMessage("Are you sure you want to start a new game? All progress will be lost.")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .show()
                        true // Rückgabewert für diesen Zweig
                    }

                    R.id.homeFragment -> {
                        Log.i("NavDrawer_Home_Kotlin", "Manually navigating to HomeFragment.")
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(navController.graph.startDestinationId, false, true) // navController.graph statt getGraph()
                            .setLaunchSingleTop(true)
                            .build()
                        try {
                            navController.navigate(R.id.homeFragment, null, navOptions)
                        } catch (e: IllegalArgumentException) {
                            Log.w("NavDrawer_Home_Kotlin", "Already at Home or cannot pop to Home: ${e.message}")
                            if (navController.currentDestination == null || navController.currentDestination?.id != R.id.homeFragment) {
                                navController.navigate(R.id.homeFragment) // Fallback
                            }
                        }
                        if (drawerLayout?.isDrawerOpen(binding!!.navView) == true) {
                            drawerLayout?.closeDrawer(binding!!.navView)
                            Log.d("NavDrawer_Home_Kotlin", "Drawer closed for Home navigation.")
                        }
                        true // Rückgabewert
                    }

                    // Für die kombinierten Fälle brauchen wir eine etwas andere Struktur im when,
                    // oder wir behandeln sie innerhalb eines 'else'-Zweiges des when(id)
                    // oder wir prüfen 'id' und 'currentDestinationId' zusammen.
                    // Hier eine Möglichkeit, es im 'else' zu verschachteln oder als separate Bedingung danach:

                    // Hier würde normalerweise der nächste case für 'id' kommen.
                    // Da die Bedingung aber auch 'currentDestinationId' beinhaltet,
                    // passt es nicht direkt in das 'when (id)'
                    // Wir können es so machen:
                    else -> { // Dieser else-Zweig behandelt alle anderen IDs
                        if (currentDestinationId == R.id.buyingFragment &&
                            (id == R.id.purchasesFragment || id == R.id.tipsFragment || id == R.id.aboutFragment || id == R.id.settingsFragment)) {
                            // SPEZIALFALL von BuyingFragment
                            Log.i("NavDrawer_Special_Kotlin", "Manually navigating from BuyingFragment to ${menuItem.title}")
                            navController.navigate(id)

                            if (drawerLayout?.isDrawerOpen(binding!!.navView) == true) {
                                drawerLayout?.closeDrawer(binding!!.navView)
                                Log.d("NavDrawer_Special_Kotlin", "Drawer closed for ${menuItem.title} from BuyingFragment.")
                            }
                            true // Rückgabewert für diesen Spezialfall
                        } else {
                            // STANDARD NavigationUI Verhalten
                            val handled = onNavDestinationSelected(menuItem, navController)
                            Log.d("NavDrawer_Kotlin", "NavigationUI.onNavDestinationSelected for ${menuItem.title} handled: $handled")

                            if (handled) {
                                navController.currentDestination?.let { dest ->
                                    Log.d("NavDrawer_Kotlin", "Navigated to destination: ${getLabelOrId(dest)} (ID: ${Integer.toHexString(dest.id)})")
                                } ?: Log.d("NavDrawer_Kotlin", "Navigated, but current destination is null.")
                            } else {
                                val graphIdHex = navController.graph?.id?.let { Integer.toHexString(it) } ?: "null"
                                Log.w("NavDrawer_Kotlin", "Item '${menuItem.title}' (ID: $id) NOT handled by NavigationUI. Current NavController graph: $graphIdHex")
                            }

                            if (drawerLayout?.isDrawerOpen(binding!!.navView) == true) {
                                if (handled) {
                                    drawerLayout?.closeDrawer(binding!!.navView)
                                    Log.d("NavDrawer_Kotlin", "Drawer closed for handled item: ${menuItem.title}")
                                } else {
                                    Log.w("NavDrawer_Kotlin", "Drawer NOT closed for unhandled item: ${menuItem.title}")
                                }
                            }
                            handled // Rückgabewert für den Standardfall
                        }
                    }
                }
            } ?: Log.e("MainActivity_Kotlin", "NavView oder Binding ist null, Listener nicht gesetzt.")
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