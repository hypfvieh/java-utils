package com.github.hypfvieh.util;

import com.github.hypfvieh.AbstractBaseUtilTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

class TimeMeasureTest extends AbstractBaseUtilTest {

    @Test
    void testTimeMeasure() throws InterruptedException {
        TimeMeasure tm = new TimeMeasure();
        assertTrue(tm.getElapsedMillis() >= 0);
        Thread.sleep(100L);
        assertTrue(tm.getElapsedMillis() >= 100);
        String str = tm.toString();
        assertPatternMatches(str, "^[0-9\\.,]+ ms$");

        tm.reset();
        long elapsed = tm.getElapsedMillis();
        assertTrue(elapsed < 10);

        tm = new TimeMeasure(System.nanoTime() - TimeUnit.SECONDS.toNanos(10));
        elapsed = tm.getElapsedMillis();
        assertTrue(elapsed >= 10000);
        assertPatternMatches(tm.toString(), "^[0-9\\.]+ seconds$");
    }

    @Test
    void testGetElapsed() {
        long startNanos = System.nanoTime() - TimeUnit.SECONDS.toNanos(10);
        TimeMeasure tm = new TimeMeasure(startNanos);
        assertEquals(10, tm.getElapsedSeconds());
    }

    @Test
    void testGetElapsedMinutes() {
        long startNanos = System.nanoTime() - TimeUnit.MINUTES.toNanos(20) - TimeUnit.SECONDS.toNanos(15);
        TimeMeasure tm = new TimeMeasure(startNanos);
        assertEquals(20.25d, tm.getElapsedMinutes(), 0.00000001d);
    }

    @ParameterizedTest(name = "[{index}] {0} -> \"{1}\"")
    @CsvSource(delimiterString = ";", value = {
            "-1; '-1,00 ms'",
            "0; 0,00 ms",
            "0.165; 0,17 ms",
            "0.5; 0,50 ms",
            "1000; 1000 ms",
            "1001; 1001 ms",
            "3721; 3721 ms",
            "4999; 4999 ms",
            "5000; 5000 ms",
            "5001; 5001 ms",
            "61000; 61 seconds",
            "180000; 180 seconds",
            "3600000; 01:00:00"
    })
    void testFormatDuration(double _elapsed, String _expected) {
        assertEquals(_expected, TimeMeasure.formatDuration(_elapsed, Locale.GERMAN));
    }
}
