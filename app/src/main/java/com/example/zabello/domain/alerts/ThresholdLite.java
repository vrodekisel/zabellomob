package com.example.zabello.domain.alerts;

public final class ThresholdLite {
    public enum Status { LOW, NORMAL, HIGH }
    private ThresholdLite(){}

    public static Status eval(double value, Double min, Double max) {
        if (min != null && value < min) return Status.LOW;
        if (max != null && value > max) return Status.HIGH;
        return Status.NORMAL;
    }
}
