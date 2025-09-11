package com.example.zabello.data.db.converters;

import androidx.room.TypeConverter;
import java.util.Date;

public class DateConverters {
    @TypeConverter
    public static Long toLong(Date date) {
        return date == null ? null : date.getTime();
    }
    @TypeConverter
    public static Date toDate(Long millis) {
        return millis == null ? null : new Date(millis);
    }
}
