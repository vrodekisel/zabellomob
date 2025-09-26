package com.example.zabello.util;

public final class InputValidatorLite {
    private InputValidatorLite(){}

    public static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Возвращает null, если не число. Удобно для мягкой проверки полей. */
    public static Double parseDoubleOrNull(String s) {
        try { return Double.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    /** min/max могут быть null => односторонняя граница. Возвращает true, если значение в пределах. */
    public static boolean inRange(Double v, Double min, Double max) {
        if (v == null) return false;
        if (min != null && v < min) return false;
        if (max != null && v > max) return false;
        return true;
    }
}
