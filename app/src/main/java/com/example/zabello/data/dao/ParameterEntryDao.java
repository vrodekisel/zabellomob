package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.ParameterEntry;

import java.util.List;

@Dao
public interface ParameterEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ParameterEntry entry);

    @Update
    int update(ParameterEntry entry);

    @Query("DELETE FROM parameter_entries WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM parameter_entries WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<ParameterEntry>> getByUser(long userId);

    @Query("SELECT * FROM parameter_entries WHERE userId = :userId AND typeId = :typeId ORDER BY timestamp DESC")
    LiveData<List<ParameterEntry>> getByUserAndType(long userId, long typeId);

    @Query("SELECT * FROM parameter_entries WHERE id = :id LIMIT 1")
    LiveData<ParameterEntry> getById(long id);
}
