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
     *
     * false -> parcially signed with certificate
     * true -> fully signed with certificate
     *
     * If there is no entry for a certificate that the resource is not signed by the certificate
     */
    private final Map<CertPath, Boolean> signStateForCertificats;

    private final SignVerifyResult signState;

    public JarSigningHolder(final String jarUrl, final Function<CertPath, CertInformation> certInfoProvider) {
        Assert.requireNonNull(jarUrl, "jarUrl");
        Assert.requireNonNull(certInfoProvider, "certInfoProvider");

        signStateForCertificats = SignVerifyUtils.getSignByMagic(jarUrl, certInfoProvider);

        signState = SignVerifyResult.SIGNED_NOT_OK; //TODO: By extracting getSignByMagic we currently can not set this...
    }

    public Set<Certificate> getCertificates() {
        Set<Certificate> calculated = getCertificatePaths().stream()
                .flatMap(certPath -> certPath.getCertificates().stream())
                .collect(Collectors.toSet());
        return Collections.unmodifiableSet(calculated);
    }

    public Set<CertPath> getCertificatePaths() {
        return Collections.unmodifiableSet(signStateForCertificats.keySet());
    }

    public SigningState getStateForPath(final CertPath certPath) {
        Assert.requireNonNull(certPath, "certPath");
        final Boolean signState = signStateForCertificats.get(certPath);
        if(signState == null) {
            return SigningState.NONE;
        }
        if(signState == false) {
            return SigningState.PARTIAL;
        }
        return SigningState.FULL;
    }

    public SigningState getState(final Certificate certificate) {
        Assert.requireNonNull(certificate, "certificate");

        return getCertificatePaths().stream()
                .filter(certPath -> certPath.getCertificates().contains(certificate))
                .findAny()
                .map(certPath -> getStateForPath(certPath))
                .orElse(SigningState.NONE);
    }

}
