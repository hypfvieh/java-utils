package com.github.hypfvieh.formatter;

/**
 * Represents string split operation into a list of tokens where each token has as maximum length.
 *
 */
@FunctionalInterface
public interface StringSplitOperator {

    /**
     * Applies this operator to the given operands.
     *
     * @param _text text to split
     * @param _len max length of each split token
     * @return list of split tokens
     */
    java.util.List<String> applySplit(String _text, int _len);
}
