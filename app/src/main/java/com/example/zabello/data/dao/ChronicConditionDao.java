package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.ChronicCondition;

import java.util.List;

@Dao
public interface ChronicConditionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ChronicCondition item);

    @Update
    int update(ChronicCondition item);

    @Query("DELETE FROM chronic_conditions WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM chronic_conditions WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<ChronicCondition>> getByUser(long userId);
}
