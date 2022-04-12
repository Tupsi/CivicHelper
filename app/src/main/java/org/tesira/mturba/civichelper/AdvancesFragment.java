package org.tesira.mturba.civichelper;

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
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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
    private SelectionTracker tracker;
    private int total;
    private int treasure;


    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdvancesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdvancesFragment newInstance(int columnCount) {
        AdvancesFragment fragment = new AdvancesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

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
        View rootView = inflater.inflate(R.layout.fragment_advances_list, container, false);
        mTreasureInput = rootView.findViewById(R.id.money);
        mBuyPrice = rootView.findViewById(R.id.moneyleft);
        advances = new ArrayList<>();

        if (savedInstanceState != null) {
            Log.v("save", "rebuild saved state");
            // Restore saved layout manager type.
            advances = (ArrayList) savedInstanceState.getSerializable(ADVANCES_LIST);
            int money = savedInstanceState.getInt(TREASURE_BOX);
            mTreasureInput.setText(money);
//            money = savedInstanceState.getInt(MONEY_LEFT);
//            mBuyPrice.setText(money);
        } else {
            loadVars();
            importAdvances(advances, FILENAME);
        }
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
        mRecyclerView.setAdapter(adapter);

        tracker = new SelectionTracker.Builder<Long>(
                "my-selection-id",
                mRecyclerView,
                new StableIdKeyProvider(mRecyclerView),
                new MyItemDetailsLookup(mRecyclerView),
                StorageStrategy.createLongStorage())
               .withSelectionPredicate(new MySelectionPredicate<>(this, advances)).build();
        adapter.setSelectionTracker(tracker);

        tracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onItemStateChanged(@NonNull Long key, boolean selected) {
                super.onItemStateChanged(key, selected);
                Log.v("INFO", "onItemStateChanged : " + key + " : " + selected);
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
        setHasOptionsMenu(true);
        return rootView;
    }

    public int getTreasure() {
        treasure = Integer.parseInt(mTreasureInput.getText().toString());
        return treasure;
    }

    public void setTotal(int total) {
        this.total = total;
        mBuyPrice.setText(""+total);
    }

    public int calculateTotal() {
        total = 0;
        Iterator<Long> it = tracker.getSelection().iterator();
        while (it.hasNext()) {
            Long id = it.next();
            int idx = Math.toIntExact(id);
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
                    advance.setFamily(Integer.parseInt(readElement));
//                    advance.setFamily(Integer.parseInt(element2.getElementsByTagName("family").item(0).getTextContent()));
//                    Log.v("TEST", "VP: " + element2.getElementsByTagName("vp").item(0).getTextContent());
                    readElement = element2.getElementsByTagName("vp").item(0).getTextContent();
                    advance.setVp(Integer.parseInt(readElement));
//                    advance.setVp(Integer.parseInt(element2.getElementsByTagName("vp").item(0).getTextContent()));
                    readElement = element2.getElementsByTagName("price").item(0).getTextContent();
                    advance.setPrice(Integer.parseInt(readElement));
//                    advance.setPrice(Integer.parseInt(element2.getElementsByTagName("price").item(0).getTextContent()));
                    for (int x=0; x<element2.getElementsByTagName("group").getLength();x++) {
//                        Log.v("TEST", "Group: " + element2.getElementsByTagName("group").item(x).getTextContent());
                        advance.addGroup(element2.getElementsByTagName("group").item(x).getTextContent());
                    }
                    for (int x=0; x<element2.getElementsByTagName("credit").getLength(); x++) {
                        String color = element2.getElementsByTagName("credit").item(x).getAttributes().item(0).getTextContent();
                        int discount = Integer.parseInt(element2.getElementsByTagName("credit").item(x).getTextContent());
//                        Log.v("Credit","" + color + " : " + discount);
                        advance.addCredits(color, discount);
                    }
//                    Log.v("Effect #", ""+element2.getElementsByTagName("effect").getLength());
                    for (int x=0; x<element2.getElementsByTagName("effect").getLength(); x++) {
                        String name = element2.getElementsByTagName("effect").item(x).getAttributes().item(0).getTextContent();
                        int value = Integer.parseInt(element2.getElementsByTagName("effect").item(x).getTextContent());
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.v("save", "saving states");
        // save stuff
        savedInstanceState.putSerializable(ADVANCES_LIST, (Serializable) advances);
        int money = Integer.parseInt(mTreasureInput.getText().toString());
        savedInstanceState.putInt(TREASURE_BOX, money);
        int moneyleft = Integer.parseInt(mBuyPrice.getText().toString());
        savedInstanceState.putInt(MONEY_LEFT, moneyleft);
        super.onSaveInstanceState(savedInstanceState);
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
            int money = Integer.parseInt(mTreasureInput.getText().toString());
            editor.putInt(TREASURE_BOX, money);
//            int money = Integer.parseInt(mBuyPrice.getText().toString());
//            editor.putInt(MONEY_LEFT, money);
            editor.apply();
        }
    }
    @SuppressLint("SetTextI18n")
    public void loadVars() {
        int money = prefs.getInt(TREASURE_BOX,0);
        mTreasureInput.setText(""+money);
//        mBuyPrice.setText(""+prefs.getInt(MONEY_LEFT,money));
    }

}