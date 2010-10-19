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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sun.security.util.HostnameChecker;
import sun.security.validator.ValidatorException;

import com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager;

/**
 * This class implements an X509 Trust Manager. The certificates it trusts are
 * "variable", in the sense that it can dynamically, and temporarily support
 * different certificates that are not in the keystore.
 */

public class VariableX509TrustManager extends X509ExtendedTrustManager {

    KeyStore userKeyStore = null;
    KeyStore caKeyStore = null;

    X509TrustManager userTrustManager = null;
    X509TrustManager caTrustManager = null;

    ArrayList<Certificate> temporarilyTrusted = new ArrayList<Certificate>();
    ArrayList<Certificate> temporarilyUntrusted = new ArrayList<Certificate>();

    static VariableX509TrustManager instance = null;

    /**
     * Constructor initializes the system, user and custom stores
     */
    public VariableX509TrustManager() {

        try {
            userKeyStore = SecurityUtil.getUserKeyStore();
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            tmFactory.init(userKeyStore);

            // tm factory initialized, now get the managers so we can assign the X509 one
            TrustManager[] trustManagers = tmFactory.getTrustManagers();

            for (int i=0; i < trustManagers.length; i++) {
                if (trustManagers[i] instanceof X509TrustManager) {
                    userTrustManager = (X509TrustManager) trustManagers[i];
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            caKeyStore = SecurityUtil.getCacertsKeyStore();
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            tmFactory.init(caKeyStore);

            // tm factory initialized, now get the managers so we can extract the X509 one
            TrustManager[] trustManagers = tmFactory.getTrustManagers();

            for (int i=0; i < trustManagers.length; i++) {
                if (trustManagers[i] instanceof X509TrustManager) {
                    caTrustManager = (X509TrustManager) trustManagers[i];
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Check if client is trusted (no support for custom here, only system/user)
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType,
                                   String hostName, String algorithm)
            throws CertificateException {
        // First try catrustmanager, then try usertrustmanager
        try {
            caTrustManager.checkClientTrusted(chain, authType);
        } catch (Exception caex) {
            try {
                userTrustManager.checkClientTrusted(chain, authType);
            } catch (Exception userex) {
                // Do nothing here. This trust manager is intended to be used
                // only in the plugin instance vm, which does not act as a
                // server
            }
        }
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
        // First try catrustmanager, then try usertrustmanager, and finally, check temp trusted certs
        try {
            caTrustManager.checkServerTrusted(chain, authType);
        } catch (ValidatorException caex) {
            try {
                userTrustManager.checkServerTrusted(chain, authType);
            } catch (ValidatorException uex) {
                if (!temporarilyTrusted.contains(chain[0]))
                    throw (CertificateException) uex;
            }
        }
    }

    /**
     * Return if the user explicitly trusted this i.e. in userTrustManager or temporarilyTrusted
     */
    private boolean isExplicitlyTrusted(X509Certificate[] chain, String authType) {
        boolean explicitlyTrusted = false;

        try {
            userTrustManager.checkServerTrusted(chain, authType);
            explicitlyTrusted = true;
        } catch (ValidatorException uex) {
            if (temporarilyTrusted.contains(chain[0]))
                explicitlyTrusted = true;
        } catch (CertificateException ce) {
            // do nothing, this means that the cert is not explicitly trusted
        }

        return explicitlyTrusted;

    }

    public X509Certificate[] getAcceptedIssuers() {
        // delegate to default
        return caTrustManager.getAcceptedIssuers();
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
        return SecurityWarningDialog.showCertWarningDialog(
                        SecurityWarningDialog.AccessType.UNVERIFIED, null,
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
