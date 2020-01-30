package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.Assert;

import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class JarSigningHolder {

    private final Set<CertPath> fullySigningCertificates;

    public JarSigningHolder(Set<CertPath> fullySigningCertificates) {
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

    public boolean isFullySignedBy(final CertPath certPath) {
        Assert.requireNonNull(certPath, "certPath");
        return fullySigningCertificates.contains(certPath);
    }

    public boolean isFullySignedBy(final Certificate certificate) {
        Assert.requireNonNull(certificate, "certificate");
        return getCertificates().contains(certificate);
    }
}
