package org.tesira.civic.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "specials")
class SpecialAbility {
    @PrimaryKey(autoGenerate = true)
    private var id: Int = 0

    @ColumnInfo(name = "advance")
    private val mAdvance: String

    @ColumnInfo(name = "ability")
    private val mAbility: String

    @Ignore
    constructor(mAdvance: String, mAbility: String) {
        this.mAdvance = mAdvance
        this.mAbility = mAbility
    }

    constructor(id: Int, mAdvance: String, mAbility: String) {
        this.id = 0
        this.mAdvance = mAdvance
        this.mAbility = mAbility
    }

    fun getId(): Int {
        return id
    }

    fun getAdvance(): String {
        return mAdvance
    }

    fun getAbility(): String {
        return mAbility
    }
}