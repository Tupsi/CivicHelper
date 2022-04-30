package org.tesira.mturba.civichelper;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;
import org.tesira.mturba.civichelper.db.Effect;
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
    private SharedPreferences prefs, savedCards;
    private FragmentAdvancesBinding binding;
    private int numberDialogs = 0;
    private LiveData<List<Card>> listCivics;


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
        listCivics = mCivicViewModel.getAllCivics(sortingOrder);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAdvancesBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();
        RecyclerView mRecyclerView = rootView.findViewById(R.id.list);
        final CivicsListAdapter adapter = new CivicsListAdapter(new CivicsListAdapter.CivicsDiff());
        mRecyclerView.setAdapter(adapter);

        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), mColumnCount));
        }
        listCivics.observe(requireActivity(), civics -> {
            adapter.submitList(civics);
        });

        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);
        mCivicViewModel.getTreasure().observe(requireActivity(), treasure -> {
            Log.v("OBSERVER", "Treasure :");
            mTreasureInput.setText(String.valueOf(treasure));
        });
        mCivicViewModel.getRemaining().observe(requireActivity(), remaining -> {
            if (getContext() != null) {
                mRemainingText.setText(requireActivity().getString(R.string.remaining_treasure)+remaining);
            }
        });

        // close SoftKeyboard on Enter
        mTreasureInput.setOnEditorActionListener((v, keyCode, event) -> {
                mCivicViewModel.setTreasure(Integer.parseInt(mTreasureInput.getText().toString()));
                // hide virtual keyboard on enter
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTreasureInput.getWindowToken(), 0);
                return true;
        });

        binding.btnBuy.setOnClickListener(v -> {
            buyAdvances();
//            Navigation.findNavController(v).popBackStack();
        });

        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                new MyItemKeyProvider<String>(ItemKeyProvider.SCOPE_MAPPED, mCivicViewModel
                        .getAllCivics(sortingOrder)),
                new MyItemDetailsLookup(mRecyclerView),
                    StorageStrategy.createStringStorage())
                    .withSelectionPredicate(new MySelectionPredicate<>(this, mCivicViewModel))
                    .build();
        adapter.setSelectionTracker(tracker);
        adapter.setCivicViewModel(mCivicViewModel);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                // item selection changed, we need to redo total selected cost
                Log.v("OBSERVER", "inside onItemStateChanged : " + key);
                mCivicViewModel.calculateTotal(tracker.getSelection());
                // works for auto greying out to expensive cards, but resets view to first item
                // also crashes on long click on some android versions
                //                mRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
                mCivicViewModel.updateIsBuyable();
                Log.v("OBSERVER", "inside onSelectionRefresh");
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                Log.v("OBSERVER", "inside onSelectionChanged");
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
                Log.v("OBSERVER", "inside onSelectionRestored");
            }
        });
        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }
//        setHasOptionsMenu(true);
        return rootView;
    }

    private void buyAdvances() {
        int credits = 0;
        boolean buyAnatomy = false;
        for (String name : tracker.getSelection()) {
            Card adv = mCivicViewModel.getAdvanceByName(name);
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

//    private void addBonus(String name) {
//        Card adv = mCivicViewModel.getAdvanceByName(name);
//        mCivicViewModel.updateBonus(adv.getCreditsBlue(), adv.getCreditsGreen(), adv.getCreditsOrange(), adv.getCreditsRed(), adv.getCreditsYellow());
//    }
//
//    public void addAnatomyFreeCard(String name) {
//        mCivicViewModel.insertPurchase(name);
//        mCivicViewModel.addBonus(name);
//    }

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
        tracker.onSaveInstanceState(savedInstanceState);
        // save stuff
        int money = parseInt(mTreasureInput.getText().toString());
        savedInstanceState.putInt(TREASURE_BOX, money);
    }

    public void onStart() {
        super.onStart();
        Log.v("DEMO","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("DEMO","---> onResume() <--- ");
    }

    public void onPause() {
        super.onPause();
        Log.v("DEMO","---> onPause() <--- ");
    }

    public void onStop() {
        super.onStop();
        Log.v("DEMO","---> onStop() <--- ");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("DEMO","---> onDestroy() <--- ");
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
                NavHostFragment.findNavController(this).popBackStack();
            } else
            {
                numberDialogs--;
            }
        }
    }
}