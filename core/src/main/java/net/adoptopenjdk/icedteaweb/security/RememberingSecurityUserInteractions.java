package net.adoptopenjdk.icedteaweb.security;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.lockingfile.StorageIoException;
import net.adoptopenjdk.icedteaweb.security.dialog.DialogProvider;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisions;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisionsFileStore;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.config.ConfigurationConstants;
import net.sourceforge.jnlp.config.DeploymentConfiguration;
import net.sourceforge.jnlp.runtime.JNLPRuntime;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.tools.CertInformation;

import java.security.cert.CertPath;
import java.util.Optional;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult.ALWAYS;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_UNSIGNED_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.of;

/**
 * Interactions with user for concerning security and permission related decisions.
 */
public class RememberingSecurityUserInteractions implements SecurityUserInteractions {
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final UserDecisions userDecisions;

    public RememberingSecurityUserInteractions() {
        this(new UserDecisionsFileStore());
    }

    RememberingSecurityUserInteractions(UserDecisions userDecisions) {
        this.userDecisions = userDecisions;
    }

    /**
     * Ask the user for permission by showing an {@link net.adoptopenjdk.icedteaweb.security.dialog.UnsignedWarningDialog}
     * when an application is unsigned. This is used with 'high-security' setting.
     *
     * @param file
     * @return
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/properties.html#sthref373">Java 8 Properties</a>
     */
    public AllowDeny askUserForPermissionToRunUnsignedApplication(final JNLPFile file) {
        DeploymentConfiguration conf = JNLPRuntime.getConfiguration();
        if (conf == null) {
            throw new StorageIoException("JNLPRuntime configuration is null. Try to reinstall IcedTea-Web");
        }
        SecurityLevel securityLevel = SecurityLevel.valueOf(conf.getProperty(ConfigurationConstants.KEY_SECURITY_LEVEL));

        if (securityLevel == SecurityLevel.VERY_HIGH) {
            return AllowDeny.DENY;
        }

        final Optional<AllowDeny> remembered = this.userDecisions.getUserDecisions(RUN_UNSIGNED_APPLICATION, file, AllowDeny.class);

        return remembered.orElseGet(() -> {
            final RememberableResult<AllowDeny> dialogResult = DialogProvider.showUnsignedWarningDialog(file);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_UNSIGNED_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });
    }

    @Override
    public AllowDenySandbox askUserHowToRunApplicationWithCertIssues(final JNLPFile file, final CertPath certPath, final CertInformation certInfo) {
        final boolean rootInCaCerts = certInfo.isRootInCacerts();

        AccessType accessType;
        if (rootInCaCerts && !certInfo.hasSigningIssues()) {
            accessType = AccessType.VERIFIED;
        } else if (certInfo.isRootInCacerts()) {
            accessType = AccessType.SIGNING_ERROR;
        } else {
            accessType = AccessType.UNVERIFIED;
        }

        final String message = getMessageFor(accessType);
        final boolean alwaysTrustSelected = (accessType == AccessType.VERIFIED);
        final String moreInformationText = getMoreInformationText(accessType, rootInCaCerts);

        final AccessWarningResult result = DialogProvider.showJarCertWarningDialog(file, certPath.getCertificates(), certInfo.getDetailsAsStrings(), message, alwaysTrustSelected, moreInformationText);

        if (result == ALWAYS) {
            // TODO: store vertificate in trust store
        }

        switch (result) {
            case YES:
            case ALWAYS:
                return AllowDenySandbox.ALLOW;
            case SANDBOX:
                return AllowDenySandbox.SANDBOX;
            default:
                return AllowDenySandbox.DENY;
        }
    }

    private static String getMoreInformationText(final AccessType accessType, final boolean rootInCaCerts) {
        String moreInformationText = rootInCaCerts ?
                TRANSLATOR.translate("STrustedSource") : TRANSLATOR.translate("SUntrustedSource");

        switch (accessType) {
            case UNVERIFIED:
            case SIGNING_ERROR:
                return moreInformationText + " " + TRANSLATOR.translate("SWarnFullPermissionsIgnorePolicy");
            default:
                return moreInformationText;
        }
    }

    private static String getMessageFor(final AccessType accessType) {
        switch (accessType) {
            case VERIFIED:
                return TRANSLATOR.translate("SSigVerified");
            case UNVERIFIED:
                return TRANSLATOR.translate("SSigUnverified");
            case SIGNING_ERROR:
                return TRANSLATOR.translate("SSignatureError");
            default:
                return "";
        }
    }
}
