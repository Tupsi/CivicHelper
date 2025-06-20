package org.tesira.civic

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tesira.civic.databinding.FragmentHomeBinding
import org.tesira.civic.db.Card
import org.tesira.civic.db.CardColor
import org.tesira.civic.db.CivicViewModel

/**
 * Shows the Dashboard where you can check the number of cities,
 * see your current color bonus and the effects the already bought
 * cards have on your game.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var mHomeCalamityAdapter: HomeCalamityAdapter
    private lateinit var mHomeSpecialsAdapter: HomeSpecialsAdapter
    private val cityIds: List<Int> = listOf(
        R.id.radio_0, R.id.radio_1, R.id.radio_2, R.id.radio_3, R.id.radio_4,
        R.id.radio_5, R.id.radio_6, R.id.radio_7, R.id.radio_8, R.id.radio_9
    )
    private var currentCities = 0
    private var currentCardsVp = 0
    private var currentAllPurchases: List<Card> = ArrayList<Card>()
    private var currentCalamities: List<Calamity> = ArrayList<Calamity>()
    private var currentSpecialsAndImmunities: List<String> = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView: View = binding.getRoot()
//        binding.root.applyHorizontalSystemBarInsetsAsPadding()

        // RecylerView Calamity Effects
        var mRecyclerView = rootView.findViewById<RecyclerView>(R.id.listCalamity)
        mRecyclerView.setLayoutManager(LinearLayoutManager(rootView.context))
        mHomeCalamityAdapter = HomeCalamityAdapter(this.requireContext())
        mRecyclerView.setAdapter(mHomeCalamityAdapter)

        // RecyclerView Special Abilities
        mRecyclerView = rootView.findViewById<RecyclerView>(R.id.listAbility)
        mRecyclerView.setLayoutManager(LinearLayoutManager(rootView.context))
        mHomeSpecialsAdapter = HomeSpecialsAdapter()
        mRecyclerView.setAdapter(mHomeSpecialsAdapter)
        binding.radio0.setOnClickListener { onCitiesClicked(it) }
        binding.radio1.setOnClickListener { onCitiesClicked(it) }
        binding.radio2.setOnClickListener { onCitiesClicked(it) }
        binding.radio3.setOnClickListener { onCitiesClicked(it) }
        binding.radio4.setOnClickListener { onCitiesClicked(it) }
        binding.radio5.setOnClickListener { onCitiesClicked(it) }
        binding.radio6.setOnClickListener { onCitiesClicked(it) }
        binding.radio7.setOnClickListener { onCitiesClicked(it) }
        binding.radio8.setOnClickListener { onCitiesClicked(it) }
        binding.radio9.setOnClickListener { onCitiesClicked(it) }

//        restoreCityButton(mCivicViewModel.getCities())
        binding.tvCivilization.text = getString(
            R.string.tv_ast,
            civicViewModel.civNumber.getValue()
        )
        registerForContextMenu(binding.tvCivilization)

        civicViewModel.totalVp.observe(getViewLifecycleOwner()) { newTotalVp ->
            binding.tvVp.text = getString(R.string.tv_vp, newTotalVp)
        }
        registerForContextMenu(binding.tvTime)
        registerForContextMenu(binding.tvAST)
        return rootView
    }

    /**
     * sets the city button to the current number of cities
     */
    internal fun restoreCityButton(cities: Int) {
        if (cities >= 0 && cities < cityIds.size) {
            val button = binding.root.findViewById<RadioButton?>(cityIds[cities])
            if (button != null) {
                button.isChecked = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        civicViewModel.calamityBonusListLiveData.observe(
            getViewLifecycleOwner(),
            Observer { calamities: List<Calamity> ->
                currentCalamities = calamities
                mHomeCalamityAdapter.submitCalamityList(calamities)
            })

        civicViewModel.getCombinedSpecialsAndImmunitiesLiveData()
            .observe(getViewLifecycleOwner(), Observer { combinedList: List<String> ->
                currentSpecialsAndImmunities = combinedList
                mHomeSpecialsAdapter.submitSpecialsList(combinedList)
            })

        civicViewModel.cardBonus.observe(
            getViewLifecycleOwner(),
            Observer { cardBonusMap: HashMap<CardColor, Int> ->

                binding.bonusBlue.text = cardBonusMap.getOrDefault(CardColor.BLUE, 0).toString()
                binding.bonusBlue.setBackgroundResource(R.color.arts)
                binding.bonusGreen.text = cardBonusMap.getOrDefault(CardColor.GREEN, 0).toString()
                binding.bonusGreen.setBackgroundResource(R.color.science)
                binding.bonusOrange.text = cardBonusMap.getOrDefault(CardColor.ORANGE, 0).toString()
                binding.bonusOrange.setBackgroundResource(R.color.crafts)
                binding.bonusRed.text = cardBonusMap.getOrDefault(CardColor.RED, 0).toString()
                binding.bonusRed.setBackgroundResource(R.color.civic)
                binding.bonusYellow.text = cardBonusMap.getOrDefault(CardColor.YELLOW, 0).toString()
                binding.bonusYellow.setBackgroundResource(R.color.religion)
            })
        // Observer für Cities
        civicViewModel.cities.observe(getViewLifecycleOwner()) { citiesValue ->
            citiesValue?.let { nonNullCities ->
                currentCities = nonNullCities
                restoreCityButton(nonNullCities)
                checkASTInternal()
            }
        }
        civicViewModel.cardsVpLiveData.observe(getViewLifecycleOwner()) { vpValue: Int? ->
            vpValue?.let { nonNullVp ->
                currentCardsVp = nonNullVp
                if (civicViewModel.cities.value != null && currentAllPurchases.isNotEmpty()) {
                    checkASTInternal()
                }
            }
        }

        civicViewModel.inventoryAsCardLiveData.observe(getViewLifecycleOwner()) { purchases ->
            currentAllPurchases = purchases
            // Wenn alle anderen benötigten Daten auch schon da sind, checkAST ausführen
            if (civicViewModel.cities.value != null && currentCardsVp != 0) {
                checkASTInternal()
            }
        }

        // Observer für Time
        civicViewModel.timeVp.observe(getViewLifecycleOwner()) { value: Int ->
            val index = value / 5
            if (CivicViewModel.Companion.TIME_TABLE.indices.contains(index)) {
                binding.tvTime.text = CivicViewModel.Companion.TIME_TABLE[index]
            } else {
                binding.tvTime.text = "-"
            }
        }
    }

    private fun setAstStatus(textView: TextView, achieved: Boolean) {
        if (achieved) {
            textView.setBackgroundResource(R.color.ast_green)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ast_onGreen))
        } else {
            textView.setBackgroundResource(R.color.ast_red)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ast_onRed))
        }
    }

    /**
     * checks if certain requirements are set to advance further on the AST and sets
     * the background on the dashboard of the respective info textview
     */
    internal fun checkASTInternal() {
        val ast = civicViewModel.astVersion.value
        val astMarkerText: String?
        val countAllPurchases = currentAllPurchases.size
        var countSize100 = 0
        var countSize200 = 0
        val ebaAchieved: Boolean
        val mbaAchieved: Boolean
        val lbaAchieved: Boolean
        val eiaAchieved: Boolean
        val liaAchieved: Boolean

        // count the number of cards which have a buying price greater 100 and 200
        for (card in currentAllPurchases) {
            if (card.price >= 100) {
                countSize100++
            }
            if (card.price >= 200) {
                countSize200++
            }
        }

        if ("basic" == ast) {
            astMarkerText = "AST (B)"
            // EBA: 2 cities
            ebaAchieved = (currentCities >= 2)
            // MBA: 3 cities & 3 cards
            mbaAchieved = (currentCities >= 3 && countAllPurchases >= 3)
            // LBA: 3 cities & 3 cards 100+
            lbaAchieved = (currentCities >= 3 && countSize100 >= 3)
            // EIA: 4 cities & 2 cards 200+
            eiaAchieved = (currentCities >= 4 && countSize200 >= 2)
            // LIA: 5 cities & 3 cards 200+
            liaAchieved = (currentCities >= 5 && countSize200 >= 3)
        } else { // "expert" AST
            astMarkerText = "AST (E)"
            // EBA: 2 cities
            ebaAchieved = (currentCities >= 3)
            // MBA: 3 cities & 5 VP
            mbaAchieved = (currentCities >= 3 && currentCardsVp >= 5)
            // LBA: 4 cities & 12 cards
            lbaAchieved = (currentCities >= 4 && countAllPurchases >= 12)
            // EIA: 5 cities & 10 cards 100+ & 38 VP
            eiaAchieved = (currentCities >= 5 && countSize100 >= 10 && currentCardsVp >= 38)
            // LIA: 6 cities & 17 cards 100+ & 56 VP
            liaAchieved = (currentCities >= 6 && countSize100 >= 17 && currentCardsVp >= 56)
        }
        binding.tvAST.text = astMarkerText

        if (isAdded && context != null) { // Prüfen, ob das Fragment attached ist und einen Context hat
            setAstStatus(binding.tvEBA, ebaAchieved)
            setAstStatus(binding.tvMBA, mbaAchieved)
            setAstStatus(binding.tvLBA, lbaAchieved)
            setAstStatus(binding.tvEIA, eiaAchieved)
            setAstStatus(binding.tvLIA, liaAchieved)
        } else {
            Log.w(
                "HomeFragment",
                "checkASTInternal called when fragment not attached or context is null."
            )
        }
    }

    fun onCitiesClicked(view: View) {
        val checked = (view as RadioButton).isChecked
        if (!checked) return

        val index = cityIds.indexOf(view.id)
        if (index != -1) {
            civicViewModel.setCities(index)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == R.id.tvTime) {
            var timeTableLength = CivicViewModel.Companion.TIME_TABLE.size
            if ("basic" == civicViewModel.astVersion.getValue()) {
                timeTableLength--
            }
            for (i in 0..<timeTableLength) {
                menu.add(0, i * 5, i, CivicViewModel.Companion.TIME_TABLE[i])
            }
        } else if (v.id == R.id.tvCivilization) {
            val context = requireContext()
            val entries = context.resources.getStringArray(R.array.civilizations_entries)
            val values = context.resources.getStringArray(R.array.civilizations_values)
            for (i in values.indices) {
                menu.add(1, i, i, entries[i]) // Group 1 = Civilization
            }
        } else if (v.id == R.id.tvAST) {
            menu.add(2, 0, 0, resources.getStringArray(R.array.ast_entries)[0])
            menu.add(2, 1, 1, resources.getStringArray(R.array.ast_entries)[1])
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId == 0) {
            // Time Menu
            civicViewModel.setTimeVp(item.itemId)
            binding.tvTime.text = item.title
            return true
        } else if (item.groupId == 1) {
            // Civilization Menu
            val values = resources.getStringArray(R.array.civilizations_values)
            if (item.itemId < values.size) {
                val selectedValue = values[item.itemId]
                civicViewModel.setCivNumber(selectedValue)
                binding.tvCivilization.text = getString(R.string.tv_ast, selectedValue)
                return true
            }
        } else if (item.groupId == 2) {
            // AST Menu
            val entries = resources.getStringArray(R.array.ast_values)
            civicViewModel.setAstVersion(entries[item.itemId])
            checkASTInternal()
            return true
        }
        return super.onContextItemSelected(item)
    }
}