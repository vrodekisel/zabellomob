package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.ParameterType;

import java.util.List;

@Dao
public interface ParameterTypeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ParameterType type);

    @Update
    int update(ParameterType type);

    @Query("DELETE FROM parameter_types WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM parameter_types ORDER BY title ASC")
    LiveData<List<ParameterType>> getAll();

    @Query("SELECT * FROM parameter_types WHERE id = :id LIMIT 1")
    LiveData<ParameterType> getById(long id);

    @Query("SELECT * FROM parameter_types WHERE code = :code LIMIT 1")
    ParameterType findByCodeSync(String code);
}
