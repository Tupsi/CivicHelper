package org.tesira.mturba.civichelper;

import static java.lang.Integer.parseInt;
import android.content.Context;
import android.content.Intent;
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
import android.view.ContextMenu;
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
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import org.tesira.mturba.civichelper.db.Effect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
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




//    private static final String ARG_COLUMN_COUNT = "column-count";
//    /**
//     * Mandatory empty constructor for the fragment manager to instantiate the
//     * fragment (e.g. upon screen orientation changes).
//     */
//    public AdvancesFragment() {
//    }
//
//    @SuppressWarnings("unused")
//    public static AdvancesFragment newInstance(int columnCount) {
//        AdvancesFragment fragment = new AdvancesFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
//        return fragment;
//    }

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
//        mAdapter = new CivicsListAdapter(new CivicsListAdapter.CivicsDiff(), mLayout, this);
        mAdapter = new BuyingListAdapter(listCivics, mCivicViewModel);
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.submitList(listCivics);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);
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

        binding.btnBuy.setOnClickListener(v -> {
            buyAdvances();
//            Navigation.findNavController(v).popBackStack();
        });
        binding.btnClear.setOnClickListener(v -> {
            if (tracker != null) {
                tracker.clearSelection();
            }
        });
        binding.btnSort.setText(sortingOptionsNames[sortingIndex]);
        binding.btnSort.setOnClickListener(v -> {
            // cycle through sorting options, get a new list and notify adapter
            sortingIndex = Arrays.asList(sortingOptionsValues).indexOf(sortingOrder);
            if (sortingIndex == sortingOptionsValues.length-1){
                sortingOrder = sortingOptionsValues[0];
                binding.btnSort.setText(sortingOptionsNames[0]);
            } else {
                sortingOrder = sortingOptionsValues[sortingIndex+1];
                binding.btnSort.setText(sortingOptionsNames[sortingIndex+1]);
            }
//            Log.v("SORTING", ""+sortingOrder+ ":"+ sortingIndex);
            listCivics = mCivicViewModel.getAllAdvancesNotBought(sortingOrder);
            mAdapter.changeList(listCivics);
        });

        myItemKeyProvider = new MyItemKeyProvider<String>(ItemKeyProvider.SCOPE_MAPPED, mCivicViewModel);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                myItemKeyProvider,
                new MyItemDetailsLookup(mRecyclerView),
                    StorageStrategy.createStringStorage())
                    .withSelectionPredicate(new MySelectionPredicate<>(this, mCivicViewModel))
                    .build();
        mAdapter.setSelectionTracker(tracker);
//        mAdapter.setCivicViewModel(mCivicViewModel);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
                mCivicViewModel.calculateTotal(tracker.getSelection());
                if (tracker.getSelection().size() == 0) {
                    mCivicViewModel.setRemaining(mCivicViewModel.getTreasure().getValue());
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
            }

        });
        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

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
            Log.v("BUY", "Adding " + name);
            mCivicViewModel.insertPurchase(name);
            mCivicViewModel.addBonus(name);
            if (name.equals("Anatomy")) buyAnatomy = true;
            if (effects.size() == 1) {
                credits += effects.get(0).getValue();
            }
            Log.v("CREDITS", "credits beim Kauf : " + name + " : " + credits);
        }
        if ((mCivicViewModel.getAnatomyCards().size() > 0) &&  buyAnatomy) {
            numberDialogs++;
            new AnatomyDialogFragment(mCivicViewModel,this, mCivicViewModel.getAnatomyCards()).show(getParentFragmentManager(), "Anatomy");
        }

        if (credits > 0) {
            numberDialogs++;
            new ExtraCreditsDialogFragment(mCivicViewModel,this,credits).show(getParentFragmentManager(), "ExtraCredits");
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
        Log.v("ADVANCES","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("ADVANCES","---> onResume() <--- ");
    }

    public void onPause() {
        super.onPause();
        Log.v("ADVANCES","---> onPause() <--- ");
    }

    public void onStop() {
        super.onStop();
        Log.v("ADVANCES","---> onStop() <--- ");
    }

    public void onDestroy() {
        super.onDestroy();
        tracker = null;
        mCivicViewModel.getRemaining().removeObservers(requireActivity());
        mCivicViewModel.getTreasure().removeObservers(requireActivity());
        binding = null;
        Log.v("ADVANCES","---> onDestroy() <--- ");
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
        if (tookWrittenRecord) {
            new ExtraCreditsDialogFragment( mCivicViewModel,this,10).show(getParentFragmentManager(), "ExtraCredits");
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
}