package com.github.hypfvieh.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builder to create a FIX message without using any proprietary API.
 * Handles header and trailer tags (including header hops) to create a properly sorted message.
 *
 * Does not support dictionaries, so be careful when dealing with repeating groups.
 *
 * @author hypfvieh
 * @since 1.0.6 - 2019-02-22
 */
public class SimpleFixMessageBuilder {

    private final List<String> tagValues;

    public SimpleFixMessageBuilder() {
        tagValues = new ArrayList<>();
    }

    /**
     * Set/add tag value to FIX message to be created.
     * Will overwrite existing tags depending on the provided boolean _overwrite.
     * @param _tag tag number
     * @param _value value
     * @param _overwrite true to overwrite existing tag, false otherwise
     * @return this
     */
    public SimpleFixMessageBuilder setValue(int _tag, Object _value, boolean _overwrite) {
        if (_value == null || _value.toString().isEmpty()) { // do nothing if we did not get any value
            return this;
        }
        if (_value.toString().contains(FixUtil.FIX_DELIM)) {
            throw new IllegalArgumentException("FIX delimiter is illegal in tag value.");
        }

        String tagVal = String.format("%s=%s", _tag, _value);
        if (_overwrite) {
            // remove entry if given tag number equals to an already existing number
            tagValues.removeIf(tv -> _tag == Integer.parseInt(tv.substring(0, tv.indexOf("="))));
        }
        tagValues.add(tagVal);
        return this;
    }

    /**
     * Set/add tag value to FIX message to be created.
     * Will always overwrite existing tags, do not use this for creating repeating group entries!
     * @param _tag tag number
     * @param _value value
     * @return this
     */
    public SimpleFixMessageBuilder setValue(int _tag, Object _value) {
        return setValue(_tag, _value, true);
    }

    public SimpleFixMessageBuilder setUtcTimestamp(int _tag) {
        return setUtcTimestamp(_tag, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public SimpleFixMessageBuilder setUtcTimestamp(int _tag, ZonedDateTime _zonedDateTime) {
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").format(_zonedDateTime);
        return setValue(_tag, ts, true);
    }

    /**
    * Creates a basic FIX message containing only dummy header and trailer with the specified FIX version and message
    * type.
    *
    * @param _fixVersion FIX version to use (null is not allowed)
    * @param _msgType message type to set (null is not allowed)
    * @return this
    */
    public SimpleFixMessageBuilder createBasicMessage(String _fixVersion, String _msgType) {
        Objects.requireNonNull(_fixVersion, "FIX version cannot be null");
        Objects.requireNonNull(_msgType, "Message type cannot be null");

        String fixVersion = _fixVersion;
        if (_fixVersion.contains("5.0")) {
            fixVersion = "FIXT.1.1";
        }

        setValue(8, fixVersion, true);
        setValue(9, "0", true);
        setValue(35, _msgType, true);
        setValue(49, "SENDER", true);
        setValue(56, "TARGET", true);
        setValue(34, "1", true);
        setUtcTimestamp(52);
        setValue(10, "0");

        return this;
    }

    /**
     * Builds the new message and returns it as String.
     * @return String, maybe empty, never null
     */
    public String build() {
        List<String> orderedMsg = new ArrayList<>();

        // order the tags in header
        for (Integer tagNum : FixUtil.stdHeader) {
            for (String tagVal : tagValues) {
                int tag = Integer.parseInt(tagVal.substring(0, tagVal.indexOf("=")));
                if (tagNum == tag) {
                    orderedMsg.add(tagVal);
                }
            }
        }

        // order the header hops
        Optional<String> hasHeaderHops = tagValues.stream().filter(tv -> Integer.parseInt(tv.substring(0, tv.indexOf("="))) == FixUtil.stdHeaderHops.get(0)).findFirst();
        if (hasHeaderHops.isPresent()) {
            List<List<String>> groupHops = new ArrayList<>();

            List<String> hop = null;
            for (String tagValue : tagValues) {
                int tagNum = Integer.parseInt(tagValue.substring(0, tagValue.indexOf("=")));

                if (!FixUtil.stdHeaderHops.contains(tagNum)) { // tag not part of group
                    continue;
                }

                if (tagNum == FixUtil.stdHeaderHops.get(0)) { // NumInGroup tag
                    continue;
                }

                if (tagNum == FixUtil.stdHeaderHops.get(1)) { // delimiting first tag in group
                    hop = new ArrayList<>(FixUtil.stdHeaderHops.size() - 1);
                    groupHops.add(hop);

                    for (int j = 0; j < FixUtil.stdHeaderHops.size() - 1; j++) {
                        hop.add(null);
                    }
                }

                if (hop != null) {
                    // all tags found here will belong to group, but may be unsorted - so we have to sort them
                    int tagPosition = FixUtil.stdHeaderHops.indexOf(tagNum);

                    hop.add(tagPosition, tagValue);
                }
            }

            orderedMsg.add(hasHeaderHops.get());
            groupHops.stream().flatMap(Collection::stream).filter(Objects::nonNull).forEach(orderedMsg::add);
        }

        List<String> trailer = new ArrayList<>();

        // order the tags in trailer
        for (Integer tagNum : FixUtil.stdTrailer) {
            for (String tagVal : tagValues) {
                int tag = Integer.parseInt(tagVal.substring(0, tagVal.indexOf("=")));
                if (tagNum == tag) {
                    trailer.add(tagVal);
                }
            }
        }

        // put all body tags in the new ordered message
        tagValues.stream().filter(tv ->{
            int tagNum = Integer.parseInt(tv.substring(0, tv.indexOf("=")));
            return !FixUtil.stdTrailer.contains(tagNum)
                    && !FixUtil.stdHeader.contains(tagNum)
                    && !FixUtil.stdHeaderHops.contains(tagNum);
        }).forEach(orderedMsg::add);

        // add trailer to message
        orderedMsg.addAll(trailer);

        // create the string (do not forget to add the trailing delimiter!)
        String msg = String.join(FixUtil.FIX_DELIM, orderedMsg) + FixUtil.FIX_DELIM;

        // update body length
        int bodyLen = FixUtil.calculateFixBodyLength(msg);

        int bodyLenPos = FixUtil.stdHeader.indexOf(9);

        if (orderedMsg.get(bodyLenPos).startsWith("9=")) {
            orderedMsg.remove(bodyLenPos);
        }

        orderedMsg.add(bodyLenPos, "9=" + bodyLen);

        // update message to add updated bodylen for checksum calculation
        msg = String.join(FixUtil.FIX_DELIM, orderedMsg) + FixUtil.FIX_DELIM;

        // update checksum

        String checkSum = FixUtil.calculateFixCheckSum(msg, '\u0001');

        if (orderedMsg.get(orderedMsg.size()-1).startsWith("10=")) {
            orderedMsg.remove(orderedMsg.size()-1);
        }
        orderedMsg.add("10=" + checkSum);

        // update message again to fix checksum
        msg = String.join(FixUtil.FIX_DELIM, orderedMsg) + FixUtil.FIX_DELIM;

        return msg;
    }
}
