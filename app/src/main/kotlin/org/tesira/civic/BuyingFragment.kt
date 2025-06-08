package org.tesira.civic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
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
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.FragmentBuyingBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CivicViewModel

/**
 * Fragment for the buy process. User can input a treasure sum and select up to that value
 * cards. Pressing the buy button finishes the buying process, adding selected cards to the
 * purchases list and returns the user back to the dashboard.
 */
class BuyingFragment : Fragment() {
    internal val mCivicViewModel: CivicViewModel by activityViewModels()
    private lateinit var tracker: SelectionTracker<String>
    private lateinit var binding: FragmentBuyingBinding
    private lateinit var mBuyingItemKeyProvider: BuyingItemKeyProvider
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayout: RecyclerView.LayoutManager
    private lateinit var mAdapter: BuyingAdapter

    private var mTreasureInput: EditText? = null
    private var mRemainingText: TextView? = null

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

        getParentFragmentManager().setFragmentResultListener(
            EXTRA_CREDITS_REQUEST_KEY,
            this,
            object : FragmentResultListener {
                override fun onFragmentResult(requestKey: String, result: Bundle) {
                    numberDialogs--
                    returnToDashboard()
                }
            })

        getParentFragmentManager().setFragmentResultListener(
            ANATOMY_REQUEST_KEY,
            this,
            object : FragmentResultListener {
                override fun onFragmentResult(requestKey: String, result: Bundle) {
                    numberDialogs--

                    val selectedAnatomyCard = result.getString("selected_card_name")
                    if ("Written Record" == selectedAnatomyCard) {
                        mCivicViewModel.triggerExtraCreditsDialog(10)
                    } else {
                        returnToDashboard()
                    }
                }
            })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBuyingBinding.inflate(inflater, container, false)
        val rootView: View = binding.getRoot()
        mRecyclerView = binding.purchasableCards

        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom

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

        val actualColumnCount = mCivicViewModel.calculateColumnCount(rootView.context)

        if (actualColumnCount <= 1) {
            mLayout = LinearLayoutManager(rootView.context)
            mRecyclerView.setLayoutManager(mLayout)
        } else {
            mLayout = GridLayoutManager(rootView.context, actualColumnCount)
            mRecyclerView.setLayoutManager(mLayout)
        }

        mAdapter = BuyingAdapter(mCivicViewModel)
        mRecyclerView.setAdapter(mAdapter)
        mTreasureInput = binding.treasure
        mRemainingText = binding.moneyleft

        mTreasureInput?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // Verzögere die Selektion leicht, um sicherzustellen, dass das EditText bereit ist
                view.post {
                    (view as? EditText)?.selectAll()
                }
            }
        }

        mCivicViewModel.allAdvancesNotBought.observe(
            getViewLifecycleOwner(),
            Observer { cards: MutableList<Card> ->
                mAdapter.changeList(cards)
                if (savedSelectionState != null) {
                    tracker.onRestoreInstanceState(savedSelectionState)
                    savedSelectionState = null
                }
            })

        mCivicViewModel.currentSortingOrder.observe(
            getViewLifecycleOwner(),
            Observer { order: String? ->
                if (order != null) {
                    updateSortButtonText(order)
                }
            })

        if (savedInstanceState != null) {
            savedSelectionState = savedInstanceState
        }

        // close SoftKeyboard on Enter
        mTreasureInput?.setOnEditorActionListener(TextView.OnEditorActionListener { v, keyCode, event ->
            if (event == null || event.action == KeyEvent.ACTION_UP || keyCode == KeyEvent.KEYCODE_ENTER) { // Sicherstellen, dass es ein "UP"-Event ist oder nur Enter ohne Event
                mCivicViewModel.treasure.value = calculateInput(mTreasureInput!!.text.toString())
                if (mCivicViewModel.remaining.value!! < 0) {
                    tracker.clearSelection()
                }
                // Tastatur verstecken
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mTreasureInput!!.windowToken, 0)

                // Fokus entfernen
                mTreasureInput!!.clearFocus() // Wichtig: Fokus explizit entfernen
                binding.root.requestFocus() // Optional: Fokus auf ein anderes Element lenken (z.B. das Root-Layout)
                return@OnEditorActionListener true
            }
            false
        })

        mRecyclerView.setOnTouchListener { v, _ ->
            mTreasureInput?.clearFocus()
            binding.root.requestFocus()

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mTreasureInput?.windowToken, 0)
            v.performClick()
            false
        }

        // button which finalizes the buy process
        binding.btnBuy.setOnClickListener(View.OnClickListener btnBuyClickListener@{ v: View? ->
            if (tracker.getSelection().isEmpty) {
                showToast(getString(R.string.no_cards_selected))
                return@btnBuyClickListener
            }
            val selectedCardNames: MutableList<String> = ArrayList<String>()
            for (name in tracker.getSelection()) {
                selectedCardNames.add(name)
            }
            for (name in selectedCardNames) {
                mCivicViewModel.addBonus(name)
            }
            mCivicViewModel.saveBonus()
            mCivicViewModel.processPurchases(selectedCardNames)
        })

        // button to clear the current selection of cards
        binding.btnClear.setOnClickListener(View.OnClickListener { v: View? ->
            tracker.clearSelection()
            mCivicViewModel.clearCurrentSelectionState()
        })
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.btnSort)
        mCivicViewModel.getShowAnatomyDialogEvent()
            .observe(getViewLifecycleOwner(), Observer { event: Event<List<String>> ->
                val anatomyCardsToShow = event.getContentIfNotHandled()
                if (anatomyCardsToShow != null && !anatomyCardsToShow.isEmpty()) {
                    numberDialogs++
                    DialogAnatomyFragment.Companion.newInstance(anatomyCardsToShow)
                        .show(getParentFragmentManager(), "Anatomy")
                }
            })

        binding.btnSort.setOnClickListener {
            cycleNextSortOrder()
        }
        mCivicViewModel.currentSortingOrder.value?.let { currentSortValue ->
            updateSortButtonText(currentSortValue)
        }

        mCivicViewModel.showExtraCreditsDialogEvent.observe(
            getViewLifecycleOwner(),
            Observer { event: Event<Int?>? ->
                val extraCredits = event!!.getContentIfNotHandled()
                if (extraCredits != null && extraCredits > 0) {
                    numberDialogs++
                    DialogExtraCreditsFragment.Companion.newInstance(extraCredits)
                        .show(getParentFragmentManager(), "ExtraCredits")
                }
            })

        mCivicViewModel.navigateToDashboardEvent.observe(
            getViewLifecycleOwner(),
            Observer { event ->
                val shouldNavigate = event.getContentIfNotHandled()
                if (shouldNavigate == true) {
                    returnToDashboard()
                }
            })

        mBuyingItemKeyProvider = BuyingItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, mAdapter)
        tracker = SelectionTracker.Builder<String>(
            "my-selection-id",
            mRecyclerView,
            mBuyingItemKeyProvider,
            BuyingItemDetailsLookup(mRecyclerView),
            StorageStrategy.createStringStorage()
        )
            .withSelectionPredicate(BuyingSelectionPredicate(mAdapter, mCivicViewModel))
            .build()

        mAdapter.setSelectionTracker(tracker)
        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onItemStateChanged(key: String, selected: Boolean) {
                super.onItemStateChanged(key, selected)

                mCivicViewModel.calculateTotal(tracker.getSelection())
                val isFinalizing = mCivicViewModel.isFinalizingPurchase.value == true

                if (!isFinalizing && key == "Library") {
                    if (selected) {
                        // Library was just selected
                        if (!mCivicViewModel.librarySelected) { // Only add if it wasn't already selected
                            mCivicViewModel.librarySelected = true
                            // Add the temporary treasure bonus
                            mCivicViewModel.treasure.value =
                                (mCivicViewModel.treasure.value ?: 0) + 40
                            showToast("$key selected, temporary adding 40 treasure.")
                        }
                    } else {
                        // Library was just deselected
                        if (mCivicViewModel.librarySelected) { // Only remove if it was previously selected
                            mCivicViewModel.librarySelected = false // Reset the flag
                            // Remove the temporary treasure bonus
                            mCivicViewModel.treasure.value =
                                (mCivicViewModel.treasure.value ?: 0) - 40
                            showToast("$key deselected, removing the temporary 40 treasure.")
                        }
                    }
                }
                val currentSelection: MutableSet<String?> = HashSet<String?>()
                for (selectedKey in tracker.getSelection()) {
                    currentSelection.add(selectedKey)
                }
                mCivicViewModel.updateSelectionState(currentSelection)
            }

            override fun onSelectionRefresh() {
                super.onSelectionRefresh()
            }

            override fun onSelectionChanged() {
                super.onSelectionChanged()
            }

            override fun onSelectionRestored() {
                super.onSelectionRestored()
                val restoredSelection: MutableSet<String?> = HashSet<String?>()
                for (selectedKey in tracker.getSelection()) {
                    restoredSelection.add(selectedKey)
                }
                mCivicViewModel.updateSelectionState(restoredSelection)

                // When selection is restored, recalculate the total based on the restored selection.
                mCivicViewModel.calculateTotal(tracker.getSelection())

                var libraryIsNowSelected = false
                if (!restoredSelection.isEmpty()) {
                    libraryIsNowSelected = restoredSelection.contains("Library")
                }
                mCivicViewModel.librarySelected = libraryIsNowSelected
            }
        })

        if (savedInstanceState == null) {
            val selectionFromVm = mCivicViewModel.selectedCardKeysForState.getValue()
            if (selectionFromVm != null && !selectionFromVm.isEmpty()) {
                if (tracker.getSelection().isEmpty && savedSelectionState == null) {
                    tracker.setItemsSelected(selectionFromVm, true)
                }
            }
        }

        mCivicViewModel.treasure.observe(getViewLifecycleOwner(), Observer { treasure: Int? ->
            mTreasureInput!!.setText(treasure.toString())
            if (treasure!! < mCivicViewModel.remaining.getValue()!!) {
                mCivicViewModel.remaining.value = treasure
                tracker.clearSelection()
            } else if (tracker.getSelection().size() > 0) {
                mCivicViewModel.calculateTotal(tracker.getSelection())
            } else {
                mCivicViewModel.remaining.value = treasure
            }
        })

        mCivicViewModel.remaining.observe(getViewLifecycleOwner(), Observer { remaining: Int? ->
            mRemainingText!!.text = remaining.toString()
            updateViews()
        })

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

        if (mCivicViewModel.treasure.value!! < 0) {
            mCivicViewModel.treasure.value = 0
        }

    }

    private fun focusTreasureInputAndShowKeyboard() {
        if (mTreasureInput != null && mTreasureInput!!.requestFocus()) {
            // Fenster-Token ist manchmal erst nach einer kleinen Verzögerung verfügbar,
            // besonders wenn das Fragment gerade erst erstellt wird.
            mTreasureInput!!.selectAll()
            mTreasureInput!!.post(Runnable {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(mTreasureInput, InputMethodManager.SHOW_IMPLICIT)
            })
        }
    }

    private fun hideKeyboard() {
        if (mTreasureInput != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(mTreasureInput!!.windowToken, 0)
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
        val lm = mRecyclerView.layoutManager as LinearLayoutManager?
        if (lm != null) {
            // Get adapter positions for first and last visible items on screen.
            val firstVisible = lm.findFirstVisibleItemPosition()
            val lastVisible = lm.findLastVisibleItemPosition()
            for (i in firstVisible..lastVisible) {
                // Find the view that corresponds to this position in the adapter.
                val visibleView = lm.findViewByPosition(i)
                if (visibleView != null) {
                    val priceText = visibleView.findViewById<TextView>(R.id.price)
                    val nameText = visibleView.findViewById<TextView>(R.id.name)
                    val name = nameText.text.toString()
                    val isSelected = tracker.isSelected(name)
                    val price = priceText.text.toString().toInt()
                    val mCardView = visibleView.findViewById<View>(R.id.card)
                    if (!isSelected) {
                        if (price > mCivicViewModel.remaining.getValue()!!) {
                            mCardView.alpha = 0.25f
                        } else {
                            mCardView.alpha = 1.0f
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        tracker.onSaveInstanceState(savedInstanceState)
        if (mCivicViewModel.treasure.getValue() != null) {
            mCivicViewModel.remaining.value = mCivicViewModel.treasure.value
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        val currentTreasure = mCivicViewModel.treasure.getValue()
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

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
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
    fun returnToDashboard() {
        // Nur zum Dashboard zurückkehren, wenn keine Dialoge mehr offen sind
        if (numberDialogs <= 0) {
            mCivicViewModel.saveBonus()
            NavHostFragment.Companion.findNavController(this).popBackStack()
            mCivicViewModel.treasure.value = 0
            mCivicViewModel.remaining.value = 0
        }
    }


    /**
     * Schaltet zur nächsten Sortierreihenfolge im Zyklus weiter.
     * Diese Methode wird beim normalen Klick auf den Button aufgerufen.
     */
    private fun cycleNextSortOrder() {
        val currentSortValue =
            mCivicViewModel.currentSortingOrder.value ?: sortingOptionsValues.first()
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
        mCivicViewModel.setSortingOrder(sortValue)
    }

    companion object {
        private const val EXTRA_CREDITS_REQUEST_KEY = "extraCreditsDialogResult"
        private const val ANATOMY_REQUEST_KEY = "anatomySelectionResult"
        private const val MENU_ID_SORT_OPTION_OFFSET = 1000
    }
}