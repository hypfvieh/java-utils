package com.github.hypfvieh.db.persistence;

import com.github.hypfvieh.util.StringUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * Base class to create persistence.xml files on the fly.
 */
public abstract class AbstractBasePersistenceLoader<T extends AbstractBasePersistenceLoader<T>> {

    private final DriverType                          driverType;

    private PersistenceLoaderContext                  context;

    private String                                    dbUrl;
    private String                                    dbUser;
    private String                                    dbPass;
    private String                                    jdbcDriver;
    private String                                    persistenceUnitName;
    private Map<String, String>                       additionalDbProps;

    private final Class<T> self;

    AbstractBasePersistenceLoader(DriverType _driverType, Class<T> _self) {
        self = _self;
        driverType = Objects.requireNonNull(_driverType);
        jdbcDriver = _driverType.getDriverClassName();
    }

    protected T self() {
        return self.cast(this);
    }

    /**
     * Database URL to use.
     * <p>
     * The provided URL must not start with jdbc: if the used DriverType is not GENERIC.
     * If you chose GENERIC you have to provide the full connection string including the prefix.
     * </p>
     * <p>
     * The URL can/should contain all additional options supported by the database driver.
     * </p>
     *
     * @param _dbUrl url
     * @return this
     */
    public T withDbUrl(String _dbUrl) {
        Objects.requireNonNull(_dbUrl, "dbUrl must not be null");

        if (driverType == DriverType.GENERIC && !_dbUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("dbUrl must start with 'jdbc:' when using GENERIC driver type");
        } else if (driverType != DriverType.GENERIC && _dbUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("dbUrl must not start with 'jdbc:' when using non-GENERIC driver type");
        }

        this.dbUrl = _dbUrl;
        return self();
    }

    /**
     * Setup database username.
     *
     * @param _dbUser username
     *
     * @return this
     */
    public T withDbUser(String _dbUser) {
        this.dbUser = _dbUser;
        return self();
    }

    /**
     * Setup database user password.
     *
     * @param _dbPass password
     *
     * @return this;
     */
    public T withDbPass(String _dbPass) {
        this.dbPass = _dbPass;
        return self();
    }

    /**
     * Set the jdbcDriver to use.
     * <p>
     * Required if you use {@link DriverType#GENERIC}.<br>
     * In all other cases the driver will be set by the used {@link DriverType}, but can be overwritten using this
     * method.
     * </p>
     *
     * @param _jdbcDriver driver class name
     *
     * @return this
     */
    public T withJdbcDriver(String _jdbcDriver) {
        this.jdbcDriver = Objects.requireNonNull(_jdbcDriver, "jdbcDriver must not be null");
        return self();
    }


    /**
     * Persistence unit to load from persistence.xml file.
     * <p>
     * Will fall back to {@link #DEFAULT_PERSISTENCE_UNIT} when not set.
     * </p>
     *
     * @param _persistenceUnitName persistence unit name
     *
     * @return this
     */
    public T withPersistenceUnitName(String _persistenceUnitName) {
        persistenceUnitName = _persistenceUnitName;
        return self();
    }

    /**
     * Add additional (arbitrary) properties passed to {@link Persistence#createEntityManagerFactory(String, Map)}.
     *
     * @param _additionalDbProps properties
     *
     * @return this
     */
    public T withAdditionalDbProps(Map<String, String> _additionalDbProps) {
        additionalDbProps = _additionalDbProps;
        return self();
    }

    /**
     * Creates a new configured instance which manages the created EntityManagers.
     * If {@link #build()} was already called, the same instance will be returned.
     *
     * @return {@link PersistenceLoaderContext}
     */
    public PersistenceLoaderContext build() {
        if (context == null) {
            context = new PersistenceLoaderContext(this);
        }

        return context;
    }

    /**
     * Creates the perisistence.xml which will be loaded later.
     * @return byte[]
     * @throws IOException when creation fails
     */
    protected abstract byte[] createPersistenceXml() throws IOException;

    protected String getDbUser() {
        return dbUser;
    }

    protected String getDbPass() {
        return dbPass;
    }

    protected String getJdbcDriver() {
        return jdbcDriver;
    }

    protected DriverType getDriverType() {
        return driverType;
    }

    protected String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    protected Map<String, String> getAdditionalDbProps() {
        return additionalDbProps;
    }

    protected String getDbUrl() {
        return dbUrl;
    }

    public enum PersistenceXmlRootProp {
        XMLNS("xmlns", "https://jakarta.ee/xml/ns/persistence"),
        XMLNS_XSI("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"),
        XSI_SCHEMALOCATION("xsi:schemaLocation",
        "https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"),
        VERSION("version", "3.0");

        private final String keyName;
        private final String defaultValue;

        PersistenceXmlRootProp(String _keyName, String _defaultValue) {
            keyName = _keyName;
            defaultValue = _defaultValue;
        }

        String getKeyName() {
            return keyName;
        }

        String getDefaultValue() {
            return defaultValue;
        }

    }

    /**
     * Context which will manage the created {@link EntityManager}s and {@link EntityManagerFactory}.
     * The created instance is closeable and will close all {@link EntityManager}
     * and the {@link EntityManagerFactory} when {@link #close()} is called.
     */
    public static final class PersistenceLoaderContext implements Closeable {
        private final Set<EntityManager> entityManagers = new HashSet<>();
        private final AbstractBasePersistenceLoader<?>  loader;
        private EntityManagerFactory     entityManagerFactory;

        PersistenceLoaderContext(AbstractBasePersistenceLoader<?> _loader) {
            StringUtil.requireNonBlank(_loader.getPersistenceUnitName(), "PersistenceUnitName required");
            StringUtil.requireNonBlank(_loader.getDbUrl(), "Database URL required");
            loader = _loader;
        }

        /**
         * Creates a new {@link EntityManager}.
         * <p>
         * On the first call to this method, an {@link EntityManagerFactory} is created using the configured connection
         * parameters. Any subsequent call will reuse the same {@link EntityManagerFactory} instance to create
         * {@link EntityManager}s.
         * </p>
         * <p>
         * This means, changing the connection properties <b>AFTER</b> the first call to this method are ignored. Every call
         * to this method will create a <b>NEW</b> {@link EntityManager} instance!
         * </p>
         * <p>
         * All created {@link EntityManager}s are stored internally and will be closed when {@link #close()} is called.
         * </p>
         *
         * @return new {@link EntityManager}
         *
         * @throws IOException when creating {@link EntityManagerFactory} fails
         */
        public EntityManager createEntityManager() throws IOException {
            if (entityManagerFactory == null) {
                entityManagerFactory = createEntityManagerFactory();
            }

            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManagers.add(entityManager);

            return entityManager;
        }

        /**
         * Creates or returns a previously created {@link EntityManager}.
         * <p>
         * A {@link EntityManager} will be created when no {@link EntityManager} was created before by calling this method
         * or {@link #createEntityManager()}.
         * </p>
         * <p>
         * This method will always return an opened {@link EntityManager}, this may be any {@link EntityManager} previously
         * created or a new one if there is no opened {@link EntityManager}.
         * </p>
         *
         * @return EntityManager
         *
         * @throws IOException when creation of {@link EntityManagerFactory} fails
         */
        public EntityManager getOrCreateEntityManager() throws IOException {
            if (entityManagers.isEmpty()) {
                return createEntityManager();
            } else {
                entityManagers.removeIf(em -> !em.isOpen());

                return entityManagers.stream()
                    .filter(EntityManager::isOpen)
                    .findFirst()
                    .orElse(createEntityManager());
            }
        }

        /**
         * Creates a new {@link EntityManagerFactory} using the configured settings.
         * <p>
         * <b>Please note:</b><br>
         * The created {@link EntityManagerFactory} must be closed by the caller!
         * </p>
         *
         * @return {@link EntityManagerFactory}
         *
         * @throws IOException when creating temporary persistence.xml failed
         */
        public EntityManagerFactory createEntityManagerFactory() throws IOException {
            if (entityManagerFactory != null) {
                return entityManagerFactory;
            }

            byte[] inMemoryData = loader.createPersistenceXml();

            JpaEnhancedClassLoader myClassLoader = new JpaEnhancedClassLoader(getClass().getClassLoader(),
                inMemoryData);
            Thread.currentThread().setContextClassLoader(myClassLoader);

            String url = String.format(loader.getDriverType().getUrlTemplate(), loader.getDbUrl());

            Map<String, String> properties = new HashMap<>();

            if (loader.getAdditionalDbProps() != null) {
                properties.putAll(loader.getAdditionalDbProps());
            }
            properties.put("jakarta.persistence.jdbc.url", url);
            properties.put("jakarta.persistence.jdbc.driver", loader.getJdbcDriver());
            properties.put("jakarta.persistence.jdbc.user", StringUtil.trimToEmpty(loader.getDbUser()));
            properties.put("jakarta.persistence.jdbc.password", StringUtil.trimToEmpty(loader.getDbPass()));

            return Persistence.createEntityManagerFactory(loader.getPersistenceUnitName(), properties);
        }

        /**
         * Closes all {@link EntityManager}s and the {@link EntityManagerFactory} if any was created.
         */
        @Override
        public void close() {
            if (!entityManagers.isEmpty()) {
                entityManagers.forEach(EntityManager::close);
                entityManagers.clear();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
                entityManagerFactory = null;
            }
        }

    }
}
