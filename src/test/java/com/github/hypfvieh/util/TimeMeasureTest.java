package com.github.hypfvieh.util;

import org.junit.Assert;
import org.junit.Test;

import com.github.hypfvieh.AbstractBaseUtilTest;

public class TimeMeasureTest extends AbstractBaseUtilTest {

    @Test
    public void testTimeMeasure() {
        TimeMeasure tm = new TimeMeasure();
        Assert.assertTrue(tm.getElapsed() >= 0);
        try {
            Thread.sleep(100L);
        } catch (InterruptedException _ex) {
            Assert.assertTrue(true);
        }
        Assert.assertTrue(tm.getElapsed() >= 100);
        Assert.assertTrue("toString() returned " + tm, tm.toString().matches("^[0-9]+ms$"));

        tm.reset();
        long elapsed = tm.getElapsed();
        Assert.assertTrue(elapsed < 10);

        tm.startTm = tm.getStartTime() - 10000;
        elapsed = tm.getElapsed();
        Assert.assertTrue("Elapsed was " + elapsed, elapsed >= 10000);
        Assert.assertTrue("toString() returned " + tm, tm.toString().matches("^[0-9]+\\.[0-9]s$"));
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
