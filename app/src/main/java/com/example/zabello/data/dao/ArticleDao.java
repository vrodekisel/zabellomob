package com.example.zabello.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zabello.data.entity.Article;

import java.util.List;

@Dao
public interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Article article);

    @Update
    int update(Article article);

    @Query("DELETE FROM articles WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    LiveData<List<Article>> getAll();

    @Query("SELECT * FROM articles WHERE slug = :slug LIMIT 1")
    LiveData<Article> getBySlug(String slug);

    // --- Новое: upsert-методы для кэша из удалённого источника ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsert(Article article);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> upsertAll(List<Article> articles);
}
