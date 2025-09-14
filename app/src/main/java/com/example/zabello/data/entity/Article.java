package com.example.zabello.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Статья мед. справочника. */
@Entity(
        tableName = "articles",
        indices = { @Index(value = {"slug"}, unique = true), @Index("title") }
)
public class Article {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Уникальный slug для ссылок. */
    @NonNull
    public String slug = "";

    /** Заголовок. */
    @NonNull
    public String title = "";

    /** Текст статьи (может быть markdown/HTML). */
    @NonNull
    public String body = "";

    /** Теги через запятую (опционально). */
    public String tags;

    public java.util.Date createdAt = new java.util.Date();
}
