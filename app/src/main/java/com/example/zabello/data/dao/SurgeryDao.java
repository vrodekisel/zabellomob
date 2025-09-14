package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.Surgery;

import java.util.List;

@Dao
public interface SurgeryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Surgery item);

    @Update
    int update(Surgery item);

    @Query("DELETE FROM surgeries WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM surgeries WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Surgery>> getByUser(long userId);
}
