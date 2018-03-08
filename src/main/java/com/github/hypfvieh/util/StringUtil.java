package com.github.hypfvieh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Utility class for String manipulation.
 *
 * @author hypfvieh
 * @since v1.0 - 2016-06-29
 */
public final class StringUtil {

    /** Characters used for random strings */
    private static final char[] SYMBOLS;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
          tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
          tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        SYMBOLS = tmp.toString().toCharArray();
      }

    private StringUtil() {

    }

    /**
     * Abbreviates a String using ellipses.
     *
     * @param _str
     * @param _length
     * @return abbreviated string, original string if string length is lower or equal then desired length or null if input was null
     */
    public static String abbreviate(String _str, int _length) {
        if (_str == null) {
            return null;
        }
        if (_str.length() <= _length) {
            return _str;
        }

        String abbr = _str.substring(0, _length -3) + "...";

        return abbr;
    }

    /**
     * Tries to split a string in a smart way.<br><br>
     *
     * String will be splitted by space and then recombined until each line has
     * the given length or less than the given length, if the next token would cause
     * the line to be longer than requested
     *
     * It is ensured that each line has a maximum length of _len, it could be short but never longer.
     *
     * @param _text
     * @param _len
     * @return list or null if _text was null
     */
    public static List<String> smartWordSplit(String _text, int _len) {
        if (_text == null) {
            return null;
        }

        // if the given string is already shorter or equal to wanted length, return immediately
        if (_text.length() <= _len) {
            return TypeUtil.createList(_text);
        }

        List<String> list = new ArrayList<>();

        String[] result = _text.split("\\s");
        for (int x=0; x<result.length; x++) {
            if (result[x].length() > _len) {
                list.addAll(splitEqually(result[x], _len));
            } else if (result[x].length() < _len) {
                StringBuilder sb = new StringBuilder();
                x = strAppender(result, sb, x, _len);
                list.add(sb.toString());
            } else {
                list.add(result[x]);
            }
        }
        return list;
    }

    /**
     * Internally used by smartStringSplit to recombine the string until the expected length is reached.
     *
     * @param _text string array to process
     * @param _sbResult resulting line
     * @param _beginIdx start index of string array
     * @param _len line length
     * @return last position in string array or -1 if _text or _sbResult is null
     */
    private static int strAppender(String[] _text, StringBuilder _sbResult, int _beginIdx, int _len) {
        if (_text == null || _sbResult == null) {
            return -1;
        }
        if (_beginIdx > _text.length) {
            return _text.length;
        }
        int i = _beginIdx;
        for (i = _beginIdx; i < _text.length; i++) {  // current token length + current buffer length
            if (_sbResult.length() < _len) {
                int condition = _text[i].length() + _sbResult.length();
                boolean firstOrLastToken = true;
                if (i <= _text.length -1 && _sbResult.length() > 0) { // add one char (for trailing space) if result is not empty and we are not on the first token
                    condition += 1; // + 1 (for space)
                    firstOrLastToken = false;
                }
                if (condition <= _len) {
                    if (!firstOrLastToken) { // append a space if result is not empty and we are not on the first token
                        _sbResult.append(" ");
                    }
                    _sbResult.append(_text[i]);
                } else {
                    i-=1;
                    break;
                }
            } else {
                if (i > _beginIdx) {
                    i-=1;
                }
                break;
            }
        }

        return i;
    }

    /**
     * Splits a Text to equal parts.
     * There is no detection of words, everything will be cut to the same length.
     *
     * @param _text
     * @param _len
     * @return list of string splitted to _len or null if _text was null
     */
    public static List<String> splitEqually(String _text, int _len) {
        if (_text == null) {
            return null;
        }
        List<String> ret = new ArrayList<String>((_text.length() + _len - 1) / _len);

        for (int start = 0; start < _text.length(); start += _len) {
            ret.add(_text.substring(start, Math.min(_text.length(), start + _len)));
        }
        return ret;
    }

    /**
     * Replace all placeholders in given string by value of the corresponding key in given Map.
     *
     * @param _searchStr
     * @param _replacements
     * @return String or null if _searchStr was null
     */
    public static String replaceByMap(String _searchStr, Map<String, String> _replacements) {
        if (_searchStr == null) {
            return null;
        }
        if (_replacements == null || _replacements.isEmpty()) {
            return _searchStr;
        }

        String str = _searchStr;

        for (Entry<String, String> entry : _replacements.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }

        return str;
    }

    /**
     * Lower case the first letter of the given string.
     *
     * @param _str
     * @return
     */
    public static String lowerCaseFirstChar(String _str) {
        if (_str == null) {
            return null;
        }
        if (_str.isEmpty()) {
            return _str;
        }

        return _str.substring(0, 1).toLowerCase() + _str.substring(1);
    }

    /**
     * Upper case the first letter of the given string.
     *
     * @param _str
     * @return
     */
    public static String upperCaseFirstChar(String _str) {
        if (_str == null) {
            return null;
        }
        if (_str.isEmpty()) {
            return _str;
        }
        return _str.substring(0, 1).toUpperCase() + _str.substring(1);
    }

    /**
     * Simple rot13 implementation.
     *
     * @param _input
     * @return
     */
    public static String rot13(String _input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _input.length(); i++) {
            char c = _input.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            } else if  (c >= 'A' && c <= 'M') {
                c += 13;
            } else if  (c >= 'n' && c <= 'z') {
                c -= 13;
            } else if  (c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            sb.append(c);
        }
        return sb.toString();
     }

    /**
     * Checks if any of the given strings in _compare is equal to _str (case-insensitive).<br>
     * Will return true if both parameters are null or _str is null and _compare is empty.
     *
     * @param _str
     * @param _compare
     * @return true if equal false otherwise
     */
    public static boolean equalsIgnoreCaseAny(String _str, String... _compare) {
        return equalsAny(true, _str, _compare);    }

    /**
     * Checks if any of the given strings in _compare is equal to _str (case-sensitive).<br>
     * Will return true if both parameters are null or _str is null and _compare is empty.
     *
     * @param _str
     * @param _compare
     * @return true if equal false otherwise
     */
    public static boolean equalsAny(String _str, String... _compare) {
       return equalsAny(false, _str, _compare);
    }

    /**
     * Checks if any of the given strings in _compare is equal to _str (either case-insensitive or case-sensitive).<br>
     * Will return true if both parameters are null or _str is null and _compare is empty.
     *
     * @param _ignoreCase
     * @param _str
     * @param _compare
     * @return true if equal false otherwise
     */
    public static boolean equalsAny(boolean _ignoreCase, String _str, String... _compare) {
        if (_str == null && _compare == null || _compare.length == 0) {
            return true;
        } else if (_str == null) {
            return false;
        }

        for (String cmp : _compare) {
            if (_ignoreCase) {
                if (cmp.equalsIgnoreCase(_str)) {
                    return true;
                }
            } else {
                if (cmp.equals(_str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given String is either null or blank.
     * Blank means:<br>
     * <pre>
     * " " - true
     * "" - true
     * null - true
     * " xx" - false
     * </pre>
     * @param _str
     * @return
     */
    public static boolean isBlank(String _str) {
        if (_str == null) {
            return true;
        }

        return _str.trim().isEmpty();
    }

    /**
     * Checks if the given String is either null or empty.
     * Blank means:<br>
     * <pre>
     * " " - false
     * "" - true
     * null - true
     * " xx" - false
     * </pre>
     * @param _str
     * @return
     */
    public static boolean isEmpty(String _str) {
        if (_str == null) {
            return true;
        }

        return _str.isEmpty();
    }

    /**
     * Checks if given String is blank (see {@link #isBlank(String)}.<br>
     * If String is blank, the given default is returned, otherwise the String is returned.
     * @param _str string to check
     * @param _default default in case of blank string
     * @return _str or _default
     */
    public static String defaultIfBlank(String _str, String _default) {
        return isBlank(_str) ? _default : _str;
    }

    /**
     * Generate a simple (cryptographic insecure) random string.
     * @param _length length of random string
     * @return random string or empty string if _length <= 0
     */
    public static String randomString(int _length) {
        if (_length <= 0) {
            return "";
        }
        Random random = new Random();
        char[] buf = new char[_length];
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = SYMBOLS[random.nextInt(SYMBOLS.length)];
        return new String(buf);
    }

    /**
     * Combines the Strings in _string using _delimiter.
     * @param _delimiter
     * @param _strings
     * @return null if _strings is null, concatenated string otherwise
     */
    public static String join(String _delimiter, List<String> _strings) {
        if (_strings == null) {
            return null;
        }
        if (_delimiter == null) {
            _delimiter = "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _strings.size(); i++) {
            sb.append(_strings.get(i));
            if (i < _strings.size() - 1) { // only append delimiter if this is not the last token
                sb.append(_delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Combines the Strings in _string using _delimiter.
     * @param _delimiter
     * @param _strings
     * @return null if _strings is null, concatenated string otherwise
     */
    public static String join(String _delimiter, String[] _strings) {
        return join(_delimiter, Arrays.asList(_strings));
    }

    /**
     * Converts a camel-case string to an upper-case string
     * where each upper-case character except the first in
     * the input string is preceded by an underscore in the
     * output string.
     * Empty or null strings are returned as-is.
     * <pre>
     *   convertCamelToUpperCase(null) = null
     *   convertCamelToUpperCase("") = ""
     *   convertCamelToUpperCase("  ") = "  "
     *   convertCamelToUpperCase("Hello") = "HELLO"
     *   convertCamelToUpperCase("HELLO") = "HELLO"
     *   convertCamelToUpperCase("AcmeCompany") = "ACME_COMPANY"
     * </pre>
     * @param _str camel-case string
     * @return upper-case string
     */
    public static String convertCamelToUpperCase(String _str) {
        if (isEmpty(_str) || isAllUpperCase(_str)) {
            return _str;
        }
        StringBuffer sb = new StringBuffer(String.valueOf(_str.charAt(0)).toUpperCase());
        for (int i = 1; i < _str.length(); i++) {
            char c = _str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append('_');
            }
            sb.append(c);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Tries to convert upper-case string to camel-case.
     * The given string will be analyzed and all string parts preceded by an underline character will be
     * converted to upper-case, all other following characters to lower-case.
     * @param _str
     * @return
     */
    public static String convertUpperToCamelCase(String _str) {
        if (_str == null || isBlank(_str)) {
            return _str;
        } else if (!_str.contains("_")) {
            return (_str.charAt(0) + "").toUpperCase() + _str.substring(1);
        }

        StringBuffer sb = new StringBuffer(String.valueOf(_str.charAt(0)).toUpperCase());
        for (int i = 1; i < _str.length(); i++) {
            char c = _str.charAt(i);
            if (c == '_') {
                i++; // get next character and convert to upper case
                c = String.valueOf(_str.charAt(i)).toUpperCase().charAt(0);
            } else {
                c = String.valueOf(c).toLowerCase().charAt(0);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Checks if the given String is in all upper-case.
     * @param _str
     * @return true if upper-case, false otherwise. Also false if string is null or empty.
     */
    public static boolean isAllUpperCase(String _str) {
        return isEmpty(_str) || !_str.matches(".*[a-z].*");
    }
}
