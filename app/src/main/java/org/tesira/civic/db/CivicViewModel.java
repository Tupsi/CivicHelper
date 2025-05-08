package org.tesira.civic.db;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.recyclerview.selection.Selection;
import org.tesira.civic.Calamity;
import org.tesira.civic.R;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CivicViewModel extends AndroidViewModel {

    private CivicRepository mRepository;
    public List<Card> cachedCards;
    private int screenWidthDp;
    private MutableLiveData<Integer> treasure;
    private MutableLiveData<Integer> remaining;
    private MutableLiveData<Integer> vp;
    public MutableLiveData<HashMap<CardColor, Integer>> cardBonus;
    private int cities;
    private Application mApplication;
    private int timeVp;
    public String heart;
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

    public CivicViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) throws ExecutionException, InterruptedException {
        super(application);
        cardBonus = new MutableLiveData<>(new HashMap<>());
        mRepository = new CivicRepository(application);
        cachedCards = mRepository.getAllCards();
        treasure = new MutableLiveData<>();
        remaining = new MutableLiveData<>();
        vp = new MutableLiveData<>();
        cities = 0;
        mApplication = application;
        timeVp = 0;
    }

    public int getCities() {
        return cities;
    }

    public void setCities(int cities) {
        this.cities = cities;
        sumVp();
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
        return timeVp;
    }

    public void setTimeVp(int timeVp) {
        this.timeVp = timeVp;
        sumVp();
    }

    public void insertPurchase(String purchase) {mRepository.insertPurchase(purchase);}
    public void deletePurchases() {
        mRepository.deletePurchases();
        mRepository.resetCurrentPrice();
        cardBonus.setValue(new HashMap<>());
    }

    public List<Card> getAllAdvancesNotBought(String sortingOrder) {
        cachedCards = mRepository.getAllAdvancesNotBought(sortingOrder);
        return cachedCards;
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
        newVp += cities;
        newVp += timeVp;
        this.vp.setValue(newVp);
        return newVp;
    }
    public void resetDB() {mRepository.resetDB();}
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

    /**
     * Updates the card price, checking for best color if the card is in two groups and for
     * the special family/row bonus.
     */
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

//    public int recalculateBonus(SharedPreferences sharedPreferences) {
//        sharedPreferences.edit().clear().commit();
//        List<Card> purchases = mRepository.getPurchasesAsCard();
//        int blue = 0, green = 0, orange = 0, red = 0, yellow = 0, special = 0;
//        for (Card card: purchases
//             ) {
//            blue += card.getCreditsBlue();
//            green += card.getCreditsGreen();
//            orange += card.getCreditsOrange();
//            red += card.getCreditsRed();
//            yellow += card.getCreditsYellow();
//            // check for Written Record or Monument
//            List<Effect> effects = mRepository.getEffect(card.getName(), "Credits");
//            if (effects.size() == 1) {
//                special += effects.get(0).getValue();
//            }
//        }
//
//        cardBonus.getValue().put(CardColor.BLUE, blue);
//        cardBonus.getValue().put(CardColor.GREEN,green);
//        cardBonus.getValue().put(CardColor.ORANGE,orange);
//        cardBonus.getValue().put(CardColor.RED,red);
//        cardBonus.getValue().put(CardColor.YELLOW, yellow);
//        saveBonus(sharedPreferences);
//        return special;
//    }

//    public void saveBonus(SharedPreferences savedBonus) {
//        SharedPreferences.Editor editor = savedBonus.edit();
//        // HashMap Save
//        for (Map.Entry<CardColor, Integer> entry: this.getCardBonus().getValue().entrySet()){
//            editor.putInt(entry.getKey().getName(), entry.getValue());
//        }
//        editor.apply();
////        Log.v("MAIN", "saveBonus in Main");
//    }

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
}
