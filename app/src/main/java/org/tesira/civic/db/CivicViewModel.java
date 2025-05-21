package org.tesira.civic.db;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.recyclerview.selection.Selection;
import androidx.preference.PreferenceManager;

import org.tesira.civic.Calamity;
import org.tesira.civic.R;
import org.tesira.civic.Event;
//import org.tesira.civic.db.Effect;
//import org.tesira.civic.db.Card;
//import org.tesira.civic.db.CardColor;
//import org.tesira.civic.db.CivicRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class CivicViewModel extends AndroidViewModel {

    private CivicRepository mRepository;
    private int screenWidthDp;
    private final MutableLiveData<Integer> treasure  = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> remaining = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> vp = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> civilization = new MutableLiveData<>(0);
    public MutableLiveData<HashMap<CardColor, Integer>> cardBonus;
    private final MutableLiveData<Integer> cities = new MutableLiveData<>(0);
    private Application mApplication;
    private final MutableLiveData<Integer> timeVp = new MutableLiveData<>(0);
    public String heart;

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


    public LiveData<Event<List<String>>> getShowAnatomyDialogEvent() {
        return showAnatomyDialogEvent;
    }
    private final MutableLiveData<Event<Integer>> _showExtraCreditsDialogEvent = new MutableLiveData<>();
    public LiveData<Event<Integer>> getShowExtraCreditsDialogEvent() {
        return _showExtraCreditsDialogEvent;
    }
    private SharedPreferences defaultPrefs, sharedPrefs;
    private static final String PREF_FILE_BONUS = "purchasedAdvancesBonus";
    private static final String PREF_CITIES = "cities";
    private static final String PREF_TIME = "time";


    public CivicViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) throws ExecutionException, InterruptedException {
        super(application);
        cardBonus = new MutableLiveData<>(new HashMap<>());
        mRepository = new CivicRepository(application);
        mApplication = application;
        librarySelected = false;
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(mApplication);
        sharedPrefs = mApplication.getSharedPreferences(PREF_FILE_BONUS, Context.MODE_PRIVATE );

        allAdvancesNotBought = Transformations.switchMap(currentSortingOrder,
                order -> mRepository.getAllAdvancesNotBoughtLiveData(order));
        loadData();

    }


    public void loadData() {
        int blue, green, orange, red, yellow;
        blue = sharedPrefs.getInt(CardColor.BLUE.getName(), 0);
        green = sharedPrefs.getInt(CardColor.GREEN.getName(), 0);
        orange = sharedPrefs.getInt(CardColor.ORANGE.getName(), 0);
        red = sharedPrefs.getInt(CardColor.RED.getName(), 0);
        yellow = sharedPrefs.getInt(CardColor.YELLOW.getName(), 0);
        getCardBonus().getValue().put(CardColor.BLUE, blue);
        getCardBonus().getValue().put(CardColor.GREEN, green);
        getCardBonus().getValue().put(CardColor.ORANGE, orange);
        getCardBonus().getValue().put(CardColor.RED, red);
        getCardBonus().getValue().put(CardColor.YELLOW, yellow);
        setHeart(defaultPrefs.getString("heart", "custom"));
        setCities(sharedPrefs.getInt(PREF_CITIES, 0));
        setTimeVp(sharedPrefs.getInt(PREF_TIME,0));
        mColumnCount = Integer.parseInt(defaultPrefs.getString("columns", "1"));
        currentSortingOrder.setValue(defaultPrefs.getString("sort", "name"));
    }
    public int getCities() {
        return cities.getValue();
    }
    public LiveData<Integer> getCitiesLive() {
        return cities;
    }

    public void setCities(int value) {
        cities.setValue(value);
        sumVp();
    }
    public void requestPriceRecalculation() {
        mRepository.recalculateCurrentPricesAsync(cardBonus.getValue());
    }
    public void addBonusAndUpdatePrices(String cardName) {
        mRepository.addBonusAndUpdatePricesAsync(cardName);
    }
    public int getScreenWidthDp() {
        return screenWidthDp;
    }
    public void setScreenWidthDp(int screenWidthDp) {
        this.screenWidthDp = screenWidthDp;
    }
    public String getHeart() {
        return heart;
    }
    public void setHeart(String heart) {
        this.heart = heart;
    }
    public int getTimeVp() {
        return timeVp.getValue();
    }
    public LiveData<Integer> getTimeVpLive() {
        return timeVp;
    }

    public void setTimeVp(int value) {
        timeVp.setValue(value);
        sumVp();
    }

    public void insertPurchase(String purchase) {mRepository.insertPurchase(purchase);}
    public void recalculateCurrentPrices() {mRepository.recalculateCurrentPricesAsync(cardBonus.getValue());}
    public void deletePurchases() {
        mRepository.deletePurchases();
        mRepository.resetCurrentPrice();
        cardBonus.setValue(new HashMap<>());
    }

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

    public Card getAdvanceByName(String name) { return mRepository.getAdvanceByNameToCard(name);}
    public List<Card> getAllAdvancesSortedByName() {return mRepository.getAllAdvancesSortedByName();}
    public List<String> getPurchasesAsString() {return mRepository.getPurchasesAsString();}
    public List<Card> getPurchasesAsCard() {return mRepository.getPurchasesAsCard();}
    public List<Calamity> getCalamityBonus() {return mRepository.getCalamityBonus();}
    public List<String> getSpecialAbilities() {return mRepository.getSpecialAbilities();}
    public List<String> getImmunities() {return mRepository.getImmunities();}
    public int sumVp() {
        int newVp = mRepository.sumVp();
        newVp += cities.getValue();
        newVp += timeVp.getValue();
        this.vp.setValue(newVp);
        return newVp;
    }
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

    public MutableLiveData<Integer> getVp() {return vp;}
    public void setVp(int newVp) {this.vp.setValue(newVp);}

    public MutableLiveData<HashMap<CardColor, Integer>> getCardBonus() {
        return cardBonus;
    }

    // This LiveData signals that a new game reset has been INITIATED or completed.
    // It will be used to trigger UI updates.

    private final MutableLiveData<Event<Boolean>> newGameStartedEvent = new MutableLiveData<>();
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
        Log.d("CivicViewModel", "requestNewGame() called. Initiating startNewGameProcess.");
        startNewGameProcess();
    }

    /**
     * Performs the core logic for starting a new game.
     * This includes resetting persistent data and ViewModel state.
     */
    public void startNewGameProcess() {
        sharedPrefs.edit().clear().apply();
        mRepository.deletePurchases();
        mRepository.resetCurrentPrice();
        mRepository.resetDB();
        treasure.setValue(0);
        remaining.setValue(0);
        cities.setValue(0);
        timeVp.setValue(0);
        vp.setValue(0);
        cardBonus.setValue(new HashMap<>());

        defaultPrefs.edit().putInt("treasure", 0).apply();
        defaultPrefs.edit().putString("heart", "custom").apply(); // Or your default value
        defaultPrefs.edit().remove("civilization").apply();

        newGameStartedEvent.setValue(new Event<>(true));
        newGameResetCompletedEvent.setValue(new Event<>(true));
    }

    /**
     * Updates the card price, checking for best color if the card is in two groups and for
     * the special family/row bonus.
     */
    // alte funktionierende Methode zur Aktualisierung der Preise
    public void calculateCurrentPrice() {
        int newCurrent;
        List<Card> cachedCards = mRepository.getAllAdvancesNotBought("name");
        for (Card adv: cachedCards) {
            if (adv.getGroup2() == null) {
                newCurrent = adv.getPrice() - cardBonus.getValue().getOrDefault(adv.getGroup1(),0);
            } else {
                int group1 = cardBonus.getValue().getOrDefault(adv.getGroup1(),0);
                int group2 = cardBonus.getValue().getOrDefault(adv.getGroup2(),0);
                newCurrent = adv.getPrice() - Math.max(group1, group2);
            }
            if (newCurrent < 0 ) newCurrent = 0;
            mRepository.updateCurrentPrice(adv.getName(), newCurrent);
        }
        // adding special family bonus if predecessor bought
        for (Card adv: mRepository.getPurchasesForBonus()) {
            Card bonusTo = mRepository.getAdvanceByNameToCard(adv.getBonusCard());
            newCurrent = bonusTo.getCurrentPrice() - adv.getBonus();
            if (newCurrent < 0 ) newCurrent = 0;
            mRepository.updateCurrentPrice(adv.getBonusCard(), newCurrent);
        }
    }

    public List<String> getAnatomyCards(){return mRepository.getAnatomyCards();}
    public List<Effect> getEffect(String advance, String name){return mRepository.getEffect(advance, name);}

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

    /**
     * Calculates the sum of all currently selected advances during the buy process and
     * updates remaining treasure.
     * @param selection Currently selected cards from the View.
     */
    public void calculateTotal(Selection<String> selection) {
        int newTotal = 0;
        if (selection.size() > 0) {
            for (String name : selection) {
                Card adv = getAdvanceByName(name);
                newTotal += adv.getCurrentPrice();
            }
        }
        this.remaining.setValue(treasure.getValue() - newTotal);
    }
    /**
     * Adds the bonuses of a bought card to the cardBonus HashSet.
     * @param name The name of the bought card.
     */
    public void addBonus(String name) {
        Card adv = getAdvanceByName(name);
        updateBonus(adv.getCreditsBlue(), adv.getCreditsGreen(), adv.getCreditsOrange(), adv.getCreditsRed(), adv.getCreditsYellow());
    }

    public List<String> getChooserCards() {
        List<String> list = null;
        switch (heart) {
            case "treasury":
                list = Arrays.asList(TREASURY);
                break;
            case "commodities":
                list = Arrays.asList(COMMODITY_CARDS);
                break;
            case "cheaper":
                list = Arrays.asList(CHEAPER_CIVILIZATION_CARDS);
                break;
            case "bend":
                list = Arrays.asList(TO_BEND_THE_RULES);
                break;
            case "more":
                list = Arrays.asList(MORE_TOKEN_ON_THE_MAP);
                break;
            case "mobility":
                list = Arrays.asList(TOKEN_MOBILITY);
                break;
            case "cities":
                list = Arrays.asList(CITIES);
                break;
            case "sea":
                list = Arrays.asList(SEA_POWER);
                break;
            case "aggression":
                list = Arrays.asList(AGGRESSION);
                break;
            case "defense":
                list = Arrays.asList(DEFENSE);
                break;
            case "custom":
            default:
        }
        return list;
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
        SharedPreferences.Editor editor = sharedPrefs.edit();
        // HashMap Save
        for (Map.Entry<CardColor, Integer> entry: getCardBonus().getValue().entrySet()){
            editor.putInt(entry.getKey().getName(), entry.getValue());
        }
        editor.apply();
    }

    public void saveData() {
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putInt(PREF_CITIES, getCities());
        edit.putInt(PREF_TIME, getTimeVp());
        edit.apply();
    }

    /**
     * Triggers the event to show the Extra Credits Dialog.
     * The observing Fragment will react to this event.
     *
     * @param credits The number of extra credits to offer.
     */
    public void triggerExtraCreditsDialog(int credits) {
        // Posten Sie einen neuen Event mit der Anzahl der Credits
        _showExtraCreditsDialogEvent.postValue(new Event<>(credits));
    }

    /**
     * Triggers the event to show the Anatomy Dialog.
     * The observing Fragment will react to this event.
     *
     * @param greenCards The list of green cards to choose from.
     */
    public void triggerAnatomyDialog(List<String> greenCards) {
        // Posten Sie einen neuen Event mit der Liste der grünen Karten
        showAnatomyDialogEvent.postValue(new Event<>(greenCards));
    }

    public int getmColumnCount() {
        return mColumnCount;
    }

    public void setmColumnCount(int mColumnCount) {
        this.mColumnCount = mColumnCount;
    }


    // Schnittstelle für die Callback vom Repository
    public interface PurchaseCompletionCallback {
        void onPurchaseCompleted(int totalExtraCredits, List<String> anatomyCardsToChoose);
        void onPurchaseFailed(String errorMessage); // Optional: Fehlerbehandlung
    }
}
