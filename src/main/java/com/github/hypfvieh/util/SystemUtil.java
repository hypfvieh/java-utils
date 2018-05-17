package com.github.hypfvieh.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

    /** List of known terminal emulators on linux/unix systems. */
    private static final String[] TERMINAL_EMULATORS = new String[]
            {"x-terminal-emulator", "gnome-terminal", "mate-terminal",
                    "konsole", "xterm", "rxvt", "xdg-terminal", "lxterminal", "pterm",
                    "aterm", "eterm", "roxterm", "qterminal", "terminator",
                    "tmux", "screen"};

    /** Character that separates components of a file path. This is "/" on UNIX and "\" on Windows. */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    /** Sequence used by operating system to separate lines in text files. */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /** The system's temporary-file directory. */
    public static final String TMP_DIR        = normalizePath(System.getProperty("java.io.tmpdir"));

    private SystemUtil() {

    }

    /**
     * Determines the operating system's type and version.
     * @return the OS type and version as a string
     */
    public static String getOs() {
        return (System.getProperty("os.name") + " " + System.getProperty("os.version"));
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
     * @return current working dir
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * Returns the running class path.
     * @return String with classpath
     */
    public static String getRunningClassPath() {
        return ClassLoader.getSystemClassLoader().getResource(".").getPath();
    }

    /**
     * Returns the temp directory of this platform.
     * @return temp directory
     */
    public static String getTempDir() {
        return TMP_DIR;
    }

    /**
     * Determines the Java version of the executing JVM.
     * @return Java version
     */
    public static String getJavaVersion() {
        String[] sysPropParms = new String[] {"java.runtime.version", "java.version"};
        for (int i = 0; i < sysPropParms.length; i++) {
            String val = System.getProperty(sysPropParms[i]);
            if (!StringUtil.isEmpty(val)) {
                return val;
            }
        }
        return null;
    }

    /**
     * Determines the current logged on user.
     * @return logged on user
     */
    public static String getCurrentUser() {
        String[] sysPropParms = new String[] {"user.name", "USER", "USERNAME"};
        for (int i = 0; i < sysPropParms.length; i++) {
            String val = System.getProperty(sysPropParms[i]);
            if (!StringUtil.isEmpty(val)) {
                return val;
            }
        }
        return null;
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
     * @param _str string to check
     * @param _suffix suffix to append
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
     * @param _filePath file path
     * @return String
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
     * @param _path path
     * @param _name directory name
     * @param _deleteOnExit delete directory on jvm shutdown
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
     * @param _deleteOnExit mark directory for deletion on jvm termination
     * @return file
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
     * @param _fileName filename
     * @return extension, empty string if no dot was found in filename or null if given String was null
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
     * Extracts the file extension (part behind last dot of a filename).
     * Only returns the extension, without the leading dot.
     *
     * @param _file file
     * @return extension, empty string if no dot was found in filename or null if given String was null
     */
    public static String getFileExtension(File _file) {
        return getFileExtension(_file.getAbsolutePath());
    }

    /**
     * Checks if given String is a valid file, e.g. file exists, is really a file and can be read.
     *
     * Throws IOException or Subclass of it, if file is either non-existing, not a file or unreadable.
     *
     * @param _file filename
     * @return file object, never null
     * @throws IOException if file could not be read
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
     * @param _bytes size in bytes
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

    /**
     * Read the JARs manifest and try to get the current program version from it.
     * @param _class class to use as entry point
     * @param _default default string to use if version could not be found
     * @return version or null
     */
    public static String getApplicationVersionFromJar(Class<?> _class, String _default) {
        try {
            Enumeration<URL> resources = _class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {

                Manifest manifest = new Manifest(resources.nextElement().openStream());
                Attributes attribs = manifest.getMainAttributes();
                String ver = attribs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
                if (ver == null) {
                	return _default;
                }

                String rev = attribs.getValue("Implementation-Revision");
                if (rev != null) {
                    ver += "-r" + rev;
                }
                return ver;

            }
        } catch (IOException _ex) {
        }

        return _default;
    }

    /**
     * Tries to find the "default" terminal emulator.
     * This will be cmd.exe on windows and may vary on linux/unix systems depending on installed terminal programs.
     * On linux/unix there is no generic way to find the default terminal,
     * so all known terminal programs will be tried until any of them is found.
     * @return String with terminal name or null if terminal could not be determined
     */
    public static String guessDefaultTerminal() {
        if (System.getProperty("os.name", "").equalsIgnoreCase("windows")) {
            return "cmd.exe";
        }

        String envPath = System.getenv("PATH");
        if (envPath == null) {
            throw new RuntimeException("Could not find enviroment PATH setting.");
        }
        String[] pathes = envPath.split(":");

        for (String term : TERMINAL_EMULATORS) {
            for (String path : pathes) {
                File terminalExe = new File(concatFilePath(path, term));
                if (terminalExe.exists() && terminalExe.canExecute()) {
                    return terminalExe.getAbsolutePath();
                }
            }
        }

        return null;
    }

    /**
     * Normalize a file system path expression for the current OS.
     * Replaces path separators by this OS's path separator.
     * Appends a final path separator if parameter is set
     * and if not yet present.
     * @param _path path
     * @param _appendFinalSeparator controls appendix of separator at the end
     * @return normalized path
     */
    public static String normalizePath(String _path, boolean _appendFinalSeparator) {
        if (_path == null) {
            return _path;
        }

        String path = _path
            .replace("\\", FILE_SEPARATOR)
            .replace("/", FILE_SEPARATOR);
        if (_appendFinalSeparator && !path.endsWith(FILE_SEPARATOR)) {
            path += FILE_SEPARATOR;
        }
        return path;
    }

    /**
     * Normalize a file system path expression for the current OS.
     * Replaces path separators by this OS's path separator.
     * Appends a final path separator unless present.
     * @param _path path
     * @return normalized path
     */
    public static String normalizePath(String _path) {
        return normalizePath(_path, true);
    }

    /**
     * Checks if the running OS is a MacOS/MacOS X.
     * @return true if MacOS (or MacOS X), false otherwise
     */
    public static boolean isMacOs() {
        String osName = System.getProperty("os.name");
        return osName == null ? false : osName.toLowerCase().startsWith("mac");
    }

    /**
     * Tries to get the current version of MacOS/MacOS X.
     * The version usually looks like '10.13.4', where the part behind the last dot represents the patchlevel.
     * The major version in this case would be '10.13'.
     * @return version without patchlevel or null
     */
    public static String getMacOsMajorVersion() {
        if (!isMacOs()) {
            return null;
        }

        String osVersion = System.getProperty("os.version");

        if (osVersion != null) {
            String[] split = osVersion.split("\\.");
            if (split.length >= 2) {
                return split[0] + "." + split[1];
            } else {
                return osVersion;
            }
        }

        return null;
    }
}
