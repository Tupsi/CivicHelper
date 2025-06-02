package org.tesira.civic.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "immunity")
data class Immunity(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "advance")
    val advance: String,

    @ColumnInfo(name = "immunity")
    val immunity: String
)