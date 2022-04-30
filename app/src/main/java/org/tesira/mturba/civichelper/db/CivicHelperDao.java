package org.tesira.mturba.civichelper.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Dao
public interface CivicHelperDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Card civilizationAdvance);

    @Query("DELETE FROM cards")
    void deleteAllCards();

    @Query("DELETE FROM effects")
    void deleteAllEffects();

    @Query("SELECT * FROM cards ORDER BY name ASC")
    List<Card> getAdvancesByName();

    @Query("SELECT * FROM cards ORDER BY price ASC")
    LiveData<List<Card>> getAdvancesByPriceOld();

    @Query("SELECT * FROM cards ORDER BY :sorting ASC")
    LiveData<List<Card>> getAdvances(String sorting);

    @Query("SELECT * FROM cards WHERE family = :familyNumber ORDER BY vp ASC")
    List<Card> getAdvancesByFamily(int familyNumber);

    @Query("UPDATE cards SET bonusCard = :newBonusCard WHERE name = :name")
    void updateBonusCard(String name, String newBonusCard);

    @Query("UPDATE cards SET bonus = :newBonus WHERE name = :name")
    void updateBonus(String name, int newBonus);

    @Query("SELECT * FROM cards WHERE name = :name ")
    LiveData<List<Card>> getAdvanceByName(String name);

    @Query("SELECT * FROM cards WHERE name = :name ")
    Card getAdvanceByNameToCard(String name);

    @Query("UPDATE cards SET isBuyable = 1 WHERE price > :remaining ")
    void updateIsBuyable(int remaining);

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL ORDER BY currentPrice ASC")
    LiveData<List<Card>> getAdvancesByPrice();

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL ORDER BY :order ASC")
    LiveData<List<Card>> getAdvancesLive(String order);


    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL AND currentPrice = 0")
    List<Card> getAdvancesForFree();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPurchase(Purchase purchase);

    @Query("DELETE FROM purchases")
    void deleteAllPurchases();

    @Query("SELECT * FROM purchases")
    LiveData<List<Purchase>> getPurchases();

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
}