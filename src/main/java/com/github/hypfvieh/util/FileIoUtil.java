package com.github.hypfvieh.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.common.SearchOrder;


public final class FileIoUtil {

    private FileIoUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileIoUtil.class);

    /**
     * Trys to read a properties file.
     * Returns null if properties file could not be loaded
     * @param _file
     * @return Properties Object or null
     */
    public static Properties readProperties(File _file) {
        if (_file.exists()) {
            try {
                return readProperties(new FileInputStream(_file));
            } catch (FileNotFoundException _ex) {
                LOGGER.info("Could not load properties file: " + _file, _ex);
            }
        }
        return null;
    }

    /**
     * Tries to read a properties file from an inputstream.
     * @param _stream
     * @return properties object/null
     */
    public static Properties readProperties(InputStream _stream) {
        Properties props = new Properties();
        if (_stream == null) {
            return null;
        }

        try {
            props.load(_stream);
            return props;
        } catch (IOException | NumberFormatException _ex) {
            LOGGER.warn("Could not properties: ", _ex);
        }
        return null;
    }

    /**
     * Read properties from given filename
     * (returns empty {@link Properties} object on failure).
     *
     * @param _fileName The properties file to read
     * @param _props optional properties object, if null a new object created in the method
     * @return {@link Properties} object
     */
    public static Properties readPropertiesFromFile(String _fileName, Properties _props) {
        Properties props = _props == null ? new Properties() : _props;

        LOGGER.debug("Trying to read properties from file: " + _fileName);
        Properties newProperties = readProperties(new File(_fileName));
        if (newProperties != null) {
            LOGGER.debug("Successfully read properties from file: " + _fileName);
            props.putAll(newProperties);
        }

        return props;
    }

    /**
     * Writes a properties Object to file.
     * Returns true on success, false otherwise.
     * @param _file
     * @param _props
     * @return true on success, false otherwise
     */
    public static boolean writeProperties(File _file, Properties _props) {
        LOGGER.debug("Trying to write Properties to file: " + _file);
        try (FileOutputStream out = new FileOutputStream(_file)) {
            _props.store(out, _file.getName());
            LOGGER.debug("Successfully wrote properties to file: " + _file);
        } catch (IOException _ex) {
            LOGGER.warn("Could not save File: " + _file, _ex);
            return false;
        }
        return true;
    }

    /**
     * Write a properties object to a given file
     * (will overwrite existing files!).
     *
     * @param _propertiesFile - The filename you want to use
     * @param _props          - The properties to save
     * @return boolean true on success, false on exception
     */
    public static boolean writeProperties(String _propertiesFile, Properties _props) {
        return writeProperties(new File(_propertiesFile), _props);
    }

    /**
     * Reads a property as boolean from an properties object.
     * Returns true if read property matches 1, yes, true, enabled, on, y
     * @param _props
     * @param _property
     * @return
     */
    public static boolean readPropertiesBoolean(Properties _props, String _property) {
        if (_props.containsKey(_property)) {
            if (_props.getProperty(_property).matches("(?i)(1|yes|true|enabled|on|y)")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrives a text file from an given URL.
     * Url could be remote (like http://) or local (file://)
     *
     * If protocol is not specified by "protocol://" (like "http://" or "file://"),
     * "file://" is assumed.
     * @param _url
     * @return fileContent as List or null if file is empty or an error occoured
     */
    public static List<String> getTextfileFromUrl(String _url) {
        return getTextfileFromUrl(_url, Charset.defaultCharset());
    }

    /**
     * Retrives a text file from an given URL and reads the content with the given charset.
     * Url could be remote (like http://) or local (file://)
     *
     * If protocol is not specified by "protocol://" (like "http://" or "file://"),
     * "file://" is assumed.
     * @param _url
     * @param _charset
     * @return fileContent as List or null if file is empty or an error occurred.
     */
    public static List<String> getTextfileFromUrl(String _url, Charset _charset) {
        return getTextfileFromUrl(_url, _charset, false);
    }

    /**
     * @see #getTextfileFromUrl(String, Charset)
     * @param _url
     * @param _charset
     * @param _silent true to not log exceptions, false otherwise
     * @return list of string or null on error
     */
    public static List<String> getTextfileFromUrl(String _url, Charset _charset, boolean _silent) {
        if (_url == null) {
            return null;
        }
        String fileUrl = _url;
        if (!fileUrl.contains("://")) {
            fileUrl = "file://" + fileUrl;
        }

        try {
            URL dlUrl;
            if (fileUrl.startsWith("file:/")) {
                dlUrl = new URL("file", "", fileUrl.replaceFirst("file:\\/{1,2}", ""));
            } else {
                dlUrl = new URL(fileUrl);
            }
            URLConnection urlConn = dlUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setUseCaches(false);

            return readTextFileFromStream(urlConn.getInputStream(), _charset, _silent);

        } catch (IOException _ex) {
            if (!_silent) {
                LOGGER.warn("Error while reading file:", _ex);
            }
        }

        return null;
    }

    /**
     * Reads a text file from given {@link InputStream} using the given {@link Charset}.
     * @param _input stream to read
     * @param _charset charset to use
     * @param _silent true to disable exception logging, false otherwise
     * @return List of string or null on error
     */
    public static List<String> readTextFileFromStream(InputStream _input, Charset _charset, boolean _silent) {
        if (_input == null) {
            return null;
        }
        try {
            List<String> fileContent;
            try (BufferedReader dis = new BufferedReader(new InputStreamReader(_input, _charset))) {
                String s;
                fileContent = new ArrayList<>();
                while ((s = dis.readLine()) != null) {
                    fileContent.add(s);
                }
            }

            return fileContent.size() > 0 ? fileContent : null;
        } catch (IOException _ex) {
            if (!_silent) {
                LOGGER.warn("Error while reading file:", _ex);
            }
        }

        return null;
    }

    /**
     * Reads a file and returns it's content as string.
     *
     * @param _file
     * @return
     */
    public static String readFileToString(String _file) {
        List<String> localText = getTextfileFromUrl(_file);
        if (localText == null) {
            return null;
        }

        return StringUtil.join(guessLineTerminatorOfFile(_file), localText);
    }

    /**
     * Reads a file to a List&lt;String&gt; (each line is one entry in list).
     * Line endings (line feed/carriage return) are NOT removed!
     *
     * @param _file
     * @return
     */
    public static List<String> readFileToList(File _file) {
        return readFileToList(_file.getAbsolutePath());
    }

    /**
     * Reads a file to a List&lt;String&gt; (each line is one entry in list).
     * Line endings (line feed/carriage return) are NOT removed!
     *
     * @param _fileName
     * @return
     */
    public static List<String> readFileToList(String _fileName) {
        List<String> localText = getTextfileFromUrl(_fileName);
        return localText;
    }

    public static String readFileToString(File _file) {
        return readFileToString(_file.getAbsolutePath());
    }

    /**
     * Read a Resource-file (eg. Text) to String.
     *
     * @param _stream
     * @param _charset
     * @return the file as string, or null on error
     */
    public static String readStringFromResources(InputStream _stream, String _charset) {
        if (_stream == null) {
            LOGGER.error("Error: null-Stream received!");
            return null;
        }

        StringBuilder sb = new StringBuilder(100);

        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(_stream, _charset));
            for (int c = br.read(); c != -1; c = br.read()) {
                sb.append((char) c);
            }
        } catch (IOException _ex) {
            LOGGER.error("Error while reading resource to string: ", _ex);
        }

        return sb.toString();
    }

    /**
     * Loads the contents of a properties file located in the Java classpath
     * into a {@link Properties} object.
     * @param _propertiesFile properties file located in Java classpath
     * @return properties object
     * @throws IOException if file not found or loading fails for other I/O reasons
     */
    public static Properties loadPropertiesFromClasspath(String _propertiesFile) throws IOException {
        InputStream is = FileIoUtil.class.getClassLoader().getResourceAsStream(_propertiesFile);
        if (is == null) {
            throw new IOException("Resource [" + _propertiesFile + "] not found in classpath.");
        }

        Properties props = readProperties(is);
        return props;
    }

    /**
     * Same as {@link #loadPropertiesFromClasspath(String)} but does not throw checked exception.
     * The returned boolean indicates if loading was successful.
     * Read properties are stored in the given properties object (should never be null!).
     *
     * @param _propertiesFile
     * @param _properties
     * @return true if properties could be loaded, false otherwise
     */
    public static boolean loadPropertiesFromClasspath(String _propertiesFile, Properties _properties) {
        if (_properties == null) {
            throw new IllegalArgumentException("Properties object required");
        }
        try {
            Properties loaded = loadPropertiesFromClasspath(_propertiesFile);
            if (loaded != null) {
                _properties.putAll(loaded);
            }
        } catch (IOException _ex) {
            return false;
        }
        return true;
    }

    /**
     * Reads a file from classpath to String.
     *
     * @param _fileName
     * @return file contents as String or null
     */
    public static String readFileFromClassPath(String _fileName) {
        StringBuilder sb = new StringBuilder();
        for (String string : readFileFromClassPathAsList(_fileName)) {
            sb.append(string).append("\n");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /**
     * Reads a file from classpath to a list of String using default charset and log any exception.
     *
     * @param _fileName
     * @return file contents or empty list
     */
    public static List<String> readFileFromClassPathAsList(String _fileName) {
    	List<String> result = readFileFromClassPathAsList(_fileName, Charset.defaultCharset(), false);
        return result == null ? new ArrayList<String>() : result;
    }

    /**
     * Reads a file from classpath to a list of String with the given charset.
     * Will optionally suppress any exception logging.
     *
     * @param _fileName file to read
     * @param _charset charset to use for reading
     * @param _silent true to suppress error logging, false otherwise
     * @return file contents or null on exception
     */
    public static List<String> readFileFromClassPathAsList(String _fileName, Charset _charset, boolean _silent) {
        List<String> contents = new ArrayList<>();
        if (StringUtil.isBlank(_fileName)) {
            return contents;
        }
        InputStream inputStream = FileIoUtil.class.getClassLoader().getResourceAsStream(_fileName);
        if (inputStream != null) {
            try (BufferedReader dis = new BufferedReader(new InputStreamReader(inputStream, _charset))) {
                String s;

                while ((s = dis.readLine()) != null) {
                    contents.add(s);
                }
                return contents;
            } catch (IOException _ex) {
                if (!_silent) {
                    LOGGER.error("Error while reading resource to string: ", _ex);
                }
                return null;
            }
        } else {
            if (_silent) {
                return null;
            }
        }

        return contents;
    }

    /**
     * Write String to file with the given charset.
     * Optionally appends the data to the file.
     *
     * @param _fileName the file to write
     * @param _fileContent the content to write
     * @param _charset the charset to use
     * @param _append append content to file, if false file will be overwritten if existing
     *
     * @return true on successful write, false otherwise
     */
    public static boolean writeTextFile(String _fileName, String _fileContent, Charset _charset, boolean _append) {
        if (StringUtil.isBlank(_fileName)) {
            return false;
        }
        String allText = "";
        if (_append) {
            File file = new File(_fileName);
            if (file.exists()) {
                allText = readFileToString(file) + guessLineTerminatorOfFile(_fileName);
            }
        }
        allText += _fileContent;

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(_fileName), _charset);
            writer.write(allText);
        } catch (IOException _ex) {
            LOGGER.error("Could not write file to '" + _fileName + "'", _ex);
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException _ex) {
                LOGGER.error("Error while closing file '" + _fileName + "'", _ex);
                return false;
            }
        }

        return true;
    }

    /**
     * Write String to file with the java default charset (usually UTF-8).
     * Optionally appends the data to the file.
     *
     * @param _fileName the file to write
     * @param _fileContent the content to write
     * @param _append append content to file, if false file will be overwritten if existing
     *
     * @return true on successful write, false otherwise
     */
    public static boolean writeTextFile(String _fileName, String _fileContent, boolean _append) {
        return writeTextFile(_fileName, _fileContent, Charset.defaultCharset(), _append);
    }

    /**
     * Read a file from different sources depending on _searchOrder.
     * Will return the first successfully read file which can be loaded either from custom path, classpath or system path.
     *
     * @param _fileName file to read
     * @param _charset charset used for reading
     * @param _searchOrder search order
     * @return List of String with file content or null if file could not be found
     */
    public static List<String> readFileFrom(String _fileName, Charset _charset, SearchOrder... _searchOrder) {
    	InputStream stream = openInputStreamForFile(_fileName, _searchOrder);
    	if (stream != null) {
    		return readTextFileFromStream(stream, _charset, true);
    	}
    	return null;
    }

    /**
     * Tries to find _fileNameWithPath in either classpath, systempath or absolute path and opens an {@link InputStream} for it.
     * Search order is specified by {@link SearchOrder} varargs.
     *
     * @param _fileNameWithPath
     * @param _searchOrder
     * @return InputStream for the file or null if file could not be found
     */
    public static InputStream openInputStreamForFile(String _fileNameWithPath, SearchOrder... _searchOrder) {
        if (_searchOrder == null) {
            return null;
        }
        try {
            for (SearchOrder searchOrder : _searchOrder) {
                switch (searchOrder) {
                case CLASS_PATH:
                    InputStream inputStream = FileIoUtil.class.getClassLoader().getResourceAsStream(_fileNameWithPath);
                    if (inputStream != null) {
                        return inputStream;
                    }
                    break;
                case SYSTEM_PATH:
                    String pathes = System.getenv("PATH");
                    String os = System.getProperty("os.name");
                    String delimiter = ":";
                    if (os != null && os.equalsIgnoreCase("windows")) {
                        delimiter = ";";
                    }
                    if (pathes != null) {
                        String[] searchPathes = pathes.split(delimiter);
                        for (String path : searchPathes) {
                            File file = new File(path, _fileNameWithPath);
                            if (file.exists() && file.canRead()) {
                                return new FileInputStream(file);
                            }
                        }
                    }
                    break;
                case CUSTOM_PATH:
                default:
                    File file = new File(_fileNameWithPath);
                    if (file.exists() && file.canRead()) {
                        return new FileInputStream(file);
                    }
                    break;
                }
            }
        } catch (FileNotFoundException _ex) {
            return null;
        }

        return null;
    }

    /**
     * Tries to find the line termination character(s) of the given file.
     * Only useful for files with text content!
     *
     * @param _file
     * @return never null, will return system default line terminator on any error
     */
    public static String guessLineTerminatorOfFile(String _file) {
        if (StringUtil.isEmpty(_file)) {
            return SystemUtil.LINE_SEPARATOR;
        }

        File file = new File(_file);
        if (!file.exists() || !file.canRead()) {
            return SystemUtil.LINE_SEPARATOR;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            boolean carriageReturn = false;
            boolean lineFeed = false;
            boolean nextIteration = false;

            char[] buf = new char[1];
            while (reader.read(buf) != -1) {

                if (buf[0] == '\r') {
                    carriageReturn = true;
                } else if (buf[0] == '\n') {
                    lineFeed = true;
                }

                // found both, must be DOS/windows like separators
                if (carriageReturn && lineFeed) {
                    return "\r\n";
                }

                // found only carriage return, check next character as well
                if (carriageReturn && !nextIteration) {
                    nextIteration = true;
                    continue;
                }

                if (lineFeed) { // we have a line-feed and no carriage return before
                    return "\n";
                } else if (carriageReturn) { // only carriage return found before, seems to macOS 9 line ending
                    return "\r";
                }
            }
        } catch (IOException _ex) {
            return SystemUtil.LINE_SEPARATOR;
        }

        return SystemUtil.LINE_SEPARATOR;
    }

}
