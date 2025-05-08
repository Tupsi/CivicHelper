package org.tesira.civic.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "immunity")
public class Immunity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name ="advance")
    private String mAdvance;

    @NonNull
    @ColumnInfo(name = "immunity")
    private String mImmunity;

    @Ignore
    public Immunity(@NonNull String mAdvance, @NonNull String mImmunity) {
        this.mAdvance = mAdvance;
        this.mImmunity = mImmunity;
    }

    public Immunity(int id, @NonNull String mAdvance, @NonNull String mImmunity) {
        this.id = 0;
        this.mAdvance = mAdvance;
        this.mImmunity = mImmunity;
    }

    public int getId() {return id;}

    @NonNull
    public String getAdvance() {
        return mAdvance;
    }

    @NonNull
    public String getImmunity() {
        return mImmunity;
    }
}

