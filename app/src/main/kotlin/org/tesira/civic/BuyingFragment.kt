package org.tesira.civic

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var mCivicViewModel: CivicViewModel
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
        mCivicViewModel =
            ViewModelProvider(requireActivity()).get<CivicViewModel>(CivicViewModel::class.java)
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

        mAdapter = BuyingAdapter(ArrayList<Card>(), mCivicViewModel)
        mRecyclerView.setAdapter(mAdapter)
        mTreasureInput = rootView.findViewById<EditText?>(R.id.treasure)
        mRemainingText = rootView.findViewById<TextView?>(R.id.moneyleft)

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

        val order = mCivicViewModel.currentSortingOrder.value ?: "name"
        updateSortButtonText(order)

        if (savedInstanceState != null) {
            savedSelectionState = savedInstanceState
        }

        // close SoftKeyboard on Enter
        mTreasureInput!!.setOnEditorActionListener(TextView.OnEditorActionListener { v: TextView?, keyCode: Int, event: KeyEvent? ->
            mCivicViewModel.treasure.value = calculateInput(mTreasureInput!!.text.toString())

            // hide virtual keyboard on enter
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mTreasureInput!!.windowToken, 0)
            true
        })

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

        // sort button
        binding.btnSort.setOnClickListener(View.OnClickListener { v: View? -> this.changeSorting() })
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCivicViewModel.getShowAnatomyDialogEvent()
            .observe(getViewLifecycleOwner(), Observer { event: Event<List<String>> ->
                val anatomyCardsToShow = event.getContentIfNotHandled()
                if (anatomyCardsToShow != null && !anatomyCardsToShow.isEmpty()) {
                    numberDialogs++
                    DialogAnatomyFragment.Companion.newInstance(anatomyCardsToShow)
                        .show(getParentFragmentManager(), "Anatomy")
                }
            })
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
     * This gets the list of possible sorting options from the preferences array and cycles
     * through them on each new entry.
     */
    private fun changeSorting() {
        var currentSortingOrderValue = mCivicViewModel.currentSortingOrder.getValue()
        if (currentSortingOrderValue == null) {
            // Fallback, falls der Wert aus dem ViewModel noch nicht verfügbar ist
            currentSortingOrderValue = sortingOptionsValues[0]
        }

        var currentSortingIndex =
            listOf<String?>(*sortingOptionsValues).indexOf(currentSortingOrderValue)
        if (currentSortingIndex == -1) {
            currentSortingIndex = 0
        }
        val nextSortingIndex: Int = if (currentSortingIndex == sortingOptionsValues.size - 1) {
            0
        } else {
            currentSortingIndex + 1
        }

        val nextSortingOrderValue = sortingOptionsValues[nextSortingIndex]
        mCivicViewModel.setSortingOrder(nextSortingOrderValue)
    }

    private fun updateSortButtonText(currentOrderValue: String?) {
        var sortingIndex = listOf<String?>(*sortingOptionsValues).indexOf(currentOrderValue)
        if (sortingIndex == -1) {
            sortingIndex = 0
        }
        val labelToShow = sortingOptionsNames[sortingIndex]

        // falls nicht genug Platz
        //   if (mCivicViewModel.getScreenWidthDp() <= 400) {
        //      labelToShow = String.valueOf(label.charAt(0));
        //   }
        binding.btnSort.text = labelToShow
    }

    companion object {
        private const val EXTRA_CREDITS_REQUEST_KEY = "extraCreditsDialogResult"
        private const val ANATOMY_REQUEST_KEY = "anatomySelectionResult"
    }
}