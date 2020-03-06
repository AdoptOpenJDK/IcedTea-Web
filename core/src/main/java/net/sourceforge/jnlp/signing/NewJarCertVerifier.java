package net.sourceforge.jnlp.signing;

import java.io.File;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.signing.SignVerifyUtils.determineCertificatesFullySigningThe;

/**
 * All jars and their fully signing certificates which have been loaded for the application.
 */
public class NewJarCertVerifier {

    private final Map<File, CertificatesFullySigningTheJar> jarToFullySigningCertificates = new HashMap<>();

    public Map<CertPath, ApplicationSigningState> verify() {
        return jarToFullySigningCertificates.values().stream()
                .flatMap(r -> r.getCertificatePaths().stream())
                .collect(Collectors.toSet()).stream()
                .collect(Collectors.toMap(Function.identity(), this::getStateForSingleCertificate));
    }

    private ApplicationSigningState getStateForSingleCertificate(final CertPath certPath) {
        final Certificate certificate = certPath.getCertificates().get(0);
        final long numFullySignedJars = jarToFullySigningCertificates.values().stream()
                .filter(certs -> certs.contains(certificate))
                .count();

        if (numFullySignedJars == jarToFullySigningCertificates.size()) {
            return ApplicationSigningState.FULL;
        }

        return numFullySignedJars == 0 ? ApplicationSigningState.NONE : ApplicationSigningState.PARTIAL;
    }

    public void addAll(final List<File> jars) {
        for (File jarFile : jars) {
            add(jarFile);
        }
    }

    public void add(final File jarFile) {
        final CertificatesFullySigningTheJar certificatesFullySigningTheJar = determineCertificatesFullySigningThe(jarFile);
        jarToFullySigningCertificates.put(jarFile, certificatesFullySigningTheJar);
    }

    public boolean isNotFullySigned() {
        return getFullySigningCertificates().isEmpty();
    }

    public Set<CertPath> getFullySigningCertificates() {
        return verify().entrySet().stream()
                .filter(e -> e.getValue() == ApplicationSigningState.FULL)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public CertificatesFullySigningTheJar certificatesSigning(final File jarFile) {
        return jarToFullySigningCertificates.get(jarFile);
    }
}
