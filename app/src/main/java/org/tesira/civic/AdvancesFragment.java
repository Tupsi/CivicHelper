package org.tesira.civic;

import static java.lang.Integer.parseInt;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;
import org.tesira.civic.db.Effect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment for the buy process. User can input a treasure sum and select up to that value
 * cards. Pressing the buy button finishes the buying process, adding selected cards to the
 * purchases list and returns the user back to the dashboard.
 */
public class AdvancesFragment extends Fragment {

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
    private MyItemKeyProvider<String> myItemKeyProvider;
    private LinearLayoutManager mLayout;
    private BuyingListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mColumnCount = 2;
    private int sortingIndex;
    private String[] sortingOptionsValues, sortingOptionsNames;

    private int oldblue, oldgreen, oldorange, oldred, oldyellow;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        listCivics = mCivicViewModel.getAllAdvancesNotBought(sortingOrder);
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
        mAdapter = new BuyingListAdapter(listCivics, mCivicViewModel);
        mRecyclerView.setAdapter(mAdapter);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);

        // store current card bonuses in case someone buys written record
        oldblue = mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.BLUE,0);
        oldgreen = mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.GREEN,0);
        oldorange = mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.ORANGE,0);
        oldred = mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.RED,0);
        oldyellow = mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.YELLOW,0);
        Log.v("farbe","---> onCreate() <--- ");
        Log.v("farbe", String.format("%d %d %d %d %d", oldblue, oldgreen, oldorange, oldred, oldyellow));
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
            if (AdvancesFragment.this.getContext() != null) {
                mRemainingText.setText(String.valueOf(remaining));
                if (tracker != null) {
                    updateViews();
                }
            }
        });

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
//            Navigation.findNavController(v).popBackStack();
        });

        // button to clear the current selection of cards
        binding.btnClear.setOnClickListener(v -> {
            if (tracker != null) {
                tracker.clearSelection();
            }
        });

        // sort button
        String label = String.valueOf(sortingOptionsNames[sortingIndex].charAt(0));
        if (mCivicViewModel.getScreenWidthDp() <= 400) {
            label = String.valueOf(label.charAt(0));
        }
        binding.btnSort.setText(label);
        binding.btnSort.setOnClickListener(this::changeSorting);

        // tracker to hold the selected cards
        myItemKeyProvider = new MyItemKeyProvider<String>(ItemKeyProvider.SCOPE_MAPPED, mCivicViewModel);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                myItemKeyProvider,
                new MyItemDetailsLookup(mRecyclerView),
                    StorageStrategy.createStringStorage())
                    .withSelectionPredicate(new MySelectionPredicate<>(this, mCivicViewModel))
                    .build();
        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }
        mAdapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
                mCivicViewModel.calculateTotal(tracker.getSelection());
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
            }

        });

        //        setHasOptionsMenu(true);
        return rootView;
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
        int credits = 0;
        boolean buyAnatomy = false;

        for (String name : tracker.getSelection()) {
            List<Effect> effects = mCivicViewModel.getEffect(name,"Credits");
            // add to list of bought cards
            mCivicViewModel.insertPurchase(name);
            mCivicViewModel.addBonus(name);
            if (name.equals("Anatomy")) buyAnatomy = true;
            if (effects.size() == 1) {
                credits += effects.get(0).getValue();
            }
        }
        if ((mCivicViewModel.getAnatomyCards().size() > 0) &&  buyAnatomy) {
            numberDialogs++;
            new AnatomyDialogFragment(mCivicViewModel,this, mCivicViewModel.getAnatomyCards()).show(getParentFragmentManager(), "Anatomy");
        }

        if (credits > 0) {
            numberDialogs++;
            new ExtraCreditsDialogFragment(mCivicViewModel,this,credits, oldblue, oldgreen, oldorange, oldred, oldyellow).show(getParentFragmentManager(), "ExtraCredits");
        }
        returnToDashboard(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item,
                Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
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
//        Log.v("ADVANCES","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
//        Log.v("ADVANCES","---> onResume() <--- ");
    }

    public void onPause() {
        super.onPause();
//        Log.v("ADVANCES","---> onPause() <--- ");
    }

    public void onStop() {
        super.onStop();
//        Log.v("ADVANCES","---> onStop() <--- ");
    }

    public void onDestroy() {
        super.onDestroy();
        tracker = null;
        mCivicViewModel.getRemaining().removeObservers(requireActivity());
        mCivicViewModel.getTreasure().removeObservers(requireActivity());
        binding = null;
//        Log.v("ADVANCES","---> onDestroy() <--- ");
    }

    public void showToast(String text) {
        if (getContext() != null) Toast.makeText(getContext(), text,Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the application back to the dashboard, but checks first if another dialog
     * must be presented because special cards where bought.
     * @param tookWrittenRecord True if Anatomy was bought and the selected free card
     *                          is Written Record.
     */
    public void returnToDashboard(boolean tookWrittenRecord) {
        // if the card selected in with Anatomy is Written Record,
        // we have to open the extra Credits Dialog
//        Log.v("farbe","---> returnToDashboard() <--- ");
//        Log.v("farbe", String.format("%d %d %d %d %d", oldblue, oldgreen, oldorange, oldred, oldyellow));

        if (tookWrittenRecord) {
            new ExtraCreditsDialogFragment( mCivicViewModel,this,10, oldblue, oldgreen, oldorange, oldred, oldyellow).show(getParentFragmentManager(), "ExtraCredits");
        } else {
            if (numberDialogs == 0) {
                mCivicViewModel.calculateCurrentPrice();
                // save bonuses to prefs
                ((MainActivity) getActivity()).saveBonus();
                NavHostFragment.findNavController(this).popBackStack();
                mCivicViewModel.setTreasure(0);
                mCivicViewModel.setRemaining(0);
            } else
            {
                numberDialogs--;
            }
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
        if (sortingIndex == sortingOptionsValues.length-1){
            sortingOrder = sortingOptionsValues[0];
            label = String.valueOf(sortingOptionsNames[0].charAt(0));
        } else {
            sortingOrder = sortingOptionsValues[sortingIndex+1];
            label = String.valueOf(sortingOptionsNames[sortingIndex+1].charAt(0));
        }
        binding.btnSort.setText(label);
        listCivics = mCivicViewModel.getAllAdvancesNotBought(sortingOrder);
        mAdapter.changeList(listCivics);
    }
}