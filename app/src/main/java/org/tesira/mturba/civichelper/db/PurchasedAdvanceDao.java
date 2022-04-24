package org.tesira.mturba.civichelper.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PurchasedAdvanceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PurchasedAdvance purchasedAdvance);

    @Query("DELETE FROM purchases")
    void deleteAll();

    @Query("SELECT * FROM purchases")
    LiveData<List<PurchasedAdvance>> getPurchases();
}
