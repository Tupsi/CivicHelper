package org.tesira.mturba.civichelper.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CivilizationAdvance.class, PurchasedAdvance.class}, version = 1, exportSchema = false)
public abstract class CivicHelperDatabase extends RoomDatabase {

    public abstract CivilizationAdvanceDao civicDao();
    public abstract PurchasedAdvanceDao purchaseDao();

    private static volatile CivicHelperDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static CivicHelperDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CivicHelperDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CivicHelperDatabase.class, "civic_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
