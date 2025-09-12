package com.example.zabello.data.db.converters;

import androidx.room.TypeConverter;

public class DateConverters {
    @TypeConverter
    public static Long fromDate(java.util.Date value) {
        return value == null ? null : value.getTime(); // millis -> Long
    }

    @TypeConverter
    public static java.util.Date toDate(Long value) {
        return value == null ? null : new java.util.Date(value); // Long -> Date
    }
}
