package com.github.hypfvieh.function;

/**
 * Supplier which allows throwing any exception.
 *
 * @param <V1> first type which is supplied
 * @param <V2> second type which is supplied
 * @param <T> type of exception which gets thrown
 *
 * @author hypfvieh
 * @since v1.2.1 - 2024-12-26
 */
@FunctionalInterface
public interface IThrowingBiConsumer<V1, V2, T extends Throwable> {
    /**
     * Performs this operation on the given arguments.
     *
     * @throws T exception
     */
    void accept(V1 _val1, V2 _val2) throws T;
}
