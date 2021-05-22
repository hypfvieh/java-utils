package com.github.hypfvieh.config.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.github.hypfvieh.util.ConverterUtil;
import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.TypeUtil;
import com.github.hypfvieh.util.xml.XmlUtil;

/**
 * Slim replacement for commons-configuration2 XMLConfiguration.
 *
 * This class handles configurations saved as XML. It does not do DTD/XSD validation.
 * @author hypfvieh
 * @since v1.0.1 - 2018-01-15
 */
public class XmlConfiguration {

    private Document xmlDocument;
    private OutputStream outputStream;
    private File outputFile;

    private String keyDelimiter = "/";
    private File inputFile;
    private InputStream inputStream;
    private ErrorHandler errorHandler;
    private boolean skipRootLevel;
    private boolean allowOverride;

    /**
     * Constructor, only called from {@link XmlConfigBuilder}.
     * @param _skipRootLevel skip the root node of the config
     */
    XmlConfiguration(boolean _skipRootLevel) {
        skipRootLevel = _skipRootLevel;

    }

    /**
     * Get the delimiting key (default is '/').
     * @return current delimiting key
     */
    public String getKeyDelimiter() {
        return keyDelimiter;
    }

    /**
     * Set the delimiting key.<br>
     * If null is given, default ('/') will be used.
     * @param _keyDelimiter delimiter to use
     */
    public void setKeyDelimiter(String _keyDelimiter) {
        if (_keyDelimiter == null) {
            keyDelimiter = "/";
        } else {
            keyDelimiter = Pattern.quote(_keyDelimiter);
        }
    }

    /**
     * Get the amount of nodes in config.
     * @return nodes in document
     */
    public int keyCount() {
        return countNodes(getXmlDocument());
    }

    /**
     * Get the String value behind the given key.
     * Shortcut of getString(String, null).
     * @param _key key to look for
     * @return value stored in config or null if value not found
     */
    public String getString(String _key) {
        return getString(_key, null);
    }

    /**
     * Get the string value behind the given key or return default.<br>
     * Will first try to pick up key from environment variables (if enabled),<br>
     * if that fails, config will be used.<br>
     * If this also fails, default is returned.
     *
     * @param _key key to look for
     * @param _default default to return if key not found
     * @return value stored in environment/config or default if key not found
     */
    public String getString(String _key, String _default) {
        String result = getStringFromEnv(_key);
        if (result == null) {

            String[] split = _key.split(keyDelimiter);
            Node node = findNode(split);
            if (node == null) {
                return _default;
            }

            if (node.getNodeName().equals(split[split.length -1])) {
                result = node.getTextContent();
            } else {
                if (XmlUtil.isElementType(node)) {
                    result = XmlUtil.toElement(node).getAttribute(split[split.length -1]);
                }
            }
        }
        return result == null ? _default : result;
    }

    /**
     * Get a configuration value as int.
     * If _key not found, or not an integer, default is returned.
     *
     * @param _key key to read
     * @param _default default to return if key not found/invalid
     * @return value as int or default
     */
    public int getInt(String _key, int _default) {
        String valAsStr = getString(_key, String.valueOf(_default));
        if (valAsStr == null || !TypeUtil.isInteger(valAsStr)) {
            return _default;
        } else {
            return Integer.parseInt(valAsStr);
        }
    }

    /**
     * Retrieve a configuration value as double.
     * If _key could not be found, or value was not of type 'double', default is returned.
     *
     * @param _key key to read
     * @param _default default to return if key not found/invalid
     * @return value as double or default
     */
    public double getDouble(String _key, double _default) {
        String valAsStr = getString(_key, _default + "");
        if (valAsStr == null || !TypeUtil.isDouble(valAsStr)) {
            return _default;
        } else {
            return Double.parseDouble(valAsStr);
        }
    }

    /**
     * Retrieve a configuration value as boolean.
     * If _key could not be found, default is returned.<br>
     * All other values will be tried to read as boolean.<br>
     * <br>
     * Considered true values are:<br>
     * <ul>
     *  <li>1</li>
     *  <li>y</li>
     *  <li>j</li>
     *  <li>ja</li>
     *  <li>yes</li>
     *  <li>true</li>
     *  <li>enabled</li>
     *  <li>enable</li>
     *  <li>active</li>
     * </ul>
     * All other values are considered to be false.
     * <br>
     * @param _key key to read
     * @param _default default to return if key not found/invalid
     * @return boolean or default value
     */
    public boolean getBoolean(String _key, boolean _default) {
        String valAsStr = getString(_key, String.valueOf(_default));
        if (StringUtil.isBlank(valAsStr)) {
            return _default;
        } else {
            return ConverterUtil.strToBool(valAsStr);
        }
    }

    /**
     * Set string value of given key
     * @param _key key to write
     * @param _asAttribute set this as attribute instead of node
     * @param _value value to write
     */
    public void setString(String _key, boolean _asAttribute, String _value) {
        String[] split = _key.split(keyDelimiter);
        Node findNode = findNode(split);

        if (_asAttribute) {
            if (XmlUtil.isElementType(findNode)) {
                XmlUtil.toElement(findNode).setAttribute(split[split.length -1], _value);
            }
        } else {
            findNode.setTextContent(_value);
        }
    }

    /**
     * Get all values behind the given key as list of string.
     * @param _key key to read
     * @return list maybe empty, never null
     */
    private List<String> getList(String _key) {
        Node findNode = findNode(_key.split(keyDelimiter));

        if (findNode == null) {
            return new ArrayList<>();
        }

        String[] keys = null;
        if (!findNode.hasChildNodes() || findNode.getChildNodes().getLength() <= 1) {
            findNode = findNode.getParentNode();
            keys = _key.split(keyDelimiter);
        }

        List<String> values = new ArrayList<>();
        NodeList childNodes = findNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (keys != null) {
                if (childNodes.item(i).getNodeName().equals(keys[keys.length -1])) {
                    values.add(childNodes.item(i).getTextContent());
                }
            } else {
                if (XmlUtil.isElementType(childNodes.item(i))) {
                    values.add(childNodes.item(i).getTextContent());
                }
            }
        }

        return values;
    }

    /**
     * Returns the results of key as ArrayList.
     *
     * @param _key key to read
     * @return never null, maybe empty list
     */
    public List<String> getStringList(String _key) {

        String str = getStringFromEnv(_key);
        if (str != null) {
            if (str.contains("~|~")) {
                return TypeUtil.createList(str.split("~\\|~"));
            } else {
                return TypeUtil.createList(str);
            }
        }

        List<String> list = getList(_key);
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    /**
     * Returns a result as Set of the given Set-Subclass.
     * If given Set-Class is null or could not be instantiated, TreeSet is used.
     *
     * @param _key key to read
     * @param _setClass Set class to use
     * @return set of string maybe empty
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<String> getStringSet(String _key, Class<? extends Set> _setClass) {
        if (_setClass == null) {
            _setClass = TreeSet.class;
        }

        List<String> list = getStringList(_key);

        Set<String> newInstance;
        try {
            newInstance = _setClass.getDeclaredConstructor().newInstance();
            newInstance.addAll(list);

            return newInstance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException _ex) {
            return new TreeSet<>(list);
        }
    }

    /**
     * Set keys found in map to value found in map.
     *
     * @param _values values to write
     * @param _asAttributes write values as attributes instead of nodes
     */
    public void setValues(Map<String, String> _values, boolean _asAttributes) {
        for (Entry<String, String> entry : _values.entrySet()) {
            setString(entry.getKey(), _asAttributes, entry.getValue());
        }
    }

    /**
     * Save config file.
     * Will replace input file!
     * @throws IOException if file writing fails
     */
    public void save() throws IOException {
        List<IOException> exception = new ArrayList<>();

        // this is a bit messy, but we want to stay compatible with XmlUtil.printDocument
        // so we save the (maybe) thrown exception in a list to throw it afterwards
        save((t, u) -> {
            try {
                XmlUtil.printDocument(t, u);
            } catch (IOException _ex) {
                exception.add(_ex);
            }
        });

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
    }

    /**
     * Save config using {@link BiConsumer}.
     * This allows transforming/formatting of output before it is saved to the configured output stream.
     *
     * @param _outputGenerator lambda to modify output with before writing
     * @throws IOException if writing fails
     */
    public void save(BiConsumer<Document, OutputStream> _outputGenerator) throws IOException {
        OutputStream output = null;
        if (outputStream != null) {
            output = outputStream;
        } else if (outputFile != null) {
            output = new FileOutputStream(outputFile, false);
        }

        if (output == null) {
            throw new IOException("No output stream or file given. Cannot save changes");
        }

        _outputGenerator.accept(xmlDocument, output);
    }

    /**
     * Get file which is used as input
     * @return file, maybe null if input is read from stream
     */
    public File getInputFile() {
        return inputFile;
    }

    /**
     * Sets input file.
     * @param _inputFile
     */
    void setInputFile(File _inputFile) {
        inputFile = _inputFile;
    }

    /**
     * Sets output file.
     * @param _outputFile
     */
    void setOutputFile(File _outputFile) {
        outputFile = _outputFile;
    }

    /**
     * Sets input stream.
     * @param _inputStream
     */
    void setInputStream(InputStream _inputStream) {
        inputStream = _inputStream;
    }

    /**
     * Sets output stream.
     * @param _outputStream
     */
    void setOutputStream(OutputStream _outputStream) {
        outputStream = _outputStream;
    }

    /**
     * Sets errorHandler used by XML parser.
     * @param _errorHandler
     */
    void setXmlErrorHandler(ErrorHandler _errorHandler) {
        errorHandler = _errorHandler;
    }

    /**
     * Allow/disallow overriding of keys using environment variables.
     * @param _allowOverride
     */
    void setAllowOverride(boolean _allowOverride) {
        allowOverride = _allowOverride;
    }

    /**
     * Read the config file, do validation if configured.
     * @throws IOException
     */
    void readAndValidate() throws IOException {
        Objects.requireNonNull(inputStream, "InputStream cannot be null");

        DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
        dbFac.setNamespaceAware(false);

        if (errorHandler != null) {
            dbFac.setValidating(true);
        }

        try {
            DocumentBuilder builder = dbFac.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            xmlDocument = builder.parse(inputStream);
        } catch (SAXException | ParserConfigurationException | IOException _ex) {
            throw new IOException(_ex);
        }
    }

    /**
     * Returns the xmlDocument as Node or returns the first node level.
     * @return
     */
    private Node getXmlDocument() {
        if (skipRootLevel) {
            return xmlDocument.getDocumentElement();
        } else {
            return xmlDocument;
        }
    }

    /**
     * Find a node by name.
     * @param split
     * @return
     */
    private Node findNode(String[] split) {

        if (split == null) {
            return null;
        }

        Node node = getXmlDocument();
        boolean found = false;
        for (String k : split) {
            found = false;
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                Node item = node.getChildNodes().item(i);
                if (item.getNodeName().equals(k)) {
                    node = item;
                    found = true;
                    break;
                }
            }
        }

        // only return the node if it was found in the loop and is not the same as the initial value
        return found ? node : null;
    }

    /**
     * Recursively count all nodes.
     *
     * @param _node
     * @return
     */
    private int countNodes(Node _node) {
        if (_node == null) {
            return 0;
        }
        Node node = _node;
        int count = 0;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (XmlUtil.isElementType(item)) {
                count++;
                if (item.hasChildNodes()) {
                    count += countNodes(item);
                }
            }
        }
        return count;
    }

    private String getStringFromEnv(String _key) {
        if (!allowOverride) {
            return null;
        }
        if (System.getProperties().containsKey(_key)) {
            String sysProp = System.getProperties().getProperty(_key);
            System.getLogger(getClass().getName()).log(Level.DEBUG, "Config-Property '{}' is overridden by environment variable, using value '{}'", _key, sysProp);

            return sysProp;
        }
        return null;
    }

}
