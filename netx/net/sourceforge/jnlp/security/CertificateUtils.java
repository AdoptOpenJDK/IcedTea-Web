/* CertificateUtils.java
   Copyright (C) 2010 Red Hat, Inc.

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Random;

import net.sourceforge.jnlp.runtime.Translator;
import net.sourceforge.jnlp.util.logging.OutputController;
import net.sourceforge.jnlp.util.replacements.BASE64Encoder;
import sun.security.provider.X509Factory;

/**
 * Common utilities to manipulate certificates. Provides methods to add
 * Certificates to a KeyStores, check if certificates already exist in a
 * KeyStore and printing certificates.
 */
public class CertificateUtils {

    /**
     * Adds the X509Certficate in the file to the KeyStore. Note that it does
     * not update the copy of the KeyStore on disk.
     */
    public static final void addToKeyStore(File file, KeyStore ks) throws CertificateException,
            IOException, KeyStoreException {

        OutputController.getLogger().log("Importing certificate from " + file + " into " + ks);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate cert = null;

        try {
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (ClassCastException cce) {
            throw new CertificateException("Input file is not an X509 Certificate", cce);
        }

        addToKeyStore(cert, ks);
    }

    /**
     * Adds an X509Certificate to the KeyStore. Note that it does not update the
     * copy of the KeyStore on disk.
     */
    public static final void addToKeyStore(X509Certificate cert, KeyStore ks)
            throws KeyStoreException {

        OutputController.getLogger().log("Importing " + cert.getSubjectX500Principal().getName());

        String alias = null;

        // does this certificate already exist?
        alias = ks.getCertificateAlias(cert);
        if (alias != null) {
            return;
        }

        // create a unique alias for this new certificate
        Random random = new Random();
        do {
            alias = new BigInteger(20, random).toString();
        } while (ks.getCertificate(alias) != null);

        ks.setCertificateEntry(alias, cert);
    }

    public static void addPKCS12ToKeyStore(File file, KeyStore ks, char[] password)
            throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(bis, password);

        Enumeration<String> aliasList = keyStore.aliases();

        while (aliasList.hasMoreElements()) {
            String alias = aliasList.nextElement();
            Certificate[] certChain = keyStore.getCertificateChain(alias);
            Key key = keyStore.getKey(alias, password);
            addPKCS12ToKeyStore(certChain, key, ks);
        }
    }

    public static void addPKCS12ToKeyStore(Certificate[] certChain, Key key, KeyStore ks)
            throws KeyStoreException {
        String alias = null;

        // does this certificate already exist?
        alias = ks.getCertificateAlias(certChain[0]);
        if (alias != null) {
            return;
        }

        // create a unique alias for this new certificate
        Random random = new Random();
        do {
            alias = new BigInteger(20, random).toString();
        } while (ks.getCertificate(alias) != null);

        ks.setKeyEntry(alias, key, KeyStores.getPassword(), certChain);
    }

    /**
     * Checks whether an X509Certificate is already in one of the keystores
     * @param c the certificate
     * @param keyStores the KeyStores to check in
     * @return true if the certificate is present in one of the keystores, false otherwise
     */
    public static final boolean inKeyStores(X509Certificate c, KeyStore[] keyStores) {
        for (int i = 0; i < keyStores.length; i++) {
            try {
                // Check against all certs
                Enumeration<String> aliases = keyStores[i].aliases();
                while (aliases.hasMoreElements()) {

                    // Verify against this entry
                    String alias = aliases.nextElement();

                    if (c.equals(keyStores[i].getCertificate(alias))) {
                    OutputController.getLogger().log(Translator.R("LCertFoundIn", c.getSubjectX500Principal().getName(), KeyStores.getPathToKeystore(keyStores[i].hashCode())));
                        return true;
                    } // else continue
                }

            } catch (KeyStoreException e) {
                OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
                // continue
            }
        }
        return false;
    }

    /**
     * Writes the certificate in base64 encoded from to the print stream.
     * See http://tools.ietf.org/html/rfc4945#section-6.1 for more information
     */
    public static void dump(Certificate cert, PrintStream out) throws IOException,
            CertificateException {

        BASE64Encoder encoder = new BASE64Encoder();
        out.println(X509Factory.BEGIN_CERT);
        encoder.encodeBuffer(cert.getEncoded(), out);
        out.println(X509Factory.END_CERT);
    }

    public static void dumpPKCS12(String alias, File file, KeyStore ks, char[] password)
            throws Exception {
        Certificate[] certChain = ks.getCertificateChain(alias);
        Key key = ks.getKey(alias, KeyStores.getPassword());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, key, password, certChain);
        keyStore.store(bos, password);
    }
}
