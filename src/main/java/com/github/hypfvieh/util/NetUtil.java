package com.github.hypfvieh.util;

import java.net.InetAddress;
import java.util.regex.Pattern;

public final class NetUtil {

    private static final Pattern IPV4_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])", Pattern.CASE_INSENSITIVE);
    private static final Pattern IPV6_PATTERN = Pattern.compile("([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}", Pattern.CASE_INSENSITIVE);

    private NetUtil() {

    }

    /**
     * Get the host name of a local address, if available.
     *
     * @param _ipAddress the IP address
     * @return the host name, or the original IP if name not available
     */
    public static String getHostName(String _ipAddress) {
        try {
            InetAddress addr = InetAddress.getByName(_ipAddress);
            return addr.getHostName();
        } catch (Exception _ex) {
            return _ipAddress;
        }
    }

    /**
     * Checks if given String is an IPv4 address.
     *
     * @param _ipAddress
     * @return true if valid address, false otherwise
     */
    public static boolean isIPv4Address(String _ipAddress) {
        return IPV4_PATTERN.matcher(_ipAddress).matches();
    }

    /**
     * Checks if given String is an IPv6 address.
     *
     * @param _ipAddress
     * @return true if valid address, false otherwise
     */
    public static boolean isIPv6Address(String _ipAddress) {
        return IPV6_PATTERN.matcher(_ipAddress).matches();
    }

    /**
     * Checks if given String is either a valid IPv4 or IPv6 address.
     *
     * @param _ipAddress
     * @return true if valid address, false otherwise
     */
    public static boolean isIPv4orIPv6Address(String _ipAddress) {
        return IPV4_PATTERN.matcher(_ipAddress).matches() || IPV6_PATTERN.matcher(_ipAddress).matches();
    }

}
