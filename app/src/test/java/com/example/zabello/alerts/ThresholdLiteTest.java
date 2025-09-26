package com.example.zabello.alerts;

import com.example.zabello.domain.alerts.ThresholdLite;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThresholdLiteTest {
    @Test public void normal_insideBounds() {
        assertEquals(ThresholdLite.Status.NORMAL, ThresholdLite.eval(5, 3.0, 8.0));
    }
    @Test public void low_whenBelowMin() {
        assertEquals(ThresholdLite.Status.LOW, ThresholdLite.eval(2.5, 3.0, 8.0));
    }
    @Test public void high_whenAboveMax() {
        assertEquals(ThresholdLite.Status.HIGH, ThresholdLite.eval(8.5, 3.0, 8.0));
    }
    @Test public void singleBoundaries() {
        assertEquals(ThresholdLite.Status.NORMAL, ThresholdLite.eval(5, null, 10.0));
        assertEquals(ThresholdLite.Status.NORMAL, ThresholdLite.eval(5, 1.0, null));
    }
}
