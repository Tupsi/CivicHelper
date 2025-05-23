package org.tesira.civic.db // Selbes Paket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("name")] // Beachte die Array-Syntax für 'indices' in Kotlin
)
data class Purchase( // 'data class' ist ideal für solche Entitäten

    @PrimaryKey
    @ColumnInfo(name = "name") // @NonNull ist hier implizit durch den Typ 'String'
    val name: String // 'val name: String' ist nicht-nullable. Für nullable wäre es 'val name: String?'
)