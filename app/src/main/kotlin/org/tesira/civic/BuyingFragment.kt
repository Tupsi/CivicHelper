package org.tesira.civic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.FragmentBuyingBinding
import org.tesira.civic.db.CivicViewModel

/**
 * Fragment for the buy process. User can input a treasure sum and select up to that value
 * cards. Pressing the buy button finishes the buying process, adding selected cards to the
 * purchases list and returns the user back to the dashboard.
 */
class BuyingFragment : Fragment() {
    internal val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var tracker: SelectionTracker<String>
    private lateinit var binding: FragmentBuyingBinding
    private lateinit var buyingItemKeyProvider: BuyingItemKeyProvider
    private lateinit var recyclerView: RecyclerView
    private lateinit var layout: RecyclerView.LayoutManager
    private lateinit var adapter: BuyingAdapter

    private var treasureInput: EditText? = null
    private var remainingText: TextView? = null

    private var numberDialogs = 0
    private lateinit var sortingOptionsValues: Array<String>
    private lateinit var sortingOptionsNames: Array<String>
    private var savedSelectionState: Bundle? = null
    private var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var treasureInputFocusedOnStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortingOptionsValues = resources.getStringArray(R.array.sort_values)
        sortingOptionsNames = resources.getStringArray(R.array.sort_entries)

        parentFragmentManager.setFragmentResultListener(EXTRA_CREDITS_REQUEST_KEY, this) { _, _ ->
            numberDialogs--
            returnToDashboard()
        }

        parentFragmentManager.setFragmentResultListener(ANATOMY_REQUEST_KEY, this) { _, result ->
            numberDialogs--

            val selectedAnatomyCard = result.getString("selected_card_name")
            if ("Written Record" == selectedAnatomyCard) {
                civicViewModel.triggerExtraCreditsDialog(10)
            } else {
                returnToDashboard()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBuyingBinding.inflate(inflater, container, false)
        val rootView: View = binding.root
        recyclerView = binding.purchasableCards
//        binding.root.applyHorizontalSystemBarInsetsAsPadding()

        val actualColumnCount = civicViewModel.calculateColumnCount(rootView.context)

        if (actualColumnCount <= 1) {
            layout = LinearLayoutManager(rootView.context)
            recyclerView.layoutManager = layout
        } else {
            layout = GridLayoutManager(rootView.context, actualColumnCount)
            recyclerView.layoutManager = layout
        }

        adapter = BuyingAdapter()
        recyclerView.adapter = adapter
        treasureInput = binding.treasure
        remainingText = binding.moneyleft

        treasureInput?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Verzögere die Selektion leicht, um sicherzustellen, dass das EditText bereit ist
                view.post {
                    (view as? EditText)?.selectAll()
                }
            } else {
                civicViewModel.treasure.value = calculateInput(treasureInput!!.text.toString())
            }
        }

        civicViewModel.allPurchasableCardsWithDetails.observe(viewLifecycleOwner) { cards ->
            adapter.submitList(cards) {
                if ((binding.purchasableCards.adapter?.itemCount ?: 0) > 0) { // Zusätzliche Prüfung, ob die Liste nicht leer ist
                    binding.purchasableCards.scrollToPosition(0)
                }
            }
            if (savedSelectionState != null) {
                tracker.onRestoreInstanceState(savedSelectionState)
                savedSelectionState = null
            }
        }

        civicViewModel.currentSortingOrder.observe(viewLifecycleOwner) { order ->
            if (order != null) {
                updateSortButtonText(order)
            }
        }

        if (savedInstanceState != null) {
            savedSelectionState = savedInstanceState
        }

        // close SoftKeyboard on Enter
        treasureInput?.setOnEditorActionListener(TextView.OnEditorActionListener { v, keyCode, event ->
            if (event == null || event.action == KeyEvent.ACTION_UP || keyCode == KeyEvent.KEYCODE_ENTER) { // Sicherstellen, dass es ein "UP"-Event ist oder nur Enter ohne Event
                civicViewModel.treasure.value = calculateInput(treasureInput!!.text.toString())
                if (civicViewModel.remaining.value!! < 0) {
                    tracker.clearSelection()
                }
                // Tastatur verstecken
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(treasureInput!!.windowToken, 0)

                // Fokus entfernen
                treasureInput!!.clearFocus() // Wichtig: Fokus explizit entfernen
                binding.root.requestFocus() // Optional: Fokus auf ein anderes Element lenken (z.B. das Root-Layout)
                return@OnEditorActionListener true
            }
            false
        })

        recyclerView.setOnTouchListener { v, _ ->
            treasureInput?.clearFocus()
            binding.root.requestFocus()

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(treasureInput?.windowToken, 0)
            v.performClick()
            false
        }

        // button which finalizes the buy process
        binding.btnBuy.setOnClickListener(View.OnClickListener btnBuyClickListener@{ v: View? ->
            if (tracker.selection.isEmpty) {
                showToast(getString(R.string.no_cards_selected))
                return@btnBuyClickListener
            }

            civicViewModel.clearRecentlyPurchasedCards()

            val selectedCardNames: MutableList<String> = ArrayList()
            for (name in tracker.selection) {
                selectedCardNames.add(name)
            }

            civicViewModel.setRecentlyPurchasedCards(selectedCardNames)

            for (name in selectedCardNames) {
                civicViewModel.addBonus(name)
            }
            civicViewModel.saveBonus()
            civicViewModel.processPurchases(selectedCardNames)
        })

        // button to clear the current selection of cards
        binding.btnClear.setOnClickListener { v: View? ->
            tracker.clearSelection()
            civicViewModel.clearCurrentSelectionState()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.btnSort)
        civicViewModel.getShowAnatomyDialogEvent().observe(viewLifecycleOwner) { event ->
            val anatomyCardsToShow = event.getContentIfNotHandled()
            if (!anatomyCardsToShow.isNullOrEmpty()) {
                numberDialogs++
                DialogAnatomyFragment.newInstance(anatomyCardsToShow)
                    .show(parentFragmentManager, "Anatomy")
            }
        }

        binding.btnSort.setOnClickListener {
            cycleNextSortOrder()
        }
        civicViewModel.currentSortingOrder.value?.let { currentSortValue ->
            updateSortButtonText(currentSortValue)
        }

        civicViewModel.showExtraCreditsDialogEvent.observe(viewLifecycleOwner) { event ->
            val extraCredits = event.getContentIfNotHandled()
            if (extraCredits != null && extraCredits > 0) {
                numberDialogs++
                DialogExtraCreditsFragment.newInstance(extraCredits)
                    .show(parentFragmentManager, "ExtraCredits")
            }
        }

        civicViewModel.navigateToDashboardEvent.observe(viewLifecycleOwner) { event ->
            if (event.getContentIfNotHandled() == true) {
                returnToDashboard()
            }
        }

        buyingItemKeyProvider = BuyingItemKeyProvider(adapter)
        tracker = SelectionTracker.Builder<String>(
            "my-selection-id",
            recyclerView,
            buyingItemKeyProvider,
            BuyingItemDetailsLookup(recyclerView),
            StorageStrategy.createStringStorage()
        )
            .withSelectionPredicate(BuyingSelectionPredicate(adapter, civicViewModel))
            .build()

        adapter.setSelectionTracker(tracker)
        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onItemStateChanged(key: String, selected: Boolean) {
                super.onItemStateChanged(key, selected)

                civicViewModel.calculateTotal(tracker.selection)
                val isFinalizing = civicViewModel.isFinalizingPurchase.value == true

                if (!isFinalizing && key == "Library") {
                    if (selected) {
                        // Library was just selected
                        if (!civicViewModel.librarySelected) { // Only add if it wasn't already selected
                            civicViewModel.librarySelected = true
                            // Add the temporary treasure bonus
                            civicViewModel.treasure.value =
                                (civicViewModel.treasure.value ?: 0) + 40
                            showToast("$key selected, temporary adding 40 treasure.")
                        }
                    } else {
                        // Library was just deselected
                        if (civicViewModel.librarySelected) { // Only remove if it was previously selected
                            civicViewModel.librarySelected = false // Reset the flag
                            // Remove the temporary treasure bonus
                            civicViewModel.treasure.value =
                                (civicViewModel.treasure.value ?: 0) - 40
                            showToast("$key deselected, removing the temporary 40 treasure.")
                        }
                    }
                }
                val currentSelection: MutableSet<String?> = HashSet()
                for (selectedKey in tracker.selection) {
                    currentSelection.add(selectedKey)
                }
                civicViewModel.updateSelectionState(currentSelection)
            }

            override fun onSelectionRestored() {
                super.onSelectionRestored()
                val restoredSelection: MutableSet<String?> = HashSet()
                for (selectedKey in tracker.selection) {
                    restoredSelection.add(selectedKey)
                }
                civicViewModel.updateSelectionState(restoredSelection)

                // When selection is restored, recalculate the total based on the restored selection.
                civicViewModel.calculateTotal(tracker.selection)
                civicViewModel.librarySelected = restoredSelection.contains("Library")
            }
        })

        if (savedInstanceState == null) {
            civicViewModel.selectedCardKeysForState.value?.let {
                if (it.isNotEmpty() && tracker.selection.isEmpty && savedSelectionState == null) {
                    tracker.setItemsSelected(it, true)
                }
            }
        }

        civicViewModel.treasure.observe(viewLifecycleOwner) { treasure ->
            treasureInput!!.setText(treasure.toString())
            if (treasure < civicViewModel.remaining.value!!) {
                civicViewModel.remaining.value = treasure
                tracker.clearSelection()
            } else if (tracker.selection.size() > 0) {
                civicViewModel.calculateTotal(tracker.selection)
            } else {
                civicViewModel.remaining.value = treasure
            }
        }

        civicViewModel.remaining.observe(viewLifecycleOwner) { remaining ->
            remainingText!!.text = remaining.toString()
            adapter.setRemaining(remaining)
            updateViews()
        }

        val rootView = requireActivity().window.decorView.rootView
        keyboardListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private var previousHeight = 0

            override fun onGlobalLayout() {
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val visibleHeight = r.height()

                if (previousHeight != 0) {
                    val heightDiff = previousHeight - visibleHeight

                    if (heightDiff < -150) {  // Tastatur wurde geschlossen
                        updateViews()
                    }
                }
                previousHeight = visibleHeight
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)

        if (civicViewModel.treasure.value!! < 0) {
            civicViewModel.treasure.value = 0
        }
        civicViewModel.showCredits.observe(viewLifecycleOwner) { isVisible ->
            adapter.setShowCredits(isVisible)
        }

        civicViewModel.showInfos.observe(viewLifecycleOwner) { isVisible ->
            adapter.setShowInfos(isVisible)
        }
    }

    private fun focusTreasureInputAndShowKeyboard() {
        if (treasureInput != null && treasureInput!!.requestFocus()) {
            treasureInput!!.selectAll()
            treasureInput!!.post {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(treasureInput, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun hideKeyboard() {
        if (treasureInput != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(treasureInput!!.windowToken, 0)
        }
    }

    /**
     * Takes a string and separates it by all chars which are not a number. Calculates the sum
     * of the pieces, returning the result.
     * @param treasureInput String with numbers somewhere
     * @return result.
     */
    private fun calculateInput(treasureInput: String): Int {
        val pieces =
            treasureInput.trim { it <= ' ' }.split("\\D+".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var result = 0
        for (number in pieces) {
            result += number.toInt()
        }
        return result
    }

    /**
     * rechecks if the visible items on the screen still can be bought after an item
     * has been selected as RecyclerView only does not on items not visible which
     * are created fresh when coming back into view.
     */
    internal fun updateViews() {
        val lm = recyclerView.layoutManager as? LinearLayoutManager
        if (lm != null) {
            // Get adapter positions for first and last visible items on screen.
            val firstVisible = lm.findFirstVisibleItemPosition()
            val lastVisible = lm.findLastVisibleItemPosition()
            for (i in firstVisible..lastVisible) {
                // Find the view that corresponds to this position in the adapter.
                val visibleView = lm.findViewByPosition(i)
                if (visibleView != null) {
                    val price = visibleView.findViewById<TextView>(R.id.price).text.toString().toInt()
                    val isSelected = visibleView.isActivated
                    if (!isSelected) {
                        if (price > civicViewModel.remaining.value!!) {
                            visibleView.alpha = 0.25f
                        } else {
                            visibleView.alpha = 1.0f
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        tracker.onSaveInstanceState(savedInstanceState)
        if (civicViewModel.treasure.getValue() != null) {
            civicViewModel.remaining.value = civicViewModel.treasure.value
        }
    }

    override fun onResume() {
        super.onResume()
        val currentTreasure = civicViewModel.treasure.getValue()
        if (currentTreasure != null && currentTreasure == 0 && !treasureInputFocusedOnStart) {
            focusTreasureInputAndShowKeyboard()
            treasureInputFocusedOnStart = true // Setze das Flag, damit es nicht wiederholt wird,
            // falls der Benutzer das Fragment verlässt und zurückkommt,
            // während treasure immer noch 0 ist.
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val rootView = requireActivity().window.decorView.rootView
        if (keyboardListener != null) {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
            keyboardListener = null
        }
    }

    fun showToast(text: String?) {
        if (context != null) Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    /**
     * Returns the application back to the dashboard, but checks first if another dialog
     * must be presented because special cards where bought.
     */
    private fun returnToDashboard() {
        if (numberDialogs > 0) {
            return
        }
        view?.post {
            if (!isAdded) {
                return@post
            }
            try {
                val cards = civicViewModel.recentlyPurchasedCards.value
                if (!cards.isNullOrEmpty()) {
                    val action = BuyingFragmentDirections.actionBuyingFragmentToBoughtCardsFragment(cards.toTypedArray())
                    findNavController().navigate(action)
                } else {
                    val navController = findNavController()
                    navController.navigate(
                        R.id.homeFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(navController.graph.startDestinationId, true)
                            .build()
                    )
                }
            } catch (e: IllegalStateException) {
                Log.e("BuyingFragment", "view.post navigation failed (ISE)", e)
            } catch (e: IllegalArgumentException) {
                Log.e("BuyingFragment", "view.post navigation failed (IAE)", e)
            } catch (e: Exception) {
                Log.e("BuyingFragment", "view.post navigation failed (Exception)", e)
            }
        } ?: Log.w("BuyingFragment", "View was null in returnToDashboard, cannot post navigation.")
    }


    /**
     * Schaltet zur nächsten Sortierreihenfolge im Zyklus weiter.
     * Diese Methode wird beim normalen Klick auf den Button aufgerufen.
     */
    private fun cycleNextSortOrder() {
        val currentSortValue =
            civicViewModel.currentSortingOrder.value ?: sortingOptionsValues.first()
        val currentIndex = sortingOptionsValues.indexOf(currentSortValue)
        val nextIndex = (currentIndex + 1) % sortingOptionsValues.size
        val nextSortValue = sortingOptionsValues[nextIndex]
        applyNewSortOrder(nextSortValue)
    }

    /**
     * Aktualisiert den Text des Sortierbuttons basierend auf dem aktuellen Sortierwert.
     * Diese Methode existiert bereits in deinem Code. Stelle sicher, dass sie korrekt funktioniert.
     * Du könntest sie anpassen, um den Anzeigenamen (`sortingOptionsNames`) zu verwenden.
     */

    private fun updateSortButtonText(sortValue: String) {
        val index = sortingOptionsValues.indexOf(sortValue)
        if (index != -1 && index < sortingOptionsNames.size) {
            binding.btnSort.text = sortingOptionsNames[index]
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == binding.btnSort.id) {
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

    private fun applyNewSortOrder(sortValue: String) {
        civicViewModel.setSortingOrder(sortValue)
    }

    companion object {
        private const val EXTRA_CREDITS_REQUEST_KEY = "extraCreditsDialogResult"
        private const val ANATOMY_REQUEST_KEY = "anatomySelectionResult"
        private const val MENU_ID_SORT_OPTION_OFFSET = 1000
    }
}