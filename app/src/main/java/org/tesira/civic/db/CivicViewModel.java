package org.tesira.civic.db;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.preference.PreferenceManager;
import androidx.lifecycle.Observer;

import org.tesira.civic.Calamity;
import org.tesira.civic.R;
import org.tesira.civic.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class CivicViewModel extends AndroidViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final CivicRepository mRepository;
    private int screenWidthDp, smallestScreenWidthDp;
    private final MutableLiveData<Integer> treasure  = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> remaining = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> vp = new MutableLiveData<>(0);
    public MutableLiveData<HashMap<CardColor, Integer>> cardBonus;
    private final MutableLiveData<Integer> cities = new MutableLiveData<>(0);
    private final Application mApplication;
    private final MutableLiveData<Integer> timeVp = new MutableLiveData<>(0);
//    public String heart;

    public boolean librarySelected;
    public final static String[] TREASURY = {"Monarchy", "Coinage", "Trade Routes",
            "Politics", "Mining"};
    public final static String[] COMMODITY_CARDS = {"Rhetoric", "Cartography", "Roadbuilding",
            "Mining", "Trade Empire", "Provincial Empire", "Wonder of the World"};
    public final static String[] CHEAPER_CIVILIZATION_CARDS = {"Empiricism","Written Record",
            "Literacy","Monument", "Library", "Mining","Mathematics","Anatomy"};
    public final static String[] TO_BEND_THE_RULES = {"Coinage", "Universal Doctrine",
            "Democracy","Wonder of the World"};
    public final static String[] MORE_TOKEN_ON_THE_MAP = {"Coinage", "Agriculture",
            "Universal Doctrine", "Democracy","Public Works","Politics","Monotheism", "Diaspora"};
    public final static String[] TOKEN_MOBILITY = {"Urbanism", "Cloth Making", "Astronavigation",
            "Agriculture", "Naval Warfare", "Military", "Roadbuilding", "Public Works",
            "Advanced Military", "Diaspora"};
    public final static String[] CITIES = {"Urbanism", "Architecture", "Fundamentalism",
            "Engineering", "Universal Doctrine", "Democracy", "Politics", "Monotheism", "Diaspora"};
    public final static String[] SEA_POWER = {"Cloth Making", "Masonry", "Astronavigation",
            "Naval Warfare", "Engineering", "Calendar"};
    public final static String[] AGGRESSION = {"Metalworking", "Fundamentalism", "Engineering",
            "Naval Warfare", "Diplomacy", "Military", "Roadbuilding", "Politics",
            "Advanced Military", "Monotheism", "Diaspora", "Cultural Ascendancy"};
    public final static String[] DEFENSE = {"Metalworking", "Agriculture", "Fundamentalism",
            "Engineering", "Diplomacy", "Naval Warfare", "Military", "Roadbuilding", "Philosophy",
            "Public Works", "Politics", "Monotheism", "Theology", "Provincial Empire", "Diaspora",
            "Cultural Ascendancy"};
    public final static String[] TIME_TABLE = {"8000 BC", "7000 BC", "6000 BC", "4300 BC", "5000 BC",
            "3300 BC", "2700 BC", "2000 BC", "1800 BC", "1700 BC", "1500 BC", "1400 BC", "1300 BC",
            "1200 BC", "800 BC", "0", "400 AD"};

    private final MutableLiveData<String> currentSortingOrder = new MutableLiveData<>();
    private final LiveData<List<Card>> allAdvancesNotBought;

    // LiveData für Dialog-Events
    private final MutableLiveData<Event<List<String>>> showAnatomyDialogEvent = new MutableLiveData<>();
    private int mColumnCount;
    private final MutableLiveData<Event<Boolean>> newGameStartedEvent = new MutableLiveData<>();
    private final LiveData<List<Calamity>> calamityBonusListLiveData;

    private final LiveData<List<String>> specialAbilitiesRawLiveData;
    private final LiveData<List<String>> immunitiesRawLiveData;
    private final MediatorLiveData<List<String>> combinedSpecialsAndImmunitiesLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Integer> _selectedTipIndex = new MutableLiveData<>();
    public LiveData<Integer> selectedTipIndex = _selectedTipIndex;

    private String[] tipsArray;

    public LiveData<Event<List<String>>> getShowAnatomyDialogEvent() {
        return showAnatomyDialogEvent;
    }
    private final MutableLiveData<Event<Integer>> _showExtraCreditsDialogEvent = new MutableLiveData<>();
    public LiveData<Event<Integer>> getShowExtraCreditsDialogEvent() {
        return _showExtraCreditsDialogEvent;
    }
    private SharedPreferences defaultPrefs;
    private static final String PREF_KEY_CIVILIZATION = "civilization";
    private static final String PREF_KEY_SORT = "sort";
    private static final String PREF_KEY_CITIES = "cities";
    private static final String PREF_KEY_TIME = "time";
    private static final String PREF_KEY_TREASURE = "treasure";
    private static final String PREF_KEY_HEART = "heart";
    private final LiveData<Integer> cardsVpFromDao;
    private final MediatorLiveData<Integer> totalVp = new MediatorLiveData<>();
    private final Map<String, Card> buyableCardMap = new HashMap<>();
    private final MutableLiveData<Boolean> areBuyableCardsReady = new MutableLiveData<>(false);
    private Observer<List<Card>> buyableCardsMapObserver;


    public CivicViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) throws ExecutionException, InterruptedException {
        super(application);
        cardBonus = new MutableLiveData<>(new HashMap<>());
        mRepository = new CivicRepository(application);
        calamityBonusListLiveData = mRepository.getCalamityBonusLiveData();
        specialAbilitiesRawLiveData = mRepository.getSpecialAbilitiesLiveData();
        immunitiesRawLiveData = mRepository.getImmunitiesLiveData();
        mApplication = application;
        librarySelected = false;
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
        defaultPrefs.registerOnSharedPreferenceChangeListener(this);
        cardsVpFromDao = mRepository.getCardsVp();
        setupTotalVpMediator();
        loadData();
        allAdvancesNotBought = Transformations.switchMap(currentSortingOrder,
                order -> mRepository.getAllAdvancesNotBoughtLiveData(order));
        setupCombinedSpecialsLiveData();
        setupBuyableCardsObserver();
        loadTipsInitialData();
    }
    private void setupBuyableCardsObserver() {
        buyableCardsMapObserver = cards -> {
            buyableCardMap.clear();
            if (cards != null) {
                for (Card card : cards) {
                    buyableCardMap.put(card.getName(), card);
                }
                areBuyableCardsReady.setValue(true);
                Log.d("CivicViewModel", "Buyable cards map updated. Size: " + buyableCardMap.size());
            } else {
                areBuyableCardsReady.setValue(false);
            }
        };
        allAdvancesNotBought.observeForever(buyableCardsMapObserver);
    }
    private void setupCombinedSpecialsLiveData() {
        // Der Observer für Änderungen in specialAbilitiesRawLiveData
        Observer<List<String>> abilitiesObserver = abilities -> {
            List<String> currentImmunities = immunitiesRawLiveData.getValue();
            combineAndSetData(abilities, currentImmunities);
        };

        // Der Observer für Änderungen in immunitiesRawLiveData
        Observer<List<String>> immunitiesObserver = immunities -> {
            List<String> currentAbilities = specialAbilitiesRawLiveData.getValue();
            combineAndSetData(currentAbilities, immunities);
        };

        combinedSpecialsAndImmunitiesLiveData.addSource(specialAbilitiesRawLiveData, abilitiesObserver);
        combinedSpecialsAndImmunitiesLiveData.addSource(immunitiesRawLiveData, immunitiesObserver);
    }

    private void combineAndSetData(List<String> abilities, List<String> immunities) {
        List<String> combinedList = new ArrayList<>();
        combinedList.add("___Special Abilities");
        if (abilities != null) {
            combinedList.addAll(abilities);
        }
        combinedList.add("___Immunities");
        if (immunities != null) {
            combinedList.addAll(immunities);
        }

        List<String> distinctList = combinedList.stream().distinct().collect(Collectors.toList());
        combinedSpecialsAndImmunitiesLiveData.setValue(distinctList);
    }

    public LiveData<List<String>> getCombinedSpecialsAndImmunitiesLiveData() {
        return combinedSpecialsAndImmunitiesLiveData;
    }
    private void setupTotalVpMediator() {
        totalVp.setValue(0);

        // Quelle 1: cardsVpFromDao
        totalVp.addSource(cardsVpFromDao, cardsVal -> { // Parameter umbenannt zur Klarheit
            Integer currentCardsVp = (cardsVal != null) ? cardsVal : 0;
            Integer currentCitiesVp = (cities.getValue() != null) ? cities.getValue() : 0;
            Integer currentTimeVp = (timeVp.getValue() != null) ? timeVp.getValue() : 0;
            totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp);
        });

        // Quelle 2: cities LiveData
        totalVp.addSource(cities, cityVal -> { // Parameter umbenannt zur Klarheit
            Integer currentCardsVp = (cardsVpFromDao.getValue() != null) ? cardsVpFromDao.getValue() : 0;
            Integer currentCitiesVp = (cityVal != null) ? cityVal : 0;
            Integer currentTimeVp = (timeVp.getValue() != null) ? timeVp.getValue() : 0;
            totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp);
        });

        // Quelle 3: timeVp LiveData
        totalVp.addSource(timeVp, timeVal -> { // Parameter umbenannt zur Klarheit
            Integer currentCardsVp = (cardsVpFromDao.getValue() != null) ? cardsVpFromDao.getValue() : 0;
            Integer currentCitiesVp = (cities.getValue() != null) ? cities.getValue() : 0;
            Integer currentTimeVp = (timeVal != null) ? timeVal : 0;
            totalVp.setValue(currentCardsVp + currentCitiesVp + currentTimeVp);
        });
    }
    public LiveData<Integer> getTotalVp() {
        return totalVp;
    }

    public int getCardsVp() {
        return cardsVpFromDao.getValue() != null ? cardsVpFromDao.getValue() : 0;
    }

    public LiveData<Integer> getCardsVpLiveData() {
        return cardsVpFromDao;
    }
    public void loadData() {
        int blue, green, orange, red, yellow;
        blue = defaultPrefs.getInt(CardColor.BLUE.getName(), 0);
        green = defaultPrefs.getInt(CardColor.GREEN.getName(), 0);
        orange = defaultPrefs.getInt(CardColor.ORANGE.getName(), 0);
        red = defaultPrefs.getInt(CardColor.RED.getName(), 0);
        yellow = defaultPrefs.getInt(CardColor.YELLOW.getName(), 0);
        if (cardBonus.getValue() != null) {
            cardBonus.getValue().put(CardColor.BLUE, blue);
            cardBonus.getValue().put(CardColor.GREEN, green);
            cardBonus.getValue().put(CardColor.ORANGE, orange);
            cardBonus.getValue().put(CardColor.RED, red);
            cardBonus.getValue().put(CardColor.YELLOW, yellow);
        }
        userPreferenceForHeartCards.setValue(defaultPrefs.getString(PREF_KEY_HEART, "custom"));
        setCities(defaultPrefs.getInt(PREF_KEY_CITIES, 0));
        setTimeVp(defaultPrefs.getInt(PREF_KEY_TIME,0));
        mColumnCount = Integer.parseInt(defaultPrefs.getString("columns", "1"));
        currentSortingOrder.setValue(defaultPrefs.getString("sort", "name"));
    }
    public int getCities() {
        return cities.getValue() != null ? cities.getValue() : 0;
    }
    public LiveData<Integer> getCitiesLive() {
        return cities;
    }

    public void setCities(int value) {
        cities.setValue(value);
    }
    public void requestPriceRecalculation() {
        mRepository.recalculateCurrentPricesAsync(cardBonus.getValue());
    }
    public int getScreenWidthDp() {
        return screenWidthDp;
    }
    public void setScreenWidthDp(int screenWidthDp) {
        this.screenWidthDp = screenWidthDp;
    }
    public int getSmallestScreenWidthDp(){return smallestScreenWidthDp;}
    public void setSmallestScreenWidthDp(int smallestScreenWidthDp) {
        this.smallestScreenWidthDp = smallestScreenWidthDp;
    };

    private final MutableLiveData<String> userPreferenceForHeartCards = new MutableLiveData<>();

    public LiveData<String> getUserPreferenceForHeartCards() {
        return userPreferenceForHeartCards;
    }
    public int getTimeVp() {
        return timeVp.getValue();
    }
    public LiveData<Integer> getTimeVpLive() {
        return timeVp;
    }
    public void setTimeVp(int value) {
        timeVp.setValue(value);
    }
    public void insertPurchase(String purchase) {mRepository.insertPurchase(purchase);}
    public void setSortingOrder(String order) {
        if (!order.equals(currentSortingOrder.getValue())) {
            currentSortingOrder.setValue(order);
        }
    }
    public LiveData<String> getCurrentSortingOrder() {
        return currentSortingOrder;
    }

    public LiveData<List<Card>> getAllAdvancesNotBought() {
        return allAdvancesNotBought;
    }
    public LiveData<List<Calamity>> getCalamityBonusListLiveData() {
        return calamityBonusListLiveData;
    }
    public LiveData<Card> getAdvanceByNameToCardLiveData(String name) {return mRepository.getAdvanceByNameToCardLiveData(name);}
    public LiveData<List<Card>> getPurchasesAsCardLiveData() {return mRepository.getPurchasesAsCardLiveData();}
    public List<Calamity> getCalamityBonus() {return mRepository.getCalamityBonus();}
    public List<String> getSpecialAbilities() {return mRepository.getSpecialAbilities();}
    public List<String> getImmunities() {return mRepository.getImmunities();}
    public MutableLiveData<Integer> getTreasure() {
        return treasure;
    }
    public void setTreasure(int treasure) {
        this.treasure.setValue(treasure);
    }
    public MutableLiveData<Integer> getRemaining() {
        return remaining;
    }
    public void setRemaining(int treasure) {
        this.remaining.setValue(treasure);
    }
    public MutableLiveData<HashMap<CardColor, Integer>> getCardBonus() {
        return cardBonus;
    }


    public LiveData<Event<Boolean>> getNewGameStartedEvent() {
        return newGameStartedEvent;
    }

    private final MutableLiveData<Event<Boolean>> newGameResetCompletedEvent = new MutableLiveData<>();
    public final LiveData<Event<Boolean>> getNewGameResetCompletedEvent() {
        return newGameResetCompletedEvent;
    }
    private final MutableLiveData<Event<Boolean>> _navigateToDashboardEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> getNavigateToDashboardEvent() {
        return _navigateToDashboardEvent;
    }

    /**
     * Signals that a new game process should be initiated by the ViewModel.
     * This is called from the UI.
     */
    public void requestNewGame() {
        startNewGameProcess();
    }

    /**
     * Performs the core logic for starting a new game.
     * This includes resetting persistent data and ViewModel state.
     */
    public void startNewGameProcess() {
        mRepository.deletePurchases();
        mRepository.resetCurrentPrice();
        mRepository.resetDB();
        treasure.setValue(0);
        remaining.setValue(0);
        cities.setValue(0);
        timeVp.setValue(0);
        vp.setValue(0);
        cardBonus.setValue(new HashMap<>());

        defaultPrefs.edit().putInt(CardColor.BLUE.getName(), 0).apply();
        defaultPrefs.edit().putInt(CardColor.GREEN.getName(), 0).apply();
        defaultPrefs.edit().putInt(CardColor.ORANGE.getName(), 0).apply();
        defaultPrefs.edit().putInt(CardColor.RED.getName(), 0).apply();
        defaultPrefs.edit().putInt(CardColor.YELLOW.getName(), 0).apply();

        defaultPrefs.edit().putInt(PREF_KEY_CITIES, 0).apply();
        defaultPrefs.edit().putInt(PREF_KEY_TIME, 0).apply();
        defaultPrefs.edit().putInt(PREF_KEY_TREASURE, 0).apply();
        defaultPrefs.edit().putString(PREF_KEY_HEART, "custom").apply();
        defaultPrefs.edit().remove(PREF_KEY_CIVILIZATION).apply();

        newGameStartedEvent.setValue(new Event<>(true));
        newGameResetCompletedEvent.setValue(new Event<>(true));
    }

    /**
     * Updates the Bonus for all colors by adding the respective fields to cardBonus HashSet.
     * @param blue additional bonus for blue cards.
     * @param green additional bonus for green cards.
     * @param orange additional bonus for orange cards.
     * @param red additional bonus for red cards.
     * @param yellow additional bonus for yellow cards.
     */
    public void updateBonus(int blue, int green, int orange, int red, int yellow) {
        cardBonus.getValue().compute(CardColor.BLUE,(k,v)->(v==null)? blue :v+blue);
        cardBonus.getValue().compute(CardColor.GREEN, (k,v) ->(v==null)? green :v+green);
        cardBonus.getValue().compute(CardColor.ORANGE, (k,v) ->(v==null)? orange :v+orange);
        cardBonus.getValue().compute(CardColor.RED, (k,v) ->(v==null)? red :v+red);
        cardBonus.getValue().compute(CardColor.YELLOW, (k,v) ->(v==null)? yellow :v+yellow);
    }

    private Card getBuyableAdvanceByNameFromMap(String name) {
        if (Boolean.TRUE.equals(areBuyableCardsReady.getValue())) {
            return buyableCardMap.get(name);
        }
        Log.w("CivicViewModel", "Attempted to get card '" + name + "' from map, but cards not ready.");
        return null;
    }

    /**
     * Calculates the sum of all currently selected advances during the buy process and
     * updates remaining treasure.
     * @param selection Currently selected cards from the View.  <-- PARAMETER TYP ANGEPASST
     */
    public void calculateTotal(Iterable<String> selection) { // Parameter-Typ von Selection<String> zu Iterable<String>
        if (!Boolean.TRUE.equals(areBuyableCardsReady.getValue())) {
            Log.w("CivicViewModel", "calculateTotal called, but buyable cards are not ready.");
            int currentTreasure = (treasure.getValue() != null) ? treasure.getValue() : 0;
            this.remaining.setValue(currentTreasure); // Setze remaining auf Treasure, wenn Karten nicht bereit
            return;
        }

        int newTotalCost = 0; // Umbenannt von newTotal zu newTotalCost für Klarheit
        if (selection != null) {
            Iterator<String> iterator = selection.iterator();
            if (iterator.hasNext()) { // Nur iterieren, wenn das Iterable nicht leer ist
                for (String name : selection) {
                    Card adv = getBuyableAdvanceByNameFromMap(name);
                    if (adv != null) {
                        newTotalCost += adv.getCurrentPrice();
                    } else {
                        Log.e("CivicViewModel", "Card with name '" + name + "' not found in buyableCardMap during calculateTotal.");
                    }
                }
            }
        }
        int currentTreasure = (treasure.getValue() != null) ? treasure.getValue() : 0;
        this.remaining.setValue(currentTreasure - newTotalCost); // remaining wird hier aktualisiert
    }

    /**
     * Adds the bonuses of a bought card to the cardBonus HashSet.
     * @param name The name of the bought card.
     */
    public void addBonus(String name) {
        Card adv = getBuyableAdvanceByNameFromMap(name);
        Log.d("CivicViewModel", "Adding bonus for card: " + name);
        updateBonus(adv.getCreditsBlue(), adv.getCreditsGreen(), adv.getCreditsOrange(), adv.getCreditsRed(), adv.getCreditsYellow());
    }

    public static Drawable getItemBackgroundColor(Card card, Resources res) {
        int backgroundColor = 0;
        if (card.getGroup2() == null) {
            switch (card.getGroup1()) {
                case ORANGE:
                    backgroundColor = R.color.crafts;
                    break;
                case YELLOW:
                    backgroundColor = R.color.religion;
                    break;
                case RED:
                    backgroundColor = R.color.civic;
                    break;
                case GREEN:
                    backgroundColor = R.color.science;
                    break;
                case BLUE:
                    backgroundColor = R.color.arts;
                    break;
                default:
                    backgroundColor = R.color.purple_700;
                    break;
            }
        } else {
            switch (card.getName()) {
                case "Engineering":
                    backgroundColor = R.drawable.engineering_background;
                    break;
                case "Mathematics":
                    backgroundColor = R.drawable.mathematics_background;
                    break;
                case "Mysticism":
                    backgroundColor = R.drawable.mysticism_background;
                    break;
                case "Written Record":
                    backgroundColor = R.drawable.written_record_background;
                    break;
                case "Theocracy":
                    backgroundColor = R.drawable.theocracy_background;
                    break;
                case "Literacy":
                    backgroundColor = R.drawable.literacy_background;
                    break;
                case "Wonder of the World":
                    backgroundColor = R.drawable.wonders_of_the_world_background;
                    break;
                case "Philosophy":
                    backgroundColor = R.drawable.philosophy_background;
                    break;
                case "Monument":
                    backgroundColor = R.drawable.monument_background;
                    break;
            }
        }
        return ResourcesCompat.getDrawable(res,backgroundColor, null);
    }

    public int getBlue() {
        return cardBonus.getValue().getOrDefault(CardColor.BLUE,0);
    }
    public int getGreen() {
        return cardBonus.getValue().getOrDefault(CardColor.GREEN,0);
    }
    public int getOrange() {
        return cardBonus.getValue().getOrDefault(CardColor.ORANGE,0);
    }
    public int getRed() {
        return cardBonus.getValue().getOrDefault(CardColor.RED,0);
    }
    public int getYellow() {
        return cardBonus.getValue().getOrDefault(CardColor.YELLOW,0);
    }

    /**
     * Orchestrates the purchase process. Delegates the database operations to the Repository
     * and handles the UI responses (dialogs) based on the results.
     * This method is called from the Fragment.
     * @param selectedCardNames The names of the cards selected for purchase.
     */
    public void processPurchases(List<String> selectedCardNames) {
        Log.d("CivicViewModel", "processPurchases() called from Fragment with " + selectedCardNames.size() + " cards.");
        // Rufe die asynchrone Methode im Repository auf
        mRepository.processPurchasesAndRecalculatePricesAsync(selectedCardNames, cardBonus,
                new PurchaseCompletionCallback() {
                    @Override
                    public void onPurchaseCompleted(int totalExtraCredits, List<String> anatomyCardsToChoose) {
                        Log.d("CivicViewModel", "PurchaseCompletionCallback: onPurchaseCompleted. Extra Credits: " + totalExtraCredits + ", Anatomy Cards: " + anatomyCardsToChoose.size());
                        // Diese Methode wird im Hintergrund-Thread aufgerufen.
                        // Aktualisiere LiveData im ViewModel auf dem Haupt-Thread mit postValue
                        if (!anatomyCardsToChoose.isEmpty()) {
                            showAnatomyDialogEvent.postValue(new Event<>(anatomyCardsToChoose));
                        }
                        if (totalExtraCredits > 0) {
                            _showExtraCreditsDialogEvent.postValue(new Event<>(totalExtraCredits));
                        }

                        // Optional: LiveData Event auslösen, um Navigation zu signalisieren, falls keine Dialoge nötig sind
                        if (anatomyCardsToChoose.isEmpty() && totalExtraCredits == 0) {
                            _navigateToDashboardEvent.postValue(new Event<>(true));
                        }
                    }

                    @Override
                    public void onPurchaseFailed(String errorMessage) {
                        Log.e("CivicViewModel", "PurchaseCompletionCallback: onPurchaseFailed. Error: " + errorMessage);
                        // Handle error, maybe show a Toast via another LiveData event
                        // _showErrorToastEvent.postValue(new Event<>(errorMessage));
                    }
                });
    }

    public void saveBonus() {
        Log.d("CivicViewModel", "Saving bonus to SharedPreferences.");
        // HashMap Save
        for (Map.Entry<CardColor, Integer> entry: getCardBonus().getValue().entrySet()){
            defaultPrefs.edit().putInt(entry.getKey().getName(), entry.getValue()).apply();
        }
    }

    public void saveData() {
        defaultPrefs.edit().putInt(PREF_KEY_CITIES, getCities()).apply();
        defaultPrefs.edit().putInt(PREF_KEY_TIME, getTimeVp()).apply();
    }

    /**
     * Triggers the event to show the Extra Credits Dialog.
     * The observing Fragment will react to this event.
     *
     * @param credits The number of extra credits to offer.
     */
    public void triggerExtraCreditsDialog(int credits) {
        _showExtraCreditsDialogEvent.postValue(new Event<>(credits));
    }

    /**
     * Triggers the event to show the Anatomy Dialog.
     * The observing Fragment will react to this event.
     *
     * @param greenCards The list of green cards to choose from.
     */
    public void triggerAnatomyDialog(List<String> greenCards) {
        // Postet einen neuen Event mit der Liste der grünen Karten
        showAnatomyDialogEvent.postValue(new Event<>(greenCards));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (buyableCardsMapObserver != null) {
            allAdvancesNotBought.removeObserver(buyableCardsMapObserver);
        }
        defaultPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    // Schnittstelle für die Callback vom Repository
    public interface PurchaseCompletionCallback {
        void onPurchaseCompleted(int totalExtraCredits, List<String> anatomyCardsToChoose);
        void onPurchaseFailed(String errorMessage); // Optional: Fehlerbehandlung
    }

    private final MutableLiveData<Set<String>> _selectedCardKeysForState = new MutableLiveData<>(Collections.emptySet());
    public LiveData<Set<String>> getSelectedCardKeysForState() {
        return _selectedCardKeysForState;
    }

    // NEU: Methode, um die Auswahl aus dem Fragment im ViewModel zu aktualisieren
    public void updateSelectionState(Set<String> currentSelection) {
        if (currentSelection == null) {
            _selectedCardKeysForState.setValue(Collections.emptySet());
        } else {
            _selectedCardKeysForState.setValue(new HashSet<>(currentSelection));
        }
    }

    // NEU oder ANPASSEN: Methode, um die gespeicherte Auswahl zu löschen
    public void clearCurrentSelectionState() {
        _selectedCardKeysForState.setValue(Collections.emptySet());
        // Die calculateTotal-Logik sollte idealerweise durch den SelectionTracker-Observer
        // im Fragment ausgelöst werden, wenn der Tracker geleert wird.
        // Wenn du hier explizit totalPrice etc. zurücksetzen willst:
        // calculateTotal(Collections.emptySet());
    }

    // Methode, um die initialen Daten für die Tipps zu laden
    // Wird vom TipsFragment aufgerufen
    public void loadTipsInitialData() {
        if (tipsArray == null) {
            tipsArray = mApplication.getResources().getStringArray(R.array.tips);
        }
        int civicNumber = Integer.parseInt(defaultPrefs.getString(PREF_KEY_CIVILIZATION, "1"));
        _selectedTipIndex.setValue(civicNumber - 1);
    }

    public void setSelectedTipIndex(int index) {
        if (index >= 0 && (tipsArray == null || index < tipsArray.length)) {
            _selectedTipIndex.setValue(index);

            // Optional: Hier die neue Auswahl in SharedPreferences speichern, wenn gewünscht
            // defaultPrefs.edit().putString("civilization", String.valueOf(index + 1)).apply();
        }
    }

    public String getTipForIndex(int index) {
        if (tipsArray != null && index >= 0 && index < tipsArray.length) {
            return tipsArray[index];
        }
        Log.w("CivicViewModel", "Attempted to get tip for invalid index: " + index + " or tipsArray not loaded.");
        return ""; // Fallback
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY_CIVILIZATION.equals(key)) {
            // Wert neu laden und LiveData aktualisieren
            int civicNumber = Integer.parseInt(sharedPreferences.getString(key, "1"));
            int newIndex = civicNumber - 1;
            // Nur aktualisieren, wenn sich der Wert tatsächlich geändert hat,
            // um unnötige UI-Updates oder Schleifen zu vermeiden.
            if (_selectedTipIndex.getValue() == null || _selectedTipIndex.getValue() != newIndex) {
                _selectedTipIndex.setValue(newIndex);
            }
        } else if (PREF_KEY_SORT.equals(key)) {
            String newSortingOrder = sharedPreferences.getString(key, "name");
            if (currentSortingOrder.getValue() == null || !currentSortingOrder.getValue().equals(newSortingOrder)) {
                currentSortingOrder.setValue(newSortingOrder);
            }
        } else if (PREF_KEY_HEART.equals(key)){
            String newHeartSelection = sharedPreferences.getString(key, "custom");
            if (userPreferenceForHeartCards.getValue() == null || !userPreferenceForHeartCards.getValue().equals(newHeartSelection)) {
                updateUserHeartPreference(newHeartSelection);
            }
        }
    }

    public void updateUserHeartPreference(String selectionName) {
        defaultPrefs.edit().putString(PREF_KEY_HEART, selectionName).apply();
        userPreferenceForHeartCards.setValue(selectionName);
        processHeartPreferenceChange(selectionName);
    }


    private void processHeartPreferenceChange(String selectionName) {
        List<String> cardNamesToMarkAsHeart = getCardNamesForHeartSelection(selectionName);

        // Wichtig: Diese Datenbankoperationen sollten in einem Hintergrundthread ausgeführt werden.
        // mRepository sollte Methoden anbieten, die dies intern tun (z.B. mit Coroutinen oder AsyncTask).
        mRepository.resetAllCardsHeartStatusAsync(() -> {
            // Dieser Callback wird ausgeführt, nachdem alle Herzen zurückgesetzt wurden.
            if (cardNamesToMarkAsHeart != null && !cardNamesToMarkAsHeart.isEmpty()) {
                mRepository.setCardsAsHeartAsync(cardNamesToMarkAsHeart, () -> {
                    // Optional: Benachrichtige die UI, dass sich die Daten geändert haben,
                    // falls die LiveData-Beobachtung der Kartenliste nicht ausreicht.
                    // Zum Beispiel, wenn du eine spezifische Aktion nach dem Update auslösen willst.
                    Log.d("CivicViewModel", "Cards updated with new heart status for: " + selectionName);
                });
            } else {
                Log.d("CivicViewModel", "No specific cards to mark for heart selection: " + selectionName + " or selection is 'custom'.");
            }
        });
    }

    /**
     * Holt die Liste der Kartennamen basierend auf der "heart"-Auswahl.
     * Nutzt deine bestehende Logik aus getChooserCards().
     */
    private List<String> getCardNamesForHeartSelection(String selectionName) {
        // Deine bestehende Logik, um die Kartenlisten zu bekommen.
        // Du hast diese Logik bereits in 'getChooserCards()', passe sie ggf. leicht an,
        // um direkt die Namen zurückzugeben oder die Logik hier zu duplizieren/refaktorieren.

        // Beispielhafte Übernahme/Anpassung deiner getChooserCards-Logik:
        switch (selectionName.toLowerCase()) { // ToLowerCase für Robustheit
            case "treasury":
                return Arrays.asList(TREASURY);
            case "commodities":
                return Arrays.asList(COMMODITY_CARDS);
            case "cheaper":
                return Arrays.asList(CHEAPER_CIVILIZATION_CARDS);
            case "bend":
                return Arrays.asList(TO_BEND_THE_RULES);
            case "more":
                return Arrays.asList(MORE_TOKEN_ON_THE_MAP);
            case "mobility":
                return Arrays.asList(TOKEN_MOBILITY);
            case "cities":
                return Arrays.asList(CITIES); // Stelle sicher, dass CITIES hier Kartennamen sind
            case "sea":
                return Arrays.asList(SEA_POWER);
            case "aggression":
                return Arrays.asList(AGGRESSION);
            case "defense":
                return Arrays.asList(DEFENSE);
            case "custom":
            default:
                return new ArrayList<>(); // Keine spezifischen Karten für "custom" oder unbekannte Auswahl
        }
    }
}
