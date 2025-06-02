package org.tesira.civic.db // Selbes Paket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("name")]
)
data class Purchase(

    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String
)