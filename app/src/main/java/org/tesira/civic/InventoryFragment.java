package org.tesira.civic;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;

/**
 * Shows all bought civilization advances (cards).
 */
public class InventoryFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private CivicViewModel mCivicViewModel;
    private InventoryAdapter adapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InventoryFragment() {
    }

    @SuppressWarnings("unused")
    public static InventoryFragment newInstance(int columnCount) {
        InventoryFragment fragment = new InventoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inventory, container, false);
        mRecyclerView = (RecyclerView) rootView;
        final int initialPaddingLeft = rootView.getPaddingLeft();
        final int initialPaddingTop = rootView.getPaddingTop();
        final int initialPaddingRight = rootView.getPaddingRight();
        final int initialPaddingBottom = rootView.getPaddingBottom();


        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
            );
            return windowInsets;
        });

        ViewCompat.requestApplyInsets(rootView);
        int actualColumnCount = mCivicViewModel.calculateColumnCount(rootView.getContext());

        if (actualColumnCount <= 1) {
            mLayout = new LinearLayoutManager(rootView.getContext());
            mRecyclerView.setLayoutManager(mLayout);
        } else {
            mLayout = new GridLayoutManager(rootView.getContext(), actualColumnCount);
            mRecyclerView.setLayoutManager(mLayout);
        }

        // Set the adapter
        if (rootView instanceof RecyclerView) {
            adapter = new InventoryAdapter();
            mRecyclerView.setAdapter(adapter);

            // LiveData beobachten und Adapter aktualisieren
            mCivicViewModel.getInventoryAsCardLiveData().observe(getViewLifecycleOwner(), purchasedCards -> {
                if (purchasedCards != null) {
                    adapter.submitList(purchasedCards);
                } else {
                    adapter.submitList(new ArrayList<>());
                }
            });
        }
        return rootView;
    }
}