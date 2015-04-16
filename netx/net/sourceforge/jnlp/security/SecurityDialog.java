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
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JDialog;

import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.JNLPClassLoader.SecurityDelegate;
import net.sourceforge.jnlp.security.SecurityDialogs.AccessType;
import net.sourceforge.jnlp.security.SecurityDialogs.DialogType;
import net.sourceforge.jnlp.security.dialogs.AccessWarningPane;
import net.sourceforge.jnlp.security.dialogs.AppletWarningPane;
import net.sourceforge.jnlp.security.dialogs.CertWarningPane;
import net.sourceforge.jnlp.security.dialogs.CertsInfoPane;
import net.sourceforge.jnlp.security.dialogs.MissingALACAttributePanel;
import net.sourceforge.jnlp.security.dialogs.MissingPermissionsAttributePanel;
import net.sourceforge.jnlp.security.dialogs.MoreInfoPane;
import net.sourceforge.jnlp.security.dialogs.PasswordAuthenticationPane;
import net.sourceforge.jnlp.security.dialogs.SecurityDialogPanel;
import net.sourceforge.jnlp.security.dialogs.SingleCertInfoPane;
import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.AppTrustWarningDialog;
import net.sourceforge.jnlp.util.ImageResources;
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
public class SecurityDialog extends JDialog {

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

    /**
     * the return value of this dialog. result: 0 = Yes, 1 = No, 2 = Cancel,
     * null = Window closed.
     */
    private Object value;

    /** Should show signed JNLP file warning */
    private boolean requiresSignedJNLPWarning;

    SecurityDialog(DialogType dialogType, AccessType accessType,
                JNLPFile file, CertVerifier JarCertVerifier, X509Certificate cert, Object[] extras) {
        super();
        setIconImages(ImageResources.INSTANCE.getApplicationImages());
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
    SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file) {
        this(dialogType, accessType, file, null, null, null);
    }

    /**
     * Create a SecurityDialog to display a certificate-related warning
     */
    SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, CertVerifier certVerifier) {
        this(dialogType, accessType, file, certVerifier, null, null);
    }

    /**
     * Create a SecurityDialog to display a certificate-related warning
     */
    SecurityDialog(DialogType dialogType, AccessType accessType,
                CertVerifier certVerifier) {
        this(dialogType, accessType, null, certVerifier, null, null);
    }

    /**
     * Create a SecurityDialog to display some sort of access warning
     * with more information
     */
    SecurityDialog(DialogType dialogType, AccessType accessType,
                        JNLPFile file, Object[] extras) {
        this(dialogType, accessType, file, null, null, extras);
    }

    /**
     * Create a SecurityWarningDailog to display information about a single
     * certificate
     */
    SecurityDialog(DialogType dialogType, X509Certificate c) {
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
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Displays CertPath information in a readable table format.
     *
     * @param certVerifier the JarCertVerifier used to verify this application
     * @param parent the parent option pane
     */
    public static void showCertInfoDialog(CertVerifier certVerifier,
                SecurityDialog parent) {
        SecurityDialog dialog = new SecurityDialog(DialogType.CERT_INFO,
                        null, null, certVerifier);
        dialog.setLocationRelativeTo(parent);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        dialog.dispose();
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
        dialog.setLocationRelativeTo(parent);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private void initDialog() {
        String dialogTitle = "";
        if (dialogType == DialogType.CERT_WARNING) {
            if (accessType == AccessType.VERIFIED)
                dialogTitle = "Security Approval Required";
            else
                dialogTitle = "Security Warning";
        } else if (dialogType == DialogType.MORE_INFO)
            dialogTitle = "More Information";
        else if (dialogType == DialogType.CERT_INFO)
            dialogTitle = "Details - Certificate";
        else if (dialogType == DialogType.ACCESS_WARNING)
            dialogTitle = "Security Warning";
        else if (dialogType == DialogType.APPLET_WARNING)
            dialogTitle = "Applet Warning";
        else if (dialogType == DialogType.PARTIALLYSIGNED_WARNING)
            dialogTitle = "Security Warning";
        else if (dialogType == DialogType.AUTHENTICATION)
            dialogTitle = "Authentication Required";

        setTitle(dialogTitle);
        setModalityType(ModalityType.MODELESS);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        installPanel();

        pack();
        centerDialog(this);

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
                    dialog.setResizable(true);
                    dialog.setValue(null);
                }
            }
        };
        addWindowListener(adapter);
        addWindowFocusListener(adapter);
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

    /**
     * Adds the appropriate JPanel to this Dialog, based on {@link DialogType}.
     */
    private void installPanel() {

        if (dialogType == DialogType.CERT_WARNING)
            panel = new CertWarningPane(this, this.certVerifier, (SecurityDelegate) extras[0]);
        else if (dialogType == DialogType.MORE_INFO)
            panel = new MoreInfoPane(this, this.certVerifier);
        else if (dialogType == DialogType.CERT_INFO)
            panel = new CertsInfoPane(this, this.certVerifier);
        else if (dialogType == DialogType.SINGLE_CERT_INFO)
            panel = new SingleCertInfoPane(this, this.certVerifier);
        else if (dialogType == DialogType.ACCESS_WARNING)
            panel = new AccessWarningPane(this, extras, this.certVerifier);
        else if (dialogType == DialogType.APPLET_WARNING)
            panel = new AppletWarningPane(this, this.certVerifier);
        else if (dialogType == DialogType.PARTIALLYSIGNED_WARNING)
            panel = AppTrustWarningDialog.partiallySigned(this, file, (SecurityDelegate) extras[0]);
        else if (dialogType == DialogType.UNSIGNED_WARNING) // Only necessary for applets on 'high security' or above
            panel = AppTrustWarningDialog.unsigned(this, file);
        else if (dialogType == DialogType.AUTHENTICATION)
            panel = new PasswordAuthenticationPane(this, extras);
        else if (dialogType == DialogType.UNSIGNED_EAS_NO_PERMISSIONS_WARNING)
            panel = new MissingPermissionsAttributePanel(this, (String) extras[0], (String) extras[1]);
        else if (dialogType == DialogType.MISSING_ALACA)
            panel = new MissingALACAttributePanel(this, (String) extras[0], (String) extras[1], (String) extras[2]);
        else if (dialogType == DialogType.MATCHING_ALACA)
            panel = AppTrustWarningDialog.matchingAlaca(this, (JNLPFile) extras[0], (String) extras[1], (String) extras[2]);

        add(panel, BorderLayout.CENTER);
    }

    private static void centerDialog(JDialog dialog) {
        ScreenFinder.centerWindowsToCurrentScreen(dialog);
    }

    private void selectDefaultButton() {
        if (panel == null) {
            OutputController.getLogger().log(OutputController.Level.MESSAGE_ALL, "initial value panel is null");
        }
        panel.requestFocusOnDefaultButton();
    }

    public void setValue(Object value) {
        OutputController.getLogger().log("Setting value:" + value);
        this.value = value;
    }

    public Object getValue() {
        OutputController.getLogger().log("Returning value:" + value);
        return value;
    }

    /**
     * Called when the SecurityDialog is hidden - either because the user
     * made a choice (Ok, Cancel, etc) or closed the window
     */
    @Override
    public void dispose() {
        notifySelectionMade();
        super.dispose();
    }

    private final List<ActionListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Notify all the listeners that the user has made a decision using this
     * security dialog.
     */
    public void notifySelectionMade() {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(null);
        }
    }

    /**
     * Adds an {@link ActionListener} which will be notified if the user makes a
     * choice using this SecurityDialog. The listener should use {@link #getValue()}
     * to actually get the user's response.
     * @param listener another action listener to be listen to
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
    
    public boolean requiresSignedJNLPWarning()
    {
        return requiresSignedJNLPWarning;
    }

}
