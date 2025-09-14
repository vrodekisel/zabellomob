package com.example.zabello.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Перенесённые операции пользователя. */
@Entity(
        tableName = "surgeries",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("userId"), @Index("date") }
)
public class Surgery {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull
    public String name = "";

    public String notes;

    public java.util.Date date;
}
