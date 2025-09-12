package com.example.zabello.data.db.converters;

import androidx.room.TypeConverter;

import com.example.zabello.data.entity.Gender;

public class EnumConverters {
    @TypeConverter
    public static String fromGender(Gender g) {
        return g == null ? null : g.name(); // enum -> String
    }

    @TypeConverter
    public static Gender toGender(String s) {
        return s == null ? null : Gender.valueOf(s); // String -> enum
    }
}
