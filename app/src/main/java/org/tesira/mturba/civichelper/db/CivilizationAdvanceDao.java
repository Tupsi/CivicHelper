package org.tesira.mturba.civichelper.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CivilizationAdvanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CivilizationAdvance civilizationAdvance);

    @Query("DELETE FROM card_table")
    void deleteAll();

    @Query("SELECT * FROM card_table ORDER BY name ASC")
    LiveData<List<CivilizationAdvance>> getAdvancesByName();

    @Query("SELECT * FROM card_table ORDER BY price ASC")
    LiveData<List<CivilizationAdvance>> getAdvancesByPrice();
}
