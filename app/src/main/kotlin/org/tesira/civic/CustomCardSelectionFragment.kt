package org.tesira.civic

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import org.tesira.civic.databinding.FragmentCustomCardSelectionBinding
import org.tesira.civic.db.CivicViewModel

class CustomCardSelectionFragment : Fragment() {

    private var _binding: FragmentCustomCardSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CivicViewModel by activityViewModels()
    private lateinit var adapter: CustomCardSelectionAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSelectableItems: MutableList<SelectableCardItem> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomCardSelectionBinding.inflate(inflater, container, false)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Speichere die ursprünglichen Padding-Werte der View, auf die die Insets angewendet werden
        val initialPaddingLeft = binding.root.paddingLeft
        val initialPaddingTop = binding.root.paddingTop
        val initialPaddingRight = binding.root.paddingRight
        val initialPaddingBottom = binding.root.paddingBottom


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Wende die Systemleisten-Insets zusätzlich zum ursprünglichen Padding an
            v.setPadding(
                initialPaddingLeft + systemBarInsets.left,
                initialPaddingTop,
                initialPaddingRight + systemBarInsets.right,
                initialPaddingBottom + systemBarInsets.bottom
            )
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModelData()

        binding.buttonSaveCustomSelection.setOnClickListener {
            saveSelection()
            Toast.makeText(requireContext(), getString(R.string.selection_saved), Toast.LENGTH_SHORT).show()
            // Optional: Zurück navigieren oder eine Bestätigung anzeigen
            parentFragmentManager.popBackStack()
        }
        binding.buttonClearCustomSelection.setOnClickListener {
            clearSelection()
            Toast.makeText(requireContext(), getString(R.string.selection_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = CustomCardSelectionAdapter { clickedItem ->
            val index = currentSelectableItems.indexOfFirst { it.cardName == clickedItem.cardName }
            if (index != -1) {
                val itemToUpdate = currentSelectableItems[index]
                val updatedItem = itemToUpdate.copy(isSelected = !itemToUpdate.isSelected)
                currentSelectableItems[index] = updatedItem
                adapter.submitList(currentSelectableItems.toList())
            }
        }
        binding.recyclerViewCustomCards.adapter = adapter
    }

    private fun observeViewModelData() {
        viewModel.allCardsWithDetails.observe(viewLifecycleOwner) { cardsWithDetails ->
            if (cardsWithDetails.isNullOrEmpty()) {
                binding.textViewCustomSelectionPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewCustomCards.visibility = View.GONE
                currentSelectableItems.clear()
                adapter.submitList(emptyList())
            } else {
                binding.textViewCustomSelectionPlaceholder.visibility = View.GONE
                binding.recyclerViewCustomCards.visibility = View.VISIBLE

                val loadedSelectedCardNames = loadSelectedCardNames()

                // Sortiere die Karten nach Namen
                val sortedCards = cardsWithDetails.sortedBy { it.card.name }

                currentSelectableItems = sortedCards.map { cardDetail ->
                    SelectableCardItem(
                        cardName = cardDetail.card.name, // Speichere den Namen
                        isSelected = loadedSelectedCardNames.contains(cardDetail.card.name)
                    )
                }.toMutableList()
                adapter.submitList(currentSelectableItems.toList()) // .toList() für eine neue Instanz
            }
        }
    }

    private fun loadSelectedCardNames(): Set<String> {
        return sharedPreferences.getStringSet(CivicViewModel.PREF_KEY_CUSTOM_HEART_CARDS, emptySet()) ?: emptySet()
    }

    private fun saveSelection() {
        val selectedNames = currentSelectableItems
            .filter { it.isSelected }
            .map { it.cardName }
            .toSet()

        sharedPreferences.edit { putStringSet(CivicViewModel.PREF_KEY_CUSTOM_HEART_CARDS, selectedNames) }
        viewModel.customHeartSettingsUpdated()
    }

    private fun clearSelection() {
        // Iteriere durch die currentSelectableItems und setze isSelected auf false
        val clearedItems = currentSelectableItems.map {
            it.copy(isSelected = false) // Erstelle neue Instanzen mit isSelected = false
        }.toMutableList()

        currentSelectableItems = clearedItems // Aktualisiere die Hauptliste
        adapter.submitList(currentSelectableItems.toList()) // UI aktualisieren mit einer neuen Listeninstanz

        // Optional: Toast-Nachricht
        // Toast.makeText(requireContext(), getString(R.string.selection_cleared), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = ""
    }

}