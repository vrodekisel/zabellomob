package com.example.zabello.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Пользователь приложения. */
@Entity(
        tableName = "users",
        indices = {@Index(value = {"login"}, unique = true)}
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Уникальный логин (e-mail или произвольная строка). */
    @NonNull
    public String login = "";

    /** Хэш пароля; НЕ хранить пароль в открытом виде. */
    @NonNull
    public String passwordHash = "";

    /** Полное имя (для профиля/дашборда). */
    @NonNull
    public String fullName = "";

    /** Гендер (политкорректная форма). */
    public Gender gender = Gender.OTHER;

    /** Дата рождения (опционально). */
    public java.util.Date birthDate;

    /** Рост (см), опционально. */
    public Float heightCm;

    /** Вес (кг), опционально. */
    public Float weightKg;
}
