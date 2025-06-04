package org.tesira.civic;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tesira.civic.databinding.FragmentBuyingBinding;
import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Fragment for the buy process. User can input a treasure sum and select up to that value
 * cards. Pressing the buy button finishes the buying process, adding selected cards to the
 * purchases list and returns the user back to the dashboard.
 */
public class BuyingFragment extends Fragment {
    private static final String EXTRA_CREDITS_REQUEST_KEY = "extraCreditsDialogResult";
    private static final String ANATOMY_REQUEST_KEY = "anatomySelectionResult";
    private CivicViewModel mCivicViewModel;
    @NonNull
    protected EditText mTreasureInput;
    @NonNull
    protected TextView mRemainingText;
    private SelectionTracker<String> tracker;
    private FragmentBuyingBinding binding;
    private int numberDialogs = 0;
    private BuyingItemKeyProvider mBuyingItemKeyProvider;
    private RecyclerView.LayoutManager mLayout;
    private BuyingAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private String[] sortingOptionsValues, sortingOptionsNames;
    private Bundle savedSelectionState = null;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
    private boolean treasureInputFocusedOnStart = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        sortingOptionsValues = getResources().getStringArray(R.array.sort_values);
        sortingOptionsNames = getResources().getStringArray(R.array.sort_entries);

        getParentFragmentManager().setFragmentResultListener(EXTRA_CREDITS_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                numberDialogs--;
                returnToDashboard();
            }
        });

        getParentFragmentManager().setFragmentResultListener(ANATOMY_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                numberDialogs--;

                String selectedAnatomyCard = result.getString("selected_card_name");
                if ("Written Record".equals(selectedAnatomyCard)) {
                    mCivicViewModel.triggerExtraCreditsDialog(10);
                } else {
                    returnToDashboard();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentBuyingBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();
        mRecyclerView = binding.purchasableCards;

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

        mAdapter = new BuyingAdapter(new ArrayList<>(), mCivicViewModel);
        mRecyclerView.setAdapter(mAdapter);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);

        mCivicViewModel.getAllAdvancesNotBought().observe(getViewLifecycleOwner(), cards -> {
            if (cards != null) {
                mAdapter.changeList(cards);
                if (savedSelectionState != null) {
                    tracker.onRestoreInstanceState(savedSelectionState);
                    savedSelectionState = null;
                }
            }
        });

        mCivicViewModel.getCurrentSortingOrder().observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                updateSortButtonText(order);
            }
        });

        String initialOrder = mCivicViewModel.getCurrentSortingOrder().getValue();
        if (initialOrder != null) {
            updateSortButtonText(initialOrder);
        } else {
            updateSortButtonText(mCivicViewModel.getCurrentSortingOrder().getValue());
        }

        if (savedInstanceState != null) {
            savedSelectionState = savedInstanceState;
        }

        // close SoftKeyboard on Enter
        mTreasureInput.setOnEditorActionListener((v, keyCode, event) -> {
            mCivicViewModel.setTreasure(calculateInput(mTreasureInput.getText().toString()));
            // hide virtual keyboard on enter
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTreasureInput.getWindowToken(), 0);
            return true;
        });

        // button which finalizes the buy process
        binding.btnBuy.setOnClickListener(v -> {
            if (tracker.getSelection().isEmpty()){
                showToast(getString(R.string.no_cards_selected));
                return;
            }
            List<String> selectedCardNames = new ArrayList<>();
            for (String name : tracker.getSelection()) {
                selectedCardNames.add(name);
            }
            for (String name : selectedCardNames) {
                mCivicViewModel.addBonus(name);
            }
            mCivicViewModel.saveBonus();
            mCivicViewModel.processPurchases(selectedCardNames);
        });

        // button to clear the current selection of cards
        binding.btnClear.setOnClickListener(v -> {
            if (tracker != null) {
                tracker.clearSelection();
                mCivicViewModel.clearCurrentSelectionState();
            }
        });

        // sort button
        binding.btnSort.setOnClickListener(this::changeSorting);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCivicViewModel.getShowAnatomyDialogEvent().observe(getViewLifecycleOwner(), event -> {
            List<String> anatomyCardsToShow = event.getContentIfNotHandled();
            if (anatomyCardsToShow != null && !anatomyCardsToShow.isEmpty()) {
                numberDialogs++;
                DialogAnatomyFragment.newInstance(anatomyCardsToShow)
                        .show(getParentFragmentManager(), "Anatomy");
            }
        });
        mCivicViewModel.getShowExtraCreditsDialogEvent().observe(getViewLifecycleOwner(), event -> {
            Integer extraCredits = event.getContentIfNotHandled();
            if (extraCredits != null && extraCredits > 0) {
                numberDialogs++;
                DialogExtraCreditsFragment.newInstance(extraCredits)
                        .show(getParentFragmentManager(), "ExtraCredits");
            }
        });

        mCivicViewModel.getNavigateToDashboardEvent().observe(getViewLifecycleOwner(), event -> {
            Boolean shouldNavigate = event.getContentIfNotHandled();
            if (shouldNavigate != null && shouldNavigate) {
                returnToDashboard();
            }
        });

        mBuyingItemKeyProvider = new BuyingItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, mAdapter);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                mBuyingItemKeyProvider,
                new BuyingItemDetailsLookup(mRecyclerView),
                StorageStrategy.createStringStorage())
                .withSelectionPredicate(new BuyingSelectionPredicate<>(mAdapter, mCivicViewModel))
                .build();

        mAdapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
                mCivicViewModel.calculateTotal(tracker.getSelection());
                boolean isFinalizing = Boolean.TRUE.equals(mCivicViewModel.isFinalizingPurchase().getValue());

                if (!isFinalizing && key.equals("Library")) {
                    if (selected) {
                        // Library was just selected
                        if (!mCivicViewModel.getLibrarySelected()) { // Only add if it wasn't already selected
                            mCivicViewModel.setLibrarySelected(true);
                            // Add the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() + 40);
                            showToast(key + " selected, temporary adding 40 treasure.");
                        }
                    } else {
                        // Library was just deselected
                        if (mCivicViewModel.getLibrarySelected()) { // Only remove if it was previously selected
                            mCivicViewModel.setLibrarySelected(false); // Reset the flag
                            // Remove the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - 40);
                            showToast(key + " deselected, removing the temporary 40 treasure.");
                        }
                    }
                }
                // NEU: ViewModel über die geänderte Auswahl informieren
                Set<String> currentSelection = new HashSet<>();
                for (String selectedKey : tracker.getSelection()) {
                    currentSelection.add(selectedKey);
                }
                mCivicViewModel.updateSelectionState(currentSelection);


            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() { super.onSelectionChanged(); }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
                Set<String> restoredSelection = new HashSet<>();
                for (String selectedKey : tracker.getSelection()) {
                    restoredSelection.add(selectedKey);
                }
                mCivicViewModel.updateSelectionState(restoredSelection);

                // When selection is restored, recalculate the total based on the restored selection.
                mCivicViewModel.calculateTotal(tracker.getSelection());

                boolean libraryIsNowSelected = false;
                if (!restoredSelection.isEmpty()) {
                    libraryIsNowSelected = restoredSelection.contains("Library");
                }
                mCivicViewModel.setLibrarySelected(libraryIsNowSelected);
            }
        });

        if (savedInstanceState == null) {
            Set<String> selectionFromVm = mCivicViewModel.getSelectedCardKeysForState().getValue();
            if (selectionFromVm != null && !selectionFromVm.isEmpty()) {
                if (tracker.getSelection().isEmpty() && savedSelectionState == null) {
                    tracker.setItemsSelected(selectionFromVm, true);
                }
            }
        }

        mCivicViewModel.getTreasure().observe(getViewLifecycleOwner(), treasure -> {
            mTreasureInput.setText(String.valueOf(treasure));

            if (treasure < mCivicViewModel.getRemaining().getValue()) {
                mCivicViewModel.setRemaining(treasure);
                tracker.clearSelection();
            } else if (tracker != null &&  tracker.getSelection().size() > 0){
                mCivicViewModel.calculateTotal(tracker.getSelection());
            } else {
                mCivicViewModel.setRemaining(treasure);
            }
        });

        mCivicViewModel.getRemaining().observe(getViewLifecycleOwner(), remaining -> {
            mRemainingText.setText(String.valueOf(remaining));
            if (tracker != null) {
                updateViews();
            }
        });

        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        keyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int visibleHeight = r.height();

                if (previousHeight != 0) {
                    int heightDiff = previousHeight - visibleHeight;

                    if (heightDiff < -150) {  // Tastatur wurde geschlossen
                        updateViews();
                    }
                }
                previousHeight = visibleHeight;
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);

        if (mCivicViewModel.getTreasure().getValue() < 0){
            mCivicViewModel.setTreasure(0);
        }
    }

    private void focusTreasureInputAndShowKeyboard() {
        if (mTreasureInput != null && mTreasureInput.requestFocus()) {
            // Fenster-Token ist manchmal erst nach einer kleinen Verzögerung verfügbar,
            // besonders wenn das Fragment gerade erst erstellt wird.
            mTreasureInput.selectAll();
            mTreasureInput.post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(mTreasureInput, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    private void hideKeyboard() {
        if (mTreasureInput != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mTreasureInput.getWindowToken(), 0);
            }
        }
    }

    /**
     * Takes a string and separates it by all chars which are not a number. Calculates the sum
     * of the pieces, returning the result.
     * @param treasureInput String with numbers somewhere
     * @return result.
     */
    private int calculateInput(String treasureInput) {
        String [] pieces = treasureInput.trim().split("\\D+");
        int result = 0;
        for (String number:pieces) {
            result += Integer.parseInt(number);
        }
        return result;
    }

    /**
     * rechecks if the visible items on the screen still can be bought after an item
     * has been selected as RecyclerView only does not on items not visible which
     * are created fresh when coming back into view.
     */
    private void updateViews() {
        LinearLayoutManager lm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (lm != null) {
            // Get adapter positions for first and last visible items on screen.
            int firstVisible = lm.findFirstVisibleItemPosition();
            int lastVisible = lm.findLastVisibleItemPosition();
            for (int i = firstVisible; i <= lastVisible; i++) {
                // Find the view that corresponds to this position in the adapter.
                View visibleView = lm.findViewByPosition(i);
                if (visibleView != null) {
                    TextView priceText = visibleView.findViewById(R.id.price);
                    TextView nameText = visibleView.findViewById(R.id.name);
                    String name = nameText.getText().toString();
                    boolean isSelected = tracker.isSelected(name);
                    int price = Integer.parseInt(priceText.getText().toString());
                    View mCardView = visibleView.findViewById(R.id.card);
                    if (!isSelected) {
                        if (price > mCivicViewModel.getRemaining().getValue()) {
                            mCardView.setAlpha(0.25F);
                        } else {
                            mCardView.setAlpha(1.0F);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (tracker != null) {
            tracker.onSaveInstanceState(savedInstanceState);
        }
        if (mCivicViewModel != null && mCivicViewModel.getTreasure().getValue() != null) {
            mCivicViewModel.setRemaining(mCivicViewModel.getTreasure().getValue());
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        Integer currentTreasure = mCivicViewModel.getTreasure().getValue();
        if (currentTreasure != null && currentTreasure == 0 && !treasureInputFocusedOnStart) {
            focusTreasureInputAndShowKeyboard();
            treasureInputFocusedOnStart = true; // Setze das Flag, damit es nicht wiederholt wird,
            // falls der Benutzer das Fragment verlässt und zurückkommt,
            // während treasure immer noch 0 ist.
        }
    }

    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        if (keyboardListener != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
            keyboardListener = null;
        }
    }

    public void showToast(String text) {
        if (getContext() != null) Toast.makeText(getContext(), text,Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the application back to the dashboard, but checks first if another dialog
     * must be presented because special cards where bought.
     */
    public void returnToDashboard() {
        // Nur zum Dashboard zurückkehren, wenn keine Dialoge mehr offen sind
        if (numberDialogs <= 0) {
            mCivicViewModel.saveBonus();
            NavHostFragment.findNavController(this).popBackStack();
            mCivicViewModel.setTreasure(0);
            mCivicViewModel.setRemaining(0);
        }
    }
    /**
     * This gets the list of possible sorting options from the preferences array and cycles
     * through them on each new entry.
     * @param v View
     */
    private void changeSorting(View v) {
        String currentSortingOrderValue = mCivicViewModel.getCurrentSortingOrder().getValue();
        if (currentSortingOrderValue == null) {
            // Fallback, falls der Wert aus dem ViewModel noch nicht verfügbar ist
            currentSortingOrderValue = sortingOptionsValues[0];
        }

        int currentSortingIndex = Arrays.asList(sortingOptionsValues).indexOf(currentSortingOrderValue);
        if (currentSortingIndex == -1) {
            currentSortingIndex = 0;
        }

        int nextSortingIndex;
        if (currentSortingIndex == sortingOptionsValues.length - 1) {
            nextSortingIndex = 0;
        } else {
            nextSortingIndex = currentSortingIndex + 1;
        }

        String nextSortingOrderValue = sortingOptionsValues[nextSortingIndex];
        mCivicViewModel.setSortingOrder(nextSortingOrderValue);
    }

    private void updateSortButtonText(String currentOrderValue) {
        int sortingIndex = Arrays.asList(sortingOptionsValues).indexOf(currentOrderValue);
        if (sortingIndex == -1) {
            sortingIndex = 0;
        }
        String labelToShow = sortingOptionsNames[sortingIndex];
        // falls nicht genug Platz
        //   if (mCivicViewModel.getScreenWidthDp() <= 400) {
        //      labelToShow = String.valueOf(label.charAt(0));
        //   }

        binding.btnSort.setText(labelToShow);
    }
}