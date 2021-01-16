package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenySandbox;
import net.adoptopenjdk.icedteaweb.security.dialog.result.CreateShortcutResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.ShortcutResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecision;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisions;
import net.adoptopenjdk.icedteaweb.userdecision.UserDecisionsFileStore;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;
import net.sourceforge.jnlp.security.HttpsCertVerifier;

import java.awt.Component;
import java.awt.Window;
import java.net.URL;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.ALLOW;
import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.DENY;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.CREATE_DESKTOP_SHORTCUT;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.CREATE_MENU_SHORTCUT;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_MATCHING_ALAC_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_MISSING_ALAC_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_MISSING_PERMISSIONS_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.RUN_PARTIALLY_APPLICATION;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.of;
import static net.sourceforge.jnlp.security.AccessType.PARTIALLY_SIGNED;
import static net.sourceforge.jnlp.security.AccessType.SIGNING_ERROR;
import static net.sourceforge.jnlp.security.AccessType.UNSIGNED;
import static net.sourceforge.jnlp.security.AccessType.UNVERIFIED;
import static net.sourceforge.jnlp.security.AccessType.VERIFIED;

public class NewDialogFactory implements DialogFactory {
    private static final Translator TRANSLATOR = Translator.getInstance();

    private final UserDecisions userDecisions;

    NewDialogFactory() {
        this(new UserDecisionsFileStore());
    }

    NewDialogFactory(final UserDecisions userDecisions) {
        this.userDecisions = userDecisions;
    }

    @Override
    public AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType, final JNLPFile file, final Object[] extras) {
        if (Arrays.asList(VERIFIED, UNVERIFIED, PARTIALLY_SIGNED, UNSIGNED, SIGNING_ERROR).contains(accessType)) {
            throw new RuntimeException(accessType + " cannot be displayed in AccessWarningDialog");
        }

        if (accessType == AccessType.CREATE_DESKTOP_SHORTCUT) {
            return askForPermissionToCreateShortcuts(file);
        } else {
            return askForAccessPermission(accessType, file, extras);
        }
    }

    @Override
    public YesNoSandbox showCertWarningDialog(final AccessType accessType, final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        final AccessWarningResult certWarningResult;

        final Optional<CertVerifier> certVerifierOptional = Optional.ofNullable(certVerifier);

        final List<? extends Certificate> certificates = certVerifierOptional
                .map(cp -> certVerifier.getCertPath())
                .map(CertPath::getCertificates)
                .orElse(Collections.emptyList());

        final List<String> certIssues = certVerifierOptional
                .map(cv -> cv.getDetails(null))
                .orElse(Collections.emptyList());

        final boolean rootInCaCerts = certVerifierOptional.map(CertVerifier::getRootInCaCerts).orElse(Boolean.FALSE);

        if (certVerifier instanceof HttpsCertVerifier) {
            certWarningResult = DialogProvider.showHttpsCertTrustDialog(file, certVerifier.getPublisher(null), rootInCaCerts, certificates, certIssues);
        } else {
            final String message = getMessageFor(accessType);
            final String moreInformationText = getMoreInformationText(accessType, rootInCaCerts);
            final boolean alwaysTrustSelected = (accessType == AccessType.VERIFIED);

            certWarningResult = DialogProvider.showJarCertWarningDialog(file, certificates, certIssues, message, alwaysTrustSelected, moreInformationText);
        }
        switch (certWarningResult) {
            case YES:
                return YesNoSandbox.yes();
            case SANDBOX:
                return YesNoSandbox.sandbox();
            default:
                return YesNoSandbox.no();
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

    @Override
    public YesNoSandbox showPartiallySignedWarningDialog(final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        final Optional<AllowDenySandbox> remembered = this.userDecisions.getUserDecisions(RUN_PARTIALLY_APPLICATION, file, AllowDenySandbox.class);

        final AllowDenySandbox result = remembered.orElseGet(() -> {
            final RememberableResult<AllowDenySandbox> dialogResult = DialogProvider.showPartiallySignedWarningDialog(file);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_PARTIALLY_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });

        return result == AllowDenySandbox.ALLOW ? YesNoSandbox.yes() :
                result == AllowDenySandbox.SANDBOX ? YesNoSandbox.sandbox() : YesNoSandbox.no();
    }

    @Override
    public NamePassword showAuthenticationPrompt(final String host, final int port, final String prompt, final String type) {
        return DialogProvider.showAuthenticationDialog(host, port, prompt, type);
    }

    @Override
    public boolean showMissingALACAttributePanel(final JNLPFile file, final URL codeBase, final Set<URL> locations) {
        // On the only place where "codeBase" is handed in, it is done as file.getCodebase().
        // In the dialog this is checked by file.getNotNullProbableCodeBase() first, which makes
        // the handover of codebase obsolete here
        final Optional<AllowDeny> remembered = this.userDecisions.getUserDecisions(RUN_MISSING_ALAC_APPLICATION, file, AllowDeny.class);

        final AllowDeny result = remembered.orElseGet(() -> {
            final RememberableResult<AllowDeny> dialogResult = DialogProvider.showMissingALACAttributeDialog(file, locations);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_MISSING_ALAC_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });

        return result == ALLOW;
    }

    @Override
    public boolean showMatchingALACAttributePanel(final JNLPFile file, final URL documentBase, final Set<URL> locations) {
        // On the only place where "documentBase" is handed in, it is done as file.getCodebase().
        // In the dialog this is checked by file.getNotNullProbableCodeBase() first, which makes
        // the handover of codebase obsolete here
        final Optional<AllowDeny> remembered = this.userDecisions.getUserDecisions(RUN_MATCHING_ALAC_APPLICATION, file, AllowDeny.class);

        final AllowDeny result = remembered.orElseGet(() -> {
            final RememberableResult<AllowDeny> dialogResult = DialogProvider.showMatchingALACAttributeDialog(file, locations);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_MATCHING_ALAC_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });

        return result == ALLOW;
    }

    @Override
    public boolean showMissingPermissionsAttributeDialogue(final JNLPFile file) {
        final Optional<AllowDeny> remembered = this.userDecisions.getUserDecisions(RUN_MISSING_PERMISSIONS_APPLICATION, file, AllowDeny.class);

        final AllowDeny result = remembered.orElseGet(() -> {
            final RememberableResult<AllowDeny> dialogResult = DialogProvider.showMissingPermissionsAttributeDialog(file);
            userDecisions.save(dialogResult.getRemember(), file, of(RUN_MISSING_PERMISSIONS_APPLICATION, dialogResult.getResult()));
            return dialogResult.getResult();
        });

        return result == ALLOW;
    }

    @Override
    public boolean show511Dialogue(final Resource r) {
        return false;
    }

    @Override
    public void showMoreInfoDialog(final CertVerifier certVerifier, final JNLPFile file) {
        final Optional<CertVerifier> certVerifierOptional = Optional.ofNullable(certVerifier);

        final List<? extends Certificate> certificates = certVerifierOptional
                .map(cp -> certVerifier.getCertPath())
                .map(CertPath::getCertificates)
                .orElse(Collections.emptyList());

        final List<String> certIssues = certVerifierOptional
                .map(cv -> cv.getDetails(null))
                .orElse(Collections.emptyList());

        DialogProvider.showCertWarningDetailsDialog(null, file, certificates, certIssues);
    }

    @Override
    public void showCertInfoDialog(final CertVerifier certVerifier, final Component parent) {
        final List<? extends Certificate> certificates = Optional.ofNullable(certVerifier)
                .map(cp -> certVerifier.getCertPath())
                .map(CertPath::getCertificates)
                .orElse(Collections.emptyList());
        DialogProvider.showCertInfoDialog(null, certificates);
    }

    @Override
    public void showSingleCertInfoDialog(final X509Certificate c, final Window parent) {
        DialogProvider.showCertInfoDialog(null, Collections.singletonList(c));
    }

    private AccessWarningPaneComplexReturn askForPermissionToCreateShortcuts(JNLPFile file) {
        final Optional<Optional<CreateShortcutResult>> rememberedDecision = getRememberedUserDecision(file).map(Optional::of);

        final Optional<CreateShortcutResult> result = rememberedDecision.orElseGet(() -> showCreateShortcutDialog(file));

        if (!result.isPresent()) {
            return new AccessWarningPaneComplexReturn(Primitive.CANCEL);
        } else {
            final AccessWarningPaneComplexReturn ar;
            ar = new AccessWarningPaneComplexReturn(Primitive.YES);
            ar.setDesktop(new ShortcutResult(result.get().getCreateDesktopShortcut() == AllowDeny.ALLOW));
            ar.setMenu(new ShortcutResult(result.get().getCreateMenuShortcut() == AllowDeny.ALLOW));
            return ar;
        }
    }

    private Optional<CreateShortcutResult> showCreateShortcutDialog(JNLPFile file) {
        final Optional<RememberableResult<CreateShortcutResult>> result = DialogProvider.showCreateShortcutDialog(file);
        return result.map(r -> {
            rememberUserDecision(file, r);
            return r.getResult();
        });
    }

    private Optional<CreateShortcutResult> getRememberedUserDecision(JNLPFile file) {
        final Optional<AllowDeny> createDesktop = userDecisions.getUserDecisions(CREATE_DESKTOP_SHORTCUT, file, AllowDeny.class);
        final Optional<AllowDeny> createMenu = userDecisions.getUserDecisions(CREATE_MENU_SHORTCUT, file, AllowDeny.class);

        if (createDesktop.isPresent() || createMenu.isPresent()) {
            return Optional.of(new CreateShortcutResult(createDesktop.orElse(DENY), createMenu.orElse(DENY)));
        }
        return Optional.empty();
    }

    private void rememberUserDecision(final JNLPFile file, final RememberableResult<CreateShortcutResult> result) {
        userDecisions.save(result.getRemember(), file, of(CREATE_DESKTOP_SHORTCUT, result.getResult().getCreateDesktopShortcut()));
        userDecisions.save(result.getRemember(), file, of(CREATE_MENU_SHORTCUT, result.getResult().getCreateMenuShortcut()));
    }

    private AccessWarningPaneComplexReturn askForAccessPermission(AccessType accessType, JNLPFile file, Object[] extras) {
        final Optional<AllowDeny> rememberedDecision = getRememberedUserDecision(accessType, file);

        final AllowDeny result = rememberedDecision.orElseGet(() -> showAccessPermissionDialog(accessType, file, extras));

        return new AccessWarningPaneComplexReturn(result == AllowDeny.ALLOW);
    }

    private AllowDeny showAccessPermissionDialog(AccessType accessType, JNLPFile file, Object[] extras) {
        final RememberableResult<AllowDeny> dialogResult;

        final Object extra = Optional.ofNullable(extras)
                .filter(nonNullExtras -> nonNullExtras.length > 0)
                .map(nonEmptyExtras -> nonEmptyExtras[0])
                .orElse("<unkown>");

        switch (accessType) {
            case READ_WRITE_FILE:
                dialogResult = DialogProvider.showReadWriteFileAccessWarningDialog(file, extra.toString());
                break;
            case READ_FILE:
                dialogResult = DialogProvider.showReadFileAccessWarningDialog(file, extra.toString());
                break;
            case WRITE_FILE:
                dialogResult = DialogProvider.showWriteFileAccessWarningDialog(file, extra.toString());
                break;
            case CLIPBOARD_READ:
                dialogResult = DialogProvider.showReadClipboardAccessWarningDialog(file);
                break;
            case CLIPBOARD_WRITE:
                dialogResult = DialogProvider.showWriteClipboardAccessWarningDialog(file);
                break;
            case PRINTER:
                dialogResult = DialogProvider.showPrinterAccessWarningDialog(file);
                break;
            case NETWORK:
                dialogResult = DialogProvider.showNetworkAccessWarningDialog(file, extra);
                break;
            default:
                dialogResult = DialogProvider.showAccessWarningDialog(file, "");
        }

        rememberUserDecision(file, accessType, dialogResult);
        return dialogResult.getResult();
    }

    private Optional<AllowDeny> getRememberedUserDecision(AccessType accessType, JNLPFile file) {
        return userDecisions.getUserDecisions(UserDecision.Key.valueOf(accessType), file, AllowDeny.class);
    }

    private void rememberUserDecision(final JNLPFile file, final AccessType accessType, final RememberableResult<AllowDeny> result) {
        userDecisions.save(result.getRemember(), file, of(accessType, result.getResult()));
    }
}
