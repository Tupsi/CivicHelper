package org.tesira.civic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.db.Card
import org.tesira.civic.db.CivicViewModel

/**
 * Shows all bought civilization advances (cards).
 */
class InventoryFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : Fragment() {
    private val mCivicViewModel: CivicViewModel by activityViewModels()
    private lateinit var adapter: InventoryAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mLayout: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_inventory, container, false)
        mRecyclerView = rootView as RecyclerView
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom


        ViewCompat.setOnApplyWindowInsetsListener(
            rootView,
            OnApplyWindowInsetsListener { v, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
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

        // Set the adapter
        adapter = InventoryAdapter(mCivicViewModel)
        mRecyclerView.setAdapter(adapter)

        // LiveData beobachten und Adapter aktualisieren
        mCivicViewModel.inventoryAsCardLiveData.observe(
            getViewLifecycleOwner(),
            Observer { purchasedCards ->
                if (purchasedCards != null) {
                    adapter.submitList(purchasedCards)
                } else {
                    adapter.submitList(ArrayList<Card>())
                }
            })
        return rootView
    }

    companion object {
        private const val ARG_COLUMN_COUNT = "column-count"

        @Suppress("unused")
        fun newInstance(columnCount: Int): InventoryFragment {
            val fragment = InventoryFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.setArguments(args)
            return fragment
        }
    }
}