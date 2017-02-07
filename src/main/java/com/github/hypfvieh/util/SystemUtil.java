package com.github.hypfvieh.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility-Class with various operating system related helper methods.
 *
 * @author hypfvieh
 * @since v0.0.5 - 2015-08-05
 */
public final class SystemUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtil.class);

    private SystemUtil() {

    }

    /**
     * Gets the host name of the local machine.
     * @return host name
     */
    public static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException _ex) {
            return null;
        }
    }

    /**
     * Returns the current working directory.
     * @return
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * Returns the running class path.
     * @return
     */
    public static String getRunningClassPath() {
        return ClassLoader.getSystemClassLoader().getResource(".").getPath();
    }

    /**
     * Returns the temp directory of this platform.
     * @return
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Concats a path from all given parts, using the path delimiter for the currently used platform.
     * @param _includeTrailingDelimiter include delimiter after last token
     * @param _parts parts to concat
     * @return concatinated string
     */
    public static String concatFilePath(boolean _includeTrailingDelimiter, String..._parts) {
        if (_parts == null) {
            return null;
        }
        StringBuilder allParts = new StringBuilder();

        for (int i = 0; i < _parts.length; i++) {
            if (_parts[i] == null) {
                continue;
            }
            allParts.append(_parts[i]);

            if (!_parts[i].endsWith(File.separator)) {
                allParts.append(File.separator);
            }
        }

        if (!_includeTrailingDelimiter && allParts.length() > 0) {
            return allParts.substring(0, allParts.lastIndexOf(File.separator));
        }

        return allParts.toString();
    }
    /**
     * Concats a path from all given parts, using the path delimiter for the currently used platform.
     * Does not include trailing delimiter.
     * @param _parts parts to concat
     * @return concatinated string
     */
    public static String concatFilePath(String... _parts) {
        return concatFilePath(false, _parts);
    }

    /**
     * Append a suffix to the string (e.g. filename) if it doesn't have it already.
     * @param _str
     * @param _suffix
     * @return string with suffix or original if no suffix was appended
     */
    public static String appendSuffixIfMissing(String _str, String _suffix) {
        if (_str == null) {
            return null;
        }
        if (!_str.endsWith(_suffix)) {
            _str += _suffix;
        }
        return _str;
    }

    /**
     * Appends the OS specific path delimiter to the end of the String, if it is missing.
     * @param _filePath
     * @return
     */
    public static String appendTrailingDelimiter(String _filePath) {
        if (_filePath == null) {
            return null;
        }
        if (!_filePath.endsWith(File.separator)) {
            _filePath += File.separator;
        }
        return _filePath;
    }

    /**
     * Creates a new temporary directory in the given path.
     * @param _path
     * @param _name
     * @param _deleteOnExit
     * @return created Directory, null if directory/file was already existing
     */
    public static File createTempDirectory(String _path, String _name, boolean _deleteOnExit) {

        File outputDir = new File(concatFilePath(_path, _name));
        if (!outputDir.exists()) {
            try {
                Files.createDirectory(Paths.get(outputDir.toString()));
            } catch (IOException _ex) {
                LOGGER.error("Error while creating temp directory: ", _ex);
            }
        } else {
            return null;
        }
        if (_deleteOnExit) {
            outputDir.deleteOnExit();
        }
        return outputDir;
    }

    /**
     * Creates a temporary directory in the given path.
     * You can  specify certain files to get a random unique name.
     * @param _path where to place the temp folder
     * @param _prefix prefix of the folder
     * @param _length length of random chars
     * @param _timestamp add timestamp (yyyyMMdd_HHmmss-SSS) to directory name
     * @param _alphanumeric use characters and numbers for random part
     * @param _deleteOnExit mark directory for deletion on jvm termination
     * @return
     */
    public static File createTempDirectory(String _path, String _prefix, int _length, boolean _timestamp, boolean _deleteOnExit) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss-SSS");
        String randomStr = StringUtil.randomString(_length);

        StringBuilder fileName = new StringBuilder();

        if (_prefix != null) {
            fileName.append(_prefix);
        }
        fileName.append(randomStr);

        if (_timestamp) {
            fileName.append("_").append(formatter.format(new Date()));
        }
        File result = createTempDirectory(_path, fileName.toString(), _deleteOnExit);
        while (result == null) {
            result = createTempDirectory(_path, _prefix, _length, _timestamp, _deleteOnExit);
        }
        return result;
    }

    /**
     * Examines some system properties to determine whether the process is likely being debugged
     * in an IDE or remotely.
     * @return true if being debugged, false otherwise
     */
    public static boolean isDebuggingEnabled() {
        boolean debuggingEnabled = false;
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0) {
            debuggingEnabled = true;
        } else if (ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-Xdebug")) {
            debuggingEnabled = true;
        } else if (System.getProperty("debug", "").equals("true")) {
            debuggingEnabled = true;
        }
        return debuggingEnabled;
    }

    /**
     * Extracts the file extension (part behind last dot of a filename).
     * Only returns the extension, without the leading dot.
     *
     * @param _fileName
     * @return extension, empty string if not dot was found in filename or null if given String was null
     */
    public static String getFileExtension(String _fileName) {
        if (_fileName == null) {
            return null;
        }
        int lastDot = _fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return _fileName.substring(lastDot + 1);
    }

    /**
     * Checks if given String is a valid file, e.g. file exists, is really a file and can be read.
     *
     * Throws IOException or Subclass of it, if file is either non-existing, not a file or unreadable.
     *
     * @param _file
     * @return file object, never null
     * @throws IOException
     */
    public static File getFileIfReadable(String _file) throws IOException {
        if (StringUtil.isBlank(_file)) {
            throw new IOException("Empty or null string is not a valid file");
        }

        File file = new File(_file);

        if (!file.exists()) {
            throw new FileNotFoundException("No such file: " + _file);
        } else if (!file.isFile()) {
            throw new IOException("Not a file: " + _file);
        } else if (!file.canRead()) {
            throw new AccessDeniedException("File not readable: " + _file);
        }

        return file;
    }

    /**
     * Formats a file size given in byte to something human readable.
     *
     * @param _bytes
     * @param _use1000BytesPerMb use 1000 bytes per MByte instead of 1024
     * @return String
     */
    public static String formatBytesHumanReadable(long _bytes, boolean _use1000BytesPerMb) {
        int unit = _use1000BytesPerMb ? 1000 : 1024;
        if (_bytes < unit)  {
            return _bytes + " B";
        }
        int exp = (int) (Math.log(_bytes) / Math.log(unit));
        String pre = (_use1000BytesPerMb ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (_use1000BytesPerMb ? "" : "i");
        return String.format("%.1f %sB", _bytes / Math.pow(unit, exp), pre);
    }
}
