package com.github.hypfvieh.db;

import com.github.hypfvieh.util.StringUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to update H2 file databases.<br>
 * <p>
 * When H2 database driver is updated converting the existing database to a new file format is often required.<br>
 * Sadly H2 does not provide any suitable method of doing this without a lot of manual interaction.
 * </p>
 * <p>
 * Purpose of this tool is, to allow converting H2 from one version to another without manual interaction or usage of<br>
 * multiple JVMs to run different versions of the H2 driver.
 * </p>
 * <p>
 * To use this tool, use the {@link H2UpdaterBuilder} to configure a update instance.<br>
 * Use {@link H2UpdaterBuilder#build} to create a new {@link H2Updater} instance and call {@link H2Updater#convert(String, String)} to<br>
 * actually convert the database file.
 * </p>
 * <p>
 * The different H2 drivers required for conversion must be given when creating a {@link H2UpdaterBuilder} instance.<br>
 * Therefore there is no need to put both driver in class path, because the driver is not picked from class path but from the given<br>
 * JAR files. <br>
 * So you can use the Update without messing up your class path with different H2 versions.
 * </p> 
 * 
 * @author hypfvieh
 * @since 1.2.1 - 2023-11-16
 */
public final class H2Updater {

    private final H2UpdaterBuilder bldr;
    
    private H2Updater(H2UpdaterBuilder _bldr) {
        bldr = _bldr;
    }

    /**
     * Convert the configured databases using the given credentials.
     * @param _dbUsername username
     * @param _dbPassword password
     * 
     * @throws H2UpdaterException when converting fails 
     */
    public void convert(String _dbUsername, String _dbPassword) throws H2UpdaterException {

        String dbUser = Optional.ofNullable(_dbUsername).map(String::trim).filter(s -> !s.isEmpty()).orElse(null); 
        String dbPass = Optional.ofNullable(_dbPassword).map(String::trim).filter(s -> !s.isEmpty()).orElse(null);

        String exportPw = StringUtil.randomString(32);
        File dumpFile = exportDatabase(dbUser, dbPass, exportPw);
        importDatabase(dbUser, dbPass, exportPw, dumpFile);
    }
    
    private void importDatabase(String _dbUsername, String _dbPassword, String _exportPw, File _dumpFile) throws H2UpdaterException {
        try {
            Class<?> importH2Driver = Class.forName("org.h2.Driver", true, bldr.getOutputH2Classloader());
            String importQry = "RUNSCRIPT FROM '" + _dumpFile.getAbsolutePath() + "' COMPRESSION LZF CIPHER AES PASSWORD '"  + _exportPw + "'";
            
            if (bldr.importExportCharset != null) {
                importQry += " CHARSET '" + bldr.importExportCharset + "'";
            }
            
            if (!bldr.importOptions.isEmpty()) {
                importQry += " " + bldr.importOptions.stream().map(Objects::toString).collect(Collectors.joining(" "));
            }
            
            String dbUrl = String.format("jdbc:h2:%s", bldr.getOutputFileName());
            
            try (Connection importConnection = createConnection(importH2Driver, dbUrl, _dbUsername, _dbPassword);
                Statement importStatement = importConnection.createStatement()) {
                
                importStatement.execute(importQry);
            }
        } catch (Exception _ex) {
            throw new H2UpdaterException("Unable to import data to new database", _ex);
        }
    }

    private File exportDatabase(String _dbUsername, String _dbPassword, String _exportPw) throws H2UpdaterException {
        String exportQry = "SCRIPT";
        if (!bldr.exportOptions.isEmpty()) {
            exportQry += " " + bldr.exportOptions.stream().map(Objects::toString).collect(Collectors.joining(" "));
        }
        
        try {
            File createTempFile = File.createTempFile(getClass().getSimpleName(), "export.sql");
            
            exportQry += " TO '" + createTempFile.getAbsolutePath() + "'" 
                + "COMPRESSION LZF CIPHER AES PASSWORD '"  + _exportPw + "'";
            
            if (bldr.importExportCharset != null) {
                exportQry += " CHARSET '" + bldr.importExportCharset + "'";
            }

            Class<?> h2Driver = Class.forName("org.h2.Driver", true, bldr.getInputH2ClassLoader());

            try (Connection exportConnection = createConnection(h2Driver, bldr.getInputJdbcUrl(), _dbUsername, _dbPassword);
                Statement exportStatement = exportConnection.createStatement()) {
                
                exportStatement.execute(exportQry);
            }
            return createTempFile;
        } catch (Exception _ex) {
            throw new H2UpdaterException("Unable to export old database", _ex);
        }
    }
    
    /**
     * Creates a connection to the H2 database using reflection (without using DriverManager).
     * 
     * @param _h2Driver h2 driver class
     * @param _url URL to connect to
     * @param _user database user
     * @param _password database user password
     * @return Connection
     * 
     * @throws InstantiationException on reflection problems
     * @throws IllegalAccessException on reflection problems
     * @throws IllegalArgumentException on reflection problems
     * @throws InvocationTargetException on reflection problems
     * @throws NoSuchMethodException on reflection problems
     * @throws SecurityException on reflection problems
     * @throws SQLException when connection fails
     * @throws RuntimeException when given class was not a sql driver compatible class
     */
    private static Connection createConnection(Class<?> _h2Driver, String _url, String _user, String _password) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
        if (java.sql.Driver.class.isAssignableFrom(_h2Driver)) {
            Driver driverInstance = (Driver) _h2Driver.getDeclaredConstructor().newInstance();
            Properties prop = new Properties();
            if (_user != null) {
                prop.setProperty("user", _user);
            }
            if (_password != null) {
                prop.setProperty("password", _password);
            }
            return driverInstance.connect(_url, prop);
        }
        throw new RuntimeException("Given driver " + _h2Driver + " is not a java.sql.Driver");
    }
 
    /**
     * Builder to create H2Updater instances.
     */
    public static class H2UpdaterBuilder {
        private ClassLoader inputH2ClassLoader;
        private ClassLoader outputH2Classloader;

        private String inputJdbcUrl;
        private String outputFileName;

        private String importExportCharset;
        
        private final Set<ExportOption> exportOptions = new LinkedHashSet<>();
        private final Set<ImportOption> importOptions = new LinkedHashSet<>();
        
        private H2UpdaterBuilder(ClassLoader _inputClzLdr, ClassLoader _outputClzLdr) {
            inputH2ClassLoader = _inputClzLdr;
            outputH2Classloader = _outputClzLdr;
        }

        ClassLoader getInputH2ClassLoader() {
            return inputH2ClassLoader;
        }

        ClassLoader getOutputH2Classloader() {
            return outputH2Classloader;
        }

        String getInputJdbcUrl() {
            return inputJdbcUrl;
        }

        String getOutputFileName() {
            return outputFileName;
        }

        /**
         * Setup the input URL to the database file which should be converted.
         * 
         * @param _url url to database
         * 
         * @return this
         * 
         * @throws H2UpdaterException when input URL is invalid 
         */
        public H2UpdaterBuilder withInputJdbcUrl(String _url) throws H2UpdaterException {
            if (_url == null || !_url.startsWith("jdbc:h2:")) {
                throw new H2UpdaterException("Invalid input jdbc url '" + _url + "'. Url has to start with jdbc:h2:");
            }
            inputJdbcUrl = _url;
            
            return this;
        }

        /**
         * Setup the output database file name.<br>
         * This must be different to the input file name and the file must not exist.<br>
         * The file extension (usually .mv.db) must be omitted (will be appended by H2 automatically).<br>
         * 
         * @param _outputFileName name and path of new (converted) database
         * 
         * @return this
         * 
         * @throws H2UpdaterException when outputFileName is invalid 
         */
        public H2UpdaterBuilder withOutputFileName(String _outputFileName) throws H2UpdaterException {
            if (_outputFileName == null || _outputFileName.isBlank()) {
                throw new H2UpdaterException("Output database file name cannot be null or blank");
            }
            
            File checkFile = new File(_outputFileName.endsWith(".mv.db") ? _outputFileName : _outputFileName + ".mv.db");
            if (checkFile.exists()) {
                throw new H2UpdaterException("Output database must not exist");
            }
            
            outputFileName = _outputFileName;
            
            return this;
        }
        
        /**
         * Setup the charset to use for export and importing the database.
         * 
         * @param _charset charset to use, null to use default 
         * 
         * @return this
         */
        public H2UpdaterBuilder withImportExportCharset(Charset _charset) {
            if (_charset == null) {
                importExportCharset = null;
            } else {
                importExportCharset = _charset.name();
            }
            return this;
        }
        
        /**
         * Configure various options used for exporting the original database.
         * 
         * @param _opt option
         * 
         * @return this
         * 
         * @throws H2UpdaterException when invalid combinations are used
         */
        public H2UpdaterBuilder addExportOption(ExportOption _opt) throws H2UpdaterException {
            if (_opt == null) {
                return this;
            }
            
            if (_opt == ExportOption.NODATA && exportOptions.contains(ExportOption.SIMPLE) || exportOptions.contains(ExportOption.COLUMNS)) {
                throw new H2UpdaterException("NODATA option cannot be used in combination with SIMPLE or COLUMNS option");
            }
            if (_opt == ExportOption.SIMPLE || _opt == ExportOption.COLUMNS && exportOptions.contains(ExportOption.NODATA)) {
                throw new H2UpdaterException("SIMPLE or COLUMNS option cannot be used in combination NODATA option");
            }
            
            exportOptions.add(_opt);
            
            return this;
        }

        /**
         * Remove a previously set {@link ExportOption}.
         * 
         * @param _opt option to remove
         * 
         * @return this
         */
        public H2UpdaterBuilder addRemoveExportOption(ExportOption _opt) {
            exportOptions.remove(_opt);
            return this;
        }
        
        /**
         * Configure various options used for importing the original database to the new database.
         * 
         * @param _opt option
         * 
         * @return this
         * 
         * @throws H2UpdaterException when invalid combinations are used
         */
        public H2UpdaterBuilder addImportOption(ImportOption _opt) throws H2UpdaterException {
            if (_opt == null) {
                return this;
            }
            
            if (_opt == ImportOption.FROM_1X && importOptions.contains(ImportOption.QUIRKS_MODE) || importOptions.contains(ImportOption.VARIABLE_BINARY)) {
                throw new H2UpdaterException("FROM_1X option cannot be used in combination with QUIRKS_MODE or VARIABLE_BINARY option");
            }
            if (_opt == ImportOption.QUIRKS_MODE || _opt == ImportOption.VARIABLE_BINARY && importOptions.contains(ImportOption.FROM_1X)) {
                throw new H2UpdaterException("QUIRKS_MODE or VARIABLE_BINARY option cannot be used in combination FROM_1X option");
            }
            
            importOptions.add(_opt);
            
            return this;
        }

        /**
         * Remove a previously set {@link ImportOption}.
         * 
         * @param _opt option to remove
         * 
         * @return this
         */
        public H2UpdaterBuilder addRemoveImportOption(ImportOption _opt) {
            importOptions.remove(_opt);
            return this;
        }
        
        /**
         * Creates a new H2Updater ensuring all required properties are set.
         * 
         * @return H2Updater
         * 
         * @throws H2UpdaterException when configuration is invalid or incomplete 
         */
        public H2Updater build() throws H2UpdaterException {
            if (inputJdbcUrl == null) {
                throw new H2UpdaterException("Setup inputJdbUrl using withInputJdbcUrl() first");
            }
            if (outputFileName == null) {
                throw new H2UpdaterException("Setup outputFileName using withOutputFileName() first");
            }
            
            return new H2Updater(this);
        }
        
        /**
         * Create a new builder instance to configure H2Updater.
         * 
         * @param _inputH2Jar jar file of H2 to read database to convert
         * @param _outputH2Jar jar file of H2 to write converted database with
         * 
         * @return this
         * 
         * @throws H2UpdaterException when loading jars failed 
         */
        public static H2UpdaterBuilder create(File _inputH2Jar, File _outputH2Jar) throws H2UpdaterException {
            return new H2UpdaterBuilder(loadJar(_inputH2Jar), loadJar(_outputH2Jar));
        }
        
        /**
         * Setup a custom classloader for the given jar file.
         * Used to support loading of the same classes from different versions of H2.
         *  
         * @param _h2Jar jar of h2 to load
         * 
         * @return ClassLoader instance
         * 
         * @throws H2UpdaterException jar file cannot be loaded 
         */
        private static ClassLoader loadJar(File _h2Jar) throws H2UpdaterException {
            if (_h2Jar == null || !_h2Jar.exists() || !_h2Jar.canRead()) {
                throw new H2UpdaterException("Given H2 jar " + _h2Jar + " does not exist or cannot be read");
            }
            try {
                return new URLClassLoader(new URL[] { _h2Jar.toURI().toURL() });
            } catch (MalformedURLException _ex) {
                throw new H2UpdaterException("Unable to convert file path to URL", _ex);
            }
        }
    }
    
    /**
     * Options to configure export of original database.
     */
    public static enum ExportOption {
        /** Do not create INSERT statements (only structure will be exported). */
        NODATA,
        /** Do not use multi row INSERT statements. */
        SIMPLE,
        /** Add explicit column names to every INSERT statement. */
        COLUMNS,
        /** Do not export stored passwords. */
        NOPASSWORDS,
        /** Do not export database settings (SET xxx). */
        NOSETTINGS,
        /** Create DROP statement for every table before default CREATE statement. */
        DROP
    }
    
    public static enum ImportOption {
        /** 
         * Various compatibility quirks for scripts from older versions of H2. 
         * Use this option when importing scripts that were generated by H2 1.4.200 or older.
         */
        QUIRKS_MODE,
        /**
         * BINARY data type will be parsed as VARBINARY. 
         * Use this when importing scripts that were generated by H2 1.4.200 or older. 
         */
        VARIABLE_BINARY,
        /**
         * Use this flag to populate a new database with the data exported from 1.*.* versions of H2.
         */
        FROM_1X;
    }
    
    /**
     * Exception which will be thrown if something went wrong.
     */
    public static class H2UpdaterException extends Exception {
        private static final long serialVersionUID = 1L;

        public H2UpdaterException(String _message, Throwable _cause) {
            super(_message, _cause);
        }

        public H2UpdaterException(String _message) {
            super(_message);
        }
        
    }
}
