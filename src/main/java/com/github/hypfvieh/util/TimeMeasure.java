package com.github.hypfvieh.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for time measurements.
 * Instances may be reset for reuse.
 */
public class TimeMeasure {

    /** Start time in milliseconds. */
    //CHECKSTYLE:OFF
    volatile long startTm;
    //CHECKSTYLE:ON

    public TimeMeasure() {
        reset();
    }

    /**
     * Resets the start time to current time in milliseconds.
     * @return the object
     */
    public final TimeMeasure reset() {
        this.startTm = System.currentTimeMillis();
        return this;
    }

    /**
     * Returns the start time in milliseconds.
     * @return start time in ms
     */
    public long getStartTime() {
        return startTm;
    }

    /**
     * Returns the elapsed time in milliseconds.
     * @return elapsed time in ms
     */
    public long getElapsed() {
        return System.currentTimeMillis() - startTm;
    }

    /**
     * Formats the elapsed time using the given dateFormatter.
     * If null is given, a new Formatter with format HH:mm:ss.SSS will be used.
     *
     * The timezone of the given dateFormatter will always be set to 'UTC' to avoid any timezone related offsets.
     *
     * @param _dateFormat
     * @return formatted string
     */
    public String getElapsedFormatted(DateFormat _dateFormat) {
        return getElapsedFormatted(_dateFormat, getElapsed());
    }

    /**
     * Same as above, used for proper unit testing.
     * @param _dateFormat
     * @param _elapsedTime
     * @return formatted string
     */
    String getElapsedFormatted(DateFormat _dateFormat, long _elapsedTime) {
        Date elapsedTime = new Date(_elapsedTime);

        DateFormat sdf = _dateFormat;
        if (_dateFormat == null) {
            sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        }
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // always use UTC so we get the correct time without timezone offset on it

        return sdf.format(elapsedTime);
    }

    public long getElapsedAndReset() {
        long elapsed = getElapsed();
        reset();
        return elapsed;
    }

    /**
     * Returns the elapsed time in milliseconds formatted as string.
     * @return elapsed time in ms
     */
    @Override
    public String toString() {
        long elapsed = getElapsed();
        return elapsed >= 5000 ? ((long) ((elapsed / 1000d) * 10) / 10d) + "s" : elapsed + "ms";
    }

}
