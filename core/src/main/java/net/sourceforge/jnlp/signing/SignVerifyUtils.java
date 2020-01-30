package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.tools.CertInformation;
import net.sourceforge.jnlp.util.JarFile;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.NetscapeCertTypeExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.MONTHS;

public class SignVerifyUtils {

    private static final String META_INF = "META-INF/";

    private static final Pattern SIG = Pattern.compile(".*" + META_INF + "SIG-.*");

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
     * Get the total number of entries in the provided map.
     *
     * @param map map of all jars
     * @return The number of entries.
     */
    static int getTotalJarEntries(final Map<String, Integer> map) {
        return map.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    static JarSigningHolder getSignByMagic(final File jarPath, final Function<CertPath, CertInformation> certInfoProvider) {
        Assert.requireNonNull(jarPath, "jarPath");
        Assert.requireNonNull(certInfoProvider, "certInfoProvider");

        final Set<CertPath> result = new HashSet<>();

        try (final JarFile jarFile = new JarFile(jarPath, true)) {
            final List<JarEntry> entries = new ArrayList<>();
            final byte[] buffer = new byte[8192];

            //CHECK: Read full Jar and see if a SecurityException happens

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

            //Now we handle the certs
            final Map<CertPath, Integer> jarSignCount = new HashMap<>();
            int numSignableEntriesInJar = 0;
            final boolean jarHasManifest = jarFile.getManifest() != null;

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

            // Find all signers that have signed every signable entry in this jar.
            for (final CertPath certPath : jarSignCount.keySet()) {
                boolean fullySignedByCert = jarSignCount.get(certPath) == numSignableEntriesInJar;
                final CertInformation certInfo = certInfoProvider.apply(certPath);
                certInfo.resetForReverification();
                certInfo.setNumJarEntriesSigned(jarPath.toString(), numSignableEntriesInJar);

                final Certificate cert = certPath.getCertificates().get(0);
                if (cert instanceof X509Certificate) {
                    checkCertUsage((X509Certificate) cert, certInfo);
                    checkExpiration((X509Certificate) cert, now, certInfo);
                }
                if (fullySignedByCert) {
                    result.add(certPath);
                }
            }

            return new JarSigningHolder(result);
        } catch (Exception e) {
            throw new RuntimeException("Error in verify jar " + jarPath, e);
        }
    }

    private static void checkExpiration(final X509Certificate cert, final ZonedDateTime now, final CertInformation certInfo) {
        final ZonedDateTime notBefore = zonedDateTime(cert.getNotBefore());
        final ZonedDateTime notAfter = zonedDateTime(cert.getNotAfter());
        if (now.isBefore(notBefore)) {
            certInfo.setNotYetValidCert();
        }
        if (now.isAfter(notAfter)) {
            certInfo.setHasExpiredCert();
        } else if (now.plus(6, MONTHS).isAfter(notAfter)) {
            certInfo.setHasExpiringCert();
        }
    }

    private static ZonedDateTime zonedDateTime(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

    private static void checkCertUsage(final X509Certificate userCert, final CertInformation certInformation) {

        // Can act as a signer?
        // 1. if KeyUsage, then [0] should be true
        // 2. if ExtendedKeyUsage, then should contains ANY or CODE_SIGNING
        // 3. if NetscapeCertType, then should contains OBJECT_SIGNING
        // 1,2,3 must be true

        final boolean[] keyUsage = userCert.getKeyUsage();
        if (keyUsage != null) {
            if (keyUsage.length < 1 || !keyUsage[0]) {
                certInformation.setBadKeyUsage();
            }
        }

        try {
            final List<String> xKeyUsage = userCert.getExtendedKeyUsage();
            if (xKeyUsage != null) {
                if (!xKeyUsage.contains("2.5.29.37.0") // anyExtendedKeyUsage
                        && !xKeyUsage.contains("1.3.6.1.5.5.7.3.3")) { // codeSigning
                    certInformation.setBadExtendedKeyUsage();
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
                    certInformation.setBadNetscapeCertType();
                }
            }
        } catch (IOException e) {
            //
        }
    }

    static ApplicationSigningState mergeSigningState(final ApplicationSigningState state1, final ApplicationSigningState state2) {
        if (state1 == ApplicationSigningState.FULL && state2 == ApplicationSigningState.FULL) {
            return ApplicationSigningState.FULL;
        }
        if (state1 == ApplicationSigningState.NONE && state2 == ApplicationSigningState.NONE) {
            return ApplicationSigningState.NONE;
        }
        return ApplicationSigningState.PARTIAL;
    }
}
