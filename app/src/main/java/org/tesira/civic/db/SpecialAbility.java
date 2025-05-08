package org.tesira.civic.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "specials")

public class SpecialAbility {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name ="advance")
    private String mAdvance;

    @NonNull
    @ColumnInfo(name = "ability")
    private String mAbility;

    @Ignore
    public SpecialAbility(@NonNull String mAdvance, @NonNull String mAbility) {
        this.mAdvance = mAdvance;
        this.mAbility = mAbility;
    }

    public SpecialAbility(int id, @NonNull String mAdvance, @NonNull String mAbility) {
        this.id = 0;
        this.mAdvance = mAdvance;
        this.mAbility = mAbility;
    }

    public int getId() {return id;}

    @NonNull
    public String getAdvance() {
        return mAdvance;
    }

    @NonNull
    public String getAbility() {
        return mAbility;
    }
}
