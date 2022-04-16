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
import android.widget.EditText;
import android.widget.TextView;

import org.tesira.mturba.civichelper.card.Advance;
import org.tesira.mturba.civichelper.databinding.FragmentAdvancesBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    // arraylist of all civilization cards
    public List<Advance> advances;
    private MyAdvancesRecyclerViewAdapter adapter;
    private SharedPreferences prefs;
    private String sortingOrder;
    protected RecyclerView mRecyclerView;
    protected EditText mTreasureInput;
    protected TextView mBuyPrice;
    private SelectionTracker<String> tracker;
    private int total;
    private int treasure;
    private FragmentAdvancesBinding binding;


    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

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
        Log.v("PREF", "onCreate: "+sortingOrder);
        prefs.registerOnSharedPreferenceChangeListener(this);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_advances, container, false);
        mTreasureInput = rootView.findViewById(R.id.treasure);
        mBuyPrice = rootView.findViewById(R.id.moneyleft);
        advances = new ArrayList<>();

        if (savedInstanceState != null) {
            Log.v("save", "savedInstanceState YES/if");
            // Restore saved layout manager type.
//            advances = (ArrayList) savedInstanceState.getSerializable(ADVANCES_LIST);
//            treasure = savedInstanceState.getInt(TREASURE_BOX);
//            mTreasureInput.setText(treasure);
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
//                new StableIdKeyProvider(mRecyclerView),
                new MyItemDetailsLookup(mRecyclerView),
                StorageStrategy.createStringStorage())
//                StorageStrategy.createLongStorage())
               .withSelectionPredicate(new MySelectionPredicate<>(this, advances)).build();
        adapter.setSelectionTracker(tracker);
        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                Log.v("TRACKER", "onItemStateChanged : " + key + " : " + selected);
                // item got deselected, need to redo total selected
                if (!selected) {
                    setTotal(calculateTotal());
                }
                int rest = treasure - total;
                adapter.setRemainingTreasure(rest);
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                Log.v("TRACKER", "onSelectionChanged fired");
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

        mTreasureInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v("Watcher", "on");
                updateRemaining();
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.v("Watcher", "after");
            }
        });
        mTreasureInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.v("Focus", "" + hasFocus);
                if (!hasFocus) {
                    if (treasure < total) {
                        tracker.clearSelection();
                    }
                }
            }
        });
        return rootView;
    }

    public int getTreasure() {
        String treasureInput = mTreasureInput.getText().toString();
        if (treasureInput.isEmpty()) {
            treasure = 0;
        } else {
            treasure = Integer.parseInt(treasureInput);
        }

        Log.v("treasure", "getTreasure :" + treasure);
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

    public int calculateTotal() {
        total = 0;
        for (String id : tracker.getSelection()) {
            int idx = advances.get(0).getIndexFromName(advances, id);
            int currentPrice = advances.get(idx).getPrice();
            total += currentPrice;
        }
        return total;
    }

    private void importAdvances(List<Advance> advances, String filename) {
        try {
            InputStream is = requireActivity().getAssets().open(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element = doc.getDocumentElement();
            element.normalize();
//            String civic = (String)civilizationSpinner.getSelectedItem();
//            Log.v("Civic", civic);
            NodeList nList = doc.getElementsByTagName("advance");
            for (int i=0; i<nList.getLength(); i++) {
//                Log.v("Card", "-----------------------------------" + (i+1));
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Advance advance = new Advance();
                    String readElement;
                    Element element2 = (Element) node;
//                    Log.v("TEST", "Name: " + element2.getElementsByTagName("name").item(0).getTextContent());
                    readElement = element2.getElementsByTagName("name").item(0).getTextContent();
                    advance.setName(readElement);
//                    advance.setName(element2.getElementsByTagName("name").item(0).getTextContent());
//                    Log.v("TEST", "Family: " + element2.getElementsByTagName("family").item(0).getTextContent());
                    readElement = element2.getElementsByTagName("family").item(0).getTextContent();
                    advance.setFamily(parseInt(readElement));
//                    advance.setFamily(Integer.parseInt(element2.getElementsByTagName("family").item(0).getTextContent()));
//                    Log.v("TEST", "VP: " + element2.getElementsByTagName("vp").item(0).getTextContent());
                    readElement = element2.getElementsByTagName("vp").item(0).getTextContent();
                    advance.setVp(parseInt(readElement));
//                    advance.setVp(Integer.parseInt(element2.getElementsByTagName("vp").item(0).getTextContent()));
                    readElement = element2.getElementsByTagName("price").item(0).getTextContent();
                    advance.setPrice(parseInt(readElement));
//                    advance.setPrice(Integer.parseInt(element2.getElementsByTagName("price").item(0).getTextContent()));
                    for (int x=0; x<element2.getElementsByTagName("group").getLength();x++) {
//                        Log.v("TEST", "Group: " + element2.getElementsByTagName("group").item(x).getTextContent());
                        advance.addGroup(element2.getElementsByTagName("group").item(x).getTextContent());
                    }
                    for (int x=0; x<element2.getElementsByTagName("credit").getLength(); x++) {
                        String color = element2.getElementsByTagName("credit").item(x).getAttributes().item(0).getTextContent();
                        int discount = parseInt(element2.getElementsByTagName("credit").item(x).getTextContent());
//                        Log.v("Credit","" + color + " : " + discount);
                        advance.addCredits(color, discount);
                    }
//                    Log.v("Effect #", ""+element2.getElementsByTagName("effect").getLength());
                    for (int x=0; x<element2.getElementsByTagName("effect").getLength(); x++) {
                        String name = element2.getElementsByTagName("effect").item(x).getAttributes().item(0).getTextContent();
                        int value = parseInt(element2.getElementsByTagName("effect").item(x).getTextContent());
//                        Log.v("Effect","" + name + " : " + value);
                        advance.addEffect(name, value);
                    }
//                    Log.v("Card", "-----------------------------------" + (i+1));
                    advances.add(advance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        saveVars();
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

    public void saveVars() {
        SharedPreferences.Editor editor = prefs.edit();
        if (!TextUtils.isEmpty(mTreasureInput.getText())) {
            int money = parseInt(mTreasureInput.getText().toString());
            editor.putInt(TREASURE_BOX, money);
            editor.apply();
        }
    }
    @SuppressLint("SetTextI18n")
    public void loadVars() {
        treasure = prefs.getInt(TREASURE_BOX,0);
        mTreasureInput.setText(""+treasure);
    }

}