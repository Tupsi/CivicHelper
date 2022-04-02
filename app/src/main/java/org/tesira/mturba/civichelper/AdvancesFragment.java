package org.tesira.mturba.civichelper;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tesira.mturba.civichelper.card.Advance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A fragment representing a list of Items.
 */
public class AdvancesFragment extends Fragment {

    public static final String FILENAME = "advances.xml";
    public List<Advance> advances;
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

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advances_list, container, false);
        advances = new ArrayList<>();
        importAdvances(advances, FILENAME);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
//            recyclerView.setAdapter(new MyAdvancesRecyclerViewAdapterBackup(PlaceholderContent.ITEMS));
            recyclerView.setAdapter(new MyAdvancesRecyclerViewAdapter(advances, context));

        }
        return view;
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

}