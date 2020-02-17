package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessage;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.security.dialog.AccessWarningDialog;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDenyRememberResult;
import net.adoptopenjdk.icedteaweb.security.dialog.result.AllowDeny;
import net.adoptopenjdk.icedteaweb.security.dialog.CertWarningDialog;
import net.adoptopenjdk.icedteaweb.security.dialog.CreateShortcutDialog;
import net.adoptopenjdk.icedteaweb.security.dialog.HttpsCertTrustDialog;
import net.adoptopenjdk.icedteaweb.security.dialog.result.CreateShortcutResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.Primitive;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.ShortcutResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
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

import static net.sourceforge.jnlp.security.AccessType.*;

public class NewDialogFactory implements DialogFactory {
    private final static Translator TRANSLATOR = Translator.getInstance();

    @Override
    public AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType, final JNLPFile file, final Object[] extras) {
        if (Arrays.asList(VERIFIED, UNVERIFIED, PARTIALLY_SIGNED, UNSIGNED, SIGNING_ERROR).contains(accessType)) {
            throw new RuntimeException(accessType + " cannot be displayed in AccessWarningDialog");
        }

        AccessWarningPaneComplexReturn ar;

        if (accessType == CREATE_DESKTOP_SHORTCUT) {
            final CreateShortcutDialog createShortcutDialog = CreateShortcutDialog.create(file);
            final Optional<CreateShortcutResult> result = createShortcutDialog.showAndWait();

            if (!result.isPresent()) {
                ar = new AccessWarningPaneComplexReturn(Primitive.CANCEL);
            }
            else {
                ar = new AccessWarningPaneComplexReturn(Primitive.YES);
                ar.setDesktop(new ShortcutResult(result.get().getCreateDesktopShortcut() == AllowDeny.ALLOW));
                ar.setMenu(new ShortcutResult(result.get().getCreateMenuShortcut() == AllowDeny.ALLOW));

                handleRememberUserDecision(file, result.get());
            }

        } else {
            final AccessWarningDialog dialogWithResult = AccessWarningDialog.create(accessType, file, extras);
            final AllowDenyRememberResult result = dialogWithResult.showAndWait();

            ar = new AccessWarningPaneComplexReturn(result.getAllowDenyResult() == AllowDeny.ALLOW);

            handleRememberUserDecision(file, accessType, result);
        }

        return ar;
    }

    private void handleRememberUserDecision(final JNLPFile file, final CreateShortcutResult result) {
        // TODO handle remember
        //
        // store user decision in file (today: UnsignedAppletTrustConfirmation.updateAppletAction()
        //
    }

   private void handleRememberUserDecision(final JNLPFile file, final AccessType accessType, final AllowDenyRememberResult result) {
        // TODO handle remember
        //
        // store user decision in file (today: UnsignedAppletTrustConfirmation.updateAppletAction()
        //
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
            dialogWithResult = HttpsCertTrustDialog.create(accessType, file, (HttpsCertVerifier) certVerifier);
        } else {
            dialogWithResult = CertWarningDialog.create(accessType, file, certVerifier, securityDelegate);
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

        // MoreInfoPane

    }

    @Override
    public void showCertInfoDialog(final CertVerifier certVerifier, final Component parent) {

    }

    @Override
    public void showSingleCertInfoDialog(final X509Certificate c, final Window parent) {
    }

    private static String getTitleFor(DialogType dialogType) {
        // TODO do translations

        String title = "";
        if (dialogType == DialogType.MORE_INFO) {
            title = "More Information";
        } else if (dialogType == DialogType.CERT_INFO) {
            title = "Details - Certificate";
        } else if (dialogType == DialogType.APPLET_WARNING) {
            title = "Applet Warning";
        } else if (dialogType == DialogType.PARTIALLY_SIGNED_WARNING) {
            title = "Security Warning";
        } else if (dialogType == DialogType.AUTHENTICATION) {
            title = "Authentication Required";
        }

        return TRANSLATOR.translate(title);
    }
}
