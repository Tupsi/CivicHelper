package org.tesira.mturba.civichelper.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchases")
public class PurchasedAdvance {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "bonusTo")
    private String mBonusTo;

//    @ColumnInfo(name = "value")
//    private int mValue;

    public PurchasedAdvance(String mName, String mBonusTo) {
        this.mName = mName;
        this.mBonusTo = mBonusTo;
    }

    public String getName() {
        return mName;
    }

    public String getBonusTo() {
        return mBonusTo;
    }

//    public int getValue() {
//        return mValue;
//    }
}
