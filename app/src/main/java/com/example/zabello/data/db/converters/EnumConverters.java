package com.example.zabello.data.db.converters;

import androidx.room.TypeConverter;
import com.example.zabello.data.entity.Gender;

public class EnumConverters {
    @TypeConverter
    public static String fromGender(Gender g) {
        return g == null ? null : g.name();
    }
    @TypeConverter
    public static Gender toGender(String name) {
        if (name == null) return null;
        try {
            return Gender.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Gender.OTHER;
        }
    }
}
