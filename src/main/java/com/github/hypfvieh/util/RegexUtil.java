package com.github.hypfvieh.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to apply regular expressions to Strings with built-in caching.
 *
 * @author hypfvieh
 * @version 1.2.0 - 2021-03-26
 */
public final class RegexUtil {

    private static final Map<String, Pattern> CACHED_PATTERNS = new HashMap<>();

    // hidden utility constructor
    private RegexUtil() {
    }

    /**
     * Splits the given input string using the given regular expression.
     * <p>
     * Will act like {@link String#split(String)} by first checking if
     * the split is done by a single character.
     * If yes, then {@link String#split(String)} will be called with the given regex.
     * If not, regex {@link Pattern} will be used (maybe from cache).
     * If input String is <code>null</code>, false is returned.
     *
     * @param _input input string
     * @param _regex regular expression
     *
     * @return split string as array, maybe null
     */
    public static String[] splitByRegex(String _input, String _regex) {
        return splitByRegex(_input, _regex, 0);
    }

    /**
     * Splits the given input string using the given regex.
     * <p>
     * Will act like {@link String#split(String)} by first checking if
     * the split is done by a single character.
     * If yes, then {@link String#split(String)} will be called with the given regex.
     * If not, regex {@link Pattern} will be used (maybe from cache).
     * If input String is <code>null</code>, false is returned.
     * <p>
     * The <code>_limit</code> argument can be used as threshold to limit the returned results.
     * If 0 or less is given, all will be returned.
     *
     * @param _input input string
     * @param _regex regular expression
     * @param _limit result threshold
     *
     * @return split String as array, maybe null
     */
    public static String[] splitByRegex(String _input, String _regex, int _limit) {
        Objects.requireNonNull(_regex, "Regex required");
        if (_input == null) {
            return null;
        }
        char ch = 0;

        boolean smart = ((_regex.length() == 1
                && ".$|()[{^?*+\\".indexOf(ch = _regex.charAt(0)) == -1)
                || (_regex.length() == 2
                    && _regex.charAt(0) == '\\'
                    && (((ch = _regex.charAt(1))-'0')|('9'-ch)) < 0
                    && ((ch-'a')|('z'-ch)) < 0
                    && ((ch-'A')|('Z'-ch)) < 0))
                && (ch < Character.MIN_HIGH_SURROGATE
                || ch > Character.MAX_LOW_SURROGATE);

        // do not use regex if not needed (same as String.split would do)
        if (smart) {
            return _input.split(_regex, _limit);
        }

        Pattern pattern = CACHED_PATTERNS.computeIfAbsent(_regex, x -> {
            if ((_regex.length() == 2 && _regex.charAt(0) == '\\') || (_regex.length() == 1 && _regex.charAt(0) == '\\')) {
                return Pattern.compile(Pattern.quote(_regex));
            }
            return Pattern.compile(_regex);
        });

        return pattern.split(_input, _limit);
    }

    /**
     * Checks if the given regular expression matches the given string.
     * <p>
     * If input String is <code>null</code>, false is returned.
     *
     * @param _input input string
     * @param _regex regular expression
     *
     * @return true if matching, false otherwise
     */
    public static boolean regexMatches(String _input, String _regex) {
        StringUtil.requireNonBlank(_regex, "Regex required");
        if (_input == null) {
            return false;
        }

        Pattern pattern = CACHED_PATTERNS.computeIfAbsent(_regex, Pattern::compile);
        Matcher matcher = pattern.matcher(_input);
        return matcher.matches();
    }

    /**
     * Applies the regular expression to the specified string and returns all values matched by the capturing groups.
     *
     * @param _input input string
     * @param _regex regular expression, required, must contain at least one capturing groups
     *
     * @return list with content of all capturing groups, maybe empty
     */
    public static List<String> extractByRegex(String _input, String _regex) {
        return extractByRegex(_input, _regex, 0);
    }

    /**
     * Applies the regular expression to the specified string and returns all values matched by the capturing groups.
     * <p>
     * If _limitResults is set to 0 or less, all results will be returned, otherwise only the specified amount of
     * results is returned (or less).
     *
     * @param _input input string
     * @param _regex regular expression, required, must contain at least one capturing groups
     * @param _limitResults limit the result length
     *
     * @return list with content of all capturing groups, maybe empty
     */
    public static List<String> extractByRegex(String _input, String _regex, int _limitResults) {
        StringUtil.requireNonBlank(_regex, "Regex required");
        if (_input == null) {
            return null;
        }
        Pattern pattern = CACHED_PATTERNS.computeIfAbsent(_regex, Pattern::compile);

        return extractByRegex(_input, pattern, _limitResults);
    }

    /**
     * Applies the regular expression to the specified string and returns all values matched by the capturing groups.
     * <p>
     * If _limitResults is set to 0 or less, all results will be returned, otherwise only the specified amount of
     * results is returned (or less).
     *
     * @param _input input string
     * @param _pattern regular expression pattern, required, must contain at least one capturing groups
     * @param _limitResults limit the result length
     *
     * @return list with content of all capturing groups, maybe empty
     */
    public static List<String> extractByRegex(String _input, Pattern _pattern, int _limitResults) {
        Objects.requireNonNull(_pattern, "Pattern required");
        Matcher matcher = _pattern.matcher(_input);
        if (matcher.groupCount() == 0) {
            throw new IllegalArgumentException("Pattern requires at least one capturing group: " + _pattern);
        }

        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount() +1; i++) {
                results.add(matcher.group(i));

                if (_limitResults > 0 && results.size() >= _limitResults) {
                    break;
                }
            }
        }

        return results;
    }

    /**
     * Applies the regular expression to the specified string and returns the value matched by the first capturing group or <code>null</code>.
     *
     * @param _input input string
     * @param _regex regular expression, required, must contain at least one capturing groups
     *
     * @return value matched by first capturing group
     */
    public static String extractFirstByRegex(String _input, String _regex) {
        List<String> results = extractByRegex(_input, _regex, 1);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Applies the regular expression to the specified string and returns the value matched by the first capturing group or <code>null</code>.
     *
     * @param _input pattern
     * @param _pattern regular expression pattern, required, must contain at least one capturing groups
     *
     * @return value matched by first capturing group
     */
    public static String extractFirstByRegex(String _input, Pattern _pattern) {
        List<String> results = extractByRegex(_input, _pattern, 1);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

}
