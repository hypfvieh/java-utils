package com.github.hypfvieh.db;

import java.util.Map;

/**
 * Instances of this immutable class hold database connection parameters.
 *
 * @author hypfvieh
 * @since 1.0.1
 */
public class DbConnParms {

    // CHECKSTYLE:OFF
    /** Database connection url. */
    private final String url;
    /** Database user name. */
    private final String user;
    /** Database password. */
    private final String password;
    /** Fully qualified driver class name. */
    private final String driverClassName;
    /** Max retries to establish connection (Default: 3). */
    private final int maxRetries;

    // CHECKSTYLE:ON

    public DbConnParms(String _url, String _user, String _password, String _driverClassName) {
        this(_url, _user, _password, _driverClassName, 3);
    }

    public DbConnParms(String _url, String _user, String _password, String _driverClassName, int _maxRetries) {
        if (_url == null || _user == null || _driverClassName == null) {
            throw new IllegalArgumentException("Url, user, driverClassName required to create new " + getClass().getName() + ".");
        }
        this.url = _url;
        this.user = _user;
        this.password = _password;
        this.driverClassName = _driverClassName;
        this.maxRetries = _maxRetries;
    }

    public DbConnParms(Map<String, String> _parms) {
        this(_parms.get("URL"), _parms.get("USER"), _parms.get("PASS"), _parms.get("DRIVER"));
    }

    @Override
    public String toString() {
        String pw = (password == null || password.isEmpty()) ? "NO_PASSWORD_SET" : "****";
        return getClass().getSimpleName() + "[url=" + url + ", user=" + user + ", password=" + pw + ", dbDriver=" + driverClassName + ", maxRetries=" + maxRetries + "']";
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

}
