package com.example.zabello.utils;

import android.text.TextUtils;

import com.example.zabello.data.entity.ParameterType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/** Универсальные проверки ввода и утилиты. */
public class ValidationLogic {

    /** Проверка логина: минимум 3 символа. */
    public static boolean isValidLogin(String login) {
        return login != null && login.trim().length() >= 3;
    }

    /** Проверка пароля: минимум 6 символов. */
    public static boolean isValidPassword(String pass) {
        return pass != null && pass.length() >= 6;
    }

    /** Хеширование пароля алгоритмом SHA-256. */
    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Проверка, что число в допустимом диапазоне. */
    public static boolean isValidNumber(Float value, Float min, Float max) {
        return value != null && (min == null || value >= min) && (max == null || value <= max);
    }

    /** Проверка даты: timestamp положительный и не дальше года вперёд. */
    public static boolean isValidDate(long timestamp) {
        return timestamp > 0 && timestamp < System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365;
    }

    /** Проверка единицы измерения: содержится ли в списке допустимых. */
    public static boolean isValidUnit(String unit, String... allowed) {
        if (unit == null) return false;
        for (String a : allowed) {
            if (unit.equalsIgnoreCase(a)) return true;
        }
        return false;
    }

    /** Парсинг числа с учётом локали. Возвращает null, если строка пустая или не число. */
    public static Float tryParseFloat(String s) {
        if (TextUtils.isEmpty(s)) return null;
        String trimmed = s.trim();
        try {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
            Number n = nf.parse(trimmed);
            return n != null ? n.floatValue() : null;
        } catch (ParseException ignore) {
            try {
                return Float.parseFloat(trimmed.replace(',', '.'));
            } catch (Exception e) {
                return null;
            }
        }
    }

    /** Проверка значения на вменяемость и попадание в диапазон типа (если задан). */
    public static @androidx.annotation.Nullable String validateValueForType(
            ParameterType type,
            Float value,
            String requiredMsg,
            String numberMsg,
            String rangeMsgTemplate
    ) {
        if (value == null) return requiredMsg;
        if (value.isNaN() || value.isInfinite()) return numberMsg;

        if (type != null) {
            Float min = type.minNormal;
            Float max = type.maxNormal;
            if (min != null && value < min) {
                if (!TextUtils.isEmpty(rangeMsgTemplate))
                    return String.format(Locale.getDefault(), rangeMsgTemplate, min, max);
                return numberMsg;
            }
            if (max != null && value > max) {
                if (!TextUtils.isEmpty(rangeMsgTemplate))
                    return String.format(Locale.getDefault(), rangeMsgTemplate, min, max);
                return numberMsg;
            }
        }
        return null; // всё ок
    }
}
