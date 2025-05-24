package org.tesira.civic;

import static java.lang.Integer.parseInt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.List;

// Import the new MenuProvider interface
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

/**
 * Fragment for the buy process. User can input a treasure sum and select up to that value
 * cards. Pressing the buy button finishes the buying process, adding selected cards to the
 * purchases list and returns the user back to the dashboard.
 */
public class BuyingFragment extends Fragment {
    private static final String EXTRA_CREDITS_REQUEST_KEY = "extraCreditsDialogResult";
    private static final String ANATOMY_REQUEST_KEY = "anatomySelectionResult";
    private static final String TREASURE_BOX = "treasure";
    private CivicViewModel mCivicViewModel;
    private String sortingOrder;
    protected EditText mTreasureInput;
    protected TextView mRemainingText;
    private SelectionTracker<String> tracker;
    private SharedPreferences prefs;
    private FragmentBuyingBinding binding;
    private int numberDialogs = 0;
    private BuyingItemKeyProvider mBuyingItemKeyProvider;
    private LinearLayoutManager mLayout;
    private BuyingListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mColumnCount = 2;
    private String[] sortingOptionsValues, sortingOptionsNames;
    private Bundle savedSelectionState = null;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mColumnCount = Integer.parseInt(prefs.getString("columns", "1"));
        sortingOrder = prefs.getString("sort", "name");
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
        mRecyclerView = rootView.findViewById(R.id.list_advances);
        if (mColumnCount <= 1) {
            mLayout = new LinearLayoutManager(rootView.getContext());
            mRecyclerView.setLayoutManager(mLayout);
        } else {
            mLayout = new GridLayoutManager(rootView.getContext(), mColumnCount);
            mRecyclerView.setLayoutManager(mLayout);
        }

        mAdapter = new BuyingListAdapter(new ArrayList<>(), mCivicViewModel);
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
                updateSortButtonText(order); // Diese Methode setzt den Button-Text
            }
        });

        String initialOrder = mCivicViewModel.getCurrentSortingOrder().getValue();
        if (initialOrder != null) {
            updateSortButtonText(initialOrder);
        } else {
            // Fallback, wenn initial noch nichts gesetzt ist, z.B. erster Start
            // Hole den Standardwert aus den SharedPreferences oder definiere einen festen Startwert
            String defaultSortOrder = prefs.getString("sort", sortingOptionsValues[0]); // Beispiel: aus Prefs
            updateSortButtonText(defaultSortOrder);
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
                showToast("Keine Karten ausgewählt.");
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
                if (mCivicViewModel.getTreasure().getValue() != null) {
                    if (mCivicViewModel.librarySelected) {
                        mCivicViewModel.librarySelected = false;
                        mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - 40);
                    }
                }
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
        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        mAdapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
                mCivicViewModel.calculateTotal(tracker.getSelection());

                if (key.equals("Library")) {
                    if (selected) {
                        // Library was just selected
                        if (!mCivicViewModel.librarySelected) { // Only add if it wasn't already selected
                            mCivicViewModel.librarySelected = true;
                            // Add the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() + 40);
                            showToast(key + " selected, temporary adding 40 treasure.");
                        }
                    } else {
                        // Library was just deselected
                        if (mCivicViewModel.librarySelected) { // Only remove if it was previously selected
                            mCivicViewModel.librarySelected = false; // Reset the flag
                            // Remove the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - 40);
                            showToast(key + " deselected, removing the temporary 40 treasure.");
                        }
                    }
                }
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
                Log.d("BuyingFragment", "onSelectionRestored");
                // When selection is restored, recalculate the total based on the restored selection.
                mCivicViewModel.calculateTotal(tracker.getSelection());

                // Re-evaluate if the Library card is among the restored selection
                boolean libraryIsSelected = false;
                for (String selectedKey : tracker.getSelection()) {
                    if (selectedKey.equals("Library")) {
                        libraryIsSelected = true;
                        break;
                    }
                }
                Log.w("BuyingFragment", "Library selected: " + libraryIsSelected);
                mCivicViewModel.librarySelected = libraryIsSelected;
            }
        });

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
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
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