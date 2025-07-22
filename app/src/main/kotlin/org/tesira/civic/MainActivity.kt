package org.tesira.civic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import org.tesira.civic.databinding.ActivityMainBinding
import org.tesira.civic.db.CivicViewModel
import org.tesira.civic.utils.applyHorizontalSystemBarInsetsAsPadding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var drawerLayout: DrawerLayout? = null
    private lateinit var navController: NavController
    private val mCivicViewModel: CivicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(getColor(R.color.md_theme_inversePrimary), getColor(R.color.md_theme_inversePrimary)),
            navigationBarStyle = SystemBarStyle.auto(getColor(R.color.md_theme_inversePrimary), getColor(R.color.md_theme_inversePrimary))
        )
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        invalidateOptionsMenu()

        binding.root.applyHorizontalSystemBarInsetsAsPadding()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as? NavHostFragment

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
                    R.id.inventoryFragment,
                    R.id.allCardsFragment,
                    R.id.tipsFragment,
                    R.id.settingsFragment,
                    R.id.aboutFragment
                )
            )
            setupActionBarWithNavController(this, navController, appBarConfiguration)
        }

        mCivicViewModel.showNewGameOptionsDialogEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                displayNewGameOptionsDialog()
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
//            showNewGameDialog()
            mCivicViewModel.triggerNewGameOptionsDialog()
            return true
        }

        if (::navController.isInitialized) {
            try {
                return onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
            } catch (e: Exception) {
                Log.e("MainActivity", "Navigation error", e)
                Toast.makeText(this, "Navigation failed. Please try again.", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            }
        } else {
            Log.w("MainActivity", "navController is not initialized.")
            return super.onOptionsItemSelected(item)
        }
    }

    private fun displayNewGameOptionsDialog() {
        // Stelle sicher, dass der Drawer geschlossen ist, falls er offen war
        // zur Zeit sinnfrei, weil wir keinen Drawer nutzen
        //if (drawerLayout?.isDrawerOpen(binding.navView) == true) {
        //    binding.navView.let { drawerLayout?.closeDrawer(it) }
        //}

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_game_options, null)

        // UI-Elemente aus dem Dialog-Layout referenzieren
        val radioGroupPlayerCount = dialogView.findViewById<RadioGroup>(R.id.rg_player_count)
        val radioGroupAstVersion = dialogView.findViewById<RadioGroup>(R.id.rg_ast_version)
        val spinnerCivilization = dialogView.findViewById<Spinner>(R.id.spinner_civilization)

        // --- 1. Zivilisations-Spinner befüllen ---
        val civEntries = resources.getStringArray(R.array.civilizations_entries)
        val civValues = resources.getStringArray(R.array.civilizations_values)
        val civAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, civEntries)
        civAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCivilization.adapter = civAdapter

        // --- 2. UI-Elemente mit aktuellen Werten vorselektieren ---
        // Hole die SharedPreferences-Instanz direkt vom ViewModel
        val currentPrefs = mCivicViewModel.defaultPrefs

        // Spieleranzahl vorselektieren
        val currentPlayerCountSetting = currentPrefs.getString(
            CivicViewModel.PREF_KEY_PLAYER_COUNT,
            CivicViewModel.PLAYER_COUNT_5_PLUS // Default-Wert aus ViewModel
        )
        when (currentPlayerCountSetting) {
            CivicViewModel.PLAYER_COUNT_3 -> radioGroupPlayerCount.check(R.id.rb_players_3)
            CivicViewModel.PLAYER_COUNT_4 -> radioGroupPlayerCount.check(R.id.rb_players_4)
            else -> radioGroupPlayerCount.check(R.id.rb_players_5_plus)
        }

        // AST-Version vorselektieren
        val currentAstVersion = currentPrefs.getString(
            CivicViewModel.PREF_KEY_AST,
            CivicViewModel.AST_BASIC
        )
        if (currentAstVersion == CivicViewModel.AST_EXPERT) {
            radioGroupAstVersion.check(R.id.rb_ast_expert)
        } else {
            radioGroupAstVersion.check(R.id.rb_ast_basic)
        }

        // Zivilisation vorselektieren
        // Verwende den ersten Wert aus civValues als Fallback, falls nichts in Prefs oder civValues leer ist.
        val defaultCivValue = civValues.firstOrNull()
        val currentCivilizationValue = currentPrefs.getString(CivicViewModel.PREF_KEY_CIVILIZATION, defaultCivValue)

        var currentCivIndex = 0 // Default zur ersten Zivilisation
        if (currentCivilizationValue != null) {
            currentCivIndex = civValues.indexOf(currentCivilizationValue).takeIf { it != -1 } ?: 0
        }
        spinnerCivilization.setSelection(currentCivIndex)

        // --- 3. AlertDialog erstellen und anzeigen ---
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.new_game_dialog_title)) // Temporär hardcoded, ersetze durch String-Ressource
            .setView(dialogView)
            .setPositiveButton(getString(R.string.start_button_text)) { dialog, _ ->
                // --- 4. Ausgewählte Werte lesen und in SharedPreferences speichern ---

                val newPlayerCount = when (radioGroupPlayerCount.checkedRadioButtonId) {
                    R.id.rb_players_3 -> CivicViewModel.PLAYER_COUNT_3
                    R.id.rb_players_4 -> CivicViewModel.PLAYER_COUNT_4
                    else -> CivicViewModel.PLAYER_COUNT_5_PLUS
                }

                val newAstVersion = if (radioGroupAstVersion.checkedRadioButtonId == R.id.rb_ast_expert) {
                    CivicViewModel.AST_EXPERT
                } else {
                    CivicViewModel.AST_BASIC
                }

                val selectedCivIndex = spinnerCivilization.selectedItemPosition
                val newCivilizationValue = if (civValues.isNotEmpty() && selectedCivIndex < civValues.size) {
                    civValues[selectedCivIndex]
                } else {
                    civValues.firstOrNull()
                }

                currentPrefs.edit {
                    putString(CivicViewModel.PREF_KEY_PLAYER_COUNT, newPlayerCount)
                    putString(CivicViewModel.PREF_KEY_AST, newAstVersion)
                    if (newCivilizationValue != null) {
                        putString(CivicViewModel.PREF_KEY_CIVILIZATION, newCivilizationValue)
                    }
                }

                // --- 5. Neuen Spielprozess im ViewModel starten ---
                mCivicViewModel.startNewGameProcess()
                navController.navigate(R.id.homeFragment)
                dialog.dismiss()
                val snackbar = Snackbar.make(binding.root, "Cleared data from old game.\nGood luck and have fun!", Snackbar.LENGTH_SHORT)
                snackbar.show()
            }
            .setNegativeButton(getString(R.string.cancel_button_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}