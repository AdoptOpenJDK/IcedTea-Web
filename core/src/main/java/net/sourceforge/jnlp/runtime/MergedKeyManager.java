package net.sourceforge.jnlp.runtime;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.os.OsUtil;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.sourceforge.jnlp.security.SecurityUtil;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.sourceforge.jnlp.security.KeyStores.Level.SYSTEM;
import static net.sourceforge.jnlp.security.KeyStores.Level.USER;
import static net.sourceforge.jnlp.security.KeyStores.Type.CLIENT_CERTS;
import static net.sourceforge.jnlp.security.KeyStores.getKeyStore;

public class MergedKeyManager extends X509ExtendedKeyManager {
    private static final Logger LOG = LoggerFactory.getLogger(MergedKeyManager.class);
    private static final Map<String, String> hostToClientAliasCache = new ConcurrentHashMap<>();
    private final X509KeyManager browserKeyManager;
    private final X509KeyManager userKeyManager;
    private final X509KeyManager systemKeyManager;
    private static final String USER_SUFFIX = " (from user keystore)";
    private static final String SYSTEM_SUFFIX = " (from system keystore)";
    private static final String BROWSER_SUFFIX = " (from browser keystore)";
    private static boolean userCancelled = false;

    MergedKeyManager() {
        userKeyManager = getKeyManager(getKeyStore(USER, CLIENT_CERTS).getKs());
        systemKeyManager = getKeyManager(getKeyStore(SYSTEM, CLIENT_CERTS).getKs());
        browserKeyManager = getX509KeyManager();
    }

    private X509KeyManager getX509KeyManager() {
        if (OsUtil.isWindows()) {
            try {
                final KeyStore ks = KeyStore.getInstance("Windows-MY");
                ks.load(null, null);
                return getKeyManager(ks);
            } catch (Exception e) {
                LOG.error("Unable to get browser keystore information", e);
            }
        }
        return null;
    }

    private X509KeyManager getKeyManager(KeyStore ks) {
        try {
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            SecurityUtil.initKeyManagerFactory(kmf, ks);
            final KeyManager[] keyManagers = kmf.getKeyManagers();
            if (keyManagers != null) {
                for (KeyManager keyManager : keyManagers) {
                    if (keyManager instanceof X509KeyManager) {
                        return (X509KeyManager) keyManager;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to get KeyStore " + ks, e);
        }
        return null;
    }

    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
        if (userCancelled) {
            LOG.warn("Client certificate selection previously cancelled by user, returning null alias");
            return null;
        }

        final String host = getHostFromSocket(socket);
        LOG.info("Retrieved host from socket : " + host);
        if (host != null) {
            final String alias = hostToClientAliasCache.get(host.toLowerCase());
            LOG.info("Found " + alias + " alias in cache for " + host.toLowerCase() + " and keyTypes " + Arrays.toString(keyTypes));
            if (alias != null) {
                return alias;
            }
        }

        final Map<String, X509Certificate> validClientAliases = new LinkedHashMap<>();
        final Map<String, X509Certificate> expiredClientAliases = new LinkedHashMap<>();
        final Map<String, X509Certificate> otherAliases = new LinkedHashMap<>();
        for (final String keyType : keyTypes) {
            final String[] aliasesWithSuffix = getClientAliases(keyType, issuers);
            //if (aliasesWithSuffix != null)
            for (final String aliasWithSuffix : aliasesWithSuffix) {
                X509Certificate[] certs = getCertificateChain(removeAliasSuffix(aliasWithSuffix));
                if (certs == null || certs.length == 0) {
                    continue;
                }
                final X509Certificate cert = certs[0];
                try {
                    final List<String> usage = cert.getExtendedKeyUsage();
                    // Extensions : 1.3.6.1.5.5.7.3.2=ClientCert 2.5.29.37.0=ANY
                    if (usage != null && (usage.contains("1.3.6.1.5.5.7.3.2") || usage.contains("2.5.29.37.0"))) {
                        try {
                            cert.checkValidity();
                            LOG.info("Found valid client alias with clientCert or ANY extension: " + aliasWithSuffix);
                            validClientAliases.put(aliasWithSuffix, cert);
                        } catch (CertificateException e) {
                            LOG.warn("Found expired client alias with clientCert or ANY extension: " + aliasWithSuffix);
                            expiredClientAliases.put(aliasWithSuffix, cert);
                        }
                    } else {
                        LOG.warn("Found non-client alias: " + aliasWithSuffix);
                        otherAliases.put(aliasWithSuffix, cert);
                    }
                } catch (CertificateParsingException e) {
                    LOG.warn("Exception while getting ExtendedKeyUsage for alias " + aliasWithSuffix);
                    otherAliases.put(aliasWithSuffix, cert);
                }
            }
        }

        final String alias;
        if (!validClientAliases.isEmpty()) {
            alias = getPreferredAlias(validClientAliases, "valid client");
        } else {
            expiredClientAliases.putAll(otherAliases);
            if (!expiredClientAliases.isEmpty()) {
                alias = getPreferredAlias(expiredClientAliases, "remaining");
            } else {
                LOG.warn("Could not find any client alias for keyTypes " + Arrays.toString(keyTypes));
                alias = null;
            }
        }

        if (socket instanceof SSLSocket && host != null && alias != null) {
            LOG.info("Added " + alias + " alias in cache for " + host.toLowerCase());
            hostToClientAliasCache.put(host.toLowerCase(), alias);
        }
        return alias;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String getHostFromSocket(Socket socket) {
        try {
            final Class c = Class.forName("sun.security.ssl.SSLSocketImpl");
            if (c.isInstance(socket)) {
                final Object o = c.cast(socket);
                Method m;
                try {
                    m = c.getDeclaredMethod("getHost");
                    m.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    m = c.getDeclaredMethod("getPeerHost");
                }
                return (String) m.invoke(o);
            }
        } catch (Exception e) {
            LOG.warn("Cannot get remote host from Socket", e);
        }
        return null;
    }

    private String getPreferredAlias(Map<String, X509Certificate> aliasesMap, String aliasType) {
        final String alias;
        if (aliasesMap.size() > 1) {
            if (JNLPRuntime.isHeadless()) {
                alias = aliasesMap.keySet().iterator().next();
                LOG.info("Returning the first " + aliasType + " alias in headless mode : " + alias);
            } else {
                final DialogResult res = SecurityDialogs.showClientCertSelectionPrompt(aliasesMap);
                if (res == null) {
                    alias = null;
                    userCancelled = true;
                    LOG.warn("Client certificate selection cancelled by user");
                } else {
                    alias = aliasesMap.keySet().toArray(new String[0])[res.getButtonIndex()];
                    LOG.info("Returning the selected " + aliasType + " alias : " + alias);
                }
            }
        } else if (aliasesMap.size() == 1) {
            alias = aliasesMap.keySet().iterator().next();
            LOG.info("Returning the only " + aliasType + " alias : " + alias);
        } else {
            alias = null;
        }
        return removeAliasSuffix(alias);
    }

    private String removeAliasSuffix(String aliasWithSuffix) {
        if (aliasWithSuffix == null) {
            return null;
        }
        if (aliasWithSuffix.endsWith(USER_SUFFIX)) {
            return aliasWithSuffix.substring(0, aliasWithSuffix.length() - USER_SUFFIX.length());
        }
        if (aliasWithSuffix.endsWith(SYSTEM_SUFFIX)) {
            return aliasWithSuffix.substring(0, aliasWithSuffix.length() - SYSTEM_SUFFIX.length());
        }
        if (aliasWithSuffix.endsWith(BROWSER_SUFFIX)) {
            return aliasWithSuffix.substring(0, aliasWithSuffix.length() - BROWSER_SUFFIX.length());
        }
        return aliasWithSuffix;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        String alias = null;
        if (userKeyManager != null) {
            alias = userKeyManager.chooseServerAlias(keyType, issuers, socket);
        }
        if (alias == null && systemKeyManager != null) {
            alias = systemKeyManager.chooseServerAlias(keyType, issuers, socket);
        }
        if (alias == null && browserKeyManager != null) {
            alias = browserKeyManager.chooseServerAlias(keyType, issuers, socket);
        }
        return alias;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] x509certificates = null;
        if (userKeyManager != null) {
            x509certificates = userKeyManager.getCertificateChain(alias);
        }
        if (x509certificates == null && systemKeyManager != null) {
            x509certificates = systemKeyManager.getCertificateChain(alias);
        }
        if (x509certificates == null && browserKeyManager != null) {
            x509certificates = browserKeyManager.getCertificateChain(alias);
        }
        return x509certificates;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        final List<String> aliases = new ArrayList<>();
        if (userKeyManager != null) {
            aliases.addAll(appendSuffixToAll(userKeyManager.getClientAliases(keyType, issuers), USER_SUFFIX));
        }
        if (systemKeyManager != null) {
            aliases.addAll(appendSuffixToAll(systemKeyManager.getClientAliases(keyType, issuers), SYSTEM_SUFFIX));
        }
        if (browserKeyManager != null) {
            aliases.addAll(appendSuffixToAll(browserKeyManager.getClientAliases(keyType, issuers), BROWSER_SUFFIX));
        }
        return aliases.toArray(new String[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey privateKey = null;
        if (userKeyManager != null) {
            privateKey = userKeyManager.getPrivateKey(alias);
        }
        if (privateKey == null && systemKeyManager != null) {
            privateKey = systemKeyManager.getPrivateKey(alias);
        }
        if (privateKey == null && browserKeyManager != null) {
            privateKey = browserKeyManager.getPrivateKey(alias);
        }
        return privateKey;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        final List<String> aliases = new ArrayList<>();
        if (userKeyManager != null) {
            aliases.addAll(appendSuffixToAll(userKeyManager.getServerAliases(keyType, issuers), null));
        }
        if (systemKeyManager != null) {
            aliases.addAll(appendSuffixToAll(systemKeyManager.getServerAliases(keyType, issuers), null));
        }
        if (browserKeyManager != null) {
            aliases.addAll(appendSuffixToAll(browserKeyManager.getServerAliases(keyType, issuers), null));
        }
        return aliases.toArray(new String[0]);
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return chooseClientAlias(keyType, issuers, null);
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return chooseServerAlias(keyType, issuers, null);
    }

    private List<String> appendSuffixToAll(String[] array, String suffix) {
        if (array == null) {
            return emptyList();
        }
        if (suffix == null) {
            return asList(array);
        }
        return Stream.of(array)
                .map(s -> s + suffix)
                .collect(toList());
    }
}
