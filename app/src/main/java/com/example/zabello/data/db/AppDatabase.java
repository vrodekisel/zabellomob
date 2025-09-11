package com.example.zabello.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.zabello.data.dao.UserDao;
import com.example.zabello.data.db.converters.DateConverters;
import com.example.zabello.data.db.converters.EnumConverters;
import com.example.zabello.data.entity.User;

@Database(
        entities = { User.class },
        version = 1,
        exportSchema = true
)
@TypeConverters({ DateConverters.class, EnumConverters.class })
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "zabello.db"
                            )
                            // На этапе разработки удобно:
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
