package org.tesira.mturba.civichelper.card;

import static org.tesira.mturba.civichelper.card.CardColor.GREEN;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.tesira.mturba.civichelper.MainActivity;
import org.tesira.mturba.civichelper.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to represent a civilization advance.
 */
public class Advance {

    // als Singleton ausbauen, wird nur einmal gebraucht

    private String name;
    // advances are divided in three columns and 17 rows and give a bonus for buying the
    // next in line in the same row
    private int family;                 // give bonus to next family member in same row
    private int vp;                     // victory points (1,3,6)
    private List<CardColor> groups;     // an advance may belong to one or two groups (colors)
    private int price;
    private List<Credit> credits;       // price reduction for next buy in which groups
    // optional
    // effects hold special bonuses during gameplay, to be displayed at in summation
    // on  startscreen
    private HashMap<String, Integer> effects;
    private int familybonus;
    private String familyname;

    public Advance() {
        this.groups = new ArrayList<>();
        this.credits = new ArrayList<>();
        this.effects = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFamily() {
        return family;
    }

    public void setFamily(int family) {
        this.family = family;
    }

    public int getVp() {
        return vp;
    }

    public void setVp(int vp) {
        this.vp = vp;
    }

    public int getPrice() {
        return price;
    }

    public List<CardColor> getGroups() {
        return groups;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public HashMap<String, Integer> getEffects() {
        return effects;
    }

    public void setEffects(HashMap<String, Integer> effects) {
        this.effects = effects;
    }

    public void addEffect(String name, int value) {
        effects.put(name, value);
    }

    public void addGroup(String color) {
        groups.add(CardColor.valueOf(color));
    }

    public List<Credit> getCredits() {
        return credits;
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public void addCredits(String color, int discount) {
        Credit credit = new Credit(CardColor.valueOf(color), discount);
        credits.add(credit);
    }

    public int getFamilybonus() {
        return familybonus;
    }

    public void setFamilybonus(int familybonus) {
        this.familybonus = familybonus;
    }

    public String getFamilyname() {
        return familyname;
    }

    public void setFamilyname(String familyname) {
        this.familyname = familyname;
    }

    public CardColor getPrimaryColor() {
        return groups.get(0);
    }

    public int getColor(){
        int rgb = 0;
        if (groups.size() == 1) {
            rgb = Advance.colorStringToColor(groups.get(0).getName());
            return rgb;
        } else {
            // hier noch Mischen der zwei Farben
            return rgb;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static int colorStringToColor(String colorString) {
        switch (colorString) {
            case "Crafts":
                return R.color.crafts;
            case "Religion":
                return R.color.religion;
            case "Civic":
                return R.color.civic;
            case "Science":
                return R.color.science;
            case "Arts":
                return R.color.arts;
            default:
                return R.color.purple_700;
        }
    }
    public static Advance getAdvanceFromName(List<Advance> list, String name) {
        int i = 0;
        for (Advance adv: list) {
            if (adv.getName().equals(name)) {
                return adv;
            }
            else {
                i++;
            }
        }
        return null;
    }

    public int getIndexFromName(List<Advance> list, String name) {
        int i = 0;
        for (Advance adv: list) {
            if (adv.getName().equals(name)) {
                return i;
            }
            else {
                i++;
            }
        }
        return -1;
    }

    public static void addFamilyBonus(List<Advance> list) {
        for (Advance adv : list) {
            int family = adv.getFamily();
            List<Advance> result;
            Log.d("Family", adv.getName());
            switch (adv.getVp()) {
                case 1:
                    Log.d("Family", adv.getName() + "in 1");
                    adv.setFamilybonus(10);
                    result = list.stream().filter(i -> (i.getVp() == 3 && i.family == family)).collect(Collectors.toList());
                    adv.setFamilyname(result.get(0).getName());
                    break;
                case 3:
                    adv.setFamilybonus(20);
                    result = list.stream().filter(i -> (i.getVp() == 6 && i.family == family)).collect(Collectors.toList());
                    adv.setFamilyname(result.get(0).getName());
                    break;
                case 6:
                    adv.setFamilybonus(0);
                    adv.setFamilyname("");
                    break;
            }
            Log.d("Family", "size :" + list.size());
        }
    }

    /**
     * @param list Adcances list
     * @return only green cards under 100
     *
     * Is used to get a set of undiscounted green cards under 100. This is needed
     * for buying Anatomy
     */
    public static Set<String> getGreenCards(List<Advance> list) {
        Set<String> greenCards = new HashSet<>();
        for (Advance adv: list) {
            List<CardColor> colors = adv.getGroups();
            if (colors.get(0).getName().equals("Science")) {
                greenCards.add(adv.getName());
                Log.v("GREEN", adv.getName());
            }
        }
        return greenCards;
    }
}
