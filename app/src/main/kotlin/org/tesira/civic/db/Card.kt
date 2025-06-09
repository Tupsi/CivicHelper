package org.tesira.civic.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "cards")
@TypeConverters(Converters::class)
data class Card(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "family")
    val family: Int,

    @ColumnInfo(name = "vp")
    val vp: Int,

    @ColumnInfo(name = "price")
    val price: Int,

    @ColumnInfo(name = "group1")
    val group1: CardColor?,

    @ColumnInfo(name = "group2")
    val group2: CardColor?,

    @ColumnInfo(name = "creditsBlue")
    val creditsBlue: Int,

    @ColumnInfo(name = "creditsGreen")
    val creditsGreen: Int,

    @ColumnInfo(name = "creditsOrange")
    val creditsOrange: Int,

    @ColumnInfo(name = "creditsRed")
    val creditsRed: Int,

    @ColumnInfo(name = "creditsYellow")
    val creditsYellow: Int,

    @ColumnInfo(name = "bonusCard")
    val bonusCard: String?,

    @ColumnInfo(name = "bonus")
    val bonus: Int,

    @ColumnInfo(name = "isBuyable")
    val isBuyable: Boolean,

    @ColumnInfo(name = "currentPrice")
    val currentPrice: Int,

    @ColumnInfo(name = "buyingPrice")
    val buyingPrice: Int,

    @ColumnInfo(name = "hasHeart")
    val hasHeart: Boolean,

    @ColumnInfo(name = "info")
    val info: String?
)