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

import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import sun.security.util.DerValue;
import sun.security.util.HostnameChecker;
import sun.security.x509.X500Name;

import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static net.adoptopenjdk.icedteaweb.i18n.Translator.R;

public class HttpsCertVerifier implements CertVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(HttpsCertVerifier.class);

    private final X509Certificate[] chain;
    private final String hostName;
    private final boolean isTrusted;
    private final boolean hostMatched;
    private final ArrayList<String> details = new ArrayList<>();

    HttpsCertVerifier(final X509Certificate[] chain,
                      final boolean isTrusted, final boolean hostMatched,
                      final String hostName) {
        this.chain = chain;
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
    public CertPath getCertPath() { // Parameter ignored.

        final ArrayList<X509Certificate> list = new ArrayList<>();
        Collections.addAll(list, chain);

        final ArrayList<CertPath> certPaths = new ArrayList<>();

        try {
            certPaths.add(CertificateFactory.getInstance("X.509").generateCertPath(list));
        } catch (CertificateException ce) {
            LOG.error("Failed to create certificate for cert paths", ce);
            // carry on
        }

        return certPaths.get(0);
    }

    @Override
    public List<String> getDetails(final CertPath certPath) { // Parameter ignored.

        if (!getAlreadyTrustPublisher()) {
            addToDetails(R("SUntrustedCertificate"));
        }

        for (X509Certificate cert : chain) {
            long now = System.currentTimeMillis();
            long SIX_MONTHS = 180 * 24 * 60 * 60 * 1000L;
            long notAfter = cert.getNotAfter().getTime();
            if (notAfter < now) {
                addToDetails(R("SHasExpiredCert"));
            } else if (notAfter < now + SIX_MONTHS) {
                addToDetails(R("SHasExpiringCert"));
            }

            try {
                cert.checkValidity();
            } catch (CertificateNotYetValidException cnyve) {
                addToDetails(R("SNotYetValidCert"));
            } catch (CertificateExpiredException cee) {
                addToDetails(R("SHasExpiredCert"));
            }
        }

        if (!hostMatched) {
            addToDetails(R("SCNMisMatch", getNamesForCert(chain[0]), this.hostName));
        }

        return details;
    }

    private String getNamesForCert(final X509Certificate c) {

        final List<String> names = new ArrayList<>();

        // We use the specification from
        // http://java.sun.com/j2se/1.5.0/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()
        // to determine the type of address
        final int ALTNAME_DNS = 2;
        final int ALTNAME_IP = 7;

        try {
            final X500Name subjectName = HostnameChecker.getSubjectX500Name(c);
            if (subjectName != null) {
                final DerValue derValue = subjectName.findMostSpecificAttribute(X500Name.commonName_oid);
                if (derValue != null) {
                    names.add(derValue.getAsString());
                }
            }

            final Collection<List<?>> subjAltNames = c.getSubjectAlternativeNames();
            if (subjAltNames != null) {
                for (List<?> next : subjAltNames) {
                    final Integer type = (Integer) next.get(0);
                    if (type == ALTNAME_IP || type == ALTNAME_DNS) {
                        names.add((String) next.get(1));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception while handling certificate " + c, e);
        }

        return names.stream().filter(Objects::nonNull).collect(joining(", "));
    }

    private void addToDetails(final String detail) {
        if (!details.contains(detail)) {
            details.add(detail);
        }
    }

    @Override
    public Certificate getPublisher(final CertPath certPath) { // Parameter ignored.
        if (chain.length > 0) {
            return chain[0];
        }
        return null;
    }

    @Override
    public Certificate getRoot(final CertPath certPath) { // Parameter ignored.
        if (chain.length > 0) {
            return chain[chain.length - 1];
        }
        return null;
    }

    @Override
    public boolean getRootInCaCerts() {
        try {
            final List<KeyStore> caCertsKeyStores = KeyStores.getCAKeyStores();
            return CertificateUtils.inKeyStores((X509Certificate) getRoot(null), caCertsKeyStores);
        } catch (Exception e) {
            LOG.error("Exception while getting root in ca certs", e);
        }
        return false;
    }
}
