package net.adoptopenjdk.icedteaweb.client.parts.dialogs.security;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.security.CertVerifier;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.security.cert.X509Certificate;

public class SecurityDialogFactory {

    /**
     * Shows more information regarding jar code signing
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent       the parent NumberOfArguments pane
     */
    public static void showMoreInfoDialog(
            CertVerifier certVerifier, SecurityDialog parent) {

        JNLPFile file = parent.getFile();
        SecurityDialog dialog =
                new SecurityDialog(SecurityDialogs.DialogType.MORE_INFO, null, file,
                        certVerifier);
        dialog.getViewableDialog().setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getViewableDialog().show();
        dialog.getViewableDialog().dispose();
    }

    /**
     * Displays CertPath information in a readable table format.
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent       the parent NumberOfArguments pane
     */
    public static void showCertInfoDialog(CertVerifier certVerifier,
                                          Component parent) {
        SecurityDialog dialog = new SecurityDialog(SecurityDialogs.DialogType.CERT_INFO,
                null, null, certVerifier);
        dialog.getViewableDialog().setLocationRelativeTo(parent);
        dialog.getViewableDialog().setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getViewableDialog().show();
        dialog.getViewableDialog().dispose();
    }

    /**
     * Displays a single certificate's information.
     *
     * @param c      the X509 certificate.
     * @param parent the parent pane.
     */
    public static void showSingleCertInfoDialog(X509Certificate c,
                                                Window parent) {
        SecurityDialog dialog = new SecurityDialog(SecurityDialogs.DialogType.SINGLE_CERT_INFO, c);
        dialog.getViewableDialog().setLocationRelativeTo(parent);
        dialog.getViewableDialog().setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getViewableDialog().show();
        dialog.getViewableDialog().dispose();
    }
}
