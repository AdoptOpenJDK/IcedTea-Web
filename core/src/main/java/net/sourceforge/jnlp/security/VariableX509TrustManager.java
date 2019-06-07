/* VariableX509TrustManager.java
   Copyright (C) 2009 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp.security;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogs;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import sun.security.util.HostnameChecker;
import sun.security.validator.ValidatorException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class implements an X509 Trust Manager. The certificates it trusts are
 * "variable", in the sense that it can dynamically, and temporarily support
 * different certificates that are not in the keystore.
 */

final public class VariableX509TrustManager {

    private final static Logger LOG = LoggerFactory.getLogger(VariableX509TrustManager.class);

    public final static String SUN_X_509 = "SunX509";

    public final static String SUN_JSSE = "SunJSSE";


    private static VariableX509TrustManager instance = null;


    private final List<Certificate> temporarilyTrusted = new ArrayList<>();

    private final List<Certificate> temporarilyUntrusted = new ArrayList<>();

    private final List<X509TrustManager> certTrustManagers = new ArrayList<>();

    private final List<X509TrustManager> clientTrustManagers = new ArrayList<>();


    private final List<X509TrustManager> caTrustManagers = new ArrayList<>();

    public static void main(String[] args) {
        new VariableX509TrustManager();
    }

    /**
     * Constructor initializes the system, user and custom stores
     */
    public VariableX509TrustManager() {
        try {
            loadManagers(Arrays.asList(KeyStores.getCertKeyStores()), certTrustManagers);
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }

        try {
            loadManagers(Arrays.asList(KeyStores.getCAKeyStores()), caTrustManagers);
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }

        try {
            loadManagers(Arrays.asList(KeyStores.getClientKeyStores()), clientTrustManagers);
        } catch (Exception e) {
            LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
        }
    }

    private void loadManagers(final List<KeyStore> keyStores, final List<X509TrustManager> managers) {
        Objects.requireNonNull(keyStores);
        Objects.requireNonNull(managers);

        keyStores.stream()
                .map(keyStore -> getTrustManagerFactory(keyStore))
                .flatMap(trustManagerFactory -> Arrays.asList(trustManagerFactory.getTrustManagers()).stream())
                .filter(trustManager -> trustManager instanceof X509TrustManager)
                .forEach(trustManager -> managers.add((X509TrustManager) trustManager));
    }

    private TrustManagerFactory getTrustManagerFactory(final KeyStore keyStore) {
        try {
            final TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(SUN_X_509, SUN_JSSE);
            tmFactory.init(keyStore);
            return tmFactory;
        } catch (final Exception e) {
            throw new RuntimeException("Error while creating TrustManagerFactory");
        }
    }

    /**
     * Check if client is trusted (no support for custom here, only system/user)
     *
     * @param chain    certificate chain
     * @param authType type of authentication
     * @param hostName hostname
     * @throws java.security.cert.CertificateException if certificate is wrong
     */
    public void checkTrustClient(final X509Certificate[] chain, final String authType,
                                 final String hostName)
            throws CertificateException {

        boolean trusted = false;
        ValidatorException savedException = null;
        for (X509TrustManager clientTrustManager : clientTrustManagers) {
            try {
                clientTrustManager.checkClientTrusted(chain, authType);
                trusted = true;
                break;
            } catch (ValidatorException caex) {
                savedException = caex;
            }
        }
        if (trusted) {
            return;
        }

        throw savedException;
    }

    /**
     * Check if the server is trusted.
     * <p>
     * First, existing stores are checked to see if the certificate is trusted.
     * Next, if the certificate is not explicitly trusted by the user, a host
     * name check is performed. The user is them prompted as needed.
     *
     * @param chain    The cert chain
     * @param authType The auth type algorithm
     * @param hostName The expected hostName that the server should have
     * @param socket   The SSLSocket in use (may be null)
     * @param engine   The SSLEngine in use (may be null)
     * @throws java.security.cert.CertificateException if certificate is wrong
     */
    public synchronized void checkTrustServer(X509Certificate[] chain,
                                              String authType, String hostName,
                                              SSLSocket socket, SSLEngine engine) throws CertificateException {
        CertificateException ce = null;
        boolean trusted = true;
        boolean CNMatched = false;

        // Check trust stores
        try {
            checkAllManagers(chain, authType, socket, engine);
        } catch (CertificateException e) {
            trusted = false;
            ce = e;
        }

        // If the certificate is not explicitly trusted, we
        // check host match
        if (!isExplicitlyTrusted(chain, authType)) {
            if (hostName != null) {
                try {
                    HostnameChecker checker = HostnameChecker
                            .getInstance(HostnameChecker.TYPE_TLS);

                    checker.match(hostName, chain[0]); // only need to match @ 0 for CN

                    CNMatched = true;
                } catch (CertificateException e) {
                    ce = e;
                }
            }
        } else {
            // If it is explicitly trusted, just return right away.
            return;
        }

        // If it is (not explicitly trusted) AND
        // ((it is not in store) OR (there is a host mismatch))
        if (!trusted || !CNMatched) {
            if (!isTemporarilyUntrusted(chain[0])) {
                boolean b = askUser(chain, authType, trusted, CNMatched, hostName);

                if (b) {
                    temporarilyTrust(chain[0]);
                    return;
                } else {
                    temporarilyUntrust(chain[0]);
                }
            }

            throw ce;
        }
    }

    /**
     * Check system, user and custom trust manager.
     * <p>
     * This method is intended to work with both, JRE6 and JRE7. If socket
     * and engine are null, it assumes that the call is for JRE6 (i.e. not
     * javax.net.ssl.X509ExtendedTrustManager which is Java 7 specific). If
     * either of those are not null, it will assume that the caTrustManagers
     * are javax.net.ssl.X509ExtendedTrustManager instances and will
     * invoke their check methods.
     *
     * @param chain    The certificate chain
     * @param authType The authentication type
     * @param socket   the SSLSocket being used for the connection
     * @param engine   the SSLEngine being used for the connection
     */
    private void checkAllManagers(X509Certificate[] chain, String authType, Socket socket, SSLEngine engine) throws CertificateException {

        // first try CA TrustManagers
        boolean trusted = false;
        ValidatorException savedException = null;
        for (X509TrustManager caTrustManager : caTrustManagers) {
            try {
                if (socket == null && engine == null) {
                    caTrustManager.checkServerTrusted(chain, authType);
                } else {
                    try {
                        Class<?> x509ETMClass = Class.forName("javax.net.ssl.X509ExtendedTrustManager");
                        if (engine == null) {
                            Method mcheckServerTrusted = x509ETMClass.getDeclaredMethod("checkServerTrusted", X509Certificate[].class, String.class, Socket.class);
                            mcheckServerTrusted.invoke(caTrustManager, chain, authType, socket);
                        } else {
                            Method mcheckServerTrusted = x509ETMClass.getDeclaredMethod("checkServerTrusted", X509Certificate[].class, String.class, SSLEngine.class);
                            mcheckServerTrusted.invoke(caTrustManager, chain, authType, engine);
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException nsme) {
                        throw new ValidatorException(nsme.getMessage());
                    }
                }
                trusted = true;
                break;
            } catch (ValidatorException caex) {
                savedException = caex;
            }
        }

        if (trusted) {
            return;
        }

        for (X509TrustManager certTrustManager : certTrustManagers) {
            try {
                certTrustManager.checkServerTrusted(chain, authType);
                trusted = true;
                break;
            } catch (ValidatorException caex) {
                savedException = caex;
            }
        }
        if (trusted) {
            return;
        }

        // finally check temp trusted certs
        if (!temporarilyTrusted.contains(chain[0])) {
            if (savedException == null) {
                // OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "IMPOSSIBLE!");
                throw new ValidatorException(ValidatorException.T_SIGNATURE_ERROR, chain[0]);
            }
            throw savedException;
        }

    }

    /**
     * Return if the user explicitly trusted this i.e. in userTrustManager or temporarilyTrusted
     */
    private boolean isExplicitlyTrusted(X509Certificate[] chain, String authType) {
        boolean explicitlyTrusted = false;

        for (X509TrustManager certTrustManager : certTrustManagers) {
            try {
                certTrustManager.checkServerTrusted(chain, authType);
                explicitlyTrusted = true;
                break;
            } catch (ValidatorException uex) {
                if (temporarilyTrusted.contains(chain[0])) {
                    explicitlyTrusted = true;
                    break;
                }
            } catch (CertificateException ce) {
                // not explicitly trusted
            }
        }

        return explicitlyTrusted;
    }

    protected X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> issuers = new ArrayList<>();

        for (X509TrustManager caTrustManager : caTrustManagers) {
            issuers.addAll(Arrays.asList(caTrustManager.getAcceptedIssuers()));
        }

        return issuers.toArray(new X509Certificate[issuers.size()]);
    }

    /**
     * Temporarily untrust the given cert - do not ask the user to trust this
     * certificate again
     *
     * @param c The certificate to trust
     */
    private void temporarilyUntrust(Certificate c) {
        temporarilyUntrusted.add(c);
    }

    /**
     * Was this certificate explicitly untrusted by user?
     *
     * @param c the certificate
     * @return true if the user was presented with this certificate and chose
     * not to trust it
     */
    private boolean isTemporarilyUntrusted(Certificate c) {
        return temporarilyUntrusted.contains(c);
    }

    /**
     * Temporarily trust the given cert (runtime)
     *
     * @param c The certificate to trust
     */
    private void temporarilyTrust(Certificate c) {
        temporarilyTrusted.add(c);
    }

    /**
     * Ask user if the certificate should be trusted
     *
     * @param chain    The certificate chain
     * @param authType The authentication algorithm
     * @return user's response
     */
    private boolean askUser(final X509Certificate[] chain, final String authType,
                            final boolean isTrusted, final boolean hostMatched,
                            final String hostName) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                YesNoSandbox r = SecurityDialogs.showCertWarningDialog(
                        AccessType.UNVERIFIED, null,
                        new HttpsCertVerifier(chain, authType,
                                isTrusted, hostMatched,
                                hostName), null);
                if (r == null) {
                    return false;
                }
                return r.compareValue(Primitive.YES);
            }
        });
    }

    /**
     * Return an instance of this singleton
     *
     * @return The instance
     */
    public static VariableX509TrustManager getInstance() {
        if (instance == null)
            instance = new VariableX509TrustManager();

        return instance;
    }
}
