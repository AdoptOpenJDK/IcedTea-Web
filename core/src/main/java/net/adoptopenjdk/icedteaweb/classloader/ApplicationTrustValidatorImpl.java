package net.adoptopenjdk.icedteaweb.classloader;

import net.adoptopenjdk.icedteaweb.classloader.JnlpApplicationClassLoader.LoadableJar;
import net.adoptopenjdk.icedteaweb.jnlp.element.resource.JARDesc;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesChecker;
import net.adoptopenjdk.icedteaweb.manifest.ManifestAttributesReader;
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
import java.util.Optional;
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

    private final ZonedDateTime now = ZonedDateTime.now();
    private final SecurityUserInteractions userInteractions;

    private final NewJarCertVerifier certVerifier;
    private final JNLPFile jnlpFile;

    private boolean allowUnsignedApplicationToRun = false; // set to true if the user accepts to run unsigned application
    private CertPath trustedCertificate = null; // certificate which was confirmed by the user


    public ApplicationTrustValidatorImpl(final JNLPFile jnlpFile) {
        this(jnlpFile, new RememberingSecurityUserInteractions());
    }

    public ApplicationTrustValidatorImpl(final JNLPFile jnlpFile, final SecurityUserInteractions userInteractions) {
        this.jnlpFile = jnlpFile;
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
        try {
            validateJars(jars, true);

        } catch (LaunchException e) {
            LOG.error("Abort application launch - {}", e.getMessage());
            JNLPRuntime.getDefaultLaunchHandler().handleLaunchError(e);
            JNLPRuntime.exit(-1);
        }
    }


    /**
     * Use the certVerifier to find certificates which sign all jars
     *
     * <pre>
     * - new jar is unsigned or signed by a certificate which does not sign all other jars
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
        try {
            validateJars(jars, false);
        } catch (LaunchException e) {
            LOG.info("Abort application - {}", e.getMessage());
            JNLPRuntime.exit(0);
        }
    }

    private void validateJars(List<LoadableJar> jars, boolean mustContainMainJar) throws LaunchException {
        final ApplicationInstance applicationInstance = ApplicationManager.getApplication(jnlpFile)
                .orElseThrow(() -> new IllegalStateException("No ApplicationInstance found for " + jnlpFile.getTitleFromJnlp()));

        if (applicationInstance.getApplicationEnvironment() == SANDBOX || allowUnsignedApplicationToRun) {
            return;
        }

        certVerifier.addAll(toFiles(jars));

        File mainJarFile = null;
        if (mustContainMainJar) {
            mainJarFile = getMainJarFile(jars, jnlpFile);
            markJnlpAsSignedIfContainedInMainJarAndMainJarIsSignedByTrustedCertificate(jnlpFile, mainJarFile);
        }

        if (certVerifier.isNotFullySigned()) {
            if (userInteractions.askUserForPermissionToRunUnsignedApplication(jnlpFile) != AllowDeny.ALLOW) {
                throw new LaunchException("User declined running unsigned application");
            }
            allowUnsignedApplicationToRun = true;
        } else {
            if (!hasTrustedCertificate(certVerifier.getFullySigningCertificates())) {
                final Map<CertPath, CertInformation> certInfos = calculateCertInfo(certVerifier.getFullySigningCertificates());

                for (final Map.Entry<CertPath, CertInformation> entry : certInfos.entrySet()) {
                    final AllowDenySandbox result = userInteractions.askUserHowToRunApplicationWithCertIssues(jnlpFile, entry.getKey(), entry.getValue());

                    if (result == AllowDenySandbox.SANDBOX) {
                        applicationInstance.setApplicationEnvironment(SANDBOX);
                        return;
                    } else if (result == AllowDenySandbox.ALLOW) {
                        trustedCertificate = entry.getKey();
                        if (mustContainMainJar) {
                            markJnlpAsSignedIfContainedInMainJarAndMainJarIsSignedByTrustedCertificate(jnlpFile, mainJarFile);
                        }
                        break;
                    }
                }

                if (!hasTrustedCertificate(certVerifier.getFullySigningCertificates())) {
                    throw new LaunchException("User exited application when asked to how to run application with certificate issues");
                }
            }
        }

        if (mustContainMainJar) {
            new ManifestAttributesChecker(jnlpFile, !certVerifier.isNotFullySigned(), new ManifestAttributesReader(mainJarFile)).checkAll();
        }
    }

    private static List<File> toFiles(List<LoadableJar> jars) {
        return jars.stream()
                .map(ApplicationTrustValidatorImpl::toFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<File> toFile(LoadableJar loadableJar) {
        return loadableJar.getLocation()
                .map(location -> {
                    try {
                        return new File(location.toURI());
                    } catch (URISyntaxException e) {
                        LOG.warn("URISyntaxException for url '{}'", location);
                        return null;
                    }
                });
    }


    private File getMainJarFile(List<LoadableJar> jars, JNLPFile file) throws LaunchException {
        final JARDesc mainJarDesc = file.getResources().getMainJAR();

        if (mainJarDesc == null) {
            throw new LaunchException("no main jar defined");
        }

        final LoadableJar mainJar = jars.stream()
                .filter(jar -> Objects.equals(jar.getJarDesc(), mainJarDesc))
                .findFirst()
                .orElseThrow(() -> {
                    LOG.debug("Main jar {} not found in {}", mainJarDesc, jars);
                    return new LaunchException("Could not find main jar among the eager jars");
                });
        return toFile(mainJar).orElseThrow(() -> new LaunchException("Could not find/download main jar file."));
    }

    private void markJnlpAsSignedIfContainedInMainJarAndMainJarIsSignedByTrustedCertificate(final JNLPFile file, final File mainJarFile) {
        if (file.isUnsigend() && isJarSignedByTrustedCertificate(mainJarFile)) {
            final JNLPMatcher jnlpMatcher = new JNLPMatcher(mainJarFile, getLocalFile(file), file.getParserSettings());
            if (jnlpMatcher.isMatch()) {
                file.markFileAsSigned();
            }
        }
    }

    private boolean isJarSignedByTrustedCertificate(final File jarFile) {
        final Set<CertPath> certPaths = certVerifier.certificatesSigning(jarFile).getCertificatePaths();
        return hasTrustedCertificate(certPaths);
    }

    private boolean hasTrustedCertificate(final Set<CertPath> certPaths) {

        final Map<CertPath, CertInformation> certInfos = calculateCertInfo(certPaths);

        final boolean hasTrustedCertificate = certInfos.values().stream()
                .filter(infos -> infos.isRootInCacerts() || infos.isPublisherAlreadyTrusted())
                .anyMatch(infos -> !infos.hasSigningIssues());

        return hasTrustedCertificate || certInfos.containsKey(trustedCertificate);
    }

    private Map<CertPath, CertInformation> calculateCertInfo(Set<CertPath> certPaths) {
        return certPaths.stream()
                .collect(Collectors.toMap(Function.identity(), certPath -> SignVerifyUtils.calculateCertInformationFor(certPath, now)));
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
