package com.github.hypfvieh.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utility functions to work with {@link LocalDate}, {@link LocalTime} and {@link LocalDateTime}.
 *
 * @author hypfvieh
 * @since v1.2.1 - 2024-09-20
 */
public final class DateUtil {

    private DateUtil() {}

    /**
     * Checks if the first date is before or equal to the second date.
     *
     * @param _first first date
     * @param _second second date
     *
     * @return false if any null or second date after first date
     */
    public static boolean isBeforeEqual(LocalDate _first, LocalDate _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isBefore(_second);
    }


    /**
     * Checks if the first date is after or equal to the second date.
     *
     * @param _first first date
     * @param _second second date
     *
     * @return false if any null or second date before first date
     */
    public static boolean isAfterEqual(LocalDate _first, LocalDate _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isAfter(_second);
    }

    /**
     * Null-safe equal comparison of two LocalDate objects.
     *
     * @param _first first date
     * @param _second second date
     * @return true if equal
     */
    public static boolean isEqual(LocalDate _first, LocalDate _second) {
        if (_first == _second) {
            return true;
        }

        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second);
    }


    /**
     * Checks if the first timestamp is before or equal to the second timestamp.
     *
     * @param _first first timestamp
     * @param _second second timestamp
     *
     * @return false if any null or second timestamp after first timestamp
     */
    public static boolean isBeforeEqual(LocalDateTime _first, LocalDateTime _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isBefore(_second);
    }


    /**
     * Checks if the first timestamp is after or equal to the second timestamp.
     *
     * @param _first first timestamp
     * @param _second second timestamp
     *
     * @return false if any null or second timestamp before first timestamp
     */
    public static boolean isAfterEqual(LocalDateTime _first, LocalDateTime _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isAfter(_second);
    }

    /**
     * Null-safe equal comparison of two LocalDateTime objects.
     *
     * @param _first first datetime
     * @param _second second datetime
     * @return true if equal
     */
    public static boolean isEqual(LocalDateTime _first, LocalDateTime _second) {
        if (_first == _second) {
            return true;
        }

        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second);
    }


    /**
     * Checks if the first time is before or equal to the second time.
     *
     * @param _first first time
     * @param _second second time
     *
     * @return false if any null or second time after first time
     */
    public static boolean isBeforeEqual(LocalTime _first, LocalTime _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isBefore(_second);
    }


    /**
     * Checks if the first time is after or equal to the second time.
     *
     * @param _first first time
     * @param _second second time
     *
     * @return false if any null or second time before first time
     */
    public static boolean isAfterEqual(LocalTime _first, LocalTime _second) {
        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second) || _first.isAfter(_second);
    }

    /**
     * Null-safe equal comparison of two LocalTime objects.
     *
     * @param _first first time
     * @param _second second time
     * @return true if equal
     */
    public static boolean isEqual(LocalTime _first, LocalTime _second) {
        if (_first == _second) {
            return true;
        }

        if (_first == null || _second == null) {
            return false;
        }

        return _first.equals(_second);
    }

    /**
     * Check if date is in range.
     *
     * @param _start range start date
     * @param _end range end date
     * @param _test date to check
     * @return true if in range
     */
    public static boolean isDateBetween(LocalDate _start, LocalDate _end, LocalDate _test) {
        return (_test.isBefore(_end) || _test.isEqual(_end)) && (_test.isAfter(_start) || _test.isEqual(_start));
    }
}
