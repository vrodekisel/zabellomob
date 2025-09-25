package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.AlertRule;

import java.util.List;

@Dao
public interface AlertRuleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(AlertRule rule);

    @Update
    int update(AlertRule rule);

    @Query("DELETE FROM alert_rules WHERE id = :id")
    int deleteById(long id);

    // Живые данные для UI (глобальные правила + правила пользователя)
    @Query("SELECT * FROM alert_rules WHERE userId IS NULL OR userId = :userId")
    LiveData<List<AlertRule>> getForUser(Long userId);

    // Синхронно для фоновых задач/репозитория
    @Query("SELECT * FROM alert_rules WHERE userId IS NULL OR userId = :userId")
    List<AlertRule> getForUserSync(Long userId);
}
