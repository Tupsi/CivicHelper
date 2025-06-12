package org.tesira.civic.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specials")
data class SpecialAbility(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "advance")
    val advance: String,

    @ColumnInfo(name = "ability")
    val ability: String

)