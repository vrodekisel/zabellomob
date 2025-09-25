package com.example.zabello.domain.alerts;

import androidx.annotation.Nullable;

import com.example.zabello.data.entity.AlertRule;
import com.example.zabello.data.entity.ParameterType;

import java.util.List;

/** Оценка значения по пороговым правилам и встроенному диапазону типа. */
public class ThresholdEvaluator {

    public static class Result {
        public final boolean anomalous;
        @Nullable public final AlertRule triggeredRule;
        public Result(boolean anomalous, @Nullable AlertRule rule) {
            this.anomalous = anomalous;
            this.triggeredRule = rule;
        }
    }

    /** true, если значение выходит за min/max включённых правил; иначе fallback к границам типа. */
    public static Result evaluate(float value, ParameterType type, @Nullable List<AlertRule> rulesForUserAndGlobal) {
        if (rulesForUserAndGlobal != null) {
            for (AlertRule r : rulesForUserAndGlobal) {
                if (!r.enabled) continue;
                if (r.parameterTypeId != type.id) continue;
                boolean lowBad = r.lowThreshold != null && value < r.lowThreshold;
                boolean highBad = r.highThreshold != null && value > r.highThreshold;
                if (lowBad || highBad) return new Result(true, r);
            }
        }
        if (type != null) {
            boolean lowBad = type.minNormal != null && value < type.minNormal;
            boolean highBad = type.maxNormal != null && value > type.maxNormal;
            if (lowBad || highBad) return new Result(true, null);
        }
        return new Result(false, null);
    }
}
