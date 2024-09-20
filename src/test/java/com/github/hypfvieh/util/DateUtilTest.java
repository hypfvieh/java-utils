package com.github.hypfvieh.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.stream.Stream;

class DateUtilTest {

    @Test
    void testIsDateInRange() {
        LocalDate startRange = LocalDate.of(2020, 9, 28);
        LocalDate endRange = LocalDate.of(2020, 10, 2);

        assertTrue(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 9, 28)));
        assertTrue(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 9, 29)));
        assertTrue(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 9, 30)));
        assertTrue(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 10, 1)));
        assertTrue(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 10, 2)));

        assertTrue(
            DateUtil.isDateBetween(LocalDate.of(2022, 5, 3), LocalDate.of(2022, 5, 3), LocalDate.of(2022, 5, 3)));

        assertFalse(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 9, 27)));
        assertFalse(DateUtil.isDateBetween(startRange, endRange, LocalDate.of(2020, 10, 3)));
    }

    @DisplayName("Test LocalTime After/Equal")
    @ParameterizedTest
    @MethodSource("createLocalTimeTestData")
    void testTimeAfterEqual(DateTestData<LocalTime> _data) {
        assertEquals(_data.isResultAfter(), DateUtil.isAfterEqual(_data.getFirst(), _data.getSecond()));
    }

    @DisplayName("Test LocalDate After/Equal")
    @ParameterizedTest
    @MethodSource("createLocalDateTestData")
    void testDateAfterEqual(DateTestData<LocalDate> _data) {
        assertEquals(_data.isResultAfter(), DateUtil.isAfterEqual(_data.getFirst(), _data.getSecond()));
    }

    @DisplayName("Test LocalDateTime After/Equal")
    @ParameterizedTest
    @MethodSource("createLocalDateTimeTestData")
    void testDateTimeAfterEqual(DateTestData<LocalDateTime> _data) {
        assertEquals(_data.isResultAfter(), DateUtil.isAfterEqual(_data.getFirst(), _data.getSecond()));
    }

    @DisplayName("Test LocalTime Before/Equal")
    @ParameterizedTest
    @MethodSource("createLocalTimeTestData")
    void testTimeBeforeEqual(DateTestData<LocalTime> _data) {
        assertEquals(_data.isResultBefore(), DateUtil.isBeforeEqual(_data.getFirst(), _data.getSecond()));
    }

    @DisplayName("Test LocalDate Before/Equal")
    @ParameterizedTest
    @MethodSource("createLocalDateTestData")
    void testDateBeforeEqual(DateTestData<LocalDate> _data) {
        assertEquals(_data.isResultBefore(), DateUtil.isBeforeEqual(_data.getFirst(), _data.getSecond()));
    }

    @DisplayName("Test LocalDateTime Before/Equal")
    @ParameterizedTest
    @MethodSource("createLocalDateTimeTestData")
    void testDateTimeBeforeEqual(DateTestData<LocalDateTime> _data) {
        assertEquals(_data.isResultBefore(), DateUtil.isBeforeEqual(_data.getFirst(), _data.getSecond()));
    }

    static Stream<DateTestData<LocalTime>> createLocalTimeTestData() {
        return Stream.of(
            new DateTestData<>(LocalTime.of(11, 11, 11), LocalTime.of(11, 11, 11), true, true),
            new DateTestData<>(LocalTime.of(10, 11, 11), LocalTime.of(11, 11, 11), true, false),
            new DateTestData<>(LocalTime.of(11, 11, 10), LocalTime.of(11, 11, 11), true, false),
            new DateTestData<>(LocalTime.of(12, 11, 10), LocalTime.of(11, 11, 11), false, true),
            new DateTestData<>(LocalTime.of(11, 11, 12), LocalTime.of(11, 11, 11), false, true)
        );
    }

    static Stream<DateTestData<LocalDate>> createLocalDateTestData() {
        return Stream.of(
            new DateTestData<>(LocalDate.of(2023, 11, 11), LocalDate.of(2023, 11, 11), true, true),
            new DateTestData<>(LocalDate.of(2023, 10, 11), LocalDate.of(2023, 11, 11), true, false),
            new DateTestData<>(LocalDate.of(2023, 11, 10), LocalDate.of(2023, 11, 11), true, false),
            new DateTestData<>(LocalDate.of(2024, 11, 11), LocalDate.of(2023, 11, 11), false, true),
            new DateTestData<>(LocalDate.of(2023, 11, 12), LocalDate.of(2023, 11, 11), false, true)
        );
    }

    static Stream<DateTestData<LocalDateTime>> createLocalDateTimeTestData() {
        return Stream.of(
            new DateTestData<>(LocalDateTime.of(2023, 11, 11, 1, 2, 3),
                LocalDateTime.of(2023, 11, 11, 1, 2, 3), true, true),
            new DateTestData<>(LocalDateTime.of(2023, 10, 11, 1, 2, 2),
                LocalDateTime.of(2023, 11, 11, 1, 2, 3), true, false),
            new DateTestData<>(LocalDateTime.of(2023, 11, 10, 1, 2, 3),
                LocalDateTime.of(2023, 11, 11, 1, 2, 3), true, false),
            new DateTestData<>(LocalDateTime.of(2024, 11, 11, 1, 2, 3),
                LocalDateTime.of(2023, 11, 11, 1, 0, 0), false, true),
            new DateTestData<>(LocalDateTime.of(2023, 11, 12, 11, 12, 13),
                LocalDateTime.of(2023, 11, 11, 1, 0, 0), false, true)
        );
    }

    static class DateTestData<T extends Temporal> {
        private final T first;
        private final T second;
        private final boolean resultBefore;
        private final boolean resultAfter;

        DateTestData(T _first, T _second, boolean _resultBefore, boolean _resultAfter) {
            this.first = _first;
            this.second = _second;
            this.resultBefore = _resultBefore;
            this.resultAfter = _resultAfter;
        }

        T getFirst() {
            return first;
        }

        T getSecond() {
            return second;
        }

        boolean isResultBefore() {
            return resultBefore;
        }

        boolean isResultAfter() {
            return resultAfter;
        }

        @Override
        public String toString() {
            return "[first=" + first + ", second=" + second + ", resultBefore=" + resultBefore + ", resultAfter=" + resultAfter + "]";
        }


    }

}
