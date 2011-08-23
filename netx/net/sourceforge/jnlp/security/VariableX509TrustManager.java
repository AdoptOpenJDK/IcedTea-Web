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

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sun.security.util.HostnameChecker;
import sun.security.validator.ValidatorException;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;

/**
 * This class implements an X509 Trust Manager. The certificates it trusts are
 * "variable", in the sense that it can dynamically, and temporarily support
 * different certificates that are not in the keystore.
 */

final public class VariableX509TrustManager extends X509ExtendedTrustManager {

    /** TrustManagers containing trusted CAs */
    private X509TrustManager[] caTrustManagers = null;

    /** TrustManagers containing trusted certificates */
    private X509TrustManager[] certTrustManagers = null;

    /** TrustManagers containing trusted client certificates */
    private X509TrustManager[] clientTrustManagers = null;

    private ArrayList<Certificate> temporarilyTrusted = new ArrayList<Certificate>();
    private ArrayList<Certificate> temporarilyUntrusted = new ArrayList<Certificate>();

    private static VariableX509TrustManager instance = null;

    /**
     * Constructor initializes the system, user and custom stores
     */
    public VariableX509TrustManager() {

        /*
         * Load TrustManagers for trusted certificates
         */
        try {
            /** KeyStores containing trusted certificates */
            KeyStore[] trustedCertKeyStores = KeyStores.getCertKeyStores();
            certTrustManagers = new X509TrustManager[trustedCertKeyStores.length];

            for (int j = 0; j < trustedCertKeyStores.length; j++) {
                TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
                tmFactory.init(trustedCertKeyStores[j]);

                // tm factory initialized, now get the managers so we can assign the X509 one
                TrustManager[] trustManagers = tmFactory.getTrustManagers();

                for (int i = 0; i < trustManagers.length; i++) {
                    if (trustManagers[i] instanceof X509TrustManager) {
                        certTrustManagers[j] = (X509TrustManager) trustManagers[i];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Load TrustManagers for trusted CAs
         */
        try {
            /** KeyStores containing trusted CAs */
            KeyStore[] trustedCAKeyStores = KeyStores.getCAKeyStores();
            caTrustManagers = new X509TrustManager[trustedCAKeyStores.length];

            for (int j = 0; j < caTrustManagers.length; j++) {
                TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
                tmFactory.init(trustedCAKeyStores[j]);

                // tm factory initialized, now get the managers so we can extract the X509 one
                TrustManager[] trustManagers = tmFactory.getTrustManagers();

                for (int i = 0; i < trustManagers.length; i++) {
                    if (trustManagers[i] instanceof X509TrustManager) {
                        caTrustManagers[j] = (X509TrustManager) trustManagers[i];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Load TrustManagers for trusted clients certificates
         */
        try {
            KeyStore[] clientKeyStores = KeyStores.getClientKeyStores();
            clientTrustManagers = new X509TrustManager[clientKeyStores.length];

            for (int j = 0; j < clientTrustManagers.length; j++) {
                TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
                tmFactory.init(clientKeyStores[j]);

                // tm factory initialized, now get the managers so we can extract the X509 one
                TrustManager[] trustManagers = tmFactory.getTrustManagers();

                for (int i = 0; i < trustManagers.length; i++) {
                    if (trustManagers[i] instanceof X509TrustManager) {
                        clientTrustManagers[j] = (X509TrustManager) trustManagers[i];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if client is trusted (no support for custom here, only system/user)
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType,
                                   String hostName, String algorithm)
            throws CertificateException {

        boolean trusted = false;
        ValidatorException savedException = null;
        for (int i = 0; i < clientTrustManagers.length; i++) {
            try {
                clientTrustManagers[i].checkClientTrusted(chain, authType);
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

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        checkClientTrusted(chain, authType, null, null);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType,
                                   String hostName, String algorithm)
            throws CertificateException {
        checkServerTrusted(chain, authType, hostName, false);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        checkServerTrusted(chain, authType, null, null);
    }

    /**
     * Check if the server is trusted
     *
     * @param chain The cert chain
     * @param authType The auth type algorithm
     * @param checkOnly Whether to "check only" i.e. no user prompt, or to prompt for permission
     */
    public synchronized void checkServerTrusted(X509Certificate[] chain,
                             String authType, String hostName,
                             boolean checkOnly) throws CertificateException {
        CertificateException ce = null;
        boolean trusted = true;
        boolean CNMatched = true;

        try {
            checkAllManagers(chain, authType);
        } catch (CertificateException e) {
            trusted = false;
            ce = e;
        }

        // If the certificate is not explicitly trusted, we
        // need to prompt the user
        if (!isExplicitlyTrusted(chain, authType)) {

            if (hostName == null) {
                CNMatched = false;
            } else {
                try {
                    HostnameChecker checker = HostnameChecker
                            .getInstance(HostnameChecker.TYPE_TLS);

                    checker.match(hostName, chain[0]); // only need to match @ 0 for
                                                       // CN

                } catch (CertificateException e) {
                    CNMatched = false;
                    ce = e;
                }
            }
        }

        if (!trusted || !CNMatched) {
            if (checkOnly) {
                throw ce;
            } else {
                if (!isTemporarilyUntrusted(chain[0])) {
                    boolean b = askUser(chain, authType, trusted, CNMatched, hostName);

                    if (b) {
                        temporarilyTrust(chain[0]);
                    } else {
                        temporarilyUntrust(chain[0]);
                    }
                }

                checkAllManagers(chain, authType);
            }
        }
    }

    /**
     * Check system, user and custom trust manager
     */
    private void checkAllManagers(X509Certificate[] chain, String authType) throws CertificateException {
        // first try CA TrustManagers
        boolean trusted = false;
        ValidatorException savedException = null;
        for (int i = 0; i < caTrustManagers.length; i++) {
            try {
                caTrustManagers[i].checkServerTrusted(chain, authType);
                trusted = true;
                break;
            } catch (ValidatorException caex) {
                savedException = caex;
            }
        }
        if (trusted) {
            return;
        }

        // then try certificate TrustManagers
        for (int i = 0; i < certTrustManagers.length; i++) {
            try {
                certTrustManagers[i].checkServerTrusted(chain, authType);
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
                // System.out.println("IMPOSSIBLE!");
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

        for (int i = 0; i < certTrustManagers.length; i++) {
            try {
                certTrustManagers[i].checkServerTrusted(chain, authType);
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

    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> issuers = new ArrayList<X509Certificate>();

        for (int i = 0; i < caTrustManagers.length; i++) {
            issuers.addAll(Arrays.asList(caTrustManagers[i].getAcceptedIssuers()));
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
        if (temporarilyUntrusted.contains(c)) {
            return true;
        }
        return false;
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
     * @param chain The certificate chain
     * @param authType The authentication algorithm
     * @return user's response
     */
    private boolean askUser(X509Certificate[] chain, String authType,
                            boolean isTrusted, boolean hostMatched,
                            String hostName) {
        return SecurityDialogs.showCertWarningDialog(
                        AccessType.UNVERIFIED, null,
                        new HttpsCertVerifier(this, chain, authType,
                                              isTrusted, hostMatched,
                                              hostName));
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
