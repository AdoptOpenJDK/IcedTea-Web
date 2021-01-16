/* CertificateUtils.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, version 2.

IcedTea is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
IcedTea; see the file COPYING. If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is making a
combined work based on this library. Thus, the terms and conditions of the GNU
General Public License cover the whole combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent modules, and
to copy and distribute the resulting executable under terms of your choice,
provided that you also meet, for each linked independent module, the terms and
conditions of the license of that module. An independent module is a module
which is not derived from or based on this library. If you modify this library,
you may extend this exception to your version of the library, but you are not
obligated to do so. If you do not wish to do so, delete this exception
statement from your version.
*/

package net.sourceforge.jnlp.security;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.io.IOUtils;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

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
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Common utilities to manipulate certificates. Provides methods to add
 * Certificates to a KeyStores, check if certificates already exist in a
 * KeyStore and printing certificates.
 */
public class CertificateUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateUtils.class);

    public static final String X_509 = "X509";

    /**
     * Adds the X509Certificate in the file to the KeyStore. Note that it does
     * not update the copy of the KeyStore on disk.
     *
     * @param file file with certificate
     * @param ks   keystore to save to
     * @throws java.security.cert.CertificateException if certificate is wrong
     * @throws java.io.IOException                     if IO fails
     * @throws java.security.KeyStoreException         if keystore fails
     */
    public static void addToKeyStore(final File file, final KeyStore ks) throws CertificateException,
            IOException, KeyStoreException {

        LOG.debug("Importing certificate from {} into {}", file, ks);

        final CertificateFactory cf = CertificateFactory.getInstance(X_509);
        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            final X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
            addToKeyStore(cert, ks);
        } catch (ClassCastException cce) {
            throw new CertificateException("Input file is not an X509 Certificate", cce);
        }
    }

    /**
     * Adds an X509Certificate to the KeyStore. Note that it does not update the
     * copy of the KeyStore on disk.
     *
     * @param cert certificate to import
     * @param ks   keystore to save to
     * @throws java.security.KeyStoreException if keystore fails
     */
    public static void addToKeyStore(final X509Certificate cert, final KeyStore ks)
            throws KeyStoreException {
        Objects.requireNonNull(ks);

        LOG.debug("Importing {}", cert.getSubjectX500Principal().getName());

        // does this certificate already exist?
        if (ks.getCertificateAlias(cert) != null) {
            return;
        }

        // create a unique alias for this new certificate
        final Random random = new Random(System.currentTimeMillis());
        if (ks.getCertificateAlias(cert) == null) {
            final String alias = new BigInteger(20, random).toString();
            if (ks.getCertificate(alias) == null) {
                ks.setCertificateEntry(alias, cert);
            }
        }
    }

    public static void addPKCS12ToKeyStore(File file, KeyStore ks, char[] password, KeyStore userCa) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(bis, password);
        KeyStore systemCa = KeyStores.getKeyStore(KeyStores.Level.SYSTEM, KeyStores.Type.CA_CERTS).getKs();

        Enumeration<String> aliasList = keyStore.aliases();

        while (aliasList.hasMoreElements()) {
            String alias = aliasList.nextElement();
            Certificate[] certChain = keyStore.getCertificateChain(alias);
            Key key = keyStore.getKey(alias, password);
            addPKCS12ToKeyStore(certChain, key, ks);
            // certificate authorities
            for (int i = 1; i < certChain.length; i++) {
                try {
                    // does this certificate already exist in system keystore?
                    if (systemCa.getCertificateAlias(certChain[i]) == null) {
                        addToKeyStore((X509Certificate) certChain[i], userCa);
                    }
                } catch (ClassCastException cce) {
                    LOG.warn("CA in input file is not an X509 Certificate");
                }
            }
        }
    }

    public static void addPKCS12ToKeyStore(Certificate[] certChain, Key key, KeyStore ks)
            throws KeyStoreException {
        // does this certificate already exist?
        String alias = ks.getCertificateAlias(certChain[0]);
        if (alias != null) {
            return;
        }

        // create a unique alias for this new certificate
        Random random = new Random();
        do {
            alias = new BigInteger(20, random).toString();
        } while (ks.getCertificate(alias) != null);

        SecurityUtil.setKeyEntry(ks, alias, key, certChain);
    }

    /**
     * Checks whether an X509Certificate is already in one of the keystores
     *
     * @param c         the certificate
     * @param keyStores the KeyStores to check in
     * @return true if the certificate is present in one of the keystores, false otherwise
     */
    public static boolean inKeyStores(X509Certificate c, List<KeyStore> keyStores) {
        for (KeyStore keyStore : keyStores) {
            try {
                // Check against all certs
                final Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    // Verify against this entry
                    final String alias = aliases.nextElement();
                    if (c.equals(keyStore.getCertificate(alias))) {
                        LOG.debug("{} found in cacerts ({})", c.getSubjectX500Principal().getName(), KeyStores.getPathToKeystore(keyStore));
                        return true;
                    } // else continue
                }
            } catch (KeyStoreException e) {
                LOG.error("exception while checking if certificate " + c + " is in keystore " + keyStore, e);
                // continue
            }
        }
        return false;
    }

    /**
     * Writes the certificate in base64 encoded from to the print stream.
     * See http://tools.ietf.org/html/rfc4945#section-6.1 for more information
     *
     * @param cert certificate to export
     * @param out  stream to print it to
     * @throws java.security.cert.CertificateException if certificate fails
     */
    public static void dump(Certificate cert, PrintStream out) throws CertificateException {
        out.println("-----BEGIN CERTIFICATE-----");
        final String encoded = IOUtils.toBase64splitIntoMultipleLines(cert.getEncoded(), 76);
        out.println(encoded);
        out.println("-----END CERTIFICATE-----");
    }

    public static void dumpPKCS12(String alias, File file, KeyStore ks, char[] password)
            throws Exception {
        Certificate[] certChain = ks.getCertificateChain(alias);
        Key key = SecurityUtil.getKey(ks, alias);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, key, password, certChain);
        keyStore.store(bos, password);
    }

    public static void saveCertificate(final X509Certificate certificate) {
        File keyStoreFile = null;
        try {
            KeyStore ks = KeyStores.getKeyStore(KeyStores.Level.USER, KeyStores.Type.CERTS).getKs();
            addToKeyStore(certificate, ks);
            keyStoreFile = KeyStores.getKeyStoreLocation(KeyStores.Level.USER, KeyStores.Type.CERTS).getFile();
            if (!keyStoreFile.isFile()) {
                FileUtils.createRestrictedFile(keyStoreFile);
            }
            SecurityUtil.storeKeyStore(ks, keyStoreFile);
            LOG.debug("Certificate is now permanently trusted.");
        } catch (Exception ex) {
            LOG.error(String.format("Error while add certificate '%s' to keystore '%s'.", certificate, keyStoreFile), ex);
        }
    }
}
