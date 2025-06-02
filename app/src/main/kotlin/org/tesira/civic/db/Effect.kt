package org.tesira.civic.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "effects")
data class Effect(

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0, // Default-Wert 0 für neue Objekte, Room überschreibt dies mit der DB-ID

    @ColumnInfo(name = "advance")
    val advance: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "value")
    val value: Int

)