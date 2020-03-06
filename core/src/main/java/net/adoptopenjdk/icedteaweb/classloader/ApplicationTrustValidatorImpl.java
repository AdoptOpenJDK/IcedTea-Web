package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.RememberingSecurityUserInteractions;
import net.adoptopenjdk.icedteaweb.security.SecurityUserInteractions;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.ApplicationManager;
import net.sourceforge.jnlp.runtime.ApplicationPermissions;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.runtime.SecurityDelegateNew;
import net.sourceforge.jnlp.signing.NewJarCertVerifier;
import net.sourceforge.jnlp.signing.SignVerifyUtils;
import net.sourceforge.jnlp.tools.CertInformation;

import java.io.File;
import java.net.URISyntaxException;
import java.security.cert.CertPath;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.SANDBOX;

/**
 * See {@link ApplicationTrustValidator}.
 */
public class ApplicationTrustValidatorImpl implements ApplicationTrustValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationTrustValidatorImpl.class);

    private final SecurityUserInteractions userInteractions;
    private final SecurityDelegate securityDelegate;

    private final NewJarCertVerifier certVerifier;
    private final JNLPFile file;


    public ApplicationTrustValidatorImpl(final JNLPFile file, final ApplicationPermissions applicationPermissions) {
        this(file, applicationPermissions, new RememberingSecurityUserInteractions());
    }

    public ApplicationTrustValidatorImpl(final JNLPFile file, final ApplicationPermissions applicationPermissions, final SecurityUserInteractions userInteractions) {
        this.file = file;
        this.certVerifier = new NewJarCertVerifier();
        this.securityDelegate = new SecurityDelegateNew(applicationPermissions, file, certVerifier);
        this.userInteractions = userInteractions;
    }

    /**
     * Use the certVerifier to find certificates which sign all jars
     *
     * <pre>
     * Eager jars added:
     * - no certificate which signs all jars
     *      -> if main jar is signed by a certificate
     *          -> find the certificate with the least (or no) problems (remember this decision)
     *          -> if certificate has problems ask user to trust certificate
     *          -> check if the JNLP file is signed
     *      -> ask user for permission to run unsigned application
     * - one or more certificates which sign all jars
     *      -> find the certificate with the least (or no) problems (remember this decision)
     *      -> if certificate has problems ask user to trust certificate
     *      -> check if the JNLP file is signed
     *
     *
     * Lazy Jar:
     * - new jar is unsigned os signed by a certificate which does not sign all other jars
     *      -> ask user for permission to run unsigned application
     * - new jar is signed by the remembered certificate
     *      -> OK
     * - new jar is signed by a certificate which also signs all other jars and has no issues
     *      -> change remembered decision
     * - new jar is signed by a certificate which also signs all other jars and has issues
     *      -> ask user to trust certificate -> change remembered decision
     * </pre>
     *
     * @param jars the new jars to add.
     */
    @Override
    public void validateJars(List<LoadableJar> jars) {

        final ApplicationInstance applicationInstance = ApplicationManager.getApplication(file)
                .orElseThrow(() -> new IllegalStateException("No ApplicationInstance found for " + file.getTitleFromJnlp()));
        if (applicationInstance.getApplicationEnvironment() == SANDBOX) {
            return;
        }

        try {
            certVerifier.addAll(toFiles(jars));

            if (certVerifier.isNotFullySigned()) {
                if (userInteractions.askUserForPermissionToRunUnsignedApplication(file) != AllowDeny.ALLOW) {
                    // TODO: add details to exception
                    throw new LaunchException("");
                }
            } else {
                final ZonedDateTime now = ZonedDateTime.now();
                final Set<CertPath> fullySigningCertificates = certVerifier.getFullySigningCertificates();
                final Map<CertPath, CertInformation> certInfos = fullySigningCertificates.stream()
                        .collect(Collectors.toMap(Function.identity(), certPath -> SignVerifyUtils.calculateCertInformationFor(certPath, now)));

                final boolean hasTrustedFullySigningCertificate = certInfos.values().stream()
                        .filter(infos -> infos.isRootInCacerts() || infos.isPublisherAlreadyTrusted())
                        .anyMatch(infos -> !infos.hasSigningIssues());

                if (hasTrustedFullySigningCertificate) {
                    return;
                }

                // find certPath with best info - what is best info? - no issues, trusted RootCA

            }

//                if (!certVerifier.isFullySigned() && !certVerifier.getAlreadyTrustPublisher()) {
//                    certVerifier.checkTrustWithUser(securityDelegate, file);
//                }
        } catch (LaunchException e) {
            // TODO: LaunchException should not be wrapped in a RuntimeException
            throw new RuntimeException(e);
        }
    }

    private static List<File> toFiles(List<LoadableJar> jars) {
        return jars.stream()
                .map(loadableJar -> {
                    try {
                        return new File(loadableJar.getLocation().toURI());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
