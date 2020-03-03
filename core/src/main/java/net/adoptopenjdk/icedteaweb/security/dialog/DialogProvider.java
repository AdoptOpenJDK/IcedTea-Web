package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.adoptopenjdk.icedteaweb.security.dialog.result.CreateShortcutResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;

import java.awt.Dialog;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * This is a dialog provider that is able to show various application dialogs and wait for the user input to
 * deliver it as specific result object. There are no side-effects during user interaction by the dialog code.
 * All user decisions are provided in the result.
 */
public class DialogProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DialogProvider.class);
    private static final Translator TRANSLATOR = Translator.getInstance();

    public static RememberableResult<AllowDeny> showReadWriteFileAccessWarningDialog(final JNLPFile file, final String filePath) {
        return showFileAccessWarningDialog(file, "SFileReadWriteAccess", filePath);
    }

    public static RememberableResult<AllowDeny> showReadFileAccessWarningDialog(final JNLPFile file, final String filePath) {
        return showFileAccessWarningDialog(file, "SFileReadAccess", filePath);
    }

    public static RememberableResult<AllowDeny> showWriteFileAccessWarningDialog(final JNLPFile file, final String filePath) {
        return showFileAccessWarningDialog(file, "SFileWriteAccess", filePath);
    }

    private static RememberableResult<AllowDeny> showFileAccessWarningDialog(final JNLPFile file, String messageKey, final String filePath) {
        String message = ofNullable(filePath)
                // .map(FileUtils::displayablePath) // TODO: we should discuss if this is really wanted
                .map(p -> TRANSLATOR.translate(messageKey, p))
                .orElse(TRANSLATOR.translate(messageKey, "AFileOnTheMachine"));

        return showAccessWarningDialog(file, message);
    }

    public static RememberableResult<AllowDeny> showReadClipboardAccessWarningDialog(final JNLPFile file) {
        return showClipboardAccessWarningDialog(file, "SClipboardReadAccess");
    }

    public static RememberableResult<AllowDeny> showWriteClipboardAccessWarningDialog(final JNLPFile file) {
        return showClipboardAccessWarningDialog(file, "SClipboardWriteAccess");
    }

    private static RememberableResult<AllowDeny> showClipboardAccessWarningDialog(final JNLPFile file, String messageKey) {
        String message = TRANSLATOR.translate(messageKey);
        return showAccessWarningDialog(file, message);
    }

    public static RememberableResult<AllowDeny> showPrinterAccessWarningDialog(final JNLPFile file) {
        String message = TRANSLATOR.translate("SPrinterAccess");
        return showAccessWarningDialog(file, message);
    }

    public static RememberableResult<AllowDeny> showNetworkAccessWarningDialog(final JNLPFile file, final Object address) {
        // TODO: Network access seems not to be used up to now, therefore it is unclear what type address really will be
        String message = ofNullable(address)
                .map(a -> TRANSLATOR.translate("SNetworkAccess", a))
                .orElse(TRANSLATOR.translate("SNetworkAccess", "<unknown>"));
        return showAccessWarningDialog(file, message);
    }

    public static RememberableResult<AllowDeny> showAccessWarningDialog(final JNLPFile file, final String message) {
        final AccessWarningDialog dialogWithResult = AccessWarningDialog.create(file, message);
        return dialogWithResult.showAndWait();
    }

    public static Optional<RememberableResult<CreateShortcutResult>> showCreateShortcutDialog(final JNLPFile file) {
        final CreateShortcutDialog createShortcutDialog = CreateShortcutDialog.create(file);
        return createShortcutDialog.showAndWait();
    }

    public static void showCertWarningDetailsDialog(final Dialog owner, final JNLPFile file, final List<? extends Certificate> certificates, final List<String> certIssues) {
        CertWarningDetailsDialog dialog = CertWarningDetailsDialog.create(owner, file, certificates, certIssues);
        dialog.showAndWait();
    }

    public static RememberableResult<AllowDeny> showUnsignedWarningDialog(final JNLPFile file) {
        final UnsignedWarningDialog unsignedWarningDialog = UnsignedWarningDialog.create(file);
        return unsignedWarningDialog.showAndWait();
    }

    public static AccessWarningResult showHttpsCertTrustDialog(final JNLPFile file, final Certificate certificate, final boolean rootInCaCerts, final List<? extends Certificate> certificates, final List<String> certIssues) {
        final HttpsCertTrustDialog dialog = HttpsCertTrustDialog.create(file, certificate, rootInCaCerts, certificates, certIssues);
        return dialog.showAndWait();
    }

    public static AccessWarningResult showJarCertWarningDialog(final AccessType accessType, final JNLPFile file, final boolean rootInCaCerts, final List<? extends Certificate> certificates, final List<String> certIssues, final SecurityDelegate securityDelegate) {
        final String message = getMessageFor(accessType);
        final String moreInformationText = getMoreInformationText(accessType, rootInCaCerts);
        final boolean alwaysTrustSelected = (accessType == AccessType.VERIFIED);

        final JarCertWarningDialog dialog = JarCertWarningDialog.create(message, file, rootInCaCerts, certificates, certIssues, securityDelegate, moreInformationText, alwaysTrustSelected);
        return dialog.showAndWait();
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

    public static RememberableResult<AllowDenySandbox> showPartiallySignedWarningDialog(final JNLPFile file) {
        final PartiallySignedWarningDialog dialog = PartiallySignedWarningDialog.create(file);
        return dialog.showAndWait();
    }

    public static RememberableResult<AllowDeny> showMissingALACAttributeDialog(final JNLPFile file, final Set<URL> locations) {
        final MissingALACAttributeDialog dialog = MissingALACAttributeDialog.create(file, locations);
        return dialog.showAndWait();
    }

    public static RememberableResult<AllowDeny> showMatchingALACAttributeDialog(final JNLPFile file, final Set<URL> locations) {
        final MatchingALACAttributeDialog dialog = MatchingALACAttributeDialog.create(file, locations);
        return dialog.showAndWait();
    }

    public static RememberableResult<AllowDeny> showMissingPermissionsAttributeDialog(final JNLPFile file) {
        final MissingPermissionsAttributeDialog dialog = MissingPermissionsAttributeDialog.create(file);
        return dialog.showAndWait();
    }

    public static void showCertInfoDialog(final Dialog owner, final List<? extends Certificate> certificates) {
        CertInfoDialog dialog = CertInfoDialog.create(owner, certificates);
        dialog.showAndWait();
    }

    public static NamePassword showAuthenticationDialog(final String host, final int port, final String prompt, final String type) {
        AuthenticationDialog dialog = AuthenticationDialog.create(host, port, prompt, type);
        return dialog.showAndWait().orElse(null);
    }

    public static void showMoreInfoDialog(final List<? extends Certificate> certificates, final List<String> certIssues, final JNLPFile file) {
        DialogProvider.showCertWarningDetailsDialog(null, file, certificates, certIssues);
    }
}