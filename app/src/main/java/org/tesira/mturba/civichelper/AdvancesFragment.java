package org.tesira.mturba.civichelper;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.MutableSelection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class AdvancesFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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
    private CivicsListAdapter mAdapter;
    MutableSelection<String> currentSelection = new MutableSelection<>();
    private Observer<Integer> mObserver;
    private RecyclerView mRecyclerView;


    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;

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
        mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        listCivics = mCivicViewModel.getAllCivics(sortingOrder);
        binding = FragmentAdvancesBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();
        mRecyclerView = rootView.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            mLayout = new LinearLayoutManager(rootView.getContext());
            mRecyclerView.setLayoutManager(mLayout);
        } else {
            mLayout = new GridLayoutManager(rootView.getContext(), mColumnCount);
            mRecyclerView.setLayoutManager(mLayout);
        }
        mAdapter = new CivicsListAdapter(new CivicsListAdapter.CivicsDiff(), mLayout, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.submitList(listCivics);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);
        mCivicViewModel.getTreasure().observe(requireActivity(), treasure -> {
            Log.v("OBSERVER", "Treasure :" + treasure + " Remaining :" + mCivicViewModel.getRemaining().getValue());
            mTreasureInput.setText(String.valueOf(treasure));
            if (treasure < mCivicViewModel.getRemaining().getValue()) {
                mCivicViewModel.setRemaining(treasure);
                tracker.clearSelection();
            } else if (tracker != null &&  tracker.getSelection().size() > 0){
                mCivicViewModel.calculateTotal(tracker.getSelection());
            } else {
                mCivicViewModel.setRemaining(treasure);
            }
            mAdapter.notifyDataSetChanged();
            updateViews();
//            mRecyclerView.setAdapter(mAdapter);
        });
        mObserver = remaining -> {
            Log.v("OBSERVER", "treasure remaining : " + remaining);
            if (AdvancesFragment.this.getContext() != null) {
                mRemainingText.setText(String.valueOf(remaining));
                if (tracker != null) {
                    updateViews();
                }
            }
        };

        mCivicViewModel.getRemaining().observe(requireActivity(), mObserver);
//        mCivicViewModel.getRemaining().observe(requireActivity(), new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer remaining) {
//                Log.v("OBSERVER", "treasure remaining : " + remaining);
//                if (AdvancesFragment.this.getContext() != null) {
//                    mRemainingText.setText(String.valueOf(remaining));
//                    if (tracker != null) {
////                        mAdapter.notifyDataSetChanged();
//                        updateAdaper();
//                    }
//                }
//            }
//        });

        // close SoftKeyboard on Enter
        mTreasureInput.setOnEditorActionListener((v, keyCode, event) -> {
            calculateInput(mTreasureInput.getText().toString());
            mCivicViewModel.setTreasure(calculateInput(mTreasureInput.getText().toString()));
//                mCivicViewModel.setTreasure(Integer.parseInt(mTreasureInput.getText().toString()));
                // hide virtual keyboard on enter
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTreasureInput.getWindowToken(), 0);
//                mCivicViewModel.calculateTotal(currentSelection);
                return true;
        });
//        mTreasureInput.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // If the event is a key-down event on the "enter" button
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // Perform action on key press
////                    Toast.makeText(v.getContext(), mTreasureInput.getText(), Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//                return false;
//            }
//        });

        binding.btnBuy.setOnClickListener(v -> {
            buyAdvances();
//            Navigation.findNavController(v).popBackStack();
        });
        myItemKeyProvider = new MyItemKeyProvider<String>(ItemKeyProvider.SCOPE_MAPPED, listCivics, mCivicViewModel);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                myItemKeyProvider,
                new MyItemDetailsLookup(mRecyclerView),
                    StorageStrategy.createStringStorage())
                    .withSelectionPredicate(new MySelectionPredicate<>(this, mCivicViewModel))
                    .build();
        mAdapter.setSelectionTracker(tracker);
        mAdapter.setCivicViewModel(mCivicViewModel);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
//                Log.v("OBSERVER", "inside onItemStateChanged : " + key + " : " + selected);
//                mCivicViewModel.calculateTotal(tracker.getSelection());
//                tracker.copySelection(currentSelection);
                mCivicViewModel.calculateTotal(tracker.getSelection());
                if (tracker.getSelection().size() == 0) {
                    mCivicViewModel.setRemaining(mCivicViewModel.getTreasure().getValue());
                }
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
//                Log.v("OBSERVER", "inside onSelectionRefresh");

            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
//                Log.v("OBSERVER", "inside onSelectionChanged");
//                mCivicViewModel.calculateTotal(currentSelection);
//                if (currentSelection.size() == 0) {
//                    mCivicViewModel.setRemaining(mCivicViewModel.getTreasure().getValue());
//                }
//                updateViews();
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
                Log.v("OBSERVER", "inside onSelectionRestored");
            }

        });
        if (savedInstanceState != null) {
            Log.v("TRACKER", "Tracker restores state");
            Log.v("TRACKER", "size before restore: " + tracker.getSelection().size());
            tracker.onRestoreInstanceState(savedInstanceState);
            Log.v("TRACKER", "size after restore: " + tracker.getSelection().size());

        }

        //        setHasOptionsMenu(true);
        return rootView;
    }

    private int calculateInput(String treasureinput) {
        String [] pieces = treasureinput.trim().split("\\D+");
        int result = 0;
        for (String number:pieces) {
            Log.v("NUMBERS", number);
            result += Integer.parseInt(number);
        }
        Log.v("NUMBERS", "Result: "+ result);
        return result;
    }

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

    private void checkBuyable() {
        int first = mLayout.findFirstVisibleItemPosition();
        int last = mLayout.findLastVisibleItemPosition();
        Log.v("VISIBLE", " first : " + first + " : last : " + last);
        for (int i = first; i <= last; i++) {
            Log.v("VISIBLE","card : " + mAdapter.getCurrentList().get(i).getName());
        }
//        int i1 = findFirstVisibleItemPosition();
//        int findFirstCompletelyVisibleItemPosition();
//        int findLastVisibleItemPosition();
//        int findLastCompletelyVisibleItemPosition();
    }

    private void buyAdvances() {
        int credits = 0;
        boolean buyAnatomy = false;
        for (String name : tracker.getSelection()) {
//            Card adv = mCivicViewModel.getAdvanceByName(name);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "sort" :
                sortingOrder = sharedPreferences.getString("sort", "name");
                if (sortingOrder.equals("family")) {
                    sharedPreferences.edit().putString("columns","3").apply();
                }
            case "name" :
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.v("TRACKER", "inside onSavedInstanceState");
        Log.v("TRACKER", " size on save: " + tracker.getSelection().size());
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
//        tracker = null;
        mCivicViewModel.getRemaining().removeObservers(requireActivity());
        mCivicViewModel.getTreasure().removeObservers(requireActivity());
//        binding = null;
        Log.v("ADVANCES","---> onDestroy() <--- ");
    }

    public void showToast(String text) {
        if (getContext() != null) Toast.makeText(getContext(), text,Toast.LENGTH_LONG).show();
    }

//    @Override
//    public void onDialogPositiveClick(DialogFragment dialog) {
//        NavHostFragment.findNavController(this).popBackStack();
//    }

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
                //TODO remove used treasure from field and save new value to pref
                // works, just uncomment
                //mCivicViewModel.setTreasure(mCivicViewModel.getTreasure().getValue() - mCivicViewModel.getTotal().getValue());

                // save bonuses to prefs
                ((MainActivity) getActivity()).saveBonus();
//                mCivicViewModel.setRemaining(mCivicViewModel.getTreasure().getValue());
                mCivicViewModel.setRemaining(0);
                mCivicViewModel.setTreasure(0);
                NavHostFragment.findNavController(this).popBackStack();
            } else
            {
                numberDialogs--;
            }
        }
    }
}