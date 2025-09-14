package com.example.zabello.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Правила алертов для параметров (пороговые значения). */
@Entity(
        tableName = "alert_rules",
        foreignKeys = {
                @ForeignKey(
                        entity = ParameterType.class,
                        parentColumns = "id",
                        childColumns = "parameterTypeId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = { @Index("parameterTypeId"), @Index("userId") }
)
public class AlertRule {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Если null — правило глобальное, иначе — для конкретного пользователя. */
    public Long userId;

    public long parameterTypeId;

    /** Нижний и верхний пороги (если null — порог не используется). */
    public Float lowThreshold;
    public Float highThreshold;

    /** Включено ли правило. */
    public boolean enabled = true;

    /** Текст уведомления (опционально). */
    public String message;
}
