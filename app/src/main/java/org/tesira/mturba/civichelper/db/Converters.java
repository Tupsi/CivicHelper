package org.tesira.mturba.civichelper.db;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public CardColor toCardColor(String value) {
        return CardColor.valueOf(value);
    }

    @TypeConverter
    public String fromCardColor(CardColor value) {
        return value.getName();
    }
}
