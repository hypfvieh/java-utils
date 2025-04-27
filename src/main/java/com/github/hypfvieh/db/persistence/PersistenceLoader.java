package com.github.hypfvieh.db.persistence;

import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility to create persistence.xml files on the fly.
 * <p>
 * Usually Java Persistence API (JPA) uses a persistence.xml found in META-INF and there is no feature which allows using other locations or filenames.
 * </p>
 * <p>
 * For convenience there are pre-defined driver types which will allow you to specify the target URL without any
 * prefixes (like 'jdbc:h2:' etc.). The DriverType will also ensure that the proper JDBC driver is configured.
 * </p>
 * <p>
 * Sample usage:<br>
 * <pre>
 * try (var context = PersistenceLoader.ofType(DriverType.H2DB)
 *     .withDbUrl("path-to-h2-db-file")
 *     .withDbUser("sa")
 *     .withPersistenceProvider("org.hibernate.jpa.HibernatePersistenceProvider")
 *     .build()) {
 *     EntityManager em = context.createEntityManager();
 * }
 * </pre>
 */
public class PersistenceLoader extends AbstractBasePersistenceLoader<PersistenceLoader> {
    private final Map<PersistenceXmlRootProp, String> persistenceXmlRootProps;
    private final Map<String, List<String>>           additionalPersistenceXmlElements;
    private final Map<String, String>                 additionalPersistenceXmlProperties;

    private String                                    persistenceXmlProvider;

    private PersistenceLoader(DriverType _driverType) {
        super(_driverType, PersistenceLoader.class);
        additionalPersistenceXmlElements = new LinkedHashMap<>();
        persistenceXmlRootProps = new LinkedHashMap<>();
        additionalPersistenceXmlProperties = new LinkedHashMap<>();
    }

    public static PersistenceLoader ofType(DriverType _type) {
        return new PersistenceLoader(_type);
    }

    /**
     * Set the persistence.xml root properties.
     * <p>
     * The root properties are the options which will be set in the root &lt;persistence&gt; element of
     * the configuration file.
     * </p>
     *
     * @param _prop property to set
     * @param _value value for property
     * @return this
     */
    public PersistenceLoader withPersistenceXmlRootProp(PersistenceXmlRootProp _prop, String _value) {
        if (_prop == null) {
            return self();
        }

        if (_value == null) {
            persistenceXmlRootProps.remove(_prop);
        } else {
            persistenceXmlRootProps.put(_prop, _value);
        }

        return self();
    }

    /**
     * Setup properties which will be added to the &lt;properties&gt; element of the &lt;persistence-unit&gt; element.
     *
     * @param _key property name
     * @param _value property value
     * @return this
     */
    public PersistenceLoader withPersistenceXmlUnitProperty(String _key, String _value) {
        if (_key == null) {
            return self();
        }

        if (_value == null) {
            additionalPersistenceXmlProperties.remove(_key);
        } else {
            additionalPersistenceXmlProperties.put(_key, _value);
        }

        return self();
    }

    /**
     * Sets the elements which will be added below the &lt;persistence-unit&gt; element.
     * <p>
     * This operation supports adding multiple same elements with different values.
     * This can be used to define the classes which should be used by JPA.
     * Providing {@code null} as value for a key will remove the key if it exists.
     * </p>
     * <p>
     * <b>Note:</b> If you call this method multiple times with the same key, the additional values will be added to that key.
     * The already provided values will not be overwritten/removed.
     * If you want to remove any value you have to remove the key and re-adding all remaining items by yourself.
     * </p>
     * <p>
     * <b>Caution:</b> The provided options are not validated, so you may add an arbitrary option which may
     * not be supported by JPA and will then cause issues when trying to load this config.
     * </p>
     * @param _key element to create
     * @param _value value of element
     * @return this
     */
    public PersistenceLoader withPersistenceXmlUnitElement(String _key, List<String> _value) {
        if (_key == null) {
            return self();
        }

        if (_value == null) {
            additionalPersistenceXmlElements.remove(_key);
        } else {
            if (additionalPersistenceXmlElements.containsKey(_key)) {
                additionalPersistenceXmlElements.get(_key).addAll(_value);
            } else {
                additionalPersistenceXmlElements.put(_key, new ArrayList<>(_value));
            }
        }

        return self();
    }

    /**
     * Set the value of the persistence provider.
     * <p>
     * <b>This value is required</b> and should be a FQCN of a library providing a JPA implementation.
     * For example hibernate would be {@code org.hibernate.jpa.HibernatePersistenceProvider}.
     * </p>
     *
     * @param _provider provider
     * @return this
     */
    public PersistenceLoader withPersistenceXmlProvider(String _provider) {
        persistenceXmlProvider = _provider;
        return self();
    }

    @Override
    protected byte[] createPersistenceXml() throws IOException {
        StringUtil.requireNonBlank(getPersistenceUnitName(), "PersistenceUnitName required");
        StringUtil.requireNonBlank(persistenceXmlProvider, "PersistenceProvider required");

        DocumentBuilderFactory docFacBuilder = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFacBuilder.newDocumentBuilder();
        } catch (ParserConfigurationException _ex) {
            throw new IOException("Error creating XML parser", _ex);
        }

        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);
        doc.setXmlVersion("1.0");

        Element rootElem = doc.createElement("persistence");

        Arrays.stream(PersistenceXmlRootProp.values()).forEach(k -> {
            String val = persistenceXmlRootProps.get(k);
            rootElem.setAttribute(k.getKeyName(), val == null ? k.getDefaultValue() : val);
        });

        Element persistenceUnit = doc.createElement("persistence-unit");
        persistenceUnit.setAttribute("name", getPersistenceUnitName());
        rootElem.appendChild(persistenceUnit);

        Element providerElem = doc.createElement("provider");
        providerElem.setTextContent(persistenceXmlProvider);

        persistenceUnit.appendChild(providerElem);

        if (!additionalPersistenceXmlElements.isEmpty()) {
            additionalPersistenceXmlElements.forEach((k, v) -> {
                v.forEach(e -> {
                    Element element = doc.createElement(k);
                    element.setTextContent(e);
                    persistenceUnit.appendChild(element);
                });
            });
        }

        if (!additionalPersistenceXmlProperties.isEmpty()) {
            Element propertiesElem = doc.createElement("properties");
            persistenceUnit.appendChild(propertiesElem);
            additionalPersistenceXmlProperties.forEach((k ,v) -> {
                Element element = doc.createElement("property");
                element.setAttribute("name", k);
                element.setAttribute("value", v);
                propertiesElem.appendChild(element);
            });
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlUtil.printDocument(doc, baos);
        return baos.toByteArray();
    }

}
