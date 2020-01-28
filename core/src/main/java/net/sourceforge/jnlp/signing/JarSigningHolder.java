package net.sourceforge.jnlp.signing;

import net.adoptopenjdk.icedteaweb.Assert;
import net.sourceforge.jnlp.tools.CertInformation;

import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JarSigningHolder {

    /**
     * defintion of Boolean value:
     * <p>
     * false -> partially signed with certificate
     * true -> fully signed with certificate
     * <p>
     * If there is no entry for a certificate that the resource is not signed by the certificate
     */
    private final Map<CertPath, Boolean> signStateForCertificates;

    private final SignVerifyResult signState;

    public JarSigningHolder(final String jarUrl, final Function<CertPath, CertInformation> certInfoProvider) {
        Assert.requireNonNull(jarUrl, "jarUrl");
        Assert.requireNonNull(certInfoProvider, "certInfoProvider");

        signStateForCertificates = SignVerifyUtils.getSignByMagic(jarUrl, certInfoProvider);

        signState = SignVerifyResult.SIGNED_NOT_OK; //TODO: By extracting getSignByMagic we currently can not set this...
    }

    public Set<Certificate> getCertificates() {
        Set<Certificate> calculated = getCertificatePaths().stream()
                .flatMap(certPath -> certPath.getCertificates().stream())
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(calculated);
    }

    public Set<CertPath> getCertificatePaths() {
        return Collections.unmodifiableSet(signStateForCertificates.keySet());
    }

    public SigningState getStateForPath(final CertPath certPath) {
        Assert.requireNonNull(certPath, "certPath");
        final Boolean signStateForCertPath = signStateForCertificates.get(certPath);
        if (signStateForCertPath == null) {
            return SigningState.NONE;
        }
        return signStateForCertPath ? SigningState.FULL : SigningState.PARTIAL;
    }

    public SigningState getState(final Certificate certificate) {
        Assert.requireNonNull(certificate, "certificate");

        Set<SigningState> states = getCertificatePaths().stream()
                .filter(certPath -> certPath.getCertificates().contains(certificate))
                .map(this::getStateForPath)
                .collect(Collectors.toSet());

        if (states.contains(SigningState.FULL)) {
            return SigningState.FULL;
        }
        if (states.contains(SigningState.PARTIAL)) {
            return SigningState.PARTIAL;
        }
        return SigningState.NONE;
    }
}
