package org.tesira.civic;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tesira.civic.databinding.FragmentHomeBinding;
import org.tesira.civic.db.Card;
import org.tesira.civic.db.CardColor;
import org.tesira.civic.db.CivicViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
                    initialPaddingTop,
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
        mHomeCalamityAdapter = new HomeCalamityAdapter(this.getContext());
        mRecyclerView.setAdapter(mHomeCalamityAdapter);

        // RecyclerView Special Abilities
        mRecyclerView = rootView.findViewById(R.id.listAbility);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mHomeSpecialsAdapter = new HomeSpecialsAdapter();
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
        binding.tvCivilization.setText(getString(R.string.tv_ast,mCivicViewModel.getCivNumber.getValue()));
        registerForContextMenu(binding.tvCivilization);

        mCivicViewModel.getTotalVp().observe(getViewLifecycleOwner(), newTotalVp -> binding.tvVp.setText(getString(R.string.tv_vp, Objects.requireNonNullElse(newTotalVp, 0))));
        registerForContextMenu(binding.tvTime);
        registerForContextMenu(binding.tvAST);
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

/*        mCivicViewModel.getNewGameStartedEvent().observe(getViewLifecycleOwner(), newGameEvent -> {
            Boolean resetTriggered = newGameEvent.getContentIfNotHandled();
            if (resetTriggered != null && resetTriggered) {
                Log.d("HomeFragment", "Resetting HomeFragment UI elements.");
                // Update UI elements that need resetting in HomeFragment
                mHomeSpecialsAdapter.submitSpecialsList(currentSpecialsAndImmunities); // Update special abilities/immunities data in the adapter
                mHomeCalamityAdapter.submitCalamityList(currentCalamities); // Update calamity data in the adapter
                binding.tvCivilization.setText(getString(R.string.tv_ast,"not set"));
            }
        });*/
        mCivicViewModel.calamityBonusListLiveData.observe(getViewLifecycleOwner(), calamities -> {
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

        mCivicViewModel.cardBonus.observe(getViewLifecycleOwner(), cardBonusMap -> {
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

        mCivicViewModel.getInventoryAsCardLiveData().observe(getViewLifecycleOwner(), purchases -> {
            if (purchases != null) {
                currentAllPurchases = purchases;
                // Wenn alle anderen benötigten Daten auch schon da sind, checkAST ausführen
                if (mCivicViewModel.getCitiesLive().getValue() != null && currentCardsVp != 0 /* oder eine andere Logik */ ) {
                    checkASTInternal();
                }
            }
        });

        // Observer für Time
        mCivicViewModel.getTimeVpLive().observe(getViewLifecycleOwner(), value -> binding.tvTime.setText(CivicViewModel.TIME_TABLE[value / 5]));
    }
    private void setAstStatus(TextView textView, boolean achieved) {
        if (achieved) {
            textView.setBackgroundResource(R.color.ast_green);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ast_onGreen));
        } else {
            textView.setBackgroundResource(R.color.ast_red);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ast_onRed));
        }
    }

    /**
     * checks if certain requirements are set to advance further on the AST and sets
     * the background on the dashboard of the respective info textview
     */
    private void checkASTInternal() {
        String ast = mCivicViewModel.astVersion.getValue();
        String astMarkerText;
        int countAllPurchases = currentAllPurchases.size();
        int countSize100 = 0;
        int countSize200 = 0;
        boolean ebaAchieved, mbaAchieved, lbaAchieved, eiaAchieved, liaAchieved;

        // count the number of cards which have a buying price greater 100 and 200
        for (Card card : currentAllPurchases) {
            if (card.getPrice() >= 100) {
                countSize100++;
            }
            if (card.getPrice() >= 200) {
                countSize200++;
            }
        }

        if ("basic".equals(ast)) {
            astMarkerText = "AST (B)";
            // EBA: 2 cities
            ebaAchieved = (currentCities >= 2 );
            // MBA: 3 cities & 3 cards
            mbaAchieved = (currentCities >= 3 && countAllPurchases >= 3);
            // LBA: 3 cities & 3 cards 100+
            lbaAchieved = (currentCities >= 3 && countSize100 >= 3);
            // EIA: 4 cities & 2 cards 200+
            eiaAchieved = (currentCities >= 4 && countSize200 >= 2);
            // LIA: 5 cities & 3 cards 200+
            liaAchieved = (currentCities >= 5 && countSize200 >= 3);
        } else { // "expert" AST
            astMarkerText = "AST (E)";
            // EBA: 2 cities
            ebaAchieved = (currentCities >= 3 );
            // MBA: 3 cities & 5 VP
            mbaAchieved = (currentCities >= 3 && currentCardsVp >= 5);
            // LBA: 4 cities & 12 cards
            lbaAchieved = (currentCities >= 4 && countAllPurchases >= 12);
            // EIA: 5 cities & 10 cards 100+ & 38 VP
            eiaAchieved = (currentCities >= 5 && countSize100 >= 10 && currentCardsVp >= 38);
            // LIA: 6 cities & 17 cards 100+ & 56 VP
            liaAchieved = (currentCities >= 6 && countSize100 >= 17 && currentCardsVp >= 56);
        }
        binding.tvAST.setText(astMarkerText);

        if (isAdded() && getContext() != null) { // Prüfen, ob das Fragment attached ist und einen Context hat
            setAstStatus(binding.tvEBA, ebaAchieved);
            setAstStatus(binding.tvMBA, mbaAchieved);
            setAstStatus(binding.tvLBA, lbaAchieved);
            setAstStatus(binding.tvEIA, eiaAchieved);
            setAstStatus(binding.tvLIA, liaAchieved);
        } else {
            Log.w("HomeFragment", "checkASTInternal called when fragment not attached or context is null.");
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

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.tvTime) {
            int timeTableLength = CivicViewModel.TIME_TABLE.length;
            if ("basic".equals(mCivicViewModel.astVersion.getValue())) {
                timeTableLength--;
            }
            for (int i = 0; i < timeTableLength; i++) {
                menu.add(0, i * 5, i, CivicViewModel.TIME_TABLE[i]);
            }
        } else if (v.getId() == R.id.tvCivilization) {
            Context context = requireContext();
            String[] entries = context.getResources().getStringArray(R.array.civilizations_entries);
            String[] values = context.getResources().getStringArray(R.array.civilizations_values);
            for (int i = 0; i < values.length; i++) {
                menu.add(1, i, i, entries[i]);  // Group 1 = Civilization
            }
        } else if (v.getId() == R.id.tvAST) {
            menu.add(2, 0, 0, getResources().getStringArray(R.array.ast_entries)[0]);
            menu.add(2, 1, 1, getResources().getStringArray(R.array.ast_entries)[1]);
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
                mCivicViewModel.setCivNumber(selectedValue);
                binding.tvCivilization.setText(getString(R.string.tv_ast, selectedValue));
                return true;
            }
        } else if (item.getGroupId() == 2) {
            // AST Menu
            String[] entries = getResources().getStringArray(R.array.ast_values);
            mCivicViewModel.setAstVersion(entries[item.getItemId()]);
            checkASTInternal();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}