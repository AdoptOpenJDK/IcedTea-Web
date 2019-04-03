/* HttpsCertVerifier.java
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

import static net.sourceforge.jnlp.runtime.Translator.R;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sourceforge.jnlp.util.logging.OutputController;

import sun.security.util.DerValue;
import sun.security.util.HostnameChecker;
import sun.security.x509.X500Name;

public class HttpsCertVerifier implements CertVerifier {

    private X509Certificate[] chain;
    private String authType;
    private String hostName;
    private boolean isTrusted;
    private boolean hostMatched;
    private ArrayList<String> details = new ArrayList<String>();

    public HttpsCertVerifier(X509Certificate[] chain, String authType,
                             boolean isTrusted, boolean hostMatched,
                             String hostName) {
        this.chain = chain;
        this.authType = authType;
        this.hostName = hostName;
        this.isTrusted = isTrusted;
        this.hostMatched = hostMatched;
    }

    @Override
    public boolean getAlreadyTrustPublisher() {
        return isTrusted;
    }


    /* XXX: Most of these methods have a CertPath param that should be passed
     * from the UI dialogs. However, this is not implemented yet so most of
     * the params are ignored.
     */

    @Override
    public CertPath getCertPath(CertPath certPath) { // Parameter ignored.

        ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        for (int i = 0; i < chain.length; i++)
            list.add(chain[i]);

        ArrayList<CertPath> certPaths = new ArrayList<CertPath>();

        try {
            certPaths.add(CertificateFactory.getInstance("X.509").generateCertPath(list));
        } catch (CertificateException ce) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ce);

            // carry on
        }

        return certPaths.get(0);
    }

    @Override
    public List<String> getDetails(CertPath certPath) { // Parameter ignored.

        boolean hasExpiredCert = false;
        boolean hasExpiringCert = false;
        boolean notYetValidCert = false;
        boolean isUntrusted = false;
        boolean CNMisMatch = !hostMatched;

        if (!getAlreadyTrustPublisher())
            isUntrusted = true;

        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];

            long now = System.currentTimeMillis();
            long SIX_MONTHS = 180 * 24 * 60 * 60 * 1000L;
            long notAfter = cert.getNotAfter().getTime();
            if (notAfter < now) {
                hasExpiredCert = true;
            } else if (notAfter < now + SIX_MONTHS) {
                hasExpiringCert = true;
            }

            try {
                cert.checkValidity();
            } catch (CertificateNotYetValidException cnyve) {
                notYetValidCert = true;
            } catch (CertificateExpiredException cee) {
                hasExpiredCert = true;
            }
        }

        String altNames = getNamesForCert(chain[0]);

        if (isUntrusted || hasExpiredCert || hasExpiringCert || notYetValidCert || CNMisMatch) {
            if (isUntrusted)
                addToDetails(R("SUntrustedCertificate"));
            if (hasExpiredCert)
                addToDetails(R("SHasExpiredCert"));
            if (hasExpiringCert)
                addToDetails(R("SHasExpiringCert"));
            if (notYetValidCert)
                addToDetails(R("SNotYetValidCert"));
            if (CNMisMatch)
                addToDetails(R("SCNMisMatch", altNames, this.hostName));
        }

        return details;
    }

    private String getNamesForCert(X509Certificate c) {

        String names = "";

        // We use the specification from
        // http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()
        // to determine the type of address
        int ALTNAME_DNS = 2;
        int ALTNAME_IP = 7;

        try {
            Collection<List<?>> subjAltNames = c.getSubjectAlternativeNames();
            X500Name subjectName = HostnameChecker.getSubjectX500Name(c);
            DerValue derValue = subjectName.findMostSpecificAttribute
                                                        (X500Name.commonName_oid);
            names += derValue.getAsString();

            if (subjAltNames != null) {
                for (List<?> next : subjAltNames) {
                    if (((Integer) next.get(0)).intValue() == ALTNAME_IP ||
                            ((Integer) next.get(0)).intValue() == ALTNAME_DNS) {
                        names += ", " + (String) next.get(1);
                    }
                }
            }

            if (subjAltNames != null)
                names = names.substring(2); // remove proceeding ", "

        } catch (CertificateParsingException cpe) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, cpe);
        } catch (IOException ioe) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, ioe);
        }

        return names;
    }

    private void addToDetails(String detail) {
        if (!details.contains(detail))
            details.add(detail);
    }

    @Override
    public Certificate getPublisher(CertPath certPath) { // Paramater ignored.
        if (chain.length > 0)
            return (Certificate) chain[0];
        return null;
    }

    @Override
    public Certificate getRoot(CertPath certPath) { // Parameter ignored.
        if (chain.length > 0)
            return (Certificate) chain[chain.length - 1];
        return null;
    }

    public boolean getRootInCacerts() {
        try {
            KeyStore[] caCertsKeyStores = KeyStores.getCAKeyStores();
            return CertificateUtils.inKeyStores((X509Certificate) getRoot(null), caCertsKeyStores);
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean hasSigningIssues(CertPath certPath) {
        return false;
    }
}
