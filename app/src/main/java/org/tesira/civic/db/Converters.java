package org.tesira.civic.db;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import org.tesira.civic.db.CardColor;

public class Converters {

    @TypeConverter
    @Nullable
    public static CardColor toCardColor(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return CardColor.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Ungültiger CardColor-String aus DB: '" + value + "' - wird zu null konvertiert.");
            return null;
        }
    }

    @TypeConverter
    @Nullable
    public static String fromCardColor(@Nullable CardColor color) { // Das Eingabe-Enum kann null sein
        return color == null ? null : color.name();
    }

}