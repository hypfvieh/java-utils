package com.github.hypfvieh.db.persistence;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
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

    private final PersistenceXmlInMemoryStreamHandler inMemHandler;

	/**
	 * Create new specialized class loader.
	 * @param _parent parent class loader for delegation
	 * @param _alternativePersistenceXmlPath file path of the "alternative" persistence.xml
	 */
	public JpaEnhancedClassLoader(ClassLoader _parent, String _alternativePersistenceXmlPath) {
		super(_parent);
		alternativePersistenceXmlPath = Objects.requireNonNull(_alternativePersistenceXmlPath);
		inMemHandler = null;
	}

    /**
     * Create new specialized class loader.
     * @param _parent parent class loader for delegation
     * @param _inMemoryData byte array containing persistence.xml
     */
    public JpaEnhancedClassLoader(ClassLoader _parent, byte[] _inMemoryData) {
        super(_parent);
        alternativePersistenceXmlPath = null;
        inMemHandler = new PersistenceXmlInMemoryStreamHandler(_inMemoryData);
    }

	@Override
	public URL getResource(String name) {
        if (name.endsWith(PERSISTENCE_XML)) {
            try {
                if (alternativePersistenceXmlPath != null) {
                    return new File(alternativePersistenceXmlPath).toURI().toURL();
                } else {
                    return createUrl();
                }
            } catch (MalformedURLException e) {
                return null;
            }
		}
		return super.getResource(name);
	}

    private URL createUrl() throws MalformedURLException {
        return new URL("persistence-mem", "", -1, PERSISTENCE_XML, inMemHandler);
    }

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (name.endsWith(PERSISTENCE_XML)) {
		    if (alternativePersistenceXmlPath != null) {
		        return Collections.enumeration(List.of(new File(alternativePersistenceXmlPath).toURI().toURL()));
		    } else {
		        return Collections.enumeration(List.of(createUrl()));
		    }
		}
		return super.getResources(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if (name.endsWith(PERSISTENCE_XML)) {
			try {
			    if (alternativePersistenceXmlPath != null) {
			        return new FileInputStream(alternativePersistenceXmlPath);
			    } else {
			        return new ByteArrayInputStream(inMemHandler.inMemoryData);
			    }
			} catch (IOException e) {
				return null;
			}
		}
		return super.getResourceAsStream(name);
	}

	private class PersistenceXmlInMemoryStreamHandler extends URLStreamHandler {
	    private static final String PROTOCOL = "persistence-mem";

        private byte[] inMemoryData;

        public PersistenceXmlInMemoryStreamHandler(byte[] _inMemoryData) {
            inMemoryData = _inMemoryData;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            if (!u.getProtocol().equals(PROTOCOL)) {
                throw new IOException(u.getProtocol() + " not supported");
            }

            return new URLConnection(u) {

                private byte[] data = null;

                @Override
                public void connect() throws IOException {
                    connected = true;
                }

                @Override
                public long getContentLengthLong() {
                    return inMemoryData.length;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(data);
                }

            };
        }

    }
}
