package com.github.hypfvieh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to setup SSL/TLS related settings.
 *
 * @author hypfvieh
 * @since 2016-10-14
 */
public final class SslTlsUtil {

    private static final String STORETYPE_JCEKS       = "jceks";
    private static final String STORETYPE_JKS         = "jks";
    private static final String STORETYPE_PKCS12      = "pkcs12";
    private static final String STORETYPE_DER_ENCODED = "cer";

    private static final Logger LOGGER = LoggerFactory.getLogger(SslTlsUtil.class);

    private SslTlsUtil() {

    }

    /**
     * Initialization of trustStoreManager used to provide access to the configured trustStore.
     *
     * @param _trustStoreFile trust store file
     * @param _trustStorePassword trust store password
     * @return TrustManager array or null
     * @throws IOException on error
     */
    public static TrustManager[] initializeTrustManagers(File _trustStoreFile, String _trustStorePassword) throws IOException {
        if (_trustStoreFile == null) {
            return null;
        }

        String storeType = getStoreTypeByFileName(_trustStoreFile);

        boolean derEncoded = storeType == STORETYPE_DER_ENCODED;
        if (derEncoded) {
            storeType = STORETYPE_JKS;
        }

        String trustStorePwd = StringUtil.defaultIfBlank(_trustStorePassword, System.getProperty("javax.net.ssl.trustStorePassword"));

        LOGGER.debug("Creating trust store of type '" + storeType + "' from " + (derEncoded ? "DER-encoded" : "") + " file '" + _trustStoreFile + "'");

        try {
            TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            KeyStore trustStore = KeyStore.getInstance(storeType);
            if (derEncoded) {
                FileInputStream fis = new FileInputStream(_trustStoreFile);
                X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);
                trustStore.load(null, null);
                trustStore.setCertificateEntry("[der_cert_alias]", certificate);
            } else {
                trustStore.load(new FileInputStream(_trustStoreFile), trustStorePwd != null ? trustStorePwd.toCharArray() : null);
            }
            trustMgrFactory.init(trustStore);
            return trustMgrFactory.getTrustManagers();
        } catch (GeneralSecurityException _ex) {
            throw new IOException("Error while setting up trustStore", _ex);
        }
    }

    /**
     * Initialization of keyStoreManager used to provide access to the configured keyStore.
     *
     * @param _keyStoreFile key store file
     * @param _keyStorePassword key store password
     * @param _keyPassword key password
     * @return KeyManager array or null
     * @throws IOException on error
     */
    public static KeyManager[] initializeKeyManagers(File _keyStoreFile, String _keyStorePassword, String _keyPassword) throws IOException {
        if (_keyStoreFile == null) {
            return null;
        }

        String keyStorePwd = StringUtil.defaultIfBlank(_keyStorePassword, System.getProperty("javax.net.ssl.keyStorePassword"));
        if (StringUtil.isBlank(keyStorePwd)) {
            keyStorePwd = "changeit";
        }

        String keyPwd = StringUtil.defaultIfBlank(_keyPassword, System.getProperty("javax.net.ssl.keyStorePassword"));
        if (StringUtil.isBlank(keyPwd)) {
            keyPwd = "changeit";
        }

        String storeType = getStoreTypeByFileName(_keyStoreFile);
        LOGGER.debug("Creating key store of type '" + storeType + "' from file '" + _keyStoreFile + "'");

        try {
            KeyManagerFactory keyMgrFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(storeType);

            try (FileInputStream fis = new FileInputStream(_keyStoreFile)) {
                keyStore.load(fis, keyStorePwd.toCharArray());
            }

            keyMgrFactory.init(keyStore, keyPwd.toCharArray());
            return keyMgrFactory.getKeyManagers();

        } catch (Exception _ex) {
            throw new IOException("Error while setting up keyStore", _ex);
        }
    }

    /**
     * Get the key/trust store 'type' by analyzing the filename extension.
     *
     * @param _file file
     * @return store type as string, defaults to JKS
     */
    public static String getStoreTypeByFileName(File _file) {

        String ext = SystemUtil.getFileExtension(_file.getName()).toLowerCase();
        switch (ext) {
            case STORETYPE_JKS:
            case STORETYPE_PKCS12:
            case STORETYPE_DER_ENCODED:
            case STORETYPE_JCEKS:
                return ext;
            default:
                return STORETYPE_JKS;
        }
    }
}
