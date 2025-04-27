package com.github.hypfvieh.db.persistence;

import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility to create use persistence.xml for Unit-Tests. This class will look for existing persistence.xml in the class
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
 * Sample usage:<br>
 *
 * <pre>
 * try (var context = UnitTestPersistenceLoader.ofType(DriverType.H2DB)
 *    .withDbUrl("path-to-test-h2-db"))
 *    .withDbUser("sa")
 *    .build()) {
 *    EntityManager em = context.createEntityManager();
 * }
 * </pre>
 * </p>
 */
public final class UnitTestPersistenceLoader extends AbstractBasePersistenceLoader<UnitTestPersistenceLoader> {
    private static final String              DEFAULT_PERSISTENCE_UNIT = "UnitTest";
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

    private UnitTestPersistenceLoader(DriverType driverType) {
        super(driverType, UnitTestPersistenceLoader.class);
        getAdditionalDbProps().putAll(DEFAULT_DB_SETTINGS);
    }

    public static UnitTestPersistenceLoader ofType(DriverType type) {
        return new UnitTestPersistenceLoader(type);
    }

    /**
     * Reads the existing persistence.xml and removes the "dangerous" parts.
     *
     * @return File pointing to the newly created persistence.xml
     *
     * @throws IOException when reading or writing fails
     */
    @Override
    protected byte[] createPersistenceXml() throws IOException {
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
                        elm.setAttribute("name", StringUtil.defaultIfBlank(getPersistenceUnitName(), DEFAULT_PERSISTENCE_UNIT));
                        break;
                    }

                }
            }

            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            XmlUtil.printDocument(document, boas);

            return boas.toByteArray();
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Error while preconfiguring persistence.xml", e);
        }
    }

}
