package org.tesira.civic.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import org.tesira.civic.Calamity;
import java.util.List;

@Dao
public interface CivicHelperDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Card civilizationAdvance);

    @Query("DELETE FROM cards")
    void deleteAllCards();

    @Query("DELETE FROM effects")
    void deleteAllEffects();

    @Query("DELETE FROM immunity")
    void deleteAllImmunities();

    @Query("DELETE FROM specials")
    void deleteAllSpecials();

    @Query("SELECT * FROM cards ORDER BY name ASC")
    List<Card> getAdvancesByName();

    @Query("SELECT * FROM cards WHERE family = :familyNumber ORDER BY vp ASC")
    List<Card> getAdvancesByFamily(int familyNumber);
    @Query("SELECT * FROM cards WHERE family = :familyNumber ORDER BY vp ASC")
    LiveData<List<Card>> getAdvancesByFamilyLiveData(int familyNumber);

    @Query("UPDATE cards SET bonusCard = :newBonusCard WHERE name = :name")
    void updateBonusCard(String name, String newBonusCard);

    @Query("UPDATE cards SET bonus = :newBonus WHERE name = :name")
    void updateBonus(String name, int newBonus);

    @Query("SELECT * FROM cards WHERE name = :name ")
    Card getAdvanceByNameToCard(String name);

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL ORDER BY " +
            "CASE WHEN :sortingOrder = 'name' THEN cards.name END ASC," +
            "CASE WHEN :sortingOrder = 'family' THEN cards.family END ASC," +
            "CASE WHEN :sortingOrder = 'color' THEN cards.group1 END," +
            "CASE WHEN :sortingOrder = 'color' THEN cards.currentPrice END ASC," +
            "CASE WHEN :sortingOrder = 'vp' THEN cards.vp END," +
            "CASE WHEN :sortingOrder = 'vp' THEN cards.currentPrice END ASC," +
            "CASE WHEN :sortingOrder = 'currentPrice'THEN cards.currentPrice END," +
            "CASE WHEN :sortingOrder = 'currentPrice'THEN cards.name END ASC")
    LiveData<List<Card>> getAllAdvancesNotBoughtLiveData(String sortingOrder);

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL ORDER BY name ASC")
    LiveData<List<Card>> getPurchasesAsCardLiveData();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPurchase(Purchase purchase);

    @Query("DELETE FROM purchases")
    void deleteAllPurchases();

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL AND cards.vp < 6")
    List<Card> getPurchasesForBonus();

    @Query("UPDATE cards SET currentPrice = :current WHERE name = :name")
    void updateCurrentPrice(String name, int current);

    @Query("Update cards SET currentPrice = price")
    void resetCurrentPrice();

    @Query("SELECT cards.name FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL AND price < 100 AND (group1 = 'GREEN' OR group2 = 'GREEN') ORDER BY cards.name ASC")
    List<String> getAnatomyCards();

    @Insert
    void insertEffect(Effect effect);

    @Query("SELECT * FROM effects WHERE name = :name AND advance = :advance ORDER BY advance ASC")
    List<Effect> getEffect(String advance, String name);

    @Query("SELECT  effects.name AS calamity, SUM(effects.value) AS bonus FROM effects LEFT JOIN purchases on effects.advance = purchases.name WHERE purchases.name IS NOT NULL AND calamity NOT LIKE 'Credits%' AND calamity NOT LIKE 'FreeScience' GROUP BY effects.name ORDER BY calamity ASC")
    List<Calamity> getCalamityBonus();

    @Query("SELECT  effects.name AS calamity, SUM(effects.value) AS bonus FROM effects LEFT JOIN purchases on effects.advance = purchases.name WHERE purchases.name IS NOT NULL AND calamity NOT LIKE 'Credits%' AND calamity NOT LIKE 'FreeScience' GROUP BY effects.name ORDER BY calamity ASC")
    LiveData<List<Calamity>> getCalamityBonusLiveData();

    @Insert
    void insertSpecialAbility(SpecialAbility ability);

    @Query("SELECT specials.ability from specials LEFT JOIN purchases on specials.advance = purchases.name WHERE purchases.name IS NOT NULL")
    LiveData<List<String>> getSpecialAbilitiesLiveData();

    @Insert
    void insertImmunity(Immunity immunity);

    @Query("SELECT immunity.immunity FROM immunity LEFT JOIN purchases on immunity.advance = purchases.name WHERE purchases.name IS NOT NULL")
    LiveData<List<String>> getImmunitiesLiveData();

    @Query("SELECT SUM(cards.vp) AS sum FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL")
    LiveData<Integer> cardsVp();

    @Query("UPDATE cards SET hasHeart = 0") // 0 für false
    void resetAllHearts();

    @Query("UPDATE cards SET hasHeart = 1 WHERE name IN (:cardNames)")
    void setHeartsForCards(List<String> cardNames);

}
