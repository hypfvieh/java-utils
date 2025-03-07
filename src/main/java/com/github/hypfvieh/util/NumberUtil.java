package com.github.hypfvieh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Helper class to convert different types of strings to double/BigDecimal.<br>
 * BigDecimals are usually created with a scale of {@link #BIGDECIMAL_DEFAULT_FRACTIONS}, if not specified otherwise.
 */
public final class NumberUtil {

    public static final int          BIGDECIMAL_DEFAULT_FRACTIONS = 15;

    public static final BigDecimal   NEGATIVE_ONE                 = new BigDecimal(-1);
    public static final BigDecimal   ONE_HUNDRED                  = new BigDecimal(100);

    private NumberUtil() {
        // hidden utility constructor
    }

    /**
     * Converts a {@link Number} to a {@link BigDecimal} using {@link #BIGDECIMAL_DEFAULT_FRACTIONS} maximum fraction digits.
     *
     * @param _num number to convert
     *
     * @return BigDecimal or null if input was null
     */
    public static BigDecimal convertNumberToBigDecimal(Number _num) {
        return convertNumberToBigDecimal(_num, BIGDECIMAL_DEFAULT_FRACTIONS);
    }

    /**
     * Converts a {@link Number} to a {@link BigDecimal} using given fraction digits as scale.
     *
     * @param _num number to convert
     *
     * @return BigDecimal or null if input was null
     */
    public static BigDecimal convertNumberToBigDecimal(Number _num, int _fractionDigits) {
        if (_num == null) {
            return null;
        }

        return new BigDecimal(_num.toString()).setScale(_fractionDigits, RoundingMode.HALF_UP);
    }

    /**
     * Compares v1 vs. v2 and will return true if v1 is smaller than v2.
     * <br>
     * This comparison ignores the scale of the {@link BigDecimal}.
     * <br>
     * Will return false if any (or both) parameter is null
     *
     * @param _v1 first value
     * @param _v2 second value
     * @return true if first value smaller than second
     */
    public static boolean isLess(BigDecimal _v1, BigDecimal _v2) {
        if (_v1 == null || _v2 == null) {
            return false;
        }

        return _v1.compareTo(_v2) == -1;
    }

    /**
     * Compares v1 vs. v2 and will return true if v1 is bigger than v2.
     * <br>
     * This comparison ignores the scale of the {@link BigDecimal}.
     * <br>
     * Will return false if any (or both) parameter is null
     *
     * @param _v1 first value
     * @param _v2 second value
     * @return true if first value bigger than second
     */
    public static boolean isGreater(BigDecimal _v1, BigDecimal _v2) {
        if (_v1 == null || _v2 == null) {
            return false;
        }

        return _v1.compareTo(_v2) == 1;
    }

    /**
     * Compares v1 vs. v2 and will return true if they are equal.
     * <br>
     * This comparison ignores the scale of the {@link BigDecimal},
     * which will be done when {@link BigDecimal#equals(Object)} is used.
     * <br>
     * Will return false if any (or both) parameter is null
     *
     * @param _v1 first value
     * @param _v2 second value
     *
     * @return true if equal
     */
    public static boolean isEqual(BigDecimal _v1, BigDecimal _v2) {
        if (_v1 == null || _v2 == null) {
            return false;
        }

        return _v1.compareTo(_v2) == 0;
    }

    /**
     * Checks if the given BigDecimal is zero.
     * This comparison ignores the scale of the {@link BigDecimal}.
     * Will return false if the input value is null.
     *
     * @param _val the value to check
     * @return true if the value is zero, false otherwise
     */
    public static boolean isZero(BigDecimal _val) {
        return isEqual(_val, BigDecimal.ZERO);
    }

    /**
     * Compares v1 vs. v2 and will return true if they are equal or v1 is bigger than v2.
     * <br>
     * This comparison ignores the scale of the {@link BigDecimal},
     * which will be done when {@link BigDecimal#equals(Object)} is used.
     * <br>
     * Will return false if any (or both) parameter is null
     *
     * @param _v1 first value
     * @param _v2 second value
     *
     * @return true if bigger or equal
     */
    public static boolean isGreaterOrEqual(BigDecimal _v1, BigDecimal _v2) {
        if (_v1 == null || _v2 == null) {
            return false;
        }

        return isGreater(_v1, _v2) || isEqual(_v1, _v2);
    }

    /**
     * Compares v1 vs. v2 and will return true if they are equal or v1 is smaller than v2.
     * <br>
     * This comparison ignores the scale of the {@link BigDecimal},
     * which will be done when {@link BigDecimal#equals(Object)} is used.
     * <br>
     * Will return false if any (or both) parameter is null
     *
     * @param _v1 first value
     * @param _v2 second value
     *
     * @return true if smaller or equal
     */
    public static boolean isLessOrEqual(BigDecimal _v1, BigDecimal _v2) {
        if (_v1 == null || _v2 == null) {
            return false;
        }

        return isLess(_v1, _v2) || isEqual(_v1, _v2);
    }

    /**
     * Checks if given BigDecimal is negative.
     *
     * @param _bd value
     * @return true if negative
     */
    public static boolean isNegative(BigDecimal _bd) {
        if (_bd == null) {
            return false;
        }

        return _bd.signum() == -1;
    }

    /**
     * Inverts a {@link BigDecimal}.
     * This means, if given value was negative, the positive
     * representation is returned and vice versa.
     *
     * @param _bd value
     * @return inverted BigDecimal or null if input was null
     */
    public static BigDecimal invert(BigDecimal _bd) {
        if (_bd == null) {
            return null;
        }

        return _bd.multiply(NEGATIVE_ONE);
    }

    /**
     * Sums the given {@link BigDecimal}s.
     * Will return null if any of the given objects is null.
     *
     * @param _add var args containing BigDecimals to sum
     *
     * @return BigDecimal, BigDecimal.ZERO when no or null input is given
     */
    public static BigDecimal sumBigDecimals(BigDecimal... _add) {
        if (_add == null || _add.length == 0) {
            return BigDecimal.ZERO;
        }

        return sumBigDecimals(Arrays.asList(_add));
    }

    /**
     * Sums the given collection of {@link BigDecimal}s.<br>
     * Will ignore any null value in collection.<br>
     * When a null collection is given, zero is returned.
     *
     * @param _add collection of bigdecimals
     *
     * @return BigDecimal, never null
     */
    public static BigDecimal sumBigDecimals(Collection<BigDecimal> _add) {
        return _add == null ? BigDecimal.ZERO : _add.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

