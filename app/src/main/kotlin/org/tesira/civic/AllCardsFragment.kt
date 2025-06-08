package org.tesira.civic

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.tesira.civic.databinding.FragmentAllCardsBinding
import org.tesira.civic.db.CivicViewModel

class AllCardsFragment : Fragment() {

    private var _binding: FragmentAllCardsBinding? = null
    private val binding get() = _binding!!
    private val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var allCardsAdapter: AllCardsAdapter
    private lateinit var sortingOptionsValues: Array<String>
    private lateinit var sortingOptionsNames: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCardsBinding.inflate(inflater, container, false)
        val rootView: View = binding.getRoot()
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom
        sortingOptionsValues = resources.getStringArray(R.array.sort_values)
        sortingOptionsNames = resources.getStringArray(R.array.sort_entries)

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView,
            OnApplyWindowInsetsListener { v: View?, windowInsets: WindowInsetsCompat? ->
                val systemBarInsets = windowInsets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
                )
                windowInsets
            })

        ViewCompat.requestApplyInsets(rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSortButton()
        registerForContextMenu(binding.btnSortAllCards)
    }


    private fun setupRecyclerView() {
        // Initialisiere den Adapter
        allCardsAdapter = AllCardsAdapter()

        binding.recyclerViewAllCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allCardsAdapter
            // Optional: ItemDecoration für Abstände zwischen Elementen
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun observeViewModel() {
        civicViewModel.allCardsWithDetails.observe(viewLifecycleOwner) { cards ->
            if (cards.isNotEmpty()) {
                binding.textViewPlaceholder.visibility = View.GONE
                binding.recyclerViewAllCards.visibility = View.VISIBLE

                // Aktualisiere den Adapter mit den neuen Daten
                allCardsAdapter.submitList(cards) {
                    if (binding.recyclerViewAllCards.isVisible && cards.isNotEmpty()) {
                        binding.recyclerViewAllCards.scrollToPosition(0)
                    }
                }

//                Log.d("AllCardsFragment", "Karten mit Details geladen und an Adapter übergeben: ${cards.size}")
            } else {
                binding.textViewPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewAllCards.visibility = View.GONE
//                binding.textViewPlaceholder.text = "Keine Karten gefunden."
//                Log.d("AllCardsFragment", "Keine Karten mit Details gefunden.")
            }
        }
        civicViewModel.currentSortingOrder.observe(viewLifecycleOwner, Observer { order ->
            if (order != null) {
                updateSortButtonText(order)
            }
        })
    }

    private fun setupSortButton() {
        civicViewModel.currentSortingOrder.value?.let { currentSortValue ->
            updateSortButtonText(currentSortValue)
        }
        civicViewModel.currentSortingOrder.observe(
            getViewLifecycleOwner(),
            Observer { order: String? ->
                if (order != null) {
                    updateSortButtonText(order)
                }
            })
        binding.btnSortAllCards.setOnClickListener {
            cycleNextSortOrder()
        }
    }

    private fun cycleNextSortOrder() {
        val currentSortValue =
            civicViewModel.currentSortingOrder.value ?: sortingOptionsValues.first()
        val currentIndex = sortingOptionsValues.indexOf(currentSortValue)
        val nextIndex = (currentIndex + 1) % sortingOptionsValues.size
        val nextSortValue = sortingOptionsValues[nextIndex]
        applyNewSortOrder(nextSortValue)
    }

    private fun applyNewSortOrder(sortValue: String) {
        civicViewModel.setSortingOrder(sortValue)
    }

    private fun updateSortButtonText(sortValue: String) {
        val index = sortingOptionsValues.indexOf(sortValue)
        if (index != -1 && index < sortingOptionsNames.size) {
            binding.btnSortAllCards.text = sortingOptionsNames[index]
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == binding.btnSortAllCards.id) {
            menu.setHeaderTitle(getString(R.string.sort_by))
            sortingOptionsNames.forEachIndexed { index, name ->
                menu.add(Menu.NONE, MENU_ID_SORT_OPTION_OFFSET + index, index, name)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selectedIndex = item.itemId - MENU_ID_SORT_OPTION_OFFSET
        if (selectedIndex >= 0 && selectedIndex < sortingOptionsValues.size) {
            val selectedSortValue = sortingOptionsValues[selectedIndex]
            applyNewSortOrder(selectedSortValue)
            return true
        }
        return super.onContextItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MENU_ID_SORT_OPTION_OFFSET = 2000
    }

}