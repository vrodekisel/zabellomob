package com.example.zabello.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Единичная запись значения параметра для пользователя. */
@Entity(
        tableName = "parameter_entries",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ParameterType.class,
                        parentColumns = "id",
                        childColumns = "typeId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("typeId"),
                @Index("timestamp")
        }
)
public class ParameterEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Владелец записи. */
    public long userId;

    /** Тип параметра. */
    public long typeId;

    /** Значение. */
    public float value;

    /** Время измерения. */
    public java.util.Date timestamp = new java.util.Date();

    /** Комментарий/метка (опционально). */
    public String note;
}
