/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package net.sourceforge.jnlp.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Random;
import java.util.Hashtable;
import java.util.Vector;

import net.sourceforge.jnlp.security.SecurityUtil;

import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

/**
 * This tool manages the user's trusted certificates
 *
 * @author Jan Luehe
 * @author Joshua Sumali
 */
public class KeyTool {

        // The user's keystore.
        private KeyStore usercerts = null;
        // JDK cacerts
        private KeyStore cacerts = null;
        // System ca-bundle.crt
        private KeyStore systemcerts = null;

        private String fullCertPath = SecurityUtil.getTrustedCertsFilename();

        private FileOutputStream fos = null;

        /**
         * Whether we trust the system cacerts file.
         */
        private boolean trustcacerts = true;

        /**
         * Whether we print certificates in rfc, base64 encoding.
         */
        private boolean rfc = true;

        private final char[] password = "changeit".toCharArray();

        /**
         * Whether we prompt for user input.
         */
        private boolean noprompt = true;

        public KeyTool() throws Exception {

                // Initialize all the keystores.
                usercerts = SecurityUtil.getUserKeyStore();
                cacerts = SecurityUtil.getCacertsKeyStore();
                systemcerts = SecurityUtil.getSystemCertStore();
        }

        /**
         * Adds a trusted certificate to the user's keystore.
         * @return true if the add was successful, false otherwise.
         */
        public boolean importCert(File file) throws Exception {

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                X509Certificate cert = null;

                if (bis.available() >= 1) {
                        try {
                        cert = (X509Certificate)cf.generateCertificate(bis);
                        } catch (ClassCastException cce) {
                                throw new Exception("Input file is not an X509 Certificate");
                        } catch (CertificateException ce) {
                                throw new Exception("Input file is not an X509 Certificate");
                        }
                }

                return importCert((Certificate)cert);
        }

        /**
         * Adds a trusted certificate to the user's keystore.
         * @return true if the add was successful, false otherwise.
         */
        public boolean importCert(Certificate cert) throws Exception {

                String alias = usercerts.getCertificateAlias(cert);

                if (alias != null) { //cert already exists
                        return true;
                } else {
                        String newAlias = getRandomAlias();
                        //check to make sure this alias doesn't exist
                        while (usercerts.getCertificate(newAlias) != null)
                                newAlias = getRandomAlias();
                        return addTrustedCert(newAlias, cert);
                }
        }

        /**
         * Generates a random alias for storing a trusted Certificate.
         */
        private String getRandomAlias() {
                Random r = new Random();
                String token = Long.toString(Math.abs(r.nextLong()), 36);
                return "trustedCert-" + token;
        }

        /**
     * Prints all keystore entries.
     */
        private void doPrintEntries(PrintStream out) throws Exception {

                out.println("KeyStore type: " + usercerts.getType());
                out.println("KeyStore provider: " + usercerts.getProvider().toString());
                out.println();

                for (Enumeration<String> e = usercerts.aliases(); e.hasMoreElements();) {
                        String alias = e.nextElement();
                        doPrintEntry(alias, out, false);
                }
        }

    /**
     * Prints a single keystore entry.
     */
        private void doPrintEntry(String alias, PrintStream out,
                        boolean printWarning) throws Exception {

                if (usercerts.containsAlias(alias) == false) {
                        throw new Exception("Alias does not exist");
                }

                if (usercerts.entryInstanceOf(alias,
                                KeyStore.TrustedCertificateEntry.class)) {
                        Certificate cert = usercerts.getCertificate(alias);

                        out.println("Alias: " + alias);
                        out.println("Date Created: " + usercerts.getCreationDate(alias));
                        out.println("Subject: " + SecurityUtil.getCN(((X509Certificate)usercerts
                                .getCertificate(alias)).getSubjectX500Principal().getName()));
                        out.println("Certificate fingerprint (MD5): "
                                        + getCertFingerPrint("MD5", cert));
                        out.println();
                }
        }

    /**
     * Gets the requested finger print of the certificate.
     */
        private String getCertFingerPrint(String mdAlg, Certificate cert)
                throws Exception {
                byte[] encCertInfo = cert.getEncoded();
                MessageDigest md = MessageDigest.getInstance(mdAlg);
                byte[] digest = md.digest(encCertInfo);
                return toHexString(digest);
        }

    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     */
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        }
        return buf.toString();
    }

        /**
         * Adds a certificate to the keystore, and writes new keystore to disk.
         */
    private boolean addTrustedCert(String alias, Certificate cert)
        throws Exception {

        if (isSelfSigned((X509Certificate)cert)) {
                        //will throw exception if this fails
                cert.verify(cert.getPublicKey());
                }

        if (noprompt) {
                usercerts.setCertificateEntry(alias, cert);
                        fos = new FileOutputStream(fullCertPath);
                        usercerts.store(fos, password);
                        fos.close();
                return true;
        }

        return false;
    }

    /**
     * Returns true if the given certificate is trusted, false otherwise.
     */
    public boolean isTrusted(Certificate cert) throws Exception {
        if (cert != null) {
                if (usercerts.getCertificateAlias(cert) != null) {
                        return true; // found in own keystore
                }
                return false;
        } else {
                return false;
        }
    }

    /**
     * Returns true if the certificate is self-signed, false otherwise.
     */
    private boolean isSelfSigned(X509Certificate cert) {
        return cert.getSubjectDN().equals(cert.getIssuerDN());
    }

    /**
     * Checks if a given certificate is part of the user's cacerts
     * keystore.
     * @param c the certificate to check
     * @returns true if the certificate is in the user's cacerts and
     * false otherwise
     */
    public boolean checkCacertsForCertificate(Certificate c) throws Exception {
        if (c != null) {

                        String alias = null;

                        //first try jdk cacerts.
                        if (cacerts != null) {
                        alias = cacerts.getCertificateAlias(c);

                                //if we can't find it here, try the system certs.
                                if (alias == null && systemcerts != null)
                                        alias = systemcerts.getCertificateAlias(c);
                        }
                        //otherwise try the system certs if you can't use the jdk certs.
                        else if (systemcerts != null)
                                alias = systemcerts.getCertificateAlias(c);

                return (alias != null);
        } else
                return false;
    }

    /**
     * Establishes a certificate chain (using trusted certificates in the
     * keystore), starting with the user certificate
     * and ending at a self-signed certificate found in the keystore.
     *
     * @param userCert the user certificate of the alias
     * @param certToVerify the single certificate provided in the reply
     */
    public boolean establishCertChain(Certificate userCert,
                                             Certificate certToVerify)
        throws Exception
    {
        if (userCert != null) {
            // Make sure that the public key of the certificate reply matches
            // the original public key in the keystore
            PublicKey origPubKey = userCert.getPublicKey();
            PublicKey replyPubKey = certToVerify.getPublicKey();
            if (!origPubKey.equals(replyPubKey)) {
                // TODO: something went wrong -- throw exception
                throw new Exception(
                        "Public keys in reply and keystore don't match");
            }

            // If the two certs are identical, we're done: no need to import
            // anything
            if (certToVerify.equals(userCert)) {
                throw new Exception(
                        "Certificate reply and certificate in keystore are identical");
            }
        }

        // Build a hash table of all certificates in the keystore.
        // Use the subject distinguished name as the key into the hash table.
        // All certificates associated with the same subject distinguished
        // name are stored in the same hash table entry as a vector.
        Hashtable<Principal, Vector<Certificate>> certs = null;
        if (usercerts.size() > 0) {
            certs = new Hashtable<Principal, Vector<Certificate>>(11);
            keystorecerts2Hashtable(usercerts, certs);
        }
        if (trustcacerts) { //if we're trusting the cacerts
                KeyStore caks = SecurityUtil.getCacertsKeyStore();
            if (caks!=null && caks.size()>0) {
                if (certs == null) {
                    certs = new Hashtable<Principal, Vector<Certificate>>(11);
                }
                keystorecerts2Hashtable(caks, certs);
            }
        }

        // start building chain
        Vector<Certificate> chain = new Vector<Certificate>(2);
        if (buildChain((X509Certificate)certToVerify, chain, certs)) {
            Certificate[] newChain = new Certificate[chain.size()];
            // buildChain() returns chain with self-signed root-cert first and
            // user-cert last, so we need to invert the chain before we store
            // it
            int j=0;
            for (int i=chain.size()-1; i>=0; i--) {
                newChain[j] = chain.elementAt(i);
                j++;
            }
            //return newChain;
            return newChain != null;
        } else {
            throw new Exception("Failed to establish chain from reply");
        }
    }

    /**
     * Stores the (leaf) certificates of a keystore in a hashtable.
     * All certs belonging to the same CA are stored in a vector that
     * in turn is stored in the hashtable, keyed by the CA's subject DN
     */
    private void keystorecerts2Hashtable(KeyStore ks,
                Hashtable<Principal, Vector<Certificate>> hash)
        throws Exception {

        for (Enumeration<String> aliases = ks.aliases();
                                        aliases.hasMoreElements(); ) {
            String alias = aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            if (cert != null) {
                Principal subjectDN = ((X509Certificate)cert).getSubjectDN();
                Vector<Certificate> vec = hash.get(subjectDN);
                if (vec == null) {
                    vec = new Vector<Certificate>();
                    vec.addElement(cert);
                } else {
                    if (!vec.contains(cert)) {
                        vec.addElement(cert);
                    }
                }
                hash.put(subjectDN, vec);
            }
        }
    }

    /**
     * Recursively tries to establish chain from pool of trusted certs.
     *
     * @param certToVerify the cert that needs to be verified.
     * @param chain the chain that's being built.
     * @param certs the pool of trusted certs
     *
     * @return true if successful, false otherwise.
     */
    private boolean buildChain(X509Certificate certToVerify,
                        Vector<Certificate> chain,
                        Hashtable<Principal, Vector<Certificate>> certs) {
        Principal subject = certToVerify.getSubjectDN();
        Principal issuer = certToVerify.getIssuerDN();
        if (subject.equals(issuer)) {
            // reached self-signed root cert;
            // no verification needed because it's trusted.
            chain.addElement(certToVerify);
            return true;
        }

        // Get the issuer's certificate(s)
        Vector<Certificate> vec = certs.get(issuer);
        if (vec == null) {
            return false;
        }

        // Try out each certificate in the vector, until we find one
        // whose public key verifies the signature of the certificate
        // in question.
        for (Enumeration<Certificate> issuerCerts = vec.elements();
             issuerCerts.hasMoreElements(); ) {
            X509Certificate issuerCert
                = (X509Certificate)issuerCerts.nextElement();
            PublicKey issuerPubKey = issuerCert.getPublicKey();
            try {
                certToVerify.verify(issuerPubKey);
            } catch (Exception e) {
                continue;
            }
            if (buildChain(issuerCert, chain, certs)) {
                chain.addElement(certToVerify);
                return true;
            }
        }
        return false;
    }

    public static void dumpCert(Certificate cert, PrintStream out)
        throws IOException, CertificateException {

        boolean printRfc = true;
        if (printRfc) {
            BASE64Encoder encoder = new BASE64Encoder();
            out.println(X509Factory.BEGIN_CERT);
            encoder.encodeBuffer(cert.getEncoded(), out);
            out.println(X509Factory.END_CERT);
        } else {
            out.write(cert.getEncoded()); // binary
        }
    }

        public static void main(String[] args) throws Exception {
                KeyTool kt = new KeyTool();
                kt.doPrintEntries(System.out);
        }
}
