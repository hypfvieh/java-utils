package com.github.hypfvieh.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for converting types (e.g. String to Integer).
 *
 * @author hypfvieh
 * @since v1.0 - 2016-07-08
 */
public final class ConverterUtil {

    private ConverterUtil() {

    }


    /**
     * Convert given String to Integer, returns _default if String is not an integer.
     * @param _str
     * @param _default
     * @return _str as Integer or _default value
     */
    public static Integer strToInt(String _str, Integer _default) {
        if (_str == null) {
            return _default;
        }
        if (TypeUtil.isInteger(_str, true)) {
            return Integer.valueOf(_str);
        } else {
            return _default;
        }
    }

    /**
     * Convert given String to Integer, returns null if String is not an integer.
     * @param _str
     * @return _str as Integer or null
     */
    public static Integer strToInt(String _str) {
        return strToInt(_str, null);
    }

    /**
     * Returns true if string matches certain boolean values.
     *
     * @param _str
     * @return
     */
    public static boolean strToBool(String _str) {
        return (_str != null && _str.matches("(?i)^(1|y|j|ja|yes|true|enabled|enable|active)$"));
    }

    /**
     * Convertes a map to a Properties object.
     * @param _map
     * @return Properties object
     */
    public static Properties toProperties(Map<?, ?> _map) {
        Properties props = new Properties();
        props.putAll(_map);
        return props;
    }

    /**
     * Converts a {@link java.util.Date} to a {@link LocalDateTime} using the default timezone.
     *
     * @param _date date to convert
     * @return LocalDateTime or null if input null
     */
    public static LocalDateTime toLocalDateTime(Date _date) {
        if (_date == null) {
            return null;
        }
        return _date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a {@link java.util.Date} to a {@link LocalDate} using the default timezone.
     *
     * @param _date date to convert
     * @return LocalDate or null if input null
     */
    public static LocalDate toLocalDate(Date _date) {
        if (_date == null) {
            return null;
        }
        return _date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts a {@link java.util.Date} to a {@link LocalTime} using the default timezone.
     *
     * @param _date date to convert
     * @return LocalTime or null if input null
     */
    public static LocalTime toLocalTime(Date _date) {
        if (_date == null) {
            return null;
        }
        return _date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

}
