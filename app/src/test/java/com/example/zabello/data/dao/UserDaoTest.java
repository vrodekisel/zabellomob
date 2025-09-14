package com.example.zabello.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.zabello.data.db.AppDatabase;
import com.example.zabello.data.entity.Gender;
import com.example.zabello.data.entity.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserDaoTest {

    private AppDatabase db;
    private UserDao userDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = db.userDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void crud_user() {
        User u = new User();
        u.login = "test";
        u.passwordHash = "hash";
        u.fullName = "Test User";
        u.gender = Gender.OTHER;

        long id = userDao.insert(u);
        assertNotNull(id);

        int exists = userDao.countByLoginSync("test");
        assertEquals(1, exists);

        User signed = userDao.signInSync("test","hash");
        assertNotNull(signed);
        assertEquals("test", signed.login);

        signed.fullName = "Updated";
        int rows = userDao.update(signed);
        assertEquals(1, rows);

        rows = userDao.deleteById(signed.id);
        assertEquals(1, rows);

        exists = userDao.countByLoginSync("test");
        assertEquals(0, exists);
    }
}
