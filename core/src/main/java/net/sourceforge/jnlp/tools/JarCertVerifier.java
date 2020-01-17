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

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AppVerifier;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.CertificateUtils;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.util.JarFile;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.NetscapeCertTypeExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.MONTHS;

/**
 * The jar certificate verifier utility.
 *
 * @author Roland Schemers
 * @author Jan Luehe
 */

public class JarCertVerifier implements CertVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(JarCertVerifier.class);

    private static final String META_INF = "META-INF/";
    private static final Pattern SIG = Pattern.compile(".*" + META_INF + "SIG-.*");

    enum VerifyResult {
        UNSIGNED, SIGNED_OK, SIGNED_NOT_OK
    }

    /**
     * All of the jar files that were verified for signing
     */
    private final List<String> verifiedJars = new ArrayList<>();

    /**
     * All of the jar files that were not verified
     */
    private final List<String> unverifiedJars = new ArrayList<>();

    /**
     * The certificates used for jar verification linked to their respective information
     */
    private final Map<CertPath, CertInformation> certs = new HashMap<>();

    /**
     * Absolute location to jars and the number of entries which are possibly signable
     */
    private final Map<String, Integer> jarSignableEntries = new HashMap<>();

    /**
     * The application verifier to use by this instance
     */
    private final AppVerifier appVerifier;

    /**
     * Temporary cert path hack to be used to keep track of which one a UI dialog is using
     */
    private CertPath currentlyUsed;

    /**
     * Create a new jar certificate verifier utility that uses the provided verifier for its strategy pattern.
     *
     * @param verifier The application verifier to be used by the new instance.
     */
    public JarCertVerifier(AppVerifier verifier) {
        appVerifier = verifier;
    }

    /**
     * @return true if there are no signable entries in the jar.
     * This will return false if any of verified jars have content more than just META-INF/.
     */
    public boolean isTriviallySigned() {
        return getTotalJarEntries(jarSignableEntries) <= 0 && certs.size() <= 0;
    }

    @Override
    public boolean getAlreadyTrustPublisher() {
        final boolean allPublishersTrusted = appVerifier.hasAlreadyTrustedPublisher(certs, jarSignableEntries);
        LOG.debug("App already has trusted publisher: {}", allPublishersTrusted);
        return allPublishersTrusted;
    }

    @Override
    public boolean getRootInCaCerts() {
        final boolean allRootCAsTrusted = appVerifier.hasRootInCacerts(certs, jarSignableEntries);
        LOG.debug("App has trusted root CA: {}", allRootCAsTrusted);
        return allRootCAsTrusted;
    }

    @Override
    public CertPath getCertPath() {
        return currentlyUsed;
    }

    @Override
    public List<String> getDetails(final CertPath certPath) {
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
        return new ArrayList<>(certs.keySet());
    }

    /**
     * Find the information the specified cert path has with respect to this application.
     *
     * @param cPath certificate to provide info
     * @return All the information the path has with this app.
     */
    public CertInformation getCertInformation(final CertPath cPath) {
        return certs.get(cPath);
    }

    /**
     * Returns whether or not the app is considered completely signed.
     * <p>
     * An app using a JNLP is considered signed if all of the entries of its jars are signed by at least one common signer.
     * <p>
     * An applet on the other hand only needs to have each individual jar be fully signed by a signer. The signers can differ between jars.
     *
     * @return Whether or not the app is considered signed.
     */
    // FIXME: Change javadoc once applets do not need entire jars signed.
    public boolean isFullySigned() {
        return isTriviallySigned() || isSigned();
    }

    private boolean isSigned() {
        final boolean fullySigned = appVerifier.isFullySigned(certs, jarSignableEntries);
        LOG.debug("App already has trusted publisher: {}", fullySigned);
        return fullySigned;
    }

    public static boolean isJarSigned(final JARDesc jar, final AppVerifier verifier, final ResourceTracker tracker) throws Exception {
        final JarCertVerifier certVerifier = new JarCertVerifier(verifier);
        certVerifier.add(jar, tracker);
        return certVerifier.allJarsSigned();
    }

    /**
     * Update the verifier to consider a new jar when verifying.
     *
     * @param jar     A new jars to be verified.
     * @param tracker Resource tracker used to obtain the the jars from cache
     * @throws Exception Caused by issues with obtaining the jars' entries or interacting with the tracker.
     */
    public void add(final JARDesc jar, final ResourceTracker tracker) throws Exception {
        verifyJars(Collections.singletonList(jar), tracker);
    }

    /**
     * Update the verifier to consider new jars when verifying.
     *
     * @param jars    List of new jars to be verified.
     * @param tracker Resource tracker used to obtain the the jars from cache
     * @throws Exception Caused by issues with obtaining the jars' entries or interacting with the tracker.
     */
    public void add(final List<JARDesc> jars, final ResourceTracker tracker) throws Exception {
        verifyJars(jars, tracker);
    }

    /**
     * Verify the jars provided and update the state of this instance to match the new information.
     *
     * @param jars    List of new jars to be verified.
     * @param tracker Resource tracker used to obtain the the jars from cache
     * @throws Exception Caused by issues with obtaining the jars' entries or interacting with the tracker.
     */
    private void verifyJars(final List<JARDesc> jars, final ResourceTracker tracker) throws Exception {

        for (JARDesc jar : jars) {
            final File jarFile = tracker.getCacheFile(jar.getLocation());

            // some sort of resource download/cache error. Nothing to add
            // in that case ... but don't fail here
            if (jarFile == null || !jarFile.isFile()) {
                continue;
            }

            final String jarPath = jarFile.getCanonicalFile().getAbsolutePath();
            if (verifiedJars.contains(jarPath) || unverifiedJars.contains(jarPath)) {
                continue;
            }

            final VerifyResult result = verifyJar(jarPath);
            if (result == VerifyResult.UNSIGNED) {
                unverifiedJars.add(jarPath);
            } else if (result == VerifyResult.SIGNED_NOT_OK) {
                verifiedJars.add(jarPath);
            } else if (result == VerifyResult.SIGNED_OK) {
                verifiedJars.add(jarPath);
            }
        }

        for (CertPath certPath : certs.keySet()) {
            checkTrustedCerts(certPath);
        }
    }

    /**
     * Checks through all the jar entries of jarName for signers, storing all the common ones in the certs hash map.
     *
     * @param jarPath The absolute path to the jar file.
     * @return The return of {@link JarCertVerifier#verifyJarEntryCerts} using the entries found in the jar located at jarName.
     */
    private VerifyResult verifyJar(final String jarPath) {
        try (final JarFile jarFile = new JarFile(jarPath, true)) {
            final List<JarEntry> entries = new ArrayList<>();
            final byte[] buffer = new byte[8192];

            final Enumeration<JarEntry> entriesEnum = jarFile.entries();
            while (entriesEnum.hasMoreElements()) {
                final JarEntry entry = entriesEnum.nextElement();
                entries.add(entry);

                try (InputStream is = jarFile.getInputStream(entry)) {
                    //noinspection StatementWithEmptyBody
                    while (is.read(buffer, 0, buffer.length) != -1) {
                        // we just read. this will throw a SecurityException
                        // if a signature/digest check fails.
                    }
                }
            }
            return verifyJarEntryCerts(jarPath, jarFile.getManifest() != null, entries);
        } catch (Exception e) {
            LOG.error("Error in verify jar " + jarPath, e);
            throw new RuntimeException("Error in verify jar " + jarPath, e);
        }
    }

    /**
     * Checks through all the jar entries for signers, storing all the common ones in the certs hash map.
     *
     * @param jarPath        The absolute path to the jar file.
     * @param jarHasManifest Whether or not the associated jar has a manifest.
     * @param entries        The list of entries in the associated jar.
     * @return If there is at least one signable entry that is not signed by a common signer, return UNSIGNED. Otherwise every signable entry is signed by at least one common signer. If the signer has no issues, return SIGNED_OK. If there are any signing issues, return SIGNED_NOT_OK.
     * @throws RuntimeException Will be thrown if there are issues with entries.
     */
    VerifyResult verifyJarEntryCerts(final String jarPath, final boolean jarHasManifest, final List<JarEntry> entries) {
        // Contains number of entries the cert with this CertPath has signed.
        final Map<CertPath, Integer> jarSignCount = new HashMap<>();
        int numSignableEntriesInJar = 0;

        // Record current time just before checking the jar begins.
        final ZonedDateTime now = ZonedDateTime.now();
        if (jarHasManifest) {
            for (JarEntry je : entries) {
                final boolean shouldHaveSignature = !je.isDirectory() && !isMetaInfFile(je.getName());
                if (shouldHaveSignature) {
                    numSignableEntriesInJar++;
                    final CodeSigner[] signers = je.getCodeSigners();
                    if (signers != null) {
                        for (final CodeSigner signer : signers) {
                            final CertPath certPath = signer.getSignerCertPath();
                            jarSignCount.putIfAbsent(certPath, 0);
                            jarSignCount.computeIfPresent(certPath, (cp, count) -> count + 1);
                        }
                    }
                }
            }
        } else {
            // set to 1 so that unsigned jars with no manifests can't sneak in
            numSignableEntriesInJar = 1;
        }

        jarSignableEntries.put(jarPath, numSignableEntriesInJar);

        // Find all signers that have signed every signable entry in this jar.
        boolean allEntriesSignedBySingleCert = false;
        for (CertPath certPath : jarSignCount.keySet()) {
            if (jarSignCount.get(certPath) == numSignableEntriesInJar) {
                allEntriesSignedBySingleCert = true;

                final CertInformation certInfo = certs.computeIfAbsent(certPath, k -> new CertInformation());
                certInfo.resetForReverification();
                certInfo.setNumJarEntriesSigned(jarPath, numSignableEntriesInJar);

                final Certificate cert = certPath.getCertificates().get(0);
                if (cert instanceof X509Certificate) {
                    checkCertUsage(certPath, (X509Certificate) cert);
                    final ZonedDateTime notBefore = zonedDateTime(((X509Certificate) cert).getNotBefore());
                    final ZonedDateTime notAfter = zonedDateTime(((X509Certificate) cert).getNotAfter());
                    if (now.isBefore(notBefore)) {
                        certInfo.setNotYetValidCert();
                    }
                    if (now.isAfter(notAfter)) {
                        certInfo.setHasExpiredCert();
                    } else if (now.plus(6, MONTHS).isAfter(notAfter)) {
                        certInfo.setHasExpiringCert();
                    }
                }
            }
        }

        // Every signable entry of this jar needs to be signed by at least
        // one signer for the jar to be considered successfully signed.
        final VerifyResult result;
        if (numSignableEntriesInJar == 0) {
            // Allow jars with no signable entries to simply be considered signed.
            // There should be no security risk in doing so.
            result = VerifyResult.SIGNED_OK;
        } else if (allEntriesSignedBySingleCert) {
            // We need to find at least one signer without any issues.
            result = verifySigners(jarSignCount);
        } else {
            result = VerifyResult.UNSIGNED;
        }

        LOG.debug("Jar found at {} has been verified as {}", jarPath, result);
        return result;
    }

    private VerifyResult verifySigners(final Map<CertPath, Integer> jarSignCount) {
        for (CertPath entryCertPath : jarSignCount.keySet()) {
            if (certs.containsKey(entryCertPath) && !certs.get(entryCertPath).hasSigningIssues()) {
                return VerifyResult.SIGNED_OK;
            }
        }
        // All signers had issues
        return VerifyResult.SIGNED_NOT_OK;
    }

    private ZonedDateTime zonedDateTime(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

    /**
     * Checks the user's trusted.certs file and the cacerts file to see if a
     * publisher's and/or CA's certificate exists there.
     *
     * @param certPath The cert path of the signer being checked for trust.
     */
    private void checkTrustedCerts(final CertPath certPath) {
        final CertInformation info = certs.get(certPath);
        try {
            final X509Certificate publisher = (X509Certificate) getPublisher(certPath);
            final KeyStore[] certKeyStores = KeyStores.getCertKeyStores();
            if (CertificateUtils.inKeyStores(publisher, certKeyStores)) {
                info.setAlreadyTrustPublisher();
            }
            final KeyStore[] caKeyStores = KeyStores.getCAKeyStores();
            // Check entire cert path for a trusted CA
            for (final Certificate c : certPath.getCertificates()) {
                if (c instanceof X509Certificate) {
                    final X509Certificate x509 = (X509Certificate) c;
                    if (CertificateUtils.inKeyStores(x509, caKeyStores)) {
                        info.setRootInCacerts();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: Warn user about not being able to
            // look through their cacerts/trusted.certs
            // file depending on exception.
            LOG.warn("Unable to read through cert store files.");
            throw e;
        }

        // Otherwise a parent cert was not found to be trusted.
        info.setUntrusted();
    }

    public void setCurrentlyUsedCertPath(final CertPath certPath) {
        currentlyUsed = certPath;
    }

    @Override
    public Certificate getPublisher(final CertPath certPath) {
        if (certPath != null) {
            currentlyUsed = certPath;
        }
        if (currentlyUsed != null) {
            final List<? extends Certificate> certList = currentlyUsed.getCertificates();
            if (certList.size() > 0) {
                return certList.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Certificate getRoot(final CertPath certPath) {
        if (certPath != null) {
            currentlyUsed = certPath;
        }
        if (currentlyUsed != null) {
            final List<? extends Certificate> certList = currentlyUsed.getCertificates();
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
     * <p>
     * Signature-related files under META-INF include: . META-INF/MANIFEST.MF . META-INF/SIG-* . META-INF/*.SF . META-INF/*.DSA . META-INF/*.RSA
     */
    static boolean isMetaInfFile(final String name) {
        if (name.endsWith("class")) {
            return false;
        }
        return name.startsWith(META_INF) && (
                name.endsWith(".MF") ||
                name.endsWith(".SF") ||
                name.endsWith(".DSA") ||
                name.endsWith(".RSA") ||
                SIG.matcher(name).matches()
        );
    }

    /**
     * Check if userCert is designed to be a code signer
     *
     * @param userCert the certificate to be examined
     */
    private void checkCertUsage(final CertPath certPath, final X509Certificate userCert) {

        // Can act as a signer?
        // 1. if KeyUsage, then [0] should be true
        // 2. if ExtendedKeyUsage, then should contains ANY or CODE_SIGNING
        // 3. if NetscapeCertType, then should contains OBJECT_SIGNING
        // 1,2,3 must be true

        final boolean[] keyUsage = userCert.getKeyUsage();
        if (keyUsage != null) {
            if (keyUsage.length < 1 || !keyUsage[0]) {
                certs.get(certPath).setBadKeyUsage();
            }
        }

        try {
            final List<String> xKeyUsage = userCert.getExtendedKeyUsage();
            if (xKeyUsage != null) {
                if (!xKeyUsage.contains("2.5.29.37.0") // anyExtendedKeyUsage
                        && !xKeyUsage.contains("1.3.6.1.5.5.7.3.3")) { // codeSigning
                    certs.get(certPath).setBadExtendedKeyUsage();
                }
            }
        } catch (java.security.cert.CertificateParsingException e) {
            // shouldn't happen
        }

        try {
            // OID_NETSCAPE_CERT_TYPE
            final byte[] netscapeEx = userCert.getExtensionValue("2.16.840.1.113730.1.1");
            if (netscapeEx != null) {
                final DerInputStream in = new DerInputStream(netscapeEx);
                final byte[] raw = in.getOctetString();
                final byte[] encoded = new DerValue(raw).getUnalignedBitString().toByteArray();

                final NetscapeCertTypeExtension extn = new NetscapeCertTypeExtension(encoded);

                if (!extn.get(NetscapeCertTypeExtension.OBJECT_SIGNING)) {
                    certs.get(certPath).setBadNetscapeCertType();
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
        return unverifiedJars.isEmpty();
    }

    public void checkTrustWithUser(final SecurityDelegate securityDelegate, final JNLPFile file) throws LaunchException {
        appVerifier.checkTrustWithUser(securityDelegate, this, file);
    }

    public Map<String, Integer> getJarSignableEntries() {
        return Collections.unmodifiableMap(jarSignableEntries);
    }

    /**
     * Get the total number of entries in the provided map.
     *
     * @param map map of all jars
     * @return The number of entries.
     */
    public static int getTotalJarEntries(final Map<String, Integer> map) {
        return map.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
