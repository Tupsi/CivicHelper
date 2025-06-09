package org.tesira.civic

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.tesira.civic.db.CivicViewModel

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

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
    }

    private fun updateCustomSelectionPreferenceVisibility() {
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
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == CivicViewModel.PREF_KEY_HEART) {
            updateCustomSelectionPreferenceVisibility()
        }
    }
}
