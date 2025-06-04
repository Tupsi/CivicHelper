package org.tesira.civic

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import org.tesira.civic.databinding.ActivityMainBinding
import org.tesira.civic.db.CivicViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: DrawerLayout? = null
    private lateinit var navController: NavController
    private val mCivicViewModel: CivicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        invalidateOptionsMenu()

        val initialPaddingLeft = toolbar.paddingLeft
//        val initialPaddingTopFromXml = toolbar.paddingTop
        val initialPaddingRight = toolbar.paddingRight
        val initialPaddingBottom = toolbar.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val newPaddingTop = systemBars.top // Standardmäßig nur die Insets für oben

            view.setPadding(
                initialPaddingLeft + systemBars.left,
                newPaddingTop,
                initialPaddingRight + systemBars.right,
                initialPaddingBottom
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.myNavHostFragment) as? NavHostFragment

        if (navHostFragment == null) {
            Log.e(
                "NavDrawer",
                "NavHostFragment nicht gefunden! App-Navigation wird nicht funktionieren."
            )
        } else {
            navController = navHostFragment.navController
            drawerLayout = binding.drawerLayout
            drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.homeFragment,
                    R.id.buyingFragment,
                    R.id.purchasesFragment,
                    R.id.tipsFragment,
                    R.id.settingsFragment,
                    R.id.aboutFragment
                )
            )
            setupActionBarWithNavController(this, navController, appBarConfiguration)
        }

        mCivicViewModel.navigateToCivilizationSelectionEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(
                    this@MainActivity,
                    "New game created. Select your civilization!",
                    Toast.LENGTH_SHORT
                ).show()
                drawerLayout?.closeDrawers()
                showCivilizationSelectionDialog()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        mCivicViewModel.saveData()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_newGame) {
            showNewGameDialog()
            return true
        }
        return onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    private fun showNewGameDialog() {
        if (drawerLayout?.isDrawerOpen(binding.navView) == true) {
            binding.navView.let { drawerLayout?.closeDrawer(it) }
        }
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> mCivicViewModel.startNewGameProcess()
                    DialogInterface.BUTTON_NEGATIVE -> { /* No button clicked - tu nichts */
                    }
                }
            }
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to start a new game? All progress will be lost.")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener)
            .show()
    }

    private fun showCivilizationSelectionDialog() {
        val civDisplayNames = resources.getStringArray(R.array.civilizations_entries)
        val civValues = resources.getStringArray(R.array.civilizations_values)

        if (civDisplayNames.isEmpty() || civValues.isEmpty() || civDisplayNames.size != civValues.size) {
            Toast.makeText(this, "Error: Civilization data not configured.", Toast.LENGTH_LONG)
                .show()
            mCivicViewModel.setCivNumber("not set") // Oder einen Default aus civValues
            navController.navigate(R.id.homeFragment)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Select Your Civilization")
            .setItems(civDisplayNames) { dialog, which ->
                val selectedCivilizationValue = civValues[which]
                val selectedCivilizationDisplayName = civDisplayNames[which]

                mCivicViewModel.setCivNumber(selectedCivilizationValue)
                Toast.makeText(
                    this,
                    "$selectedCivilizationDisplayName selected",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(R.id.homeFragment)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(this, "No civilization selected, using default.", Toast.LENGTH_SHORT)
                    .show()
                val defaultCivValue = if (civValues.isNotEmpty()) civValues[0] else "not set"
                mCivicViewModel.setCivNumber(defaultCivValue)
                navController.navigate(R.id.homeFragment)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}