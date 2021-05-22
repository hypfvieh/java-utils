package com.github.hypfvieh.config.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.xml.sax.ErrorHandler;

import com.github.hypfvieh.common.SearchOrder;
import com.github.hypfvieh.util.xml.XmlErrorHandlers.XmlErrorHandlerQuiet;

/**
 * Builder for {@link XmlConfiguration}.
 *
 * @author hypfvieh
 * @since v1.0.1 - 2018-01-15
 */
public class XmlConfigBuilder {

    private InputStream inputStream;
    private OutputStream outputStream;

    private File outputFile;

    private File inputFile;

    private ErrorHandler errorHandler = new XmlErrorHandlerQuiet();

    private String delimiterKey;
    private boolean skipRoot;
    private boolean allowOverride;

    public XmlConfigBuilder() {
    }

    /**
     * Set {@link InputStream} used to read configuration file.
     *
     * @param _inputStream input stream to read from
     * @return this for chaining
     */
    public XmlConfigBuilder setInputStream(InputStream _inputStream) {
        if (inputFile != null) {
            throw new ConfigurationException("Cannot set inputStream as inputFile is already set.");
        }
        inputStream = _inputStream;
        return this;
    }

    /**
     * Set {@link OutputStream} used to save configuration file.
     * @param _outputStream output stream to write to
     * @return this for chaining
     */
    public XmlConfigBuilder setOutputStream(OutputStream _outputStream) {
        if (outputFile != null) {
            throw new ConfigurationException("Cannot set outputStream as outputFile is already set.");
        }
        outputStream = _outputStream;
        return this;
    }

    /**
     * Set output file used for saving.
     * Note: This cannot be set if outputStream is already set.
     *
     * @param _outputFile output file to write to
     * @return this for chaining
     */
    public XmlConfigBuilder setOutputFile(File _outputFile) {
        if (outputStream != null) {
            throw new ConfigurationException("Cannot set outputFile as outputStream is already set.");
        }

        outputFile = _outputFile;
        return this;
    }

    /**
     * Set output file used for saving.
     * Note: This cannot be set if outputStream is already set.
     *
     * @param _outputFile output file to write to
     * @return this for chaining
     */
    public XmlConfigBuilder setOutputFile(String _outputFile) {
        return setOutputFile(new File(_outputFile));
    }

    /**
     * Set input file used for reading configuration.
     * Note: This cannot be set if inputStream is already set.
     *
     * @param _inputFile input file to read from
     * @param _order search order to use
     * @return this for chaining
     */
    public XmlConfigBuilder setInputFile(File _inputFile, SearchOrder... _order) {
        if (inputStream != null) {
            throw new ConfigurationException("Cannot set inputFile as inputStream is already set.");
        }
        if (_order == null || _order.length == 0) {
            _order = new SearchOrder[] {SearchOrder.CUSTOM_PATH, SearchOrder.CLASS_PATH};
        }
        InputStream findFile = SearchOrder.findFile(_inputFile.toString(), _order);

        inputFile = _inputFile;
        inputStream = findFile;

        return this;
    }

    /**
     * Set input file used for reading configuration.
     * Note: This cannot be set if inputStream is already set.
     *
     * @param _inputFile input file to read from
     * @param _order search order to use
     * @return this for chaining
     */
    public XmlConfigBuilder setInputFile(String _inputFile, SearchOrder... _order) {
        return setInputFile(new File(_inputFile), _order);
    }

    /**
     * Set the {@link ErrorHandler} used by the XML parser to handle XML errors.<br>
     * By default a 'quiet' handler is used (suppressing all errors).<br>
     * Use null here to disable validation.
     *
     * @param _xmlErrorHandler error handler to use
     * @return this for chaining
     */
    public XmlConfigBuilder setXmlErrorHandler(ErrorHandler _xmlErrorHandler) {
        errorHandler = _xmlErrorHandler;
        return this;
    }

    /**
     * Set the delimiter used to delimit XML nodes.<br>
     * By default "/" is used, which means all pathes have to be expressed like:<br>
     * foo/bar/baz
     *
     * @param _delimiterKey delimiting string
     * @return this for chaining
     */
    public XmlConfigBuilder setKeyDelimiter(String _delimiterKey) {
        delimiterKey = _delimiterKey;
        return this;
    }

    /**
     * Set this to true to ignore the first level of nodes inside the config.<br>
     * Usually each XML is starting with a single node (e.g. &lt;Config&gt;) and all<br>
     * keys are below that node. <br><br>
     * If this is set to true, you can use e.g. key/subKey instead of Config/key/subKey to address
     * your values.
     * <br><br>
     * Default is false.
     *
     * @param _skipRoot true to skip root, false otherwise
     * @return this for chaining
     */
    public XmlConfigBuilder setSkipRoot(boolean _skipRoot) {
        skipRoot = _skipRoot;
        return this;
    }

    /**
     * Set this to true to allow the user to override config parameters using system properties.<br>
     * This means, if key foo/bar is available in config and a environment variable of the same name<br>
     * exists, the value of the environment variable is used, instead of the value in config.
     * <br><br>
     * This allows the user to easily override values by e.g. using -Dfoo/bar=value instead of editing the config file every time.
     * <br><br>
     * Default is false
     *
     * @param _allowOverride true to enable override, false otherwise
     * @return this for chaining
     */
    public XmlConfigBuilder setAllowKeyOverrideFromEnvironment(boolean _allowOverride) {
        allowOverride = _allowOverride;
        return this;
    }

    /**
     * Build the {@link XmlConfiguration} and return it.
     * Will create a new {@link XmlConfiguration} on each call.
     * @return {@link XmlConfiguration}
     */
    public XmlConfiguration build() {
        XmlConfiguration xmlConfiguration = new XmlConfiguration(skipRoot);

        xmlConfiguration.setKeyDelimiter(delimiterKey);
        xmlConfiguration.setXmlErrorHandler(errorHandler);
        xmlConfiguration.setAllowOverride(allowOverride);

        xmlConfiguration.setInputStream(inputStream);
        xmlConfiguration.setOutputStream(outputStream);

        xmlConfiguration.setInputFile(inputFile);
        xmlConfiguration.setOutputFile(outputFile);

        try {
            xmlConfiguration.readAndValidate();
        } catch (IOException _ex) {
            throw new ConfigurationException(_ex);
        }

        return xmlConfiguration;
    }
}
