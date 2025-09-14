package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Update
    int update(User user);

    @Query("DELETE FROM users WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM users ORDER BY id ASC")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getById(long id);

    @Query("SELECT * FROM users WHERE login = :login AND passwordHash = :passwordHash LIMIT 1")
    User signInSync(String login, String passwordHash);

    @Query("SELECT COUNT(*) FROM users WHERE login = :login")
    int countByLoginSync(String login);
}
