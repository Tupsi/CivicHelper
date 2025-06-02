package org.tesira.civic.db

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.preference.PreferenceManager
import org.tesira.civic.Calamity
import org.tesira.civic.Event
import org.tesira.civic.R
import java.util.Arrays
import java.util.Locale

class CivicViewModel(application: Application, savedStateHandle: SavedStateHandle?) :
    AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {
    private val mRepository: CivicRepository
    @JvmField
    val treasure: MutableLiveData<Int?> = MutableLiveData<Int?>(0)
    @JvmField
    val remaining: MutableLiveData<Int?> = MutableLiveData<Int?>(0)
    private val vp = MutableLiveData<Int?>(0)
    @JvmField
    var cardBonus: MutableLiveData<HashMap<CardColor, Int>>
    private val cities = MutableLiveData<Int?>(0)
    private val mApplication: Application
    private val timeVp = MutableLiveData<Int?>(0)
    @JvmField
    var librarySelected: Boolean
    @JvmField
    val allAdvancesNotBought: LiveData<MutableList<Card>>
    private val _currentSortingOrder = MutableLiveData<String>()
    private val _columns = MutableLiveData<Int?>()
    private val showAnatomyDialogEvent = MutableLiveData<Event<List<String>>>()
    private val _newGameStartedEvent = MutableLiveData<Event<Boolean?>>()
    val newGameStartedEvent: LiveData<Event<Boolean?>>
        get() = _newGameStartedEvent


    @JvmField
    val calamityBonusListLiveData: LiveData<List<Calamity>>
    private val specialAbilitiesRawLiveData: LiveData<List<String>>
    private val immunitiesRawLiveData: LiveData<List<String>>
    private val combinedSpecialsAndImmunitiesLiveData = MediatorLiveData<MutableList<String>>()
    private val _selectedTipIndex = MutableLiveData<Int?>()
    var selectedTipIndex: LiveData<Int?> = _selectedTipIndex
    private val _astVersion = MutableLiveData<String?>()
    @JvmField
    var astVersion: LiveData<String?> = _astVersion
    private val _civNumber = MutableLiveData<String?>()
    @JvmField
    var getCivNumber: LiveData<String?> = _civNumber
    private lateinit var tipsArray: Array<String>
    private val _showExtraCreditsDialogEvent = MutableLiveData<Event<Int?>?>()
    private val defaultPrefs: SharedPreferences
    val cardsVpLiveData: LiveData<Int>
    private val totalVp = MediatorLiveData<Int?>()
    private val buyableCardMap: MutableMap<String, Card> = HashMap<String, Card>()
    private val areBuyableCardsReady = MutableLiveData<Boolean?>(false)
    private lateinit var buyableCardsMapObserver: Observer<MutableList<Card>>

    fun getShowAnatomyDialogEvent(): LiveData<Event<List<String>>> {
        return showAnatomyDialogEvent
    }

    val showExtraCreditsDialogEvent: LiveData<Event<Int?>?>
        get() = _showExtraCreditsDialogEvent

    fun setAstVersion(version: String?) {
        _astVersion.setValue(version)
        if (version != null) {
            defaultPrefs.edit().putString(PREF_KEY_AST, version).apply()
        }
    }

    fun setCivNumber(civNumber: String?) {
        _civNumber.setValue(civNumber)
        if (civNumber != null) {
            defaultPrefs.edit().putString(PREF_KEY_CIVILIZATION, civNumber).apply()
        }
    }

    private fun setupBuyableCardsObserver() {
        buyableCardsMapObserver = Observer { cards: MutableList<Card> ->
            buyableCardMap.clear()
            if (cards != null) {
                for (card in cards) {
                    buyableCardMap.put(card.name, card)
                }
                areBuyableCardsReady.setValue(true)
                Log.d("CivicViewModel", "Buyable cards map updated. Size: " + buyableCardMap.size)
            } else {
                areBuyableCardsReady.setValue(false)
            }
        }
        allAdvancesNotBought.observeForever(buyableCardsMapObserver)
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
        totalVp.setValue(0)

        // Quelle 1: cardsVpFromDao
        totalVp.addSource<Int?>(
            this.cardsVpLiveData,
            Observer { cardsVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp = if (cardsVal != null) cardsVal else 0
                val currentCitiesVp = (if (cities.getValue() != null) cities.getValue() else 0)!!
                val currentTimeVp = (if (timeVp.getValue() != null) timeVp.getValue() else 0)!!
                totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp)
            })

        // Quelle 2: cities LiveData
        totalVp.addSource<Int?>(
            cities,
            Observer { cityVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp: Int =
                    (if (cardsVpLiveData.getValue() != null) cardsVpLiveData.getValue() else 0)!!
                val currentCitiesVp = if (cityVal != null) cityVal else 0
                val currentTimeVp = (if (timeVp.getValue() != null) timeVp.getValue() else 0)!!
                totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp)
            })

        // Quelle 3: timeVp LiveData
        totalVp.addSource<Int?>(
            timeVp,
            Observer { timeVal: Int? ->  // Parameter umbenannt zur Klarheit
                val currentCardsVp: Int =
                    (if (cardsVpLiveData.getValue() != null) cardsVpLiveData.getValue() else 0)!!
                val currentCitiesVp = (if (cities.getValue() != null) cities.getValue() else 0)!!
                val currentTimeVp = if (timeVal != null) timeVal else 0
                totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp)
            })
    }

    fun getTotalVp(): LiveData<Int?> {
        return totalVp
    }

    val columnsLiveData: LiveData<Int?>
        get() = _columns
    val columns: Int
        get() = _columns.getValue()!!

    fun loadData() {
        val blue: Int
        val green: Int
        val orange: Int
        val red: Int
        val yellow: Int
        blue = defaultPrefs.getInt(CardColor.BLUE.colorName, 0)
        green = defaultPrefs.getInt(CardColor.GREEN.colorName, 0)
        orange = defaultPrefs.getInt(CardColor.ORANGE.colorName, 0)
        red = defaultPrefs.getInt(CardColor.RED.colorName, 0)
        yellow = defaultPrefs.getInt(CardColor.YELLOW.colorName, 0)
        if (cardBonus.getValue() != null) {
            cardBonus.getValue()!!.put(CardColor.BLUE, blue)
            cardBonus.getValue()!!.put(CardColor.GREEN, green)
            cardBonus.getValue()!!.put(CardColor.ORANGE, orange)
            cardBonus.getValue()!!.put(CardColor.RED, red)
            cardBonus.getValue()!!.put(CardColor.YELLOW, yellow)
        }
        userPreferenceForHeartCards.setValue(defaultPrefs.getString(PREF_KEY_HEART, "custom"))
        setCities(defaultPrefs.getInt(PREF_KEY_CITIES, 0))
        setTimeVp(defaultPrefs.getInt(PREF_KEY_TIME, 0))
        _columns.setValue(defaultPrefs.getString(PREF_KEY_COLUMNS, "1")!!.toInt())
        _currentSortingOrder.value = (defaultPrefs.getString(PREF_KEY_SORT, "name"))!!
        _astVersion.setValue(defaultPrefs.getString(PREF_KEY_AST, "basic"))
        _civNumber.setValue(defaultPrefs.getString(PREF_KEY_CIVILIZATION, "not set"))

//        if (tipsArray == null) {
            tipsArray = mApplication.getResources().getStringArray(R.array.tips)
//        }
    }

    fun getCities(): Int {
        return (if (cities.getValue() != null) cities.getValue() else 0)!!
    }

    val citiesLive: LiveData<Int?>
        get() = cities

    fun setCities(value: Int) {
        cities.setValue(value)
    }

    fun requestPriceRecalculation() {
        mRepository.recalculateCurrentPricesAsync(cardBonus)
    }

    private val userPreferenceForHeartCards = MutableLiveData<String?>()
    fun getTimeVp(): Int {
        return timeVp.getValue()!!
    }

    val timeVpLive: LiveData<Int?>
        get() = timeVp

    fun setTimeVp(value: Int) {
        timeVp.setValue(value)
    }

    fun insertPurchase(purchase: String) {
        mRepository.insertPurchase(purchase)
    }

    fun setSortingOrder(order: String) {
        if (order != _currentSortingOrder.getValue()) {
            _currentSortingOrder.setValue(order)
        }
    }

    val currentSortingOrder: LiveData<String>
        get() = _currentSortingOrder

    val inventoryAsCardLiveData: LiveData<List<Card>>
        get() = mRepository.inventoryAsCardLiveData

    fun setTreasure(treasure: Int) {
        this.treasure.setValue(treasure)
    }

    fun setRemaining(treasure: Int) {
        this.remaining.setValue(treasure)
    }

    private val newGameResetCompletedEvent = MutableLiveData<Event<Boolean?>?>()
    fun getNewGameResetCompletedEvent(): LiveData<Event<Boolean?>?> {
        return newGameResetCompletedEvent
    }

    private val _navigateToDashboardEvent = MutableLiveData<Event<Boolean?>?>()
    val navigateToDashboardEvent: LiveData<Event<Boolean?>?>
        get() = _navigateToDashboardEvent

    /**
     * Performs the core logic for starting a new game.
     * This includes resetting persistent data and ViewModel state.
     */
    fun startNewGameProcess() {
        mRepository.deleteInventory()
        mRepository.resetCurrentPrice()
        mRepository.resetDB(mApplication.getApplicationContext())
        treasure.setValue(0)
        remaining.setValue(0)
        cities.setValue(0)
        timeVp.setValue(0)
        vp.setValue(0)
        cardBonus.setValue(HashMap<CardColor, Int>())

        defaultPrefs.edit().putInt(CardColor.BLUE.colorName, 0).apply()
        defaultPrefs.edit().putInt(CardColor.GREEN.colorName, 0).apply()
        defaultPrefs.edit().putInt(CardColor.ORANGE.colorName, 0).apply()
        defaultPrefs.edit().putInt(CardColor.RED.colorName, 0).apply()
        defaultPrefs.edit().putInt(CardColor.YELLOW.colorName, 0).apply()

        defaultPrefs.edit().putInt(PREF_KEY_CITIES, 0).apply()
        defaultPrefs.edit().putInt(PREF_KEY_TIME, 0).apply()
        defaultPrefs.edit().putInt(PREF_KEY_TREASURE, 0).apply()
        defaultPrefs.edit().putString(PREF_KEY_HEART, "custom").apply()
        defaultPrefs.edit().remove(PREF_KEY_CIVILIZATION).apply()

        _newGameStartedEvent.value= Event(true)
        newGameResetCompletedEvent.setValue(Event<Boolean?>(true))
    }

    /**
     * Updates the Bonus for all colors by adding the respective fields to cardBonus HashSet.
     * @param blue additional bonus for blue cards.
     * @param green additional bonus for green cards.
     * @param orange additional bonus for orange cards.
     * @param red additional bonus for red cards.
     * @param yellow additional bonus for yellow cards.
     */
    fun updateBonus(blue: Int, green: Int, orange: Int, red: Int, yellow: Int) {
        cardBonus.getValue()!!
            .compute(CardColor.BLUE) { k: CardColor?, v: Int? -> if (v == null) blue else v + blue }
        cardBonus.getValue()!!
            .compute(CardColor.GREEN) { k: CardColor?, v: Int? -> if (v == null) green else v + green }
        cardBonus.getValue()!!
            .compute(CardColor.ORANGE) { k: CardColor?, v: Int? -> if (v == null) orange else v + orange }
        cardBonus.getValue()!!
            .compute(CardColor.RED) { k: CardColor?, v: Int? -> if (v == null) red else v + red }
        cardBonus.getValue()!!
            .compute(CardColor.YELLOW) { k: CardColor?, v: Int? -> if (v == null) yellow else v + yellow }
    }

    private fun getBuyableAdvanceByNameFromMap(name: String?): Card? {
        if (java.lang.Boolean.TRUE == areBuyableCardsReady.getValue()) {
            return buyableCardMap.get(name)
        }
        Log.w(
            "CivicViewModel",
            "Attempted to get card '" + name + "' from map, but cards not ready."
        )
        return null
    }

    /**
     * Calculates the sum of all currently selected advances during the buy process and
     * updates remaining treasure.
     * @param selection Currently selected cards from the View.  <-- PARAMETER TYP ANGEPASST
     */
    fun calculateTotal(selection: Iterable<String>) { // Parameter-Typ von Selection<String> zu Iterable<String>
        if (java.lang.Boolean.TRUE != areBuyableCardsReady.getValue()) {
            Log.w("CivicViewModel", "calculateTotal called, but buyable cards are not ready.")
            val currentTreasure: Int =
                (if (treasure.getValue() != null) treasure.getValue() else 0)!!
            this.remaining.setValue(currentTreasure) // Setze remaining auf Treasure, wenn Karten nicht bereit
            return
        }

        var newTotalCost = 0 // Umbenannt von newTotal zu newTotalCost für Klarheit
        if (selection != null) {
            val iterator: Iterator<String> = selection.iterator()
            if (iterator.hasNext()) { // Nur iterieren, wenn das Iterable nicht leer ist
                for (name in selection) {
                    val adv = getBuyableAdvanceByNameFromMap(name)
                    if (adv != null) {
                        newTotalCost += adv.currentPrice
                    } else {
                        Log.e(
                            "CivicViewModel",
                            "Card with name '" + name + "' not found in buyableCardMap during calculateTotal."
                        )
                    }
                }
            }
        }
        val currentTreasure: Int = (if (treasure.getValue() != null) treasure.getValue() else 0)!!
        this.remaining.setValue(currentTreasure - newTotalCost) // remaining wird hier aktualisiert
    }

    /**
     * Adds the bonuses of a bought card to the cardBonus HashSet.
     * @param name The name of the bought card.
     */
    fun addBonus(name: String?) {
        val adv = getBuyableAdvanceByNameFromMap(name)
        Log.d("CivicViewModel", "Adding bonus for card: " + name)
        updateBonus(
            adv!!.creditsBlue,
            adv.creditsGreen,
            adv.creditsOrange,
            adv.creditsRed,
            adv.creditsYellow
        )
    }

    val blue: Int
        get() = cardBonus.getValue()!!.getOrDefault(CardColor.BLUE, 0)!!
    val green: Int
        get() = cardBonus.getValue()!!.getOrDefault(CardColor.GREEN, 0)!!
    val orange: Int
        get() = cardBonus.getValue()!!.getOrDefault(CardColor.ORANGE, 0)!!
    val red: Int
        get() = cardBonus.getValue()!!.getOrDefault(CardColor.RED, 0)!!
    val yellow: Int
        get() = cardBonus.getValue()!!.getOrDefault(CardColor.YELLOW, 0)!!

    /**
     * Orchestrates the purchase process. Delegates the database operations to the Repository
     * and handles the UI responses (dialogs) based on the results.
     * This method is called from the Fragment.
     * @param selectedCardNames The names of the cards selected for purchase.
     */
    fun processPurchases(selectedCardNames: List<String>) {
        Log.d(
            "CivicViewModel",
            "processPurchases() called from Fragment with " + selectedCardNames.size + " cards."
        )
        // Rufe die asynchrone Methode im Repository auf
        mRepository.processPurchasesAndRecalculatePricesAsync(
            selectedCardNames, cardBonus,
            object : PurchaseCompletionCallback {
                override fun onPurchaseCompleted(
                    totalExtraCredits: Int,
                    anatomyCardsToChoose: List<String>
                ) {
                    Log.d(
                        "CivicViewModel",
                        "PurchaseCompletionCallback: onPurchaseCompleted. Extra Credits: " + totalExtraCredits + ", Anatomy Cards: " + anatomyCardsToChoose.size
                    )
                    // Diese Methode wird im Hintergrund-Thread aufgerufen.
                    // Aktualisiere LiveData im ViewModel auf dem Haupt-Thread mit postValue
                    if (!anatomyCardsToChoose.isEmpty()) {
                        showAnatomyDialogEvent.postValue(
                            Event<List<String>>(anatomyCardsToChoose)
                        )
                    }
                    if (totalExtraCredits > 0) {
                        _showExtraCreditsDialogEvent.postValue(Event<Int?>(totalExtraCredits))
                    }

                    // Optional: LiveData Event auslösen, um Navigation zu signalisieren, falls keine Dialoge nötig sind
                    if (anatomyCardsToChoose.isEmpty() && totalExtraCredits == 0) {
                        _navigateToDashboardEvent.postValue(Event<Boolean?>(true))
                    }
                }

                override fun onPurchaseFailed(errorMessage: String) {
                    Log.e(
                        "CivicViewModel",
                        "PurchaseCompletionCallback: onPurchaseFailed. Error: " + errorMessage
                    )
                    // Handle error, maybe show a Toast via another LiveData event
                    // _showErrorToastEvent.postValue(new Event<>(errorMessage));
                }
            })
    }

    fun saveBonus() {
        Log.d("CivicViewModel", "Saving bonus to SharedPreferences.")
        // HashMap Save
        for (entry in this.cardBonus.getValue()!!.entries) {
            defaultPrefs.edit().putInt(entry.key!!.colorName, entry.value!!).apply()
        }
    }

    fun saveData() {
        defaultPrefs.edit().putInt(PREF_KEY_CITIES, getCities()).apply()
        defaultPrefs.edit().putInt(PREF_KEY_TIME, getTimeVp()).apply()
    }

    /**
     * Triggers the event to show the Extra Credits Dialog.
     * The observing Fragment will react to this event.
     *
     * @param credits The number of extra credits to offer.
     */
    fun triggerExtraCreditsDialog(credits: Int) {
        _showExtraCreditsDialogEvent.postValue(Event<Int?>(credits))
    }

    /**
     * Triggers the event to show the Anatomy Dialog.
     * The observing Fragment will react to this event.
     *
     * @param greenCards The list of green cards to choose from.
     */
    fun triggerAnatomyDialog(greenCards: MutableList<String>) {
        // Postet einen neuen Event mit der Liste der grünen Karten
        showAnatomyDialogEvent.postValue(Event(greenCards))
    }

    override fun onCleared() {
        super.onCleared()
        if (buyableCardsMapObserver != null) {
            allAdvancesNotBought.removeObserver(buyableCardsMapObserver)
        }
        defaultPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    // Schnittstelle für die Callback vom Repository
    interface PurchaseCompletionCallback {
        fun onPurchaseCompleted(totalExtraCredits: Int, anatomyCardsToChoose: List<String>)
        fun onPurchaseFailed(errorMessage: String) // Optional: Fehlerbehandlung
    }

    private val _selectedCardKeysForState =
        MutableLiveData<MutableSet<String?>?>(mutableSetOf<String?>())

    init {
        cardBonus = MutableLiveData<HashMap<CardColor, Int>>(HashMap<CardColor, Int>())
        mRepository = CivicRepository(application)
        calamityBonusListLiveData = mRepository.calamityBonusLiveData
        specialAbilitiesRawLiveData = mRepository.specialAbilitiesLiveData
        immunitiesRawLiveData = mRepository.immunitiesLiveData
        mApplication = application
        librarySelected = false
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(mApplication)
        defaultPrefs.registerOnSharedPreferenceChangeListener(this)
        cardsVpLiveData = mRepository.cardsVp
        setupTotalVpMediator()
        loadData()
        allAdvancesNotBought = _currentSortingOrder
            .switchMap { order: String ->
                mRepository.getAllAdvancesNotBoughtLiveData(order).map { it.toMutableList() }
            }

        setupCombinedSpecialsLiveData()
        setupBuyableCardsObserver()
    }

    val selectedCardKeysForState: LiveData<MutableSet<String?>?>
        get() = _selectedCardKeysForState

    // NEU: Methode, um die Auswahl aus dem Fragment im ViewModel zu aktualisieren
    fun updateSelectionState(currentSelection: MutableSet<String?>?) {
        if (currentSelection == null) {
            _selectedCardKeysForState.setValue(mutableSetOf<String?>())
        } else {
            _selectedCardKeysForState.setValue(HashSet<String?>(currentSelection))
        }
    }

    // NEU oder ANPASSEN: Methode, um die gespeicherte Auswahl zu löschen
    fun clearCurrentSelectionState() {
        _selectedCardKeysForState.setValue(mutableSetOf<String?>())
        // Die calculateTotal-Logik sollte idealerweise durch den SelectionTracker-Observer
        // im Fragment ausgelöst werden, wenn der Tracker geleert wird.
        // Wenn du hier explizit totalPrice etc. zurücksetzen willst:
        // calculateTotal(Collections.emptySet());
    }

    fun setSelectedTipIndex(index: Int) {
        if (index >= 0 && (tipsArray == null || index < tipsArray!!.size)) {
            _selectedTipIndex.setValue(index)
        }
    }

    fun getTipForIndex(index: Int): String? {
        if (tipsArray != null && index >= 0 && index < tipsArray!!.size) {
            return tipsArray!![index]
        }
        Log.w(
            "CivicViewModel",
            "Attempted to get tip for invalid index: " + index + " or tipsArray not loaded."
        )
        return "" // Fallback
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (PREF_KEY_CIVILIZATION == key) {
            val civicNumber = sharedPreferences.getString(key, "1")!!.toInt()
            val newCivNumber: String = sharedPreferences.getString(key, "not set")!!
            if (_civNumber.getValue() == null || _civNumber.getValue() != newCivNumber) {
                _civNumber.setValue(newCivNumber)
            }
            val newIndex = civicNumber - 1
            if (_selectedTipIndex.getValue() == null || _selectedTipIndex.getValue() != newIndex) {
                _selectedTipIndex.setValue(newIndex)
            }
        } else if (PREF_KEY_SORT == key) {
            val newSortingOrder: String = sharedPreferences.getString(key, "name")!!
            if (_currentSortingOrder.getValue() == null || _currentSortingOrder.getValue() != newSortingOrder) {
                _currentSortingOrder.setValue(newSortingOrder)
            }
        } else if (PREF_KEY_HEART == key) {
            val newHeartSelection: String = sharedPreferences.getString(key, "custom")!!
            if (userPreferenceForHeartCards.getValue() == null || userPreferenceForHeartCards.getValue() != newHeartSelection) {
                userPreferenceForHeartCards.setValue(newHeartSelection)
                processHeartPreferenceChange(newHeartSelection)
            }
        } else if (PREF_KEY_COLUMNS == key) {
            val newColumns = sharedPreferences.getString(key, "1")!!.toInt()
            if (_columns.getValue() == null || _columns.getValue() != newColumns) {
                _columns.setValue(newColumns)
            }
        } else if (PREF_KEY_AST == key) {
            val newAstVersion: String = sharedPreferences.getString(key, "name")!!
            if (_astVersion.getValue() == null || _astVersion.getValue() != newAstVersion) {
                _astVersion.setValue(newAstVersion)
            }
        }
    }

    fun updateUserHeartPreference(selectionName: String) {
        defaultPrefs.edit().putString(PREF_KEY_HEART, selectionName).apply()
        userPreferenceForHeartCards.setValue(selectionName)
        processHeartPreferenceChange(selectionName)
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
                        // Optional: Benachrichtige die UI, dass sich die Daten geändert haben,
                        // falls die LiveData-Beobachtung der Kartenliste nicht ausreicht.
                        // Zum Beispiel, wenn du eine spezifische Aktion nach dem Update auslösen willst.
                        Log.d(
                            "CivicViewModel",
                            "Cards updated with new heart status for: " + selectionName
                        )
                    })
            } else {
                Log.d(
                    "CivicViewModel",
                    "No specific cards to mark for heart selection: " + selectionName + " or selection is 'custom'."
                )
            }
        })
    }

    /**
     * Holt die Liste der Kartennamen basierend auf der "heart"-Auswahl.
     * Nutzt deine bestehende Logik aus getChooserCards().
     */
    private fun getCardNamesForHeartSelection(selectionName: String): List<String> {
        when (selectionName.lowercase(Locale.getDefault())) {
            "treasury" -> return Arrays.asList<String>(*TREASURY)
            "commodities" -> return Arrays.asList<String>(*COMMODITY_CARDS)
            "cheaper" -> return Arrays.asList<String>(*CHEAPER_CIVILIZATION_CARDS)
            "bend" -> return Arrays.asList<String>(*TO_BEND_THE_RULES)
            "more" -> return Arrays.asList<String>(*MORE_TOKEN_ON_THE_MAP)
            "mobility" -> return Arrays.asList<String>(*TOKEN_MOBILITY)
            "cities" -> return Arrays.asList<String>(*CITIES)
            "sea" -> return Arrays.asList<String>(*SEA_POWER)
            "aggression" -> return Arrays.asList<String>(*AGGRESSION)
            "defense" -> return Arrays.asList<String>(*DEFENSE)
            "custom" -> return ArrayList<String>()
            else -> return ArrayList<String>()
        }
    }

    fun calculateColumnCount(context: Context): Int {
        // 1. Hole die aktuelle Gerätekonfiguration
        val configuration = context.getResources().getConfiguration()
        val screenWidthDp = configuration.screenWidthDp // Aktuelle Bildschirmbreite in dp
        val orientation = configuration.orientation // Aktuelle Orientierung

        // 2. Deine Logik zur Bestimmung der Spaltenanzahl
        // Beispiel: Wenn im Querformat und die Benutzereinstellung 1 ist, setze auf 2 Spalten.
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (this.columns <= 1) {
                return 2 // Immer 2 Spalten im Querformat, wenn User 1 wollte
            } else {
                return this.columns
            }
        } else { // Portrait-Modus
            // Im Portrait-Modus, verwende die Benutzereinstellung
            return this.columns
        }

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
        private const val PREF_KEY_HEART = "heart"
        private const val PREF_KEY_COLUMNS = "columns"
        private const val PREF_KEY_AST = "ast"
        @JvmStatic
        fun getItemBackgroundColor(card: Card, res: Resources): Drawable? {
            var backgroundColor = 0
            if (card.group2 == null) {
                when (card.group1) {
                    CardColor.ORANGE -> backgroundColor = R.color.crafts
                    CardColor.YELLOW -> backgroundColor = R.color.religion
                    CardColor.RED -> backgroundColor = R.color.civic
                    CardColor.GREEN -> backgroundColor = R.color.science
                    CardColor.BLUE -> backgroundColor = R.color.arts
                    else -> backgroundColor = R.color.purple_700
                }
            } else {
                when (card.name) {
                    "Engineering" -> backgroundColor = R.drawable.engineering_background
                    "Mathematics" -> backgroundColor = R.drawable.mathematics_background
                    "Mysticism" -> backgroundColor = R.drawable.mysticism_background
                    "Written Record" -> backgroundColor = R.drawable.written_record_background
                    "Theocracy" -> backgroundColor = R.drawable.theocracy_background
                    "Literacy" -> backgroundColor = R.drawable.literacy_background
                    "Wonder of the World" -> backgroundColor =
                        R.drawable.wonders_of_the_world_background

                    "Philosophy" -> backgroundColor = R.drawable.philosophy_background
                    "Monument" -> backgroundColor = R.drawable.monument_background
                }
            }
            return ResourcesCompat.getDrawable(res, backgroundColor, null)
        }
    }
}