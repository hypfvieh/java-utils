package com.github.hypfvieh.db;

/**
 * Driver types supported by {@link UnitTestPersistenceLoader}. Use {@link DriverType#GENERIC} to configure any not yet
 * supported driver.
 */
public enum DriverType {
	HSQLDB("jdbc:hsqldb:file:%s;hsqldb.log_data=false;shutdown=true;sql.pad_space=false",
		"org.hsqldb.jdbc.JDBCDriver"),
	MSSQL("jdbc:sqlserver://%s", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
	GENERIC("%s", null);

	private final String urlTemplate;
	private final String driverClassName;

	DriverType(String template, String driverClassName) {
		this.urlTemplate = template;
		this.driverClassName = driverClassName;
	}

	public String getUrlTemplate() {
		return urlTemplate;
	}

	public String getDriverClassName() {
		return driverClassName;
	}
}
