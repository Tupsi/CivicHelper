package org.tesira.civic;

import static java.lang.Integer.parseInt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import org.tesira.civic.R;
import org.tesira.civic.databinding.FragmentAdvancesBinding;
import org.tesira.civic.db.Card;
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
    // arraylist of all civilization cards
    private String sortingOrder;
    protected EditText mTreasureInput;
    protected TextView mRemainingText;
    private SelectionTracker<String> tracker;
    private SharedPreferences prefs;
    private FragmentAdvancesBinding binding;
    private int numberDialogs = 0;
    private List<Card> listCivics = new ArrayList<>();
    private BuyingItemKeyProvider mBuyingItemKeyProvider;
    private LinearLayoutManager mLayout;
    private BuyingListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mColumnCount = 2;
    private int sortingIndex;
    private String[] sortingOptionsValues, sortingOptionsNames;
    private Bundle savedSelectionState = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mColumnCount = Integer.parseInt(prefs.getString("columns", "1"));
        sortingOrder = prefs.getString("sort", "name");
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        sortingOptionsValues = getResources().getStringArray(R.array.sort_values);
        sortingOptionsNames = getResources().getStringArray(R.array.sort_entries);
        sortingIndex = Arrays.asList(sortingOptionsValues).indexOf(sortingOrder);

        getParentFragmentManager().setFragmentResultListener(EXTRA_CREDITS_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d("BuyingFragment", "Received result from Extra Credits Dialog.");
                numberDialogs--;
                returnToDashboard();
            }
        });

        // Listener für das Ergebnis vom Anatomy Dialog
        // Auch hier verwenden Sie getParentFragmentManager()
        getParentFragmentManager().setFragmentResultListener(ANATOMY_REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Log.d("BuyingFragment", "Received result from Anatomy Dialog.");
                numberDialogs--;

                String selectedAnatomyCard = result.getString("selected_card_name");
                if ("Written Record".equals(selectedAnatomyCard)) {
                    Log.d("BuyingFragment", "Anatomy Result: Written Record selected. Showing Extra Credits Dialog.");
                    mCivicViewModel.triggerExtraCreditsDialog(10);
                } else {
                    Log.d("BuyingFragment", "Anatomy Result: Other card selected.");
                    returnToDashboard();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAdvancesBinding.inflate(inflater, container,false);
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
                    savedSelectionState = null; // Nur einmal ausführen
                }
                updateViews();
            }
        });

        if (savedInstanceState != null) {
            savedSelectionState = savedInstanceState;
        }

        // close SoftKeyboard on Enter
        mTreasureInput.setOnEditorActionListener((v, keyCode, event) -> {
            calculateInput(mTreasureInput.getText().toString());
            mCivicViewModel.setTreasure(calculateInput(mTreasureInput.getText().toString()));
                // hide virtual keyboard on enter
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTreasureInput.getWindowToken(), 0);
                return true;
        });

        // button which finalizes the buy process
        binding.btnBuy.setOnClickListener(v -> {
            buyAdvances();
        });

        // button to clear the current selection of cards
        binding.btnClear.setOnClickListener(v -> {
            if (tracker != null) {
                tracker.clearSelection();
                if (mCivicViewModel.librarySelected){
                    mCivicViewModel.librarySelected = false;
                    mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - 40);
                }
            }
        });

        // sort button
        String label = String.valueOf(sortingOptionsNames[sortingIndex].charAt(0));
        if (mCivicViewModel.getScreenWidthDp() <= 400) {
            label = String.valueOf(label.charAt(0));
        }
        binding.btnSort.setText(label);
        binding.btnSort.setOnClickListener(this::changeSorting);



        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the treasure value to zero each time the fragment is entered.
        mCivicViewModel.setTreasure(0);
        mCivicViewModel.setRemaining(0); // Also reset the remaining value

        mCivicViewModel.getShowAnatomyDialogEvent().observe(getViewLifecycleOwner(), event -> {
            List<String> anatomyCardsToShow = event.getContentIfNotHandled(); // Das ist deine List<String>
            if (anatomyCardsToShow != null && !anatomyCardsToShow.isEmpty()) {
                Log.d("BuyingFragment", "Observed Anatomy Dialog Event. Showing dialog with cards: " + anatomyCardsToShow);
                numberDialogs++;
                DialogAnatomyFragment.newInstance(anatomyCardsToShow) // Korrekter Aufruf
                        .show(getParentFragmentManager(), "Anatomy");
            }
        });
        mCivicViewModel.getShowExtraCreditsDialogEvent().observe(getViewLifecycleOwner(), event -> {
            Integer extraCredits = event.getContentIfNotHandled();
            if (extraCredits != null && extraCredits > 0) {
                Log.d("BuyingFragment", "Observed Extra Credits Dialog Event. Showing dialog with " + extraCredits + " credits.");
                numberDialogs++;
                DialogExtraCreditsFragment.newInstance(extraCredits) // Korrekter Aufruf mit newInstance
                        .show(getParentFragmentManager(), "ExtraCredits");
            }
        });

        mCivicViewModel.getNavigateToDashboardEvent().observe(getViewLifecycleOwner(), event -> {
            Boolean shouldNavigate = event.getContentIfNotHandled();
            if (shouldNavigate != null && shouldNavigate) {
                Log.d("BuyingFragment", "ViewModel signaled navigation (purchase complete, no dialogs).");
                // Da keine Dialoge ausgelöst wurden, sollte numberDialogs bereits 0 sein.
                // returnToDashboard wird dies bestätigen und dann navigieren.
                returnToDashboard();
            }
        });
        // tracker to hold the selected cards
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
                Log.v("BuyingFragment", key + " wurde: " + selected);

                if (key.equals("Library")) {
                    if (selected) {
                        // Library was just selected
                        if (!mCivicViewModel.librarySelected) { // Only add if it wasn't already selected
                            mCivicViewModel.librarySelected = true;
                            // Add the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() + 40);
                            showToast(key + " selected, temporary adding 40 treasure.");
                            Log.v("BuyingFragment", key + " selected, temporary adding 40 treasure."); // Add log
                        }
                    } else {
                        // Library was just deselected
                        if (mCivicViewModel.librarySelected) { // Only remove if it was previously selected
                            mCivicViewModel.librarySelected = false; // Reset the flag
                            // Remove the temporary treasure bonus
                            mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - 40);
                            showToast(key + " deselected, removing the temporary 40 treasure.");
                            Log.v("BuyingFragment", key + " deselected, removing the temporary 40 treasure."); // Add log
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
                // You are already calculating total in onItemStateChanged,
                // so this might be redundant or could be used for other updates.
                // Consider if you need calculateTotal here as well.
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
                    if (selectedKey.equals("Library")) { // Directly check for "Library" name
                        libraryIsSelected = true;
                        break;
                    }
                }
                mCivicViewModel.librarySelected = libraryIsSelected; // Set the flag based on restored selection
            }

        });

        mCivicViewModel.getTreasure().observe(requireActivity(), treasure -> {
            mTreasureInput.setText(String.valueOf(treasure));
            if (treasure < mCivicViewModel.getRemaining().getValue()) {
                mCivicViewModel.setRemaining(treasure);
                tracker.clearSelection();
            } else if (tracker != null &&  tracker.getSelection().size() > 0){
                mCivicViewModel.calculateTotal(tracker.getSelection());
            } else {
                mCivicViewModel.setRemaining(treasure);
            }
            // treasure changed, and so does what we might be able to buy.
            mAdapter.notifyDataSetChanged();
            updateViews();
        });

        mCivicViewModel.getRemaining().observe(requireActivity(), remaining -> {
            if (BuyingFragment.this.getContext() != null) {
                mRemainingText.setText(String.valueOf(remaining));
                if (tracker != null) {
                    updateViews();
                }
            }
        });

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Inflate the menu here
                menuInflater.inflate(R.menu.options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Check for the "New Game" menu item
                if (menuItem.getItemId() == R.id.menu_newGame) {
                    // Show the confirmation dialog before starting a new game
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                // Yes button clicked, trigger the reset process in the ViewModel
                                mCivicViewModel.requestNewGame();
                                // The UI update will be handled by the resetEvent observer

                                // --- START: Add Navigation Back to HomeFragment ---
                                // Navigate back to the HomeFragment ONLY when initiating New Game from AdvancesFragment
                                Navigation.findNavController(requireView()).navigate(R.id.homeFragment); // Assuming 'homeFragment' is the ID of your HomeFragment destination in the navigation graph
                                // --- END: Add Navigation Back to HomeFragment ---
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                // No button clicked
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext()
                    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                    return true;
                }

                // Let the NavigationUI handle navigation destinations.
                // This is important for "settingsFragment" and "aboutFragment"
                // if those IDs match destinations in your navigation graph.
                // If NavigationUI handles the item, it returns true.
                return NavigationUI.onNavDestinationSelected(menuItem,
                        Navigation.findNavController(requireView()));
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED); // Register the MenuProvider with the Fragment's lifecycle
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
                            mCardView.setAlpha(0.5F);
                        } else {
                            mCardView.setAlpha(1.0F);
                        }
                    }
                }
            }
        }
    }

    /**
     * after user hits the buy button, this adds all selected advances to the purchase list
     * checks if advances with special bonuses are bought (Written Record, Monument, Anatomy)
     * and pops up a dialog to ask for needed input if needed.
     * Checks then if it is ok to return to the dashboard with returnToDashboard.
     */
    private void buyAdvances() {
        Log.d("BuyingFragment", "buyAdvances() called.");
        // Sammle die Namen der ausgewählten Karten
        List<String> selectedCardNames = new ArrayList<>();
        for (String name : tracker.getSelection()) {
            selectedCardNames.add(name);
        }

        if (selectedCardNames.isEmpty()) {
            showToast("Keine Karten ausgewählt."); // Optional
            Log.d("BuyingFragment", "No cards selected for purchase.");
            return;
        }

        for (String name : selectedCardNames) {
            mCivicViewModel.addBonus(name);
            Log.d("BuyingFragment", "Processing purchase for: " + name);
        }
        mCivicViewModel.saveBonus();
        mCivicViewModel.processPurchases(selectedCardNames);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        tracker.onSaveInstanceState(savedInstanceState);
        // save stuff
        int money = parseInt(mTreasureInput.getText().toString());
        savedInstanceState.putInt(TREASURE_BOX, money);
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
        tracker = null;
        mCivicViewModel.getRemaining().removeObservers(requireActivity());
        mCivicViewModel.getTreasure().removeObservers(requireActivity());
        binding = null;
    }

    public void showToast(String text) {
        if (getContext() != null) Toast.makeText(getContext(), text,Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the application back to the dashboard, but checks first if another dialog
     * must be presented because special cards where bought.
     */
    public void returnToDashboard() { // Kein Parameter mehr
        Log.d("BuyingFragment", "returnToDashboard() called. Number of open dialogs: " + numberDialogs);

        // Nur zum Dashboard zurückkehren, wenn keine Dialoge mehr offen sind
        if (numberDialogs <= 0) { // Benutze <= 0 für den Fall, dass der Zähler negativ wird
            Log.d("BuyingFragment", "returnToDashboard: No open dialogs, navigating back.");
            // mCivicViewModel.calculateCurrentPrice(); // Überprüfen, ob dies noch nötig ist
            // save bonuses to prefs
            ((MainActivity) getActivity()).saveBonus(); // Vielleicht sollte dies im ViewModel sein
            NavHostFragment.findNavController(this).popBackStack();
            mCivicViewModel.setTreasure(0);
            mCivicViewModel.setRemaining(0);
        } else {
            Log.d("BuyingFragment", "returnToDashboard: " + numberDialogs + " dialog(s) still open.");
        }
    }
    /**
     * This gets the list of possible sorting options from the preferences array and cycles
     * through them on each new entry. Gets a new sorted list from the db and updates the adapter.
     * @param v View
     */
    private void changeSorting(View v){
        String label;
        sortingIndex = Arrays.asList(sortingOptionsValues).indexOf(sortingOrder);
        if (sortingIndex == sortingOptionsValues.length - 1){
            sortingOrder = sortingOptionsValues[0];
            label = String.valueOf(sortingOptionsNames[0].charAt(0));
        } else {
            sortingOrder = sortingOptionsValues[sortingIndex + 1];
            label = String.valueOf(sortingOptionsNames[sortingIndex + 1].charAt(0));
        }
        binding.btnSort.setText(label);
        mCivicViewModel.setSortingOrder(sortingOrder);
    }
}