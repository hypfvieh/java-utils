package com.github.hypfvieh.function;

/**
 * Function which allows throwing any exception.
 *
 * @param <V> type of value
 * @param <R> type of result
 * @param <T> type of exception which gets thrown
 *
 * @author hypfvieh
 * @since v1.2.1 - 2024-12-26
 */
@FunctionalInterface
public interface IThrowingFunction<V, R, T extends Throwable> {
    /**
     * Returns the result of the function or throws an exception.
     *
     * @return result of supplied function
     * @throws T exception
     */
    R apply(V _val) throws T;
}
