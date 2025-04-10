package com.github.hypfvieh.db;

import com.github.hypfvieh.util.StringUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Utility to create use persistence.xml in Unit-Tests. This class will look for existing persistence.xml in the class
 * path and uses this as template to create a persistence.xml for unit testing.
 * <p>
 * <b>This is only designed for testing as it uses a temporary persistence.xml file and also changes the context class
 * loader!</b>
 * </p>
 * <p>
 * It is assumed that there is only one persistence-unit in the persistence.xml file. The tool will ensure that this
 * persistence-unit is renamed to the desired (or default) name. It will also remove any properties defined in the
 * persistence.xml. Instead, all properties have to be defined using the builder patterns. Certain default properties
 * are always set.
 * </p>
 * <p>
 * For convenience there are pre-defined driver types which will allow you to specify the target URL without any
 * prefixes (like 'jdbc:h2:file:' etc.). The DriverType will also ensure that the proper JDBC driver is configured.
 * </p>
 * <p>
 * Sample usage:<br>
 *
 * <pre>
 * try (var em = UnitTestPersistenceLoader.ofType(UnitTestPersistenceLoader.DriverType.HSQLDB)
 *     .withDbUrl("path-to-test-hsql-db")
 *     .createEntityManager()) {
 *     // do something
 * }
 * </pre>
 * </p>
 */
public final class UnitTestPersistenceLoader implements Closeable {
    private static final String              DEFAULT_PERSISTENCE_UNIT = "Intranet-UnitTest";
    private static final String              FILE_PREFIX              = "persistence-";
    private static final String              FILE_SUFFIX              = ".xml";

    private static final Map<String, String> DEFAULT_DB_SETTINGS      = java.util.Map.of(
        "jakarta.persistence.schema-generation.database.action", "none",
        "hibernate.mapping.ignore_not_found", "true",
        "hibernate.hbm2ddl.skip_foreign_keys", "true",
        "hibernate.cache.use_second_level_cache", "false",
        "hibernate.cache.use_query_cache", "false",
        "hibernate.enable_lazy_load_no_trans", "true",
        "hibernate.integration.envers.enabled", "false");

    private final DriverType                 driverType;

    private EntityManagerFactory             entityManagerFactory;
    private final Set<EntityManager>         entityManagers;

    private String                           dbUrl;
    private String                           dbUser;
    private String                           dbPass;
    private String                           jdbcDriver;
    private String                           persistenceUnitName;
    private Map<String, String>              additionalDbProps;

    private UnitTestPersistenceLoader(DriverType driverType) {
        this.driverType = Objects.requireNonNull(driverType);
        jdbcDriver = driverType.getDriverClassName();
        entityManagers = new HashSet<>();
    }

    public static UnitTestPersistenceLoader ofType(DriverType type) {
        return new UnitTestPersistenceLoader(type);
    }

    public static EntityManagerFactory createEntityManagerFactory(String dbUrl, String persistenceUnitName, Map<String, String> dbProps) {

        Map<String, String> properties = new HashMap<>(DEFAULT_DB_SETTINGS);
        properties.putAll(dbProps);
        properties.put("jakarta.persistence.jdbc.url", dbUrl);

        return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
    }

    public UnitTestPersistenceLoader withDbUrl(String dbUrl) {
        Objects.requireNonNull(dbUrl, "dbUrl must not be null");

        if (driverType == DriverType.GENERIC && !dbUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("dbUrl must start with 'jdbc:' when using GENERIC driver type");
        } else if (driverType != DriverType.GENERIC && dbUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("dbUrl must not start with 'jdbc:' when using non-GENERIC driver type");
        }

        this.dbUrl = dbUrl;
        return this;
    }

    /**
     * Setup database username.
     *
     * @param dbUser username
     *
     * @return this
     */
    public UnitTestPersistenceLoader withDbUser(String dbUser) {
        this.dbUser = dbUser;
        return this;
    }

    /**
     * Setup database user password.
     *
     * @param dbPass password
     *
     * @return this;
     */
    public UnitTestPersistenceLoader withDbPass(String dbPass) {
        this.dbPass = dbPass;
        return this;
    }

    /**
     * Set the jdbcDriver to use.
     * <p>
     * Required if you use {@link DriverType#GENERIC}.<br>
     * In all other cases the driver will be set by the used {@link DriverType}, but can be overwritten using this
     * method.
     * </p>
     *
     * @param jdbcDriver driver class name
     *
     * @return this
     */
    public UnitTestPersistenceLoader withJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = Objects.requireNonNull(jdbcDriver, "jdbcDriver must not be null");
        return this;
    }

    /**
     * Persistence unit to load from persistence.xml file.
     * <p>
     * Will fall back to {@link #DEFAULT_PERSISTENCE_UNIT} when not set.
     * </p>
     *
     * @param persistenceUnitName persistence unit name
     *
     * @return this
     */
    public UnitTestPersistenceLoader withPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    /**
     * Add additional (arbitrary) properties passed to {@link Persistence#createEntityManagerFactory(String, Map)}.
     *
     * @param additionalDbProps properties
     *
     * @return this
     */
    public UnitTestPersistenceLoader withAdditionalDbProps(Map<String, String> additionalDbProps) {
        this.additionalDbProps = additionalDbProps;
        return this;
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
        if (StringUtil.isBlank(jdbcDriver)) {
            throw new IllegalStateException("jdbcDriver must not be set");
        }
        File tmpPersistenceXml = copyAndPreConfigurePersistenceXml();

        JpaEnhancedClassLoader myClassLoader = new JpaEnhancedClassLoader(getClass().getClassLoader(),
            tmpPersistenceXml.getAbsolutePath());
        Thread.currentThread().setContextClassLoader(myClassLoader);

        String url = String.format(driverType.getUrlTemplate(), dbUrl);

        Map<String, String> properties = new HashMap<>();
        DEFAULT_DB_SETTINGS.forEach((k, v) -> {
            properties.put(k, StringUtil.trimToNull(v));
        });

        if (additionalDbProps != null) {
            properties.putAll(additionalDbProps);
        }
        properties.put("jakarta.persistence.jdbc.url", url);
        properties.put("jakarta.persistence.jdbc.driver", jdbcDriver);
        properties.put("jakarta.persistence.jdbc.user", StringUtil.trimToEmpty(dbUser));
        properties.put("jakarta.persistence.jdbc.password", StringUtil.trimToEmpty(dbPass));

        return Persistence.createEntityManagerFactory(StringUtil.defaultIfBlank(persistenceUnitName, DEFAULT_PERSISTENCE_UNIT), properties);
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
            return entityManagers.stream()
                .filter(EntityManager::isOpen)
                .findFirst()
                .orElse(createEntityManager());
        }
    }

    /**
     * Reads the existing persistence.xml and removes the "dangerous" parts.
     *
     * @return File pointing to the newly created persistence.xml
     *
     * @throws IOException when reading or writing fails
     */
    private File copyAndPreConfigurePersistenceXml() throws IOException {
        try (InputStream persistenceXmlStream = getClass().getResourceAsStream("/META-INF/persistence.xml")) {
            if (persistenceXmlStream == null) {
                throw new IllegalStateException("persistence.xml not found in classpath");
            }

            // caution: the filename must match the length of '/META-INF/persistence.xml' -> 26
            String persistenceXmlFileName = FILE_PREFIX + StringUtil.randomString(
                    JpaEnhancedClassLoader.PERSISTENCE_XML.length() - FILE_PREFIX.length() - FILE_SUFFIX.length());

            File fileName = new File(System.getProperty("java.io.tmpdir"), persistenceXmlFileName);
            fileName.deleteOnExit();

            DocumentBuilderFactory dbFac = DocumentBuilderFactory.newInstance();
            dbFac.setNamespaceAware(false);
            dbFac.setValidating(false);

            Document document = dbFac.newDocumentBuilder().parse(persistenceXmlStream);

            NodeList childNodes = document.getChildNodes().item(0).getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i) instanceof Element) {
                    Element elm = (Element) childNodes.item(i);
                    if (elm.getNodeName().equals("persistence-unit")) {

                        NodeList jtaDataSources = elm.getElementsByTagName("jta-data-source");
                        if (jtaDataSources.getLength() > 0) {
                            elm.removeChild(jtaDataSources.item(0));
                        }
                        NodeList properties = elm.getElementsByTagName("properties");
                        if (properties.getLength() > 0) {
                            elm.removeChild(properties.item(0));
                        }
                        elm.setAttribute("name", StringUtil.defaultIfBlank(persistenceUnitName, DEFAULT_PERSISTENCE_UNIT));
                        break;
                    }

                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(document);

            FileWriter writer = new FileWriter(fileName);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            return fileName;
        } catch (ParserConfigurationException | TransformerException | SAXException e) {
            throw new IOException("Error while preconfiguring persistence.xml", e);
        }
    }

    /**
     * Closes all {@link EntityManager}s and the {@link EntityManagerFactory} if any was created.
     */
    @Override
    public void close() {
        if (!entityManagers.isEmpty()) {
            entityManagers.forEach(EntityManager::close);
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }
}
