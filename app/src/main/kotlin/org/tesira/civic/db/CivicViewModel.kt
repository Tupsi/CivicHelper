package org.tesira.civic.db

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.preference.PreferenceManager
import org.tesira.civic.Calamity
import org.tesira.civic.Event
import org.tesira.civic.R
import java.util.Locale

class CivicViewModel(application: Application) :
    AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var buyableCardsMapObserver: Observer<MutableList<Card>>
    private lateinit var tipsArray: Array<String>
    private val mRepository: CivicRepository = CivicRepository(application)
    private val vp = MutableLiveData<Int>(0)
    private val cities = MutableLiveData<Int>(0)
    private val mApplication: Application = application
    private val timeVp = MutableLiveData<Int>(0)
    private val showAnatomyDialogEvent = MutableLiveData<Event<List<String>>>()
    private val specialAbilitiesRawLiveData: LiveData<List<String>> = mRepository.specialAbilitiesLiveData
    private val immunitiesRawLiveData: LiveData<List<String>> = mRepository.immunitiesLiveData
    private val combinedSpecialsAndImmunitiesLiveData = MediatorLiveData<MutableList<String>>()
    private val defaultPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplication)
    private val totalVp = MediatorLiveData<Int>()
    private val buyableCardMap: MutableMap<String, Card> = HashMap<String, Card>()
    private val areBuyableCardsReady = MutableLiveData<Boolean?>(false)
    private val userPreferenceForHeartCards = MutableLiveData<String?>()
    private val allCardsUnsortedOnce: LiveData<List<CardWithDetails>> = mRepository.getAllCardsWithDetailsUnsorted()
    private val _pendingExtraCredits = MutableLiveData<Int?>()
    private val _customCardSelectionForHeart = MutableLiveData<Set<String>>(emptySet())

    private val _isFinalizingPurchase = MutableLiveData(false)
    val isFinalizingPurchase: LiveData<Boolean> = _isFinalizingPurchase
    private val _showCredits = MutableLiveData<Boolean>()
    val showCredits: LiveData<Boolean> = _showCredits
    private val _selectedTipIndex = MutableLiveData<Int>()
    var selectedTipIndex: LiveData<Int> = _selectedTipIndex
    private val _astVersion = MutableLiveData<String>()
    var astVersion: LiveData<String> = _astVersion
    private val _civNumber = MutableLiveData<String>()
    var getCivNumber: LiveData<String> = _civNumber
    private val _showExtraCreditsDialogEvent = MutableLiveData<Event<Int>>()
    val showExtraCreditsDialogEvent: LiveData<Event<Int>> get() = _showExtraCreditsDialogEvent
    private val _columns = MutableLiveData<Int>()
    private val _navigateToDashboardEvent = MutableLiveData<Event<Boolean>>()
    val navigateToDashboardEvent: LiveData<Event<Boolean>> get() = _navigateToDashboardEvent
    private val _navigateToCivilizationSelectionEvent = MutableLiveData<Event<Unit>>()
    val navigateToCivilizationSelectionEvent: LiveData<Event<Unit>> get() = _navigateToCivilizationSelectionEvent
    private val _selectedCardKeysForState = MutableLiveData<MutableSet<String?>?>(mutableSetOf<String?>())
    val selectedCardKeysForState: LiveData<MutableSet<String?>?> get() = _selectedCardKeysForState

    private val _currentSortingOrder = MutableLiveData<String>()
    val currentSortingOrder: LiveData<String> get() = _currentSortingOrder
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    val treasure: MutableLiveData<Int> = MutableLiveData<Int>(0)
    val remaining: MutableLiveData<Int> = MutableLiveData<Int>(0)
    val calamityBonusListLiveData: LiveData<List<Calamity>> = mRepository.calamityBonusLiveData
    val cardsVpLiveData: LiveData<Int> = mRepository.cardsVp
    val inventoryAsCardLiveData: LiveData<List<Card>> get() = mRepository.inventoryAsCardLiveData
    val blue: Int get() = cardBonus.getValue()!!.getOrDefault(CardColor.BLUE, 0)
    val green: Int get() = cardBonus.getValue()!!.getOrDefault(CardColor.GREEN, 0)
    val orange: Int get() = cardBonus.getValue()!!.getOrDefault(CardColor.ORANGE, 0)
    val red: Int get() = cardBonus.getValue()!!.getOrDefault(CardColor.RED, 0)
    val yellow: Int get() = cardBonus.getValue()!!.getOrDefault(CardColor.YELLOW, 0)

    var librarySelected: Boolean = false
    var cardBonus: MutableLiveData<HashMap<CardColor, Int>> = MutableLiveData<HashMap<CardColor, Int>>(HashMap<CardColor, Int>())

    val allAdvancesNotBought: LiveData<MutableList<Card>>
    val allCardsWithDetails: LiveData<List<CardWithDetails>>


    init {
        defaultPrefs.registerOnSharedPreferenceChangeListener(this)
        loadData()
//        allCardsWithDetails = _currentSortingOrder.switchMap { sortOrder ->
//            allCardsUnsortedOnce.map { unsortedList ->
////                Log.d("CivicViewModel", "Sortiere allCards. Unsortierte Liste vorhanden (Größe: ${unsortedList?.size ?: 0}). SortOrder: $sortOrder")
//                if (unsortedList.isEmpty()) {
//                    emptyList()
//                } else {
//                    sortCardList(unsortedList, sortOrder)
//                }
//            }
//        }

        allCardsWithDetails = MediatorLiveData<List<CardWithDetails>>().apply {
            var currentUnsortedList: List<CardWithDetails>? = null
            var currentSortOrder: String? = _currentSortingOrder.value // Nur noch ein Sortierparameter
            var currentQuery: String? = _searchQuery.value

            fun updateFilterAndSort() {
                val unsortedList = currentUnsortedList
                val sortOrder = currentSortOrder
                val query = currentQuery

                if (unsortedList != null && sortOrder != null && query != null) {
                    Log.d("CivicViewModel", "updateFilterAndSort triggered. Query: '$query', SortOrder: $sortOrder, Unsorted Size: ${unsortedList.size}")

                    // 1. Filtern basierend auf dem Suchbegriff
                    val filteredList = filterCardList(unsortedList, query)
                    Log.d("CivicViewModel", "Filtered list size: ${filteredList.size}")

                    // 2. Sortieren der gefilterten Liste basierend auf dem kombinierten Sortier-String
                    value = sortCardList(filteredList, sortOrder) // sortCardList braucht nur noch den sortOrder
                    Log.d("CivicViewModel", "Final sorted list size for allCardsWithDetails: ${this.value?.size}")
                } else {
                    Log.d("CivicViewModel", "updateFilterAndSort skipped. Unsorted: ${unsortedList != null}, SortOrder: ${sortOrder != null}, Query: ${query != null}")
                }
            }

            addSource(allCardsUnsortedOnce) { list ->
                Log.d("CivicViewModel", "allCardsUnsortedOnce changed. Size: ${list?.size}")
                currentUnsortedList = list
                updateFilterAndSort()
            }
            addSource(_currentSortingOrder) { sortOrder -> // Beobachtet nur noch _currentSortingOrder
                Log.d("CivicViewModel", "_currentSortingOrder changed to: $sortOrder")
                currentSortOrder = sortOrder
                updateFilterAndSort()
            }
            // addSource für _isCurrentSortAscending entfällt
            addSource(_searchQuery) { query ->
                Log.d("CivicViewModel", "_searchQuery changed to: '$query'")
                currentQuery = query
                updateFilterAndSort()
            }
        }

        allAdvancesNotBought = _currentSortingOrder
            .switchMap { order: String ->
                mRepository.getAllAdvancesNotBoughtLiveData(order).map { it.toMutableList() }
            }

        setupTotalVpMediator()
        setupCombinedSpecialsLiveData()
        setupBuyableCardsObserver()
    }

    fun loadData() {

        val blue: Int = defaultPrefs.getInt(CardColor.BLUE.colorName, 0)
        val green: Int = defaultPrefs.getInt(CardColor.GREEN.colorName, 0)
        val orange: Int = defaultPrefs.getInt(CardColor.ORANGE.colorName, 0)
        val red: Int = defaultPrefs.getInt(CardColor.RED.colorName, 0)
        val yellow: Int = defaultPrefs.getInt(CardColor.YELLOW.colorName, 0)

        val currentBonuses = cardBonus.value!!
        currentBonuses.put(CardColor.BLUE, blue)
        currentBonuses.put(CardColor.GREEN, green)
        currentBonuses.put(CardColor.ORANGE, orange)
        currentBonuses.put(CardColor.RED, red)
        currentBonuses.put(CardColor.YELLOW, yellow)
        cardBonus.value = currentBonuses

        userPreferenceForHeartCards.value = defaultPrefs.getString(PREF_KEY_HEART, "custom")
        setCities(defaultPrefs.getInt(PREF_KEY_CITIES, 0))
        setTimeVp(defaultPrefs.getInt(PREF_KEY_TIME, 0))
        _columns.value = defaultPrefs.getString(PREF_KEY_COLUMNS, "0")!!.toInt()
        _currentSortingOrder.value = defaultPrefs.getString(PREF_KEY_SORT, "name") ?: "name"
        _astVersion.value = defaultPrefs.getString(PREF_KEY_AST, "basic")
        _civNumber.value = defaultPrefs.getString(PREF_KEY_CIVILIZATION, "not set")

        tipsArray = mApplication.resources.getStringArray(R.array.tips)
        _selectedTipIndex.value = _civNumber.value?.toIntOrNull()?.minus(1)
        _showCredits.value = defaultPrefs.getBoolean(PREF_KEY_SHOW_CREDITS, true)

        _customCardSelectionForHeart.value = defaultPrefs.getStringSet(
            PREF_KEY_CUSTOM_HEART_CARDS,
            emptySet()
        ) ?: emptySet()
        Log.d("CivicViewModel", "Loaded initial custom heart selection: ${_customCardSelectionForHeart.value?.size} items")

    }

    fun getShowAnatomyDialogEvent(): LiveData<Event<List<String>>> {
        return showAnatomyDialogEvent
    }

    fun setAstVersion(version: String) {
        _astVersion.value = version
        defaultPrefs.edit { putString(PREF_KEY_AST, version) }
    }

    fun setCivNumber(civNumber: String) {
        _civNumber.value = civNumber
        _selectedTipIndex.value = civNumber.toIntOrNull()?.minus(1)
        defaultPrefs.edit { putString(PREF_KEY_CIVILIZATION, civNumber) }
    }

    private fun setupBuyableCardsObserver() {
        buyableCardsMapObserver = Observer { cards: MutableList<Card> ->
            buyableCardMap.clear()
            for (card in cards) {
                buyableCardMap.put(card.name, card)
            }
            areBuyableCardsReady.value = true
        }
        allAdvancesNotBought.observeForever(buyableCardsMapObserver)
    }

    private fun getBuyableAdvanceByNameFromMap(name: String): Card? {
        if (areBuyableCardsReady.value == true) {
            return buyableCardMap[name]
        }
        return null
    }

    private fun setupCombinedSpecialsLiveData() {
        // Der Observer für Änderungen in specialAbilitiesRawLiveData
        val abilitiesObserver = Observer<List<String>?> { abilities: List<String>? ->
            val currentImmunities = immunitiesRawLiveData.value
            combineAndSetData(
                abilities ?: emptyList(),
                currentImmunities ?: emptyList()
            )
        }

        // Der Observer für Änderungen in immunitiesRawLiveData
        val immunitiesObserver = Observer<List<String>?> { immunities: List<String>? ->
            val currentAbilities = specialAbilitiesRawLiveData.value
            combineAndSetData(
                currentAbilities ?: emptyList(),
                immunities ?: emptyList()
            )
        }

        combinedSpecialsAndImmunitiesLiveData.addSource(
            specialAbilitiesRawLiveData,
            abilitiesObserver
        )
        combinedSpecialsAndImmunitiesLiveData.addSource(
            immunitiesRawLiveData,
            immunitiesObserver
        )
    }

    private fun combineAndSetData(
        abilities: List<String>,
        immunities: List<String>
    ) {
        val combinedList: MutableList<String> = ArrayList<String>()
        if (abilities.isNotEmpty()) {
            combinedList.add("___Special Abilities")
            combinedList.addAll(abilities)
        }
        if (immunities.isNotEmpty()) {
            combinedList.add("___Immunities")
            combinedList.addAll(immunities)
        }

        val distinctList = combinedList.distinct()
        combinedSpecialsAndImmunitiesLiveData.value = distinctList.toMutableList()
    }

    fun getCombinedSpecialsAndImmunitiesLiveData(): LiveData<MutableList<String>> {
        return combinedSpecialsAndImmunitiesLiveData
    }

    private fun setupTotalVpMediator() {
        totalVp.value = 0

        // Quelle 1: cardsVpFromDao
        totalVp.addSource<Int?>(
            this.cardsVpLiveData,
            Observer { cardsVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp = cardsVal ?: 0
                val currentCitiesVp = (if (cities.getValue() != null) cities.getValue() else 0)!!
                val currentTimeVp = (if (timeVp.getValue() != null) timeVp.getValue() else 0)!!
                totalVp.value = currentCardsVp + currentCitiesVp + currentTimeVp
            })

        // Quelle 2: cities LiveData
        totalVp.addSource<Int?>(
            cities,
            Observer { cityVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp: Int =
                    (if (cardsVpLiveData.getValue() != null) cardsVpLiveData.getValue() else 0)!!
                val currentCitiesVp = cityVal ?: 0
                val currentTimeVp = (if (timeVp.getValue() != null) timeVp.getValue() else 0)!!
                totalVp.value = currentCardsVp + currentCitiesVp + currentTimeVp
            })

        // Quelle 3: timeVp LiveData
        totalVp.addSource<Int?>(
            timeVp,
            Observer { timeVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp: Int =
                    (if (cardsVpLiveData.getValue() != null) cardsVpLiveData.getValue() else 0)!!
                val currentCitiesVp = (if (cities.getValue() != null) cities.getValue() else 0)!!
                val currentTimeVp = timeVal ?: 0
                totalVp.value = currentCardsVp + currentCitiesVp + currentTimeVp
            })
    }

    fun getTotalVp(): LiveData<Int?> {
        return totalVp
    }

    val columns: Int
        get() = _columns.getValue()!!


    fun getCities(): Int {
        return (if (cities.getValue() != null) cities.getValue() else 0)!!
    }

    val citiesLive: LiveData<Int?>
        get() = cities

    fun setCities(value: Int) {
        cities.value = value
    }

    fun requestPriceRecalculation() {
        mRepository.recalculateCurrentPricesAsync(cardBonus)
    }

    fun getTimeVp(): Int {
        return timeVp.getValue()!!
    }

    val timeVpLive: LiveData<Int?>
        get() = timeVp

    fun setTimeVp(value: Int) {
        timeVp.value = value
    }

    fun insertPurchase(purchase: String) {
        mRepository.insertPurchase(purchase)
    }

    fun insertCard(card: Card) {
        mRepository.insertCard(card)
    }

    fun setSortingOrder(newSortOrder: String) {
        if (_currentSortingOrder.value != newSortOrder) {
            _currentSortingOrder.value = newSortOrder
            // will ich das speichern? Dann wärs kein Setting mehr oder?
            // defaultPrefs.edit {putString(PREF_KEY_SORT, _currentSortingOrder.value)}
        }
    }

    fun setSearchQuery(query: String) {
        // Verhindere unnötige Updates, wenn sich der Query nicht wirklich ändert
        val trimmedQuery = query.trim()
        if (_searchQuery.value != trimmedQuery) {
            _searchQuery.value = trimmedQuery
        }
    }

    private fun filterCardList(listToFilter: List<CardWithDetails>, query: String): List<CardWithDetails> {
        if (query.isBlank()) {
            return listToFilter
        }
        val lowerCaseQuery = query.lowercase()
        return listToFilter.filter { cardWithDetails ->
            val card = cardWithDetails.card
            val effectsText = cardWithDetails.effects.joinToString(separator = " ") { it.name }
            val specialsText = cardWithDetails.specialAbilities.joinToString(separator = " ") { it.ability }
            val immunitiesText = cardWithDetails.immunities.joinToString(separator = " ") { it.immunity }

            card.name.lowercase().contains(lowerCaseQuery) ||
//                    card.group1?.toString()?.lowercase()?.contains(lowerCaseQuery) == true ||
//                    card.group2?.toString()?.lowercase()?.contains(lowerCaseQuery) == true ||
                    effectsText.lowercase().contains(lowerCaseQuery) ||
                    specialsText.lowercase().contains(lowerCaseQuery) ||
                    immunitiesText.lowercase().contains(lowerCaseQuery)
        }
    }

    /**
     * Performs the core logic for starting a new game.
     * This includes resetting persistent data and ViewModel state.
     */
    fun startNewGameProcess() {
        mRepository.deleteInventory()
        mRepository.resetCurrentPrice()
        mRepository.resetDB(mApplication.applicationContext)
        treasure.value = 0
        remaining.value = 0
        cities.value = 0
        timeVp.value = 0
        vp.value = 0
        cardBonus.value = HashMap<CardColor, Int>()
        cardBonus.value = cardBonus.value
        librarySelected = false

        defaultPrefs.edit { putInt(CardColor.BLUE.colorName, 0) }
        defaultPrefs.edit { putInt(CardColor.GREEN.colorName, 0) }
        defaultPrefs.edit { putInt(CardColor.ORANGE.colorName, 0) }
        defaultPrefs.edit { putInt(CardColor.RED.colorName, 0) }
        defaultPrefs.edit { putInt(CardColor.YELLOW.colorName, 0) }

        defaultPrefs.edit { putInt(PREF_KEY_CITIES, 0) }
        defaultPrefs.edit { putInt(PREF_KEY_TIME, 0) }
        defaultPrefs.edit { putInt(PREF_KEY_TREASURE, 0) }
        defaultPrefs.edit { remove(PREF_KEY_HEART) }
        defaultPrefs.edit { remove(PREF_KEY_CIVILIZATION) }

//        _newGameStartedEvent.value = Event(true)
        _navigateToCivilizationSelectionEvent.value = Event(Unit)
    }

    /**
     * Calculates the sum of all currently selected advances during the buy process and
     * updates remaining treasure.
     * @param selection Currently selected cards from the View.  <-- PARAMETER TYP ANGEPASST
     */
    fun calculateTotal(selection: Iterable<String>) {
        if (java.lang.Boolean.TRUE != areBuyableCardsReady.getValue()) {
            val currentTreasure: Int =
                (if (treasure.getValue() != null) treasure.getValue() else 0)!!
            this.remaining.value =
                currentTreasure
            return
        }

        var newTotalCost = 0
        val iterator: Iterator<String> = selection.iterator()
        if (iterator.hasNext()) {
            for (name in selection) {
                val adv = getBuyableAdvanceByNameFromMap(name)
                if (adv != null) {
                    newTotalCost += adv.currentPrice
                } else {
                    Log.e(
                        "CivicViewModel",
                        "Card with name '$name' not found in buyableCardMap during calculateTotal."
                    )
                }
            }
        }
        val currentTreasure: Int = (if (treasure.getValue() != null) treasure.getValue() else 0)!!
        this.remaining.value = currentTreasure - newTotalCost // remaining wird hier aktualisiert
    }

    /**
     * Updates the Bonus for all colors by adding the respective fields to cardBonus HashSet.
     * @param card one of the civilization cards.
     */
    fun addBonus(card: Card) {
        val currentBonuses = cardBonus.value!!
        val adder: (Int, Int) -> Int = { oldValue, valueToAdd -> oldValue + valueToAdd }
        if (card.creditsBlue != 0) currentBonuses.merge(CardColor.BLUE, card.creditsBlue, adder)
        if (card.creditsGreen != 0) currentBonuses.merge(CardColor.GREEN, card.creditsGreen, adder)
        if (card.creditsOrange != 0) currentBonuses.merge(
            CardColor.ORANGE,
            card.creditsOrange,
            adder
        )
        if (card.creditsRed != 0) currentBonuses.merge(CardColor.RED, card.creditsRed, adder)
        if (card.creditsYellow != 0) currentBonuses.merge(
            CardColor.YELLOW,
            card.creditsYellow,
            adder
        )
        cardBonus.value = currentBonuses
    }

    /**
     * Adds the bonuses of a bought card to the cardBonus HashSet.
     * @param name The name of the bought card.
     */
    fun addBonus(name: String) {
        val card = getBuyableAdvanceByNameFromMap(name)
        addBonus(card!!)
    }

    /**
     * Orchestrates the purchase process. Delegates the database operations to the Repository
     * and handles the UI responses (dialogs) based on the results.
     * This method is called from the Fragment.
     * @param selectedCardNames The names of the cards selected for purchase.
     */
    fun processPurchases(selectedCardNames: List<String>) {
        _isFinalizingPurchase.value = true
        // Rufe die asynchrone Methode im Repository auf
        mRepository.processPurchasesAndRecalculatePricesAsync(
            selectedCardNames, cardBonus,
            object : PurchaseCompletionCallback {
                override fun onPurchaseCompleted(
                    totalExtraCredits: Int,
                    anatomyCardsToChoose: List<String>
                ) {
                    _isFinalizingPurchase.postValue(false)

                    if (anatomyCardsToChoose.isNotEmpty()) {
                        // Speichere die ExtraCredits für später, falls vorhanden
                        if (totalExtraCredits > 0) {
                            _pendingExtraCredits.postValue(totalExtraCredits)
                        }
                        // Zeige ZUERST den Anatomy-Dialog
                        showAnatomyDialogEvent.postValue(Event(anatomyCardsToChoose))
                    } else if (totalExtraCredits > 0) {
                        // Keine Anatomy-Karten, zeige direkt den ExtraCredits-Dialog
                        _showExtraCreditsDialogEvent.postValue(Event(totalExtraCredits))
                        _navigateToDashboardEvent.postValue(Event(true))
                    } else {
                        // Weder Anatomy noch Extra Credits
                        _navigateToDashboardEvent.postValue(Event(true))
                    }
                }

                override fun onPurchaseFailed(errorMessage: String) {
                    Log.e(
                        "CivicViewModel",
                        "PurchaseCompletionCallback: onPurchaseFailed. Error: $errorMessage"
                    )
                    // Handle error, maybe show a Toast via another LiveData event
                    // _showErrorToastEvent.postValue(new Event<>(errorMessage));
                    _isFinalizingPurchase.postValue(false)
                }
            })
    }

    fun onAnatomyCardSelected(selectedGreenCardName: String) {
        // 1. Verarbeite die ausgewählte grüne Karte (Boni hinzufügen etc.)
        //    Dies könnte eine ähnliche Logik wie in deinem Repository sein,
        //    um die Boni dieser Karte zu `cardBonus` hinzuzufügen.
        //    Beispielhaft:
        val card = getBuyableAdvanceByNameFromMap(selectedGreenCardName) // Oder hol es aus dem Repo
        if (card != null) {
            // Füge die Boni der ausgewählten grünen Karte hinzu.
            // Du brauchst eine Methode, die die Boni einer einzelnen Karte zu `cardBonus` addiert.
            // Das könnte eine angepasste Version von `updateBonus` sein oder eine neue Methode.
            addBonus(card)
            saveBonus() // Speichere die aktualisierten Boni
        }

        // 2. Prüfe, ob Extra Credits anstanden und zeige jetzt den Dialog
        _pendingExtraCredits.value?.let { credits ->
            _showExtraCreditsDialogEvent.postValue(Event(credits))
            _pendingExtraCredits.value = null // Zurücksetzen
        }

        // 3. Navigiere weiter, falls nötig (z.B. wenn keine Extra Credits anstanden)
        if (_pendingExtraCredits.value == null) {
            _navigateToDashboardEvent.postValue(Event(true))
        }
    }

    fun saveBonus() {
        Log.d("CivicViewModel", "Saving bonus to SharedPreferences.")
        // HashMap Save
        for (entry in this.cardBonus.getValue()!!.entries) {
            defaultPrefs.edit { putInt(entry.key.colorName, entry.value) }
        }
    }

    fun saveData() {
        defaultPrefs.edit { putInt(PREF_KEY_CITIES, getCities()) }
        defaultPrefs.edit { putInt(PREF_KEY_TIME, getTimeVp()) }
    }

    /**
     * Triggers the event to show the Extra Credits Dialog.
     * The observing Fragment will react to this event.
     *
     * @param credits The number of extra credits to offer.
     */
    fun triggerExtraCreditsDialog(credits: Int) {
        _showExtraCreditsDialogEvent.postValue(Event<Int>(credits))
    }

    override fun onCleared() {
        super.onCleared()
        allAdvancesNotBought.removeObserver(buyableCardsMapObserver)
        defaultPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    // Schnittstelle für die Callback vom Repository
    interface PurchaseCompletionCallback {
        fun onPurchaseCompleted(totalExtraCredits: Int, anatomyCardsToChoose: List<String>)
        fun onPurchaseFailed(errorMessage: String) // Optional: Fehlerbehandlung
    }

    private fun sortCardList(list: List<CardWithDetails>, sortOrder: String): List<CardWithDetails> {
//        Log.d("CivicViewModel", "sortCardList called with sortOrder: $sortOrder. List size: ${list.size}")


        // Log.d("CivicViewModel", "Criterion: $criterion, isAscending: $isAscending") // Optional für Debugging

        return when (sortOrder) {
            "family" -> list.sortedBy { it.card.family }
            "name" -> list.sortedBy { it.card.name }
            "currentPrice" -> {
                val comparator = compareBy<CardWithDetails> { it.card.price }.thenBy { it.card.name }
                list.sortedWith(comparator)
            }

            "color" -> {
                val comparator = compareBy<CardWithDetails> { it.card.group1?.ordinal }.thenBy { it.card.price }
                list.sortedWith(comparator)
            }

            "vp" -> {
                val comparator = compareBy<CardWithDetails> { it.card.vp }.thenBy { it.card.price }
                list.sortedWith(comparator)
            }

            "heart" -> {
                val comparator = compareByDescending<CardWithDetails> { it.card.hasHeart }.thenBy { it.card.price }.thenBy { it.card.name }
                list.sortedWith(comparator)
            }

            else -> list.sortedBy { it.card.name }
        }
    }

    // NEU: Methode, um die Auswahl aus dem Fragment im ViewModel zu aktualisieren
    fun updateSelectionState(currentSelection: MutableSet<String?>?) {
        if (currentSelection == null) {
            _selectedCardKeysForState.value = mutableSetOf<String?>()
        } else {
            _selectedCardKeysForState.value = HashSet<String?>(currentSelection)
        }
    }

    // NEU oder ANPASSEN: Methode, um die gespeicherte Auswahl zu löschen
    fun clearCurrentSelectionState() {
        _selectedCardKeysForState.value = mutableSetOf<String?>()
        // Die calculateTotal-Logik sollte idealerweise durch den SelectionTracker-Observer
        // im Fragment ausgelöst werden, wenn der Tracker geleert wird.
        // Wenn du hier explizit totalPrice etc. zurücksetzen willst:
        // calculateTotal(Collections.emptySet());
    }

    fun setSelectedTipIndex(index: Int) {
        if (index >= 0 && (index < tipsArray.size)) {
            _selectedTipIndex.value = index
        }
    }

    fun getTipForIndex(index: Int): String? {
        if (index >= 0 && index < tipsArray.size) {
            return tipsArray[index]
        }
        Log.w(
            "CivicViewModel",
            "Attempted to get tip for invalid index: $index or tipsArray not loaded."
        )
        return "" // Fallback
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        // Civilization selection
        if (PREF_KEY_CIVILIZATION == key) {
            val civicNumber = sharedPreferences.getString(key, "1")!!.toInt()
            val newCivNumber: String = sharedPreferences.getString(key, "not set")!!
            if (_civNumber.getValue() == null || _civNumber.getValue() != newCivNumber) {
                _civNumber.value = newCivNumber
            }
            val newIndex = civicNumber - 1
            if (_selectedTipIndex.getValue() == null || _selectedTipIndex.getValue() != newIndex) {
                _selectedTipIndex.value = newIndex
            }
            // Sorting of cards (buying)
        } else if (PREF_KEY_SORT == key) {
            val newSortingOrder: String = sharedPreferences.getString(key, "name")!!
            if (_currentSortingOrder.getValue() == null || _currentSortingOrder.getValue() != newSortingOrder) {
                _currentSortingOrder.value = newSortingOrder
            }
            // What do you want
        } else if (PREF_KEY_HEART == key) {
            val newHeartSelection: String = sharedPreferences.getString(key, "custom")!!
            if (userPreferenceForHeartCards.getValue() == null || userPreferenceForHeartCards.getValue() != newHeartSelection) {
                userPreferenceForHeartCards.value = newHeartSelection
                processHeartPreferenceChange(newHeartSelection)
            }
            // Number of columns
        } else if (key == PREF_KEY_CUSTOM_HEART_CARDS) {
            // Die Custom-Liste selbst wurde geändert.
            val newCustomSet = sharedPreferences.getStringSet(key, emptySet()) ?: emptySet()
            if (_customCardSelectionForHeart.value != newCustomSet) {
                _customCardSelectionForHeart.value = newCustomSet
                Log.d("CivicViewModel", "onSharedPreferenceChanged: Custom selection updated from prefs. Size: ${newCustomSet.size}")
                // Wenn "custom" gerade aktiv ist, müssen wir die Herzen in der DB aktualisieren.
                if (userPreferenceForHeartCards.value == "custom") {
                    processHeartPreferenceChange("custom")
                }
            }
        } else if (PREF_KEY_COLUMNS == key) {
            val newColumns = sharedPreferences.getString(key, "0")!!.toInt()
            if (_columns.getValue() == null || _columns.getValue() != newColumns) {
                _columns.value = newColumns
            }
            // Game Version
        } else if (PREF_KEY_AST == key) {
            val newAstVersion: String = sharedPreferences.getString(key, "basic")!!
            if (_astVersion.getValue() == null || _astVersion.getValue() != newAstVersion) {
                _astVersion.value = newAstVersion
            }
        } else if (PREF_KEY_SHOW_CREDITS == key) {
            val newShowCredits = sharedPreferences.getBoolean(key, true)
            if (_showCredits.value == null || _showCredits.value != newShowCredits) {
                _showCredits.value = newShowCredits
            }
        }
    }

    fun customHeartSettingsUpdated() {
        // 1. Lade die neue Custom-Auswahl aus den SharedPreferences
        _customCardSelectionForHeart.value = defaultPrefs.getStringSet(
            PREF_KEY_CUSTOM_HEART_CARDS,
            emptySet()
        ) ?: emptySet()
        Log.d("CivicViewModel", "customHeartSettingsUpdated: New custom selection size: ${_customCardSelectionForHeart.value?.size}")

        // 2. Wenn die aktuelle "Heart"-Einstellung "custom" ist,
        //    dann die DB mit der neuen Custom-Liste aktualisieren.
        if (userPreferenceForHeartCards.value == "custom") {
            processHeartPreferenceChange("custom")
        }
        // Kein expliziter Trigger für Mediator nötig, wenn er auf Room-Änderungen lauscht.
    }

    private fun processHeartPreferenceChange(selectionName: String) {
        val cardNamesToMarkAsHeart: List<String> = getCardNamesForHeartSelection(selectionName)

        // Wichtig: Diese Datenbankoperationen sollten in einem Hintergrundthread ausgeführt werden.
        // mRepository sollte Methoden anbieten, die dies intern tun (z.B. mit Coroutinen oder AsyncTask).
        mRepository.resetAllCardsHeartStatusAsync(CivicRepository.RepositoryCallback {
            // Dieser Callback wird ausgeführt, nachdem alle Herzen zurückgesetzt wurden.
            if (cardNamesToMarkAsHeart.isNotEmpty()) {
                mRepository.setCardsAsHeartAsync(
                    cardNamesToMarkAsHeart,
                    CivicRepository.RepositoryCallback {
                    })
            } else {
                Log.d(
                    "CivicViewModel",
                    "No specific cards to mark for heart selection: $selectionName or selection is 'custom'."
                )
            }
        })
    }

    /**
     * Holt die Liste der Kartennamen basierend auf der "heart"-Auswahl.
     * Nutzt deine bestehende Logik aus getChooserCards().
     */
    private fun getCardNamesForHeartSelection(selectionName: String): List<String> {
        return when (selectionName.lowercase(Locale.getDefault())) {
            "treasury" -> listOf<String>(*TREASURY)
            "commodities" -> listOf<String>(*COMMODITY_CARDS)
            "cheaper" -> listOf<String>(*CHEAPER_CIVILIZATION_CARDS)
            "bend" -> listOf<String>(*TO_BEND_THE_RULES)
            "more" -> listOf<String>(*MORE_TOKEN_ON_THE_MAP)
            "mobility" -> listOf<String>(*TOKEN_MOBILITY)
            "cities" -> listOf<String>(*CITIES)
            "sea" -> listOf<String>(*SEA_POWER)
            "aggression" -> listOf<String>(*AGGRESSION)
            "defense" -> listOf<String>(*DEFENSE)
            "custom" -> _customCardSelectionForHeart.value?.toList() ?: listOf<String>()
            else -> listOf<String>()
        }
    }

    fun calculateColumnCount(context: Context): Int {
        // 1. Hole die aktuelle Gerätekonfiguration
        val configuration = context.resources.configuration
        val screenWidthDp = configuration.screenWidthDp // Aktuelle Bildschirmbreite in dp
//        val orientation = configuration.orientation // Aktuelle Orientierung
        return if (columns == 0) {
            screenWidthDp / 180
        } else {
            columns
        }

//        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (this.columns <= 1) {
////                2 // Immer 2 Spalten im Querformat, wenn User 1 wollte
//                screenWidthDp / 200
//            } else {
//                this.columns
//            }
//        } else { // Portrait-Modus
//            // Im Portrait-Modus, verwende die Benutzereinstellung
//            this.columns
//        }

        // Alternative oder erweiterte Logik basierend auf screenWidthDp:
        // (Diese kannst du mit der obigen Orientierungslogik kombinieren oder stattdessen verwenden)
        /*
        if (screenWidthDp >= 600) { // Beispiel: typische Tablet-Breite oder sehr breites Querformat
            if (mColumnCountPreference < 2) return 2; // Mindestens 2 Spalten
            if (mColumnCountPreference < 3 && screenWidthDp >= 800) return 3; // Mindestens 3 Spalten auf sehr breiten Screens
            return mColumnCountPreference; // Ansonsten Benutzereinstellung
        } else if (screenWidthDp >= 480) { // Breiteres Smartphone im Querformat
             if (mColumnCountPreference <= 1) return 2; // Mindestens 2 Spalten
             return mColumnCountPreference;
        } else { // Schmaleres Smartphone
            return mColumnCountPreference; // Im Hochformat oder schmal, die Benutzereinstellung
        }
        */
    }

    companion object {
        val TREASURY: Array<String> = arrayOf(
            "Monarchy", "Coinage", "Trade Routes",
            "Politics", "Mining"
        )
        val COMMODITY_CARDS: Array<String> = arrayOf(
            "Rhetoric", "Cartography", "Roadbuilding",
            "Mining", "Trade Empire", "Provincial Empire", "Wonder of the World"
        )
        val CHEAPER_CIVILIZATION_CARDS: Array<String> = arrayOf(
            "Empiricism", "Written Record",
            "Literacy", "Monument", "Library", "Mining", "Mathematics", "Anatomy"
        )
        val TO_BEND_THE_RULES: Array<String> = arrayOf(
            "Coinage", "Universal Doctrine",
            "Democracy", "Wonder of the World"
        )
        val MORE_TOKEN_ON_THE_MAP: Array<String> = arrayOf(
            "Coinage", "Agriculture",
            "Universal Doctrine", "Democracy", "Public Works", "Politics", "Monotheism", "Diaspora"
        )
        val TOKEN_MOBILITY: Array<String> = arrayOf(
            "Urbanism", "Cloth Making", "Astronavigation",
            "Agriculture", "Naval Warfare", "Military", "Roadbuilding", "Public Works",
            "Advanced Military", "Diaspora"
        )
        val CITIES: Array<String> = arrayOf(
            "Urbanism", "Architecture", "Fundamentalism",
            "Engineering", "Universal Doctrine", "Democracy", "Politics", "Monotheism", "Diaspora"
        )
        val SEA_POWER: Array<String> = arrayOf(
            "Cloth Making", "Masonry", "Astronavigation",
            "Naval Warfare", "Engineering", "Calendar"
        )
        val AGGRESSION: Array<String> = arrayOf(
            "Metalworking", "Fundamentalism", "Engineering",
            "Naval Warfare", "Diplomacy", "Military", "Roadbuilding", "Politics",
            "Advanced Military", "Monotheism", "Diaspora", "Cultural Ascendancy"
        )
        val DEFENSE: Array<String> = arrayOf(
            "Metalworking", "Agriculture", "Fundamentalism",
            "Engineering", "Diplomacy", "Naval Warfare", "Military", "Roadbuilding", "Philosophy",
            "Public Works", "Politics", "Monotheism", "Theology", "Provincial Empire", "Diaspora",
            "Cultural Ascendancy"
        )

        @JvmField
        val TIME_TABLE: Array<String> = arrayOf(
            "8000 BC", "7000 BC", "6000 BC", "4300 BC", "5000 BC",
            "3300 BC", "2700 BC", "2000 BC", "1800 BC", "1700 BC", "1500 BC", "1400 BC", "1300 BC",
            "1200 BC", "800 BC", "0", "400 AD"
        )

        private const val PREF_KEY_CIVILIZATION = "civilization"
        private const val PREF_KEY_SORT = "sort"
        private const val PREF_KEY_CITIES = "cities"
        private const val PREF_KEY_TIME = "time"
        private const val PREF_KEY_TREASURE = "treasure"
        internal const val PREF_KEY_HEART = "heart"
        private const val PREF_KEY_COLUMNS = "columns"
        private const val PREF_KEY_AST = "ast"
        private const val PREF_KEY_SHOW_CREDITS = "showCredits"
        internal const val PREF_KEY_CUSTOM_HEART_CARDS = "pref_key_select_custom_cards"

        @JvmStatic
        fun getItemBackgroundColor(card: Card, res: Resources): Drawable? {

            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    ResourcesCompat.getColor(res, R.color.arts, null),
                    ResourcesCompat.getColor(res, R.color.science, null),
                    ResourcesCompat.getColor(res, R.color.crafts, null),
                    ResourcesCompat.getColor(res, R.color.civic, null),
                    ResourcesCompat.getColor(res, R.color.religion, null)
                )
            )
            var backgroundColor = 0
            if (card.group2 == null) {
                backgroundColor = when (card.group1) {
                    CardColor.ORANGE -> R.color.crafts
                    CardColor.YELLOW -> R.color.religion
                    CardColor.RED -> R.color.civic
                    CardColor.GREEN -> R.color.science
                    CardColor.BLUE -> R.color.arts
                    else -> R.color.purple_700
                }
            } else {
                when (card.name) {
                    "Engineering" -> backgroundColor = R.drawable.engineering_background
                    "Mathematics" -> backgroundColor = R.drawable.mathematics_background
                    "Mysticism" -> backgroundColor = R.drawable.mysticism_background
                    "Written Record" -> backgroundColor = R.drawable.written_record_background
                    "Theocracy" -> backgroundColor = R.drawable.theocracy_background
                    "Literacy" -> backgroundColor = R.drawable.literacy_background
                    "Wonder of the World" -> backgroundColor = R.drawable.wonders_of_the_world_background
                    "Philosophy" -> backgroundColor = R.drawable.philosophy_background
                    "Monument" -> backgroundColor = R.drawable.monument_background
                    "Written Record Extra Credits" -> backgroundColor = R.drawable.written_record_background
                    "Monument Extra Credits" -> backgroundColor = R.drawable.monument_background
                    "Extra Credits WR & Monument" -> return gradient
                    //"Extra Credits WR & Monument" -> backgroundColor = R.drawable.extra_credits_background
                }
            }
            return ResourcesCompat.getDrawable(res, backgroundColor, null)
        }

        fun getTextColor(card: Card): Int {
            var textColor: Int = when (card.group1) {
                CardColor.YELLOW, CardColor.GREEN -> Color.BLACK
                else -> Color.WHITE
            }

            textColor = when (card.name) {
                "Engineering" -> Color.DKGRAY
                "Literacy" -> Color.LTGRAY
                "Mathematics" -> Color.LTGRAY
                "Monument" -> Color.DKGRAY
                "Mysticism" -> Color.DKGRAY
                "Philosophy" -> Color.DKGRAY
                "Theocracy" -> Color.DKGRAY
                "Wonder of the World" -> Color.WHITE
                "Written Record" -> Color.WHITE
                "Extra Credits WR & Monument" -> Color.DKGRAY
                "Written Record Extra Credits" -> Color.WHITE
                "Monument Extra Credits" -> Color.DKGRAY
                else -> textColor
            }
            return textColor
        }
    }
}