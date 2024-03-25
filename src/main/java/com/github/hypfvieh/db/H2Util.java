package com.github.hypfvieh.db;

import com.github.hypfvieh.util.TypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility methods to work with H2 database files.
 *
 * @author hypfvieh
 * @since 1.2.1 - 2024-03-21
 */
public class H2Util {
    private static final byte[] MAGIC_XSET = new byte[] {0x26, 0x58, 0x53, 0x45, 0x54}; // "&XSET"
    private static final byte[] MAGIC_BUILD_NUMBER = new byte[] {0x20, 0x43, 0x52, 0x45, 0x41, 0x54, 0x45, 0x5f, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x20}; // " CREATE_BUILD "
    private static final byte[] MAGIC_FORMAT = new byte[] {0x2c, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x3a}; // "FORMAT:"

    /**
     * Reads the given stream and tries to extract the version information.<br>
     * If version information cannot be determined, <code>null</code> is returned.
     * <p>
     * Given stream will be closed after operation.
     * </p>
     *
     * @param _input input stream to H2 file
     *
     * @return version info or <code>null</code> if version cannot be found
     *
     * @throws IOException when read operation fails
     */
    public static H2VersionInfo findH2Version(InputStream _input) throws IOException {
        byte[] buf = new byte[2048];
        String buildNumber = null;
        String formatNumber = null;

        try (var fis = _input) {
            int pos = -1;
            boolean foundXset = false;
            while ((fis.read(buf)) > 0) {
                if (!foundXset) {
                    foundXset = byteScan(buf, MAGIC_XSET) > -1;
                }
                if (buildNumber == null && foundXset) {
                    pos = byteScan(buf, MAGIC_BUILD_NUMBER);
                    if (pos > -1) {
                        buildNumber = findNumbers(buf, pos, 3);
                    }
                }

                if (formatNumber == null) {
                    pos = TypeUtil.indexOfByteArray(buf, MAGIC_FORMAT);
                    if (pos > -1) {
                        int searchPos = pos + MAGIC_FORMAT.length;
                        formatNumber = new String(Arrays.copyOfRange(buf, searchPos, searchPos + 1));
                    }
                }

                if (buildNumber != null && formatNumber != null) {
                    break;
                }

            }
        }

        if (!TypeUtil.isInteger(formatNumber) || !TypeUtil.isInteger(buildNumber)) {
            return null;
        }

        return new H2VersionInfo(Integer.valueOf(buildNumber), Integer.valueOf(formatNumber));
    }

    /**
     * Look for ASCII numbers in the given byte array.
     *
     * @param _buf byte array to read
     * @param _offset offset to start reading at
     * @param _amountOfNumbers amount of numbers to find
     * @return String or null if no numbers found
     */
    static String findNumbers(byte[] _buf, int _offset, int _amountOfNumbers) {
        if (_buf.length < _offset || _buf.length < _amountOfNumbers) {
            return null;
        }

        ByteBuffer bucket = ByteBuffer.allocate(_amountOfNumbers);
        int bytesRead = 0;
        for (int i = _offset; i < _buf.length; i++) {

            // ignore everything non numeric
            if (_buf[i] >= 48 && _buf[i] <= 57) {
                bucket.put(_buf[i]);
                bytesRead++;
            }

            // read until we have desired amount of numbers found
            if (bytesRead < _amountOfNumbers) {
                continue;
            }

            return new String(bucket.array());

        }
        return null;
    }

    /**
     * Look for needle in heystack assuming needle contains ASCII only.
     * Uses a sliding window to find needle in case text in heystack is polluted by non-ASCII bytes.
     *
     * @param _heystack heystack to search
     * @param _needle needle to find in heystack
     *
     * @return position of needle in heystack, -1 if not found
     */
    static int byteScan(byte[] _heystack, byte[] _needle) {
        ByteBuffer bucket = ByteBuffer.allocate(_needle.length);
        boolean foundFirst = false;
        int bytesRead = 0;
        for (int i = 0; i < _heystack.length; i++) {
            // skip everything until we find at least the first byte
            if (!foundFirst && _heystack[i] == _needle[0]) {
                foundFirst = true;
            }

            // ignore everything non-ascii
            if (foundFirst && _heystack[i] >= 32 && _heystack[i] <= 126) {
                bucket.put(_heystack[i]);
                bytesRead++;
            }

            // read at least as many bytes as we need for our needle to be found
            if (bytesRead < _needle.length) {
                continue;
            }

            if (TypeUtil.indexOfByteArray(bucket.array(), _needle) > -1) {
                return i;
            } else {
                // no match yet, drop first byte so we have a sliding-window to find needle
                byte[] current = new byte[bucket.array().length -1];
                System.arraycopy(bucket.array(), 1, current, 0, bucket.array().length -1);
                bucket.rewind();
                bucket.put(current);
                bytesRead = bucket.array().length -1;
            }

        }
        return -1;
    }

    /**
     * Encapsulates version information of H2 database file.
     */
    public static class H2VersionInfo implements Comparable<H2VersionInfo> {
        private final int build;
        private final int format;

        public H2VersionInfo(Integer _build, Integer _format) {
            build = _build;
            format = _format;
        }

        public int getBuild() {
            return build;
        }

        public int getFormat() {
            return format;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [build=" + build + ", format=" + format + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(build, format);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            H2VersionInfo other = (H2VersionInfo) obj;
            return build == other.build && format == other.format;
        }

        @Override
        public int compareTo(H2VersionInfo _o) {
            if (_o == null) {
                return -1;
            } else if (_o.getBuild() == getBuild() && _o.getFormat() == getFormat()) {
                return 0;
            } else if (_o.getBuild() > getBuild()) {
                return -1;
            } else if (_o.getFormat() > getFormat()) {
                return -1;
            }

            return 1;
        }

    }
}
