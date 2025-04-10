package com.github.hypfvieh.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;


/**
 * Specialised classloader to inject temporary {@code persistence.xml}.
 */
public class JpaEnhancedClassLoader extends ClassLoader {

	public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

	private final String alternativePersistenceXmlPath;

	/**
	 * Create new specialized class loader.
	 * @param parent parent class loader for delegation
	 * @param alternativePersistenceXmlPath file path of the "alternative" persistence.xml
	 */
	public JpaEnhancedClassLoader(ClassLoader parent, String alternativePersistenceXmlPath) {
		super(parent);
		this.alternativePersistenceXmlPath = Objects.requireNonNull(alternativePersistenceXmlPath);
	}

	@Override
	public URL getResource(String name) {
		if (name.endsWith(PERSISTENCE_XML)) {
			try {
				return new File(alternativePersistenceXmlPath).toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		return super.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (name.endsWith(PERSISTENCE_XML)) {
			return Collections.enumeration(List.of(new File(alternativePersistenceXmlPath).toURI().toURL()));
		}
		return super.getResources(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if (name.endsWith(PERSISTENCE_XML)) {
			try {
				return new FileInputStream(alternativePersistenceXmlPath);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return super.getResourceAsStream(name);
	}

}
