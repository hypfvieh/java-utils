package com.github.hypfvieh.db;

import com.github.hypfvieh.util.TypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility methods to work with H2 database files.
 *
 * @author hypfvieh
 * @since 1.2.1 - 2024-03-21
 */
public class H2Util {
    private static final byte[] MAGIC_BUILD_NUMBER = new byte[] {0x54, 0x20, 0x43, 0x52, 0x45, 0x41, 0x54, 0x45, 0x5f, 0x42, 0x55, 0x49, 0x4c, 0x44};
    private static final byte[] MAGIC_FORMAT = new byte[] {0x2c, 0x66, 0x6f, 0x72, 0x6d, 0x61, 0x74, 0x3a};

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
            long readBytes = 0;
            int read = 0;
            int pos = -1;
            while ((read = fis.read(buf)) > 0) {
                readBytes += read;
                if (buildNumber == null) {
                    pos = TypeUtil.indexOfByteArray(buf, MAGIC_BUILD_NUMBER);
                    if (pos > -1) {
                        int searchPos = pos + MAGIC_BUILD_NUMBER.length + 1;
                        buildNumber = new String(Arrays.copyOfRange(buf, searchPos, searchPos + 3));
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

                if (readBytes >= 16384) {
                    // stop reading after 16384 bytes
                    // header information should be found before that or we are unable to determine
                    break;
                }
            }
        }

        if (!TypeUtil.isInteger(formatNumber) || !TypeUtil.isInteger(buildNumber)) {
            return null;
        }

        return new H2VersionInfo(Integer.parseInt(buildNumber), Integer.parseInt(formatNumber));
    }

    /**
     * Encapsulates version information of H2 database file.
     */
    public static class H2VersionInfo implements Comparable<H2VersionInfo> {
        private final int build;
        private final int format;

        public H2VersionInfo(int _build, int _format) {
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
