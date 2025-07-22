package org.tesira.civic

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.tesira.civic.db.CivicViewModel

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val civicViewModel: CivicViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val customCardSelectionPreference: Preference? = findPreference(CivicViewModel.PREF_KEY_CUSTOM_HEART_CARDS)
        customCardSelectionPreference?.setOnPreferenceClickListener {
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_customCardSelectionFragment)
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Navigation to CustomCardSelectionFragment failed.", e)
            }
            true
        }
        updateCustomSelectionPreferenceVisibility()

        val playerCountPreference: ListPreference? = findPreference(CivicViewModel.PREF_KEY_PLAYER_COUNT)

        playerCountPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val oldValue = preference.sharedPreferences?.getString(preference.key, "")
                if (oldValue != newValue) {
                    showPlayerCountChangeConfirmationDialog {
                        preference.sharedPreferences?.edit {
                            putString(preference.key, newValue.toString())
                        }
                        civicViewModel.startNewGameProcess()
                        findNavController().navigate(R.id.homeFragment)
                    }
                    return@OnPreferenceChangeListener false
                }
                true
            }
    }

    private fun updateCustomSelectionPreferenceVisibility() {
        if (!isAdded) return
        val heartCategoryPreference: ListPreference? = findPreference(CivicViewModel.PREF_KEY_HEART)
        val customCardSelectionPreference: Preference? = findPreference(CivicViewModel.PREF_KEY_CUSTOM_HEART_CARDS)

        if (heartCategoryPreference == null || customCardSelectionPreference == null) {
            Log.w(
                "SettingsFragment",
                "Could not find one or both preferences for visibility update. HeartKey: ${CivicViewModel.PREF_KEY_HEART}, CustomNavKey: $CivicViewModel.PREF_KEY_CUSTOM_HEART_CARDS"
            )
            return
        }

        val currentHeartCategory = heartCategoryPreference.value
        val isCustomCategorySelected = currentHeartCategory == "custom" // "custom" ist der value in arrays.xml für die Custom-Option

        customCardSelectionPreference.isVisible = isCustomCategorySelected
        if (isCustomCategorySelected) {
            customCardSelectionPreference.summary = getString(R.string.summary_select_custom_cards)
        } else {
            customCardSelectionPreference.summary = getString(R.string.summary_select_custom_cards_disabled_hint)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == CivicViewModel.PREF_KEY_HEART) {
            updateCustomSelectionPreferenceVisibility()
        }
    }

    private fun showPlayerCountChangeConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Player Count Change")
            .setMessage("Changing the number of players will start a new game and your current progress will be lost. Are you sure you want to continue?")
            .setPositiveButton("Yes, Start New Game") { dialog, which ->
                onConfirm()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User cancelled. Do nothing. The preference value remains unchanged.
                // The ListPreference will revert to showing the old value.
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
