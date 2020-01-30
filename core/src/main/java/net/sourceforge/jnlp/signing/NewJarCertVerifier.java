package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.resources.ResourceTracker;
import net.sourceforge.jnlp.tools.CertInformation;

import java.io.File;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NewJarCertVerifier {

    private final List<JarSigningHolder> holders = new ArrayList<>();

    private final Map<CertPath, CertInformation> certInfoMap = new HashMap<>();

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
        final Set<Certificate> certificates = holders.stream()
                .flatMap(r -> r.getCertificates().stream())
                .collect(Collectors.toSet());

        return certificates.stream()
                .map(certificate -> getState(certificate))
                .reduce((state1, state2) -> SignVerifyUtils.mergeSigningState(state1, state2))
                .orElse(ApplicationSigningState.NONE); // What is the correct state if we do not have any certificates????
    }

    @Deprecated
    public void add(final JARDesc jar, final ResourceTracker tracker) throws Exception {
        final File jarFile = tracker.getCacheFile(jar.getLocation());
        add(jarFile);
    }

    public void add(final File jarFile) {
        final JarSigningHolder holder = SignVerifyUtils.getSignByMagic(jarFile);
        holders.add(holder);
    }
}
