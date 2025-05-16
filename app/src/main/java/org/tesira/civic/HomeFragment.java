package org.tesira.civic;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
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
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.CalamityAdapter;
import org.tesira.civic.MainActivity;
import org.tesira.civic.R;
import org.tesira.civic.SpecialsAdapter;
import org.tesira.civic.databinding.FragmentHomeBinding;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;

import java.util.Arrays;
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
    private final List<Integer> cityIds = Arrays.asList(
            R.id.radio_0, R.id.radio_1, R.id.radio_2, R.id.radio_3, R.id.radio_4,
            R.id.radio_5, R.id.radio_6, R.id.radio_7, R.id.radio_8, R.id.radio_9
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        prefsDefault = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
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
        mCivicViewModel.getVp().observe(getViewLifecycleOwner(), integer -> binding.tvVp.setText(getString(R.string.tv_vp, integer)));

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
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Inflate the menu here
                menuInflater.inflate(R.menu.options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu item selection here
                switch (menuItem.getItemId()) {
                    case R.id.menu_newGame:

                        // Show the confirmation dialog before starting a new game
                        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    // Yes button clicked, trigger the reset process in the ViewModel
                                    mCivicViewModel.requestNewGame();
                                    // The UI update will be handled by the resetEvent observer
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    // No button clicked
                                    break;
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext()
                        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        return true;
                    default:
                        return NavigationUI.onNavDestinationSelected(menuItem, Navigation.findNavController(requireView()));
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        mCivicViewModel.getNewGameResetCompletedEvent().observe(getViewLifecycleOwner(), newGameEvent -> {
            Log.d("HomeFragment", "New Game Reset Completed Event received in observer");
            Boolean resetTriggered = newGameEvent.getContentIfNotHandled();
            if (resetTriggered != null && resetTriggered) {
                // Update UI elements that need resetting in HomeFragment
                calamityAdapter.clearData(); // Clear calamity data in the adapter
                specialsAdapter.clearData(); // Clear special abilities/immunities data in the adapter

                // Reset the AST indicators to red (or their default state)
                binding.tvMBA.setBackgroundResource(R.color.ast_red);
                binding.tvLBA.setBackgroundResource(R.color.ast_red);
                binding.tvEIA.setBackgroundResource(R.color.ast_red);
                binding.tvLIA.setBackgroundResource(R.color.ast_red);

                // Set the cities radio button to 0
                binding.radio0.setChecked(true);
                // The ViewModel's LiveData for civilization preference will update tvCivilization if it's observed.
                // If tvCivilization is not updated by an observer, you might need to update it directly here:
                binding.tvCivilization.setText(R.string.reset_tv_civic);
                binding.tvTime.setText(CivicViewModel.TIME_TABLE[mCivicViewModel.getTimeVp()/5]);
            }
        });

        mCivicViewModel.getCardBonus().observe(getViewLifecycleOwner(), cardBonusMap -> {
            if (cardBonusMap == null) return;
            binding.bonusBlue.setText(String.valueOf(cardBonusMap.getOrDefault(CardColor.BLUE, 0)));
            binding.bonusBlue.setBackgroundResource(R.color.arts);
            binding.bonusGreen.setText(String.valueOf(cardBonusMap.getOrDefault(CardColor.GREEN, 0)));
            binding.bonusGreen.setBackgroundResource(R.color.science);
            binding.bonusOrange.setText(String.valueOf(cardBonusMap.getOrDefault(CardColor.ORANGE, 0)));
            binding.bonusOrange.setBackgroundResource(R.color.crafts);
            binding.bonusRed.setText(String.valueOf(cardBonusMap.getOrDefault(CardColor.RED, 0)));
            binding.bonusRed.setBackgroundResource(R.color.civic);
            binding.bonusYellow.setText(String.valueOf(cardBonusMap.getOrDefault(CardColor.YELLOW, 0)));
            binding.bonusYellow.setBackgroundResource(R.color.religion);
        });

    }

    public void onCitiesClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (!checked) return;

        int index = cityIds.indexOf(view.getId());
        if (index != -1) {
            mCivicViewModel.setCities(index);
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
//        loadBonus();
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

    public boolean onContextItemSelected(MenuItem item) {
        Log.v("DEMO", "" + item.getItemId());
        mCivicViewModel.setTimeVp(item.getItemId());
        binding.tvTime.setText(item.getTitle());
        return true;
    }
}