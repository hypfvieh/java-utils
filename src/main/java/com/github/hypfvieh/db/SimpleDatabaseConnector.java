package com.github.hypfvieh.db;

import java.io.InvalidClassException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.TypeUtil;

/**
 * Simple class to allow connection to any JDBC compatible database.
 * Allows to send any SQL statement to the database as well as retrieving data (SELECT) from a database in a List-of-Map format.
 * <br><br>
 * All columns selected will be accessible by iterating through the list of maps.
 * Each item of the list represents one row as HashMap where each key is one column.
 * Column names are <b>case insensitive</b>!
 * <br><br>
 * It allows caching of retrieved DB results in JSON format. If caching is enabled it will automatically use the offline cache if
 * database is unreachable.
 * <br><br>
 * One instance of {@link SimpleDatabaseConnector} can store multiple select queries in offline cache (map of SQL-Query(key) and Result (value)).
 * <br><br>
 * Sample Usage:
 *
 * <pre>
 * {@code
    IDatabaseConnector sdc = newSqlConnectorInstance(getDatabaseConnectionParameters("EnxMasterdata"), true); // use false to disable cache

        if (sdc.openDatabase() || sdc.isOffline()) { // if cache is disabled you should stop if isOffline() == true
            List&lt;Map&lt;String, String&gt;&gt; selectedRows = sdc.executeSelectQuery(true, sqlGattungString);
            for (Map&lt;String, String&gt; tableEntry : selectedRows) {
                isin2enx.put(tableEntry.get("isin"), tableEntry.get("enx_code"));
            }
        }
    }
 *  </pre>
 *
 * @author hypfvieh
 * @since 1.0.1
 */
public class SimpleDatabaseConnector {
    private final Logger        logger;

    private final AtomicInteger connectionRetries;

    private boolean             dbOpen           = false;
    private boolean             supportsBatch    = false;

    private DbConnParms         connectionParams = null;
    private Connection          dbConnection     = null;

    public SimpleDatabaseConnector(DbConnParms _connectionParams) {
        if (_connectionParams == null) {
            throw new IllegalArgumentException("Database connection parameters cannot be null.");
        }

        logger = System.getLogger(getClass().getName());

        dbOpen = false;
        connectionParams = _connectionParams;

        connectionRetries = new AtomicInteger(0);
    }

    /**
     * Trys to open a connection to the given database parameters.
     * Returns true if connection could be established, false otherwise.
     *
     * Throws InvalidClassException if given SQL-Driver is not JDBC compatible.
     * Throws ClassNotFoundException if given SQL-Driver class could not be found.
     * Throws SQLException on any connection error.
     *
     * @return true if connected, false otherwise
     * @throws InvalidClassException if class is not a java.sql.Driver derivative
     * @throws ClassNotFoundException if class could not be found
     */
    public final synchronized boolean openDatabase() throws InvalidClassException, ClassNotFoundException {
        if (dbOpen) {
            logger.log(Level.WARNING, "Connection to database already opened.");
            return dbOpen;
        }
        Class<?> driverClazz;
        try {
            driverClazz = Class.forName(connectionParams.getDriverClassName());
            Object driverInstance = driverClazz.getDeclaredConstructor().newInstance();
            if (!(driverInstance instanceof Driver)) {
                logger.log(Level.ERROR, "{} does not implement java.sql.Driver interface!", connectionParams.getDriverClassName());
                throw new InvalidClassException(connectionParams.getDriverClassName() + " does not implement java.sql.Driver interface!");
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException _ex) {
            logger.log(Level.ERROR, "Cannot instanciate database driver: " + connectionParams.getDriverClassName(), _ex);
        }

        // open the connection or switch to offline mode if connection fails
        openConnection();

        if (dbOpen && dbConnection != null) {
            // if database could be opened, check if we can use batch processing, as it is much faster
            try {
                if (dbConnection.getMetaData().supportsBatchUpdates()) {
                    supportsBatch = true;
                }
            } catch (SQLException _ex) {
                logger.log(Level.INFO, "Could not determine if database supports batch update feature.", _ex);
            }
        }

        return dbOpen;
    }

    /**
     * This method opens the DB connection.<br>
     * If this fails and file caching is allowed, {@link #offline} mode will be enabled and null is returned.
     * @return Connection or null
     */
    private void openConnection() {
        try {
            dbConnection = DriverManager.getConnection(connectionParams.getUrl(), connectionParams.getUser(), connectionParams.getPassword());
            dbOpen = true;
            logger.log(Level.DEBUG, "Connection to database at: {} established", connectionParams.getUrl());
        } catch (SQLRecoverableException _ex) { // is thrown if connection could not be established, so we may retry connection
            if (connectionRetries.incrementAndGet() > connectionParams.getMaxRetries()) {
                logger.log(Level.ERROR, "Connection could not be established within {} attempts, url={}", connectionParams.getMaxRetries(), connectionParams.getUrl());
            } else {
                logger.log(Level.ERROR, "Connection could not be established. Reconnection attempt #{} of {}, url={}, exception={}", connectionRetries.get(), connectionParams.getMaxRetries(), connectionParams.getUrl(), _ex.getMessage());
                openConnection();
            }
        } catch (SQLException _ex) {
            logger.log(Level.ERROR, "Database at [{}] could not be opened and offline cache was disabled.", connectionParams.getUrl());
            // if debug logging is enabled, print exception as well (may help analyzing issues)
            logger.log(Level.DEBUG, "Exception was: ", _ex);
        }
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    /**
     * Closes the database connection if it was previously connected.
     * @throws SQLException if closing fails
     */
    public synchronized void closeDatabase() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
        }
        dbOpen = false;
    }

    /**
     * Returns true if database has been opened.
     * @return true if open, false otherwise
     */
    public synchronized boolean isDbOpen() {
        return dbOpen;
    }

    /**
     * Run update/inserts as batch update.
     * Will fallback to sequential insert/update if database implemenation does not support batch.<br><br>
     *
     * <b>Attention:</b> Do not use batch when calling stored procedures in oracle databases, as this is not supported by oracle and will not work.<br><br>
     *
     * @param _sqlQuery sql query to use (with '?' placeholders)
     * @param _sqlParameters an array of values for replacing '?' placeholders in query
     * @param _batchSize batch size to use
     * @return true on successful execution, false if any error occurred
     */
    public synchronized boolean executeBatchQuery(String _sqlQuery, List<Object[]> _sqlParameters, int _batchSize) {
        if (dbConnection == null) {
            logger.log(Level.ERROR, "Database connection for [{}] not established yet", connectionParams);
            return false;
        }

        if (_sqlParameters == null || _sqlParameters.isEmpty()) {
            return false;
        } else if (isSupportsBatch()) {
            logger.log(Level.DEBUG, "About to perform {} updates with batch size {}.", _sqlParameters.size(), _batchSize);
            List<List<Object[]>> splitList = TypeUtil.splitList(_sqlParameters, _batchSize);
            boolean hasError = false;
            try {
                for (List<Object[]> batchPart : splitList) {

                    try (PreparedStatement stmt = dbConnection.prepareStatement(_sqlQuery)) {
                        for (Object[] sqlParams : batchPart) {
                            if (sqlParams != null) {
                                for (int i = 0; i < sqlParams.length; i++) {
                                    stmt.setObject(i + 1, sqlParams[i]);
                                }
                                stmt.addBatch();
                            }
                        }
                        int[] numUpdates = stmt.executeBatch();
                        for (int i = 0; i < numUpdates.length; i++) {
                            if (numUpdates[i] == Statement.SUCCESS_NO_INFO) {
                                logger.log(Level.TRACE, "Execution of batch {}: successful, but unknown number of rows affected", i);
                            } else if (numUpdates[i] == Statement.EXECUTE_FAILED) {
                                logger.log(Level.ERROR, "Execution of batch {}/{} failed, parms: {}", i, numUpdates.length, Arrays.toString(batchPart.get(i)));
                                hasError = true;
                            } else {
                                logger.log(Level.TRACE, "Execution of batch {} successful.", i);
                            }
                        }
                    }
                }
                return !hasError;
            } catch (SQLException _ex) {
                logger.log(Level.ERROR, "Error while processing batch.",_ex);
                return false;
            }
        } else {
            logger.log(Level.WARNING, "Using serial insert/update as database implementation does not support batch update!");
            boolean hasError = false;
            for (Object[] args : _sqlParameters) {
                if (logger.isLoggable(Level.DEBUG)) { // do not do conversion of array to list if logging is disabled
                    logger.log(Level.DEBUG, "Executing query {} with arguments: {}", _sqlQuery, Arrays.asList(args));
                }
                if (!executeQuery(_sqlQuery, args)) {
                    hasError = true;
                }
            }
            return !hasError;
        }
    }

    /**
     * Executes an sql query.
     *
     * @param _sql query to execute
     * @param _args arguments to fill-in placeholders in query, can be omitted if none needed
     * @return true if SQL-query returns an update-count, false on error or if result is resultset instead of update count.
     */
    public synchronized boolean executeQuery(String _sql, Object... _args) {
        if (dbConnection == null) {
            logger.log(Level.ERROR, "Database connection for [{}] not established yet", connectionParams);
            return false;
        }

        try (PreparedStatement ps = dbConnection.prepareStatement(_sql)) {
            if (_args != null) {
                for (int i = 0; i < _args.length; i++) {
                    ps.setObject(i + 1, _args[i]);
                }
            }
            return !ps.execute();
        } catch (SQLException _ex) {
            logger.log(Level.ERROR, "Failed to execute sql statement: " + _sql, _ex);
        }
        return false;
    }

    /**
     * Returns the result of an SQL Select-PreparedStatement as list of maps where each key in the map is a column.
     *
     * @param _sql the sql statement to execute (can use '?' placeholder which will be replaced by the parameters in _args)
     * @param _args parameters to replace '?'- placeholder insert _sql (if none, this can be omitted)
     * @return list of maps with the result of the query, each list entry is one row of the database
     */
    public synchronized List<Map<String, String>> executeSelectQuery(String _sql, Object... _args) {
        if (dbConnection == null) {
            logger.log(Level.ERROR, "Database connection for [{}] not established yet", connectionParams);
            return null;
        }

        List<Map<String, String>> queryResult = new ArrayList<>();

        try (PreparedStatement ps = dbConnection.prepareStatement(_sql)) {
            if (_args != null) {
                for (int i = 0; i < _args.length; i++) {
                    ps.setObject(i + 1, _args[i]);
                }
            }
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
                        String columnName = result.getMetaData().getColumnLabel(i + 1);
                        map.put(columnName, result.getString(i + 1));
                    }
                    queryResult.add(map);
                }
            }
        } catch (SQLException _ex) {
            logger.log(Level.ERROR, "Failed to execute sql statement: " + _sql, _ex);
        }

        logger.log(Level.DEBUG, "Query: '{}' returned {} rows with parms: {}", _sql, queryResult.size(), Arrays.toString(_args));

        return queryResult;

    }

    /**
     * Creates a prepared statement which can be used for batch statements.
     * @param _sql sql to create prepared statement for
     * @return new prepared statement or null on error
     */
    public synchronized PreparedStatement createPreparedStatement(String _sql) {
        if (dbConnection == null || !isDbOpen()) {
            logger.log(Level.ERROR, "Could not create prepared statement: database connection missing for [{}]", connectionParams);
            return null;
        }
        if (StringUtil.isBlank(_sql)) {
            logger.log(Level.ERROR, "Could not create prepared statement: statement cannot be empty or null");
            return null;
        }

        PreparedStatement ps;
        try {
            ps = dbConnection.prepareStatement(_sql);
        } catch (SQLException _ex) {
            logger.log(Level.ERROR, "Could not create prepared statement: ", _ex);
            return null;
        }

        return ps;
    }

    /**
     * Execute a previously created prepared statement (update or delete).
     * @param _ps prepared statement to execute
     * @return true if execution successfully, false otherwise
     */
    public synchronized boolean executeQuery(PreparedStatement _ps) {
        if (dbConnection == null) {
            logger.log(Level.ERROR, "Database connection for [{}] not established yet", connectionParams);
            return false;
        }
        if (_ps == null) {
            logger.log(Level.ERROR, "Statement should not be null!");
            return false;
        }

        try {
            return !_ps.execute();
        } catch (SQLException _ex) {
            logger.log(Level.ERROR, "Failed to execute sql statement:", _ex);
            return false;
        }
    }

    public synchronized boolean isSupportsBatch() {
        return supportsBatch;
    }

    /**
     * Enable/Disable autocommit on database connection.
     * @param _onOff enable/disable autocommit
     *
     * @throws SQLException if autocommit option cannot be changed
     */
    public void setAutoCommit(boolean _onOff) throws SQLException {
        if (dbConnection != null) {
            dbConnection.setAutoCommit(_onOff);
        }
    }

    /**
     * Returns status of autocommit option.
     * @return true if autocommit enabled, false otherwise
     * @throws SQLException if autocommit option status could not be determined
     */
    public boolean isAutoCommit() throws SQLException {
        if (dbConnection != null) {
            return dbConnection.getAutoCommit();
        }
        return false;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[open=" + dbOpen + ", connectionParams=" + connectionParams + "]";
    }

}
