package com.example.zabello.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Хронические состояния пользователя. */
@Entity(
        tableName = "chronic_conditions",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = { @Index("userId"), @Index("name") }
)
public class ChronicCondition {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull
    public String name = "";

    public String notes;

    public java.util.Date diagnosedAt;
}
