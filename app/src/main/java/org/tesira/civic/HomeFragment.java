package org.tesira.civic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.databinding.FragmentHomeBinding;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shows the Dashboard where you can check the number of cities,
 * see your current color bonus and the effects the already bought
 * cards have on your game.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CivicViewModel mCivicViewModel;
    private HomeCalamityAdapter mHomeCalamityAdapter;
    private HomeSpecialsAdapter mHomeSpecialsAdapter;
    private SharedPreferences prefsDefault;
    private final List<Integer> cityIds = Arrays.asList(
            R.id.radio_0, R.id.radio_1, R.id.radio_2, R.id.radio_3, R.id.radio_4,
            R.id.radio_5, R.id.radio_6, R.id.radio_7, R.id.radio_8, R.id.radio_9
    );
    private int currentCities = 0;
    private int currentCardsVp = 0;
    private List<Card> currentAllPurchases = new ArrayList<>();
    private List<Calamity> currentCalamities = new ArrayList<>();
    private List<String> currentSpecialsAndImmunities = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container,false);
        prefsDefault = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mCivicViewModel = new ViewModelProvider(requireActivity()).get(CivicViewModel.class);
        View rootView = binding.getRoot();

        // Speichere die ursprünglichen Padding-Werte der View, auf die die Insets angewendet werden
        final int initialPaddingLeft = rootView.getPaddingLeft();
        final int initialPaddingTop = rootView.getPaddingTop();
        final int initialPaddingRight = rootView.getPaddingRight();
        final int initialPaddingBottom = rootView.getPaddingBottom();


        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Insets für die Tastatur, falls du auch darauf reagieren möchtest (hier nicht primär im Fokus)
            // Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            // Wende die Systemleisten-Insets zusätzlich zum ursprünglichen Padding an
            v.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop + systemBarInsets.top,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
            );

            // Es ist wichtig, die WindowInsets (ggf. modifiziert) zurückzugeben,
            // damit Kind-Views sie auch konsumieren können.
            // Wenn du hier nichts an den windowInsets selbst änderst, gib sie einfach weiter.
            return windowInsets;
        });

        // RecylerView Calamity Effects
        RecyclerView mRecyclerView = rootView.findViewById(R.id.listCalamity);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mHomeCalamityAdapter = new HomeCalamityAdapter(mCivicViewModel, this.getContext());
        mRecyclerView.setAdapter(mHomeCalamityAdapter);

        // RecyclerView Special Abilities
        mRecyclerView = rootView.findViewById(R.id.listAbility);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mHomeSpecialsAdapter = new HomeSpecialsAdapter(mCivicViewModel);
        mRecyclerView.setAdapter(mHomeSpecialsAdapter);

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
        String civicAST = prefsDefault.getString("civilization", "not set");
        binding.tvCivilization.setText(getString(R.string.tv_ast,civicAST));
        binding.tvCivilization.setOnClickListener(v -> {
            String civ = prefsDefault.getString("civilization", "not set");
            if (!civ.equals("not set")) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_homeFragment_to_tipsFragment);
            } else {
                Toast.makeText(getActivity(), "You need to set a civilization first.\nLong-Press the text again.", Toast.LENGTH_SHORT).show();
            }
        });
        registerForContextMenu(binding.tvCivilization);

        // shortcuts to purchase
        binding.tvBoni.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));
        binding.bonusBlue.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));
        binding.bonusGreen.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));
        binding.bonusOrange.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));
        binding.bonusRed.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));
        binding.bonusYellow.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_purchasesFragment));

        // shortcut to advances/buy fragment
        binding.tvSpecials.setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_buyingFragment));

        mCivicViewModel.getTotalVp().observe(getViewLifecycleOwner(), newTotalVp -> {
            if (newTotalVp != null) { // Null-Check, falls LiveData initial noch keinen Wert hat
                binding.tvVp.setText(getString(R.string.tv_vp, newTotalVp));
            } else {
                binding.tvVp.setText(getString(R.string.tv_vp, 0));
            }
        });
        binding.tvTime.setText(CivicViewModel.TIME_TABLE[mCivicViewModel.getTimeVp()/5]);
        registerForContextMenu(binding.tvTime);

        return rootView;
    }

    /**
     * sets the city button to the current number of cities
     */
    private void restoreCityButton(int cities) {
        if (cities >= 0 && cities < cityIds.size()) {
            RadioButton button = binding.getRoot().findViewById(cityIds.get(cities));
            if (button != null) {
                button.setChecked(true);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCivicViewModel.getNewGameResetCompletedEvent().observe(getViewLifecycleOwner(), newGameEvent -> {
            Boolean resetTriggered = newGameEvent.getContentIfNotHandled();
            if (resetTriggered != null && resetTriggered) {
                // Update UI elements that need resetting in HomeFragment
                mHomeSpecialsAdapter.submitSpecialsList(currentSpecialsAndImmunities); // Update special abilities/immunities data in the adapter
                mHomeCalamityAdapter.submitCalamityList(currentCalamities); // Update calamity data in the adapter
                String civicAST = prefsDefault.getString("civilization", "not set");
                binding.tvCivilization.setText(getString(R.string.tv_ast,civicAST));
            }
        });
        mCivicViewModel.getCalamityBonusListLiveData().observe(getViewLifecycleOwner(), calamities -> {
            // 'calamities' ist hier die List<Calamity> aus der Datenbank (über LiveData)
            if (calamities != null) {
                currentCalamities = calamities;
                mHomeCalamityAdapter.submitCalamityList(calamities);
            }
        });

        mCivicViewModel.getCombinedSpecialsAndImmunitiesLiveData().observe(getViewLifecycleOwner(), combinedList -> {
            if (combinedList != null) {
                currentSpecialsAndImmunities = combinedList;
                mHomeSpecialsAdapter.submitSpecialsList(combinedList);
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
        // Observer für Cities
        mCivicViewModel.getCitiesLive().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer cities) {
                if (cities != null) {
                    currentCities = cities;
                    restoreCityButton(currentCities);
                    checkASTInternal();
                }
            }
        });
        mCivicViewModel.getCardsVpLiveData().observe(getViewLifecycleOwner(), vp -> {
            if (vp != null) {
                currentCardsVp = vp;
                // Wenn alle anderen benötigten Daten auch schon da sind, checkAST ausführen
                if (mCivicViewModel.getCitiesLive().getValue() != null && !currentAllPurchases.isEmpty()) {
                    checkASTInternal();
                }
            }
        });

        mCivicViewModel.getPurchasesAsCardLiveData().observe(getViewLifecycleOwner(), purchases -> {
            if (purchases != null) {
                currentAllPurchases = purchases;
                // Wenn alle anderen benötigten Daten auch schon da sind, checkAST ausführen
                if (mCivicViewModel.getCitiesLive().getValue() != null && currentCardsVp != 0 /* oder eine andere Logik */ ) {
                    checkASTInternal();
                }
            }
        });

        // Observer für Time
        mCivicViewModel.getTimeVpLive().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                binding.tvTime.setText(CivicViewModel.TIME_TABLE[value/5]);
            }
        });
    }

    /**
     * checks if certain requirements are set to advance further on the AST and sets
     * the background on the dashboard of the respective info textview
     */
    private void checkASTInternal() {
        String ast = prefsDefault.getString("ast", "basic");
        int numberPurchases = currentAllPurchases.size();
        int size100 = 0;
        int size200 = 0;
        for (Card card : currentAllPurchases) {
            if (card.getPrice() >= 100) {
                size100++;
            }
            if (card.getPrice() >= 200) {
                size200++;
            }
        }

        // Reset der UI-Elemente
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
            if (numberPurchases >= 3 && currentCities >= 3) {
                // MBA needs 3 cities & 3 cards
                binding.tvMBA.setBackgroundResource(R.color.ast_green);
                binding.tvMBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 3 && size100 >= 3 && currentCities >= 3) {
                // LBA needs 3 cities & 3 cards 100+
                binding.tvLBA.setBackgroundResource(R.color.ast_green);
                binding.tvLBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 2 && size200 >= 2 && currentCities >= 4) {
                // EIA needs 4 cities & 2 cards 200+
                binding.tvEIA.setBackgroundResource(R.color.ast_green);
                binding.tvEIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 3 && size200 >= 3 && currentCities >= 5) {
                // LEA needs 5 cities & 3 cards 200+
                binding.tvLIA.setBackgroundResource(R.color.ast_green);
                binding.tvLIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
        } else {
            // expert version needs a bit more
            if (currentCardsVp >= 5 && currentCities >= 3) {
                // MBA needs 3 cities & 5 VP
                binding.tvMBA.setBackgroundResource(R.color.ast_green);
                binding.tvMBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (numberPurchases >= 12 && currentCities >= 4) {
                // LBA needs 4 cities & 12 cards
                binding.tvLBA.setBackgroundResource(R.color.ast_green);
                binding.tvLBA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (size100 >= 10 && currentCardsVp >= 38 && currentCities >= 5) {
                // EIA needs 5 cities & 10 cards 100+ & 38 VP
                binding.tvEIA.setBackgroundResource(R.color.ast_green);
                binding.tvEIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
            if (size100 >= 17 && currentCardsVp >= 56 && currentCities >= 6) {
                // LEA needs 6 cities & 17 cards 100+ & 56 VP
                binding.tvLIA.setBackgroundResource(R.color.ast_green);
                binding.tvLIA.setTextColor(ContextCompat.getColor(getContext(), R.color.ast_onGreen));
            }
        }
    }

    public void onCitiesClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (!checked) return;

        int index = cityIds.indexOf(view.getId());
        if (index != -1) {
            mCivicViewModel.setCities(index);
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.tvTime) {
            int timeTableLength = CivicViewModel.TIME_TABLE.length;
            if ("basic".equals(prefsDefault.getString("ast", "basic"))) {
                timeTableLength--;
            }
            for (int i = 0; i < timeTableLength; i++) {
                menu.add(0, i * 5, i, CivicViewModel.TIME_TABLE[i]);
            }
        } else if (v.getId() == R.id.tvCivilization) {
            Context context = requireContext();
            String[] entries = context.getResources().getStringArray(R.array.civilizations_entries);
            String[] values = context.getResources().getStringArray(R.array.civilizations_values);
            String currentValue = prefsDefault.getString("civilization", "");
            for (int i = 0; i < values.length; i++) {
                menu.add(1, i, i, entries[i]);  // Group 1 = Civilization
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 0) {
            // Time Menu
            mCivicViewModel.setTimeVp(item.getItemId());
            binding.tvTime.setText(item.getTitle());
            return true;
        } else if (item.getGroupId() == 1) {
            // Civilization Menu
            String[] values = getResources().getStringArray(R.array.civilizations_values);
            if (item.getItemId() < values.length) {
                String selectedValue = values[item.getItemId()];
                prefsDefault.edit().putString("civilization", selectedValue).apply();
                binding.tvCivilization.setText(getString(R.string.tv_ast, selectedValue));
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
}