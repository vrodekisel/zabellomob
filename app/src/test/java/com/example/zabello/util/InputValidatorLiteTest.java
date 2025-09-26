package com.example.zabello.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class InputValidatorLiteTest {
    @Test public void notEmpty_ok() {
        assertTrue(InputValidatorLite.notEmpty("x"));
    }
    @Test public void notEmpty_fail() {
        assertFalse(InputValidatorLite.notEmpty("  "));
    }
    @Test public void parseDouble_ok() {
        assertEquals(12.5, InputValidatorLite.parseDoubleOrNull("12.5"), 0.0001);
    }
    @Test public void parseDouble_nullOnError() {
        assertNull(InputValidatorLite.parseDoubleOrNull("abc"));
    }
    @Test public void range_checks() {
        assertTrue(InputValidatorLite.inRange(5.0, 1.0, 10.0));
        assertFalse(InputValidatorLite.inRange(0.9, 1.0, 10.0));
        assertFalse(InputValidatorLite.inRange(10.1, 1.0, 10.0));
        assertTrue(InputValidatorLite.inRange(5.0, null, 10.0));
        assertTrue(InputValidatorLite.inRange(5.0, 1.0, null));
    }
}
