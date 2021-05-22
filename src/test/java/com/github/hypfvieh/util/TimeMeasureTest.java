package com.github.hypfvieh.util;

import org.junit.jupiter.api.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class TimeMeasureTest extends AbstractBaseUtilTest {

    @Test
    public void testTimeMeasure() {
        TimeMeasure tm = new TimeMeasure();
        assertTrue(tm.getElapsed() >= 0);
        try {
            Thread.sleep(100L);
        } catch (InterruptedException _ex) {
            assertTrue(true);
        }
        assertTrue(tm.getElapsed() >= 100);
        assertTrue(tm.toString().matches("^[0-9]+ms$"), "toString() returned " + tm);

        tm.reset();
        long elapsed = tm.getElapsed();
        assertTrue(elapsed < 10);

        tm.setStartTm(tm.getStartTime() - 10000);
        elapsed = tm.getElapsed();
        assertTrue(elapsed >= 10000, "Elapsed was " + elapsed);
        assertTrue(tm.toString().matches("^[0-9]+\\.[0-9]s$"), "toString() returned " + tm);
    }

    @Test
    public void testTimeMeasureFormatter() {
        TimeMeasure tm = new TimeMeasure();

        String oneSecond = tm.getElapsedFormatted(null, 1000);
        assertEquals("00:00:01.000", oneSecond);

        String oneMinuteoneSecond = tm.getElapsedFormatted(null, 61000);
        assertEquals("00:01:01.000", oneMinuteoneSecond);

        String threeSecondsAfewMillis = tm.getElapsedFormatted(null, 3721);
        assertEquals("00:00:03.721", threeSecondsAfewMillis);

    }
}
