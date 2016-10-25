/* SecurityDialog.java
   Copyright (C) 2010 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package net.sourceforge.jnlp.security;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.swing.JDialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.DialogType;
import net.sourceforge.jnlp.security.dialogresults.DialogResult;
import net.sourceforge.jnlp.security.dialogs.AccessWarningPane;
import net.sourceforge.jnlp.security.dialogs.AppletWarningPane;
import net.sourceforge.jnlp.security.dialogs.CertWarningPane;
import net.sourceforge.jnlp.security.dialogs.CertsInfoPane;
import net.sourceforge.jnlp.security.dialogs.InetSecurity511Panel;
import net.sourceforge.jnlp.security.dialogs.MissingALACAttributePanel;
import net.sourceforge.jnlp.security.dialogs.MissingPermissionsAttributePanel;
import net.sourceforge.jnlp.security.dialogs.MoreInfoPane;
import net.sourceforge.jnlp.security.dialogs.PasswordAuthenticationPane;
import net.sourceforge.jnlp.security.dialogs.SecurityDialogPanel;
import net.sourceforge.jnlp.security.dialogs.SingleCertInfoPane;
import net.sourceforge.jnlp.security.dialogs.ViwableDialog;
import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.AppTrustWarningDialog;
import net.sourceforge.jnlp.util.ScreenFinder;
import net.sourceforge.jnlp.util.logging.OutputController;

/**
 * Provides methods for showing security warning dialogs for a wide range of
 * JNLP security issues. Note that the security dialogs should be running in the
 * secure AppContext - this class should not be used directly from an applet or
 * application. See {@link SecurityDialogs} for a way to show security dialogs.
 *
 * @author <a href="mailto:jsumali@redhat.com">Joshua Sumali</a>
 */
public class SecurityDialog {

    /** The type of dialog we want to show */
    private final DialogType dialogType;

    /** The type of access that this dialog is for */
    private final AccessType accessType;

    private SecurityDialogPanel panel;

    /** The application file associated with this security warning */
    private final JNLPFile file;

    private final CertVerifier certVerifier;

    private final X509Certificate cert;

    /** An optional String array that's only necessary when a dialog
     * label requires some parameters (e.g. showing which address an application
     * is trying to connect to).
     */
    private final Object[] extras;

    /** Whether or not this object has been fully initialized */
    private boolean initialized = false;

    private DialogResult value;
    
    private ViwableDialog viwableDialog;

    /** Should show signed JNLP file warning */
    private boolean requiresSignedJNLPWarning;

    SecurityDialog(DialogType dialogType, AccessType accessType,
                JNLPFile file, CertVerifier JarCertVerifier, X509Certificate cert, Object[] extras) {
        this.viwableDialog = new ViwableDialog();
        this.dialogType = dialogType;
        this.accessType = accessType;
        this.file = file;
        this.certVerifier = JarCertVerifier;
        this.cert = cert;
        this.extras = extras;
        initialized = true;

        if(file != null)
            requiresSignedJNLPWarning= file.requiresSignedJNLPWarning();

        initDialog();
    }

    /**
     * Construct a SecurityDialog to display some sort of access warning
     */
    private SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file) {
        this(dialogType, accessType, file, null, null, null);
    }

    /**
     * Create a SecurityDialog to display a certificate-related warning
     */
    private SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, CertVerifier certVerifier) {
        this(dialogType, accessType, file, certVerifier, null, null);
    }

    /**
     * Create a SecurityDialog to display a certificate-related warning
     */
    private SecurityDialog(DialogType dialogType, AccessType accessType,
                CertVerifier certVerifier) {
        this(dialogType, accessType, null, certVerifier, null, null);
    }

    /**
     * Create a SecurityDialog to display some sort of access warning
     * with more information
     */
    private SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, Object[] extras) {
        this(dialogType, accessType, file, null, null, extras);
    }

    /**
     * Create a SecurityWarningDailog to display information about a single
     * certificate
     */
    private SecurityDialog(DialogType dialogType, X509Certificate c) {
        this(dialogType, null, null, null, c, null);
    }

    /**
     * Returns if this dialog has been fully initialized yet.
     * @return true if this dialog has been initialized, and false otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Shows more information regarding jar code signing
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent the parent option pane
     */
    public static void showMoreInfoDialog(
                CertVerifier certVerifier, SecurityDialog parent) {

        JNLPFile file= parent.getFile();
        SecurityDialog dialog =
                        new SecurityDialog(DialogType.MORE_INFO, null, file,
                                certVerifier);
        dialog.getViwableDialog().setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.getViwableDialog().show();
        dialog.getViwableDialog().dispose();
    }

    /**
     * Displays CertPath information in a readable table format.
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent the parent option pane
     */
    public static void showCertInfoDialog(CertVerifier certVerifier,
                Component parent) {
        SecurityDialog dialog = new SecurityDialog(DialogType.CERT_INFO,
                        null, null, certVerifier);
        dialog.getViwableDialog().setLocationRelativeTo(parent);
        dialog.getViwableDialog().setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.getViwableDialog().show();
        dialog.getViwableDialog().dispose();
    }

    /**
     * Displays a single certificate's information.
     *
     * @param c the X509 certificate.
     * @param parent the parent pane.
     */
    public static void showSingleCertInfoDialog(X509Certificate c,
                        JDialog parent) {
        SecurityDialog dialog = new SecurityDialog(DialogType.SINGLE_CERT_INFO, c);
        dialog.getViwableDialog().setLocationRelativeTo(parent);
        dialog.getViwableDialog().setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.getViwableDialog().show();
        dialog.getViwableDialog().dispose();
    }

    private void initDialog() {
        String dialogTitle = createTitle();

        getViwableDialog().setTitle(dialogTitle);
        getViwableDialog().setModalityType(ModalityType.MODELESS);

        getViwableDialog().setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        installPanel();

        getViwableDialog().pack();
        getViwableDialog().centerDialog();

        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    selectDefaultButton();
                    gotFocus = true;
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
                if (e.getSource() instanceof SecurityDialog) {
                    SecurityDialog dialog = (SecurityDialog) e.getSource();
                    dialog.getViwableDialog().setResizable(true);
                    dialog.setValue(null);
                }
            }
        };
        getViwableDialog().addWindowListener(adapter);
        getViwableDialog().addWindowFocusListener(adapter);
    }

    private String createTitle() {
        return createTitle(dialogType, accessType);
    }
    private static String createTitle(DialogType dtype, AccessType atype) {
        String dialogTitle = "";
        if (dtype == DialogType.CERT_WARNING) {
            if (atype == AccessType.VERIFIED)
                dialogTitle = "Security Approval Required";
            else
                dialogTitle = "Security Warning";
        } else if (dtype == DialogType.MORE_INFO)
            dialogTitle = "More Information";
        else if (dtype == DialogType.CERT_INFO)
            dialogTitle = "Details - Certificate";
        else if (dtype == DialogType.ACCESS_WARNING)
            dialogTitle = "Security Warning";
        else if (dtype == DialogType.APPLET_WARNING)
            dialogTitle = "Applet Warning";
        else if (dtype == DialogType.PARTIALLYSIGNED_WARNING)
            dialogTitle = "Security Warning";
        else if (dtype == DialogType.AUTHENTICATION)
            dialogTitle = "Authentication Required";
        return dialogTitle;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public JNLPFile getFile() {
        return file;
    }

    public CertVerifier getCertVerifier() {
        return certVerifier;
    }

    public X509Certificate getCert() {
        return cert;
    }

    /*
     * find appropriate JPanel to this Dialog, based on {@link DialogType}.
     */
    private SecurityDialogPanel getPanel() {
        return getPanel(this);
    }
    
    /*
     * find appropriate JPanel to given Dialog, based on {@link DialogType}.
     */
    static SecurityDialogPanel getPanel(SecurityDialog sd) {
        return getPanel(sd.dialogType, sd);
    }
    
    static SecurityDialogPanel getPanel(DialogType type, SecurityDialog sd) {
        SecurityDialogPanel lpanel = null;
        if (type == DialogType.CERT_WARNING) {
            lpanel = new CertWarningPane(sd, sd.certVerifier, (SecurityDelegate) sd.extras[0]);
        } else if (type == DialogType.MORE_INFO) {
            lpanel = new MoreInfoPane(sd, sd.certVerifier);
        } else if (type == DialogType.CERT_INFO) {
            lpanel = new CertsInfoPane(sd, sd.certVerifier);
        } else if (type == DialogType.SINGLE_CERT_INFO) {
            lpanel = new SingleCertInfoPane(sd, sd.certVerifier);
        } else if (type == DialogType.ACCESS_WARNING) {
            lpanel = new AccessWarningPane(sd, sd.extras, sd.certVerifier);
        } else if (type == DialogType.APPLET_WARNING) {
            lpanel = new AppletWarningPane(sd, sd.certVerifier);
        } else if (type == DialogType.PARTIALLYSIGNED_WARNING) {
            lpanel = AppTrustWarningDialog.partiallySigned(sd, sd.file, (SecurityDelegate) sd.extras[0]);
        } else if (type == DialogType.UNSIGNED_WARNING) {
            lpanel = AppTrustWarningDialog.unsigned(sd, sd.file); // Only necessary for applets on 'high security' or above
        } else if (type == DialogType.AUTHENTICATION) {
            lpanel = new PasswordAuthenticationPane(sd, sd.extras);
        } else if (type == DialogType.UNSIGNED_EAS_NO_PERMISSIONS_WARNING) {
            lpanel = new MissingPermissionsAttributePanel(sd, sd.file.getTitle(), sd.file.getNotNullProbalbeCodeBase().toExternalForm());
        } else if (type == DialogType.MISSING_ALACA) {
            lpanel = new MissingALACAttributePanel(sd, sd.file.getTitle(), (String) sd.extras[0], (String) sd.extras[1]);
        } else if (type == DialogType.MATCHING_ALACA) {
            lpanel = AppTrustWarningDialog.matchingAlaca(sd, sd.file, (String) sd.extras[0], (String) sd.extras[1]);
        } else if (type == DialogType.SECURITY_511) {
            lpanel = new InetSecurity511Panel(sd, (URL) sd.extras[0]);
        } else {
            throw new RuntimeException("Unknown value of " + sd.dialogType + ". Panel will be null. Tahts not allowed.");
        }
        return lpanel;
    }

    /*
     * Adds the appropriate JPanel to this Dialog, based on {@link DialogType}.
     */
    private void installPanel() {
        panel = getPanel();
        getViwableDialog().add(panel, BorderLayout.CENTER);
    }

    private void selectDefaultButton() {
        if (panel == null) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "initial value panel is null");
        } else {
            panel.requestFocusOnDefaultButton();
        }
    }

    public void setValue(DialogResult value) {
        OutputController.getLogger().log("Setting value:" + value);
        this.value = value;
    }

    public DialogResult getValue() {
        OutputController.getLogger().log("Returning value:" + value);
        return value;
    }

    
    public boolean requiresSignedJNLPWarning()
    {
        return requiresSignedJNLPWarning;
    }

    DialogResult getDefaultNegativeAnswer() {
        return panel.getDefaultNegativeAnswer();
    }

    DialogResult getDefaultPositiveAnswer() {
        return  panel.getDefaultPositiveAnswer();
    }

    String getText() {
        return panel.getText();
    }

    DialogResult readFromStdIn(String what){
        return panel.readFromStdIn(what);
    }

    String helpToStdIn(){
        return panel.helpToStdIn();
    }

    public ViwableDialog getViwableDialog() {
        return viwableDialog;
    }
    
    public SecurityDialogPanel getSecurityDialogPanel(){
        return panel;
    }
}
