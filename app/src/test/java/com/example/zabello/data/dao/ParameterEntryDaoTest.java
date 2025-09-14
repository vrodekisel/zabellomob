package com.example.zabello.data.dao;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.ParameterEntry;
import com.example.zabello.data.entity.ParameterType;
import com.example.zabello.data.entity.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParameterEntryDaoTest {

    private AppDatabase db;
    private ParameterTypeDao typeDao;
    private ParameterEntryDao entryDao;
    private UserDao userDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        typeDao = db.parameterTypeDao();
        entryDao = db.parameterEntryDao();
        userDao = db.userDao();
    }

    @After
    public void closeDb() { db.close(); }

    @Test
    public void insert_and_query_entries() {
        User u = new User();
        u.login = "u1"; u.passwordHash = "p";
        long userId = userDao.insert(u);

        ParameterType t = new ParameterType();
        t.code = "HR"; t.title = "Пульс"; t.unit = "bpm";
        long typeId = typeDao.insert(t);

        ParameterEntry e = new ParameterEntry();
        e.userId = userId;
        e.typeId = typeId;
        e.value = 72f;
        long entryId = entryDao.insert(e);

        // Простейшая проверка количества записей
        assertEquals(1, db.getOpenHelper().getWritableDatabase()
                .query("SELECT COUNT(*) FROM parameter_entries").getLong(0));
    }
}
