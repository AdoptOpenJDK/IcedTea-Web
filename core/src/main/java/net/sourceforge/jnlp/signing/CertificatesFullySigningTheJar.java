package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.Assert;

import java.io.File;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the set of all {@link CertPath} which fully sign a single jar file.
 */
public class CertificatesFullySigningTheJar {

    private final File jarFile;
    private final Set<CertPath> fullySigningCertificates;

    public CertificatesFullySigningTheJar(File jarFile, Set<CertPath> fullySigningCertificates) {
        this.jarFile = jarFile;
        this.fullySigningCertificates = fullySigningCertificates;
    }

    public Set<Certificate> getCertificates() {
        Set<Certificate> calculated = getCertificatePaths().stream()
                .map(certPath -> certPath.getCertificates().get(0))
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(calculated);
    }

    public Set<CertPath> getCertificatePaths() {
        return Collections.unmodifiableSet(fullySigningCertificates);
    }

    public boolean contains(final CertPath certPath) {
        Assert.requireNonNull(certPath, "certPath");
        return fullySigningCertificates.contains(certPath);
    }

    public boolean contains(final Certificate certificate) {
        Assert.requireNonNull(certificate, "certificate");
        return getCertificates().contains(certificate);
    }
}
