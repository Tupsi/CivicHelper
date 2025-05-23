package org.tesira.civic.db // Selbes Paket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // Wichtig für die CardColor-Felder

@Entity(tableName = "cards")
@TypeConverters(Converters::class) // Den Konverter hier auf Klassenebene deklarieren
data class Card(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String, // Nicht-nullable

    @ColumnInfo(name = "family")
    val family: Int,

    @ColumnInfo(name = "vp")
    val vp: Int,

    @ColumnInfo(name = "price")
    val price: Int,

    // Für group1, das nicht null sein soll:
    // Der Konverter Converters.toCardColor gibt CardColor? zurück.
    // Wenn du hier 'val group1: CardColor' (nicht-nullable) deklarierst,
    // musst du sicherstellen, dass der Konverter für group1 nie null liefert
    // ODER du deklarierst es als nullable und machst eine Prüfung.
    // Sicherer ist, es hier als nullable zu deklarieren, um dem Konverter-Output zu entsprechen,
    // und die Nicht-Null-Logik woanders zu erzwingen (z.B. bei der Erstellung des Objekts
    // bevor es in die DB kommt, oder durch einen spezifischeren NonNull-Konverter für group1).
    // Fürs Erste, um den Room-Fehler zu beheben:
    @ColumnInfo(name = "group1")
    val group1: CardColor?, // Akzeptiert den Output von Converters.toCardColor

    @ColumnInfo(name = "group2")
    val group2: CardColor?, // Ist sowieso nullable

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
    val bonusCard: String?, // Ist nullable

    @ColumnInfo(name = "bonus")
    val bonus: Int,

    @ColumnInfo(name = "isBuyable")
    val isBuyable: Boolean,

    @ColumnInfo(name = "currentPrice")
    val currentPrice: Int
) {
    // Wichtig: Wenn group1 logisch NIE null sein darf, aber hier als CardColor? deklariert ist,
    // um mit dem generischen Konverter kompatibel zu sein, solltest du an der Stelle,
    // wo Card-Objekte *erstellt* werden (z.B. beim Parsen der XML),
    // sicherstellen, dass group1 immer einen Wert bekommt oder einen Fehler werfen.
    // Für Room ist es so aber erstmal "sicher" zu lesen.
}