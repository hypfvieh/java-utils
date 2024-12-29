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
public interface IThrowingSupplier<V, T extends Throwable> {
    /**
     * Returns the result of the supplier or throws an exception.
     *
     * @return result of supplied function
     * @throws T exception
     */
    V get() throws T;
}
