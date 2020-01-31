package net.sourceforge.jnlp.signing;

import java.io.File;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.signing.SignVerifyUtils.determineCertificatesFullySigningThe;

/**
 * All jars and their fully signing certificates which have been loaded for the application.
 */
public class NewJarCertVerifier {

    private final Map<File, CertificatesFullySigningTheJar> jarToFullySigningCertificates = new HashMap<>();

    public Map<Certificate, ApplicationSigningState> getState() {
        return jarToFullySigningCertificates.values().stream()
                .flatMap(r -> r.getCertificates().stream())
                .collect(Collectors.toSet()).stream()
                .collect(Collectors.toMap(Function.identity(), this::getStateForSingleCertificate));
    }

    private ApplicationSigningState getStateForSingleCertificate(final Certificate certificate) {
        final long numFullySignedJars = jarToFullySigningCertificates.values().stream()
                .filter(certs -> certs.contains(certificate))
                .count();

        if (numFullySignedJars == jarToFullySigningCertificates.size()) {
            return ApplicationSigningState.FULL;
        }

        return numFullySignedJars == 0 ? ApplicationSigningState.NONE : ApplicationSigningState.PARTIAL;
    }

    public void add(final File jarFile) {
        final CertificatesFullySigningTheJar certificatesFullySigningTheJar = determineCertificatesFullySigningThe(jarFile);
        jarToFullySigningCertificates.put(jarFile, certificatesFullySigningTheJar);
    }

    public boolean allJarsSigned() {
        throw new RuntimeException("Not implemented yet!");
    }

    public boolean isFullySigned() {
        throw new RuntimeException("Not implemented yet!");
    }
}
