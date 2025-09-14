package com.example.zabello.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Справочник измеряемых параметров (давление, пульс и т.п.). */
@Entity(
        tableName = "parameter_types",
        indices = {
                @Index(value = {"code"}, unique = true),
                @Index(value = {"title"})
        }
)
public class ParameterType {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Уникальный код типа параметра (напр., BP_SYS, HR). */
    @NonNull
    public String code = "";

    /** Человекочитаемое название. */
    @NonNull
    public String title = "";

    /** Единицы измерения (например, мм рт. ст., bpm). */
    public String unit;

    /** Нижняя граница нормы (опционально). */
    public Float minNormal;

    /** Верхняя граница нормы (опционально). */
    public Float maxNormal;

    /** Доп. описание. */
    public String description;
}
