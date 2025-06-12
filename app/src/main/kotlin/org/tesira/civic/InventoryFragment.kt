package org.tesira.civic

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
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.FragmentAllCardsBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CivicViewModel

/**
 * Shows all bought civilization advances (cards).
 */
class InventoryFragment : Fragment() {
    internal val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var _binding: FragmentAllCardsBinding
    private val binding get() = _binding
    private lateinit var allCardsAdapter: AllCardsAdapter
    private lateinit var sortingOptionsValues: Array<String>
    private lateinit var sortingOptionsNames: Array<String>
    private var sortOrderChangedByUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllCardsBinding.inflate(inflater, container, false)
        val rootView: View = binding.root
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom
        sortingOptionsValues = resources.getStringArray(R.array.sort_values)
        sortingOptionsNames = resources.getStringArray(R.array.sort_entries)

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView,
            OnApplyWindowInsetsListener { v: View, windowInsets: WindowInsetsCompat ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
                )
                windowInsets
            })

        // ViewCompat.requestApplyInsets(rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSortButton()
        setupSearchInput()
        registerForContextMenu(binding.btnSortAllCards)
    }


    private fun setupRecyclerView() {
        // Initialisiere den Adapter
        allCardsAdapter = AllCardsAdapter()

        binding.recyclerViewAllCards.apply {
            val actualColumnCount = civicViewModel.calculateColumnCount(context)
            if (actualColumnCount <= 1) {
                layoutManager = LinearLayoutManager(context)
            } else {
                layoutManager = GridLayoutManager(context, actualColumnCount)
            }
//            layoutManager = LinearLayoutManager(context)
            adapter = allCardsAdapter
            // Optional: ItemDecoration für Abstände zwischen Elementen
            if (itemDecorationCount == 0) {
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
        }
    }

    private fun observeViewModel() {

        civicViewModel.showCredits.observe(viewLifecycleOwner) { isVisible ->
            allCardsAdapter.setShowCredits(isVisible)
        }

        civicViewModel.allPurchasedCardsWithDetails.observe(viewLifecycleOwner) { cards -> // Keine Notwendigkeit für expliziten Observer-Typ
            if (cards.isNullOrEmpty()) {
                binding.textViewPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewAllCards.visibility = View.GONE
                // NEU: Platzhaltertext basierend auf Suche anpassen
                if (civicViewModel.searchQuery.value.isNullOrBlank()) {
                    binding.textViewPlaceholder.text = getString(R.string.placeholder_no_cards_available) // "Keine Karten gefunden."
                } else {
                    binding.textViewPlaceholder.text = getString(R.string.placeholder_no_search_results_found) // "Keine Suchergebnisse."
                }
                allCardsAdapter.submitList(emptyList()) // Wichtig, um den Adapter zu leeren
            } else {
                binding.textViewPlaceholder.visibility = View.GONE
                binding.recyclerViewAllCards.visibility = View.VISIBLE

                allCardsAdapter.submitList(cards) {
                    binding.recyclerViewAllCards.scrollToPosition(0)
//                    Log.d("AllCardsFragment", "Scroll to top triggered")
//                    Log.d("AllCardsFragment", "Current list size: ${cards.size}")
//                    Log.d("AllCardsFragment", "sortOderChanged: $sortOrderChangedByUser")
//                    Log.d("AllCardsFragment", "binding.recyclerViewAllCards.isVisible: ${binding.recyclerViewAllCards.isVisible}")
                    // Scroll zum Anfang, wenn die Sortierung durch den Benutzer geändert wurde

                    // zu kompliziert gedacht, sortOrderChangedByUser kann wohl raus
//                    if (sortOrderChangedByUser && binding.recyclerViewAllCards.isVisible && cards.isNotEmpty()) {
//                        binding.recyclerViewAllCards.scrollToPosition(0)
//                        sortOrderChangedByUser = false
//                    }
                    // Kein automatisches Scrollen zum Anfang bei reiner Suchtextänderung
                }
            }
        }
        civicViewModel.currentSortingOrder.observe(viewLifecycleOwner, Observer { order ->
            if (order != null) {
                updateSortButtonText(order)
            }
        })

        civicViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            // Verhindere eine Endlosschleife, wenn der Text programmatisch gesetzt wird
            if (binding.editTextSearchAllCards.text.toString() != query) {
                binding.editTextSearchAllCards.setText(query)
                // Optional: Cursor ans Ende setzen, besonders wenn der Text programmatisch aktualisiert wird
                query?.length?.let { binding.editTextSearchAllCards.setSelection(it) }
            }
        }
    }

    private fun setupSearchInput() {
        binding.editTextSearchAllCards.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nicht benötigt
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nicht benötigt für Live-Suche beim Tippen
            }

            override fun afterTextChanged(s: Editable?) {
                val searchQuery = s.toString()
                civicViewModel.setSearchQuery(searchQuery)
            }
        })

        binding.editTextSearchAllCards.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            // Prüfe, ob die Aktionstaste gedrückt wurde (kann auch über 'event' geprüft werden, aber actionId ist oft zuverlässiger)
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                // Manchmal auch IME_ACTION_GO, je nach Tastatur und EditText-Konfiguration
                (event != null && event.action == android.view.KeyEvent.ACTION_DOWN && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)
            ) {

                // Verstecke die Tastatur
                hideKeyboard(v)
                // Optional: Fokus vom EditText entfernen, damit der Cursor nicht mehr blinkt
                v.clearFocus()

                // Die Suche selbst wird bereits durch den TextWatcher ausgelöst.
                // Wenn du hier explizit eine Suche auslösen müsstest (z.B. wenn kein Live-Search),
                // dann hier: civicViewModel.setSearchQuery(v.text.toString())

                return@OnEditorActionListener true // Ereignis wurde behandelt
            }
            return@OnEditorActionListener false // Ereignis nicht behandelt, Standardverhalten ausführen
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
        sortOrderChangedByUser = true
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
//        _binding = null
    }

    companion object {
        private const val MENU_ID_SORT_OPTION_OFFSET = 2000
    }
}