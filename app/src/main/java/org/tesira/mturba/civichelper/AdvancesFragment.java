package org.tesira.mturba.civichelper;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.card.CardColor;
import org.tesira.mturba.civichelper.card.Credit;
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;
import org.tesira.mturba.civichelper.databinding.FragmentHomeBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A fragment representing a list of Items.
 */
public class AdvancesFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ADVANCES_LIST = "advancesList";
    private static final String TREASURE_BOX = "treasure";
    private static final String MONEY_LEFT = "moneyLeft";
    private static final String FILENAME = "advances.xml";
    private static final String PURCHASED = "purchasedAdvances";
    // arraylist of all civilization cards
    public List<Advance> advances;
    private MyAdvancesRecyclerViewAdapter adapter;
    private SharedPreferences prefs, savedCards;
    private String sortingOrder;
    protected RecyclerView mRecyclerView;
    protected EditText mTreasureInput;
    protected TextView mBuyPrice;
    private SelectionTracker<String> tracker;
    private int total;
    private int treasure;
    private FragmentAdvancesBinding binding;
    private Set<String> purchasedAdvances;
    private int bonusRed;
    private int bonusGreen;
    private int bonusBlue;
    private int bonusYellow;
    private int bonusOrange;



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
        sortingOrder = prefs.getString("sort", "name");
        mColumnCount = Integer.parseInt(prefs.getString("columns", "1"));
        prefs.registerOnSharedPreferenceChangeListener(this);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        savedCards = this.getActivity().getSharedPreferences(PURCHASED, Context.MODE_PRIVATE );
        purchasedAdvances = savedCards.getStringSet(PURCHASED, new HashSet<>());
        loadBonus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdvancesBinding.inflate(inflater, container,false);
        View rootView = binding.getRoot();
//        View rootView = inflater.inflate(R.layout.fragment_advances, container, false);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mBuyPrice = rootView.findViewById(R.id.moneyleft);
        advances = new ArrayList<>();
        binding.btnBuy.setOnClickListener(v -> {
            buyAdvances();
            Navigation.findNavController(v).popBackStack();
        });

        if (savedInstanceState != null) {
            Log.v("save", "savedInstanceState YES/if");
        } else {
            Log.v("save", "savedInstanceState NO/else");
        }
        loadVars();
        importAdvances(advances, FILENAME);
        setTotal(0);

        // sorting the cards
        switch (sortingOrder) {
            case "price" :
//                Collections.sort(advances);
                advances.sort(Comparator.comparing(Advance::getPrice).thenComparing(Advance::getName));
                break;
            case "name" :
            default :
//                Collections.sort(advances, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
                advances.sort(Comparator.comparing(Advance::getName));
                break;
        }
        removePurchasedAdvances();

        // Set the adapter
        Context context = rootView.getContext();
        mRecyclerView = rootView.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new MyAdvancesRecyclerViewAdapter(advances, context);
        adapter.setRemainingTreasure(treasure);
        mRecyclerView.setAdapter(adapter);
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                new MyItemKeyProvider<String>(ItemKeyProvider.SCOPE_MAPPED, advances, adapter),
                new MyItemDetailsLookup(mRecyclerView),
                StorageStrategy.createStringStorage())
               .withSelectionPredicate(new MySelectionPredicate<>(this, advances)).build();
        adapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
//                Log.v("TRACKER", "onItemStateChanged : " + key + " : " + selected);
                // item got deselected, need to redo total selected
                if (!selected) {
                    setTotal(calculateTotal());
                }
                int rest = treasure - total;
                adapter.setRemainingTreasure(rest);
                mRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
//                Log.v("TRACKER", "onSelectionRefresh fired");
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
//                Log.v("TRACKER", "onSelectionChanged fired");
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
            }
        });
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }
        selectZeroCostAdvances();
        mTreasureInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRemaining();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mTreasureInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (treasure < total) {
                    tracker.clearSelection();
                }
            }
        });
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
            Advance adv = advances.get(advances.get(0).getIndexFromName(advances, name));
            advances.remove(adv);
        }
    }

    private void buyAdvances() {
        Log.v("Button", "Buy button pressed!");
        for (String name: tracker.getSelection()) {
            Log.v("Button", name);
            purchasedAdvances.add(name);
            addBonus(name);
        }
    }

    private void addBonus(String name) {
        Advance adv = Advance.getAdvanceFromName(advances, name);
        for (Credit credit: adv.getCredits()) {
            Log.v("Credits", credit.getGroup().getName() + " : " + credit.getValue());
            switch (credit.getGroup()) {
                case BLUE :
                    bonusBlue += credit.getValue();
                    break;
                case RED:
                    bonusRed += credit.getValue();
                    break;
                case GREEN:
                    bonusGreen += credit.getValue();
                    break;
                case ORANGE:
                    bonusOrange += credit.getValue();
                    break;
                case YELLOW:
                    bonusYellow += credit.getValue();
                    break;
            }
        }
    }

    /**
     * @return Current Treasure from input field.
     */
    public int getTreasure() {
        String treasureInput = mTreasureInput.getText().toString();
        if (treasureInput.isEmpty()) {
            treasure = 0;
        } else {
            treasure = Integer.parseInt(treasureInput);
        }

//        Log.v("treasure", "getTreasure :" + treasure);
        return treasure;
    }

    public void updateRemaining() {
        getTreasure();
        mBuyPrice.setText(getString(R.string.remaining_treasure)+(treasure-total));
    }
    @SuppressLint("SetTextI18n")
    public void setTotal(int total) {
        this.total = total;
        // showing not total on screen but remaining
        mBuyPrice.setText(getString(R.string.remaining_treasure)+(treasure-total));
    }

    /**
     * Calculates the sum of all currently selected advances during the buy process.
     * @return Total price.
     */
    public int calculateTotal() {
        total = 0;
        for (String name : tracker.getSelection()) {
            int idx = advances.get(0).getIndexFromName(advances, name);
            Advance adv = Advance.getAdvanceFromName(advances, name);

            int currentPrice = advances.get(idx).getPrice();
            total += currentPrice;
        }
        return total;
    }

    /**
     * Imports all civilization advances from file into an ArrayList to be used in the RecyclerView.
     * @param advances The ArrayList in which to import the civilization advances to.
     * @param filename The filename of the xml data of all civilization advances.
     */
    private void importAdvances(List<Advance> advances, String filename) {
        try {
            InputStream is = requireActivity().getAssets().open(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            Element element = doc.getDocumentElement();
            element.normalize();
            NodeList nList = doc.getElementsByTagName("advance");
            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Advance advance = new Advance();
                    String readElement;
                    Element element2 = (Element) node;
                    readElement = element2.getElementsByTagName("name").item(0).getTextContent();
                    advance.setName(readElement);
                    readElement = element2.getElementsByTagName("family").item(0).getTextContent();
                    advance.setFamily(parseInt(readElement));
                    readElement = element2.getElementsByTagName("vp").item(0).getTextContent();
                    advance.setVp(parseInt(readElement));
                    readElement = element2.getElementsByTagName("price").item(0).getTextContent();
                    advance.setPrice(parseInt(readElement));
                    for (int x=0; x<element2.getElementsByTagName("group").getLength();x++) {
                        advance.addGroup(element2.getElementsByTagName("group").item(x).getTextContent());
                    }
                    for (int x=0; x<element2.getElementsByTagName("credit").getLength(); x++) {
                        String color = element2.getElementsByTagName("credit").item(x).getAttributes().item(0).getTextContent();
                        int discount = parseInt(element2.getElementsByTagName("credit").item(x).getTextContent());
                        advance.addCredits(color, discount);
                    }
                    for (int x=0; x<element2.getElementsByTagName("effect").getLength(); x++) {
                        String name = element2.getElementsByTagName("effect").item(x).getAttributes().item(0).getTextContent();
                        int value = parseInt(element2.getElementsByTagName("effect").item(x).getTextContent());
                        advance.addEffect(name, value);
                    }
                    int current = calculateCurrentPrice(advance);
                    advance.setPrice(current);
                    advances.add(advance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int calculateCurrentPrice(Advance advance) {
        int current = advance.getPrice();
        if (advance.getGroups().size() == 1 ) {
            current -= getReductionFromGroup(advance.getGroups().get(0));
        } else {
            int group1 = getReductionFromGroup(advance.getGroups().get(0));
            int group2 = getReductionFromGroup(advance.getGroups().get(1));
            if (group1 > group2) {
                current -= group1;
            } else {
                current -= group2;
            }
        }
        if (current < 0) {
            current = 0;
        }
        return current;
    }

    private int getReductionFromGroup(CardColor cardColor) {
        int bonus = 0;
        switch (cardColor) {
            case BLUE:
                bonus = bonusBlue;
                break;
            case YELLOW:
                bonus = bonusYellow;
                break;
            case ORANGE:
                bonus = bonusOrange;
                break;
            case GREEN:
                bonus = bonusGreen;
                break;
            case RED:
                bonus = bonusRed;
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
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key){
            case "sort" :
                sortingOrder = sharedPreferences.getString("sort", "name");
            case "name" :
            default:
                break;
        }
        Log.v("PREF", "onSharedPrefChanged: "+sortingOrder);
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Log.v("save", "saving states");
        super.onSaveInstanceState(savedInstanceState);
        tracker.onSaveInstanceState(savedInstanceState);
        // save stuff
//        savedInstanceState.putSerializable(ADVANCES_LIST, (Serializable) advances);
        int money = parseInt(mTreasureInput.getText().toString());
        savedInstanceState.putInt(TREASURE_BOX, money);
//        int moneyleft = Integer.parseInt(mBuyPrice.getText().toString());
//        savedInstanceState.putInt(MONEY_LEFT, moneyleft);
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
        saveVars();
        saveBonus();
    }

    public void onStop() {
        super.onStop();
        Log.v("DEMO","---> onStop() <--- ");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("DEMO","---> onDestroy() <--- ");
    }

    public void saveVars() {
        SharedPreferences.Editor editor = prefs.edit();
        Log.v("Button", "saving...");
        for (String name: purchasedAdvances
             ) {
            Log.v("Button", "inSave: " + name);
        }
        editor.putInt(TREASURE_BOX, treasure);
        editor.apply();
        SharedPreferences.Editor editorCards = savedCards.edit();
        editorCards.putInt("saved", purchasedAdvances.size());
        editorCards.putStringSet(PURCHASED, purchasedAdvances);
        editorCards.commit();
    }
    @SuppressLint("SetTextI18n")
    public void loadVars() {
        treasure = prefs.getInt(TREASURE_BOX,0);
        mTreasureInput.setText(""+treasure);
    }

    /**
     * loads already bought bonuses from SharePreferences file
     */
    public void loadBonus() {
        bonusRed = savedCards.getInt("bonusRed", 0);
        bonusGreen = savedCards.getInt("bonusGreen", 0);
        bonusBlue = savedCards.getInt("bonusBlue", 0);
        bonusYellow = savedCards.getInt("bonusYellow", 0);
        bonusOrange = savedCards.getInt("bonusOrange", 0);
    }

    /**
     * saves already bought bonuses to SharedPreferences file
     */
    public void saveBonus() {
        SharedPreferences.Editor editor = savedCards.edit();
        editor.putInt("bonusRed", bonusRed);
        editor.putInt("bonusGreen", bonusGreen);
        editor.putInt("bonusBlue", bonusBlue);
        editor.putInt("bonusYellow", bonusYellow);
        editor.putInt("bonusOrange", bonusOrange);
        editor.commit();
    }
}