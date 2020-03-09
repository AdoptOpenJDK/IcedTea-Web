package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;
import net.adoptopenjdk.icedteaweb.security.RememberingSecurityUserInteractions;
import net.adoptopenjdk.icedteaweb.security.SecurityUserInteractions;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.JNLPMatcher;
import net.sourceforge.jnlp.LaunchException;
import net.sourceforge.jnlp.runtime.ApplicationInstance;
import net.sourceforge.jnlp.runtime.ApplicationManager;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.signing.CertificatesFullySigningTheJar;
import net.sourceforge.jnlp.signing.NewJarCertVerifier;
import net.sourceforge.jnlp.signing.SignVerifyUtils;
import net.sourceforge.jnlp.tools.CertInformation;

import java.io.File;
import java.net.URISyntaxException;
import java.security.cert.CertPath;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.adoptopenjdk.icedteaweb.jnlp.element.security.ApplicationEnvironment.SANDBOX;
import static net.sourceforge.jnlp.util.UrlUtils.FILE_PROTOCOL;

/**
 * See {@link ApplicationTrustValidator}.
 */
public class ApplicationTrustValidatorImpl implements ApplicationTrustValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationTrustValidatorImpl.class);

    private final SecurityUserInteractions userInteractions;

    private final NewJarCertVerifier certVerifier;
    private final JNLPFile file;


    public ApplicationTrustValidatorImpl(final JNLPFile file) {
        this(file, new RememberingSecurityUserInteractions());
    }

    public ApplicationTrustValidatorImpl(final JNLPFile file, final SecurityUserInteractions userInteractions) {
        this.file = file;
        this.certVerifier = new NewJarCertVerifier();
        this.userInteractions = userInteractions;
    }

    /**
     * Use the certVerifier to find certificates which sign all jars
     *
     * <pre>
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
     * </pre>
     *
     * @param jars the new jars to add.
     */
    @Override
    public void validateEagerJars(List<LoadableJar> jars) {

        final ApplicationInstance applicationInstance = ApplicationManager.getApplication(file)
                .orElseThrow(() -> new IllegalStateException("No ApplicationInstance found for " + file.getTitleFromJnlp()));
        if (applicationInstance.getApplicationEnvironment() == SANDBOX) {
            return;
        }

        try {
            certVerifier.addAll(toFiles(jars));

            if (isJnlpSigned(jars, file)) {
                file.markFileAsSigned();
            }

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
                CertPath certPath = findBestCertificate(certInfos);
                final AllowDenySandbox result = userInteractions.askUserHowToRunApplicationWithCertIssues(file, certPath, certInfos.get(certPath));

                switch (result) {
                    case SANDBOX:
                        applicationInstance.setApplicationEnvironment(SANDBOX);
                    case ALLOW:
                        return;
                    default:
                        throw new LaunchException("User canceled launch of application when asked to how to run application with certificate issues");
                }
            }

        }
        catch (LaunchException e) {
            LOG.info("Abort application launch - {}", e.getMessage());
            JNLPRuntime.exit(0);
        }
    }

    private CertPath findBestCertificate(final Map<CertPath, CertInformation> certInfos) {
        // TODO: improve implementation to find best
        return certInfos.keySet().iterator().next();
    }


    /**
     * Use the certVerifier to find certificates which sign all jars
     *
     * <pre>
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
    public void validateLazyJars(List<LoadableJar> jars) {
        throw new RuntimeException("Not implemented yet!");
    }

    private static List<File> toFiles(List<LoadableJar> jars) {
        return jars.stream()
                .map(ApplicationTrustValidatorImpl::toFile)
                .collect(Collectors.toList());
    }

    private static File toFile(LoadableJar loadableJar) {
        try {
            return new File(loadableJar.getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isJnlpSigned(final List<LoadableJar> jars, final JNLPFile file) {
        final JARDesc mainJarDesc = file.getResources().getMainJAR();

        if (mainJarDesc == null) {
            return false;
        }

        final LoadableJar mainJar = jars.stream()
                .filter(jar -> Objects.equals(jar.getJarDesc(), mainJarDesc))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Main jar not found"));
        final File mainJarFile = toFile(mainJar);

        final ZonedDateTime now = ZonedDateTime.now();
        final CertificatesFullySigningTheJar certs = certVerifier.certificatesSigning(mainJarFile);

        final Map<CertPath, CertInformation> certInfos = certs.getCertificatePaths().stream()
                .collect(Collectors.toMap(Function.identity(), certPath -> SignVerifyUtils.calculateCertInformationFor(certPath, now)));

        final boolean hasTrustedCertificate = certInfos.values().stream()
                .filter(infos -> infos.isRootInCacerts() || infos.isPublisherAlreadyTrusted())
                .anyMatch(infos -> !infos.hasSigningIssues());

        if (!hasTrustedCertificate) {
            return false;
        }

        return isJnlpSigned(file, mainJarFile);
    }

    private static boolean isJnlpSigned(final JNLPFile file, final File mainJarFile) {
        return new JNLPMatcher(mainJarFile, getLocalFile(file), file.getParserSettings()).isMatch();
    }

    private static File getLocalFile(final JNLPFile file) {
        // If the file is on the local file system, use original path, otherwise find cached file
        if (file.getFileLocation().getProtocol().toLowerCase().equals(FILE_PROTOCOL)) {
            return new File(file.getFileLocation().getPath());
        } else {
            return Cache.getCacheFile(file.getFileLocation(), file.getFileVersion());
        }
    }
}
