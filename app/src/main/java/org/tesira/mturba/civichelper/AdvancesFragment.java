package org.tesira.mturba.civichelper;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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

import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.card.CardColor;
import org.tesira.mturba.civichelper.card.Credit;
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;
import org.tesira.mturba.civichelper.db.Card;
import org.tesira.mturba.civichelper.db.CivicViewModel;

import java.util.List;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 */
public class AdvancesFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
//        ExtraCreditsDialogFragment.ExtracCreditsDialogListener {

    private static final String ADVANCES_LIST = "advancesList";
    private static final String TREASURE_BOX = "treasure";
    private static final String MONEY_LEFT = "moneyLeft";
    private static final String FILENAME = "advances.xml";
    private static final String PURCHASED = "purchasedAdvances";
    private static final String FAMILY = "familyBonus";

    private CivicViewModel mCivicViewModel;

    // arraylist of all civilization cards
    public List<Advance> advances;
    private MyAdvancesRecyclerViewAdapter adapter;
    private String sortingOrder;
    protected RecyclerView mRecyclerVie;
    protected EditText mTreasureInput;
    protected TextView mRemainingText;
    private SelectionTracker<String> tracker;
    private SharedPreferences prefs, savedCards;
//    private int total;
//    private int treasure;
    private FragmentAdvancesBinding binding;
    private Set<String> purchasedAdvances;
//    private int bonusRed;
//    private int bonusGreen;
//    private int bonusBlue;
//    private int bonusYellow;
//    private int bonusOrange;
    private Set<String> bonusFamily;
    private Set<String> greenCardsAnatomy;
    private int numberDialogs = 0;


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

        //        savedCards = this.getActivity().getSharedPreferences(PURCHASED, Context.MODE_PRIVATE );
//        prefs.registerOnSharedPreferenceChangeListener(this);
//        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
//        }
//        // Set<String> s = new HashSet<String>(sharedPrefs.getStringSet("key", new HashSet<String>()));
//        purchasedAdvances = savedCards.getStringSet(PURCHASED, new HashSet<>());
//        bonusFamily = savedCards.getStringSet(FAMILY, new HashSet<>());
//        loadBonus();
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

//        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        mCivicViewModel.getAllCivics(sortingOrder).observe(requireActivity(), civics -> {
            adapter.submitList(civics);
        });

        mTreasureInput = rootView.findViewById(R.id.treasure);
        mRemainingText = rootView.findViewById(R.id.moneyleft);
//        total = 0;
//        treasure = prefs.getInt(TREASURE_BOX,0);
//        mCivicViewModel.setTreasure(prefs.getInt(TREASURE_BOX,0));
        mCivicViewModel.getTreasure().observe(requireActivity(), treasure -> mTreasureInput.setText(String.valueOf(treasure)));
//        mCivicViewModel.getTotal().observe(requireActivity(), new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer total) {
//                if (getContext() != null) {
//                    mRemainingText.setText(getString(R.string.remaining_treasure)+(mCivicViewModel.getTreasure().getValue() - total));
//                }
//
//            }
//        });

        mCivicViewModel.getRemaining().observe(requireActivity(), remaining -> {
//            Log.v("CONTEXT", "aussen");
            if (getContext() != null) {
//                Log.v("CONTEXT", "innen");
                mRemainingText.setText(requireActivity().getString(R.string.remaining_treasure)+remaining);
            }
            else {
//                Log.v("CONTEXT", "else ohne context");
            }
        });


//        mRemainingText.setText(getString(R.string.remaining_treasure)+(mCivicViewModel.getTreasure() - mCivicViewModel.getTotal()));
//        mTreasureInput.setText(String.valueOf(mCivicViewModel.getTreasure()));
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

//        mTreasureInput = rootView.findViewById(R.id.treasure);
//        mBuyPrice = rootView.findViewById(R.id.moneyleft);
//        advances = new ArrayList<>();
//
//        if (savedInstanceState != null) {
//            Log.v("save", "savedInstanceState YES/if");
//        } else {
//            Log.v("save", "savedInstanceState NO/else");
//        }
//        loadVars();
//        importAdvances(advances, FILENAME);
//        setTotal(0);
//
//        // sorting the cards
//        switch (sortingOrder) {
//            case "price" :
////                Collections.sort(advances);
//                advances.sort(Comparator.comparing(Advance::getPrice).thenComparing(Advance::getName));
//                break;
//            case "color":
//                advances.sort(Comparator.comparing(Advance::getPrimaryColor).thenComparing(Advance::getPrice));
//                break;
//            case "family":
//                advances.sort(Comparator.comparing(Advance::getFamily).thenComparing(Advance::getVp));
//                break;
//            case "name" :
//            default :
////                Collections.sort(advances, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
//                advances.sort(Comparator.comparing(Advance::getName));
//                break;
//        }
//        removePurchasedAdvances();

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
                // item got deselected, need to redo total selected
//                if (!selected) {
//                    setTotal(calculateTotal());
//                }
                Log.v("OBSERVER", "inside onItemStateChanged : " + key);
                mCivicViewModel.setTotal(calculateTotal());
//                int rest = treasure - total;

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
//        setHasOptionsMenu(true);
//        if (savedInstanceState != null) {
//            tracker.onRestoreInstanceState(savedInstanceState);
//        }
//        // we want free stuff already selected
//        selectZeroCostAdvances();
//        mTreasureInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                updateRemaining();
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//        mTreasureInput.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                if (treasure < total) {
//                    tracker.clearSelection();
//                }
//            }
//        });

        return rootView;
    }

    private void selectZeroCostAdvances() {
        for (Advance adv: advances) {
            if (adv.getPrice() == 0) {
                tracker.select(adv.getName());
            }
        }
    }

    /**
     * Removes all already bought advances from the ArrayList.
     */
    private void removePurchasedAdvances() {
        for (String name: purchasedAdvances) {
            Advance adv = Advance.getAdvanceFromName(advances, name);
            advances.remove(adv);
        }
    }
    private void buyAdvances() {
        int credits = 0;
        boolean buyAnatomy = false;
        for (String name : tracker.getSelection()) {
            Card adv = mCivicViewModel.getAdvanceByName(name);
            // add to list of bought cards
            Log.v("BUY", "Adding " + name);
            mCivicViewModel.insertPurchase(name);
            addBonus(name);



            //            purchasedAdvances.add(name);

            // remove from possible buy option for Anatomy
//
//            greenCardsAnatomy.remove(name);
//            if (name.equals("Anatomy")) buyAnatomy = true;
//            bonusFamily.add(adv.getFamilyname());
//            Integer effect = adv.getEffects().get("Credits");
//            if (effect != null) {
//                credits += effect;
//            }
//        }
//        if ((greenCardsAnatomy.size() > 0) &&  buyAnatomy) {
//            numberDialogs++;
//            new AnatomyDialogFragment(this, greenCardsAnatomy).show(getParentFragmentManager(), "Anatomy");
//        }
//
//        if (credits > 0) {
//            numberDialogs++;
//            new ExtraCreditsDialogFragment(this,credits).show(getParentFragmentManager(), "ExtraCredits");
//        }
//        returnToDashboard(false);
        }
    }

    private void addBonus(String name) {
        Card adv = mCivicViewModel.getAdvanceByName(name);
        int newValue;
        newValue = adv.getCreditsBlue() + mCivicViewModel.getBonusBlue();
        mCivicViewModel.setBonusBlue(newValue);;
        newValue = adv.getCreditsGreen() + mCivicViewModel.getBonusGreen();
        mCivicViewModel.setBonusGreen(newValue);
        newValue = adv.getCreditsOrange() + mCivicViewModel.getBonusOrange();
        mCivicViewModel.setBonusOrange(newValue);
        newValue = adv.getCreditsRed() + mCivicViewModel.getBonusRed();
        mCivicViewModel.setBonusRed(newValue);
        newValue = adv.getCreditsYellow() + mCivicViewModel.getBonusYellow();
        mCivicViewModel.setBonusYellow(newValue);
    }

    public void addAnatomyFreeCard(String name) {
        mCivicViewModel.insertPurchase(name);
        addBonus(name);
    }

    /**
     * Calculates the sum of all currently selected advances during the buy process.
     * @return Total price.
     */
    public int calculateTotal() {
        int total = 0;
        for (String name : tracker.getSelection()) {
            Card adv = mCivicViewModel.getAdvanceByName(name);
            int currentPrice = adv.getPrice();
            total += currentPrice;
        }
        return total;
    }

    private void setCurrentPrice() {
        for (Advance adv: advances) {
            int current = adv.getPrice();
            if (adv.getGroups().size() == 1 ) {
                current -= getReductionFromGroup(adv.getGroups().get(0));
            } else {
                int group1 = getReductionFromGroup(adv.getGroups().get(0));
                int group2 = getReductionFromGroup(adv.getGroups().get(1));
                current -= Math.max(group1, group2);
            }
            if (bonusFamily.contains(adv.getName())) {
//                Log.d("FamilyBonus", "hat familiy bonus :" + adv.getName());
                if (adv.getVp() == 3) {
                    current -= 10;
                } else {
                    current -= 20;
                }
            }
            if (current < 0) {
                current = 0;
            }
            adv.setPrice(current);
        }
    }

    private int getReductionFromGroup(CardColor cardColor) {
        int bonus = 0;
        switch (cardColor) {
            case BLUE:
                bonus = mCivicViewModel.getBonusBlue();
                break;
            case YELLOW:
                bonus = mCivicViewModel.getBonusYellow();
                break;
            case ORANGE:
                bonus = mCivicViewModel.getBonusOrange();
                break;
            case GREEN:
                bonus = mCivicViewModel.getBonusGreen();
                break;
            case RED:
                bonus = mCivicViewModel.getBonusRed();
                break;
        }
        return bonus;
    }

   
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
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
//        saveVars();
//        saveBonus();
    }

    public void onStop() {
        super.onStop();
        Log.v("DEMO","---> onStop() <--- ");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("DEMO","---> onDestroy() <--- ");
    }


//    /**
//     * loads already bought bonuses from SharePreferences file
//     */
//    public void loadBonus() {
//        bonusRed = savedCards.getInt("bonusRed", 0);
//        bonusGreen = savedCards.getInt("bonusGreen", 0);
//        bonusBlue = savedCards.getInt("bonusBlue", 0);
//        bonusYellow = savedCards.getInt("bonusYellow", 0);
//        bonusOrange = savedCards.getInt("bonusOrange", 0);
//    }

    /**
     * saves already bought bonuses to SharedPreferences file
     */
    public void saveBonus() {
//        SharedPreferences.Editor editor = savedCards.edit();
//        editor.putInt("bonusBlue", bonusBlue);
//        editor.putInt("bonusGreen", bonusGreen);
//        editor.putInt("bonusOrange", bonusOrange);
//        editor.putInt("bonusRed", bonusRed);
//        editor.putInt("bonusYellow", bonusYellow);
//        editor.commit();
//        Log.v("DEMO", "saveBonus in Advanced");

    }

    public void showToast(String text) {
        Toast.makeText(getContext(), text,Toast.LENGTH_LONG).show();
    }

//    public void updateBonus(int blue, int green, int orange, int red, int yellow) {
//        bonusBlue += blue;
//        bonusGreen += green;
//        bonusOrange += orange;
//        bonusRed += red;
//        bonusYellow += yellow;
//        saveBonus();
//    }

//    @Override
//    public void onDialogPositiveClick(DialogFragment dialog) {
//        NavHostFragment.findNavController(this).popBackStack();
//    }

    public void returnToDashboard(boolean tookWrittenRecord) {
        if (tookWrittenRecord) {
            new ExtraCreditsDialogFragment(this,10).show(getParentFragmentManager(), "ExtraCredits");
        } else {
            if (numberDialogs == 0) {
                NavHostFragment.findNavController(this).popBackStack();
            } else
            {
                numberDialogs--;
            }
        }
    }
}