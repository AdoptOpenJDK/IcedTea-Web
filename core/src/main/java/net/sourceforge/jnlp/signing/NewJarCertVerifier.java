package net.sourceforge.jnlp.signing;

import java.io.File;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.sourceforge.jnlp.signing.SignVerifyUtils.determineCertificatesFullySigningThe;

public class NewJarCertVerifier {

    private final Map<File, CertificatesFullySigningTheJar> jarToFullySigningCertificates = new HashMap<>();

    public ApplicationSigningState getState() {
        final Set<ApplicationSigningState> states = jarToFullySigningCertificates.values().stream()
                .flatMap(r -> r.getCertificates().stream())
                .collect(Collectors.toSet()).stream()
                .map(this::getState)
                .collect(Collectors.toSet());

        if (states.contains(ApplicationSigningState.FULL)) {
            return ApplicationSigningState.FULL;
        }
        return states.contains(ApplicationSigningState.PARTIAL) ? ApplicationSigningState.PARTIAL : ApplicationSigningState.NONE;
    }

    private ApplicationSigningState getState(final Certificate certificate) {
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
