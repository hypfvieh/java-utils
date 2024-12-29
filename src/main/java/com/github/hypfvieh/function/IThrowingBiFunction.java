package com.github.hypfvieh.function;

/**
 * Function which allows throwing any exception.
 *
 * @param <V1> first type of value
 * @param <V2> second type of value
 * @param <R> type of result
 * @param <T> type of exception which gets thrown
 *
 * @author hypfvieh
 * @since v1.2.1 - 2024-12-26
 */
@FunctionalInterface
public interface IThrowingBiFunction<V1, V2, R, T extends Throwable> {
    /**
     * Returns the result of the function or throws an exception.
     *
     * @return result of supplied function
     * @throws T exception
     */
    R apply(V1 _val1, V2 _val2) throws T;
}
