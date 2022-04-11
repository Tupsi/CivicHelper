package org.tesira.mturba.civichelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.AttributeSet;
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
import android.widget.Toast;

import org.tesira.mturba.civichelper.card.Advance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A fragment representing a list of Items.
 */
public class AdvancesFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String ADVANCES_LIST = "advancesList";
    private static final String MONEY = "money";
    private static final String MONEY_LEFT = "moneyLeft";
    private static final String FILENAME = "advances.xml";
    public List<Advance> advances;
    private MyAdvancesRecyclerViewAdapter adapter;
    private SharedPreferences prefs;
    private String sortingOrder;
    protected RecyclerView mRecyclerView;
    protected EditText mEditText;
    protected TextView mTextView;
    private SelectionTracker<Long> tracker;


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
        mEditText = rootView.findViewById(R.id.money);
        mTextView = rootView.findViewById(R.id.moneyleft);
        advances = new ArrayList<>();

        if (savedInstanceState != null) {
            Log.v("save", "rebuild saved state");
            // Restore saved layout manager type.
            advances = (ArrayList) savedInstanceState.getSerializable(ADVANCES_LIST);
            int money = savedInstanceState.getInt(MONEY);
            mEditText.setText(money);
            money = savedInstanceState.getInt(MONEY_LEFT);
            mTextView.setText(money);
        } else {
            loadVars();
            importAdvances(advances, FILENAME);
        }
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
                .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build();
        adapter.setSelectionTracker(tracker);

        setHasOptionsMenu(true);
        return rootView;
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
        int money = Integer.parseInt(mEditText.getText().toString());
        savedInstanceState.putInt(MONEY, money);
        int moneyleft = Integer.parseInt(mTextView.getText().toString());
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
        if (!TextUtils.isEmpty(mEditText.getText())) {
            int money = Integer.parseInt(mEditText.getText().toString());
            editor.putInt(MONEY, money);
            money = Integer.parseInt(mTextView.getText().toString());
            editor.putInt(MONEY_LEFT, money);
            editor.apply();
        }
    }
    @SuppressLint("SetTextI18n")
    public void loadVars() {
        int money = prefs.getInt(MONEY,0);
        mEditText.setText(""+money);
        mTextView.setText(""+prefs.getInt(MONEY_LEFT,money));
    }

}