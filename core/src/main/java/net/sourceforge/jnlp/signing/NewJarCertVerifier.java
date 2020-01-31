package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;

import java.io.File;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NewJarCertVerifier {

    private final List<JarSigningHolder> holders = new ArrayList<>();

    private ApplicationSigningState getState(final Certificate certificate) {
        final long numFullySignedResources = holders.stream()
                .filter(jarSigningHolder -> jarSigningHolder.isFullySignedBy(certificate))
                .count();

        if (numFullySignedResources == holders.size()) {
            return ApplicationSigningState.FULL;
        }

        return numFullySignedResources == 0 ? ApplicationSigningState.NONE : ApplicationSigningState.PARTIAL;
    }

    public ApplicationSigningState getState() {
        final Set<ApplicationSigningState> states = holders.stream()
                .flatMap(r -> r.getCertificates().stream())
                .map(this::getState)
                .collect(Collectors.toSet());

        if (states.contains(ApplicationSigningState.FULL)) {
            return ApplicationSigningState.FULL;
        }
        return states.contains(ApplicationSigningState.PARTIAL) ? ApplicationSigningState.PARTIAL : ApplicationSigningState.NONE;
    }

    public void add(final File jarFile) {
        final JarSigningHolder holder = SignVerifyUtils.getSignByMagic(jarFile);
        holders.add(holder);
    }

    public boolean allJarsSigned() {
        throw new RuntimeException("Not implemented yet!");
    }

    public boolean isFullySigned() {
        throw new RuntimeException("Not implemented yet!");
    }
}
