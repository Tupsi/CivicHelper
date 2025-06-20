package org.tesira.civic.utils

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.tesira.civic.AllCardsAdapter
import org.tesira.civic.R
import org.tesira.civic.databinding.FragmentAllCardsBinding
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.db.CivicViewModel

abstract class BaseCardListFragment : Fragment() {

    // ViewModel
    internal val civicViewModel: CivicViewModel by activityViewModels()

    // View Binding
    private var _binding: FragmentAllCardsBinding? = null
    protected val binding: FragmentAllCardsBinding get() = _binding!!

    // Adapter
    protected lateinit var allCardsAdapter: AllCardsAdapter

    // Sortierung
    private lateinit var sortingOptionsValues: Array<String>
    private lateinit var sortingOptionsNames: Array<String>

    // --- Abstrakte Methode für die Datenquelle ---
    /**
     * Muss von Unterklassen implementiert werden, um die spezifische LiveData-Quelle
     * für die Kartenliste bereitzustellen.
     */
    abstract fun getCardsLiveData(): LiveData<List<CardWithDetails>>
    abstract fun getButton(): View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllCardsBinding.inflate(inflater, container, false)
        sortingOptionsValues = resources.getStringArray(R.array.sort_values)
        sortingOptionsNames = resources.getStringArray(R.array.sort_entries)
        setupInsets(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModelData()
        setupSortButton()
        setupSearchInput()
        registerForContextMenu(getButton())
    }

    private fun setupInsets(rootView: View) {
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPaddingLeft + systemBarInsets.left,
                initialPaddingTop,
                initialPaddingRight + systemBarInsets.right,
                initialPaddingBottom + systemBarInsets.bottom
            )
            windowInsets
        }
    }

    private fun setupRecyclerView() {
        allCardsAdapter = AllCardsAdapter()

        binding.recyclerViewAllCards.apply {
            val actualColumnCount = civicViewModel.calculateColumnCount(requireContext())
            layoutManager = if (actualColumnCount <= 1) {
                LinearLayoutManager(requireContext())
            } else {
                GridLayoutManager(requireContext(), actualColumnCount)
            }
            adapter = allCardsAdapter
        }
    }

    private fun observeViewModelData() {
        // Gemeinsame Observer
        civicViewModel.showCredits.observe(viewLifecycleOwner) { isVisible ->
            allCardsAdapter.setShowCredits(isVisible)
        }

        civicViewModel.showInfos.observe(viewLifecycleOwner) { isVisible ->
            allCardsAdapter.setShowInfos(isVisible)
        }

        civicViewModel.currentSortingOrder.observe(viewLifecycleOwner) { order ->
            if (order != null) {
                updateSortButtonText(order)
            }
        }

        civicViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (binding.editTextSearchAllCards.text.toString() != query) {
                binding.editTextSearchAllCards.setText(query)
                query?.length?.let { binding.editTextSearchAllCards.setSelection(it) }
            }
        }

        // Spezifische Kartenliste über die abstrakte Methode beobachten
        getCardsLiveData().observe(viewLifecycleOwner) { cards ->
            if (cards.isNullOrEmpty()) {
                binding.textViewPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewAllCards.visibility = View.GONE
                if (civicViewModel.searchQuery.value.isNullOrBlank()) {
                    binding.textViewPlaceholder.text = getString(R.string.placeholder_no_cards_available)
                } else {
                    binding.textViewPlaceholder.text = getString(R.string.placeholder_no_search_results_found)
                }
                allCardsAdapter.submitList(emptyList())
            } else {
                binding.textViewPlaceholder.visibility = View.GONE
                binding.recyclerViewAllCards.visibility = View.VISIBLE
                allCardsAdapter.submitList(cards) {
                    if (isAdded && view != null && _binding != null && binding.recyclerViewAllCards.isVisible && cards.isNotEmpty()) {
                        binding.recyclerViewAllCards.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun setupSearchInput() {
        binding.editTextSearchAllCards.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                civicViewModel.setSearchQuery(s.toString())
            }
        })

        binding.editTextSearchAllCards.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == android.view.KeyEvent.ACTION_DOWN && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)
            ) {
                hideKeyboard(v)
                v.clearFocus()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupSortButton() {
        civicViewModel.currentSortingOrder.value?.let { currentSortValue ->
            updateSortButtonText(currentSortValue)
        }
        binding.btnSortAllCards.setOnClickListener {
            cycleNextSortOrder()
        }
    }

    private fun cycleNextSortOrder() {
        val currentSortValue = civicViewModel.currentSortingOrder.value ?: sortingOptionsValues.first()
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
        binding.recyclerViewAllCards.adapter = null
        _binding = null
    }

    companion object {
        private const val MENU_ID_SORT_OPTION_OFFSET = 2000
    }
}