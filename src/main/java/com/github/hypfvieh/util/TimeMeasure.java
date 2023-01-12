package com.github.hypfvieh.util;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for time measurements.<br>
 * Instances may be reset for reuse.
 */
public final class TimeMeasure {

    private static final long MILLIS_TO_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    /** Start time in nanoseconds. */
    private volatile long     startTime;

    /**
     * Default constructor with start time equals current time.
     */
    public TimeMeasure() {
        reset();
    }

    /**
     * Constructor with a start time (useful for unit testing).
     * @param _startTime start time in nanoseconds
     */
    TimeMeasure(long _startTime) {
        startTime = _startTime;
    }

    /**
     * Resets the start time to current time.
     *
     * @return the object
     */
    public TimeMeasure reset() {
        startTime = System.nanoTime();
        return this;
    }

    /**
     * Gets the elapsed time in milliseconds.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        return getElapsedNanos() / MILLIS_TO_NANOS;
    }

    /**
     * Gets the elapsed time in nanoseconds.
     *
     * @return elapsed time in nanoseconds
     */
    public long getElapsedNanos() {
        return System.nanoTime() - startTime;
    }

    /**
     * Gets the elapsed time in seconds.
     *
     * @return elapsed time in seconds
     */
    public long getElapsedSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(getElapsedNanos());
    }

    /**
     * Gets the elapsed time in fractional minutes.
     *
     * @return elapsed time in minutes
     */
    public double getElapsedMinutes() {
        return TimeUnit.NANOSECONDS.toSeconds(getElapsedNanos()) / 60d;
    }

    /**
     * Returns the elapsed time as a formatted, user-friendly string.
     *
     * @return formatted string
     */
    public String getElapsedFormatted() {
        return formatDuration((double) getElapsedNanos() / MILLIS_TO_NANOS);
    }

    /**
     * Gets the elapsed time in milliseconds and resets the instance.
     *
     * @return elapsed time
     */
    public long getElapsedAndReset() {
        long elapsed = getElapsedMillis();
        reset();
        return elapsed;
    }

    /**
     * Converts a fractional milliseconds duration to a user-friendly formatted string e.g. for logging.
     *
     * @param _millis elapsed time in milliseconds
     * @return formatted string
     */
    public static String formatDuration(double _millis) {
        return formatDuration(_millis, Locale.getDefault());
    }

    /**
     * Converts a fractional milliseconds duration to a user-friendly formatted string e.g. for logging.
     *
     * @param _millis elapsed time in milliseconds
     * @param _locale locale
     * @return formatted string
     */
    static String formatDuration(double _millis, Locale _locale) {
        if (_millis < 1000) {
            return String.format(_locale, "%.2f ms", _millis);
        }
        if (_millis < 10000) {
            return String.format(_locale, "%d ms", (long) _millis);
        }

        long seconds = (long) (_millis / 1000);
        if (seconds < 300) {
            return seconds + " seconds";
        }
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        if (minutes < 60) {
            return minutes + " minutes";
        }

        Duration dur = Duration.ofMillis((long) _millis);
        return String.format("%02d:%02d:%02d", dur.toHoursPart(), dur.toMinutesPart(), dur.toSecondsPart());
    }

    /**
     * Returns the elapsed time in milliseconds formatted as string by calling {@link #getElapsedFormatted()}.
     *
     * @return formatted elapsed time
     * @see #getElapsedFormatted()
     */
    @Override
    public String toString() {
        return getElapsedFormatted();
    }

}
