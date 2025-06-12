package org.tesira.civic.db

import androidx.room.TypeConverter
import java.util.Locale

object Converters {
    @TypeConverter
    fun toCardColor(value: String?): CardColor? {
        if (value == null) {
            return null
        }
        try {
            return CardColor.valueOf(value.uppercase(Locale.getDefault()))
        } catch (_: IllegalArgumentException) {
            System.err.println("Ungültiger CardColor-String aus DB: '$value' - wird zu null konvertiert.")
            return null
        }
    }

    @TypeConverter
    fun fromCardColor(color: CardColor?): String? {
        return color?.name
    }
}