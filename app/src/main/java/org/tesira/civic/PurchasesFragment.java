package org.tesira.civic;

import android.content.Context;
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
public class PurchasesFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
//    private int mColumnCount = 1;
    private CivicViewModel mCivicViewModel;
    private PurchasesRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PurchasesFragment() {
    }

    @SuppressWarnings("unused")
    public static PurchasesFragment newInstance(int columnCount) {
        PurchasesFragment fragment = new PurchasesFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_purchases, container, false);

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

        // Set the adapter
        if (rootView instanceof RecyclerView) {
            Context context = rootView.getContext();
            RecyclerView recyclerView = (RecyclerView) rootView;
            if (mCivicViewModel.getColumns() <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mCivicViewModel.getColumns()));
            }
            adapter = new PurchasesRecyclerViewAdapter();
            recyclerView.setAdapter(adapter);

            // LiveData beobachten und Adapter aktualisieren
            mCivicViewModel.getPurchasesAsCardLiveData().observe(getViewLifecycleOwner(), purchasedCards -> {
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