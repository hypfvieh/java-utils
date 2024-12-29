package com.github.hypfvieh.function;

/**
 * Supplier which allows throwing any exception.
 *
 * @param <V> type which is supplied
 * @param <T> type of exception which gets thrown
 *
 * @author hypfvieh
 * @since v1.2.1 - 2024-12-26
 */
@FunctionalInterface
public interface IThrowingConsumer<V, T extends Throwable> {
    /**
     * Performs this operation on the given argument.
     *
     * @throws T exception
     */
    void accept(V _val) throws T;
}
