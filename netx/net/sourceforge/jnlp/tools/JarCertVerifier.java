/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;

import net.sourceforge.jnlp.JARDesc;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.cache.ResourceTracker;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.CertificateUtils;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.util.JarFile;
import net.sourceforge.jnlp.util.logging.OutputController;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.NetscapeCertTypeExtension;

/**
 * The jar certificate verifier utility.
 * 
 * @author Roland Schemers
 * @author Jan Luehe
 */

public class JarCertVerifier implements CertVerifier {

    private static final String META_INF = "META-INF/";

    // prefix for new signature-related files in META-INF directory
    private static final String SIG_PREFIX = META_INF + "SIG-";

    private static final long SIX_MONTHS = 180 * 24 * 60 * 60 * 1000L; // milliseconds

    static enum VerifyResult {
        UNSIGNED, SIGNED_OK, SIGNED_NOT_OK
    }

    /** All of the jar files that were verified for signing */
    private List<String> verifiedJars = new ArrayList<String>();

    /** All of the jar files that were not verified */
    private List<String> unverifiedJars = new ArrayList<String>();

    /** The certificates used for jar verification linked to their respective information */
    private Map<CertPath, CertInformation> certs = new HashMap<CertPath, CertInformation>();

    /** Temporary cert path hack to be used to keep track of which one a UI dialog is using */
    private CertPath currentlyUsed;

    /** Absolute location to jars and the number of entries which are possibly signable */
    private Map<String, Integer> jarSignableEntries = new HashMap<String, Integer>();

    /** The application verifier to use by this instance */
    private AppVerifier appVerifier;

    /**
     * Create a new jar certificate verifier utility that uses the provided verifier for its strategy pattern.
     * 
     * @param verifier
     *            The application verifier to be used by the new instance.
     */
    public JarCertVerifier(AppVerifier verifier) {
        appVerifier = verifier;
    }

    /**
     * Return true if there are no signable entries in the jar.
     * This will return false if any of verified jars have content more than just META-INF/.
     */
    public boolean isTriviallySigned() {
        return getTotalJarEntries(jarSignableEntries) <= 0
                && certs.size() <= 0;
    }

    public boolean getAlreadyTrustPublisher() {
        boolean allPublishersTrusted = appVerifier.hasAlreadyTrustedPublisher(
                certs, jarSignableEntries);
        OutputController.getLogger().log("App already has trusted publisher: "
                    + allPublishersTrusted);
        return allPublishersTrusted;
    }

    public boolean getRootInCacerts() {
        boolean allRootCAsTrusted = appVerifier.hasRootInCacerts(certs,
                jarSignableEntries);
        OutputController.getLogger().log("App has trusted root CA: " + allRootCAsTrusted);
        return allRootCAsTrusted;
    }

    public CertPath getCertPath(CertPath cPath) { // Parameter ignored.
        return currentlyUsed;
    }

    public boolean hasSigningIssues(CertPath certPath) {
        return certs.get(certPath).hasSigningIssues();
    }

    public List<String> getDetails(CertPath certPath) {
        if (certPath != null) {
            currentlyUsed = certPath;
        }
        return certs.get(currentlyUsed).getDetailsAsStrings();
    }

    /**
     * Get a list of the cert paths of all signers across the app.
     * 
     * @return List of CertPath vars representing each of the signers present on any jar.
     */
    public List<CertPath> getCertsList() {
        return new ArrayList<CertPath>(certs.keySet());
    }

    /**
     * Find the information the specified cert path has with respect to this application.
     * 
     * @return All the information the path has with this app.
     */
    public CertInformation getCertInformation(CertPath cPath) {
        return certs.get(cPath);
    }

    /**
     * Returns whether or not the app is considered completely signed.
     * 
     * An app using a JNLP is considered signed if all of the entries of its jars are signed by at least one common signer.
     * 
     * An applet on the other hand only needs to have each individual jar be fully signed by a signer. The signers can differ between jars.
     * 
     * @return Whether or not the app is considered signed.
     */
    // FIXME: Change javadoc once applets do not need entire jars signed.
    public boolean isFullySigned() {
        if (isTriviallySigned())
            return true;
        boolean fullySigned = appVerifier.isFullySigned(certs,
                jarSignableEntries);
        OutputController.getLogger().log("App already has trusted publisher: "
                    + fullySigned);
        return fullySigned;
    }

    public static boolean isJarSigned(JARDesc jar, AppVerifier verifier, ResourceTracker tracker) throws Exception {
        JarCertVerifier certVerifier = new JarCertVerifier(verifier);
        List<JARDesc> singleJarList = new ArrayList<JARDesc>();
        singleJarList.add(jar);
        certVerifier.add(singleJarList, tracker);
        return certVerifier.allJarsSigned();
    }

    /**
     * Update the verifier to consider new jars when verifying.
     * 
     * @param jars
     *            List of new jars to be verified.
     * @param tracker
     *            Resource tracker used to obtain the the jars from cache
     * @throws Exception
     *             Caused by issues with obtaining the jars' entries or interacting with the tracker.
     */
    public void add(List<JARDesc> jars, ResourceTracker tracker)
            throws Exception {
        verifyJars(jars, tracker);
    }

    /**
     * Verify the jars provided and update the state of this instance to match the new information.
     * 
     * @param jars
     *            List of new jars to be verified.
     * @param tracker
     *            Resource tracker used to obtain the the jars from cache
     * @throws Exception
     *             Caused by issues with obtaining the jars' entries or interacting with the tracker.
     */
    private void verifyJars(List<JARDesc> jars, ResourceTracker tracker)
            throws Exception {

        for (JARDesc jar : jars) {

            try {

                File jarFile = tracker.getCacheFile(jar.getLocation());

                // some sort of resource download/cache error. Nothing to add
                // in that case ... but don't fail here
                if (jarFile == null) {
                    continue;
                }

                String localFile = jarFile.getAbsolutePath();
                if (verifiedJars.contains(localFile)
                        || unverifiedJars.contains(localFile)) {
                    continue;
                }

                VerifyResult result = verifyJar(localFile);

                if (result == VerifyResult.UNSIGNED) {
                    unverifiedJars.add(localFile);
                } else if (result == VerifyResult.SIGNED_NOT_OK) {
                    verifiedJars.add(localFile);
                } else if (result == VerifyResult.SIGNED_OK) {
                    verifiedJars.add(localFile);
                }
            } catch (Exception e) {
                // We may catch exceptions from using verifyJar()
                // or from checkTrustedCerts
                throw e;
            }
        }

        for (CertPath certPath : certs.keySet())
            checkTrustedCerts(certPath);
    }

    /**
     * Checks through all the jar entries of jarName for signers, storing all the common ones in the certs hash map.
     * 
     * @param jarName
     *            The absolute path to the jar file.
     * @return The return of {@link JarCertVerifier#verifyJarEntryCerts} using the entries found in the jar located at jarName.
     * @throws Exception
     *             Will be thrown if there are any problems with the jar.
     */
    private VerifyResult verifyJar(String jarName) throws Exception {
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(jarName, true);
            Vector<JarEntry> entriesVec = new Vector<JarEntry>();
            byte[] buffer = new byte[8192];

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                entriesVec.addElement(je);

                InputStream is = jarFile.getInputStream(je);
                try {
                    while (is.read(buffer, 0, buffer.length) != -1) {
                        // we just read. this will throw a SecurityException
                        // if a signature/digest check fails.
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
            return verifyJarEntryCerts(jarName, jarFile.getManifest() != null,
                    entriesVec);

        } catch (Exception e) {
            OutputController.getLogger().log(OutputController.Level.ERROR_ALL, e);
            throw e;
        } finally { // close the resource
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }

    /**
     * Checks through all the jar entries for signers, storing all the common ones in the certs hash map.
     * 
     * @param jarName
     *            The absolute path to the jar file.
     * @param jarHasManifest
     *            Whether or not the associated jar has a manifest.
     * @param entries
     *            The list of entries in the associated jar.
     * @return If there is at least one signable entry that is not signed by a common signer, return UNSIGNED. Otherwise every signable entry is signed by at least one common signer. If the signer has no issues, return SIGNED_OK. If there are any signing issues, return SIGNED_NOT_OK.
     * @throws Exception
     *             Will be thrown if there are issues with entries.
     */
    VerifyResult verifyJarEntryCerts(String jarName, boolean jarHasManifest,
            Vector<JarEntry> entries) throws Exception {
        // Contains number of entries the cert with this CertPath has signed.
        Map<CertPath, Integer> jarSignCount = new HashMap<CertPath, Integer>();
        int numSignableEntriesInJar = 0;

        // Record current time just before checking the jar begins.
        long now = System.currentTimeMillis();
        if (jarHasManifest) {

            for (JarEntry je : entries) {
                String name = je.getName();
                CodeSigner[] signers = je.getCodeSigners();
                boolean isSigned = (signers != null);

                boolean shouldHaveSignature = !je.isDirectory()
                        && !isMetaInfFile(name);

                if (shouldHaveSignature) {
                    numSignableEntriesInJar++;
                }

                if (shouldHaveSignature && isSigned) {
                    for (int i = 0; i < signers.length; i++) {
                        CertPath certPath = signers[i].getSignerCertPath();

                        if (!jarSignCount.containsKey(certPath))
                            jarSignCount.put(certPath, 1);
                        else
                            jarSignCount.put(certPath,
                                    jarSignCount.get(certPath) + 1);
                    }
                }
            } // while e has more elements
        } else { // if manifest is null

            // Else increment total entries by 1 so that unsigned jars with
            // no manifests can't sneak in
            numSignableEntriesInJar++;
        }

        jarSignableEntries.put(jarName, numSignableEntriesInJar);

        // Find all signers that have signed every signable entry in this jar.
        boolean allEntriesSignedBySingleCert = false;
        for (CertPath certPath : jarSignCount.keySet()) {
            if (jarSignCount.get(certPath) == numSignableEntriesInJar) {
                allEntriesSignedBySingleCert = true;

                boolean wasPreviouslyVerified = certs.containsKey(certPath);
                if (!wasPreviouslyVerified)
                    certs.put(certPath, new CertInformation());

                CertInformation certInfo = certs.get(certPath);
                if (wasPreviouslyVerified)
                    certInfo.resetForReverification();

                certInfo.setNumJarEntriesSigned(jarName,
                        numSignableEntriesInJar);

                Certificate cert = certPath.getCertificates().get(0);
                if (cert instanceof X509Certificate) {
                    checkCertUsage(certPath, (X509Certificate) cert, null);
                    long notBefore = ((X509Certificate) cert).getNotBefore().getTime();
                    long notAfter = ((X509Certificate) cert).getNotAfter().getTime();
                    if (now < notBefore) {
                        certInfo.setNotYetValidCert();
                    }

                    if (notAfter < now) {
                        certInfo.setHasExpiredCert();
                    } else if (notAfter < now + SIX_MONTHS) {
                        certInfo.setHasExpiringCert();
                    }
                }
            }
        }

        // Every signable entry of this jar needs to be signed by at least
        // one signer for the jar to be considered successfully signed.
        VerifyResult result = null;

        if (numSignableEntriesInJar == 0) {
            // Allow jars with no signable entries to simply be considered signed.
            // There should be no security risk in doing so.
            result = VerifyResult.SIGNED_OK;
        } else if (allEntriesSignedBySingleCert) {

            // We need to find at least one signer without any issues.
            for (CertPath entryCertPath : jarSignCount.keySet()) {
                if (certs.containsKey(entryCertPath)
                        && !hasSigningIssues(entryCertPath)) {
                    result = VerifyResult.SIGNED_OK;
                    break;
                }
            }
            if (result == null) {
                // All signers had issues
                result = VerifyResult.SIGNED_NOT_OK;
            }
        } else {
            result = VerifyResult.UNSIGNED;
        }

        OutputController.getLogger().log("Jar found at " + jarName
                    + "has been verified as " + result);
        return result;
    }

    /**
     * Checks the user's trusted.certs file and the cacerts file to see if a
     * publisher's and/or CA's certificate exists there.
     *
     * @param certPath
     *            The cert path of the signer being checked for trust.
     */
    private void checkTrustedCerts(CertPath certPath) throws Exception {
        CertInformation info = certs.get(certPath);
        try {
            X509Certificate publisher = (X509Certificate) getPublisher(certPath);
            KeyStore[] certKeyStores = KeyStores.getCertKeyStores();
            if (CertificateUtils.inKeyStores(publisher, certKeyStores))
                info.setAlreadyTrustPublisher();
            KeyStore[] caKeyStores = KeyStores.getCAKeyStores();
            // Check entire cert path for a trusted CA
            for (Certificate c : certPath.getCertificates()) {
                if (CertificateUtils.inKeyStores((X509Certificate) c,
                        caKeyStores)) {
                    info.setRootInCacerts();
                    return;
                }
            }
        } catch (Exception e) {
            // TODO: Warn user about not being able to
            // look through their cacerts/trusted.certs
            // file depending on exception.
            OutputController.getLogger().log("WARNING: Unable to read through cert store files.");
            throw e;
        }

        // Otherwise a parent cert was not found to be trusted.
        info.setUntrusted();
    }

    public void setCurrentlyUsedCertPath(CertPath cPath) {
        currentlyUsed = cPath;
    }

    public Certificate getPublisher(CertPath cPath) {
        if (cPath != null) {
            currentlyUsed = cPath;
        }
        if (currentlyUsed != null) {
            List<? extends Certificate> certList = currentlyUsed
                    .getCertificates();
            if (certList.size() > 0) {
                return certList.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Certificate getRoot(CertPath cPath) {
        if (cPath != null) {
            currentlyUsed = cPath;
        }
        if (currentlyUsed != null) {
            List<? extends Certificate> certList = currentlyUsed
                    .getCertificates();
            if (certList.size() > 0) {
                return certList.get(certList.size() - 1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns whether a file is in META-INF, and thus does not require signing.
     * 
     * Signature-related files under META-INF include: . META-INF/MANIFEST.MF . META-INF/SIG-* . META-INF/*.SF . META-INF/*.DSA . META-INF/*.RSA
     */
    static boolean isMetaInfFile(String name) {
        String ucName = name.toUpperCase();
        return ucName.startsWith(META_INF);
    }

    /**
     * Check if userCert is designed to be a code signer
     * 
     * @param userCert
     *            the certificate to be examined
     * @param bad
     *            3 booleans to show if the KeyUsage, ExtendedKeyUsage, 
     *            NetscapeCertType has codeSigning flag turned on. If null, 
     *            the class field badKeyUsage, badExtendedKeyUsage, 
     *            badNetscapeCertType will be set.
     * 
     *            Required for verifyJar()
     */
    void checkCertUsage(CertPath certPath, X509Certificate userCert,
            boolean[] bad) {

        // Can act as a signer?
        // 1. if KeyUsage, then [0] should be true
        // 2. if ExtendedKeyUsage, then should contains ANY or CODE_SIGNING
        // 3. if NetscapeCertType, then should contains OBJECT_SIGNING
        // 1,2,3 must be true

        if (bad != null) {
            bad[0] = bad[1] = bad[2] = false;
        }

        boolean[] keyUsage = userCert.getKeyUsage();
        if (keyUsage != null) {
            if (keyUsage.length < 1 || !keyUsage[0]) {
                if (bad != null) {
                    bad[0] = true;
                } else {
                    certs.get(certPath).setBadKeyUsage();
                }
            }
        }

        try {
            List<String> xKeyUsage = userCert.getExtendedKeyUsage();
            if (xKeyUsage != null) {
                if (!xKeyUsage.contains("2.5.29.37.0") // anyExtendedKeyUsage
                        && !xKeyUsage.contains("1.3.6.1.5.5.7.3.3")) { // codeSigning
                    if (bad != null) {
                        bad[1] = true;
                    } else {
                        certs.get(certPath).setBadExtendedKeyUsage();
                    }
                }
            }
        } catch (java.security.cert.CertificateParsingException e) {
            // shouldn't happen
        }

        try {
            // OID_NETSCAPE_CERT_TYPE
            byte[] netscapeEx = userCert
                    .getExtensionValue("2.16.840.1.113730.1.1");
            if (netscapeEx != null) {
                DerInputStream in = new DerInputStream(netscapeEx);
                byte[] encoded = in.getOctetString();
                encoded = new DerValue(encoded).getUnalignedBitString()
                        .toByteArray();

                NetscapeCertTypeExtension extn = new NetscapeCertTypeExtension(
                        encoded);

                Boolean val = (Boolean) extn
                        .get(NetscapeCertTypeExtension.OBJECT_SIGNING);
                if (!val) {
                    if (bad != null) {
                        bad[2] = true;
                    } else {
                        certs.get(certPath).setBadNetscapeCertType();
                    }
                }
            }
        } catch (IOException e) {
            //
        }
    }

    /**
     * Returns if all jars are signed.
     * 
     * @return True if all jars are signed, false if there are one or more unsigned jars
     */
    public boolean allJarsSigned() {
        return this.unverifiedJars.size() == 0;
    }

    public void checkTrustWithUser(SecurityDelegate securityDelegate, JNLPFile file) throws LaunchException {
        appVerifier.checkTrustWithUser(securityDelegate, this, file);
    }

    public Map<String, Integer> getJarSignableEntries() {
        return Collections.unmodifiableMap(jarSignableEntries);
    }

    /**
     * Get the total number of entries in the provided map.
     * 
     * @return The number of entries.
     */
    public static int getTotalJarEntries(Map<String, Integer> map) {
        int sum = 0;
        for (int value : map.values()) {
            sum += value;
        }
        return sum;
    }
}
