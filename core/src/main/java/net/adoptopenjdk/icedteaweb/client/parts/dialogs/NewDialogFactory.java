package net.adoptopenjdk.icedteaweb.client.parts.dialogs;

import net.adoptopenjdk.icedteaweb.client.parts.dialogs.security.SecurityDialogMessage;
import net.adoptopenjdk.icedteaweb.i18n.Translator;
import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.adoptopenjdk.icedteaweb.resources.Resource;
import net.adoptopenjdk.icedteaweb.security.dialogs.AccessWarningDialog;
import net.adoptopenjdk.icedteaweb.security.dialogs.AccessWarningResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.AccessWarningPaneComplexReturn;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.DialogResult;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.NamePassword;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandbox;
import net.adoptopenjdk.icedteaweb.ui.swing.dialogresults.YesNoSandboxLimited;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.SecurityDelegate;
import net.sourceforge.jnlp.security.AccessType;
import net.sourceforge.jnlp.security.CertVerifier;

import java.awt.Component;
import java.awt.Window;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Set;

public class NewDialogFactory implements DialogFactory {
    private final static Translator TRANSLATOR = Translator.getInstance();

    @Override
    public AccessWarningPaneComplexReturn showAccessWarningDialog(final AccessType accessType, final JNLPFile file, final Object[] extras) {
        String title = getTitleFor(DialogType.ACCESS_WARNING, accessType);
        String message = getMessageFor(accessType, extras);
        AccessWarningDialog dialogWithResult = AccessWarningDialog.create(title, message, file, extras[0]);

        final AccessWarningResult accessWarningResult = dialogWithResult.showAndWait();

        return new AccessWarningPaneComplexReturn(accessWarningResult == AccessWarningResult.OK);
    }

    @Override
    public YesNoSandboxLimited showUnsignedWarningDialog(final JNLPFile file) {
        String title = getTitleFor(DialogType.UNSIGNED_WARNING, AccessType.UNSIGNED);

        // calls UnsignedAppletTrustWarningPanel
        // to be removed as Applets are not longer supported?

        return null;
    }

    @Override
    public YesNoSandbox showCertWarningDialog(final AccessType accessType, final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        String title = getTitleFor(DialogType.CERT_WARNING, accessType);

        // calls CertWarningPane

        return null;
    }

    @Override
    public YesNoSandbox showPartiallySignedWarningDialog(final JNLPFile file, final CertVerifier certVerifier, final SecurityDelegate securityDelegate) {
        String title = getTitleFor(DialogType.PARTIALLY_SIGNED_WARNING, AccessType.PARTIALLY_SIGNED);

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

    private static String getTitleFor(DialogType dialogType, AccessType accessType) {
        // TODO do translations

        String title = "";
        if (dialogType == DialogType.CERT_WARNING) {
            if (accessType == AccessType.VERIFIED) {
                title = "Security Approval Required";
            } else {
                title = "Security Warning";
            }
        } else if (dialogType == DialogType.MORE_INFO) {
            title = "More Information";
        } else if (dialogType == DialogType.CERT_INFO) {
            title = "Details - Certificate";
        } else if (dialogType == DialogType.ACCESS_WARNING) {
            title = "Security Warning";
        } else if (dialogType == DialogType.APPLET_WARNING) {
            title = "Applet Warning";
        } else if (dialogType == DialogType.PARTIALLY_SIGNED_WARNING) {
            title = "Security Warning";
        } else if (dialogType == DialogType.AUTHENTICATION) {
            title = "Authentication Required";
        }

        return TRANSLATOR.translate(title);
    }

    private static String getMessageFor(final AccessType accessType, final Object[] extras) {
        switch (accessType) {
            case READ_WRITE_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileReadWriteAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileReadWriteAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case READ_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileReadAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileReadAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case WRITE_FILE:
                if (extras != null && extras.length > 0 && extras[0] instanceof String) {
                    return TRANSLATOR.translate("SFileWriteAccess", FileUtils.displayablePath((String) extras[0]));
                } else {
                    return TRANSLATOR.translate("SFileWriteAccess", TRANSLATOR.translate("AFileOnTheMachine"));
                }
            case CREATE_DESKTOP_SHORTCUT:
                return TRANSLATOR.translate("SDesktopShortcut");
            case CLIPBOARD_READ:
                return TRANSLATOR.translate("SClipboardReadAccess");
            case CLIPBOARD_WRITE:
                return TRANSLATOR.translate("SClipboardWriteAccess");
            case PRINTER:
                return TRANSLATOR.translate("SPrinterAccess");
            case NETWORK:
                if (extras != null && extras.length >= 0) {
                    return TRANSLATOR.translate("SNetworkAccess", extras[0]);
                } else {
                    return TRANSLATOR.translate("SNetworkAccess", "(address here)");
                }
            default:
                return "";
        }
    }
}
