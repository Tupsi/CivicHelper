package org.tesira.mturba.civichelper.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Card civilizationAdvance);

    @Query("DELETE FROM card_table")
    void deleteAll();

    @Query("SELECT * FROM card_table ORDER BY name ASC")
    List<Card> getAdvancesByName();

    @Query("SELECT * FROM card_table ORDER BY price ASC")
    LiveData<List<Card>> getAdvancesByPrice();

    @Query("SELECT * FROM card_table ORDER BY :sorting ASC")
    LiveData<List<Card>> getAdvances(String sorting);

    @Query("SELECT * FROM card_table WHERE family = :familyNumber ORDER BY vp ASC")
    List<Card> getAdvancesByFamily(int familyNumber);

    @Query("UPDATE card_table SET bonusCard = :newBonusCard WHERE name = :name")
    void updateBonusCard(String name, String newBonusCard);

    @Query("UPDATE card_table SET bonus = :newBonus WHERE name = :name")
    void updateBonus(String name, int newBonus);

    @Query("SELECT * FROM card_table WHERE name = :name ")
    LiveData<List<Card>> getAdvanceByName(String name);

    @Query("SELECT * FROM card_table WHERE name = :name ")
    Card getAdvanceByNameToCard(String name);

    @Query("UPDATE card_table SET isBuyable = 1 WHERE price > :remaining ")
    void updateIsBuyable(int remaining);

}
