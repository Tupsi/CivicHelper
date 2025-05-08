package org.tesira.civic;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.R;
import org.tesira.civic.databinding.FragmentHomeBinding;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;
import java.util.List;

/**
 * Shows the Dashboard where you can check the number of cities,
 * see your current color bonus and the effects the already bought
 * cards have on your game.
 */
public class HomeFragment extends Fragment {

    private static final String BONUS = "purchasedAdvancesBonus";
    private FragmentHomeBinding binding;
    private CivicViewModel mCivicViewModel;
    private CalamityAdapter calamityAdapter;
    private SpecialsAdapter specialsAdapter;
    private SharedPreferences prefsDefault;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        prefsDefault = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
//        binding.startBtn.setOnClickListener(this::onClickButton);
//        binding.resetBtn.setOnClickListener(this::onClickButton);
        setHasOptionsMenu(true);
        View rootView = binding.getRoot();
        RecyclerView mRecyclerView = rootView.findViewById(R.id.listCalamity);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        calamityAdapter = new CalamityAdapter(mCivicViewModel.getCalamityBonus(), this.getContext());
        mRecyclerView.setAdapter(calamityAdapter);
        mRecyclerView = rootView.findViewById(R.id.listAbility);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        List<String> specialsList = mCivicViewModel.getSpecialAbilities();
        specialsList.add(0,"___Special Abilities");
        specialsList.add(               "___Immunities");
        specialsList.addAll(mCivicViewModel.getImmunities());
        specialsAdapter = new SpecialsAdapter(specialsList.toArray(new String[0]));
        mRecyclerView.setAdapter(specialsAdapter);
//        binding.tvVp.setText(getString(R.string.tv_vp, mCivicViewModel.sumVp()));
        String civicAST = prefsDefault.getString("civilization", "not set");
        binding.tvCivilization.setText(getString(R.string.tv_ast,civicAST));
        binding.radio0.setOnClickListener(this::onCitiesClicked);
        binding.radio1.setOnClickListener(this::onCitiesClicked);
        binding.radio2.setOnClickListener(this::onCitiesClicked);
        binding.radio3.setOnClickListener(this::onCitiesClicked);
        binding.radio4.setOnClickListener(this::onCitiesClicked);
        binding.radio5.setOnClickListener(this::onCitiesClicked);
        binding.radio6.setOnClickListener(this::onCitiesClicked);
        binding.radio7.setOnClickListener(this::onCitiesClicked);
        binding.radio8.setOnClickListener(this::onCitiesClicked);
        binding.radio9.setOnClickListener(this::onCitiesClicked);
        restoreCityButton(mCivicViewModel.getCities());
        checkAST();
        binding.tvCivilization.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.tipsFragment));
        binding.tvBoni.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.purchasesFragment));
        registerForContextMenu(rootView.findViewById(R.id.tvTime));
        mCivicViewModel.getVp().observe(getActivity(), integer -> binding.tvVp.setText(getString(R.string.tv_vp, integer)));
        binding.tvTime.setText(mCivicViewModel.TIME_TABLE[mCivicViewModel.getTimeVp()/5]);
        return rootView;
    }

    private void restoreCityButton(int cities) {
        switch(cities){
            case 0:
                binding.radio0.setChecked(true);
                break;
            case 1:
                binding.radio1.setChecked(true);
                break;
            case 2:
                binding.radio2.setChecked(true);
                break;
            case 3:
                binding.radio3.setChecked(true);
                break;
            case 4:
                binding.radio4.setChecked(true);
                break;
            case 5:
                binding.radio5.setChecked(true);
                break;
            case 6:
                binding.radio6.setChecked(true);
                break;
            case 7:
                binding.radio7.setChecked(true);
                break;
            case 8:
                binding.radio8.setChecked(true);
                break;
            case 9:
                binding.radio9.setChecked(true);
                break;
        }
    }

    /**
     * checks if certain requirements are set to advance further on the AST and sets
     * the background on the dashboard of the respective info textview
     */
    public void checkAST() {
        int cities = mCivicViewModel.getCities();
        String ast = prefsDefault.getString("ast","basic");
        int vp = mCivicViewModel.sumVp();
//        Log.v("checkAST", ast);
        List<Card> allPurchases = mCivicViewModel.getPurchasesAsCard();
        int size100 = 0, size200 = 0, numberPurchases;
        numberPurchases = allPurchases.size();
        for (Card card: allPurchases) {
            if (card.getPrice() >= 100) {
                size100++;
            }
            if (card.getPrice() >= 200) {
                size200++;
            }
        }
//        Log.v("checkAST","100: " + size100 + " 200: " + size200);

        // reset everything first and then see where we are
        binding.tvMBA.setBackgroundResource(R.color.ast_red);
        binding.tvMBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onRed));
        binding.tvLBA.setBackgroundResource(R.color.ast_red);
        binding.tvLBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onRed));
        binding.tvEIA.setBackgroundResource(R.color.ast_red);
        binding.tvEIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onRed));
        binding.tvLIA.setBackgroundResource(R.color.ast_red);
        binding.tvLIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onRed));
        if (ast.compareTo("basic") == 0){
            if (numberPurchases >= 3 && cities >= 3) {
                // MBA needs 3 cities & 3 cards
                binding.tvMBA.setBackgroundResource(R.color.ast_green);
                binding.tvMBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 3 && size100 >= 3 && cities >= 3) {
                // LBA needs 3 cities & 3 cards 100+
                binding.tvLBA.setBackgroundResource(R.color.ast_green);
                binding.tvLBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 2 && size200 >= 2 && cities >= 4) {
                // EIA needs 4 cities & 2 cards 200+
                binding.tvEIA.setBackgroundResource(R.color.ast_green);
                binding.tvEIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 3 && size200 >= 3 && cities >= 5) {
                // LEA needs 5 cities & 3 cards 200+
                binding.tvLIA.setBackgroundResource(R.color.ast_green);
                binding.tvLIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
        } else {
            // expert version needs a bit more
            if (vp >= 5 && cities >= 3) {
                // MBA needs 3 cities & 5 VP
                binding.tvMBA.setBackgroundResource(R.color.ast_green);
                binding.tvMBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 12 && cities >= 4) {
                // LBA needs 4 cities & 12 cards
                binding.tvLBA.setBackgroundResource(R.color.ast_green);
                binding.tvLBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (size100 >= 10 && vp >= 38 && cities >= 5) {
                // EIA needs 5 cities & 10 cards 100+ & 38 VP
                binding.tvEIA.setBackgroundResource(R.color.ast_green);
                binding.tvEIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (size100 >= 17 && vp >= 56 && cities >= 6) {
                // LEA needs 6 cities & 17 cards 100+ & 56 VP
                binding.tvLIA.setBackgroundResource(R.color.ast_green);
                binding.tvLIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
        }
//        int currentNightMode = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switch (currentNightMode) {
//            case Configuration.UI_MODE_NIGHT_NO:
//                // Night mode is not active on device
//                break;
//            case Configuration.UI_MODE_NIGHT_YES:
//                // Night mode is active on device
//                break;
//        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

//    public void onClickButton(View v) {
//        switch (v.getId()) {
////            case R.id.startBtn:
////                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_advancesFragment);
////                break;
////            case R.id.resetBtn:
////                resetGame();
////                break;
//        }
//    }

//    public void recalculateBonus() {
//        int specials = mCivicViewModel.recalculateBonus(this.getActivity().getSharedPreferences(BONUS, Context.MODE_PRIVATE ));
//        requireActivity().getSupportFragmentManager().setFragmentResultListener("extraCredits", getViewLifecycleOwner(), (requestKey, result) -> loadBonus());
//        if (specials > 0) {
//            new ExtraCreditsDialogFragment(mCivicViewModel,null,specials).show(getParentFragmentManager(),"ExtraCredits");
//        }
//        mCivicViewModel.saveBonus(this.getActivity().getSharedPreferences(BONUS, Context.MODE_PRIVATE ));
//        loadBonus();
//    }

    public void resetGame() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    ((MainActivity) getActivity()).newGame();
                    loadBonus();
                    calamityAdapter.clearData();
                    specialsAdapter.clearData();
//                    binding.tvVp.setText(getString(R.string.tv_vp,0));
                    binding.tvMBA.setBackgroundResource(R.color.ast_red);
                    binding.tvLBA.setBackgroundResource(R.color.ast_red);
                    binding.tvEIA.setBackgroundResource(R.color.ast_red);
                    binding.tvLIA.setBackgroundResource(R.color.ast_red);
                    binding.radio0.setChecked(true);
                    mCivicViewModel.setCities(0);
                    mCivicViewModel.setTimeVp(0);
                    binding.tvTime.setText(mCivicViewModel.TIME_TABLE[mCivicViewModel.getTimeVp()/5]);
                    prefsDefault.edit().remove("civilization").apply();
                    binding.tvCivilization.setText(R.string.reset_tv_civic);
                    mCivicViewModel.resetDB();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void loadBonus() {
        binding.bonusBlue.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.BLUE,0)));
        binding.bonusBlue.setBackgroundResource(R.color.arts);
        binding.bonusGreen.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.GREEN,0)));
        binding.bonusGreen.setBackgroundResource(R.color.science);
        binding.bonusOrange.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.ORANGE,0)));
        binding.bonusOrange.setBackgroundResource(R.color.crafts);
        binding.bonusRed.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.RED,0)));
        binding.bonusRed.setBackgroundResource(R.color.civic);
        binding.bonusYellow.setText(String.valueOf(mCivicViewModel.getCardBonus().getValue().getOrDefault(CardColor.YELLOW,0)));
        binding.bonusYellow.setBackgroundResource(R.color.religion);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newGame:
                resetGame();
                return true;
// debug menu option
//            case R.id.menu_debugRecalBonus:
//                recalculateBonus();
//                return true;
           default:
                return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
        }
//        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(requireView())) || super.onOptionsItemSelected(item);
    }

    public void onCitiesClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_0:
                if (checked)
                    mCivicViewModel.setCities(0);
                break;
            case R.id.radio_1:
                if (checked)
                    mCivicViewModel.setCities(1);
                break;
            case R.id.radio_2:
                if (checked)
                    mCivicViewModel.setCities(2);
                break;
            case R.id.radio_3:
                if (checked)
                    mCivicViewModel.setCities(3);
                break;
            case R.id.radio_4:
                if (checked)
                    mCivicViewModel.setCities(4);
                break;
            case R.id.radio_5:
                if (checked)
                    mCivicViewModel.setCities(5);
                break;
            case R.id.radio_6:
                if (checked)
                    mCivicViewModel.setCities(6);
                break;
            case R.id.radio_7:
                if (checked)
                    mCivicViewModel.setCities(7);
                break;
            case R.id.radio_8:
                if (checked)
                    mCivicViewModel.setCities(8);
                break;
            case R.id.radio_9:
                if (checked)
                    mCivicViewModel.setCities(9);
                break;
        }
        checkAST();
    }

    public void onPause() {
        super.onPause();
        Log.v("HOME","---> onPause() <--- ");
    }

    public void onStart() {
        super.onStart();
        Log.v("HOME","---> onStart() <--- ");
    }

    public void onResume() {
        super.onResume();
        Log.v("HOME","---> onResume() <--- ");
        loadBonus();
    }

    public void onStop() {
        super.onStop();
        Log.v("HOME","---> onStop() <--- ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCivicViewModel != null) {
            mCivicViewModel.getVp().removeObservers(getActivity());
        }
        binding = null;
        Log.v("HOME","---> onDestroy() <--- ");
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu,v,info);
        int timeTableLength = CivicViewModel.TIME_TABLE.length;
        if (prefsDefault.getString("ast", "basic").compareTo("basic") == 0) {
            // basic A.S.T. is one step shorter then expert A.S.T.
            timeTableLength--;
        }
        for (int i=0; i < timeTableLength; i++) {
            // lets make the id=vp we get in the end right from the start
            menu.add(0,(i)*5,i,CivicViewModel.TIME_TABLE[i]);
        }
        MenuInflater mi = new MenuInflater(getContext());
        mi.inflate(R.menu.time_menu, menu);
    }

    // onOptionsItemSelected() wird ausgeführt, wenn ein Item des Kontextmenus angeklickt wurde.
    // Dadurch wird, je nach ausgewähltem Menupunkt, zu ActivityAlpha oder ActivityBeta umgeschaltet.

    public boolean onContextItemSelected(MenuItem item) {
        Log.v("DEMO", "" + item.getItemId());
        mCivicViewModel.setTimeVp(item.getItemId());
        binding.tvTime.setText(item.getTitle());
        return true;
    }
}