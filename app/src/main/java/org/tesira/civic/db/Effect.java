package org.tesira.civic.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "effects")
public class Effect {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name ="advance")
    private String mAdvance;

    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name="value")
    private int mValue;

    @Ignore
    public Effect(@NonNull String mAdvance, @NonNull String mName, int mValue) {
        this.mAdvance = mAdvance;
        this.mName = mName;
        this.mValue = mValue;
    }

    public Effect(@NonNull String mAdvance, @NonNull String mName, int mValue, int id) {
        this.id = 0;
        this.mAdvance = mAdvance;
        this.mName = mName;
        this.mValue = mValue;
    }

    public int getId() {return id;}

    @NonNull
    public String getAdvance() {return mAdvance;}

    @NonNull
    public String getName() {return mName;}

    public int getValue() {return mValue;}
}
