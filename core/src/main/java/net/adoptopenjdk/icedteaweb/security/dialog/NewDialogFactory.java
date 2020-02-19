package net.adoptopenjdk.icedteaweb.security.dialog;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.DialogFactory;
import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessage;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.result.CreateShortcutResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.RememberableResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.ShortcutResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
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
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny.DENY;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.CREATE_DESKTOP_SHORTCUT;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.Key.CREATE_MENU_SHORTCUT;
import static net.adoptopenjdk.icedteaweb.userdecision.UserDecision.of;
import static net.sourceforge.jnlp.security.AccessType.PARTIALLY_SIGNED;
import static net.sourceforge.jnlp.security.AccessType.SIGNING_ERROR;
import static net.sourceforge.jnlp.security.AccessType.UNSIGNED;
import static net.sourceforge.jnlp.security.AccessType.UNVERIFIED;
import static net.sourceforge.jnlp.security.AccessType.VERIFIED;

public class NewDialogFactory implements DialogFactory {
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
        final CreateShortcutDialog createShortcutDialog = CreateShortcutDialog.create(file);
        return createShortcutDialog.showAndWait()
                .map(r -> {
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
        final AccessWarningDialog dialogWithResult = AccessWarningDialog.create(accessType, file, extras);
        final RememberableResult<AllowDeny> dialogResult = dialogWithResult.showAndWait();
        rememberUserDecision(file, accessType, dialogResult);
        return dialogResult.getResult();
    }

    private Optional<AllowDeny> getRememberedUserDecision(AccessType accessType, JNLPFile file) {
        return userDecisions.getUserDecisions(UserDecision.Key.valueOf(accessType), file, AllowDeny.class);
    }

    private void rememberUserDecision(final JNLPFile file, final AccessType accessType, final RememberableResult<AllowDeny> result) {
        userDecisions.save(result.getRemember(), file, of(accessType, result.getResult()));
    }

    @Override
    public YesNoSandboxLimited showUnsignedWarningDialog(final JNLPFile file) {
        // calls UnsignedAppletTrustWarningPanel
        // to be removed as Applets are not longer supported?
        return null;
    }

    @Override
    public YesNoSandbox showCertWarningDialog(final AccessType accessType, final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        CertWarningDialog dialogWithResult;
        if (certVerifier instanceof HttpsCertVerifier) {
            dialogWithResult = HttpsCertTrustDialog.create(file, (HttpsCertVerifier) certVerifier);
        } else {
            dialogWithResult = JarCertWarningDialog.create(accessType, file, certVerifier, securityDelegate);
        }

        final AccessWarningResult certWarningResult = dialogWithResult.showAndWait();

        switch (certWarningResult) {
            case YES:
                return YesNoSandbox.yes();
            case SANDBOX:
                return YesNoSandbox.sandbox();
            default:
                return YesNoSandbox.no();
        }
    }

    @Override
    public YesNoSandbox showPartiallySignedWarningDialog(final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        return null;
    }

    @Override
    public NamePassword showAuthenticationPrompt(final String host, final int port, final String prompt, final String type) {
        return null;
    }

    @Override
    public boolean showMissingALACAttributePanel(final JNLPFile file, final URL codeBase, final Set<URL> remoteUrls) {
        return false;
    }

    @Override
    public boolean showMatchingALACAttributePanel(final JNLPFile file, final URL documentBase, final Set<URL> remoteUrls) {
        return false;
    }

    @Override
    public boolean showMissingPermissionsAttributeDialogue(final JNLPFile file) {
        return false;
    }

    @Override
    public DialogResult getUserResponse(final SecurityDialogMessage message) {
        return null;
    }

    @Override
    public boolean show511Dialogue(final Resource r) {
        return false;
    }

    @Override
    public void showMoreInfoDialog(final CertVerifier certVerifier, final JNLPFile file) {
         CertWarningDetailsDialog dialog = new CertWarningDetailsDialog(null, file, certVerifier);
         dialog.showAndWait();
    }

    @Override
    public void showCertInfoDialog(final CertVerifier certVerifier, final Component parent) {
        // obsolete, as we show this not longer as a modal dialog but as part of the CertWarningDetailsDialog (collapsible panel: CertificateDetailsPanel)
    }

    @Override
    public void showSingleCertInfoDialog(final X509Certificate c, final Window parent) {
    }
}
