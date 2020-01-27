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

package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.Dialogs;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.CertificateUtils;
import net.sourceforge.jnlp.security.KeyStores;
import net.sourceforge.jnlp.tools.CertInformation;
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
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MONTHS;
import static net.sourceforge.jnlp.LaunchException.FATAL;

/**
 * The jar certificate verifier utility.
 *
 * @author Roland Schemers
 * @author Jan Luehe
 */

@Deprecated
public class JarCertVerifier implements CertVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(JarCertVerifier.class);

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
     * Temporary cert path hack to be used to keep track of which one a UI dialog is using
     */
    private CertPath currentlyUsed;












    private SigningState getState(final Certificate certificate) {
        final List<JarSigningHolder> allResources = getAllResources();

        return allResources.stream()
                .map(resource -> resource.getState(certificate))
                .reduce((state1, state2) -> SignVerifyUtils.mergeSigningState(state1, state2))
                .orElse(SigningState.NONE); // What is the correct state if we do not have any resources????
    }

    public SigningState getState() {
        final List<JarSigningHolder> allResources = getAllResources();

        final Set<Certificate> certificates = allResources.stream()
                .flatMap(r -> r.getCertificates().stream())
                .collect(Collectors.toSet());

        return certificates.stream()
                .map(certificate -> getState(certificate))
                .reduce((state1, state2) -> SignVerifyUtils.mergeSigningState(state1, state2))
                .orElse(SigningState.NONE); // What is the correct state if we do not have any certificates????
    }

    public List<JarSigningHolder> getAllResources() {
        return null;
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
        checkTrustWithUser(securityDelegate, this, file);
    }

    @Override
    public boolean getAlreadyTrustPublisher() {
        final boolean allPublishersTrusted = hasAlreadyTrustedPublisher(certs, jarSignableEntries);
        LOG.debug("App already has trusted publisher: {}", allPublishersTrusted);
        return allPublishersTrusted;
    }

    @Override
    public boolean getRootInCaCerts() {
        final boolean allRootCAsTrusted = hasRootInCacerts(certs, jarSignableEntries);
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
    public boolean isFullySigned() {
        return isTriviallySigned() || isSigned();
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
     * Get a list of the cert paths of all signers across the app.
     *
     * @return List of CertPath vars representing each of the signers present on any jar.
     */
    List<CertPath> getCertsList() {
        return new ArrayList<>(certs.keySet());
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
    SignVerifyResult verifyJarEntryCerts(final String jarPath, final boolean jarHasManifest, final List<JarEntry> entries) {
        // Contains number of entries the cert with this CertPath has signed.
        final Map<CertPath, Integer> jarSignCount = new HashMap<>();
        int numSignableEntriesInJar = 0;

        // Record current time just before checking the jar begins.
        final ZonedDateTime now = ZonedDateTime.now();
        if (jarHasManifest) {
            for (JarEntry je : entries) {
                final boolean shouldHaveSignature = !je.isDirectory() && !SignVerifyUtils.isMetaInfFile(je.getName());
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
        final SignVerifyResult result;
        if (numSignableEntriesInJar == 0) {
            // Allow jars with no signable entries to simply be considered signed.
            // There should be no security risk in doing so.
            result = SignVerifyResult.SIGNED_OK;
        } else if (allEntriesSignedBySingleCert) {
            // We need to find at least one signer without any issues.
            result = verifySigners(jarSignCount);
        } else {
            result = SignVerifyResult.UNSIGNED;
        }

        LOG.debug("Jar found at {} has been verified as {}", jarPath, result);
        return result;
    }


    /**
     * @return true if there are no signable entries in the jar.
     * This will return false if any of verified jars have content more than just META-INF/.
     */
    private boolean isTriviallySigned() {
        return SignVerifyUtils.getTotalJarEntries(jarSignableEntries) <= 0 && certs.size() <= 0;
    }

    private boolean isSigned() {
        final boolean fullySigned = isFullySigned(certs, jarSignableEntries);
        LOG.debug("App already has trusted publisher: {}", fullySigned);
        return fullySigned;
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

            final SignVerifyResult result = verifyJar(jarPath);
            if (result == SignVerifyResult.UNSIGNED) {
                unverifiedJars.add(jarPath);
            } else if (result == SignVerifyResult.SIGNED_NOT_OK) {
                verifiedJars.add(jarPath);
            } else if (result == SignVerifyResult.SIGNED_OK) {
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
    private SignVerifyResult verifyJar(final String jarPath) {
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

    private SignVerifyResult verifySigners(final Map<CertPath, Integer> jarSignCount) {
        for (CertPath entryCertPath : jarSignCount.keySet()) {
            if (certs.containsKey(entryCertPath) && !certs.get(entryCertPath).hasSigningIssues()) {
                return SignVerifyResult.SIGNED_OK;
            }
        }
        // All signers had issues
        return SignVerifyResult.SIGNED_NOT_OK;
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
            LOG.warn("Unable to read through cert store files.");
            throw e;
        }

        // Otherwise a parent cert was not found to be trusted.
        info.setUntrusted();
    }

    private void setCurrentlyUsedCertPath(final CertPath certPath) {
        currentlyUsed = certPath;
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

    private Map<String, Integer> getJarSignableEntries() {
        return Collections.unmodifiableMap(jarSignableEntries);
    }

    /**
     * Checks if the app has already found trust in its publisher(s).
     *
     * @param certs      The certs to search through and their cert information
     * @param signedJars A map of all the jars of this app and the number of
     *                   signed entries each one has.
     * @return True if the app trusts its publishers.
     */
    private boolean hasAlreadyTrustedPublisher(
            Map<CertPath, CertInformation> certs,
            Map<String, Integer> signedJars) {
        int sumOfSignableEntries = SignVerifyUtils.getTotalJarEntries(signedJars);
        for (CertInformation certInfo : certs.values()) {
            Map<String, Integer> certSignedJars = certInfo.getSignedJars();

            if (SignVerifyUtils.getTotalJarEntries(certSignedJars) == sumOfSignableEntries
                    && certInfo.isPublisherAlreadyTrusted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the app has signer(s) whose certs along their chains are in CA certs.
     *
     * @param certs      The certs to search through and their cert information
     * @param signedJars A map of all the jars of this app and the number of
     *                   signed entries each one has.
     * @return True if the app has a root in the CA certs store.
     */
    private boolean hasRootInCacerts(Map<CertPath, CertInformation> certs,
                                     Map<String, Integer> signedJars) {
        int sumOfSignableEntries = SignVerifyUtils.getTotalJarEntries(signedJars);
        for (CertInformation certInfo : certs.values()) {
            Map<String, Integer> certSignedJars = certInfo.getSignedJars();

            if (SignVerifyUtils.getTotalJarEntries(certSignedJars) == sumOfSignableEntries
                    && certInfo.isRootInCacerts()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the app's jars are covered by the provided certificates, enough
     * to consider the app fully signed.
     *
     * @param certs      Any possible signer and their respective information regarding this app.
     * @param signedJars A map of all the jars of this app and the number of
     *                   signed entries each one has.
     * @return true if jar is fully signed
     */
    private boolean isFullySigned(Map<CertPath, CertInformation> certs,
                                  Map<String, Integer> signedJars) {
        int sumOfSignableEntries = SignVerifyUtils.getTotalJarEntries(signedJars);
        for (CertPath cPath : certs.keySet()) {
            // If this cert has signed everything, return true
            if (hasCompletelySignedApp(certs.get(cPath), sumOfSignableEntries)) {
                return true;
            }
        }

        // No cert found that signed all entries. Return false.
        return false;
    }

    /**
     * Prompt the user with requests for trusting the certificates used by this app
     *
     * @param securityDelegate parental security
     * @param jcv              jar verifier
     * @param file             jnlp file to provide information
     * @throws LaunchException if it fails to verify
     */
    private void checkTrustWithUser(SecurityDelegate securityDelegate, JarCertVerifier jcv, JNLPFile file)
            throws LaunchException {

        int sumOfSignableEntries = SignVerifyUtils.getTotalJarEntries(jcv.getJarSignableEntries());
        for (CertPath cPath : jcv.getCertsList()) {
            jcv.setCurrentlyUsedCertPath(cPath);
            CertInformation info = jcv.getCertInformation(cPath);
            if (hasCompletelySignedApp(info, sumOfSignableEntries)) {
                if (info.isPublisherAlreadyTrusted()) {
                    return;
                }

                AccessType dialogType;
                if (info.isRootInCacerts() && !info.hasSigningIssues()) {
                    dialogType = AccessType.VERIFIED;
                } else if (info.isRootInCacerts()) {
                    dialogType = AccessType.SIGNING_ERROR;
                } else {
                    dialogType = AccessType.UNVERIFIED;
                }

                YesNoSandbox action = Dialogs.showCertWarningDialog(
                        dialogType, file, jcv, securityDelegate);
                if (action != null && action.toBoolean()) {
                    if (action.compareValue(Primitive.SANDBOX)) {
                        securityDelegate.setRunInSandbox();
                    }
                    return;
                }
            }
        }

        throw new LaunchException(null, null, FATAL, "Launch Error",
                "Cancelled on user request.", "");
    }

    /**
     * Find out if the CertPath with the given info has fully signed the app.
     *
     * @param info                 The information regarding the CertPath in question
     * @param sumOfSignableEntries The total number of signable entries in the app.
     * @return True if the signer has fully signed this app.
     */
    private boolean hasCompletelySignedApp(CertInformation info, int sumOfSignableEntries) {
        return SignVerifyUtils.getTotalJarEntries(info.getSignedJars()) == sumOfSignableEntries;
    }
}
