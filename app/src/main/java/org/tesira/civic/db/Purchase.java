package org.tesira.civic.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchases", indices = {@Index("name")})
public class Purchase {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String mName;

    public Purchase(@NonNull String mName) {
        this.mName = mName;
    }

    public String getName() {
        return mName;
    }

}
