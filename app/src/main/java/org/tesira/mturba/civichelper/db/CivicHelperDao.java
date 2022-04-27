package org.tesira.mturba.civichelper.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CivicHelperDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Card civilizationAdvance);

    @Query("DELETE FROM cards")
    void deleteAll();

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

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL ORDER BY price ASC")
    LiveData<List<Card>> getAdvancesByPrice();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPurchase(Purchase purchase);

    @Query("DELETE FROM purchases")
    void deleteAllPurchases();

    @Query("SELECT * FROM purchases")
    LiveData<List<Purchase>> getPurchases();
}
