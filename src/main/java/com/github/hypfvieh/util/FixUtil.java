package com.github.hypfvieh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class FixUtil {

    //CHECKSTYLE:OFF
    public static final String FIX_DELIM     = "\u0001";

    static final List<Integer> stdHeader     = Arrays.asList(
            8, 9, 35, 49, 56, 115, 128, 90, 91, 34, 50, 142,
            57, 143, 116, 144, 129, 145, 43, 97, 52, 122, 212, 213, 347, 369);
    static final List<Integer> stdHeaderHops = Arrays.asList(627, 628, 629, 630); // Routing fields
    static final List<Integer> stdTrailer    = Arrays.asList(93, 98, 10);
    //CHECKSTYLE:ON

    private FixUtil() {

    }

    /**
     * Extract delimiter of FixMessageString.
     * @param _line line to read
     * @return delimiter or null
     */
    public static String getDelimiterFromFixMsgStr(String _line) {
        if (StringUtil.isBlank(_line)) {
            return null;
        }
        // Try to get delimiter using the char between the first and second tag
        Matcher matcher = Pattern.compile(".*?\\d+=[A-Za-z0-9\\. ]+([^0-9]*?)\\d+=.*").matcher(_line);
        if (matcher.matches() && !matcher.group(1).isEmpty()) {
            return matcher.group(1);
        } else {
            // if that didnt work out, try to find the char between some-tag and 9=
            matcher = Pattern.compile(".*?(.)9=.*").matcher(_line);
            if (matcher.matches()) {
                return matcher.group(1);
            } else { // if this also didnt work, use the last char of the string
                return _line.substring(_line.length() -1);
            }

        }
    }

    /**
     * Get a value from a FixMessage String.
     *
     * @param _msg string fix message to parse
     * @param _delimiter delimiter to use
     * @param _tag tag to find
     *
     * @return value or null if tag could not be found
     */
    public static String getFixTagValueFromString(String _msg, char _delimiter, int _tag) {
        String[] messageParts = StringUtil.split(_msg,_delimiter);
        for (String part : messageParts) {
            String[] tagOrVals = StringUtil.split(part, "=");
            if (tagOrVals.length == 2) {
                if (TypeUtil.isInteger(tagOrVals[0]) && Integer.parseInt(tagOrVals[0]) == _tag) {
                    return tagOrVals[1];
                }
            }
        }
        return null;
    }

    /**
     * Get a value from a FixMessage String.
     * @param _msg fix message as string
     * @param _tag tag to read
     *
     * @return value or null if tag could not be found
     */
    public static String getFixTagValueFromString(String _msg, int _tag) {
        String delimiter = getDelimiterFromFixMsgStr(_msg);
        return getFixTagValueFromString(_msg, delimiter.charAt(0), _tag);
    }

    /**
     * Tries to update a fix message by setting _tag to _value.
     * If _tag is not found, it is appended.
     * This method also ensures that the header and trailer tags are in correct order.
     *
     * @param _msg fix message as string
     * @param _tag tag to write
     * @param _value value for tag
     * @return string with added/changed tag value
     */
    public static String setFixTagOnMsgStr(String _msg, int _tag, String _value) {
        char delimiter = getDelimiterFromFixMsgStr(_msg).charAt(0);
        return setFixTagOnMsgStr(_msg, _tag, _value, delimiter);
    }

    /**
     * Tries to update a fix message by setting _tag to _value.
     * If _tag is not found, it is appended.
     * This method also ensures that the header and trailer tags are in correct order.
     *
     * @param _msg fix message as string
     * @param _tag tag to write
     * @param _value value to set
     * @param _delim delimiter to use
     * @return string with added/changed tag value
     */
    public static String setFixTagOnMsgStr(String _msg, int _tag, String _value, char _delim) {
        List<String> msgKeyValues = new ArrayList<>(Arrays.asList(StringUtil.split(_msg, _delim)));

        List<TagValue> header = new ArrayList<>();
        List<TagValue> body = new ArrayList<>();
        List<TagValue> trailer = new ArrayList<>();

        // extract the string to different lists so it is easier to add/modify tags to it
        for (String keyVal : msgKeyValues) {
            String[] split = keyVal.split("=");
            if (split.length < 2 || split.length > 2) {
                continue; // unexpected length
            }
            if (TypeUtil.isInteger(split[0], false)) {
                int tagNo = Integer.parseInt(split[0]);

                if (stdHeader.contains(tagNo)) {
                    header.add(new TagValue(tagNo, split[1]));
                } else if (stdHeaderHops.contains(tagNo)) { // note: we don't support modifying the header repeating group!
                    header.add(new TagValue(tagNo, split[1]));
                } else if (stdTrailer.contains(tagNo)) {
                    trailer.add(new TagValue(tagNo, split[1]));
                } else {
                    body.add(new TagValue(tagNo, split[1]));
                }
            }
        }

        // update potential header tag
        if (stdHeader.contains(_tag) || stdHeaderHops.contains(_tag)) {
            addOrUpdateTag(_tag, _value, header);
        } else if (stdTrailer.contains(_tag)) {
            addOrUpdateTag(_tag, _value, trailer);
        } else {
            addOrUpdateTag(_tag, _value, body);
        }

        StringBuilder fixMsgStr = new StringBuilder();
        // append header tags in order
        stdHeader.forEach(hdrTag -> header.forEach(tagVal -> {
            if (tagVal.tag == hdrTag) {
                fixMsgStr.append(hdrTag).append("=").append(tagVal.value).append(_delim);
            }
        }));
        // append routing information (hops repeating group)
        stdHeaderHops.forEach(hdrTag -> header.forEach(tagVal -> {
            if (tagVal.tag == hdrTag) {
                fixMsgStr.append(hdrTag).append("=").append(tagVal.value).append(_delim);
            }
        }));

        // append left body tags
        body.forEach(tv -> fixMsgStr.append(tv.tag).append("=").append(tv.value).append(_delim));

        // append trailer tags in order
        stdTrailer.forEach(tlrTag -> trailer.forEach(tagVal -> {
            if (tagVal.tag == tlrTag) {
                fixMsgStr.append(tlrTag).append("=").append(tagVal.value).append(_delim);
            }
        }));

        return fixMsgStr.toString();
    }

    /**
     * Internal helper method to easily add or update a list of tagvalues.
     * @param _tag tag to update
     * @param _val value to set
     * @param _list list of tag values to iterate
     */
    private static void addOrUpdateTag(int _tag, String _val, List<TagValue> _list) {
        if (_list == null || StringUtil.isBlank(_val)) {
            return;
        }
        AtomicBoolean updated = new AtomicBoolean(false);
        _list.stream().filter(tv -> tv.tag == _tag).forEach(
                tv -> {
                        tv.value = _val; updated.set(true);
                      });
        if (!updated.get()) {
            _list.add(new TagValue(_tag, _val));
        }
    }

   /**
    * Calculates the body length of the given message.
    * @param _msg fix message as string
    * @return checksum
    */
    public static int calculateFixBodyLength(String _msg) {
        String delimiter = getDelimiterFromFixMsgStr(_msg);
        return calculateFixBodyLength(_msg, delimiter.charAt(0));
    }


    /**
     * Calculates the body length of the given message.
     * @param _msg fix message as string
     * @param _delimiter delimiter to use
     * @return body length
     */
     public static int calculateFixBodyLength(String _msg, char _delimiter) {
         String modMsg = setFixTagOnMsgStr(_msg, 9, 0 + ""); // reset body length to 0, this also ensures that we have a body length tag
         List<String> msgKeyValues = Arrays.asList(StringUtil.split(modMsg, _delimiter));

         boolean catchNext = false;

         int msgLen = 0;

         for (String keyValueStrPair : msgKeyValues) {
             String[] split = keyValueStrPair.split("=");
             if (split.length < 2 || split.length > 2) {
                 continue; // unexpected length
             }

             if (TypeUtil.isInteger(split[0])) {
                 if (split[0].equals("10")) { // Tag 10 (checksum) is not part of bodylength and should be last field of message
                     catchNext = false;
                 }

                 if (catchNext) {
                     msgLen += keyValueStrPair.length() + 1; // + 1 because of delimiter
                 }

                 if (split[0].equals("9")) { // everything between tag 9 (second field of msg) and tag 10 (last field) counts for bodylength
                     catchNext = true;
                 }
             }
         }
         return msgLen;
     }

     /**
     * Calculates the bodylength of a messages and writes it to tag 9.
     * @param _msg fix message as string
     * @return  message with updated body length
     */
    public static String updateFixBodyLength(String _msg) {
        int msgLen = calculateFixBodyLength(_msg);
        return setFixTagOnMsgStr(_msg, 9, String.valueOf(msgLen));
    }

    /**
     * Calculates the checksum of the message.
     * @param _msg fix message as string
     * @param _delim delimiter to use
     * @return checksum
     */
    public static String calculateFixCheckSum(String _msg, char _delim) {
        if (StringUtil.isEmpty(_msg)) {
            return null;
        }
        int chkSum = 0; String msg = _msg.replaceAll(10 + "=" + ".+$", "").replace(_delim, "\u0001".charAt(0));
        for (int i = 0; i < msg.length(); chkSum += msg.charAt(i++)) /*CHECKSTYLE:OFF*/ { } /*CHECKSTYLE:ON*/
        return String.format("%03d", chkSum % 256);
    }

    /**
     * Sets the correct checksum in tag 10.
     * @param _msg fix message as string
     * @return fix message with updated checksum
     */
    public static String updateFixCheckSum(String _msg) {
        String fixMsg = _msg.replace(getDelimiterFromFixMsgStr(_msg), "\u0001");
        String checksum = calculateFixCheckSum(fixMsg, "\u0001".charAt(0));

        return setFixTagOnMsgStr(_msg, 10, checksum);
    }

    /**
     * Checks if given String looks like a FIX message.
     * @param _msg fix message as string
     * @return true if format matches a FIX message, false otherwise
     */
    public static boolean looksLikeFixMsg(String _msg) {

        return _msg.matches("^8=.*10=\\d+.$");

    }

    /**
     * Structure to hold tag and value information (for internal use only).
     */
    private static class TagValue {
        private int tag;
        private String value;

        TagValue(int _tag, String _val) {
            tag = _tag;
            value = _val;
        }

        @Override
        public String toString() {
            return "TagValue [tag=" + tag + ", value=" + value + "]";
        }

    }
}
