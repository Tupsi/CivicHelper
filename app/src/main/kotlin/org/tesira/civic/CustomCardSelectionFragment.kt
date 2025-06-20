package org.tesira.civic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.tesira.civic.databinding.FragmentCustomCardSelectionBinding
import org.tesira.civic.db.CivicViewModel

class CustomCardSelectionFragment : Fragment() {

    private var _binding: FragmentCustomCardSelectionBinding? = null
    private val binding get() = _binding!!
    private val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var adapter: CustomCardSelectionAdapter
    private var currentSelectableItems: MutableList<SelectableCardItem> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomCardSelectionBinding.inflate(inflater, container, false)
//        binding.root.applyHorizontalSystemBarInsetsAsPadding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModelData()

        binding.buttonSaveCustomSelection.setOnClickListener {
            civicViewModel.saveSelection(currentSelectableItems)
            Toast.makeText(requireContext(), getString(R.string.selection_saved), Toast.LENGTH_SHORT).show()
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
        civicViewModel.allCardsUnsortedOnce.observe(viewLifecycleOwner) { cardsWithDetails ->
            if (cardsWithDetails.isNullOrEmpty()) {
                binding.textViewCustomSelectionPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewCustomCards.visibility = View.GONE
                currentSelectableItems.clear()
                adapter.submitList(emptyList())
            } else {
                binding.textViewCustomSelectionPlaceholder.visibility = View.GONE
                binding.recyclerViewCustomCards.visibility = View.VISIBLE

                val loadedSelectedCardNames = civicViewModel.loadSelectedCardNames()
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

    private fun clearSelection() {
        // Iteriere durch die currentSelectableItems und setze isSelected auf false
        val clearedItems = currentSelectableItems.map { it.copy(isSelected = false) }.toMutableList()
        currentSelectableItems = clearedItems
        adapter.submitList(currentSelectableItems.toList())
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